package l2.gameserver.instancemanager.games;

import l2.commons.dbutils.DbUtils;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

public class LotteryManager
{
	public static final long SECOND = 1000;
	public static final long MINUTE = 60000;
	private static final Logger _log;
	private static final String INSERT_LOTTERY = "INSERT INTO games(id, idnr, enddate, prize, newprize) VALUES (?, ?, ?, ?, ?)";
	private static final String UPDATE_PRICE = "UPDATE games SET prize=?, newprize=? WHERE id = 1 AND idnr = ?";
	private static final String UPDATE_LOTTERY = "UPDATE games SET finished=1, prize=?, newprize=?, number1=?, number2=?, prize1=?, prize2=?, prize3=? WHERE id=1 AND idnr=?";
	private static final String SELECT_LAST_LOTTERY = "SELECT idnr, prize, newprize, enddate, finished FROM games WHERE id = 1 ORDER BY idnr DESC LIMIT 1";
	private static final String SELECT_LOTTERY_ITEM = "SELECT items.enchant AS `enchant`, items_options.damaged AS `damaged` FROM items, items_options WHERE items_options.blessed = ? AND items.item_id = items_options.item_id AND items.item_type = 4442";
	private static final String SELECT_LOTTERY_TICKET = "SELECT number1, number2, prize1, prize2, prize3 FROM games WHERE id = 1 AND idnr = ?";
	private static LotteryManager _instance;
	
	static
	{
		_log = LoggerFactory.getLogger(LotteryManager.class);
	}
	
	protected int _number = 1;
	protected int _prize = Config.SERVICES_LOTTERY_PRIZE;
	protected boolean _isSellingTickets;
	protected boolean _isStarted;
	protected long _enddate = System.currentTimeMillis();
	
	public LotteryManager()
	{
		if(Config.SERVICES_ALLOW_LOTTERY)
		{
			new startLottery().run();
		}
	}
	
