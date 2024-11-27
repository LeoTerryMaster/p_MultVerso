package events.SavingSnowman;

import ai.FlyingSantaAI;
import l2.commons.util.RandomUtils;
import l2.commons.util.Rnd;
import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.data.xml.holder.ZoneHolder;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.Summon;
import l2.gameserver.model.Territory;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.s2c.CharMoveToLocation;
import l2.gameserver.network.l2.s2c.RadarControl;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.templates.ZoneTemplate;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.PositionUtils;
import l2.gameserver.utils.Util;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

public class SavingSnowman extends Functions implements ScriptFile, OnDeathListener, OnPlayerEnterListener
{
	private static final Logger _log = LoggerFactory.getLogger(SavingSnowman.class);
	private static final List<SimpleSpawner> _spawns = new ArrayList<>();
	private static final int INITIAL_SAVE_DELAY = 1800000;
	private static final int SAVE_INTERVAL = 7200000;
	private static final int SNOWMAN_SHOUT_INTERVAL = 60000;
	private static final int THOMAS_EAT_DELAY = 600000;
	private static final int SATNA_SAY_INTERVAL = 600000;
	private static final int SANTA_ACTION_SPAWN_INTERVAL = 6600000;
	private static final int EVENT_MANAGER_ID = 13184;
	private static final int CTREE_ID = 13006;
	private static final int EVENT_REWARDER_ID = 13186;
	private static final int SNOWMAN_ID = 13160;
	private static final int THOMAS_ID = 13183;
	private static final int[] DROP_ITEM_ID;
	private static final long[] DROP_ITEM_COUNT;
	private static final double DROP_ITEM_CHANCE;
	private static final Location[] _santaSpawn;
	private static final List<Pair<Pair<Integer, Long>, Double>> REWARD;
	private static final double REWARD_CHANCE_SUM;
	private static final ReentrantLock _loc;
	public static SnowmanState _snowmanState;
	private static ScheduledFuture<?> _snowmanShoutTask;
	private static ScheduledFuture<?> _saveTask;
	private static ScheduledFuture<?> _sayTask;
	private static ScheduledFuture<?> _eatTask;
	private static ScheduledFuture<?> _santaTask;
	private static NpcInstance _snowman;
	private static NpcInstance _thomas;
	private static boolean _active;
	
	static
	{
		DROP_ITEM_ID = new int[] {17130, 17131, 17132, 17133, 17134, 17135, 17136, 17137};
		DROP_ITEM_COUNT = new long[] {1, 1, 1, 1, 1, 1, 1, 1};
		DROP_ITEM_CHANCE = 10.0;
		_active = false;
		_santaSpawn = new Location[] {new Location(82632, 148712, -3472), new Location(147448, 28552, -2272), new Location(145873, -54756, -2807)};
		REWARD = Arrays.asList(new Pair[] {new ImmutablePair(new ImmutablePair(14612, 1), 20.0), new ImmutablePair(new ImmutablePair(9627, 1), 10.0), new ImmutablePair(new ImmutablePair(5561, 1), 8.0), new ImmutablePair(new ImmutablePair(5560, 1), 5.0), new ImmutablePair(new ImmutablePair(22206, 1), 5.0), new ImmutablePair(new ImmutablePair(22207, 1), 5.0), new ImmutablePair(new ImmutablePair(21587, 1), 5.0), new ImmutablePair(new ImmutablePair(21588, 1), 5.0), new ImmutablePair(new ImmutablePair(21583, 1), 5.0), new ImmutablePair(new ImmutablePair(21126, 1), 5.0), new ImmutablePair(new ImmutablePair(21124, 1), 5.0), new ImmutablePair(new ImmutablePair(20020, 1), 5.0), new ImmutablePair(new ImmutablePair(13490, 1), 5.0), new ImmutablePair(new ImmutablePair(21618, 1), 1.0), new ImmutablePair(new ImmutablePair(21924, 1), 1.0), new ImmutablePair(new ImmutablePair(21925, 1), 1.0), new ImmutablePair(new ImmutablePair(21926, 1), 1.0), new ImmutablePair(new ImmutablePair(21944, 1), 1.0), new ImmutablePair(new ImmutablePair(21945, 1), 1.0), new ImmutablePair(new ImmutablePair(21946, 1), 1.0), new ImmutablePair(new ImmutablePair(21938, 1), 1.0), new ImmutablePair(new ImmutablePair(21956, 1), 1.0), new ImmutablePair(new ImmutablePair(21963, 1), 1.0), new ImmutablePair(new ImmutablePair(21964, 1), 1.0), new ImmutablePair(new ImmutablePair(21965, 1), 1.0)});
		Collections.sort(REWARD, RandomUtils.DOUBLE_GROUP_COMPARATOR);
		double sum = 0.0;
		for(Pair<Pair<Integer, Long>, Double> e : REWARD)
		{
			sum += e.getRight().doubleValue();
		}
		REWARD_CHANCE_SUM = sum;
		_loc = new ReentrantLock();
	}
	
