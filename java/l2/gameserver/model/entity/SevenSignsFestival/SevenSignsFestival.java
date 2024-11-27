package l2.gameserver.model.entity.SevenSignsFestival;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.Experience;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.s2c.PledgeShowInfoUpdate;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.tables.ClanTable;
import l2.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class SevenSignsFestival
{
	public static final int FESTIVAL_MANAGER_START = 120000;
	public static final int FESTIVAL_LENGTH = 1080000;
	public static final int FESTIVAL_CYCLE_LENGTH = 2280000;
	public static final int FESTIVAL_SIGNUP_TIME = 1200000;
	public static final int FESTIVAL_FIRST_SPAWN = 120000;
	public static final int FESTIVAL_FIRST_SWARM = 300000;
	public static final int FESTIVAL_SECOND_SPAWN = 540000;
	public static final int FESTIVAL_SECOND_SWARM = 720000;
	public static final int FESTIVAL_CHEST_SPAWN = 900000;
	public static final int FESTIVAL_COUNT = 5;
	public static final int FESTIVAL_LEVEL_MAX_31 = 0;
	public static final int FESTIVAL_LEVEL_MAX_42 = 1;
	public static final int FESTIVAL_LEVEL_MAX_53 = 2;
	public static final int FESTIVAL_LEVEL_MAX_64 = 3;
	public static final int FESTIVAL_LEVEL_MAX_NONE = 4;
	public static final int[] FESTIVAL_LEVEL_SCORES;
	public static final int FESTIVAL_BLOOD_OFFERING = 5901;
	public static final int FESTIVAL_OFFERING_VALUE = 1;
	private static final Logger _log = LoggerFactory.getLogger(SevenSignsFestival.class);
	private static final SevenSigns _signsInstance;
	private static SevenSignsFestival _instance;
	private static boolean _festivalInitialized;
	private static long[] _accumulatedBonuses;
	private static Map<Integer, Long> _dawnFestivalScores;
	private static Map<Integer, Long> _duskFestivalScores;
	
	static
	{
		_signsInstance = SevenSigns.getInstance();
		FESTIVAL_LEVEL_SCORES = new int[] {60, 70, 100, 120, 150};
	}
	
	private final Map<Integer, Map<Integer, StatsSet>> _festivalData;
	
	public SevenSignsFestival()
	{
		_accumulatedBonuses = new long[5];
		_dawnFestivalScores = new ConcurrentHashMap<>();
		_duskFestivalScores = new ConcurrentHashMap<>();
		_festivalData = new ConcurrentHashMap<>();
		restoreFestivalData();
	}
	
	public static SevenSignsFestival getInstance()
	{
		if(_instance == null)
		{
			_instance = new SevenSignsFestival();
		}
		return _instance;
	}
	
	public static String getFestivalName(int festivalID)
	{
		switch(festivalID)
		{
			case 0:
			{
				return "31";
			}
			case 1:
			{
				return "42";
			}
			case 2:
			{
				return "53";
			}
			case 3:
			{
				return "64";
			}
		}
		return "No Level Limit";
	}
	
	public static int getMaxLevelForFestival(int festivalId)
	{
		switch(festivalId)
		{
			case 0:
			{
				return 31;
			}
			case 1:
			{
				return 42;
			}
			case 2:
			{
				return 53;
			}
			case 3:
			{
				return 64;
			}
		}
		return Experience.getMaxLevel();
	}
	
	public static int getStoneCount(int festivalId, int stoneId)
	{
		switch(festivalId)
		{
			case 0:
			{
				if(stoneId == 6360)
				{
					return 900;
				}
				if(stoneId == 6361)
				{
					return 520;
				}
				return 270;
			}
			case 1:
			{
				if(stoneId == 6360)
				{
					return 1500;
				}
				if(stoneId == 6361)
				{
					return 900;
				}
				return 450;
			}
			case 2:
			{
				if(stoneId == 6360)
				{
					return 3000;
				}
				if(stoneId == 6361)
				{
					return 1500;
				}
				return 900;
			}
			case 3:
			{
				if(stoneId == 6360)
				{
					return 1500;
				}
				if(stoneId == 6361)
				{
					return 2700;
				}
				return 1350;
			}
			case 4:
			{
				if(stoneId == 6360)
				{
					return 6000;
				}
				if(stoneId == 6361)
				{
					return 3600;
				}
				return 1800;
			}
		}
		return 0;
	}
	
	public static String implodeString(List<?> strArray)
	{
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while(i < strArray.size())
		{
			Object o = strArray.get(i);
			if(o instanceof Player)
			{
				sb.append(((Player) o).getName());
			}
			else
			{
				sb.append(o);
			}
			if(++i >= strArray.size())
				continue;
			sb.append(",");
		}
		return sb.toString();
	}
	
	private void restoreFestivalData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT festivalId, cabal, cycle, date, score, members, names FROM seven_signs_festival");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int cycle = _signsInstance.getCurrentCycle();
				int festivalId = rset.getInt("festivalId");
				int cabal = SevenSigns.getCabalNumber(rset.getString("cabal"));
				StatsSet festivalDat = new StatsSet();
				festivalDat.set("festivalId", festivalId);
				festivalDat.set("cabal", cabal);
				festivalDat.set("cycle", cycle);
				festivalDat.set("date", rset.getString("date"));
				festivalDat.set("score", rset.getInt("score"));
				festivalDat.set("members", rset.getString("members"));
				festivalDat.set("names", rset.getString("names"));
				if(cabal == 2)
				{
					festivalId += 5;
				}
				Map<Integer, StatsSet> tempData;
				if((tempData = _festivalData.get(cycle)) == null)
				{
					tempData = new TreeMap<>();
				}
				tempData.put(festivalId, festivalDat);
				_festivalData.put(cycle, tempData);
			}
			DbUtils.close(statement, rset);
			StringBuilder query = new StringBuilder("SELECT festival_cycle, ");
			int i;
			for(i = 0;i < 4;++i)
			{
				query.append("accumulated_bonus" + String.valueOf(i) + ", ");
			}
			query.append("accumulated_bonus" + 4 + " ");
			query.append("FROM seven_signs_status");
			statement = con.prepareStatement(query.toString());
			rset = statement.executeQuery();
			while(rset.next())
			{
				for(i = 0;i < 5;++i)
				{
					_accumulatedBonuses[i] = rset.getInt("accumulated_bonus" + String.valueOf(i));
				}
			}
		}
		catch(SQLException e)
		{
			_log.error("SevenSignsFestival: Failed to load configuration: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public synchronized void saveFestivalData(boolean updateSettings)
	{
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE seven_signs_festival SET date=?, score=?, members=?, names=? WHERE cycle=? AND cabal=? AND festivalId=?");
			statement2 = con.prepareStatement("INSERT INTO seven_signs_festival (festivalId, cabal, cycle, date, score, members, names) VALUES (?,?,?,?,?,?,?)");
			for(Map<Integer, StatsSet> currCycleData : _festivalData.values())
			{
				for(StatsSet festivalDat : currCycleData.values())
				{
					int festivalCycle = festivalDat.getInteger("cycle");
					int festivalId = festivalDat.getInteger("festivalId");
					String cabal = SevenSigns.getCabalShortName(festivalDat.getInteger("cabal"));
					statement.setLong(1, Long.valueOf(festivalDat.getString("date")));
					statement.setInt(2, festivalDat.getInteger("score"));
					statement.setString(3, festivalDat.getString("members"));
					statement.setString(4, festivalDat.getString("names", ""));
					statement.setInt(5, festivalCycle);
					statement.setString(6, cabal);
					statement.setInt(7, festivalId);
					boolean update;
					boolean bl = update = statement.executeUpdate() > 0;
					if(update)
						continue;
					statement2.setInt(1, festivalId);
					statement2.setString(2, cabal);
					statement2.setInt(3, festivalCycle);
					statement2.setLong(4, Long.valueOf(festivalDat.getString("date")));
					statement2.setInt(5, festivalDat.getInteger("score"));
					statement2.setString(6, festivalDat.getString("members"));
					statement2.setString(7, festivalDat.getString("names", ""));
					statement2.execute();
				}
			}
		}
		catch(Exception e)
		{
			_log.error("SevenSignsFestival: Failed to save configuration!", e);
		}
		finally
		{
			DbUtils.closeQuietly(statement2);
			DbUtils.closeQuietly(con, statement);
		}
		if(updateSettings)
		{
			_signsInstance.saveSevenSignsData(0, true);
		}
	}
	
	public void rewardHighestRanked()
	{
		for(int i = 0;i < 5;++i)
		{
			StatsSet overallData = getOverallHighestScoreData(i);
			if(overallData == null)
				continue;
			String[] partyMembers = overallData.getString("members").split(",");
			for(String partyMemberId : partyMembers)
			{
				addReputationPointsForPartyMemberClan(partyMemberId);
			}
		}
	}
	
	private void addReputationPointsForPartyMemberClan(String playerId)
	{
		Player player = GameObjectsStorage.getPlayer(Integer.parseInt(playerId));
		if(player != null)
		{
			if(player.getClan() != null)
			{
				player.getClan().incReputation(100, true, "SevenSignsFestival");
				SystemMessage sm = new SystemMessage(1775);
				sm.addName(player);
				sm.addNumber(100);
				player.getClan().broadcastToOnlineMembers(sm);
			}
		}
		else
		{
			Connection con = null;
			PreparedStatement statement = null;
			ResultSet rset = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT char_name, clanid FROM characters WHERE obj_Id = ?");
				statement.setString(1, playerId);
				rset = statement.executeQuery();
				Clan clan;
				int clanId;
				if(rset.next() && (clanId = rset.getInt("clanid")) > 0 && (clan = ClanTable.getInstance().getClan(clanId)) != null)
				{
					clan.incReputation(100, true, "SevenSignsFestival");
					clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
					SystemMessage sm = new SystemMessage(1775);
					sm.addString(rset.getString("char_name"));
					sm.addNumber(100);
					clan.broadcastToOnlineMembers(sm);
				}
			}
			catch(Exception e)
			{
				_log.warn("could not get clan name of " + playerId + ": " + e);
				_log.error("", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement, rset);
			}
		}
	}
	
	public void resetFestivalData(boolean updateSettings)
	{
		for(int i = 0;i < 5;++i)
		{
			_accumulatedBonuses[i] = 0;
		}
		_dawnFestivalScores.clear();
		_duskFestivalScores.clear();
		TreeMap<Integer, StatsSet> newData = new TreeMap<>();
		for(int i = 0;i < 10;++i)
		{
			int festivalId = i;
			if(i >= 5)
			{
				festivalId -= 5;
			}
			StatsSet tempStats = new StatsSet();
			tempStats.set("festivalId", festivalId);
			tempStats.set("cycle", _signsInstance.getCurrentCycle());
			tempStats.set("date", "0");
			tempStats.set("score", 0);
			tempStats.set("members", "");
			if(i >= 5)
			{
				tempStats.set("cabal", 2);
			}
			else
			{
				tempStats.set("cabal", 1);
			}
			newData.put(i, tempStats);
		}
		_festivalData.put(_signsInstance.getCurrentCycle(), newData);
		saveFestivalData(updateSettings);
		for(Player onlinePlayer : GameObjectsStorage.getAllPlayers())
		{
			Functions.removeItem(onlinePlayer, 5901, Functions.getItemCount(onlinePlayer, 5901));
		}
		_log.info("SevenSignsFestival: Reinitialized engine for next competition period.");
	}
	
	public boolean isFestivalInitialized()
	{
		return _festivalInitialized;
	}
	
	public static void setFestivalInitialized(boolean isInitialized)
	{
		_festivalInitialized = isInitialized;
	}
	
	public String getTimeToNextFestivalStr()
	{
		if(_signsInstance.isSealValidationPeriod())
		{
			return "<font color=\"FF0000\">This is the Seal Validation period. Festivals will resume next week.</font>";
		}
		return "<font color=\"FF0000\">The next festival is ready to start.</font>";
	}
	
	public long getHighestScore(int oracle, int festivalId)
	{
		return getHighestScoreData(oracle, festivalId).getLong("score");
	}
	
	public StatsSet getHighestScoreData(int oracle, int festivalId)
	{
		int offsetId = festivalId;
		if(oracle == 2)
		{
			offsetId += 5;
		}
		StatsSet currData = null;
		try
		{
			currData = _festivalData.get(_signsInstance.getCurrentCycle()).get(offsetId);
		}
		catch(Exception e)
		{
			_log.info("SSF: Error while getting scores");
			_log.info("oracle=" + oracle + " festivalId=" + festivalId + " offsetId" + offsetId + " _signsCycle" + _signsInstance.getCurrentCycle());
			_log.info("_festivalData=" + _festivalData);
			_log.error("", e);
		}
		if(currData == null)
		{
			currData = new StatsSet();
			currData.set("score", 0);
			currData.set("members", "");
			_log.warn("SevenSignsFestival: Data missing for " + SevenSigns.getCabalName(oracle) + ", FestivalID = " + festivalId + " (Current Cycle " + _signsInstance.getCurrentCycle() + ")");
		}
		return currData;
	}
	
	public StatsSet getOverallHighestScoreData(int festivalId)
	{
		StatsSet result = null;
		int highestScore = 0;
		for(Map<Integer, StatsSet> currCycleData : _festivalData.values())
		{
			for(StatsSet currFestData : currCycleData.values())
			{
				int currFestID = currFestData.getInteger("festivalId");
				int festivalScore = currFestData.getInteger("score");
				if(currFestID != festivalId || festivalScore <= highestScore)
					continue;
				highestScore = festivalScore;
				result = currFestData;
			}
		}
		return result;
	}
	
	public boolean setFinalScore(Party party, int oracle, int festivalId, long offeringScore)
	{
		List<Integer> partyMemberIds = party.getPartyMembersObjIds();
		List<Player> partyMembers = party.getPartyMembers();
		long currDawnHighScore = getHighestScore(2, festivalId);
		long currDuskHighScore = getHighestScore(1, festivalId);
		long thisCabalHighScore;
		long otherCabalHighScore;
		if(oracle == 2)
		{
			thisCabalHighScore = currDawnHighScore;
			otherCabalHighScore = currDuskHighScore;
			_dawnFestivalScores.put(festivalId, offeringScore);
		}
		else
		{
			thisCabalHighScore = currDuskHighScore;
			otherCabalHighScore = currDawnHighScore;
			_duskFestivalScores.put(festivalId, offeringScore);
		}
		StatsSet currFestData = getHighestScoreData(oracle, festivalId);
		if(offeringScore > thisCabalHighScore)
		{
			currFestData.set("date", String.valueOf(System.currentTimeMillis()));
			currFestData.set("score", offeringScore);
			currFestData.set("members", implodeString(partyMemberIds));
			currFestData.set("names", implodeString(partyMembers));
			if(offeringScore > otherCabalHighScore)
			{
				_signsInstance.updateFestivalScore();
			}
			saveFestivalData(true);
			return true;
		}
		return false;
	}
	
	public long getAccumulatedBonus(int festivalId)
	{
		return _accumulatedBonuses[festivalId];
	}
	
	public void addAccumulatedBonus(int festivalId, int stoneType, long stoneAmount)
	{
		int eachStoneBonus = 0;
		switch(stoneType)
		{
			case 6360:
			{
				eachStoneBonus = 3;
				break;
			}
			case 6361:
			{
				eachStoneBonus = 5;
				break;
			}
			case 6362:
			{
				eachStoneBonus = 10;
			}
		}
		long[] arrl = _accumulatedBonuses;
		int n = festivalId;
		arrl[n] = arrl[n] + stoneAmount * (long) eachStoneBonus;
	}
	
	public void distribAccumulatedBonus()
	{
		int i;
		long[][] result = new long[5][];
		long draw_count = 0;
		long draw_score = 0;
		for(i = 0;i < 5;++i)
		{
			long duskHigh;
			long dawnHigh = getHighestScore(2, i);
			if(dawnHigh > (duskHigh = getHighestScore(1, i)))
			{
				result[i] = new long[] {2, dawnHigh};
				continue;
			}
			if(duskHigh > dawnHigh)
			{
				result[i] = new long[] {1, duskHigh};
				continue;
			}
			result[i] = new long[] {0, dawnHigh};
			++draw_count;
			draw_score += _accumulatedBonuses[i];
		}
		for(i = 0;i < 5;++i)
		{
			if(result[i][0] == 0)
				continue;
			StatsSet high = getHighestScoreData((int) result[i][0], i);
			String membersString = high.getString("members");
			long add = draw_count > 0 ? draw_score / draw_count : 0;
			String[] members = membersString.split(",");
			long count = (_accumulatedBonuses[i] + add) / (long) members.length;
			for(String pIdStr : members)
			{
				SevenSigns.getInstance().addPlayerStoneContrib(Integer.parseInt(pIdStr), 0, 0, count / 10);
			}
		}
	}
}