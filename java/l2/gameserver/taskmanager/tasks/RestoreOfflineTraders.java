package l2.gameserver.taskmanager.tasks;

import l2.commons.dbutils.DbUtils;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.skills.AbnormalEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RestoreOfflineTraders extends RunnableImpl
{
	private static final Logger _log = LoggerFactory.getLogger(RestoreOfflineTraders.class);
	
	@Override
	public void runImpl() throws Exception
	{
		int count = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
			{
				int expireTimeSecs = (int) (System.currentTimeMillis() / 1000 - Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK);
				statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offline' AND value < ?");
				statement.setLong(1, expireTimeSecs);
				statement.executeUpdate();
				DbUtils.close(statement);
			}
			statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offline' AND obj_id IN (SELECT obj_id FROM characters WHERE accessLevel < 0)");
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("SELECT obj_id, value FROM character_variables WHERE name = 'offline'");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int objectId = rset.getInt("obj_id");
				int expireTimeSecs = rset.getInt("value");
				Player p = Player.restore(objectId);
				if(p == null)
					continue;
				if(p.isDead())
				{
					p.kick();
					continue;
				}
				if(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR_CHANGE)
				{
					p.setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR);
				}
				if(Config.SERVICES_OFFLINE_TRADE_ABNORMAL != AbnormalEffect.NULL)
				{
					p.startAbnormalEffect(Config.SERVICES_OFFLINE_TRADE_ABNORMAL);
				}
				p.setOfflineMode(true);
				p.setIsOnline(true);
				p.spawnMe();
				if(p.getClan() != null && p.getClan().getAnyMember(p.getObjectId()) != null)
				{
					p.getClan().getAnyMember(p.getObjectId()).setPlayerInstance(p, false);
				}
				if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
				{
					p.startKickTask((Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK + (long) expireTimeSecs - System.currentTimeMillis() / 1000) * 1000);
				}
				if(Config.SERVICES_TRADE_ONLY_FAR)
				{
					for(Player player : World.getAroundPlayers(p, Config.SERVICES_TRADE_RADIUS, 200))
					{
						if(!player.isInStoreMode())
							continue;
						if(player.isInOfflineMode())
						{
							player.setOfflineMode(false);
							player.kick();
							_log.warn("Offline trader: " + player + " kicked.");
							continue;
						}
						player.setPrivateStoreType(0);
						player.standUp();
						player.broadcastCharInfo();
					}
				}
				++count;
			}
		}
		catch(Exception e)
		{
			_log.error("Error while restoring offline traders!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_log.info("Restored " + count + " offline traders");
	}
}