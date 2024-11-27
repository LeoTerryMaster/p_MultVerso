package events.TvT;

import l2.commons.geometry.Polygon;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.instancemanager.ServerVariables;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.listener.actor.player.OnPlayerExitListener;
import l2.gameserver.listener.actor.player.OnTeleportListener;
import l2.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Territory;
import l2.gameserver.model.Zone;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.Revive;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.PositionUtils;
import l2.gameserver.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

public class TvT extends Functions implements ScriptFile, OnDeathListener, OnTeleportListener, OnPlayerExitListener
{
	private static final Logger _log = LoggerFactory.getLogger(TvT.class);
	private static final String EVENT_NAME = "TvT";
	private static final Zone _zone;
	private static final ZoneListener _zoneListener;
	private static final Territory team1spawn;
	private static final Territory team2spawn;
	private static ScheduledFuture<?> _startTask;
	private static List<Long> players_list1;
	private static List<Long> players_list2;
	private static List<Long> live_list1;
	private static List<Long> live_list2;
	private static boolean _isRegistrationActive;
	private static int _status;
	private static int _time_to_start;
	private static int _category;
	private static int _minLevel;
	private static int _maxLevel;
	private static int _autoContinue;
	private static ScheduledFuture<?> _endTask;
	private static boolean _active;
	
	static
	{
		players_list1 = new CopyOnWriteArrayList<>();
		players_list2 = new CopyOnWriteArrayList<>();
		live_list1 = new CopyOnWriteArrayList<>();
		live_list2 = new CopyOnWriteArrayList<>();
		_isRegistrationActive = false;
		_status = 0;
		_autoContinue = 0;
		_zone = ReflectionUtils.getZone("[colosseum_battle]");
		_zoneListener = new ZoneListener();
		team1spawn = new Territory().add(new Polygon().add(149878, 47505).add(150262, 47513).add(150502, 47233).add(150507, 46300).add(150256, 46002).add(149903, 46005).setZmin(-3408).setZmax(-3308));
		team2spawn = new Territory().add(new Polygon().add(149027, 46005).add(148686, 46003).add(148448, 46302).add(148449, 47231).add(148712, 47516).add(149014, 47527).setZmin(-3408).setZmax(-3308));
		_active = false;
	}
	
	private static boolean isActive()
	{
		return IsActive("TvT");
	}
	
	public static boolean isRunned()
	{
		return _isRegistrationActive || _status > 0;
	}
	
	public static int getMinLevelForCategory(int category)
	{
		switch(category)
		{
			case 1:
			{
				return 20;
			}
			case 2:
			{
				return 30;
			}
			case 3:
			{
				return 40;
			}
			case 4:
			{
				return 52;
			}
			case 5:
			{
				return 62;
			}
			case 6:
			{
				return 76;
			}
		}
		return 0;
	}
	
	public static int getMaxLevelForCategory(int category)
	{
		switch(category)
		{
			case 1:
			{
				return 29;
			}
			case 2:
			{
				return 39;
			}
			case 3:
			{
				return 51;
			}
			case 4:
			{
				return 61;
			}
			case 5:
			{
				return 75;
			}
			case 6:
			{
				return 85;
			}
		}
		return 0;
	}
	
	public static int getCategory(int level)
	{
		if(level >= 20 && level <= 29)
		{
			return 1;
		}
		if(level >= 30 && level <= 39)
		{
			return 2;
		}
		if(level >= 40 && level <= 51)
		{
			return 3;
		}
		if(level >= 52 && level <= 61)
		{
			return 4;
		}
		if(level >= 62 && level <= 75)
		{
			return 5;
		}
		if(level >= 76)
		{
			return 6;
		}
		return 0;
	}
	
	public static void sayToAll(String address, String[] replacements)
	{
		Announcements.getInstance().announceByCustomMessage(address, replacements, ChatType.CRITICAL_ANNOUNCE);
	}
	
