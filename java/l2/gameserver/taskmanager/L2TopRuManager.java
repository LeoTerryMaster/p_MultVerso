package l2.gameserver.taskmanager;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class L2TopRuManager
{
	private static final Logger _log = LoggerFactory.getLogger(L2TopRuManager.class);
	private static final DateFormat DATE_FORMAT;
	private static final String USERAGENT;
	private static final Map<Integer, Long> _voteDateCache;
	private static L2TopRuManager _instance;
	
	static
	{
		USERAGENT = "Mozilla/5.0 (SunOS; 5.10; amd64; U) Java HotSpot(TM) 64-Bit Server VM/16.2-b04";
		DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		_voteDateCache = new ConcurrentHashMap<>();
	}
	
	private Pattern _webPattern;
	private Pattern _smsPattern;
	
	private L2TopRuManager()
	{
		if(Config.L2TOPRU_DELAY < 1)
		{
			return;
		}
		_log.info("L2TopRuManager: Initializing.");
		_webPattern = Pattern.compile("^([\\d-]+\\s[\\d:]+)\\s+(?:" + Config.L2TOPRU_PREFIX + "-)*([^\\s]+)$", 8);
		_smsPattern = Pattern.compile("^([\\d-]+\\s[\\d:]+)\\s+(?:" + Config.L2TOPRU_PREFIX + "-)*([^\\s]+)\\s+x(\\d{1,2})$", 8);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new L2TopRuTask(), Config.L2TOPRU_DELAY, Config.L2TOPRU_DELAY);
	}
	
	public static L2TopRuManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new L2TopRuManager();
		}
		return _instance;
	}
	
	protected ArrayList<L2TopRuVote> filterVotes(ArrayList<L2TopRuVote> votes)
	{
		ArrayList<L2TopRuVote> result = new ArrayList<>();
		HashMap<String, Integer> chars = new HashMap<>();
		Connection con = null;
		Statement stmt = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			stmt = con.createStatement();
			rset = stmt.executeQuery("SELECT `obj_Id`,`char_name` FROM `characters`");
			while(rset.next())
			{
				chars.put(rset.getString("char_name"), rset.getInt("obj_Id"));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, stmt, rset);
		}
		
		for(L2TopRuVote vote : votes)
		{
			if(!chars.containsKey(vote.charname))
				continue;
			int charObjId = chars.get(vote.charname).intValue();
			if(!isRewardReq(charObjId, vote.datetime))
				continue;
			
			vote.char_obj_id = charObjId;
			result.add(vote);
		}
		return result;
	}
	
	private boolean isRewardReq(int charObjId, long date)
	{
		block17:
		{
			long lastDate = 0;
			Connection con = null;
			PreparedStatement pstmt = null;
			if(_voteDateCache.containsKey(charObjId))
			{
				lastDate = _voteDateCache.get(charObjId);
				if(date > lastDate)
				{
					_voteDateCache.put(charObjId, date);
					try
					{
						con = DatabaseFactory.getInstance().getConnection();
						pstmt = con.prepareStatement("REPLACE DELAYED INTO `l2topru_votes`(`obj_Id`,`last_vote`) VALUES (?,?)");
						pstmt.setInt(1, charObjId);
						pstmt.setLong(2, date);
						pstmt.executeUpdate();
						DbUtils.closeQuietly(con, pstmt);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					return true;
				}
			}
			else
			{
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					pstmt = con.prepareStatement("SELECT `obj_Id`,`last_vote` FROM `l2topru_votes` WHERE `obj_Id` = ?");
					pstmt.setInt(1, charObjId);
					ResultSet rset = pstmt.executeQuery();
					if(rset.next())
					{
						lastDate = rset.getLong("last_vote");
						DbUtils.closeQuietly(pstmt, rset);
						if(date > lastDate)
						{
							_voteDateCache.put(charObjId, date);
							try
							{
								pstmt = con.prepareStatement("REPLACE DELAYED INTO `l2topru_votes`(`obj_Id`,`last_vote`) VALUES (?,?)");
								pstmt.setInt(1, charObjId);
								pstmt.setLong(2, date);
								pstmt.executeUpdate();
								DbUtils.closeQuietly(con, pstmt);
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
							return true;
						}
						_voteDateCache.put(charObjId, lastDate);
						break block17;
					}
					DbUtils.closeQuietly(pstmt, rset);
					_voteDateCache.put(charObjId, date);
					try
					{
						pstmt = con.prepareStatement("REPLACE DELAYED INTO `l2topru_votes`(`obj_Id`,`last_vote`) VALUES (?,?)");
						pstmt.setInt(1, charObjId);
						pstmt.setLong(2, date);
						pstmt.executeUpdate();
						DbUtils.closeQuietly(con, pstmt);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					return true;
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					DbUtils.closeQuietly(con);
				}
			}
		}
		return false;
	}
	
	private void giveItem(int charObjId, int itemId, int itemCount)
	{
		if(charObjId < 1)
		{
			return;
		}
		Player player = GameObjectsStorage.getPlayer(charObjId);
		if(player != null)
		{
			player.sendMessage(new CustomMessage("l2.gameserver.taskmanager.L2TopRuManager", player).addItemName(player.getInventory().addItem(itemId, itemCount)));
		}
		else
		{
			ItemInstance newItem = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
			newItem.setCount(itemCount);
			newItem.setOwnerId(charObjId);
			newItem.setLocation(ItemInstance.ItemLocation.INVENTORY);
			newItem.save();
		}
	}
	
	private void rewardVotes(ArrayList<L2TopRuVote> votes)
	{
		for(L2TopRuVote vote : votes)
		{
			switch(vote.type)
			{
				case WEB:
				{
					_log.info("L2TopRuManager: Rewarding " + vote);
					giveItem(vote.char_obj_id, Config.L2TOPRU_WEB_REWARD_ITEMID, Config.L2TOPRU_WEB_REWARD_ITEMCOUNT);
					break;
				}
				case SMS:
				{
					_log.info("L2TopRuManager: Rewarding " + vote);
					if(!Config.L2TOPRU_SMS_REWARD_VOTE_MULTI)
						break;
					giveItem(vote.char_obj_id, Config.L2TOPRU_SMS_REWARD_ITEMID, Config.L2TOPRU_SMS_REWARD_VOTE_MULTI ? Config.L2TOPRU_SMS_REWARD_ITEMCOUNT * vote.count : Config.L2TOPRU_SMS_REWARD_ITEMCOUNT);
				}
			}
		}
	}
	
	private ArrayList<L2TopRuVote> getAllVotes()
	{
		ArrayList<L2TopRuVote> result = new ArrayList<>();
		try
		{
			String dateTimeStr;
			String nameStr;
			L2TopRuVote vote;
			Matcher m = _webPattern.matcher(getPage(Config.L2TOPRU_WEB_VOTE_URL));
			while(m.find())
			{
				dateTimeStr = m.group(1);
				nameStr = m.group(2);
				if(!Util.isMatchingRegexp(nameStr, Config.CNAME_TEMPLATE))
					continue;
				vote = new L2TopRuVote(dateTimeStr, nameStr);
				result.add(vote);
			}
			m = _smsPattern.matcher(getPage(Config.L2TOPRU_SMS_VOTE_URL));
			while(m.find())
			{
				dateTimeStr = m.group(1);
				nameStr = m.group(2);
				String mulStr = m.group(3);
				if(!Util.isMatchingRegexp(nameStr, Config.CNAME_TEMPLATE))
					continue;
				vote = new L2TopRuVote(dateTimeStr, nameStr, mulStr);
				result.add(vote);
			}
			Collections.sort(result, new L2TopRuVoteComparator());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	private void tick()
	{
		rewardVotes(filterVotes(getAllVotes()));
	}
	
	private String getPage(String uri) throws Exception
	{
		try
		{
			URL url = new URL(uri);
			URLConnection conn = url.openConnection();
			conn.addRequestProperty("Host", url.getHost());
			conn.addRequestProperty("Accept", "*/*");
			conn.addRequestProperty("Connection", "close");
			conn.addRequestProperty("User-Agent", USERAGENT);
			conn.setConnectTimeout(30000);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "cp1251"));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while((line = in.readLine()) != null)
			{
				builder.append(line).append("\n");
			}
			return builder.toString();
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	private enum L2TopRuVoteType
	{
		WEB,
		SMS;
	}
	
	private final class L2TopRuVoteComparator<T> implements Comparator<L2TopRuVote>
	{
		@Override
		public int compare(L2TopRuVote o, L2TopRuVote o1)
		{
			if(o.datetime == o1.datetime)
			{
				return 0;
			}
			if(o.datetime < o1.datetime)
			{
				return Integer.MIN_VALUE;
			}
			if(o.datetime > o1.datetime)
			{
				return Integer.MAX_VALUE;
			}
			return -1;
		}
	}
	
	private class L2TopRuVote
	{
		public long datetime;
		public String charname;
		public int count;
		public int char_obj_id;
		public L2TopRuVoteType type;
		
		public L2TopRuVote(String date, String charName, String itemcount) throws Exception
		{
			char_obj_id = -1;
			datetime = DATE_FORMAT.parse(date).getTime() / 1000;
			count = Byte.parseByte(itemcount);
			charname = charName;
			type = L2TopRuVoteType.SMS;
		}
		
		public L2TopRuVote(String date, String charName) throws Exception
		{
			char_obj_id = -1;
			datetime = DATE_FORMAT.parse(date).getTime() / 1000;
			charname = charName;
			count = 1;
			type = L2TopRuVoteType.WEB;
		}
		
		@Override
		public String toString()
		{
			return charname + "-" + count + "[" + char_obj_id + "(" + datetime + "|" + type.name() + ")]";
		}
	}
	
	private class L2TopRuTask implements Runnable
	{
		@Override
		public void run()
		{
			getInstance().tick();
		}
	}
}