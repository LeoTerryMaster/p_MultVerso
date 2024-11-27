package bosses;

import l2.commons.threading.RunnableImpl;
import l2.commons.time.cron.SchedulingPattern;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.data.StringHolder;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Spawner;
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

public class ValakasManager extends Functions implements ScriptFile, OnDeathListener
{
	public static final int VALAKAS_NPC_ID = 29028;
	private static final Logger _log = LoggerFactory.getLogger(ValakasManager.class);
	private static final int[][] _teleportCubeLocation = {{214880, -116144, -1644, 0}, {213696, -116592, -1644, 0}, {212112, -116688, -1644, 0}, {211184, -115472, -1664, 0}, {210336, -114592, -1644, 0}, {211360, -113904, -1644, 0}, {213152, -112352, -1644, 0}, {214032, -113232, -1644, 0}, {214752, -114592, -1644, 0}, {209824, -115568, -1421, 0}, {210528, -112192, -1403, 0}, {213120, -111136, -1408, 0}, {215184, -111504, -1392, 0}, {215456, -117328, -1392, 0}, {213200, -118160, -1424, 0}};
	private static final List<NpcInstance> _teleportCube = new ArrayList<>();
	private static final int _teleportCubeId = 31759;
	private static final AtomicBoolean Dying;
	private static final Location TELEPORT_POSITION;
	private static BossInstance _valakas;
	private static ScheduledFuture<?> _valakasSpawnTask;
	private static ScheduledFuture<?> _intervalEndTask;
	private static ScheduledFuture<?> _socialTask;
	private static ScheduledFuture<?> _mobiliseTask;
	private static ScheduledFuture<?> _moveAtRandomTask;
	private static ScheduledFuture<?> _respawnValakasTask;
	private static ScheduledFuture<?> _sleepCheckTask;
	private static ScheduledFuture<?> _onAnnihilatedTask;
	private static EpicBossState _state;
	private static Zone _zone;
	private static long _lastAttackTime;
	private static volatile boolean _entryLocked;
	
