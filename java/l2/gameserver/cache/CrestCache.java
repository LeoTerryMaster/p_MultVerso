package l2.gameserver.cache;

import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CrestCache
{
	public static final int ALLY_CREST_SIZE = 192;
	public static final int CREST_SIZE = 256;
	public static final int LARGE_CREST_SIZE = 2176;
	private static final Logger _log = LoggerFactory.getLogger(CrestCache.class);
	private static final CrestCache _instance = new CrestCache();
	private final TIntIntHashMap _pledgeCrestId = new TIntIntHashMap();
	private final TIntIntHashMap _pledgeCrestLargeId = new TIntIntHashMap();
	private final TIntIntHashMap _allyCrestId = new TIntIntHashMap();
	private final TIntObjectHashMap<byte[]> _pledgeCrest = new TIntObjectHashMap();
	private final TIntObjectHashMap<byte[]> _pledgeCrestLarge = new TIntObjectHashMap();
	private final TIntObjectHashMap<byte[]> _allyCrest = new TIntObjectHashMap();
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	
	private CrestCache()
	{
		load();
	}
	
	public static final CrestCache getInstance()
	{
		return _instance;
	}
	
	private static int getCrestId(int pledgeId, byte[] crest)
	{
		return Math.abs(new HashCodeBuilder(15, 87).append(pledgeId).append(crest).toHashCode());
	}
	
	public static boolean isValidCrestData(byte[] crestData)
	{
		switch(crestData.length)
		{
			case 192:
			case 256:
			case 2176:
			{
				break;
			}
			default:
			{
				return false;
			}
		}
		if(crestData[0] != 68 || crestData[1] != 68 || crestData[2] != 83 || crestData[3] != 32 || crestData[84] != 68 || crestData[85] != 88 || crestData[86] != 84 || crestData[87] != 49)
		{
			return false;
		}
		switch(crestData.length)
		{
			case 192:
			{
				if(crestData[12] == 16 && crestData[16] == 8)
					break;
				return false;
			}
			case 256:
			{
				if(crestData[12] == 16 && crestData[16] == 16)
					break;
				return false;
			}
			case 2176:
			{
				if(crestData[12] == 64 && crestData[16] == 64)
					break;
				return false;
			}
		}
		return true;
	}
	
	public void load()
	{
		int count = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT clan_id, crest FROM clan_data WHERE crest IS NOT NULL");
			rset = statement.executeQuery();
			byte[] crest;
			int crestId;
			int pledgeId;
			while(rset.next())
			{
				++count;
				pledgeId = rset.getInt("clan_id");
				crest = rset.getBytes("crest");
				crestId = getCrestId(pledgeId, crest);
				_pledgeCrestId.put(pledgeId, crestId);
				_pledgeCrest.put(crestId, crest);
			}
			DbUtils.close(statement, rset);
			statement = con.prepareStatement("SELECT clan_id, largecrest FROM clan_data WHERE largecrest IS NOT NULL");
			rset = statement.executeQuery();
			while(rset.next())
			{
				++count;
				pledgeId = rset.getInt("clan_id");
				crest = rset.getBytes("largecrest");
				crestId = getCrestId(pledgeId, crest);
				_pledgeCrestLargeId.put(pledgeId, crestId);
				_pledgeCrestLarge.put(crestId, crest);
			}
			DbUtils.close(statement, rset);
			statement = con.prepareStatement("SELECT ally_id, crest FROM ally_data WHERE crest IS NOT NULL");
			rset = statement.executeQuery();
			while(rset.next())
			{
				++count;
				pledgeId = rset.getInt("ally_id");
				crest = rset.getBytes("crest");
				crestId = getCrestId(pledgeId, crest);
				_allyCrestId.put(pledgeId, crestId);
				_allyCrest.put(crestId, crest);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_log.info("CrestCache: Loaded " + count + " crests");
	}
	
	public byte[] getPledgeCrest(int crestId)
	{
		readLock.lock();
		byte[] crest;
		try
		{
			crest = _pledgeCrest.get(crestId);
		}
		finally
		{
			readLock.unlock();
		}
		return crest;
	}
	
	public byte[] getPledgeCrestLarge(int crestId)
	{
		readLock.lock();
		byte[] crest;
		try
		{
			crest = _pledgeCrestLarge.get(crestId);
		}
		finally
		{
			readLock.unlock();
		}
		return crest;
	}
	
	public byte[] getAllyCrest(int crestId)
	{
		readLock.lock();
		byte[] crest;
		try
		{
			crest = _allyCrest.get(crestId);
		}
		finally
		{
			readLock.unlock();
		}
		return crest;
	}
	
	public int getPledgeCrestId(int pledgeId)
	{
		readLock.lock();
		int crestId;
		try
		{
			crestId = _pledgeCrestId.get(pledgeId);
		}
		finally
		{
			readLock.unlock();
		}
		return crestId;
	}
	
	public int getPledgeCrestLargeId(int pledgeId)
	{
		readLock.lock();
		int crestId;
		try
		{
			crestId = _pledgeCrestLargeId.get(pledgeId);
		}
		finally
		{
			readLock.unlock();
		}
		return crestId;
	}
	
	public int getAllyCrestId(int pledgeId)
	{
		readLock.lock();
		int crestId;
		try
		{
			crestId = _allyCrestId.get(pledgeId);
		}
		finally
		{
			readLock.unlock();
		}
		return crestId;
	}
	
	public void removePledgeCrest(int pledgeId)
	{
		writeLock.lock();
		try
		{
			_pledgeCrest.remove(_pledgeCrestId.remove(pledgeId));
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET crest=? WHERE clan_id=?");
			statement.setNull(1, -3);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void removePledgeCrestLarge(int pledgeId)
	{
		writeLock.lock();
		try
		{
			_pledgeCrestLarge.remove(_pledgeCrestLargeId.remove(pledgeId));
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET largecrest=? WHERE clan_id=?");
			statement.setNull(1, -3);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void removeAllyCrest(int pledgeId)
	{
		writeLock.lock();
		try
		{
			_allyCrest.remove(_allyCrestId.remove(pledgeId));
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE ally_data SET crest=? WHERE ally_id=?");
			statement.setNull(1, -3);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public int savePledgeCrest(int pledgeId, byte[] crest)
	{
		int crestId = getCrestId(pledgeId, crest);
		writeLock.lock();
		try
		{
			_pledgeCrestId.put(pledgeId, crestId);
			_pledgeCrest.put(crestId, crest);
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET crest=? WHERE clan_id=?");
			statement.setBytes(1, crest);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return crestId;
	}
	
	public int savePledgeCrestLarge(int pledgeId, byte[] crest)
	{
		int crestId = getCrestId(pledgeId, crest);
		writeLock.lock();
		try
		{
			_pledgeCrestLargeId.put(pledgeId, crestId);
			_pledgeCrestLarge.put(crestId, crest);
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET largecrest=? WHERE clan_id=?");
			statement.setBytes(1, crest);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return crestId;
	}
	
	public int saveAllyCrest(int pledgeId, byte[] crest)
	{
		int crestId = getCrestId(pledgeId, crest);
		writeLock.lock();
		try
		{
			_allyCrestId.put(pledgeId, crestId);
			_allyCrest.put(crestId, crest);
		}
		finally
		{
			writeLock.unlock();
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE ally_data SET crest=? WHERE ally_id=?");
			statement.setBytes(1, crest);
			statement.setInt(2, pledgeId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return crestId;
	}
}