	public static LotteryManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new LotteryManager();
		}
		return _instance;
	}
	
	public void increasePrize(int count)
	{
		_prize += count;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE games SET prize=?, newprize=? WHERE id = 1 AND idnr = ?");
			statement.setInt(1, getPrize());
			statement.setInt(2, getPrize());
			statement.setInt(3, getId());
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.warn("Lottery: Could not increase current lottery prize: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	private boolean restoreLotteryData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT idnr, prize, newprize, enddate, finished FROM games WHERE id = 1 ORDER BY idnr DESC LIMIT 1");
			rset = statement.executeQuery();
			if(rset.next())
			{
				_number = rset.getInt("idnr");
				if(rset.getInt("finished") == 1)
				{
					++_number;
					_prize = rset.getInt("newprize");
				}
				else
				{
					_prize = rset.getInt("prize");
					_enddate = rset.getLong("enddate");
					if(_enddate <= System.currentTimeMillis() + 120000)
					{
						new finishLottery().run();
						boolean bl = false;
						return bl;
					}
					if(_enddate > System.currentTimeMillis())
					{
						_isStarted = true;
						ThreadPoolManager.getInstance().schedule(new finishLottery(), _enddate - System.currentTimeMillis());
						if(_enddate > System.currentTimeMillis() + 720000)
						{
							_isSellingTickets = true;
							ThreadPoolManager.getInstance().schedule(new stopSellingTickets(), _enddate - System.currentTimeMillis() - 600000);
						}
						boolean bl = false;
						return bl;
					}
				}
			}
		}
		catch(SQLException e)
		{
			_log.warn("Lottery: Could not restore lottery data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return true;
	}
	
	private void announceLottery()
	{
		if(Config.SERVICES_ALLOW_LOTTERY)
		{
			_log.info("Lottery: Starting ticket sell for lottery #" + getId() + ".");
		}
		_isSellingTickets = true;
		_isStarted = true;
		Announcements.getInstance().announceToAll("Lottery tickets are now available for Lucky Lottery #" + getId() + ".");
	}
	
	private void scheduleEndOfLottery()
	{
		Calendar finishtime = Calendar.getInstance();
		finishtime.setTimeInMillis(_enddate);
		finishtime.set(12, 0);
		finishtime.set(13, 0);
		if(finishtime.get(7) == 1)
		{
			finishtime.set(11, 19);
			_enddate = finishtime.getTimeInMillis();
			_enddate += 604800000;
		}
		else
		{
			finishtime.set(7, 1);
			finishtime.set(11, 19);
			_enddate = finishtime.getTimeInMillis();
		}
		ThreadPoolManager.getInstance().schedule(new stopSellingTickets(), _enddate - System.currentTimeMillis() - 600000);
		ThreadPoolManager.getInstance().schedule(new finishLottery(), _enddate - System.currentTimeMillis());
	}
	
	private void createNewLottery()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO games(id, idnr, enddate, prize, newprize) VALUES (?, ?, ?, ?, ?)");
			statement.setInt(1, 1);
			statement.setInt(2, getId());
			statement.setLong(3, getEndDate());
			statement.setInt(4, getPrize());
			statement.setInt(5, getPrize());
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.warn("Lottery: Could not store new lottery data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public int[] decodeNumbers(int enchant, int type2)
	{
		int val;
		int[] res = new int[5];
		int id = 0;
		int nr = 1;
		while(enchant > 0)
		{
			val = enchant / 2;
			if((double) val != (double) enchant / 2.0)
			{
				res[id++] = nr;
			}
			enchant /= 2;
			++nr;
		}
		nr = 17;
		while(type2 > 0)
		{
			val = type2 / 2;
			if((double) val != (double) type2 / 2.0)
			{
				res[id++] = nr;
			}
			type2 /= 2;
			++nr;
		}
		return res;
	}
	
	public int[] checkTicket(int id, int enchant, int type2)
	{
		int[] res = {0, 0};
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT number1, number2, prize1, prize2, prize3 FROM games WHERE id = 1 AND idnr = ?");
			statement.setInt(1, id);
			rset = statement.executeQuery();
			if(!rset.next())
				return res;
			int curenchant = rset.getInt("number1") & enchant;
			int curtype2 = rset.getInt("number2") & type2;
			if(curenchant == 0 && curtype2 == 0)
			{
				int[] arrn = res;
				return arrn;
			}
			int count = 0;
			for(int i = 1;i <= 16;++i)
			{
				int val = curenchant / 2;
				if((double) val != (double) curenchant / 2.0)
				{
					++count;
				}
				int val2;
				if((double) (val2 = curtype2 / 2) != (double) curtype2 / 2.0)
				{
					++count;
				}
				curenchant = val;
				curtype2 = val2;
			}
			switch(count)
			{
				case 0:
				{
					return res;
				}
				case 5:
				{
					res[0] = 1;
					res[1] = rset.getInt("prize1");
					return res;
				}
				case 4:
				{
					res[0] = 2;
					res[1] = rset.getInt("prize2");
					return res;
				}
				case 3:
				{
					res[0] = 3;
					res[1] = rset.getInt("prize3");
					return res;
				}
				default:
				{
					res[0] = 4;
					res[1] = 200;
					return res;
				}
			}
		}
		catch(SQLException e)
		{
			_log.warn("Lottery: Could not check lottery ticket #" + id + ": " + e);
			return res;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public int[] checkTicket(ItemInstance item)
	{
		return checkTicket(item.getBlessed(), item.getEnchantLevel(), item.getDamaged());
	}
	
	public boolean isSellableTickets()
	{
		return _isSellingTickets;
	}
	
	public boolean isStarted()
	{
		return _isStarted;
	}
	
	public int getId()
	{
		return _number;
	}
	
	public int getPrize()
	{
		return _prize;
	}
	
	public long getEndDate()
	{
		return _enddate;
	}
	
	private class finishLottery extends RunnableImpl
	{
		protected finishLottery()
		{
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(Config.SERVICES_ALLOW_LOTTERY)
			{
				_log.info("Lottery: Ending lottery #" + getId() + ".");
			}
			int[] luckynums = new int[5];
			int luckynum = 0;
			for(int i = 0;i < 5;++i)
			{
				boolean found = true;
				while(found)
				{
					luckynum = Rnd.get(20) + 1;
					found = false;
					for(int j = 0;j < i;++j)
					{
						if(luckynums[j] != luckynum)
							continue;
						found = true;
					}
				}
				luckynums[i] = luckynum;
			}
			if(Config.SERVICES_ALLOW_LOTTERY)
			{
				_log.info("Lottery: The lucky numbers are " + luckynums[0] + ", " + luckynums[1] + ", " + luckynums[2] + ", " + luckynums[3] + ", " + luckynums[4] + ".");
			}
			int enchant = 0;
			int type2 = 0;
			for(int i = 0;i < 5;++i)
			{
				if(luckynums[i] < 17)
				{
					enchant = (int) ((double) enchant + Math.pow(2.0, luckynums[i] - 1));
					continue;
				}
				type2 = (int) ((double) type2 + Math.pow(2.0, luckynums[i] - 17));
			}
			if(Config.SERVICES_ALLOW_LOTTERY)
			{
				_log.info("Lottery: Encoded lucky numbers are " + enchant + ", " + type2);
			}
			int count1 = 0;
			int count2 = 0;
			int count3 = 0;
			int count4 = 0;
			Connection con = null;
			PreparedStatement statement = null;
			ResultSet rset = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT items.enchant AS `enchant`, items_options.damaged AS `damaged` FROM items, items_options WHERE items_options.blessed = ? AND items.item_id = items_options.item_id AND items.item_type = 4442");
				statement.setInt(1, getId());
				rset = statement.executeQuery();
				while(rset.next())
				{
					int curenchant = rset.getInt("enchant") & enchant;
					int curtype2 = rset.getInt("damaged") & type2;
					if(curenchant == 0 && curtype2 == 0)
						continue;
					int count = 0;
					for(int i = 1;i <= 16;++i)
					{
						int val = curenchant / 2;
						if((double) val != (double) curenchant / 2.0)
						{
							++count;
						}
						int val2;
						if((double) (val2 = curtype2 / 2) != (double) curtype2 / 2.0)
						{
							++count;
						}
						curenchant = val;
						curtype2 = val2;
					}
					if(count == 5)
					{
						++count1;
						continue;
					}
					if(count == 4)
					{
						++count2;
						continue;
					}
					if(count == 3)
					{
						++count3;
						continue;
					}
					if(count <= 0)
						continue;
					++count4;
				}
			}
			catch(SQLException e)
			{
				_log.warn("Lottery: Could restore lottery data: " + e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement, rset);
			}
			int prize4 = count4 * Config.SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE;
			int prize1 = 0;
			if(count1 > 0)
			{
				prize1 = (int) ((double) (getPrize() - prize4) * Config.SERVICES_LOTTERY_5_NUMBER_RATE / (double) count1);
			}
			int prize2 = 0;
			if(count2 > 0)
			{
				prize2 = (int) ((double) (getPrize() - prize4) * Config.SERVICES_LOTTERY_4_NUMBER_RATE / (double) count2);
			}
			int prize3 = 0;
			if(count3 > 0)
			{
				prize3 = (int) ((double) (getPrize() - prize4) * Config.SERVICES_LOTTERY_3_NUMBER_RATE / (double) count3);
			}
			int newprize = prize1 == 0 && prize2 == 0 && prize3 == 0 ? getPrize() : getPrize() + prize1 + prize2 + prize3;
			if(Config.SERVICES_ALLOW_LOTTERY)
			{
				_log.info("Lottery: Jackpot for next lottery is " + newprize + ".");
			}
			if(count1 > 0)
			{
				SystemMessage sm = new SystemMessage(1112);
				sm.addNumber(getId());
				sm.addNumber(getPrize());
				sm.addNumber(count1);
				Announcements.getInstance().announceToAll(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(1113);
				sm.addNumber(getId());
				sm.addNumber(getPrize());
				Announcements.getInstance().announceToAll(sm);
			}
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE games SET finished=1, prize=?, newprize=?, number1=?, number2=?, prize1=?, prize2=?, prize3=? WHERE id=1 AND idnr=?");
				statement.setInt(1, getPrize());
				statement.setInt(2, newprize);
				statement.setInt(3, enchant);
				statement.setInt(4, type2);
				statement.setInt(5, prize1);
				statement.setInt(6, prize2);
				statement.setInt(7, prize3);
				statement.setInt(8, getId());
				statement.execute();
			}
			catch(SQLException e)
			{
				_log.warn("Lottery: Could not store finished lottery data: " + e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
			ThreadPoolManager.getInstance().schedule(new startLottery(), 60000);
			++_number;
			_isStarted = false;
		}
	}
	
	private class stopSellingTickets extends RunnableImpl
	{
		protected stopSellingTickets()
		{
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(Config.SERVICES_ALLOW_LOTTERY)
			{
				_log.info("Lottery: Stopping ticket sell for lottery #" + getId() + ".");
			}
			_isSellingTickets = false;
			Announcements.getInstance().announceToAll(Msg.LOTTERY_TICKET_SALES_HAVE_BEEN_TEMPORARILY_SUSPENDED);
		}
	}
	
	private class startLottery extends RunnableImpl
	{
		protected startLottery()
		{
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(restoreLotteryData())
			{
				announceLottery();
				scheduleEndOfLottery();
				createNewLottery();
			}
		}
	}
}