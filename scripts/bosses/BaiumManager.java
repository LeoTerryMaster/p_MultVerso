package bosses;

import l2.commons.threading.RunnableImpl;
import l2.commons.time.cron.SchedulingPattern;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Zone;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.instances.BossInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.Earthquake;
import l2.gameserver.network.l2.s2c.PlaySound;
import l2.gameserver.network.l2.s2c.SocialAction;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.ReflectionUtils;
import l2.gameserver.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class BaiumManager extends Functions implements ScriptFile, OnDeathListener
{
	public static final int BAIUM_NPC_ID = 29020;
	private static final Logger _log = LoggerFactory.getLogger(BaiumManager.class);
	private static final List<NpcInstance> _monsters;
	private static final Map<Integer, SimpleSpawner> _monsterSpawn;
	private static final List<NpcInstance> _angels;
	private static final List<SimpleSpawner> _angelSpawns;
	private static final int ARCHANGEL = 29021;
	private static final int BAIUM_NPC = 29025;
	private static final AtomicBoolean Dying;
	private static final Location[] ANGEL_LOCATION;
	private static final Location CUBE_LOCATION;
	private static final Location STATUE_LOCATION;
	private static final int TELEPORT_CUBE = 31759;
	private static ScheduledFuture<?> _callAngelTask;
	private static ScheduledFuture<?> _cubeSpawnTask;
	private static ScheduledFuture<?> _intervalEndTask;
	private static ScheduledFuture<?> _killPcTask;
	private static ScheduledFuture<?> _mobiliseTask;
	private static ScheduledFuture<?> _moveAtRandomTask;
	private static ScheduledFuture<?> _sleepCheckTask;
	private static ScheduledFuture<?> _socialTask;
	private static ScheduledFuture<?> _socialTask2;
	private static ScheduledFuture<?> _onAnnihilatedTask;
	private static EpicBossState _state;
	private static long _lastAttackTime;
	private static NpcInstance _npcBaium;
	private static SimpleSpawner _statueSpawn;
	private static NpcInstance _teleportCube;
	private static SimpleSpawner _teleportCubeSpawn;
	private static Zone _zone;
	
	static
	{
		_lastAttackTime = 0;
		_statueSpawn = null;
		_teleportCube = null;
		_teleportCubeSpawn = null;
		_monsters = new ArrayList<>();
		_monsterSpawn = new ConcurrentHashMap<>();
		_angels = new ArrayList<>();
		_angelSpawns = new ArrayList<>();
		Dying = new AtomicBoolean(false);
		ANGEL_LOCATION = new Location[] {new Location(113004, 16209, 10076, 60242), new Location(114053, 16642, 10076, 4411), new Location(114563, 17184, 10076, 49241), new Location(116356, 16402, 10076, 31109), new Location(115015, 16393, 10076, 32760), new Location(115481, 15335, 10076, 16241), new Location(114680, 15407, 10051, 32485), new Location(114886, 14437, 10076, 16868), new Location(115391, 17593, 10076, 55346), new Location(115245, 17558, 10076, 35536)};
		CUBE_LOCATION = new Location(115203, 16620, 10078, 0);
		STATUE_LOCATION = new Location(115996, 17417, 10106, 41740);
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
			_onAnnihilatedTask = ThreadPoolManager.getInstance().schedule(new onAnnihilated(), Config.BAIUM_CLEAR_ZONE_IF_ALL_DIE);
		}
	}
	
	private static void deleteArchangels()
	{
		for(NpcInstance angel : _angels)
		{
			if(angel == null || angel.getSpawn() == null)
				continue;
			angel.getSpawn().stopRespawn();
			angel.deleteMe();
		}
		_angels.clear();
	}
	
	private static List<Player> getPlayersInside()
	{
		return getZone().getInsidePlayers();
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
	
	public static void onBaiumDie(Creature self)
	{
		if(!Dying.compareAndSet(false, true))
		{
			return;
		}
		self.broadcastPacket(new PlaySound(PlaySound.Type.MUSIC, "BS02_D", 1, 0, self.getLoc()));
		_state.setRespawnDate(getRespawnInterval());
		_state.setState(EpicBossState.State.INTERVAL);
		_state.update();
		scheduleIntervalEnd();
		Log.add("Baium died", "bosses");
		deleteArchangels();
		_cubeSpawnTask = ThreadPoolManager.getInstance().schedule(new CubeSpawn(), 10000);
	}
	
	private static long getRespawnInterval()
	{
		if(!Config.FWA_FIXTIMEPATTERNOFBAIUM.isEmpty())
		{
			long now = System.currentTimeMillis();
			try
			{
				SchedulingPattern timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFBAIUM);
				long delay = timePattern.next(now) - now;
				return Math.max(60000, delay);
			}
			catch(SchedulingPattern.InvalidPatternException e)
			{
				throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFBAIUM + "\" in " + BaiumManager.class.getSimpleName(), e);
			}
		}
		return (long) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (double) (Config.FWB_FIXINTERVALOFBAIUM + Rnd.get((long) 0, Config.FWB_RANDOMINTERVALOFBAIUM)));
	}
	
	private static void setIntervalEndTask()
	{
		setUnspawn();
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
	
	public static void setLastAttackTime()
	{
		_lastAttackTime = System.currentTimeMillis();
	}
	
	public static void setUnspawn()
	{
		banishForeigners();
		deleteArchangels();
		for(NpcInstance mob : _monsters)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_monsters.clear();
		if(_teleportCube != null)
		{
			_teleportCube.getSpawn().stopRespawn();
			_teleportCube.deleteMe();
			_teleportCube = null;
		}
		if(_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(false);
			_cubeSpawnTask = null;
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
		if(_socialTask2 != null)
		{
			_socialTask2.cancel(false);
			_socialTask2 = null;
		}
		if(_killPcTask != null)
		{
			_killPcTask.cancel(false);
			_killPcTask = null;
		}
		if(_callAngelTask != null)
		{
			_callAngelTask.cancel(false);
			_callAngelTask = null;
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
	
	private static void sleepBaium()
	{
		setUnspawn();
		Log.add("Baium going to sleep, spawning statue", "bosses");
		_state.setState(EpicBossState.State.NOTSPAWN);
		_state.update();
		_statueSpawn.doSpawn(true);
	}
	
	public static void spawnBaium(NpcInstance NpcBaium, Player awake_by)
	{
		Dying.set(false);
		_npcBaium = NpcBaium;
		SimpleSpawner baiumSpawn = _monsterSpawn.get(29020);
		baiumSpawn.setLoc(_npcBaium.getLoc());
		_npcBaium.getSpawn().stopRespawn();
		_npcBaium.deleteMe();
		BossInstance baium = (BossInstance) baiumSpawn.doSpawn(true);
		_monsters.add(baium);
		_state.setRespawnDate(getRespawnInterval());
		_state.setState(EpicBossState.State.ALIVE);
		_state.update();
		Log.add("Spawned Baium, awake by: " + awake_by, "bosses");
		setLastAttackTime();
		baium.startImmobilized();
		baium.broadcastPacket(new PlaySound(PlaySound.Type.MUSIC, "BS02_A", 1, 0, baium.getLoc()));
		baium.broadcastPacket(new SocialAction(baium.getObjectId(), 2));
		_socialTask = ThreadPoolManager.getInstance().schedule(new Social(baium, 3), 15000);
		ThreadPoolManager.getInstance().schedule(new EarthquakeTask(baium), 25000);
		_socialTask2 = ThreadPoolManager.getInstance().schedule(new Social(baium, 1), 25000);
		_killPcTask = ThreadPoolManager.getInstance().schedule(new KillPc(awake_by, baium), 26000);
		_callAngelTask = ThreadPoolManager.getInstance().schedule(new CallArchAngel(), 35000);
		_mobiliseTask = ThreadPoolManager.getInstance().schedule(new SetMobilised(baium), 35500);
		Location pos = new Location(Rnd.get(112826, 116241), Rnd.get(15575, 16375), 10078, 0);
		_moveAtRandomTask = ThreadPoolManager.getInstance().schedule(new MoveAtRandom(baium, pos), 36000);
		_sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 600000);
	}
	
	private void init()
	{
		_state = new EpicBossState(29020);
		_zone = ReflectionUtils.getZone("[baium_epic]");
		CharListenerList.addGlobal(this);
		try
		{
			_statueSpawn = new SimpleSpawner(NpcHolder.getInstance().getTemplate(29025));
			_statueSpawn.setAmount(1);
			_statueSpawn.setLoc(STATUE_LOCATION);
			_statueSpawn.stopRespawn();
			SimpleSpawner tempSpawn = new SimpleSpawner(NpcHolder.getInstance().getTemplate(29020));
			tempSpawn.setAmount(1);
			_monsterSpawn.put(29020, tempSpawn);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			NpcTemplate Cube = NpcHolder.getInstance().getTemplate(31759);
			_teleportCubeSpawn = new SimpleSpawner(Cube);
			_teleportCubeSpawn.setAmount(1);
			_teleportCubeSpawn.setLoc(CUBE_LOCATION);
			_teleportCubeSpawn.setRespawnDelay(60);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			NpcTemplate angel = NpcHolder.getInstance().getTemplate(29021);
			_angelSpawns.clear();
			ArrayList<Integer> random = new ArrayList<>();
			for(int i = 0;i < 5;++i)
			{
				int r = -1;
				while(r == -1 || random.contains(r))
				{
					r = Rnd.get(10);
				}
				random.add(r);
			}
			for(int i2 : random)
			{
				SimpleSpawner spawnDat = new SimpleSpawner(angel);
				spawnDat.setAmount(1);
				spawnDat.setLoc(ANGEL_LOCATION[i2]);
				spawnDat.setRespawnDelay(300000);
				_angelSpawns.add(spawnDat);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		_log.info("BaiumManager: State of Baium is " + _state.getState() + ".");
		if(_state.getState().equals(EpicBossState.State.NOTSPAWN))
		{
			_statueSpawn.doSpawn(true);
		}
		else if(_state.getState().equals(EpicBossState.State.ALIVE))
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
			_statueSpawn.doSpawn(true);
		}
		else if(_state.getState().equals(EpicBossState.State.INTERVAL) || _state.getState().equals(EpicBossState.State.DEAD))
		{
			setIntervalEndTask();
		}
		_log.info("BaiumManager: Next spawn date: " + TimeUtils.toSimpleFormat(_state.getRespawnDate()));
	}
	
	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if(self.isPlayer() && _state != null && _state.getState() == EpicBossState.State.ALIVE && _zone != null && _zone.checkIfInZone(self))
		{
			checkAnnihilated();
		}
		else if(self.isNpc() && self.getNpcId() == 29020)
		{
			onBaiumDie(self);
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
		sleepBaium();
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	public static class EarthquakeTask extends RunnableImpl
	{
		private final BossInstance baium;
		
		public EarthquakeTask(BossInstance _baium)
		{
			baium = _baium;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			Earthquake eq = new Earthquake(baium.getLoc(), 40, 5);
			baium.broadcastPacket(eq);
		}
	}
	
	public static class onAnnihilated extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			Class<BaiumManager> class_ = BaiumManager.class;
			synchronized(BaiumManager.class)
			{
				if(!isPlayersAnnihilated() || _state.getState() == EpicBossState.State.INTERVAL)
				{
					_onAnnihilatedTask = null;
					return;
				}
				sleepBaium();
				return;
			}
		}
	}
	
	public static class Social extends RunnableImpl
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
			SocialAction sa = new SocialAction(_npc.getObjectId(), _action);
			_npc.broadcastPacket(sa);
		}
	}
	
	public static class SetMobilised extends RunnableImpl
	{
		private final BossInstance _boss;
		
		public SetMobilised(BossInstance boss)
		{
			_boss = boss;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			_boss.stopImmobilized();
		}
	}
	
	public static class MoveAtRandom extends RunnableImpl
	{
		private final NpcInstance _npc;
		private final Location _pos;
		
		public MoveAtRandom(NpcInstance npc, Location pos)
		{
			_npc = npc;
			_pos = pos;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(_npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
			{
				_npc.moveToLocation(_pos, 0, false);
			}
		}
	}
	
	public static class KillPc extends RunnableImpl
	{
		private final BossInstance _boss;
		private final Player _target;
		
		public KillPc(Player target, BossInstance boss)
		{
			_target = target;
			_boss = boss;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			Skill skill = SkillTable.getInstance().getInfo(4136, 1);
			if(_target != null && skill != null)
			{
				_boss.setTarget(_target);
				_boss.doCast(skill, _target, false);
			}
		}
	}
	
	public static class IntervalEnd extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			setUnspawn();
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
			_statueSpawn.doSpawn(true);
		}
	}
	
	public static class CubeSpawn extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			_teleportCube = _teleportCubeSpawn.doSpawn(true);
		}
	}
	
	public static class CheckLastAttack extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(_state.getState().equals(EpicBossState.State.ALIVE))
			{
				if(_lastAttackTime + Config.FWB_LIMITUNTILSLEEPBAIUM < System.currentTimeMillis())
				{
					sleepBaium();
				}
				else
				{
					_sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 60000);
				}
			}
		}
	}
	
	public static class CallArchAngel extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			for(SimpleSpawner spawn : _angelSpawns)
			{
				_angels.add(spawn.doSpawn(true));
			}
		}
	}
}