	private static boolean isActive()
	{
		return IsActive("SavingSnowman");
	}
	
	public static void spawnRewarder(Player rewarded)
	{
		for(NpcInstance npc : rewarded.getAroundNpc(1500, 300))
		{
			if(npc.getNpcId() != 13186)
				continue;
			return;
		}
		Location spawnLoc = Location.findPointToStay(rewarded, 300, 400);
		for(int i = 0;i < 20 && !GeoEngine.canSeeCoord(rewarded, spawnLoc.x, spawnLoc.y, spawnLoc.z, false);++i)
		{
			spawnLoc = Location.findPointToStay(rewarded, 300, 400);
		}
		NpcTemplate template = NpcHolder.getInstance().getTemplate(13186);
		if(template == null)
		{
			_log.info("WARNING! events.SavingSnowman.spawnRewarder template is null for npc: 13186");
			Thread.dumpStack();
			return;
		}
		NpcInstance rewarder = new NpcInstance(IdFactory.getInstance().getNextId(), template);
		rewarder.setLoc(spawnLoc);
		rewarder.setHeading((int) (Math.atan2(spawnLoc.y - rewarded.getY(), spawnLoc.x - rewarded.getX()) * 10430.378350470453) + 32768);
		rewarder.spawnMe();
		Functions.npcSayCustomMessage(rewarder, "scripts.events.SavingSnowman.RewarderPhrase1", (Object[]) new Object[0]);
		Location targetLoc = Location.findPointToStay(rewarded, 40, 50);
		rewarder.setSpawnedLoc(targetLoc);
		rewarder.broadcastPacket(new CharMoveToLocation(rewarder.getObjectId(), rewarder.getLoc(), targetLoc));
		executeTask("events.SavingSnowman.SavingSnowman", "reward", new Object[] {rewarder, rewarded}, (long) 5000);
	}
	
	public static void reward(NpcInstance rewarder, Player rewarded)
	{
		if(!_active || rewarder == null || rewarded == null)
		{
			return;
		}
		Functions.npcSayCustomMessage(rewarder, "scripts.events.SavingSnowman.RewarderPhrase2", (Object[]) new Object[] {rewarded.getName()});
		Functions.addItem(rewarded, 14616, (long) 1);
		executeTask("events.SavingSnowman.SavingSnowman", "removeRewarder", new Object[] {rewarder}, (long) 5000);
	}
	
	public static void removeRewarder(NpcInstance rewarder)
	{
		if(!_active || rewarder == null)
		{
			return;
		}
		Functions.npcSayCustomMessage(rewarder, "scripts.events.SavingSnowman.RewarderPhrase3", (Object[]) new Object[0]);
		Location loc = rewarder.getSpawnedLoc();
		double radian = PositionUtils.convertHeadingToRadian(rewarder.getHeading());
		int x = loc.x - (int) (Math.sin(radian) * 300.0);
		int y = loc.y + (int) (Math.cos(radian) * 300.0);
		int z = loc.z;
		rewarder.broadcastPacket(new CharMoveToLocation(rewarder.getObjectId(), loc, new Location(x, y, z)));
		executeTask("events.SavingSnowman.SavingSnowman", "unspawnRewarder", new Object[] {rewarder}, (long) 2000);
	}
	
	public static void unspawnRewarder(NpcInstance rewarder)
	{
		if(!_active || rewarder == null)
		{
			return;
		}
		rewarder.decayMe();
		rewarder.deleteMe();
	}
	
	private static Location getRandomSpawnPoint()
	{
		ArrayList<ZoneTemplate> zones = new ArrayList<>();
		for(ZoneTemplate zt : ZoneHolder.getInstance().getZones().values())
		{
			if(zt == null || !zt.isDefault())
				continue;
			zones.add(zt);
		}
		Territory terr = zones.get(Rnd.get(zones.size())).getTerritory();
		return terr.getRandomLoc(0);
	}
	