	public static void question()
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player == null || player.isDead() || player.getLevel() < _minLevel || player.getLevel() > _maxLevel || !player.getReflection().isDefault() || player.isOlyParticipant() || player.isInObserverMode())
				continue;
			player.scriptRequest(new CustomMessage("scripts.events.TvT.AskPlayer", player, new Object[0]).toString(), "events.TvT.TvT:addPlayer", new Object[0]);
		}
	}
	
	public static void announce()
	{
		if(players_list1.isEmpty() || players_list2.isEmpty())
		{
			sayToAll("scripts.events.TvT.AnnounceEventCancelled", null);
			_isRegistrationActive = false;
			_status = 0;
			executeTask("events.TvT.TvT", "autoContinue", new Object[0], (long) 10000);
			return;
		}
		if(_time_to_start > 1)
		{
			String[] param = {String.valueOf(--_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel)};
			sayToAll("scripts.events.TvT.AnnouncePreStart", param);
			executeTask("events.TvT.TvT", "announce", new Object[0], (long) 60000);
		}
		else
		{
			_status = 1;
			_isRegistrationActive = false;
			sayToAll("scripts.events.TvT.AnnounceEventStarting", null);
			executeTask("events.TvT.TvT", "prepare", new Object[0], (long) 5000);
		}
	}
	
	public static boolean checkPlayer(Player player, boolean first)
	{
		if(first && (!_isRegistrationActive || player.isDead()))
		{
			show(new CustomMessage("scripts.events.Late", player), player);
			return false;
		}
		if(first && (players_list1.contains(player.getStoredId()) || players_list2.contains(player.getStoredId())))
		{
			show(new CustomMessage("scripts.events.TvT.Cancelled", player), player);
			return false;
		}
		if(player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
		{
			show(new CustomMessage("scripts.events.TvT.CancelledLevel", player), player);
			return false;
		}
		if(player.isMounted())
		{
			show(new CustomMessage("scripts.events.TvT.Cancelled", player), player);
			return false;
		}
		if(player.isInDuel())
		{
			show(new CustomMessage("scripts.events.TvT.CancelledDuel", player), player);
			return false;
		}
		if(player.getTeam() != TeamType.NONE)
		{
			show(new CustomMessage("scripts.events.TvT.CancelledOtherEvent", player), player);
			return false;
		}
		if(player != null && !player.isDead() && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().isDefault() && !player.isOlyParticipant() && !player.isInObserverMode())
		{
			show(new CustomMessage("scripts.events.TvT.CancelledOlympiad", player), player);
			return false;
		}
		if(player.isInParty() && player.getParty().isInDimensionalRift())
		{
			show(new CustomMessage("scripts.events.TvT.CancelledOtherEvent", player), player);
			return false;
		}
		if(player.isTeleporting())
		{
			show(new CustomMessage("scripts.events.TvT.CancelledTeleport", player), player);
			return false;
		}
		return true;
	}
	
	public static void prepare()
	{
		ReflectionUtils.getDoor(24190002).closeMe();
		ReflectionUtils.getDoor(24190003).closeMe();
		cleanPlayers();
		clearArena();
		executeTask("events.TvT.TvT", "ressurectPlayers", new Object[0], (long) 1000);
		executeTask("events.TvT.TvT", "healPlayers", new Object[0], (long) 2000);
		executeTask("events.TvT.TvT", "paralyzePlayers", new Object[0], (long) 4000);
		executeTask("events.TvT.TvT", "teleportPlayersToColiseum", new Object[0], (long) 5000);
		executeTask("events.TvT.TvT", "go", new Object[0], (long) 60000);
		sayToAll("scripts.events.TvT.AnnounceFinalCountdown", null);
	}
	
	public static void go()
	{
		_status = 2;
		upParalyzePlayers();
		checkLive();
		clearArena();
		sayToAll("scripts.events.TvT.AnnounceFight", null);
		_endTask = executeTask("events.TvT.TvT", "endBattle", new Object[0], (long) 300000);
	}
	
	public static void endBattle()
	{
		ReflectionUtils.getDoor(24190002).openMe();
		ReflectionUtils.getDoor(24190003).openMe();
		_status = 0;
		removeAura();
		if(live_list1.isEmpty())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedBlueWins", null);
			giveItemsToWinner(false, true, 1.0);
		}
		else if(live_list2.isEmpty())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedRedWins", null);
			giveItemsToWinner(true, false, 1.0);
		}
		else if(live_list1.size() < live_list2.size())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedBlueWins", null);
			giveItemsToWinner(false, true, 1.0);
		}
		else if(live_list1.size() > live_list2.size())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedRedWins", null);
			giveItemsToWinner(true, false, 1.0);
		}
		else if(live_list1.size() == live_list2.size())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedDraw", null);
			giveItemsToWinner(true, true, 0.5);
		}
		sayToAll("scripts.events.TvT.AnnounceEnd", null);
		executeTask("events.TvT.TvT", "end", new Object[0], (long) 30000);
		_isRegistrationActive = false;
		if(_endTask != null)
		{
			_endTask.cancel(false);
			_endTask = null;
		}
	}
	
	public static void end()
	{
		executeTask("events.TvT.TvT", "ressurectPlayers", new Object[0], (long) 1000);
		executeTask("events.TvT.TvT", "healPlayers", new Object[0], (long) 2000);
		executeTask("events.TvT.TvT", "teleportPlayers", new Object[0], (long) 3000);
		executeTask("events.TvT.TvT", "autoContinue", new Object[0], (long) 10000);
	}
	
	public static void giveItemsToWinner(boolean team1, boolean team2, double rate)
	{
		if(team1)
		{
			for(Player player : getPlayers(players_list1))
			{
				addItem(player, Config.EVENT_TvTItemID, Math.round((double) (Config.EVENT_TvT_rate ? player.getLevel() : 1) * Config.EVENT_TvTItemCOUNT * rate));
			}
		}
		if(team2)
		{
			for(Player player : getPlayers(players_list2))
			{
				addItem(player, Config.EVENT_TvTItemID, Math.round((double) (Config.EVENT_TvT_rate ? player.getLevel() : 1) * Config.EVENT_TvTItemCOUNT * rate));
			}
		}
	}
	
	public static void teleportPlayersToColiseum()
	{
		for(Player player : getPlayers(players_list1))
		{
			unRide(player);
			unSummonPet(player, true);
			player.teleToLocation(Territory.getRandomLoc(team1spawn));
		}
		for(Player player : getPlayers(players_list2))
		{
			unRide(player);
			unSummonPet(player, true);
			player.teleToLocation(Territory.getRandomLoc(team2spawn));
		}
	}
	
	public static void teleportPlayers()
	{
		for(Player player : getPlayers(players_list1))
		{
			player.teleToLocation(151480, 46712, -3400);
		}
		for(Player player : getPlayers(players_list2))
		{
			player.teleToLocation(147640, 46712, -3400);
		}
	}
	
	public static void paralyzePlayers()
	{
		Skill revengeSkill = SkillTable.getInstance().getInfo(4515, 1);
		for(Player player : getPlayers(players_list1))
		{
			player.getEffectList().stopEffect(1411);
			revengeSkill.getEffects(player, player, false, false);
			if(player.getPet() == null)
				continue;
			revengeSkill.getEffects(player, player.getPet(), false, false);
		}
		for(Player player : getPlayers(players_list2))
		{
			player.getEffectList().stopEffect(1411);
			revengeSkill.getEffects(player, player, false, false);
			if(player.getPet() == null)
				continue;
			revengeSkill.getEffects(player, player.getPet(), false, false);
		}
	}
	
	public static void upParalyzePlayers()
	{
		for(Player player : getPlayers(players_list1))
		{
			player.getEffectList().stopEffect(4515);
			if(player.getPet() != null)
			{
				player.getPet().getEffectList().stopEffect(4515);
			}
			player.leaveParty();
		}
		for(Player player : getPlayers(players_list2))
		{
			player.getEffectList().stopEffect(4515);
			if(player.getPet() != null)
			{
				player.getPet().getEffectList().stopEffect(4515);
			}
			player.leaveParty();
		}
	}
	
	public static void ressurectPlayers()
	{
		for(Player player : getPlayers(players_list1))
		{
			if(!player.isDead())
				continue;
			player.restoreExp();
			player.setCurrentCp((double) player.getMaxCp());
			player.setCurrentHp((double) player.getMaxHp(), true);
			player.setCurrentMp((double) player.getMaxMp());
			player.broadcastPacket(new Revive(player));
		}
		for(Player player : getPlayers(players_list2))
		{
			if(!player.isDead())
				continue;
			player.restoreExp();
			player.setCurrentCp((double) player.getMaxCp());
			player.setCurrentHp((double) player.getMaxHp(), true);
			player.setCurrentMp((double) player.getMaxMp());
			player.broadcastPacket(new Revive(player));
		}
	}
	
	public static void healPlayers()
	{
		for(Player player : getPlayers(players_list1))
		{
			player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
			player.setCurrentCp((double) player.getMaxCp());
		}
		for(Player player : getPlayers(players_list2))
		{
			player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
			player.setCurrentCp((double) player.getMaxCp());
		}
	}
	
	public static void cleanPlayers()
	{
		for(Player player : getPlayers(players_list1))
		{
			if(checkPlayer(player, false))
				continue;
			removePlayer(player);
		}
		for(Player player : getPlayers(players_list2))
		{
			if(checkPlayer(player, false))
				continue;
			removePlayer(player);
		}
	}
	
	public static void checkLive()
	{
		Player player;
		CopyOnWriteArrayList<Long> new_live_list1 = new CopyOnWriteArrayList<>();
		for(Long storeId : live_list1)
		{
			player = GameObjectsStorage.getAsPlayer(storeId);
			if(player == null)
				continue;
			new_live_list1.add(storeId);
		}
		CopyOnWriteArrayList<Long> new_live_list2 = new CopyOnWriteArrayList<>();
		for(Long storeId : live_list2)
		{
			player = GameObjectsStorage.getAsPlayer(storeId);
			if(player == null)
				continue;
			new_live_list2.add(storeId);
		}
		live_list1 = new_live_list1;
		live_list2 = new_live_list2;
		for(Player player2 : getPlayers(live_list1))
		{
			if(player2.isInZone(_zone) && !player2.isDead() && !player2.isLogoutStarted())
			{
				player2.setTeam(TeamType.RED);
				continue;
			}
			loosePlayer(player2);
		}
		for(Player player2 : getPlayers(live_list2))
		{
			if(player2.isInZone(_zone) && !player2.isDead() && !player2.isLogoutStarted())
			{
				player2.setTeam(TeamType.BLUE);
				continue;
			}
			loosePlayer(player2);
		}
		if(live_list1.size() < 1 || live_list2.size() < 1)
		{
			endBattle();
		}
	}
	
	public static void removeAura()
	{
		for(Player player : getPlayers(live_list1))
		{
			player.setTeam(TeamType.NONE);
		}
		for(Player player : getPlayers(live_list2))
		{
			player.setTeam(TeamType.NONE);
		}
	}
	
	public static void clearArena()
	{
		for(Creature obj : _zone.getObjects())
		{
			Player player;
			if(obj == null || (player = obj.getPlayer()) == null || live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId()))
				continue;
			player.teleToLocation(147451, 46728, -3410);
		}
	}
	
	private static void loosePlayer(Player player)
	{
		if(player != null)
		{
			live_list1.remove(player.getStoredId());
			live_list2.remove(player.getStoredId());
			player.setTeam(TeamType.NONE);
			show(new CustomMessage("scripts.events.TvT.YouLose", player), player);
		}
	}
	
	private static void removePlayer(Player player)
	{
		if(player != null)
		{
			live_list1.remove(player.getStoredId());
			live_list2.remove(player.getStoredId());
			players_list1.remove(player.getStoredId());
			players_list2.remove(player.getStoredId());
			player.setTeam(TeamType.NONE);
		}
	}
	
	private static List<Player> getPlayers(List<Long> list)
	{
		ArrayList<Player> result = new ArrayList<>();
		for(Long storeId : list)
		{
			Player player = GameObjectsStorage.getAsPlayer(storeId);
			if(player == null)
				continue;
			result.add(player);
		}
		return result;
	}
	
	@Override
	public void onLoad()
	{
		if(isActive())
		{
			_active = true;
			CharListenerList.addGlobal(this);
			_zone.addListener(_zoneListener);
			_startTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new StartTask(), 3600000, 3600000);
			_active = ServerVariables.getString("TvT", "off").equalsIgnoreCase("on");
			_log.info("Loaded Event: TvT [state: active]");
		}
		else
		{
			_active = false;
			_log.info("Loaded Event: TvT [state: deactivated]");
		}
	}
	
	@Override
	public void onReload()
	{
		_zone.removeListener(_zoneListener);
		if(_startTask != null)
		{
			_startTask.cancel(false);
			_startTask = null;
		}
	}
	
	@Override
	public void onShutdown()
	{
		onReload();
	}
	
	public void activateEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(!isActive())
		{
			if(_startTask == null)
			{
				_startTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new StartTask(), 3600000, 3600000);
			}
			ServerVariables.set("TvT", "on");
			_log.info("Event 'TvT' activated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.TvT.AnnounceEventStarted", null);
		}
		else
		{
			player.sendMessage("Event 'TvT' already active.");
		}
		_active = true;
		show("admin/events/events.htm", player);
	}
	
	public void deactivateEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(isActive())
		{
			if(_startTask != null)
			{
				_startTask.cancel(false);
				_startTask = null;
			}
			ServerVariables.unset("TvT");
			_log.info("Event 'TvT' deactivated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.TvT.AnnounceEventStoped", null);
		}
		else
		{
			player.sendMessage("Event 'TvT' not active.");
		}
		_active = false;
		show("admin/events/events.htm", player);
	}
	
	public void start(String[] var)
	{
		Player player = getSelf();
		if(var.length != 2)
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}
		Integer category;
		Integer autoContinue;
		try
		{
			category = Integer.valueOf(var[0]);
			autoContinue = Integer.valueOf(var[1]);
		}
		catch(Exception e)
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}
		_category = category;
		_autoContinue = autoContinue;
		if(_category == -1)
		{
			_minLevel = 1;
			_maxLevel = 85;
		}
		else
		{
			_minLevel = getMinLevelForCategory(_category);
			_maxLevel = getMaxLevelForCategory(_category);
		}
		if(_endTask != null)
		{
			show(new CustomMessage("common.TryLater", player), player);
			return;
		}
		_status = 0;
		_isRegistrationActive = true;
		_time_to_start = Config.EVENT_TvTTime;
		players_list1 = new CopyOnWriteArrayList<>();
		players_list2 = new CopyOnWriteArrayList<>();
		live_list1 = new CopyOnWriteArrayList<>();
		live_list2 = new CopyOnWriteArrayList<>();
		String[] param = {String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel)};
		sayToAll("scripts.events.TvT.AnnouncePreStart", param);
		executeTask("events.TvT.TvT", "question", new Object[0], (long) 10000);
		executeTask("events.TvT.TvT", "announce", new Object[0], (long) 60000);
	}
	
	public void addPlayer()
	{
		Player player = getSelf();
		if(player == null || !checkPlayer(player, true))
		{
			return;
		}
		int size1 = players_list1.size();
		int size2 = players_list2.size();
		int team = size1 > size2 ? 2 : size1 < size2 ? 1 : Rnd.get(1, 2);
		if(team == 1)
		{
			players_list1.add(player.getStoredId());
			live_list1.add(player.getStoredId());
			show(new CustomMessage("scripts.events.TvT.Registered", player), player);
		}
		else if(team == 2)
		{
			players_list2.add(player.getStoredId());
			live_list2.add(player.getStoredId());
			show(new CustomMessage("scripts.events.TvT.Registered", player), player);
		}
		else
		{
			_log.info("WTF??? Command id 0 in TvT...");
		}
	}
	
	public void autoContinue()
	{
		live_list1.clear();
		live_list2.clear();
		players_list1.clear();
		players_list2.clear();
		if(_autoContinue > 0)
		{
			if(_autoContinue >= 6)
			{
				_autoContinue = 0;
				return;
			}
			start(new String[] {"" + (_autoContinue + 1), "" + (_autoContinue + 1)});
		}
	}
	
	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if(_status > 1 && self.isPlayer() && self.getTeam() != TeamType.NONE && (live_list1.contains(self.getStoredId()) || live_list2.contains(self.getStoredId())))
		{
			loosePlayer((Player) self);
			checkLive();
		}
	}
	
	@Override
	public void onTeleport(Player player, int x, int y, int z, Reflection reflection)
	{
		if(_zone.checkIfInZone(x, y, z, reflection))
		{
			return;
		}
		if(_status > 1 && player != null && player.getTeam() != TeamType.NONE && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
		{
			removePlayer(player);
			checkLive();
		}
	}
	
	@Override
	public void onPlayerExit(Player player)
	{
		if(player.getTeam() == TeamType.NONE)
		{
			return;
		}
		if(_status == 0 && _isRegistrationActive && player.getTeam() != TeamType.NONE && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
		{
			removePlayer(player);
			return;
		}
		if(_status == 1 && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
		{
			removePlayer(player);
			try
			{
				String var = player.getVar("TvT_backCoords");
				if(var == null || var.equals(""))
				{
					return;
				}
				String[] coords = var.split(" ");
				if(coords.length != 4)
				{
					return;
				}
				player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
				player.unsetVar("TvT_backCoords");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return;
		}
		if(_status > 1 && player != null && player.getTeam() != TeamType.NONE && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
		{
			removePlayer(player);
			checkLive();
		}
	}
	
	private static class TeleportTask extends RunnableImpl
	{
		Location loc;
		Creature target;
		
		public TeleportTask(Creature target, Location loc)
		{
			this.target = target;
			this.loc = loc;
			target.block();
		}
		
		@Override
		public void runImpl() throws Exception
		{
			target.unblock();
			target.teleToLocation(loc);
		}
	}
	
	private static class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			if(cha == null)
			{
				return;
			}
			Player player = cha.getPlayer();
			if(_status > 0 && player != null && !live_list1.contains(player.getStoredId()) && !live_list2.contains(player.getStoredId()))
			{
				ThreadPoolManager.getInstance().schedule(new TeleportTask(cha, new Location(147451, 46728, -3410)), 3000);
			}
		}
		
		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			if(cha == null)
			{
				return;
			}
			Player player = cha.getPlayer();
			if(_status > 1 && player != null && player.getTeam() != TeamType.NONE && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
			{
				double angle = PositionUtils.convertHeadingToDegree(cha.getHeading());
				double radian = Math.toRadians(angle - 90.0);
				int x = (int) ((double) cha.getX() + 50.0 * Math.sin(radian));
				int y = (int) ((double) cha.getY() - 50.0 * Math.cos(radian));
				int z = cha.getZ();
				ThreadPoolManager.getInstance().schedule(new TeleportTask(cha, new Location(x, y, z)), 3000);
			}
		}
	}
	
	public class StartTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if(!_active)
			{
				return;
			}
			if(Functions.isPvPEventStarted())
			{
				_log.info("TvT not started: another event is already running");
				return;
			}
			if(!Rnd.chance(Config.EVENT_TvTChanceToStart))
			{
				_log.debug("TvT not started: chance");
				return;
			}
			for(Residence c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
			{
				if(c.getSiegeEvent() == null || !c.getSiegeEvent().isInProgress())
					continue;
				_log.debug("TvT not started: CastleSiege in progress");
				return;
			}
			start(new String[] {"1", "1"});
		}
	}
}