package l2.gameserver.dao;

import l2.commons.dao.JdbcEntityState;
import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.entity.residence.Castle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CastleDAO
{
	public static final String SELECT_SQL_QUERY = "SELECT tax_percent, treasury, reward_count, siege_date, last_siege_date, own_date FROM castle WHERE id=? LIMIT 1";
	public static final String UPDATE_SQL_QUERY = "UPDATE castle SET tax_percent=?, treasury=?, reward_count=?, siege_date=?, last_siege_date=?, own_date=? WHERE id=?";
	private static final Logger _log = LoggerFactory.getLogger(CastleDAO.class);
	private static final CastleDAO _instance = new CastleDAO();
	
	public static CastleDAO getInstance()
	{
		return _instance;
	}
	
	public void select(Castle castle)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT tax_percent, treasury, reward_count, siege_date, last_siege_date, own_date FROM castle WHERE id=? LIMIT 1");
			statement.setInt(1, castle.getId());
			rset = statement.executeQuery();
			if(rset.next())
			{
				castle.setTaxPercent(rset.getInt("tax_percent"));
				castle.setTreasury(rset.getLong("treasury"));
				castle.setRewardCount(rset.getInt("reward_count"));
				castle.getSiegeDate().setTimeInMillis(rset.getLong("siege_date"));
				castle.getLastSiegeDate().setTimeInMillis(rset.getLong("last_siege_date"));
				castle.getOwnDate().setTimeInMillis(rset.getLong("own_date"));
			}
		}
		catch(Exception e)
		{
			_log.error("CastleDAO.select(Castle):" + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public void update(Castle residence)
	{
		if(!residence.getJdbcState().isUpdatable())
		{
			return;
		}
		update0(residence);
		residence.setJdbcState(JdbcEntityState.STORED);
	}
	
	private void update0(Castle castle)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE castle SET tax_percent=?, treasury=?, reward_count=?, siege_date=?, last_siege_date=?, own_date=? WHERE id=?");
			statement.setInt(1, castle.getTaxPercent0());
			statement.setLong(2, castle.getTreasury());
			statement.setInt(3, castle.getRewardCount());
			statement.setLong(4, castle.getSiegeDate().getTimeInMillis());
			statement.setLong(5, castle.getLastSiegeDate().getTimeInMillis());
			statement.setLong(6, castle.getOwnDate().getTimeInMillis());
			statement.setInt(7, castle.getId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("CastleDAO#update0(Castle): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}