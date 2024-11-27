package bosses;

import l2.commons.threading.RunnableImpl;
import l2.commons.time.cron.SchedulingPattern;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Zone;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.SocialAction;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.ReflectionUtils;
import l2.gameserver.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class SailrenManager extends Functions implements ScriptFile, OnDeathListener
{
	private static final Logger _log = LoggerFactory.getLogger(SailrenManager.class);
	private static final int Sailren = 29065;
	private static final int Velociraptor = 22198;
	private static final int Pterosaur = 22199;
	private static final int Tyrannosaurus = 22217;
	private static final int TeleportCubeId = 31759;
	private static final Location _enter;
	private static final boolean FWS_ENABLESINGLEPLAYER;
	private static final AtomicBoolean _isAlreadyEnteredOtherParty;
	private static final AtomicBoolean Dying;
	private static NpcInstance _velociraptor;
	private static NpcInstance _pterosaur;
	private static NpcInstance _tyranno;
	private static NpcInstance _sailren;
	private static NpcInstance _teleportCube;
	private static ScheduledFuture<?> _cubeSpawnTask;
	private static ScheduledFuture<?> _monsterSpawnTask;
	private static ScheduledFuture<?> _intervalEndTask;
	private static ScheduledFuture<?> _socialTask;
	private static ScheduledFuture<?> _activityTimeEndTask;
	private static ScheduledFuture<?> _onAnnihilatedTask;
	private static EpicBossState _state;
	private static Zone _zone;
	
	static
	{
		_cubeSpawnTask = null;
		_monsterSpawnTask = null;
		_intervalEndTask = null;
		_socialTask = null;
		_activityTimeEndTask = null;
		_onAnnihilatedTask = null;
		_enter = new Location(27734, -6938, -1982);
		FWS_ENABLESINGLEPLAYER = Boolean.TRUE;
		_isAlreadyEnteredOtherParty = new AtomicBoolean(false);
		Dying = new AtomicBoolean(false);
	}
	
	private static void banishForeigners()
	{
		for(Player player : getPlayersInside())
		{
			player.teleToClosestTown();
		}
	}
	
	private static synchronized void checkAnnihilated()
	{
		if(_onAnnihilatedTask == null && isPlayersAnnihilated())
		{
			_onAnnihilatedTask = ThreadPoolManager.getInstance().schedule(new onAnnihilated(), Config.SAILREN_CLEAR_ZONE_IF_ALL_DIE);
		}
	}
	
	private static List<Player> getPlayersInside()
	{
		return getZone().getInsidePlayers();
	}
	
	private static long getRespawnInterval()
	{
		if(!Config.FWA_FIXTIMEPATTERNOFSAILREN.isEmpty())
		{
			long now = System.currentTimeMillis();
			try
			{
				SchedulingPattern timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFSAILREN);
				long delay = timePattern.next(now) - now;
				return Math.max(60000, delay);
			}
			catch(SchedulingPattern.InvalidPatternException e)
			{
				throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFSAILREN + "\" in " + SailrenManager.class.getSimpleName(), e);
			}
		}
		return (long) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (double) (Config.FWS_FIXINTERVALOFSAILRENSPAWN + Rnd.get((long) 0, Config.FWS_RANDOMINTERVALOFSAILRENSPAWN)));
	}
	
	public static Zone getZone()
	{
		return _zone;
	}
	
	private static boolean isPlayersAnnihilated()
	{
		for(Player pc : getPlayersInside())
		{
			if(pc.isDead())
				continue;
			return false;
		}
		return true;
	}
	
	private static void onSailrenDie(Creature killer)
	{
		if(!Dying.compareAndSet(false, true))
		{
			return;
		}
		_state.setRespawnDate(getRespawnInterval());
		_state.setState(EpicBossState.State.INTERVAL);
		_state.update();
		scheduleIntervalEnd();
		Log.add("Sailren died", "bosses");
		_cubeSpawnTask = ThreadPoolManager.getInstance().schedule(new CubeSpawn(), 10000);
	}
	
	private static void setIntervalEndTask()
	{
		setUnspawn();
		if(_state.getState().equals(EpicBossState.State.ALIVE))
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
			return;
		}
		if(!_state.getState().equals(EpicBossState.State.INTERVAL))
		{
			_state.setRespawnDate(getRespawnInterval());
			_state.setState(EpicBossState.State.INTERVAL);
			_state.update();
		}
		scheduleIntervalEnd();
	}
	
	private static void scheduleIntervalEnd()
	{
		if(_intervalEndTask != null)
		{
			_intervalEndTask.cancel(false);
			_intervalEndTask = null;
		}
		_intervalEndTask = ThreadPoolManager.getInstance().schedule(new IntervalEnd(), _state.getInterval());
	}
	
	private static void setUnspawn()
	{
		banishForeigners();
		if(_velociraptor != null)
		{
			if(_velociraptor.getSpawn() != null)
			{
				_velociraptor.getSpawn().stopRespawn();
			}
			_velociraptor.deleteMe();
			_velociraptor = null;
		}
		if(_pterosaur != null)
		{
			if(_pterosaur.getSpawn() != null)
			{
				_pterosaur.getSpawn().stopRespawn();
			}
			_pterosaur.deleteMe();
			_pterosaur = null;
		}
		if(_tyranno != null)
		{
			if(_tyranno.getSpawn() != null)
			{
				_tyranno.getSpawn().stopRespawn();
			}
			_tyranno.deleteMe();
			_tyranno = null;
		}
		if(_sailren != null)
		{
			if(_sailren.getSpawn() != null)
			{
				_sailren.getSpawn().stopRespawn();
			}
			_sailren.deleteMe();
			_sailren = null;
		}
		if(_teleportCube != null)
		{
			if(_teleportCube.getSpawn() != null)
			{
				_teleportCube.getSpawn().stopRespawn();
			}
			_teleportCube.deleteMe();
			_teleportCube = null;
		}
		if(_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(false);
			_cubeSpawnTask = null;
		}
		if(_monsterSpawnTask != null)
		{
			_monsterSpawnTask.cancel(false);
			_monsterSpawnTask = null;
		}
		if(_intervalEndTask != null)
		{
			_intervalEndTask.cancel(false);
			_intervalEndTask = null;
		}
		if(_socialTask != null)
		{
			_socialTask.cancel(false);
			_socialTask = null;
		}
		if(_activityTimeEndTask != null)
		{
			_activityTimeEndTask.cancel(false);
			_activityTimeEndTask = null;
		}
		if(_onAnnihilatedTask != null)
		{
			_onAnnihilatedTask.cancel(false);
			_onAnnihilatedTask = null;
		}
	}
	
	private static void sleep()
	{
		setUnspawn();
		if(_state.getState().equals(EpicBossState.State.ALIVE))
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
		}
	}
	
	public static synchronized void setSailrenSpawnTask()
	{
		if(_monsterSpawnTask == null)
		{
			_monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new SailrenSpawn(22198), Config.FWS_INTERVALOFNEXTMONSTER);
		}
	}
	
	public static boolean isEnableEnterToLair()
	{
		return _state.getState() == EpicBossState.State.NOTSPAWN;
	}
	
	public static int canIntoSailrenLair(Player pc)
	{
		if(!FWS_ENABLESINGLEPLAYER && pc.getParty() == null)
		{
			return 4;
		}
		if(_isAlreadyEnteredOtherParty.get())
		{
			return 2;
		}
		if(_state.getState().equals(EpicBossState.State.NOTSPAWN))
		{
			return 0;
		}
		if(_state.getState().equals(EpicBossState.State.ALIVE) || _state.getState().equals(EpicBossState.State.DEAD))
		{
			return 1;
		}
		if(_state.getState().equals(EpicBossState.State.INTERVAL))
		{
			return 3;
		}
		return 0;
	}
	
	public static void entryToSailrenLair(Player pc)
	{
		if(_isAlreadyEnteredOtherParty.compareAndSet(false, true))
		{
			if(pc.getParty() == null)
			{
				pc.teleToLocation(Location.findPointToStay(_enter, 80, pc.getGeoIndex()));
			}
			else
			{
				ArrayList<Player> members = new ArrayList<>();
				for(Player mem : pc.getParty().getPartyMembers())
				{
					if(mem == null || mem.isDead() || !mem.isInRange(pc, 1000))
						continue;
					members.add(mem);
				}
				for(Player mem : members)
				{
					mem.teleToLocation(Location.findPointToStay(_enter, 80, mem.getGeoIndex()));
				}
			}
		}
	}
	
	private void init()
	{
		CharListenerList.addGlobal(this);
		_state = new EpicBossState(29065);
		_zone = ReflectionUtils.getZone("[sailren_epic]");
		_log.info("SailrenManager: State of Sailren is " + _state.getState() + ".");
		if(!_state.getState().equals(EpicBossState.State.NOTSPAWN))
		{
			setIntervalEndTask();
		}
		_log.info("SailrenManager: Next spawn date of Sailren is " + TimeUtils.toSimpleFormat(_state.getRespawnDate()) + ".");
	}
	
	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if(self.isPlayer() && _state != null && _state.getState() == EpicBossState.State.ALIVE && _zone != null && _zone.checkIfInZone(self.getX(), self.getY()))
		{
			checkAnnihilated();
		}
		else if(self == _velociraptor)
		{
			if(_monsterSpawnTask != null)
			{
				_monsterSpawnTask.cancel(false);
			}
			_monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new SailrenSpawn(22199), Config.FWS_INTERVALOFNEXTMONSTER);
		}
		else if(self == _pterosaur)
		{
			if(_monsterSpawnTask != null)
			{
				_monsterSpawnTask.cancel(false);
			}
			_monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new SailrenSpawn(22217), Config.FWS_INTERVALOFNEXTMONSTER);
		}
		else if(self == _tyranno)
		{
			if(_monsterSpawnTask != null)
			{
				_monsterSpawnTask.cancel(false);
			}
			_monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new SailrenSpawn(29065), Config.FWS_INTERVALOFNEXTMONSTER);
		}
		else if(self == _sailren)
		{
			onSailrenDie(killer);
		}
	}
	
	@Override
	public void onLoad()
	{
		init();
	}
	
	@Override
	public void onReload()
	{
		sleep();
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	private static class SailrenSpawn extends RunnableImpl
	{
		private final int _npcId;
		private final Location _pos = new Location(27628, -6109, -1982, 44732);
		
		SailrenSpawn(int npcId)
		{
			_npcId = npcId;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(_socialTask != null)
			{
				_socialTask.cancel(false);
				_socialTask = null;
			}
			switch(_npcId)
			{
				case 22198:
				{
					Dying.set(false);
					_velociraptor = Functions.spawn(new Location(27852, -5536, -1983, 44732), 22198);
					((DefaultAI) _velociraptor.getAI()).addTaskMove(_pos, false);
					if(_socialTask != null)
					{
						_socialTask.cancel(false);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new Social(_velociraptor, 2), 6000);
					if(_activityTimeEndTask != null)
					{
						_activityTimeEndTask.cancel(false);
						_activityTimeEndTask = null;
					}
					_activityTimeEndTask = ThreadPoolManager.getInstance().schedule(new ActivityTimeEnd(), Config.FWS_ACTIVITYTIMEOFMOBS);
					break;
				}
				case 22199:
				{
					_pterosaur = Functions.spawn(new Location(27852, -5536, -1983, 44732), 22199);
					((DefaultAI) _pterosaur.getAI()).addTaskMove(_pos, false);
					if(_socialTask != null)
					{
						_socialTask.cancel(false);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new Social(_pterosaur, 2), 6000);
					if(_activityTimeEndTask != null)
					{
						_activityTimeEndTask.cancel(false);
						_activityTimeEndTask = null;
					}
					_activityTimeEndTask = ThreadPoolManager.getInstance().schedule(new ActivityTimeEnd(), Config.FWS_ACTIVITYTIMEOFMOBS);
					break;
				}
				case 22217:
				{
					_tyranno = Functions.spawn(new Location(27852, -5536, -1983, 44732), 22217);
					((DefaultAI) _tyranno.getAI()).addTaskMove(_pos, false);
					if(_socialTask != null)
					{
						_socialTask.cancel(false);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new Social(_tyranno, 2), 6000);
					if(_activityTimeEndTask != null)
					{
						_activityTimeEndTask.cancel(false);
						_activityTimeEndTask = null;
					}
					_activityTimeEndTask = ThreadPoolManager.getInstance().schedule(new ActivityTimeEnd(), Config.FWS_ACTIVITYTIMEOFMOBS);
					break;
				}
				case 29065:
				{
					_sailren = Functions.spawn(new Location(27810, -5655, -1983, 44732), 29065);
					_state.setRespawnDate(getRespawnInterval() + Config.FWS_ACTIVITYTIMEOFMOBS);
					_state.setState(EpicBossState.State.ALIVE);
					_state.update();
					_sailren.setRunning();
					((DefaultAI) _sailren.getAI()).addTaskMove(_pos, false);
					if(_socialTask != null)
					{
						_socialTask.cancel(false);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new Social(_sailren, 2), 6000);
					if(_activityTimeEndTask != null)
					{
						_activityTimeEndTask.cancel(false);
						_activityTimeEndTask = null;
					}
					_activityTimeEndTask = ThreadPoolManager.getInstance().schedule(new ActivityTimeEnd(), Config.FWS_ACTIVITYTIMEOFMOBS);
				}
			}
		}
	}
	
	private static class onAnnihilated extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			Class<SailrenManager> class_ = SailrenManager.class;
			synchronized(SailrenManager.class)
			{
				if(!isPlayersAnnihilated() || _state.getState() == EpicBossState.State.INTERVAL)
				{
					_onAnnihilatedTask = null;
					return;
				}
				sleep();
				return;
			}
		}
	}
	
	private static class Social extends RunnableImpl
	{
		private final int _action;
		private final NpcInstance _npc;
		
		public Social(NpcInstance npc, int actionId)
		{
			_npc = npc;
			_action = actionId;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			_npc.broadcastPacket(new SocialAction(_npc.getObjectId(), _action));
		}
	}
	
	private static class IntervalEnd extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			setUnspawn();
			_isAlreadyEnteredOtherParty.set(false);
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
		}
	}
	
	private static class CubeSpawn extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			_teleportCube = Functions.spawn(new Location(27734, -6838, -1982, 0), 31759);
		}
	}
	
	private static class ActivityTimeEnd extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			sleep();
		}
	}
}