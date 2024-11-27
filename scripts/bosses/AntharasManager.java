package bosses;

import l2.commons.threading.RunnableImpl;
import l2.commons.time.cron.SchedulingPattern;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.data.StringHolder;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Zone;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.instances.BossInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.network.l2.s2c.PlaySound;
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

public class AntharasManager extends Functions implements ScriptFile, OnDeathListener
{
	public static final int ANTHARAS_STRONG = 29068;
	private static final Logger _log = LoggerFactory.getLogger(AntharasManager.class);
	private static final int _teleportCubeId = 31859;
	private static final int PORTAL_STONE = 3865;
	private static final Location TELEPORT_POSITION = new Location(179892, 114915, -7704);
	private static final Location _teleportCubeLocation = new Location(177615, 114941, -7709, 0);
	private static final Location _antharasLocation = new Location(181911, 114835, -7678, 32542);
	private static final List<NpcInstance> _spawnedMinions;
	private static final AtomicBoolean Dying;
	private static BossInstance _antharas;
	private static NpcInstance _teleCube;
	private static ScheduledFuture<?> _monsterSpawnTask;
	private static ScheduledFuture<?> _intervalEndTask;
	private static ScheduledFuture<?> _socialTask;
	private static ScheduledFuture<?> _moveAtRandomTask;
	private static ScheduledFuture<?> _sleepCheckTask;
	private static ScheduledFuture<?> _onAnnihilatedTask;
	private static EpicBossState _state;
	private static Zone _zone;
	private static long _lastAttackTime;
	private static volatile boolean _entryLocked;
	
	static
	{
		_spawnedMinions = new ArrayList<>();
		_lastAttackTime = 0;
		Dying = new AtomicBoolean(false);
		_entryLocked = false;
	}
	
