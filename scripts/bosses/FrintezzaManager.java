package bosses;

import handler.items.SimpleItemHandler;
import l2.commons.listener.Listener;
import l2.commons.threading.RunnableImpl;
import l2.commons.time.cron.SchedulingPattern;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.handler.admincommands.AdminCommandHandler;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.handler.items.ItemHandler;
import l2.gameserver.instancemanager.SpawnManager;
import l2.gameserver.listener.CharListener;
import l2.gameserver.listener.actor.OnCurrentHpDamageListener;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Party;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Spawner;
import l2.gameserver.model.Zone;
import l2.gameserver.model.instances.DoorInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.Earthquake;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.network.l2.s2c.MagicSkillCanceled;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.network.l2.s2c.SocialAction;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.NpcUtils;
import l2.gameserver.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class FrintezzaManager implements ScriptFile, IAdminCommandHandler
{
	static final int BREAKING_ARROW_ITEM_ID = 8192;
	static final int DEWDROP_OF_DESSTRUCTION_ID = 8556;
	static final int FRINTEZZA_SEEKER_NPC_ID = 29059;
	static final long BREAKING_ARROW_CANCEL_TIME = 60000;
	private static final Logger LOG = LoggerFactory.getLogger(FrintezzaManager.class);
	private static final FrintezzaManager INSTANCE = new FrintezzaManager();
	private static final Location[] TELEPORT_PARTY_LOCATIONS = {new Location(173247, -76979, -5104, 1000), new Location(174985, -75161, -5104, 1000), new Location(173917, -76109, -5104, 1000), new Location(174037, -76321, -5104, 1000), new Location(173249, -75154, -5104, 1000)};
	private static final Location FRINTEZZA_SEEKER_LOCATION = new Location(174259, -86550, -5088, 16440);
	private static final String LAST_IMPERIAL_TOMB_ZONE_NAME = "[LastImperialTomb]";
	private static final String FRINTESSA_ZONE_NAME = "[Frintezza]";
	private static final String ROOM_A_ALARM_DEVICES_NAME = "[last_imperial_tomb_a_alarm_devices]";
	private static final String ROOM_A_HALL_KEEPERS_NAME = "[last_imperial_tomb_a_hall_keeper]";
	private static final String ROOM_B_UNDEADBAND_PLAYERS = "[last_imperial_tomb_b_undeadband_players]";
	private static final String ROOM_B_UNDEADBAND_MANSTER = "[last_imperial_tomb_b_undeadband_masters]";
	private static final String ROOM_B_UNDEADBAND = "[last_imperial_tomb_b_undeadband_member]";
	private static final int _intervalOfFrintezzaSongs = 30000;
	private static final NpcLocation scarletSpawnWeak = new NpcLocation(174234, -88015, -5116, 48028, 29046);
	private static final NpcLocation[] portraitSpawns = {new NpcLocation(175880, -88696, -5104, 35048, 29048), new NpcLocation(175816, -87160, -5104, 28205, 29049), new NpcLocation(172648, -87176, -5104, 64817, 29048), new NpcLocation(172600, -88664, -5104, 57730, 29049)};
	private static final NpcLocation[] demonSpawns = {new NpcLocation(175880, -88696, -5104, 35048, 29050), new NpcLocation(175816, -87160, -5104, 28205, 29051), new NpcLocation(172648, -87176, -5104, 64817, 29051), new NpcLocation(172600, -88664, -5104, 57730, 29050)};
	private static final long battleStartDelay = 300000;
	public static int FRINTEZZA_NPC_ID = 29045;
	private static final NpcLocation frintezzaSpawn = new NpcLocation(174240, -89805, -5022, 16048, FRINTEZZA_NPC_ID);
	private final List<Spawner> _roomAAlarmDevices = new ArrayList<>();
	private final List<Spawner> _roomAHallKeepers = new ArrayList<>();
	private final List<Spawner> _roomBUndeadBandPlayers = new ArrayList<>();
	private final List<Spawner> _roomBUndeadBandMasters = new ArrayList<>();
	private final List<Spawner> _roomBUndeadBandMembers = new ArrayList<>();
	private final List<DoorInstance> _gravePathwayA = new ArrayList<>();
	private final List<DoorInstance> _gravePathwayB = new ArrayList<>();
	private final List<DoorInstance> _wallDoorA = new ArrayList<>();
	private final List<DoorInstance> _wallDoorB = new ArrayList<>();
	private final NpcInstance[] portraits;
	private final NpcInstance[] demons;
	EpicBossState _state;
	private Skill _arrowSkill;
	private Zone _lastImperialTombZone;
	private Zone _frintessaZone;
	private Zone _areadataHeal1;
	private Zone _areadataHeal2;
	private Zone _areadataPower1;
	private Zone _areadataPower2;
	private Zone _areadataPsycho1;
	private Zone _areadataPsycho2;
	private Zone _areadataRampage1;
	private Zone _areadataRampage2;
	private Zone _areadataPlague1;
	private Zone _areadataPlague2;
	private DeathListener _deathListener;
	private FrintessaEnterListener _frintessaEnterListener;
	private CurrentHpListener _currentHpListener;
	private AtomicInteger _progress;
	private Future<?> _timeoutTask;
	private NpcInstance _frintezzaDummy;
	private NpcInstance frintezza;
	private NpcInstance weakScarlet;
	private NpcInstance strongScarlet;
	private Future<?> _intervalEndTask;
	private Future<?> _frintezzaSpawnTask;
	private int _scarletMorph;
	private ScheduledFuture<?> musicTask;
	
	public FrintezzaManager()
	{
		_currentHpListener = new CurrentHpListener();
		portraits = new NpcInstance[4];
		demons = new NpcInstance[4];
		_scarletMorph = 0;
	}
	
	public static final FrintezzaManager getInstance()
	{
		return INSTANCE;
	}
	
	private static long getRespawnInterval()
	{
		if(!Config.FWA_FIXTIMEPATTERNOFFRINTEZZA.isEmpty())
		{
			long now = System.currentTimeMillis();
			try
			{
				SchedulingPattern timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFFRINTEZZA);
				long delay = timePattern.next(now) - now;
				return Math.max(60000, delay);
			}
			catch(SchedulingPattern.InvalidPatternException e)
			{
				throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFFRINTEZZA + "\" in " + FrintezzaManager.class.getSimpleName(), e);
			}
		}
		return (long) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (double) Config.FWV_FIXINTERVALOFFRINTEZZA);
	}
	
	public EpicBossState getEpicBossState()
	{
		return _state;
	}
	
	private void init()
	{
		_state = new EpicBossState(FRINTEZZA_NPC_ID);
		_progress = new AtomicInteger(0);
		_lastImperialTombZone = ReflectionUtils.getZone("[LastImperialTomb]");
		_frintessaZone = ReflectionUtils.getZone("[Frintezza]");
		LOG.info("FrintezzaManager: State of Frintezza is " + _state.getState() + ".");
		if(!EpicBossState.State.NOTSPAWN.equals(_state.getState()))
		{
			setIntervalEndTask();
		}
		Date dt = new Date(_state.getRespawnDate());
		LOG.info("FrintezzaManager: Next spawn date of Frintezza is " + dt + ".");
		_areadataHeal1 = ReflectionUtils.getZone("[25_15_frintessa_01_01]");
		_areadataHeal2 = ReflectionUtils.getZone("[25_15_frintessa_02_01]");
		_areadataPower1 = ReflectionUtils.getZone("[25_15_frintessa_01_02]");
		_areadataPower2 = ReflectionUtils.getZone("[25_15_frintessa_02_02]");
		_areadataPsycho1 = ReflectionUtils.getZone("[25_15_frintessa_01_03]");
		_areadataPsycho2 = ReflectionUtils.getZone("[25_15_frintessa_02_03]");
		_areadataRampage1 = ReflectionUtils.getZone("[25_15_frintessa_01_04]");
		_areadataRampage2 = ReflectionUtils.getZone("[25_15_frintessa_02_04]");
		_areadataPlague1 = ReflectionUtils.getZone("[25_15_frintessa_01_05]");
		_areadataPlague2 = ReflectionUtils.getZone("[25_15_frintessa_02_05]");
		int doorId;
		for(doorId = 25150042;doorId <= 25150043;++doorId)
		{
			_gravePathwayA.add(ReflectionUtils.getDoor(doorId));
		}
		for(doorId = 25150045;doorId <= 25150046;++doorId)
		{
			_gravePathwayB.add(ReflectionUtils.getDoor(doorId));
		}
		for(doorId = 25150051;doorId <= 25150058;++doorId)
		{
			_wallDoorA.add(ReflectionUtils.getDoor(doorId));
		}
		for(doorId = 25150061;doorId <= 25150070;++doorId)
		{
			_wallDoorB.add(ReflectionUtils.getDoor(doorId));
		}
		_roomAAlarmDevices.addAll(SpawnManager.getInstance().getSpawners("[last_imperial_tomb_a_alarm_devices]"));
		_roomAHallKeepers.addAll(SpawnManager.getInstance().getSpawners("[last_imperial_tomb_a_hall_keeper]"));
		_roomBUndeadBandPlayers.addAll(SpawnManager.getInstance().getSpawners("[last_imperial_tomb_b_undeadband_players]"));
		_roomBUndeadBandMasters.addAll(SpawnManager.getInstance().getSpawners("[last_imperial_tomb_b_undeadband_masters]"));
		_roomBUndeadBandMembers.addAll(SpawnManager.getInstance().getSpawners("[last_imperial_tomb_b_undeadband_member]"));
		_deathListener = new DeathListener();
		_currentHpListener = new CurrentHpListener();
		_frintessaEnterListener = new FrintessaEnterListener();
		_arrowSkill = SkillTable.getInstance().getInfo(2234, 1);
		_frintessaZone.addListener(_frintessaEnterListener);
		ItemHandler.getInstance().registerItemHandler(new FrintezzaItemHandler());
	}
	
	public void setIntervalEndTask()
	{
		if(!EpicBossState.State.INTERVAL.equals(_state.getState()))
		{
			_state.setRespawnDate(getRespawnInterval());
			_state.setState(EpicBossState.State.INTERVAL);
			_state.update();
		}
		_intervalEndTask = ThreadPoolManager.getInstance().schedule(new IntervalEnd(), _state.getInterval());
	}
	
	private void done()
	{
	}
	
	public boolean isInUse()
	{
		return _progress.get() != 0;
	}
	
	public boolean canEnter()
	{
		if(isInUse())
		{
			return false;
		}
		return _state.getState() == EpicBossState.State.NOTSPAWN;
	}
	
	public boolean tryEnter(List<Party> parties)
	{
		try
		{
			if(!startUp())
			{
				return false;
			}
			for(int i = 0;i < parties.size();++i)
			{
				Party party = parties.get(i);
				Location teleLoc = TELEPORT_PARTY_LOCATIONS[i % TELEPORT_PARTY_LOCATIONS.length];
				for(Player member : party.getPartyMembers())
				{
					preparePlayableToEnter(member);
					Location loc = Location.findPointToStay(teleLoc, 256, member.getGeoIndex());
					member.teleToLocation(loc);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean startUp()
	{
		try
		{
			if(_progress.compareAndSet(0, 1))
			{
				banishForeigners();
				SpawnManager.getInstance().despawn("[frintezza_teleporter]");
				cleanupPrev();
				closeDoors(_wallDoorA);
				closeDoors(_gravePathwayA);
				removeListenerFromAllSpawned(_roomAAlarmDevices, _deathListener);
				despawn(_roomAAlarmDevices);
				despawn(_roomAHallKeepers);
				removeListenerFromAllSpawned(_roomBUndeadBandPlayers, _deathListener);
				despawn(_roomBUndeadBandPlayers);
				removeListenerFromAllSpawned(_roomBUndeadBandMasters, _deathListener);
				despawn(_roomBUndeadBandMasters);
				despawn(_roomBUndeadBandMembers);
				spawn(_roomAAlarmDevices);
				stopRespawn(_roomAAlarmDevices);
				addListenerToAllSpawned(_roomAAlarmDevices, _deathListener);
				spawn(_roomAHallKeepers);
				_timeoutTask = ThreadPoolManager.getInstance().schedule(new TombTimeoutTask(), 5000);
				return true;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	private void cleanupPrev()
	{
		if(_frintezzaDummy != null)
		{
			_frintezzaDummy.deleteMe();
			_frintezzaDummy = null;
		}
		if(frintezza != null)
		{
			frintezza.deleteMe();
			frintezza = null;
		}
		if(weakScarlet != null)
		{
			weakScarlet.deleteMe();
			weakScarlet = null;
		}
		if(strongScarlet != null)
		{
			strongScarlet.deleteMe();
			strongScarlet = null;
		}
		for(int pidx = 0;pidx < portraits.length;++pidx)
		{
			if(portraits[pidx] == null)
				continue;
			portraits[pidx].deleteMe();
			portraits[pidx] = null;
		}
		for(int didx = 0;didx < demons.length;++didx)
		{
			if(demons[didx] == null)
				continue;
			demons[didx].deleteMe();
			demons[didx] = null;
		}
		for(Creature c : _lastImperialTombZone.getObjects())
		{
			if(c == null || !(c instanceof NpcInstance))
				continue;
			c.deleteMe();
		}
		if(musicTask != null)
		{
			musicTask.cancel(true);
			musicTask = null;
		}
		if(_frintezzaSpawnTask != null)
		{
			_frintezzaSpawnTask.cancel(true);
			_frintezzaSpawnTask = null;
		}
		disableMusicZones();
	}
	
	public boolean cleanUp()
	{
		if(_progress.get() != 0)
		{
			banishForeigners();
			removeListenerFromAllSpawned(_roomAAlarmDevices, _deathListener);
			despawn(_roomAAlarmDevices);
			despawn(_roomAHallKeepers);
			removeListenerFromAllSpawned(_roomBUndeadBandPlayers, _deathListener);
			despawn(_roomBUndeadBandPlayers);
			removeListenerFromAllSpawned(_roomBUndeadBandMasters, _deathListener);
			despawn(_roomBUndeadBandMasters);
			despawn(_roomBUndeadBandMembers);
			closeDoors(_wallDoorA);
			closeDoors(_gravePathwayA);
			closeDoors(_wallDoorB);
			closeDoors(_gravePathwayB);
			cleanupPrev();
			if(_intervalEndTask != null)
			{
				_intervalEndTask.cancel(true);
				_intervalEndTask = null;
			}
			if(_state.getState() == EpicBossState.State.ALIVE || _progress.get() > 0 && _progress.get() < 5)
			{
				_state.setState(EpicBossState.State.NOTSPAWN);
				_state.update();
			}
			else
			{
				setIntervalEndTask();
			}
			_progress.set(0);
			return true;
		}
		return false;
	}
	
	private void openDoors(List<DoorInstance> doors)
	{
		for(DoorInstance door : doors)
		{
			if(door == null)
				continue;
			door.openMe();
		}
	}
	
	private void closeDoors(List<DoorInstance> doors)
	{
		for(DoorInstance door : doors)
		{
			if(door == null)
				continue;
			door.closeMe();
		}
	}
	
	private void spawn(List<Spawner> spawners)
	{
		for(Spawner spawner : spawners)
		{
			spawner.init();
		}
	}
	
	private void stopRespawn(List<Spawner> spawners)
	{
		for(Spawner spawner : spawners)
		{
			spawner.stopRespawn();
		}
	}
	
	private void despawn(List<Spawner> spawners)
	{
		for(Spawner spawner : spawners)
		{
			spawner.deleteAll();
		}
	}
	
	private List<NpcInstance> getAllSpawned(List<Spawner> spawners)
	{
		ArrayList<NpcInstance> result = new ArrayList<>();
		for(Spawner spawner : spawners)
		{
			result.addAll(spawner.getAllSpawned());
		}
		return result;
	}
	
	private boolean isNpcOfSpawn(NpcInstance npc, List<Spawner> spawners)
	{
		Spawner npcSpawner = npc.getSpawn();
		for(Spawner spawner : spawners)
		{
			if(npcSpawner != spawner)
				continue;
			return true;
		}
		return false;
	}
	
	private List<NpcInstance> getAliveSpawned(List<Spawner> spawners)
	{
		ArrayList<NpcInstance> result = new ArrayList<>();
		for(Spawner spawner : spawners)
		{
			List<NpcInstance> allSpawned = spawner.getAllSpawned();
			for(NpcInstance spanedNpc : allSpawned)
			{
				if(spanedNpc.isDead())
					continue;
				result.add(spanedNpc);
			}
		}
		return result;
	}
	
	private boolean isAllSpawnedDead(List<Spawner> spawners)
	{
		for(Spawner spawner : spawners)
		{
			List<NpcInstance> allSpawned = spawner.getAllSpawned();
			for(NpcInstance spanedNpc : allSpawned)
			{
				if(spanedNpc.isDead())
					continue;
				return false;
			}
		}
		return true;
	}
	
	private void addListenerToAllSpawned(List<Spawner> spawners, CharListener listener)
	{
		List<NpcInstance> allSpawned = getAllSpawned(spawners);
		for(NpcInstance spawnedNpc : allSpawned)
		{
			spawnedNpc.addListener((Listener) listener);
		}
	}
	
	private void removeListenerFromAllSpawned(List<Spawner> spawners, CharListener listener)
	{
		List<NpcInstance> allSpawned = getAllSpawned(spawners);
		for(NpcInstance spawnedNpc : allSpawned)
		{
			spawnedNpc.removeListener((Listener) listener);
		}
	}
	
	private void onRoomAAlarmDeviceDied(NpcInstance device)
	{
		if(isAllSpawnedDead(_roomAAlarmDevices))
		{
			if(_progress.compareAndSet(1, 2))
			{
				removeListenerFromAllSpawned(_roomAAlarmDevices, _deathListener);
				despawn(_roomAHallKeepers);
				openDoors(_wallDoorA);
				openDoors(_gravePathwayA);
				Functions.npcShoutCustomMessage(device, "LastImperialTomb.DeactivateTheAlarm", (Object[]) new Object[0]);
				removeListenerFromAllSpawned(_roomAAlarmDevices, _deathListener);
				closeDoors(_wallDoorB);
				closeDoors(_gravePathwayB);
				spawn(_roomBUndeadBandPlayers);
				stopRespawn(_roomBUndeadBandPlayers);
				addListenerToAllSpawned(_roomBUndeadBandPlayers, _deathListener);
				spawn(_roomBUndeadBandMasters);
				stopRespawn(_roomBUndeadBandMasters);
				addListenerToAllSpawned(_roomBUndeadBandMasters, _deathListener);
				spawn(_roomBUndeadBandMembers);
			}
		}
		else
		{
			Functions.npcShoutCustomMessage(device, "LastImperialTomb.Intruders", (Object[]) new Object[0]);
		}
	}
	
	private void undeadBandMasterRandomShout()
	{
		NpcInstance rndUndeadBansMaster;
		List<NpcInstance> undeadBandAliveMasters = getAliveSpawned(_roomBUndeadBandMasters);
		if(!undeadBandAliveMasters.isEmpty() && (rndUndeadBansMaster = undeadBandAliveMasters.get(Rnd.get(undeadBandAliveMasters.size()))) != null)
		{
			Functions.npcShoutCustomMessage(rndUndeadBansMaster, String.format("LastImperialTomb.UndeadBandMasterShout%d", 1 + Rnd.get(3)), (Object[]) new Object[0]);
		}
	}
	
	private void checkUndeadBandAllDead()
	{
		if(isAllSpawnedDead(_roomBUndeadBandMasters) && _progress.compareAndSet(2, 3))
		{
			stopRespawn(_roomBUndeadBandMembers);
			despawn(_roomBUndeadBandMembers);
			removeListenerFromAllSpawned(_roomBUndeadBandMasters, _deathListener);
			despawn(_roomBUndeadBandMasters);
			openDoors(_gravePathwayB);
		}
	}
	
	private void onRoomBUndeadBandPlayerDied(NpcInstance undeadBandPlayer)
	{
		if(isAllSpawnedDead(_roomBUndeadBandPlayers))
		{
			closeDoors(_wallDoorA);
			closeDoors(_gravePathwayA);
			openDoors(_wallDoorB);
			undeadBandMasterRandomShout();
			removeListenerFromAllSpawned(_roomBUndeadBandPlayers, _deathListener);
			despawn(_roomBUndeadBandPlayers);
			undeadBandMasterRandomShout();
		}
		checkUndeadBandAllDead();
	}
	
	private void onRoomBUndeadBandMasterDied(NpcInstance undeadBandMaster, Creature lastAttaker)
	{
		if(lastAttaker != null && lastAttaker.getPlayer() != null && getAliveSpawned(_roomBUndeadBandMasters).size() % 2 == 0)
		{
			undeadBandMaster.dropItem(lastAttaker.getPlayer(), 8192, 1);
		}
		checkUndeadBandAllDead();
	}
	
	private void onEnterFrontessaZone()
	{
		if(_progress.compareAndSet(3, 4))
		{
			_frintezzaSpawnTask = ThreadPoolManager.getInstance().schedule(new Spawn(1), 300000);
		}
	}
	
	private void cancelMusic()
	{
		if(musicTask != null)
		{
			musicTask.cancel(false);
		}
		if(frintezza != null)
		{
			frintezza.broadcastPacket(new MagicSkillCanceled(frintezza));
			musicTask = ThreadPoolManager.getInstance().schedule(new Music(1), 60000 + (long) Rnd.get(10000));
		}
	}
	
	private void disableMusicZones()
	{
		_areadataHeal1.setActive(false);
		_areadataHeal2.setActive(false);
		_areadataPower1.setActive(false);
		_areadataPower2.setActive(false);
		_areadataPsycho1.setActive(false);
		_areadataPsycho2.setActive(false);
		_areadataRampage1.setActive(false);
		_areadataRampage2.setActive(false);
		_areadataPlague1.setActive(false);
		_areadataPlague2.setActive(false);
	}
	
	private NpcInstance spawn(NpcLocation loc)
	{
		return NpcUtils.spawnSingle(loc.npcId, loc);
	}
	
	private void showSocialActionMovie(NpcInstance target, int dist, int yaw, int pitch, int time, int duration, int socialAction)
	{
		if(target == null)
		{
			return;
		}
		for(Player pc : _frintessaZone.getInsidePlayers())
		{
			if(pc.getDistance(target) <= 2550.0)
			{
				pc.enterMovieMode();
				pc.specialCamera(target, dist, yaw, pitch, time, duration);
				continue;
			}
			pc.leaveMovieMode();
		}
		if(socialAction > 0 && socialAction < 5)
		{
			target.broadcastPacket(new SocialAction(target.getObjectId(), socialAction));
		}
	}
	
	private void blockAll(boolean flag)
	{
		block(frintezza, flag);
		block(weakScarlet, flag);
		block(strongScarlet, flag);
		for(int i = 0;i < 4;++i)
		{
			block(portraits[i], flag);
			block(demons[i], flag);
		}
	}
	
	private void block(NpcInstance npc, boolean flag)
	{
		if(npc == null || npc.isDead())
		{
			return;
		}
		if(flag)
		{
			npc.abortAttack(true, false);
			npc.abortCast(true, true);
			npc.setTarget(null);
			if(npc.isMoving())
			{
				npc.stopMove();
			}
			npc.block();
		}
		else
		{
			npc.unblock();
		}
		npc.setIsInvul(flag);
	}
	
	public void preparePlayableToEnter(Playable playable)
	{
		long breakingArrowAmount = ItemFunctions.getItemCount(playable, 8192);
		if(breakingArrowAmount > 0)
		{
			ItemFunctions.removeItem(playable, 8192, breakingArrowAmount, true);
		}
		long dewdropCount;
		if((dewdropCount = ItemFunctions.getItemCount(playable, 8556)) > 0)
		{
			ItemFunctions.removeItem(playable, 8556, dewdropCount, true);
		}
		if(playable.getPet() != null)
		{
			preparePlayableToEnter(playable.getPet());
		}
	}
	
	private void banishForeigners()
	{
		for(Player player : _lastImperialTombZone.getInsidePlayers())
		{
			player.teleToClosestTown();
		}
	}
	
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		switch(command)
		{
			case admin_fri_startup:
			{
				getInstance().startUp();
				break;
			}
			case admin_fri_cleanup:
			{
				getInstance().cleanUp();
				break;
			}
			case admin_fri_go:
			{
				activeChar.teleToLocation(TELEPORT_PARTY_LOCATIONS[Rnd.get(TELEPORT_PARTY_LOCATIONS.length)]);
				break;
			}
			case admin_fri_devdump:
			{
				System.out.println("progress: " + _progress.get());
				System.out.println("room A devices: " + Arrays.deepToString(_roomAAlarmDevices.toArray(new Spawner[_roomAAlarmDevices.size()])));
				System.out.println("room A keepers: " + Arrays.deepToString(_roomAHallKeepers.toArray(new Spawner[_roomAHallKeepers.size()])));
				System.out.println("room B Players: " + Arrays.deepToString(_roomBUndeadBandPlayers.toArray(new Spawner[_roomBUndeadBandPlayers.size()])));
				System.out.println("room B masters: " + Arrays.deepToString(_roomBUndeadBandMasters.toArray(new Spawner[_roomBUndeadBandMasters.size()])));
				System.out.println("state: " + _state.getState());
				break;
			}
			case admin_fri_musdump:
			{
				System.out.println("areadataHeal1: " + _areadataHeal1 + " | " + _areadataHeal1.isActive() + " " + _areadataHeal1.getInsidePlayers().size());
				System.out.println("areadataHeal2: " + _areadataHeal2 + " | " + _areadataHeal2.isActive() + " " + _areadataHeal2.getInsidePlayers().size());
				System.out.println("areadataPower1: " + _areadataPower1 + " | " + _areadataPower1.isActive() + " " + _areadataPower1.getInsidePlayers().size());
				System.out.println("areadataPower2: " + _areadataPower2 + " | " + _areadataPower2.isActive() + " " + _areadataPower2.getInsidePlayers().size());
				System.out.println("areadataPsycho1: " + _areadataPsycho1 + " | " + _areadataPsycho1.isActive() + " " + _areadataPsycho1.getInsidePlayers().size());
				System.out.println("areadataPsycho2: " + _areadataPsycho2 + " | " + _areadataPsycho2.isActive() + " " + _areadataPsycho2.getInsidePlayers().size());
				System.out.println("areadataRampage1: " + _areadataRampage1 + " | " + _areadataRampage1.isActive() + " " + _areadataRampage1.getInsidePlayers().size());
				System.out.println("areadataRampage2: " + _areadataRampage2 + " | " + _areadataRampage2.isActive() + " " + _areadataRampage2.getInsidePlayers().size());
				System.out.println("areadataPlague1: " + _areadataPlague1 + " | " + _areadataPlague1.isActive() + " " + _areadataPlague1.getInsidePlayers().size());
				System.out.println("areadataPlague2: " + _areadataPlague2 + " | " + _areadataPlague2.isActive() + " " + _areadataPlague2.getInsidePlayers().size());
			}
		}
		return false;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	@Override
	public void onLoad()
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(getInstance());
		getInstance().init();
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
		getInstance().done();
	}
	
	private enum Commands
	{
		admin_fri_startup,
		admin_fri_cleanup,
		admin_fri_go,
		admin_fri_devdump,
		admin_fri_musdump;
	}
	
	public static class NpcLocation extends Location
	{
		public int npcId;
		
		public NpcLocation()
		{
		}
		
		public NpcLocation(int x, int y, int z, int heading, int npcId)
		{
			super(x, y, z, heading);
			this.npcId = npcId;
		}
	}
	
	private class FrintezzaItemHandler extends SimpleItemHandler
	{
		@Override
		protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
		{
			int itemId = item.getItemId();
			if(!SimpleItemHandler.useItem(player, item, 1))
			{
				return false;
			}
			switch(itemId)
			{
				case 8192:
				{
					if(player.getTarget() == null || !player.getTarget().isNpc() || player.getTarget() != getInstance().frintezza)
						break;
					NpcInstance npc = (NpcInstance) player.getTarget();
					if(npc == null || npc.getNpcId() != FRINTEZZA_NPC_ID)
						break;
					player.callSkill(_arrowSkill, Collections.singletonList(getInstance().frintezza), false);
					player.broadcastPacket(new MagicSkillUse(player, getInstance().frintezza, 2234, 1, 1000, 0));
					getInstance().cancelMusic();
					break;
				}
				case 8556:
				{
					NpcInstance portrait;
					if(player.getTarget() == null || !player.getTarget().isNpc() || (portrait = (NpcInstance) player.getTarget()).getNpcId() != 29048 && portrait.getNpcId() != 29049)
						break;
					portrait.doDie(player);
				}
			}
			return false;
		}
		
		@Override
		public int[] getItemIds()
		{
			return new int[] {8556, 8192};
		}
	}
	
	private class FrintessaEnterListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature actor)
		{
			onEnterFrontessaZone();
		}
		
		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			if(cha.isNpc() && (cha == weakScarlet || cha == strongScarlet))
			{
				cha.teleToLocation(new Location(174240, -88020, -5112));
				((NpcInstance) cha).getAggroList().clear(true);
				cha.setCurrentHpMp((double) cha.getMaxHp(), (double) cha.getMaxMp());
				cha.broadcastCharInfo();
			}
		}
	}
	
	public class CurrentHpListener implements OnCurrentHpDamageListener
	{
		@Override
		public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill)
		{
			if(actor.isDead() || actor != weakScarlet)
			{
				return;
			}
			double newHp = actor.getCurrentHp() - damage;
			double maxHp = actor.getMaxHp();
			switch(_scarletMorph)
			{
				case 1:
				{
					if(newHp >= 0.75 * maxHp)
						break;
					_scarletMorph = 2;
					ThreadPoolManager.getInstance().schedule(new SecondMorph(1), 1100);
					break;
				}
				case 2:
				{
					if(newHp >= 0.1 * maxHp)
						break;
					_scarletMorph = 3;
					ThreadPoolManager.getInstance().schedule(new ThirdMorph(1), 2000);
				}
			}
		}
	}
	
	private class TombTimeoutTask extends RunnableImpl
	{
		private int _timeLeft;
		
		public TombTimeoutTask()
		{
			_timeLeft = Config.FRINTEZZA_TOMB_TIMEOUT;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(_timeLeft > 0)
			{
				List<Player> players = _lastImperialTombZone.getInsidePlayers();
				for(Player player : players)
				{
					player.sendPacket(new ExShowScreenMessage(new CustomMessage("LastImperialTomb.Remaining", player, new Object[] {_timeLeft}).toString(), 10000, ExShowScreenMessage.ScreenMessageAlign.BOTTOM_RIGHT, false));
				}
				if(_timeLeft > 5)
				{
					_timeLeft -= 5;
					ThreadPoolManager.getInstance().schedule(this, 300000);
				}
				else
				{
					--_timeLeft;
					ThreadPoolManager.getInstance().schedule(this, 60000);
				}
			}
			else
			{
				cleanUp();
			}
		}
	}
	
	private class DeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature actor, Creature killer)
		{
			if(actor.isNpc())
			{
				NpcInstance npc = (NpcInstance) actor;
				if(npc == weakScarlet)
				{
					npc.decayMe();
				}
				else if(npc == strongScarlet)
				{
					ThreadPoolManager.getInstance().schedule(new Die(1), 10);
				}
				else if(isNpcOfSpawn(npc, _roomAAlarmDevices))
				{
					onRoomAAlarmDeviceDied(npc);
				}
				else if(isNpcOfSpawn(npc, _roomBUndeadBandPlayers))
				{
					onRoomBUndeadBandPlayerDied((NpcInstance) actor);
				}
				else if(isNpcOfSpawn(npc, _roomBUndeadBandMasters))
				{
					onRoomBUndeadBandMasterDied(npc, killer);
				}
			}
		}
	}
	
	private class Die extends RunnableImpl
	{
		private int _taskId;
		
		public Die(int taskId)
		{
			_taskId = 0;
			_taskId = taskId;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			try
			{
				switch(_taskId)
				{
					case 1:
					{
						blockAll(true);
						int _angle = Math.abs((strongScarlet.getHeading() < 32768 ? 180 : 540) - (int) ((double) strongScarlet.getHeading() / 182.044444444));
						showSocialActionMovie(strongScarlet, 300, _angle - 180, 5, 0, 7000, 0);
						showSocialActionMovie(strongScarlet, 200, _angle, 85, 4000, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new Die(2), 7500);
						break;
					}
					case 2:
					{
						showSocialActionMovie(frintezza, 100, 120, 5, 0, 7000, 0);
						showSocialActionMovie(frintezza, 100, 90, 5, 5000, 15000, 0);
						ThreadPoolManager.getInstance().schedule(new Die(3), 6000);
						break;
					}
					case 3:
					{
						showSocialActionMovie(frintezza, 900, 90, 25, 7000, 10000, 0);
						frintezza.doDie(frintezza);
						frintezza = null;
						ThreadPoolManager.getInstance().schedule(new Die(4), 7000);
						break;
					}
					case 4:
					{
						disableMusicZones();
						for(Player pc : _frintessaZone.getInsidePlayers())
						{
							pc.leaveMovieMode();
						}
						SpawnManager.getInstance().spawn("[frintezza_teleporter]");
						_state.setState(EpicBossState.State.DEAD);
						_state.update();
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private class ThirdMorph extends RunnableImpl
	{
		private int _taskId;
		private int _angle;
		
		public ThirdMorph(int taskId)
		{
			_taskId = 0;
			_angle = 0;
			_taskId = taskId;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			try
			{
				switch(_taskId)
				{
					case 1:
					{
						_angle = Math.abs((weakScarlet.getHeading() < 32768 ? 180 : 540) - (int) ((double) weakScarlet.getHeading() / 182.044444444));
						for(Player pc : _frintessaZone.getInsidePlayers())
						{
							pc.enterMovieMode();
						}
						blockAll(true);
						frintezza.broadcastPacket(new MagicSkillCanceled(frintezza));
						frintezza.broadcastPacket(new SocialAction(frintezza.getObjectId(), 4));
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(2), 100);
						break;
					}
					case 2:
					{
						showSocialActionMovie(frintezza, 250, 120, 15, 0, 1000, 0);
						showSocialActionMovie(frintezza, 250, 120, 15, 0, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(3), 6500);
						break;
					}
					case 3:
					{
						frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5006, 1, 34000, 0));
						showSocialActionMovie(frintezza, 500, 70, 15, 3000, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(4), 3000);
						break;
					}
					case 4:
					{
						showSocialActionMovie(frintezza, 2500, 90, 12, 6000, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(5), 3000);
						break;
					}
					case 5:
					{
						showSocialActionMovie(weakScarlet, 250, _angle, 12, 0, 1000, 0);
						showSocialActionMovie(weakScarlet, 250, _angle, 12, 0, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(6), 500);
						break;
					}
					case 6:
					{
						weakScarlet.doDie(weakScarlet);
						showSocialActionMovie(weakScarlet, 450, _angle, 14, 8000, 8000, 0);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(7), 6250);
						break;
					}
					case 7:
					{
						NpcLocation loc = new NpcLocation();
						loc.set(weakScarlet.getLoc());
						loc.npcId = 29047;
						weakScarlet.deleteMe();
						weakScarlet = null;
						strongScarlet = spawn(loc);
						strongScarlet.addListener((Listener) _deathListener);
						block(strongScarlet, true);
						showSocialActionMovie(strongScarlet, 450, _angle, 12, 500, 14000, 2);
						ThreadPoolManager.getInstance().schedule(new ThirdMorph(9), 5000);
						break;
					}
					case 9:
					{
						blockAll(false);
						for(Player pc : _frintessaZone.getInsidePlayers())
						{
							pc.leaveMovieMode();
						}
						Skill skill = SkillTable.getInstance().getInfo(5017, 1);
						skill.getEffects(strongScarlet, strongScarlet, false, false);
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private class SecondMorph extends RunnableImpl
	{
		private int _taskId;
		
		public SecondMorph(int taskId)
		{
			_taskId = 0;
			_taskId = taskId;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			try
			{
				switch(_taskId)
				{
					case 1:
					{
						int angle = Math.abs((weakScarlet.getHeading() < 32768 ? 180 : 540) - (int) ((double) weakScarlet.getHeading() / 182.044444444));
						for(Player pc : _frintessaZone.getInsidePlayers())
						{
							pc.enterMovieMode();
						}
						blockAll(true);
						showSocialActionMovie(weakScarlet, 500, angle, 5, 500, 15000, 0);
						ThreadPoolManager.getInstance().schedule(new SecondMorph(2), 2000);
						break;
					}
					case 2:
					{
						weakScarlet.broadcastPacket(new SocialAction(weakScarlet.getObjectId(), 1));
						weakScarlet.setCurrentHp((double) (weakScarlet.getMaxHp() * 3 / 4), false);
						weakScarlet.setRHandId(7903);
						weakScarlet.broadcastCharInfo();
						ThreadPoolManager.getInstance().schedule(new SecondMorph(3), 5500);
						break;
					}
					case 3:
					{
						weakScarlet.broadcastPacket(new SocialAction(weakScarlet.getObjectId(), 4));
						blockAll(false);
						Skill skill = SkillTable.getInstance().getInfo(5017, 1);
						skill.getEffects(weakScarlet, weakScarlet, false, false);
						for(Player pc : _frintessaZone.getInsidePlayers())
						{
							pc.leaveMovieMode();
						}
						break;
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private class Music extends RunnableImpl
	{
		private final int _sondId;
		
		private Music(int sondId)
		{
			_sondId = sondId;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(frintezza == null)
			{
				return;
			}
			disableMusicZones();
			if(!frintezza.isBlocked())
			{
				switch(_sondId)
				{
					case 1:
					{
						_areadataHeal1.setActive(true);
						_areadataHeal2.setActive(true);
						String songName = "Requiem of Hatred";
						frintezza.broadcastPacket(new ExShowScreenMessage(songName, 3000, 0, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
						frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5007, 1, _intervalOfFrintezzaSongs, 0));
						break;
					}
					case 2:
					{
						_areadataRampage1.setActive(true);
						_areadataRampage2.setActive(true);
						String songName = "Frenetic Toccata";
						frintezza.broadcastPacket(new ExShowScreenMessage(songName, 3000, 0, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
						frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5007, 2, _intervalOfFrintezzaSongs, 0));
						break;
					}
					case 3:
					{
						_areadataPower1.setActive(true);
						_areadataPower2.setActive(true);
						String songName = "Fugue of Jubilation";
						frintezza.broadcastPacket(new ExShowScreenMessage(songName, 3000, 0, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
						frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5007, 3, _intervalOfFrintezzaSongs, 0));
						break;
					}
					case 4:
					{
						_areadataPlague1.setActive(true);
						_areadataPlague2.setActive(true);
						String songName = "Mournful Chorale Prelude";
						frintezza.broadcastPacket(new ExShowScreenMessage(songName, 3000, 0, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
						frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5007, 4, _intervalOfFrintezzaSongs, 0));
						break;
					}
					case 5:
					{
						_areadataPsycho1.setActive(true);
						_areadataPsycho2.setActive(true);
						String songName = "Hypnotic Mazurka";
						frintezza.broadcastPacket(new ExShowScreenMessage(songName, 3000, 0, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
						frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5007, 5, _intervalOfFrintezzaSongs, 0));
						break;
					}
					default:
					{
						String songName = "Rondo of Solitude";
						if(frintezza != null)
						{
							frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5006, 1, _intervalOfFrintezzaSongs, 0));
						}
						disableMusicZones();
					}
				}
			}
			musicTask = ThreadPoolManager.getInstance().schedule(new Music(getSong()), (long) (_intervalOfFrintezzaSongs + Rnd.get(10000)));
		}
		
		private List<Creature> getSongTargets(int songId)
		{
			ArrayList<Creature> targets = new ArrayList<>();
			if(songId < 4)
			{
				if(weakScarlet != null && !weakScarlet.isDead())
				{
					targets.add(weakScarlet);
				}
				if(strongScarlet != null && !strongScarlet.isDead())
				{
					targets.add(strongScarlet);
				}
				for(int i = 0;i < 4;++i)
				{
					if(portraits[i] != null && !portraits[i].isDead() && portraits[i] != null)
					{
						targets.add(portraits[i]);
					}
					if(demons[i] == null || demons[i].isDead())
						continue;
					targets.add(demons[i]);
				}
			}
			else
			{
				for(Player pc : _frintessaZone.getInsidePlayers())
				{
					if(pc.isDead())
						continue;
					targets.add(pc);
				}
			}
			return targets;
		}
		
		private int getSong()
		{
			if(minionsNeedHeal())
			{
				return 1;
			}
			return 1 + Rnd.get(5);
		}
		
		private boolean minionsNeedHeal()
		{
			if(!Rnd.chance(40))
			{
				return false;
			}
			if(weakScarlet != null && !weakScarlet.isAlikeDead() && weakScarlet.getCurrentHp() < (double) (weakScarlet.getMaxHp() * 2 / 3))
			{
				return true;
			}
			if(strongScarlet != null && !strongScarlet.isAlikeDead() && strongScarlet.getCurrentHp() < (double) (strongScarlet.getMaxHp() * 2 / 3))
			{
				return true;
			}
			for(int i = 0;i < 4;++i)
			{
				if(portraits[i] != null && !portraits[i].isDead() && portraits[i].getCurrentHp() < (double) (portraits[i].getMaxHp() / 3))
				{
					return true;
				}
				if(demons[i] == null || demons[i].isDead() || demons[i].getCurrentHp() >= (double) (demons[i].getMaxHp() / 3))
					continue;
				return true;
			}
			return false;
		}
	}
	
	private class Spawn extends RunnableImpl
	{
		private int _taskId;
		
		public Spawn(int taskId)
		{
			_taskId = 0;
			_taskId = taskId;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			try
			{
				switch(_taskId)
				{
					case 1:
					{
						_frintezzaDummy = spawn(new NpcLocation(174232, -89816, -5016, 16048, 29059));
						ThreadPoolManager.getInstance().schedule(new Spawn(2), 1000);
						break;
					}
					case 2:
					{
						_state.setRespawnDate(getRespawnInterval());
						_state.setState(EpicBossState.State.ALIVE);
						_state.update();
						_progress.compareAndSet(4, 5);
						closeDoors(_gravePathwayB);
						frintezza = spawn(frintezzaSpawn);
						showSocialActionMovie(frintezza, 500, 90, 0, 6500, 8000, 0);
						for(int i = 0;i < 4;++i)
						{
							portraits[i] = spawn(portraitSpawns[i]);
							portraits[i].startImmobilized();
							demons[i] = spawn(demonSpawns[i]);
						}
						blockAll(true);
						ThreadPoolManager.getInstance().schedule(new Spawn(3), 6500);
						break;
					}
					case 3:
					{
						showSocialActionMovie(_frintezzaDummy, 1800, 90, 8, 6500, 7000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(4), 900);
						break;
					}
					case 4:
					{
						showSocialActionMovie(_frintezzaDummy, 140, 90, 10, 2500, 4500, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(5), 4000);
						break;
					}
					case 5:
					{
						showSocialActionMovie(frintezza, 40, 75, -10, 0, 1000, 0);
						showSocialActionMovie(frintezza, 40, 75, -10, 0, 12000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(6), 1350);
						break;
					}
					case 6:
					{
						frintezza.broadcastPacket(new SocialAction(frintezza.getObjectId(), 2));
						musicTask = ThreadPoolManager.getInstance().schedule(new Music(0), 5000);
						ThreadPoolManager.getInstance().schedule(new Spawn(7), 7000);
						break;
					}
					case 7:
					{
						_frintezzaDummy.deleteMe();
						_frintezzaDummy = null;
						ThreadPoolManager.getInstance().schedule(new Spawn(8), 1000);
						break;
					}
					case 8:
					{
						showSocialActionMovie(demons[0], 140, 0, 3, 22000, 3000, 1);
						ThreadPoolManager.getInstance().schedule(new Spawn(9), 2800);
						break;
					}
					case 9:
					{
						showSocialActionMovie(demons[1], 140, 0, 3, 22000, 3000, 1);
						ThreadPoolManager.getInstance().schedule(new Spawn(10), 2800);
						break;
					}
					case 10:
					{
						showSocialActionMovie(demons[2], 140, 180, 3, 22000, 3000, 1);
						ThreadPoolManager.getInstance().schedule(new Spawn(11), 2800);
						break;
					}
					case 11:
					{
						showSocialActionMovie(demons[3], 140, 180, 3, 22000, 3000, 1);
						ThreadPoolManager.getInstance().schedule(new Spawn(12), 3000);
						break;
					}
					case 12:
					{
						showSocialActionMovie(frintezza, 240, 90, 0, 0, 1000, 0);
						showSocialActionMovie(frintezza, 240, 90, 25, 5500, 10000, 3);
						ThreadPoolManager.getInstance().schedule(new Spawn(13), 3000);
						break;
					}
					case 13:
					{
						showSocialActionMovie(frintezza, 100, 195, 35, 0, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(14), 700);
						break;
					}
					case 14:
					{
						showSocialActionMovie(frintezza, 100, 195, 35, 0, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(15), 1300);
						break;
					}
					case 15:
					{
						showSocialActionMovie(frintezza, 120, 180, 45, 1500, 10000, 0);
						frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5006, 1, 34000, 0));
						ThreadPoolManager.getInstance().schedule(new Spawn(16), 1500);
						break;
					}
					case 16:
					{
						showSocialActionMovie(frintezza, 520, 135, 45, 8000, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(17), 7500);
						break;
					}
					case 17:
					{
						showSocialActionMovie(frintezza, 1500, 110, 25, 10000, 13000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(18), 9500);
						break;
					}
					case 18:
					{
						weakScarlet = spawn(scarletSpawnWeak);
						block(weakScarlet, true);
						weakScarlet.addListener((Listener) _currentHpListener);
						weakScarlet.broadcastPacket(new MagicSkillUse(weakScarlet, weakScarlet, 5016, 1, 3000, 0));
						_frintessaZone.broadcastPacket(new Earthquake(weakScarlet.getLoc(), 50, 6), false);
						showSocialActionMovie(weakScarlet, 1000, 160, 20, 6000, 6000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(19), 5500);
						break;
					}
					case 19:
					{
						showSocialActionMovie(weakScarlet, 800, 160, 5, 1000, 10000, 2);
						ThreadPoolManager.getInstance().schedule(new Spawn(20), 2100);
						break;
					}
					case 20:
					{
						showSocialActionMovie(weakScarlet, 300, 60, 8, 0, 10000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(21), 2000);
						break;
					}
					case 21:
					{
						showSocialActionMovie(weakScarlet, 1000, 90, 10, 3000, 5000, 0);
						ThreadPoolManager.getInstance().schedule(new Spawn(22), 3000);
						break;
					}
					case 22:
					{
						ThreadPoolManager.getInstance().schedule(new Spawn(23), 2000);
						for(Player pc : _frintessaZone.getInsidePlayers())
						{
							pc.leaveMovieMode();
						}
						break;
					}
					case 23:
					{
						blockAll(false);
						_scarletMorph = 1;
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private class IntervalEnd implements Runnable
	{
		@Override
		public void run()
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
		}
	}
}