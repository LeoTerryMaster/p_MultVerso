package l2.gameserver.instancemanager.games;

import l2.commons.dbutils.DbUtils;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.instancemanager.ServerVariables;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FishingChampionShipManager
{
	private static final Logger _log = LoggerFactory.getLogger(FishingChampionShipManager.class);
	private static final FishingChampionShipManager _instance = new FishingChampionShipManager();
	private final List<String> _playersName = new ArrayList<>();
	private final List<String> _fishLength = new ArrayList<>();
	private final List<String> _winPlayersName = new ArrayList<>();
	private final List<String> _winFishLength = new ArrayList<>();
	private final List<Fisher> _tmpPlayers = new ArrayList<>();
	private final List<Fisher> _winPlayers = new ArrayList<>();
	private long _enddate;
	private double _minFishLength;
	private boolean _needRefresh = true;
	
	private FishingChampionShipManager()
	{
		restoreData();
		refreshWinResult();
		recalculateMinLength();
		if(_enddate <= System.currentTimeMillis())
		{
			_enddate = System.currentTimeMillis();
			new finishChamp().run();
		}
		else
		{
			ThreadPoolManager.getInstance().schedule(new finishChamp(), _enddate - System.currentTimeMillis());
		}
	}
	
	public static final FishingChampionShipManager getInstance()
	{
		return _instance;
	}
	
	private void setEndOfChamp()
	{
		Calendar finishtime = Calendar.getInstance();
		finishtime.setTimeInMillis(_enddate);
		finishtime.set(12, 0);
		finishtime.set(13, 0);
		finishtime.add(5, 6);
		finishtime.set(7, 3);
		finishtime.set(11, 19);
		_enddate = finishtime.getTimeInMillis();
	}
	
	private void restoreData()
	{
		_enddate = ServerVariables.getLong("fishChampionshipEnd", 0);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `PlayerName`, `fishLength`, `rewarded` FROM fishing_championship");
			ResultSet rs = statement.executeQuery();
			while(rs.next())
			{
				int rewarded = rs.getInt("rewarded");
				if(rewarded == 0)
				{
					_tmpPlayers.add(new Fisher(rs.getString("PlayerName"), rs.getDouble("fishLength"), 0));
				}
				if(rewarded <= 0)
					continue;
				_winPlayers.add(new Fisher(rs.getString("PlayerName"), rs.getDouble("fishLength"), rewarded));
			}
			rs.close();
		}
		catch(SQLException e)
		{
			_log.warn("Exception: can't get fishing championship info: " + e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public synchronized void newFish(Player pl, int lureId)
	{
		if(!Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
		{
			return;
		}
		double p1 = Rnd.get(60, 80);
		long diff;
		if(p1 < 90.0 && lureId > 8484 && lureId < 8486 && (diff = Math.round(90.0 - p1)) > 1)
		{
			p1 += (double) Rnd.get(1, diff);
		}
		double len = (double) Rnd.get(100, 999) / 1000.0 + p1;
		if(_tmpPlayers.size() < 5)
		{
			for(Fisher fisher : _tmpPlayers)
			{
				if(!fisher.getName().equalsIgnoreCase(pl.getName()))
					continue;
				if(fisher.getLength() < len)
				{
					fisher.setLength(len);
					pl.sendMessage(new CustomMessage("l2p.gameserver.instancemanager.games.FishingChampionShipManager.ResultImproveOn", pl));
					recalculateMinLength();
				}
				return;
			}
			_tmpPlayers.add(new Fisher(pl.getName(), len, 0));
			pl.sendMessage(new CustomMessage("l2p.gameserver.instancemanager.games.FishingChampionShipManager.YouInAPrizeList", pl));
			recalculateMinLength();
		}
		else if(_minFishLength < len)
		{
			for(Fisher fisher : _tmpPlayers)
			{
				if(!fisher.getName().equalsIgnoreCase(pl.getName()))
					continue;
				if(fisher.getLength() < len)
				{
					fisher.setLength(len);
					pl.sendMessage(new CustomMessage("l2p.gameserver.instancemanager.games.FishingChampionShipManager.ResultImproveOn", pl));
					recalculateMinLength();
				}
				return;
			}
			Fisher minFisher = null;
			double minLen = 99999.0;
			for(Fisher fisher : _tmpPlayers)
			{
				if(fisher.getLength() >= minLen)
					continue;
				minFisher = fisher;
				minLen = minFisher.getLength();
			}
			_tmpPlayers.remove(minFisher);
			_tmpPlayers.add(new Fisher(pl.getName(), len, 0));
			pl.sendMessage(new CustomMessage("l2p.gameserver.instancemanager.games.FishingChampionShipManager.YouInAPrizeList", pl));
			recalculateMinLength();
		}
	}
	
	private void recalculateMinLength()
	{
		double minLen = 99999.0;
		for(Fisher fisher : _tmpPlayers)
		{
			if(fisher.getLength() >= minLen)
				continue;
			minLen = fisher.getLength();
		}
		_minFishLength = minLen;
	}
	
	public long getTimeRemaining()
	{
		return (_enddate - System.currentTimeMillis()) / 60000;
	}
	
	public String getWinnerName(int par)
	{
		if(_winPlayersName.size() >= par)
		{
			return _winPlayersName.get(par - 1);
		}
		return "—";
	}
	
	public String getCurrentName(int par)
	{
		if(_playersName.size() >= par)
		{
			return _playersName.get(par - 1);
		}
		return "—";
	}
	
	public String getFishLength(int par)
	{
		if(_winFishLength.size() >= par)
		{
			return _winFishLength.get(par - 1);
		}
		return "0";
	}
	
	public String getCurrentFishLength(int par)
	{
		if(_fishLength.size() >= par)
		{
			return _fishLength.get(par - 1);
		}
		return "0";
	}
	
	public void getReward(Player pl)
	{
		String filename = "fisherman/championship/getReward.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(pl.getObjectId());
		html.setFile(filename);
		pl.sendPacket(html);
		for(Fisher fisher : _winPlayers)
		{
			if(!fisher._name.equalsIgnoreCase(pl.getName()) || fisher.getRewardType() == 2)
				continue;
			int rewardCnt = 0;
			block8:
			for(int x = 0;x < _winPlayersName.size();++x)
			{
				if(!_winPlayersName.get(x).equalsIgnoreCase(pl.getName()))
					continue;
				switch(x)
				{
					case 0:
					{
						rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_1;
						continue block8;
					}
					case 1:
					{
						rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_2;
						continue block8;
					}
					case 2:
					{
						rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_3;
						continue block8;
					}
					case 3:
					{
						rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_4;
						continue block8;
					}
					case 4:
					{
						rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_5;
					}
				}
			}
			fisher.setRewardType(2);
			if(rewardCnt <= 0)
				continue;
			SystemMessage smsg = new SystemMessage(53).addItemName(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM).addNumber(rewardCnt);
			pl.sendPacket(smsg);
			pl.getInventory().addItem(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM, rewardCnt);
			pl.sendItemList(false);
		}
	}
	
	public void showMidResult(Player pl)
	{
		if(_needRefresh)
		{
			refreshResult();
			ThreadPoolManager.getInstance().schedule(new needRefresh(), 60000);
		}
		NpcHtmlMessage html = new NpcHtmlMessage(pl.getObjectId());
		String filename = "fisherman/championship/MidResult.htm";
		html.setFile(filename);
		String str = null;
		for(int x = 1;x <= 5;++x)
		{
			str = str + "<tr><td width=70 align=center>" + x + (pl.isLangRus() ? " Место:" : " Position:") + "</td>";
			str = str + "<td width=110 align=center>" + getCurrentName(x) + "</td>";
			str = str + "<td width=80 align=center>" + getCurrentFishLength(x) + "</td></tr>";
		}
		html.replace("%TABLE%", str);
		html.replace("%prizeItem%", ItemHolder.getInstance().getTemplate(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM).getName());
		html.replace("%prizeFirst%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_1));
		html.replace("%prizeTwo%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_2));
		html.replace("%prizeThree%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_3));
		html.replace("%prizeFour%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_4));
		html.replace("%prizeFive%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_5));
		pl.sendPacket(html);
	}
	
	public void showChampScreen(Player pl, NpcInstance npc)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(pl.getObjectId());
		String filename = "fisherman/championship/champScreen.htm";
		html.setFile(filename);
		String str = null;
		for(int x = 1;x <= 5;++x)
		{
			str = str + "<tr><td width=70 align=center>" + x + (pl.isLangRus() ? " Место:" : " Position:") + "</td>";
			str = str + "<td width=110 align=center>" + getWinnerName(x) + "</td>";
			str = str + "<td width=80 align=center>" + getFishLength(x) + "</td></tr>";
		}
		html.replace("%TABLE%", str);
		html.replace("%prizeItem%", ItemHolder.getInstance().getTemplate(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM).getName());
		html.replace("%prizeFirst%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_1));
		html.replace("%prizeTwo%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_2));
		html.replace("%prizeThree%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_3));
		html.replace("%prizeFour%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_4));
		html.replace("%prizeFive%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_5));
		html.replace("%refresh%", String.valueOf(getTimeRemaining()));
		html.replace("%objectId%", String.valueOf(npc.getObjectId()));
		pl.sendPacket(html);
	}
	
	public void shutdown()
	{
		ServerVariables.set("fishChampionshipEnd", _enddate);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM fishing_championship");
			statement.execute();
			statement.close();
			for(Fisher fisher : _winPlayers)
			{
				statement = con.prepareStatement("INSERT INTO fishing_championship(PlayerName,fishLength,rewarded) VALUES (?,?,?)");
				statement.setString(1, fisher.getName());
				statement.setDouble(2, fisher.getLength());
				statement.setInt(3, fisher.getRewardType());
				statement.execute();
				statement.close();
			}
			for(Fisher fisher : _tmpPlayers)
			{
				statement = con.prepareStatement("INSERT INTO fishing_championship(PlayerName,fishLength,rewarded) VALUES (?,?,?)");
				statement.setString(1, fisher.getName());
				statement.setDouble(2, fisher.getLength());
				statement.setInt(3, 0);
				statement.execute();
				statement.close();
			}
		}
		catch(SQLException e)
		{
			_log.warn("Exception: can't update player vitality: " + e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	private synchronized void refreshResult()
	{
		_needRefresh = false;
		_playersName.clear();
		_fishLength.clear();
		for(int x = 0;x <= _tmpPlayers.size() - 1;++x)
		{
			for(int y = 0;y <= _tmpPlayers.size() - 2;++y)
			{
				Fisher fisher1 = _tmpPlayers.get(y);
				Fisher fisher2 = _tmpPlayers.get(y + 1);
				if(fisher1.getLength() >= fisher2.getLength())
					continue;
				_tmpPlayers.set(y, fisher2);
				_tmpPlayers.set(y + 1, fisher1);
			}
		}
		for(int x = 0;x <= _tmpPlayers.size() - 1;++x)
		{
			_playersName.add(_tmpPlayers.get(x)._name);
			_fishLength.add(String.valueOf(_tmpPlayers.get(x).getLength()));
		}
	}
	
	private void refreshWinResult()
	{
		_winPlayersName.clear();
		_winFishLength.clear();
		for(int x = 0;x <= _winPlayers.size() - 1;++x)
		{
			for(int y = 0;y <= _winPlayers.size() - 2;++y)
			{
				Fisher fisher1 = _winPlayers.get(y);
				Fisher fisher2 = _winPlayers.get(y + 1);
				if(fisher1.getLength() >= fisher2.getLength())
					continue;
				_winPlayers.set(y, fisher2);
				_winPlayers.set(y + 1, fisher1);
			}
		}
		for(int x = 0;x <= _winPlayers.size() - 1;++x)
		{
			_winPlayersName.add(_winPlayers.get(x)._name);
			_winFishLength.add(String.valueOf(_winPlayers.get(x).getLength()));
		}
	}
	
	private class Fisher
	{
		private double _length;
		private String _name;
		private int _reward;
		
		public Fisher(String name, double length, int rewardType)
		{
			_length = 0.0;
			_reward = 0;
			setName(name);
			setLength(length);
			setRewardType(rewardType);
		}
		
		public String getName()
		{
			return _name;
		}
		
		public void setName(String value)
		{
			_name = value;
		}
		
		public int getRewardType()
		{
			return _reward;
		}
		
		public void setRewardType(int value)
		{
			_reward = value;
		}
		
		public double getLength()
		{
			return _length;
		}
		
		public void setLength(double value)
		{
			_length = value;
		}
	}
	
	private class needRefresh extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			_needRefresh = true;
		}
	}
	
	private class finishChamp extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			_winPlayers.clear();
			for(Fisher fisher : _tmpPlayers)
			{
				fisher.setRewardType(1);
				_winPlayers.add(fisher);
			}
			_tmpPlayers.clear();
			refreshWinResult();
			setEndOfChamp();
			shutdown();
			_log.info("Fishing Championship Manager : start new event period.");
			ThreadPoolManager.getInstance().schedule(new finishChamp(), _enddate - System.currentTimeMillis());
		}
	}
}