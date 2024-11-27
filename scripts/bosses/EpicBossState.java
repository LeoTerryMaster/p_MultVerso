package bosses;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

public class EpicBossState
{
	private static final Logger _log = LoggerFactory.getLogger(EpicBossState.class);
	private int _bossId;
	private long _respawnDate;
	private State _state;
	
	public EpicBossState(int bossId)
	{
		this(bossId, true);
	}
	
	public EpicBossState(int bossId, boolean isDoLoad)
	{
		_bossId = bossId;
		if(isDoLoad)
		{
			load();
		}
	}
	
	public int getBossId()
	{
		return _bossId;
	}
	
	public void setBossId(int newId)
	{
		_bossId = newId;
	}
	
	public State getState()
	{
		return _state;
	}
	
	public void setState(State newState)
	{
		_state = newState;
	}
	
	public long getRespawnDate()
	{
		return _respawnDate;
	}
	
	public void setRespawnDate(long interval)
	{
		_respawnDate = interval + System.currentTimeMillis();
	}
	
	public void load()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM epic_boss_spawn WHERE bossId = ? LIMIT 1");
			statement.setInt(1, _bossId);
			rset = statement.executeQuery();
			if(rset.next())
			{
				_respawnDate = rset.getLong("respawnDate") * 1000;
				int tempState;
				_state = _respawnDate - System.currentTimeMillis() <= 0 ? State.NOTSPAWN : (tempState = rset.getInt("state")) == State.NOTSPAWN.ordinal() ? State.NOTSPAWN : tempState == State.INTERVAL.ordinal() ? State.INTERVAL : tempState == State.ALIVE.ordinal() ? State.ALIVE : tempState == State.DEAD.ordinal() ? State.DEAD : State.NOTSPAWN;
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
	}
	
	public void save()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO epic_boss_spawn (bossId,respawnDate,state) VALUES(?,?,?)");
			statement.setInt(1, _bossId);
			statement.setInt(2, (int) (_respawnDate / 1000));
			statement.setInt(3, _state.ordinal());
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
	
	public void update()
	{
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("UPDATE epic_boss_spawn SET respawnDate=" + _respawnDate / 1000 + ", state=" + _state.ordinal() + " WHERE bossId=" + _bossId);
			Date dt = new Date(_respawnDate);
			_log.info("update EpicBossState: ID:" + _bossId + ", RespawnDate:" + dt + ", State:" + _state);
		}
		catch(Exception e)
		{
			_log.error("Exception on update EpicBossState: ID " + _bossId + ", RespawnDate:" + _respawnDate / 1000 + ", State:" + _state, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void setNextRespawnDate(long newRespawnDate)
	{
		_respawnDate = newRespawnDate;
	}
	
	public long getInterval()
	{
		long interval = _respawnDate - System.currentTimeMillis();
		return interval > 0 ? interval : 0;
	}
	
	public enum State
	{
		NOTSPAWN,
		ALIVE,
		DEAD,
		INTERVAL;
	}
}