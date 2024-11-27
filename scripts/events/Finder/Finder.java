package events.Finder;

import l2.commons.listener.Listener;
import l2.commons.util.Rnd;
import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.data.xml.holder.ZoneHolder;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.Territory;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.CharMoveToLocation;
import l2.gameserver.network.l2.s2c.RadarControl;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.templates.ZoneTemplate;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.PositionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class Finder extends Functions implements ScriptFile, OnDeathListener
{
	private static final Logger _log = LoggerFactory.getLogger(Finder.class);
	private static final String EventName = "Finder";
	private static final long CaptureTime = 600000;
	private static final long ShoutTime = 60000;
	private static final int EventManagerNpcID = 40018;
	private static final int HostageNpcID = 40017;
	private static final int RaiderNpcID = 40016;
	private static final int RewarderNpcID = 40019;
	private static final int[][] EventManagerLocs;
	private static boolean _active;
	private static NpcInstance _hostage;
	private static NpcInstance _raider;
	private static Location _hostage_loc;
	private static ArrayList<SimpleSpawner> _em_spawns;
	private static ScheduledFuture<?> _captureTask;
	private static ScheduledFuture<?> _shoutTask;
	private static Map<String, ScheduledFuture<?>> _event_tasks;
	
	static
	{
		EventManagerLocs = new int[][] {{82040, 148888, -3472, 8191}, {146405, 28360, -2269, 49648}, {19319, 144919, -3103, 31135}, {-82805, 149890, -3129, 16384}, {-12347, 122549, -3104, 16384}, {110642, 220165, -3655, 61898}, {116619, 75463, -2721, 20881}, {85513, 16014, -3668, 23681}, {81999, 53793, -1496, 61621}, {148159, -55484, -2734, 44315}, {44185, -48502, -797, 27479}, {86899, -143229, -1293, 8192}};
	}
	
	private static boolean isActive()
	{
		return IsActive("Finder");
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
	
	private static void removeHostageAndRaider()
	{
		if(_hostage != null && _hostage.getSpawn() != null)
		{
			_hostage.deleteMe();
		}
		if(_raider != null && _raider.getSpawn() != null)
		{
			_raider.deleteMe();
		}
		_hostage = null;
		_raider = null;
		_hostage_loc = null;
		if(_shoutTask != null)
		{
			_shoutTask.cancel(true);
			_shoutTask = null;
		}
	}
	
	public static void spawnRewarder(Player rewarded)
	{
		for(NpcInstance npc : rewarded.getAroundNpc(1500, 300))
		{
			if(npc.getNpcId() != 40019)
				continue;
			return;
		}
		Location spawnLoc = Location.findPointToStay(rewarded, 300, 400);
		for(int i = 0;i < 20 && !GeoEngine.canSeeCoord(rewarded, spawnLoc.x, spawnLoc.y, spawnLoc.z, false);++i)
		{
			spawnLoc = Location.findPointToStay(rewarded, 300, 400);
		}
		NpcTemplate template = NpcHolder.getInstance().getTemplate(40019);
		if(template == null)
		{
			_log.info("WARNING! events.SavingSnowman.spawnRewarder template is null for npc: 40019");
			Thread.dumpStack();
			return;
		}
		NpcInstance rewarder = new NpcInstance(IdFactory.getInstance().getNextId(), template);
		rewarder.setLoc(spawnLoc);
		rewarder.setHeading((int) (Math.atan2(spawnLoc.y - rewarded.getY(), spawnLoc.x - rewarded.getX()) * 10430.378350470453) + 32768);
		rewarder.spawnMe();
		Functions.npcSayCustomMessage(rewarder, "scripts.events.Finder.RewarderPhrase1", (Object[]) new Object[0]);
		Location targetLoc = Location.findFrontPosition(rewarded, rewarded, 40, 50);
		rewarder.setSpawnedLoc(targetLoc);
		rewarder.broadcastPacket(new CharMoveToLocation(rewarder.getObjectId(), rewarder.getLoc(), targetLoc));
		executeTask("events.Finder.Finder", "reward", new Object[] {rewarder, rewarded}, (long) 5000);
	}
	
	public static void reward(NpcInstance rewarder, Player rewarded)
	{
		if(!_active || rewarder == null || rewarded == null)
		{
			return;
		}
		Functions.npcSayCustomMessage(rewarder, "scripts.events.Finder.RewarderPhrase2", (Object[]) new Object[] {rewarded.getName()});
		Functions.addItem(rewarded, Config.EVENT_FINDER_REWARD_ID, (long) Config.EVENT_FINDER_ITEM_COUNT);
		executeTask("events.Finder.Finder", "removeRewarder", new Object[] {rewarder}, (long) 5000);
	}
	
	public static void removeRewarder(NpcInstance rewarder)
	{
		if(!_active || rewarder == null)
		{
			return;
		}
		Functions.npcSayCustomMessage(rewarder, "scripts.events.Finder.RewarderPhrase3", (Object[]) new Object[0]);
		Location loc = rewarder.getSpawnedLoc();
		double radian = PositionUtils.convertHeadingToRadian(rewarder.getHeading());
		int x = loc.x - (int) (Math.sin(radian) * 300.0);
		int y = loc.y + (int) (Math.cos(radian) * 300.0);
		int z = loc.z;
		rewarder.broadcastPacket(new CharMoveToLocation(rewarder.getObjectId(), loc, new Location(x, y, z)));
		executeTask("events.Finder.Finder", "unspawnRewarder", new Object[] {rewarder}, (long) 2000);
	}
	
	public static void unspawnRewarder(NpcInstance rewarder)
	{
		if(!_active || rewarder == null)
		{
			return;
		}
		rewarder.decayMe();
		rewarder.deleteMe();
		removeHostageAndRaider();
	}
	
	public static void OnDie(Creature cha, Creature killer)
	{
		if(!_active)
		{
			return;
		}
		if(killer.isPlayer())
		{
			_log.info("killed 0 " + cha.getName());
		}
		if(cha.isNpc() && cha == _raider && killer.isPlayer())
		{
			_log.info("killed 1 " + cha.getName());
			Player saver = killer.getPlayer();
			if(_captureTask != null)
			{
				_captureTask.cancel(true);
				_captureTask = null;
			}
			if(_shoutTask != null)
			{
				_shoutTask.cancel(true);
				_shoutTask = null;
			}
			spawnRewarder(saver);
			_log.info("killed 2 " + cha.getName());
			for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			{
				Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.Finder.AnnounceHostageSavedByS1", new String[] {saver.getName()}, ChatType.ANNOUNCEMENT);
			}
			for(NpcInstance npc : saver.getAroundNpc(1500, 300))
			{
				if(npc.getNpcId() != 40017)
					continue;
				_log.info("killed 3 " + cha.getName());
				Functions.npcSayCustomMessage(npc, "scripts.events.Finder.HostageThx", (Object[]) new Object[0]);
				return;
			}
		}
	}
	
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive("Finder", true))
		{
			activate();
		}
		else
		{
			player.sendMessage("Event 'Finder' already active.");
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
		if(SetActive("Finder", false))
		{
			diactivate();
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.Finder.AnnounceEventStoped", null);
		}
		else
		{
			player.sendMessage("Event 'Finder' already diactivated.");
		}
		_active = false;
		show("admin/events/events.htm", player);
	}
	
	private void activate()
	{
		if(_em_spawns != null)
		{
			deSpawnNPCs(_em_spawns);
		}
		_em_spawns = new ArrayList();
		SpawnNPCs(40018, EventManagerLocs, _em_spawns);
		if(_event_tasks != null)
		{
			for(ScheduledFuture sf : _event_tasks.values())
			{
				sf.cancel(true);
			}
			_event_tasks = null;
		}
		_event_tasks = ScheduleTimeStarts(new EventTask(), Config.EVENT_FinderHostageStartTime);
		_log.info("Event 'Finder' started on " + Arrays.toString(_event_tasks.keySet().toArray(new String[_event_tasks.keySet().size()])));
	}
	
	private void diactivate()
	{
		if(_event_tasks != null)
		{
			for(ScheduledFuture sf : _event_tasks.values())
			{
				sf.cancel(true);
			}
			_event_tasks = null;
		}
		if(_em_spawns != null)
		{
			deSpawnNPCs(_em_spawns);
			_em_spawns.clear();
			_em_spawns = null;
		}
	}
	
	public void spawnHostageAndRaider()
	{
		if(!_active)
		{
			return;
		}
		Location spawnPoint = getRandomSpawnPoint();
		NpcTemplate template = NpcHolder.getInstance().getTemplate(40017);
		if(template == null)
		{
			_log.info("WARNING! events.Finder.Finder.spawnHostageAndRaider template is null for npc: 40017");
			Thread.dumpStack();
			return;
		}
		SimpleSpawner sp = new SimpleSpawner(template);
		sp.setLoc(spawnPoint);
		sp.setAmount(1);
		sp.setRespawnDelay(0);
		sp.stopRespawn();
		_hostage = sp.doSpawn(true);
		if(_hostage == null)
		{
			return;
		}
		template = NpcHolder.getInstance().getTemplate(40016);
		if(template == null)
		{
			_log.info("WARNING! events.Finder.Finder.spawnHostageAndRaider template is null for npc: 40016");
			Thread.dumpStack();
			return;
		}
		Location pos = Location.findPointToStay(_hostage.getX(), _hostage.getY(), _hostage.getZ(), 100, 120, _hostage.getReflection().getGeoIndex());
		SimpleSpawner sp2 = new SimpleSpawner(template);
		sp2.setLoc(pos);
		sp2.setAmount(1);
		sp2.setRespawnDelay(0);
		sp2.stopRespawn();
		_raider = sp2.doSpawn(true);
		_raider.addListener((Listener) this);
		if(_captureTask != null)
		{
			_captureTask.cancel(true);
			_captureTask = null;
		}
		_captureTask = ThreadPoolManager.getInstance().schedule(new HostageKilledTask(), 600000);
		if(_shoutTask != null)
		{
			_shoutTask.cancel(true);
			_shoutTask = null;
		}
		_shoutTask = ThreadPoolManager.getInstance().schedule(new ShoutTask(), 60000);
		_hostage_loc = _hostage.getLoc();
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.Finder.AnnounceHostageTaken", null, ChatType.ANNOUNCEMENT);
		}
	}
	
	public void GetPoint()
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
		if(_hostage_loc != null)
		{
			player.sendPacket(new SystemMessage(2010).addZoneName(_hostage_loc).addString(new CustomMessage("scripts.events.Finder.HostageAtS1", player, new Object[0]).toString()));
			player.sendPacket(new RadarControl(2, 2, _hostage_loc), new RadarControl(0, 1, _hostage_loc));
		}
		else
		{
			player.sendMessage(new CustomMessage("scripts.events.Finder.NoHostage", player));
		}
	}
	
	@Override
	public void onLoad()
	{
		if(isActive())
		{
			_active = true;
			activate();
		}
		else
		{
			_log.info("Loaded Event: 'Finder' [state: deactivated]");
		}
	}
	
	@Override
	public void onReload()
	{
		if(_active)
		{
			diactivate();
			activate();
		}
	}
	
	@Override
	public void onShutdown()
	{
		diactivate();
	}
	
	@Override
	public void onDeath(Creature actor, Creature killer)
	{
		actor.removeListener(this);
		if(!_active || killer == null)
		{
			return;
		}
		if(killer.isPlayer())
		{
			_log.info("killed 0 " + actor.getName());
		}
		if(actor.isNpc() && actor == _raider && killer.isPlayer())
		{
			_log.info("killed 1 " + actor.getName());
			Player saver = killer.getPlayer();
			if(_captureTask != null)
			{
				_captureTask.cancel(true);
				_captureTask = null;
			}
			if(_shoutTask != null)
			{
				_shoutTask.cancel(true);
				_shoutTask = null;
			}
			spawnRewarder(saver);
			_log.info("killed 2 " + actor.getName());
			for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			{
				Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.Finder.AnnounceHostageSavedByS1", new String[] {saver.getName()}, ChatType.ANNOUNCEMENT);
			}
			for(NpcInstance npc : saver.getAroundNpc(1500, 300))
			{
				if(npc.getNpcId() != 40017)
					continue;
				_log.info("killed 3 " + actor.getName());
				Functions.npcSayCustomMessage(npc, "scripts.events.Finder.HostageThx", (Object[]) new Object[0]);
				return;
			}
		}
	}
	
	public class ShoutTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!_active || _hostage == null || _hostage_loc == null)
			{
				return;
			}
			Functions.npcShoutCustomMessage(_hostage, "scripts.events.Finder.HostageShout", (Object[]) new Object[0]);
		}
	}
	
	private class HostageKilledTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!_active || _hostage == null || _raider == null)
			{
				return;
			}
			removeHostageAndRaider();
			for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			{
				Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.Finder.AnnounceHostageKilled", null, ChatType.ANNOUNCEMENT);
			}
		}
	}
	
	private class EventTask implements Runnable
	{
		@Override
		public void run()
		{
			spawnHostageAndRaider();
		}
	}
}