	public static void eatSnowman()
	{
		if(_snowman == null || _thomas == null)
		{
			return;
		}
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.SavingSnowman.AnnounceSnowmanKilled", null, ChatType.ANNOUNCEMENT);
		}
		_snowmanState = SnowmanState.KILLED;
		if(_snowmanShoutTask != null)
		{
			_snowmanShoutTask.cancel(true);
			_snowmanShoutTask = null;
		}
		_snowman.deleteMe();
		_thomas.deleteMe();
	}
	
	public static void freeSnowman(Creature topDamager)
	{
		_loc.lock();
		try
		{
			if(_snowmanState != SnowmanState.CAPTURED || _snowman == null || topDamager == null || !topDamager.isPlayable())
			{
				return;
			}
			_snowmanState = SnowmanState.SAVED;
			for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			{
				Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.SavingSnowman.AnnounceSnowmanSaved", null, ChatType.ANNOUNCEMENT);
			}
			if(_snowmanShoutTask != null)
			{
				_snowmanShoutTask.cancel(true);
				_snowmanShoutTask = null;
			}
			if(_eatTask != null)
			{
				_eatTask.cancel(true);
				_eatTask = null;
			}
			Player player = topDamager.getPlayer();
			Functions.npcSayCustomMessage(_snowman, "scripts.events.SavingSnowman.SnowmanSayTnx", (Object[]) new Object[] {player.getName()});
			addItem(player, 14616, (long) 5);
			if(topDamager.isPlayer())
			{
				spawnRewarder(topDamager.getPlayer());
			}
			_snowman.decayMe();
			_snowman.deleteMe();
		}
		finally
		{
			_loc.unlock();
		}
	}
	
	@Override
	public void onLoad()
	{
		if(isActive())
		{
			_active = true;
			spawnEventManagers();
			_log.info("Loaded Event: SavingSnowman [state: activated]");
			_saveTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SaveTask(), 1800000, 7200000);
			_sayTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SayTask(), 600000, 600000);
			_santaTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SantaTask(), 6600000, 6600000);
			_snowmanState = SnowmanState.SAVED;
			CharListenerList.addGlobal(this);
			CharListenerList.addGlobal(this);
		}
		else
		{
			_log.info("Loaded Event: SavingSnowman [state: deactivated]");
		}
	}
	
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive("SavingSnowman", true))
		{
			spawnEventManagers();
			_log.info("Event 'SavingSnowman' started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.SavingSnowman.AnnounceEventStarted", null);
			if(_saveTask == null)
			{
				_saveTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SaveTask(), 1800000, 7200000);
			}
			if(_sayTask == null)
			{
				_sayTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SayTask(), 600000, 600000);
			}
			if(_santaTask == null)
			{
				_santaTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SantaTask(), 6600000, 6600000);
			}
			_snowmanState = SnowmanState.SAVED;
			CharListenerList.addGlobal(this);
			CharListenerList.addGlobal(this);
		}
		else
		{
			player.sendMessage("Event 'SavingSnowman' already started.");
		}
		_active = true;
		show("admin/events/events.htm", player);
	}
	
	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive("SavingSnowman", false))
		{
			unSpawnEventManagers();
			if(_snowman != null)
			{
				_snowman.deleteMe();
			}
			if(_thomas != null)
			{
				_thomas.deleteMe();
			}
			_log.info("Event 'SavingSnowman' stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.SavingSnowman.AnnounceEventStoped", null);
			if(_saveTask != null)
			{
				_saveTask.cancel(true);
				_saveTask = null;
			}
			if(_sayTask != null)
			{
				_sayTask.cancel(true);
				_sayTask = null;
			}
			if(_eatTask != null)
			{
				_eatTask.cancel(true);
				_eatTask = null;
			}
			if(_santaTask != null)
			{
				_santaTask.cancel(true);
				_santaTask = null;
			}
			_snowmanState = SnowmanState.SAVED;
			CharListenerList.removeGlobal(this);
			CharListenerList.removeGlobal(this);
		}
		else
		{
			player.sendMessage("Event 'SavingSnowman' not started.");
		}
		_active = false;
		show("admin/events/events.htm", player);
	}
	
	private void spawnEventManagers()
	{
		int[][] EVENT_MANAGERS = {{83640, 147976, -3400, 24575}, {146405, 28360, -2269, 49648}, {19319, 144919, -3103, 31135}, {-82805, 149890, -3129, 16384}, {-12347, 122549, -3104, 16384}, {110642, 220165, -3655, 61898}, {116619, 75463, -2721, 20881}, {85513, 16014, -3668, 23681}, {81999, 53793, -1496, 61621}, {148159, -55484, -2734, 44315}, {44185, -48502, -797, 27479}, {86899, -143229, -1293, 8192}};
		SpawnNPCs(13184, EVENT_MANAGERS, _spawns);
		int[][] CTREES = {{83656, 148056, -3400, 24576}, {83204, 147912, -3368, 32768}, {83204, 148395, -3368, 32768}, {83204, 148844, -3368, 32768}, {83202, 149325, -3368, 32768}, {146445, 28360, -2269, 0}, {19319, 144959, -3103, 0}, {-82845, 149890, -3129, 0}, {-12387, 122549, -3104, 0}, {110602, 220165, -3655, 0}, {116659, 75463, -2721, 0}, {85553, 16014, -3668, 0}, {81999, 53743, -1496, 0}, {148199, -55484, -2734, 0}, {44185, -48542, -797, 0}, {86859, -143229, -1293, 0}};
		SpawnNPCs(13006, CTREES, _spawns);
	}
	
	private void unSpawnEventManagers()
	{
		deSpawnNPCs(_spawns);
	}
	
	@Override
	public void onReload()
	{
		unSpawnEventManagers();
		if(_saveTask != null)
		{
			_saveTask.cancel(true);
		}
		if(_sayTask != null)
		{
			_sayTask.cancel(true);
		}
		if(_santaTask != null)
		{
			_santaTask.cancel(true);
		}
		_snowmanState = SnowmanState.SAVED;
	}
	
	@Override
	public void onShutdown()
	{
		CharListenerList.removeGlobal(this);
		CharListenerList.removeGlobal(this);
		unSpawnEventManagers();
	}
	
	public void buff()
	{
		Player player = getSelf();
		if(!_active || player.isActionsDisabled() || player.isSitting() || player.getLastNpc() == null || player.getLastNpc().getDistance(player) > 300.0)
		{
			return;
		}
		if(!player.isQuestContinuationPossible(true))
		{
			return;
		}
		if(_snowmanState != SnowmanState.SAVED)
		{
			show("default/13184-3.htm", player);
			return;
		}
		Summon pet = player.getPet();
		if(pet != null)
		{
			
		}
	}
	
	public void locateSnowman()
	{
		Player player = getSelf();
		if(!_active || player.isActionsDisabled() || player.isSitting() || player.getLastNpc() == null || player.getLastNpc().getDistance(player) > 300.0)
		{
			return;
		}
		if(_snowman != null)
		{
			player.sendPacket(new RadarControl(2, 2, _snowman.getLoc()), new RadarControl(0, 1, _snowman.getLoc()));
			player.sendPacket(new SystemMessage(2010).addZoneName(_snowman.getLoc()).addString("Снеговика захватили в "));
		}
		else
		{
			player.sendPacket(Msg.YOUR_TARGET_CANNOT_BE_FOUND);
		}
	}
	
	public void lotery()
	{
		Player player = getSelf();
		if(!_active || player.isActionsDisabled() || player.isSitting() || player.getLastNpc() == null || player.getLastNpc().getDistance(player) > 300.0)
		{
			return;
		}
		if(!player.isQuestContinuationPossible(true))
		{
			return;
		}
		if(getItemCount(player, Config.EVENT_SAVING_SNOWMAN_LOTERY_CURENCY) < Config.EVENT_SAVING_SNOWMAN_LOTERY_PRICE)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
			return;
		}
		removeItem(player, Config.EVENT_SAVING_SNOWMAN_LOTERY_CURENCY, Config.EVENT_SAVING_SNOWMAN_LOTERY_PRICE);
		if(REWARD_CHANCE_SUM != 100.0)
		{
			_log.info("WARN: Sum != 100: " + REWARD_CHANCE_SUM);
		}
		Pair reward = RandomUtils.pickRandomSortedGroup(REWARD, REWARD_CHANCE_SUM);
		int rewardItemId = (Integer) reward.getKey();
		long rewardItemCount = (Long) reward.getValue();
		addItem(player, rewardItemId, rewardItemCount);
	}
	
	public String DialogAppend_13184(Integer val)
	{
		if(val != 0)
		{
			return "";
		}
		return " (" + Util.formatAdena(Config.EVENT_SAVING_SNOWMAN_LOTERY_PRICE) + " Adena)";
	}
	
	public void captureSnowman()
	{
		Location spawnPoint = getRandomSpawnPoint();
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.SavingSnowman.AnnounceSnowmanCaptured", null, ChatType.ANNOUNCEMENT);
			player.sendPacket(new SystemMessage(2010).addZoneName(spawnPoint).addString("Ищите Снеговика в "));
			player.sendPacket(new RadarControl(2, 2, spawnPoint), new RadarControl(0, 1, spawnPoint));
		}
		NpcTemplate template = NpcHolder.getInstance().getTemplate(13160);
		if(template == null)
		{
			_log.info("WARNING! events.SavingSnowman.captureSnowman template is null for npc: 13160");
			Thread.dumpStack();
			return;
		}
		SimpleSpawner sp = new SimpleSpawner(template);
		sp.setLoc(spawnPoint);
		sp.setAmount(1);
		sp.setRespawnDelay(0);
		_snowman = sp.doSpawn(true);
		if(_snowman == null)
		{
			return;
		}
		template = NpcHolder.getInstance().getTemplate(13183);
		if(template == null)
		{
			_log.info("WARNING! events.SavingSnowman.captureSnowman template is null for npc: 13183");
			Thread.dumpStack();
			return;
		}
		Location pos = Location.findPointToStay(_snowman.getX(), _snowman.getY(), _snowman.getZ(), 100, 120, _snowman.getReflection().getGeoIndex());
		SimpleSpawner sp2 = new SimpleSpawner(template);
		sp2.setLoc(pos);
		sp2.setAmount(1);
		sp2.setRespawnDelay(0);
		_thomas = sp2.doSpawn(true);
		if(_thomas == null)
		{
			return;
		}
		_snowmanState = SnowmanState.CAPTURED;
		if(_snowmanShoutTask != null)
		{
			_snowmanShoutTask.cancel(true);
			_snowmanShoutTask = null;
		}
		_snowmanShoutTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ShoutTask(), 1, 60000);
		if(_eatTask != null)
		{
			_eatTask.cancel(true);
			_eatTask = null;
		}
		_eatTask = executeTask("events.SavingSnowman.SavingSnowman", "eatSnowman", new Object[0], (long) 600000);
	}
	
	@Override
	public void onDeath(Creature actor, Creature killer)
	{
		if(_active && killer != null && _active && SimpleCheckDrop(actor, killer) && Rnd.chance(DROP_ITEM_CHANCE))
		{
			int item_idx = Rnd.get(DROP_ITEM_ID.length);
			((NpcInstance) actor).dropItem(killer.getPlayer(), DROP_ITEM_ID[item_idx], DROP_ITEM_COUNT[item_idx]);
		}
	}
	
	@Override
	public void onPlayerEnter(Player player)
	{
		if(_active)
		{
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.SavingSnowman.AnnounceEventStarted", null);
		}
	}
	
	public enum SnowmanState
	{
		CAPTURED,
		KILLED,
		SAVED;
	}
	
	public class SaveTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!_active || _snowmanState == SnowmanState.CAPTURED)
			{
				return;
			}
			captureSnowman();
		}
	}
	
	public class ShoutTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!_active || _snowman == null || _snowmanState != SnowmanState.CAPTURED)
			{
				return;
			}
			Functions.npcShoutCustomMessage(_snowman, "scripts.events.SavingSnowman.SnowmanShout", (Object[]) new Object[0]);
		}
	}
	
	public class SantaTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!_active)
			{
				return;
			}
			for(Location loc : _santaSpawn)
			{
				NpcInstance npc = NpcHolder.getInstance().getTemplate(13186).getNewInstance();
				npc.setAI(new FlyingSantaAI(npc));
				npc.spawnMe(loc);
				npc.setSpawnedLoc(loc);
			}
		}
	}
	
	public class SayTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!_active)
			{
				return;
			}
			for(SimpleSpawner s : _spawns)
			{
				if(s.getCurrentNpcId() != 13184)
					continue;
				Functions.npcSayCustomMessage(s.getLastSpawn(), "scripts.events.SavingSnowman.SantaSay", (Object[]) new Object[0]);
			}
		}
	}
}