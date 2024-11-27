package l2.gameserver.dao;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.entity.residence.Residence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CastleDamageZoneDAO
{
	public static final String SELECT_SQL_QUERY = "SELECT zone FROM castle_damage_zones WHERE residence_id=?";
	public static final String INSERT_SQL_QUERY = "INSERT INTO castle_damage_zones (residence_id, zone) VALUES (?,?)";
	public static final String DELETE_SQL_QUERY = "DELETE FROM castle_damage_zones WHERE residence_id=?";
	private static final CastleDamageZoneDAO _instance = new CastleDamageZoneDAO();
	private static final Logger _log = LoggerFactory.getLogger(CastleDoorUpgradeDAO.class);
	
	public static CastleDamageZoneDAO getInstance()
	{
		return _instance;
	}
	
	public List<String> load(Residence r)
	{
		List<String> set = Collections.emptyList();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT zone FROM castle_damage_zones WHERE residence_id=?");
			statement.setInt(1, r.getId());
			rset = statement.executeQuery();
			set = new ArrayList();
			while(rset.next())
			{
				set.add(rset.getString("zone"));
			}
		}
		catch(Exception e)
		{
			_log.error("CastleDamageZoneDAO:load(Residence): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return set;
	}
	
	public void insert(Residence residence, String name)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO castle_damage_zones (residence_id, zone) VALUES (?,?)");
			statement.setInt(1, residence.getId());
			statement.setString(2, name);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("CastleDamageZoneDAO:insert(Residence, String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void delete(Residence residence)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM castle_damage_zones WHERE residence_id=?");
			statement.setInt(1, residence.getId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("CastleDamageZoneDAO:delete(Residence): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}