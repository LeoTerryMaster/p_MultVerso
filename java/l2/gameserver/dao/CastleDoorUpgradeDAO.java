package l2.gameserver.dao;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CastleDoorUpgradeDAO
{
	public static final String SELECT_SQL_QUERY = "SELECT hp FROM castle_door_upgrade WHERE door_id=?";
	public static final String REPLACE_SQL_QUERY = "REPLACE INTO castle_door_upgrade (door_id, hp) VALUES (?,?)";
	public static final String DELETE_SQL_QUERY = "DELETE FROM castle_door_upgrade WHERE door_id=?";
	private static final CastleDoorUpgradeDAO _instance = new CastleDoorUpgradeDAO();
	private static final Logger _log = LoggerFactory.getLogger(CastleDoorUpgradeDAO.class);
	
	public static CastleDoorUpgradeDAO getInstance()
	{
		return _instance;
	}
	
	public int load(int doorId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT hp FROM castle_door_upgrade WHERE door_id=?");
			statement.setInt(1, doorId);
			rset = statement.executeQuery();
			if(rset.next())
			{
				int n = rset.getInt("hp");
				return n;
			}
		}
		catch(Exception e)
		{
			_log.error("CastleDoorUpgradeDAO:load(int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return 0;
	}
	
	public void insert(int uId, int val)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO castle_door_upgrade (door_id, hp) VALUES (?,?)");
			statement.setInt(1, uId);
			statement.setInt(2, val);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("CastleDoorUpgradeDAO:insert(int, int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void delete(int uId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM castle_door_upgrade WHERE door_id=?");
			statement.setInt(1, uId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("CastleDoorUpgradeDAO:delete(int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}