	static
	{
		_valakasSpawnTask = null;
		_intervalEndTask = null;
		_socialTask = null;
		_mobiliseTask = null;
		_moveAtRandomTask = null;
		_respawnValakasTask = null;
		_sleepCheckTask = null;
		_onAnnihilatedTask = null;
		_lastAttackTime = 0;
		Dying = new AtomicBoolean(false);
		TELEPORT_POSITION = new Location(203940, -111840, 66);
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
			_onAnnihilatedTask = ThreadPoolManager.getInstance().schedule(new onAnnihilated(), Config.VALAKAS_CLEAR_ZONE_IF_ALL_DIE);
		}
	}
	
	private static List<Player> getPlayersInside()
	{
		return getZone().getInsidePlayers();
	}
	
	private static long getRespawnInterval()
	{
		if(!Config.FWA_FIXTIMEPATTERNOFVALAKAS.isEmpty())
		{
			long now = System.currentTimeMillis();
			try
			{
				SchedulingPattern timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFVALAKAS);
				long delay = timePattern.next(now) - now;
				return Math.max(60000, delay);
			}
			catch(SchedulingPattern.InvalidPatternException e)
			{
				throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFVALAKAS + "\" in " + AntharasManager.class.getSimpleName(), e);
			}
		}
		return (long) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (double) Config.FWV_FIXINTERVALOFVALAKAS);
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
	
	private static void onValakasDie()
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
		for(int[] ints : _teleportCubeLocation)
		{
			_teleportCube.add(Functions.spawn(new Location(ints[0], ints[1], ints[2], ints[3]), 31759));
		}
		Log.add("Valakas died", "bosses");
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
		_entryLocked = false;
		if(_valakas != null)
		{
			_valakas.deleteMe();
		}
		for(NpcInstance cube : _teleportCube)
		{
			if(cube == null)
				continue;
			Spawner spawner = cube.getSpawn();
			if(spawner != null)
			{
				spawner.stopRespawn();
			}
			cube.deleteMe();
		}
		_teleportCube.clear();
		if(_valakasSpawnTask != null)
		{
			_valakasSpawnTask.cancel(false);
			_valakasSpawnTask = null;
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
		if(_mobiliseTask != null)
		{
			_mobiliseTask.cancel(false);
			_mobiliseTask = null;
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
		if(_respawnValakasTask != null)
		{
			_respawnValakasTask.cancel(false);
			_respawnValakasTask = null;
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
	
	public static synchronized void setValakasSpawnTask()
	{
		if(_valakasSpawnTask == null)
		{
			_valakasSpawnTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(1), Config.FWV_APPTIMEOFVALAKAS);
		}
	}
	
	public static void broadcastCustomScreenMessage(String address)
	{
		for(Player p : getPlayersInside())
		{
			p.sendPacket(new ExShowScreenMessage(StringHolder.getInstance().getNotNull(p, address), 8000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false));
		}
	}
	
	public static boolean isEnableEnterToLair()
	{
		return _state.getState() == EpicBossState.State.NOTSPAWN;
	}
	
	public static void enterTheLair(Player player)
	{
		if(player == null)
		{
			return;
		}
		if(_entryLocked)
		{
			player.sendMessage(new CustomMessage("ValakasStillReborn", player));
			return;
		}
		switch(_state.getState())
		{
			case ALIVE:
			{
				player.sendMessage(new CustomMessage("ValakasAlreadyReborn", player));
				return;
			}
			case DEAD:
			case INTERVAL:
			{
				player.sendMessage(new CustomMessage("ValakasStillReborn", player));
				return;
			}
		}
		if(player.isDead() || player.isFlying() || player.isCursedWeaponEquipped() || !player.isInRange(player, 500))
		{
			player.sendMessage(new CustomMessage("ValakasPlayerNotRequirements", player));
			return;
		}
		player.teleToLocation(TELEPORT_POSITION);
		setValakasSpawnTask();
	}
	
	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if(self.isPlayer() && _state != null && _state.getState() == EpicBossState.State.ALIVE && _zone != null && _zone.checkIfInZone(self.getX(), self.getY()))
		{
			checkAnnihilated();
		}
		else if(self.isNpc() && self.getNpcId() == 29028)
		{
			ThreadPoolManager.getInstance().schedule(new SpawnDespawn(12), 1);
		}
	}
	
	private void init()
	{
		CharListenerList.addGlobal(this);
		_state = new EpicBossState(29028);
		_zone = ReflectionUtils.getZone("[valakas_epic]");
		_log.info("ValakasManager: State of Valakas is " + _state.getState() + ".");
		if(!_state.getState().equals(EpicBossState.State.NOTSPAWN))
		{
			setIntervalEndTask();
		}
		_log.info("ValakasManager: Next spawn date of Valakas is " + TimeUtils.toSimpleFormat(_state.getRespawnDate()) + ".");
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
	
	private static class SpawnDespawn extends RunnableImpl
	{
		private final int _distance = 2550;
		private final int _taskId;
		private final List<Player> _players = getPlayersInside();
		
		SpawnDespawn(int taskId)
		{
			_taskId = taskId;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			switch(_taskId)
			{
				case 1:
				{
					_valakas = (BossInstance) Functions.spawn(new Location(212852, -114842, -1632, 833), 29028);
					Dying.set(false);
					_valakas.block();
					_valakas.broadcastPacket(new PlaySound(PlaySound.Type.MUSIC, "BS03_A", 1, _valakas.getObjectId(), _valakas.getLoc()));
					_state.setRespawnDate(Config.FWV_FIXINTERVALOFVALAKAS);
					_state.setState(EpicBossState.State.ALIVE);
					_state.update();
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(2), 16);
					_entryLocked = true;
					break;
				}
				case 2:
				{
					_valakas.broadcastPacket(new SocialAction(_valakas.getObjectId(), 1));
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1800, 180, -1, 1500, 15000, 0, 0, 1, 0);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(3), 1500);
					break;
				}
				case 3:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1300, 180, -5, 3000, 15000, 0, -5, 1, 0);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(4), 3300);
					break;
				}
				case 4:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 500, 180, -8, 600, 15000, 0, 60, 1, 0);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(5), 2900);
					break;
				}
				case 5:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 800, 180, -8, 2700, 15000, 0, 30, 1, 0);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(6), 2700);
					break;
				}
				case 6:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 200, 250, 70, 0, 15000, 30, 80, 1, 0);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(7), 1);
					break;
				}
				case 7:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1100, 250, 70, 2500, 15000, 30, 80, 1, 0);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(8), 3200);
					break;
				}
				case 8:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 700, 150, 30, 0, 15000, -10, 60, 1, 0);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(9), 1400);
					break;
				}
				case 9:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1200, 150, 20, 2900, 15000, -10, 30, 1, 0);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(10), 6700);
					break;
				}
				case 10:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 750, 170, -10, 3400, 15000, 10, -15, 1, 0);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(11), 5700);
					break;
				}
				case 11:
				{
					for(Player pc : _players)
					{
						pc.leaveMovieMode();
					}
					_valakas.unblock();
					broadcastCustomScreenMessage("ValakasFool");
					if(_valakas.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
					{
						_valakas.moveToLocation(new Location(Rnd.get(211080, 214909), Rnd.get(-115841, -112822), -1662, 0), 0, false);
					}
					_sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 600000);
					break;
				}
				case 12:
				{
					_valakas.broadcastPacket(new PlaySound(PlaySound.Type.MUSIC, "B03_D", 1, _valakas.getObjectId(), _valakas.getLoc()));
					broadcastCustomScreenMessage("VALAKAS_THE_EVIL_FIRE_DRAGON_VALAKAS_DEFEATED");
					onValakasDie();
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 2000, 130, -1, 0, 15000, 0, 0, 1, 1);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(13), 500);
					break;
				}
				case 13:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1100, 210, -5, 3000, 15000, -13, 0, 1, 1);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(14), 3500);
					break;
				}
				case 14:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1300, 200, -8, 3000, 15000, 0, 15, 1, 1);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(15), 4500);
					break;
				}
				case 15:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1000, 190, 0, 500, 15000, 0, 10, 1, 1);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(16), 500);
					break;
				}
				case 16:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1700, 120, 0, 2500, 15000, 12, 40, 1, 1);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(17), 4600);
					break;
				}
				case 17:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1700, 20, 0, 700, 15000, 10, 10, 1, 1);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(18), 750);
					break;
				}
				case 18:
				{
					for(Player pc : _players)
					{
						if(pc.getDistance(_valakas) <= (double) _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1700, 10, 0, 1000, 15000, 20, 70, 1, 1);
							continue;
						}
						pc.leaveMovieMode();
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(19), 2500);
					break;
				}
				case 19:
				{
					for(Player pc : _players)
					{
						pc.leaveMovieMode();
					}
					break;
				}
			}
		}
	}
	
	private static class onAnnihilated extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			Class<ValakasManager> class_ = ValakasManager.class;
			synchronized(ValakasManager.class)
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
	
	private static class IntervalEnd extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			setUnspawn();
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
		}
	}
	
	private static class CheckLastAttack extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(_state.getState() == EpicBossState.State.ALIVE)
			{
				if(_lastAttackTime + Config.FWV_LIMITUNTILSLEEPVALAKAS < System.currentTimeMillis())
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
}