	public static EpicBossState getEpicBossState()
	{
		return _state;
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
			_onAnnihilatedTask = ThreadPoolManager.getInstance().schedule(new onAnnihilated(), Config.ANTHARAS_CLEAR_ZONE_IF_ALL_DIE);
		}
	}
	
	private static List<Player> getPlayersInside()
	{
		return getZone().getInsidePlayers();
	}
	
	private static long getRespawnInterval()
	{
		if(!Config.FWA_FIXTIMEPATTERNOFANTHARAS.isEmpty())
		{
			long now = System.currentTimeMillis();
			try
			{
				SchedulingPattern timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFANTHARAS);
				long delay = timePattern.next(now) - now;
				return Math.max(60000, delay);
			}
			catch(SchedulingPattern.InvalidPatternException e)
			{
				throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFANTHARAS + "\" in " + AntharasManager.class.getSimpleName(), e);
			}
		}
		return (long) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (double) Config.FWA_FIXTIMEINTERVALOFANTHARAS);
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
	
	private static void onAntharasDie()
	{
		if(!Dying.compareAndSet(false, true))
		{
			return;
		}
		_state.setRespawnDate(getRespawnInterval());
		_state.setState(EpicBossState.State.INTERVAL);
		_state.update();
		scheduleIntervalEnd();
		_entryLocked = false;
		_teleCube = Functions.spawn(_teleportCubeLocation, 31859);
		Log.add("Antharas died", "bosses");
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
		if(_antharas != null)
		{
			_antharas.deleteMe();
		}
		for(NpcInstance npc : _spawnedMinions)
		{
			npc.deleteMe();
		}
		if(_teleCube != null)
		{
			_teleCube.deleteMe();
		}
		_entryLocked = false;
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
		if(_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(false);
			_moveAtRandomTask = null;
		}
		if(_sleepCheckTask != null)
		{
			_sleepCheckTask.cancel(false);
			_sleepCheckTask = null;
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
	
	public static void setLastAttackTime()
	{
		_lastAttackTime = System.currentTimeMillis();
	}
	
	public static synchronized void setAntharasSpawnTask()
	{
		if(_monsterSpawnTask == null)
		{
			_monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(1), Config.FWA_APPTIMEOFANTHARAS);
		}
	}
	
	public static void broadcastCustomScreenMessage(String address)
	{
		for(Player p : getPlayersInside())
		{
			p.sendPacket(new ExShowScreenMessage(StringHolder.getInstance().getNotNull(p, address), 8000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false));
		}
	}
	
	public static void addSpawnedMinion(NpcInstance npc)
	{
		_spawnedMinions.add(npc);
	}
	
	public static void enterTheLair(Player player)
	{
		if(player == null)
		{
			return;
		}
		if(_entryLocked)
		{
			player.sendMessage(new CustomMessage("AntharasStillReborn", player));
			return;
		}
		switch(_state.getState())
		{
			case ALIVE:
			{
				player.sendMessage(new CustomMessage("AntharasAlreadyReborn", player));
				return;
			}
			case DEAD:
			case INTERVAL:
			{
				player.sendMessage(new CustomMessage("AntharasStillReborn", player));
				return;
			}
		}
		if(player.isDead() || player.isFlying() || player.isCursedWeaponEquipped() || player.getInventory().getCountOf(3865) < 1 || !player.isInRange(player, 500))
		{
			player.sendMessage(new CustomMessage("AntharasPlayerNotRequirements", player));
			return;
		}
		player.getInventory().destroyItemByItemId(3865, 1);
		player.teleToLocation(TELEPORT_POSITION);
		setAntharasSpawnTask();
	}
	
	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if(self.isPlayer() && _state != null && _state.getState() == EpicBossState.State.ALIVE && _zone != null && _zone.checkIfInZone(self.getX(), self.getY()))
		{
			checkAnnihilated();
		}
		else if(self.isNpc() && self.getNpcId() == 29068)
		{
			ThreadPoolManager.getInstance().schedule(new AntharasSpawn(8), 10);
		}
	}
	
	private void init()
	{
		_state = new EpicBossState(29068);
		_zone = ReflectionUtils.getZone("[antharas_epic]");
		CharListenerList.addGlobal(this);
		_log.info("AntharasManager: State of Antharas is " + _state.getState() + ".");
		if(!_state.getState().equals(EpicBossState.State.NOTSPAWN))
		{
			setIntervalEndTask();
		}
		_log.info("AntharasManager: Next spawn date of Antharas is " + TimeUtils.toSimpleFormat(_state.getRespawnDate()) + ".");
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
	
	private static class onAnnihilated extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			Class<AntharasManager> class_ = AntharasManager.class;
			synchronized(AntharasManager.class)
			{
				if(!isPlayersAnnihilated() || _state.getState() == EpicBossState.State.INTERVAL)
				{
					_onAnnihilatedTask = null;
					return;
				}
				sleep();
			}
		}
	}
	
	private static class IntervalEnd extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			setUnspawn();
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
		}
	}
	
	private static class CheckLastAttack extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if(_state.getState() == EpicBossState.State.ALIVE)
			{
				if(_lastAttackTime + Config.FWA_LIMITUNTILSLEEPANTHARAS < System.currentTimeMillis())
				{
					sleep();
				}
				else
				{
					_sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 60000);
				}
			}
		}
	}
	
	private static class AntharasSpawn extends RunnableImpl
	{
		private final int _distance = 2550;
		private final List<Player> _players = getPlayersInside();
		private final int _taskId;
		
		AntharasSpawn(int taskId)
		{
			_taskId = taskId;
		}
		
		@Override
		public void runImpl()
		{
			switch(_taskId)
			{
				case 1:
				{
					Dying.set(false);
					_antharas = (BossInstance) Functions.spawn(_antharasLocation, 29068);
					_antharas.setAggroRange(0);
					_state.setRespawnDate(getRespawnInterval());
					_state.setState(EpicBossState.State.ALIVE);
					_state.update();
					_socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(2), 2000);
					_entryLocked = true;
					break;
				}
				case 2:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_antharas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 700, 13, -19, 0, 20000, 0, 0, 0, 0);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(3), 3000);
					break;
				}
				case 3:
				{
					_antharas.broadcastPacket(new SocialAction(_antharas.getObjectId(), 1));
					for(Player pc : _players)
					{
						if(pc.getDistance(_antharas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 700, 13, 0, 6000, 20000, 0, 0, 0, 0);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(4), 10000);
					break;
				}
				case 4:
				{
					_antharas.broadcastPacket(new SocialAction(_antharas.getObjectId(), 2));
					for(Player pc : _players)
					{
						if(pc.getDistance(_antharas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 3700, 0, -3, 0, 10000, 0, 0, 0, 0);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(5), 200);
					break;
				}
				case 5:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_antharas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 1100, 0, -3, 22000, 30000, 0, 0, 0, 0);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(6), 10800);
					break;
				}
				case 6:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_antharas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 1100, 0, -3, 300, 7000, 0, 0, 0, 0);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(7), 7000);
					break;
				}
				case 7:
				{
					for(Player pc : _players)
					{
						pc.leaveMovieMode();
					}
					broadcastCustomScreenMessage("AntharasYoucannothope");
					_antharas.broadcastPacket(new PlaySound(PlaySound.Type.MUSIC, "BS02_A", 1, _antharas.getObjectId(), _antharas.getLoc()));
					_antharas.setAggroRange(_antharas.getTemplate().aggroRange);
					_antharas.setRunning();
					_antharas.moveToLocation(new Location(179011, 114871, -7704), 0, false);
					_sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 600000);
					break;
				}
				case 8:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_antharas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 1200, 20, -10, 0, 13000, 0, 0, 0, 0);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(9), 13000);
					break;
				}
				case 9:
				{
					for(Player pc : _players)
					{
						pc.leaveMovieMode();
					}
					broadcastCustomScreenMessage("ANTHARAS_THE_EVIL_LAND_DRAGON_ANTHARAS_DEFEATED");
					onAntharasDie();
				}
			}
		}
	}
}