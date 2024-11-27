package l2.gameserver.instancemanager;

import l2.commons.dbutils.DbUtils;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.database.mysql;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.Spawner;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.RaidBossInstance;
import l2.gameserver.model.instances.ReflectionBossInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.tables.ClanTable;
import l2.gameserver.tables.GmListTable;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.SqlBatch;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RaidBossSpawnManager
{
	public static final Integer KEY_RANK;
	public static final Integer KEY_TOTAL_POINTS;
	private static final Logger _log = LoggerFactory.getLogger(RaidBossSpawnManager.class);
	protected static Map<Integer, Spawner> _spawntable;
	protected static Map<Integer, StatsSet> _storedInfo;
	protected static Map<Integer, Map<Integer, Integer>> _points;
	private static RaidBossSpawnManager _instance;
	
	static
	{
		_spawntable = new ConcurrentHashMap<>();
		KEY_RANK = new Integer(-1);
		KEY_TOTAL_POINTS = new Integer(0);
	}
	
	private final Lock pointsLock = new ReentrantLock();
	
	private RaidBossSpawnManager()
	{
		if(!Config.DONTLOADSPAWN)
		{
			reloadBosses();
		}
	}
	
	public static RaidBossSpawnManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new RaidBossSpawnManager();
		}
		return _instance;
	}
	
	private static void addRespawnAnnounce(int npcId, long respawnDelay)
	{
		long now;
		if(Config.ALT_RAID_BOSS_SPAWN_ANNOUNCE_DELAY > 0 && respawnDelay > 0 && ArrayUtils.contains(Config.ALT_RAID_BOSS_SPAWN_ANNOUNCE_IDS, npcId) && respawnDelay - (long) Config.ALT_RAID_BOSS_SPAWN_ANNOUNCE_DELAY > (now = System.currentTimeMillis() / 1000))
		{
			ThreadPoolManager.getInstance().schedule(new RunnableImpl()
			{
				
				@Override
				public void runImpl() throws Exception
				{
					NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(npcId);
					if(npcTemplate != null)
					{
						Announcements.getInstance().announceByCustomMessage("l2.gameserver.instancemanager.RaidBossSpawnManager.AltAnnounceRaidbossSpawnSoon", new String[] {npcTemplate.getName()});
					}
				}
			}, (respawnDelay - (long) Config.ALT_RAID_BOSS_SPAWN_ANNOUNCE_DELAY - now) * 1000);
		}
	}
	
	public void reloadBosses()
	{
		loadStatus();
		restorePointsTable();
		calculateRanking();
	}
	
	public void cleanUp()
	{
		updateAllStatusDb();
		updatePointsDb();
		_storedInfo.clear();
		_spawntable.clear();
		_points.clear();
	}
	
	private void loadStatus()
	{
		_storedInfo = new ConcurrentHashMap<>();
		Connection con = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			rset = con.createStatement().executeQuery("SELECT * FROM `raidboss_status`");
			while(rset.next())
			{
				int id = rset.getInt("id");
				StatsSet info = new StatsSet();
				info.set("current_hp", rset.getDouble("current_hp"));
				info.set("current_mp", rset.getDouble("current_mp"));
				info.set("respawn_delay", rset.getInt("respawn_delay"));
				_storedInfo.put(id, info);
			}
		}
		catch(Exception e)
		{
			_log.warn("RaidBossSpawnManager: Couldnt load raidboss statuses");
		}
		finally
		{
			Statement statement = null;
			DbUtils.closeQuietly(con, statement, rset);
		}
		_log.info("RaidBossSpawnManager: Loaded " + _storedInfo.size() + " Statuses");
	}
	
	public void updateAllStatusDb()
	{
		Iterator<Integer> iterator = _storedInfo.keySet().iterator();
		while(iterator.hasNext())
		{
			int id = iterator.next();
			updateStatusDb(id);
		}
	}
	
	private void updateStatusDb(int id)
	{
		Spawner spawner = _spawntable.get(id);
		if(spawner == null)
		{
			return;
		}
		StatsSet info = _storedInfo.get(id);
		if(info == null)
		{
			info = new StatsSet();
			_storedInfo.put(id, info);
		}
		NpcInstance raidboss;
		if((raidboss = spawner.getFirstSpawned()) instanceof ReflectionBossInstance)
		{
			return;
		}
		int respawnDelay = 0;
		if(raidboss != null && !raidboss.isDead())
		{
			info.set("current_hp", raidboss.getCurrentHp());
			info.set("current_mp", raidboss.getCurrentMp());
			info.set("respawn_delay", 0);
		}
		else
		{
			respawnDelay = spawner.getRespawnTime();
			info.set("current_hp", 0);
			info.set("current_mp", 0);
			info.set("respawn_delay", respawnDelay);
			addRespawnAnnounce(id, respawnDelay);
		}
		Log.add("updateStatusDb id=" + id + " current_hp=" + info.getDouble("current_hp") + " current_mp=" + info.getDouble("current_mp") + " respawn_delay=" + info.getInteger("respawn_delay", 0) + (raidboss != null ? new StringBuilder().append(" respawnTime=").append(raidboss.getSpawn().getRespawnTime()).toString() : ""), "RaidBossSpawnManager");
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO `raidboss_status` (id, current_hp, current_mp, respawn_delay) VALUES (?,?,?,?)");
			statement.setInt(1, id);
			statement.setInt(2, (int) info.getDouble("current_hp"));
			statement.setInt(3, (int) info.getDouble("current_mp"));
			statement.setInt(4, respawnDelay);
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.warn("RaidBossSpawnManager: Couldnt update raidboss_status table");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void addNewSpawn(int npcId, Spawner spawnDat)
	{
		if(_spawntable.containsKey(npcId))
		{
			return;
		}
		_spawntable.put(npcId, spawnDat);
		StatsSet info = _storedInfo.get(npcId);
		if(info != null)
		{
			long respawnTime = info.getLong("respawn_delay", 0);
			spawnDat.setRespawnTime((int) respawnTime);
			Log.add("AddSpawn npc=" + npcId + " respawnDelay=" + spawnDat.getRespawnDelay() + " respawnDelayRandom=" + spawnDat.getRespawnDelayRandom() + " respawnCron=" + spawnDat.getRespawnCron() + " respawn_delay=" + respawnTime, "RaidBossSpawnManager");
			if(respawnTime > 0)
			{
				addRespawnAnnounce(npcId, respawnTime);
			}
		}
	}
	
	public void onBossSpawned(RaidBossInstance raidboss)
	{
		int bossId = raidboss.getNpcId();
		if(!_spawntable.containsKey(bossId))
		{
			return;
		}
		StatsSet info = _storedInfo.get(bossId);
		if(info != null && info.getDouble("current_hp") > 1.0)
		{
			raidboss.setCurrentHp(info.getDouble("current_hp"), false);
			raidboss.setCurrentMp(info.getDouble("current_mp"));
		}
		Log.add("onBossSpawned npc=" + bossId + " current_hp=" + raidboss.getCurrentHp() + " current_mp=" + raidboss.getCurrentMp(), "RaidBossSpawnManager");
		GmListTable.broadcastMessageToGMs("Spawning RaidBoss " + raidboss.getName());
		if(ArrayUtils.contains(Config.ALT_RAID_BOSS_SPAWN_ANNOUNCE_IDS, raidboss.getNpcId()))
		{
			Announcements.getInstance().announceByCustomMessage("l2.gameserver.instancemanager.RaidBossSpawnManager.AltAnnounceRaidbossSpawn", new String[] {raidboss.getName()});
		}
	}
	
	public void onBossDespawned(RaidBossInstance raidboss)
	{
		updateStatusDb(raidboss.getNpcId());
	}
	
	public Status getRaidBossStatusId(int bossId)
	{
		Spawner spawner = _spawntable.get(bossId);
		if(spawner == null)
		{
			return Status.UNDEFINED;
		}
		NpcInstance npc = spawner.getFirstSpawned();
		return npc == null ? Status.DEAD : Status.ALIVE;
	}
	
	public boolean isDefined(int bossId)
	{
		return _spawntable.containsKey(bossId);
	}
	
	public Map<Integer, Spawner> getSpawnTable()
	{
		return _spawntable;
	}
	
	private void restorePointsTable()
	{
		pointsLock.lock();
		_points = new ConcurrentHashMap<>();
		Connection con = null;
		Statement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT owner_id, boss_id, points FROM `raidboss_points` ORDER BY owner_id ASC");
			int currentOwner = 0;
			HashMap<Integer, Integer> score = null;
			while(rset.next())
			{
				if(currentOwner != rset.getInt("owner_id"))
				{
					currentOwner = rset.getInt("owner_id");
					score = new HashMap<>();
					_points.put(currentOwner, score);
				}
				assert score != null;
				int bossId = rset.getInt("boss_id");
				NpcTemplate template = NpcHolder.getInstance().getTemplate(bossId);
				if(bossId == KEY_RANK || bossId == KEY_TOTAL_POINTS || template == null || template.rewardRp <= 0)
					continue;
				score.put(bossId, rset.getInt("points"));
			}
		}
		catch(Exception e)
		{
			_log.warn("RaidBossSpawnManager: Couldnt load raidboss points");
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		pointsLock.unlock();
	}
	
	public void updatePointsDb()
	{
		pointsLock.lock();
		if(!mysql.set("TRUNCATE `raidboss_points`"))
		{
			_log.warn("RaidBossSpawnManager: Couldnt empty raidboss_points table");
		}
		if(_points.isEmpty())
		{
			pointsLock.unlock();
			return;
		}
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			SqlBatch b = new SqlBatch("INSERT INTO `raidboss_points` (owner_id, boss_id, points) VALUES");
			for(Map.Entry<Integer, Map<Integer, Integer>> pointEntry : _points.entrySet())
			{
				Map<Integer, Integer> tmpPoint = pointEntry.getValue();
				if(tmpPoint == null || tmpPoint.isEmpty())
					continue;
				for(Map.Entry<Integer, Integer> pointListEntry : tmpPoint.entrySet())
				{
					if(KEY_RANK.equals(pointListEntry.getKey()) || KEY_TOTAL_POINTS.equals(pointListEntry.getKey()) || pointListEntry.getValue() == null || pointListEntry.getValue() == 0)
						continue;
					StringBuilder sb = new StringBuilder("(");
					sb.append(pointEntry.getKey()).append(",");
					sb.append(pointListEntry.getKey()).append(",");
					sb.append(pointListEntry.getValue()).append(")");
					b.write(sb.toString());
				}
			}
			if(!b.isEmpty())
			{
				statement.executeUpdate(b.close());
			}
		}
		catch(SQLException e)
		{
			_log.warn("RaidBossSpawnManager: Couldnt update raidboss_points table");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		pointsLock.unlock();
	}
	
	public void deletePoints(int ownerId)
	{
		if(ownerId <= 0)
		{
			return;
		}
		pointsLock.lock();
		try
		{
			_points.remove(ownerId);
		}
		finally
		{
			pointsLock.unlock();
		}
	}
	
	public void addPoints(int ownerId, int bossId, int points)
	{
		if(points <= 0 || ownerId <= 0 || bossId <= 0)
		{
			return;
		}
		pointsLock.lock();
		Map<Integer, Integer> pointsTable = _points.get(ownerId);
		if(pointsTable == null)
		{
			pointsTable = new HashMap<>();
			_points.put(ownerId, pointsTable);
		}
		if(pointsTable.isEmpty())
		{
			pointsTable.put(bossId, points);
		}
		else
		{
			Integer currentPoins;
			pointsTable.put(bossId, (currentPoins = pointsTable.get(bossId)) == null ? points : currentPoins + points);
		}
		pointsLock.unlock();
	}
	
	public TreeMap<Integer, Integer> calculateRanking()
	{
		pointsLock.lock();
		TreeMap<Integer, Integer> tmpRanking = new TreeMap<>();
		for(Map.Entry<Integer, Map<Integer, Integer>> point : _points.entrySet())
		{
			Map<Integer, Integer> tmpPoint = point.getValue();
			tmpPoint.remove(KEY_RANK);
			tmpPoint.remove(KEY_TOTAL_POINTS);
			int totalPoints = 0;
			for(Map.Entry<Integer, Integer> e : tmpPoint.entrySet())
			{
				totalPoints += e.getValue().intValue();
			}
			if(totalPoints == 0)
				continue;
			tmpPoint.put(KEY_TOTAL_POINTS, totalPoints);
			tmpRanking.put(totalPoints, point.getKey());
		}
		int ranking = 1;
		for(Map.Entry entry : tmpRanking.descendingMap().entrySet())
		{
			Map<Integer, Integer> tmpPoint = _points.get(entry.getValue());
			tmpPoint.put(KEY_RANK, ranking);
			++ranking;
		}
		pointsLock.unlock();
		return tmpRanking;
	}
	
	public void distributeRewards()
	{
		pointsLock.lock();
		TreeMap<Integer, Integer> ranking = calculateRanking();
		Iterator<Integer> e = ranking.descendingMap().values().iterator();
		for(int counter = 1;e.hasNext() && counter <= 100;++counter)
		{
			int reward = 0;
			int playerId = e.next();
			if(counter == 1)
			{
				reward = 2500;
			}
			else if(counter == 2)
			{
				reward = 1800;
			}
			else if(counter == 3)
			{
				reward = 1400;
			}
			else if(counter == 4)
			{
				reward = 1200;
			}
			else if(counter == 5)
			{
				reward = 900;
			}
			else if(counter == 6)
			{
				reward = 700;
			}
			else if(counter == 7)
			{
				reward = 600;
			}
			else if(counter == 8)
			{
				reward = 400;
			}
			else if(counter == 9)
			{
				reward = 300;
			}
			else if(counter == 10)
			{
				reward = 200;
			}
			else if(counter <= 50)
			{
				reward = 50;
			}
			else if(counter <= 100)
			{
				reward = 25;
			}
			Player player = GameObjectsStorage.getPlayer(playerId);
			Clan clan = player != null ? player.getClan() : ClanTable.getInstance().getClan(mysql.simple_get_int("clanid", "characters", "obj_Id=" + playerId));
			if(clan == null)
				continue;
			clan.incReputation(reward, true, "RaidPoints");
		}
		_points.clear();
		updatePointsDb();
		pointsLock.unlock();
	}
	
	public Map<Integer, Map<Integer, Integer>> getPoints()
	{
		return _points;
	}
	
	public Map<Integer, Integer> getPointsForOwnerId(int ownerId)
	{
		return _points.get(ownerId);
	}
	
	public enum Status
	{
		ALIVE,
		DEAD,
		UNDEFINED;
	}
}