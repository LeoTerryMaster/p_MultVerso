package l2.gameserver.taskmanager.actionrunner.tasks;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.database.mysql;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.utils.Strings;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class DeleteExpiredVarsTask extends AutomaticTask
{
	private static final Logger _log = Logger.getLogger(DeleteExpiredVarsTask.class);
	
	@Override
	public void doTask() throws Exception
	{
		long t = System.currentTimeMillis();
		HashMap<Integer, String> varMap = new HashMap<>();
		ResultSet rs = null;
		PreparedStatement query = null;
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			query = con.prepareStatement("SELECT obj_id, name FROM character_variables WHERE expire_time > 0 AND expire_time < ?");
			query.setLong(1, System.currentTimeMillis());
			rs = query.executeQuery();
			while(rs.next())
			{
				String name = rs.getString("name");
				String obj_id = Strings.stripSlashes(rs.getString("obj_id"));
				varMap.put(Integer.parseInt(obj_id), name);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, query, rs);
		}
		if(!varMap.isEmpty())
		{
			for(Map.Entry entry : varMap.entrySet())
			{
				Player player = GameObjectsStorage.getPlayer((Integer) entry.getKey());
				if(player != null && player.isOnline())
				{
					player.unsetVar((String) entry.getValue());
					continue;
				}
				mysql.set("DELETE FROM `character_variables` WHERE `obj_id`=? AND `type`='user-var' AND `name`=? LIMIT 1", entry.getKey(), entry.getValue());
			}
		}
	}
	
	@Override
	public long reCalcTime(boolean start)
	{
		return System.currentTimeMillis() + 600000;
	}
}