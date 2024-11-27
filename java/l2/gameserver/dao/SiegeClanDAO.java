package l2.gameserver.dao;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.entity.events.objects.SiegeClanObject;
import l2.gameserver.model.entity.residence.Residence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SiegeClanDAO
{
	public static final String SELECT_SQL_QUERY = "SELECT clan_id, param, date FROM siege_clans WHERE residence_id=? AND type=? ORDER BY date";
	public static final String INSERT_SQL_QUERY = "INSERT INTO siege_clans(residence_id, clan_id, param, type, date) VALUES (?, ?, ?, ?, ?)";
	public static final String UPDATE_SQL_QUERY = "UPDATE siege_clans SET type=?, param=? WHERE residence_id=? AND clan_id=?";
	public static final String DELETE_SQL_QUERY = "DELETE FROM siege_clans WHERE residence_id=? AND clan_id=? AND type=?";
	public static final String DELETE_SQL_QUERY2 = "DELETE FROM siege_clans WHERE residence_id=?";
	private static final Logger _log = LoggerFactory.getLogger(SiegeClanDAO.class);
	private static final SiegeClanDAO _instance = new SiegeClanDAO();
	
	public static SiegeClanDAO getInstance()
	{
		return _instance;
	}
	
	public List<SiegeClanObject> load(Residence residence, String name)
	{
		List<SiegeClanObject> siegeClans = Collections.emptyList();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT clan_id, param, date FROM siege_clans WHERE residence_id=? AND type=? ORDER BY date");
			statement.setInt(1, residence.getId());
			statement.setString(2, name);
			rset = statement.executeQuery();
			siegeClans = new ArrayList<>();
			while(rset.next())
			{
				int clanId = rset.getInt("clan_id");
				long param = rset.getLong("param");
				long date = rset.getLong("date");
				Object object = residence.getSiegeEvent().newSiegeClan(name, clanId, param, date);
				if(object != null)
				{
					siegeClans.add((SiegeClanObject) object);
					continue;
				}
				_log.info("SiegeClanDAO#load(Residence, String): null clan: " + clanId + "; residence: " + residence.getId());
			}
		}
		catch(Exception e)
		{
			_log.warn("SiegeClanDAO#load(Residence, String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return siegeClans;
	}
	
	public void insert(Residence residence, SiegeClanObject siegeClan)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO siege_clans(residence_id, clan_id, param, type, date) VALUES (?, ?, ?, ?, ?)");
			statement.setInt(1, residence.getId());
			statement.setInt(2, siegeClan.getObjectId());
			statement.setLong(3, siegeClan.getParam());
			statement.setString(4, siegeClan.getType());
			statement.setLong(5, siegeClan.getDate());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("SiegeClanDAO#insert(Residence, SiegeClan): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void delete(Residence residence, SiegeClanObject siegeClan)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM siege_clans WHERE residence_id=? AND clan_id=? AND type=?");
			statement.setInt(1, residence.getId());
			statement.setInt(2, siegeClan.getObjectId());
			statement.setString(3, siegeClan.getType());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("SiegeClanDAO#delete(Residence, SiegeClan): " + e, e);
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
			statement = con.prepareStatement("DELETE FROM siege_clans WHERE residence_id=?");
			statement.setInt(1, residence.getId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("SiegeClanDAO#delete(Residence): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void update(Residence residence, SiegeClanObject siegeClan)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE siege_clans SET type=?, param=? WHERE residence_id=? AND clan_id=?");
			statement.setString(1, siegeClan.getType());
			statement.setLong(2, siegeClan.getParam());
			statement.setInt(3, residence.getId());
			statement.setInt(4, siegeClan.getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("SiegeClanDAO#update(Residence, SiegeClan): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}