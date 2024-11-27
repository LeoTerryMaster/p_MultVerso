package instances;

import l2.commons.listener.Listener;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.listener.actor.OnCurrentHpDamageListener;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Zone;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.Earthquake;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.network.l2.s2c.MagicSkillCanceled;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.network.l2.s2c.SocialAction;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.Location;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class Frintezza extends Reflection
{
	private static final int HallAlarmDevice = 18328;
	private static final int DarkChoirPlayer = 18339;
	private static final int _weakScarletId = 29046;
	private static final int _strongScarletId = 29047;
	private static final int TeleportCube = 29061;
	private static final int _frintezzasSwordId = 7903;
	private static final int DewdropItem = 8556;
	private static final int[] hallADoors = {25150051, 25150052, 25150053, 25150054, 25150055, 25150056, 25150057, 25150058};
	private static final int[] corridorADoors = {25150042, 25150043};
	private static final int[] hallBDoors = {25150061, 25150062, 25150063, 25150064, 25150065, 25150066, 25150067, 25150068, 25150069, 25150070};
	private static final int[] corridorBDoors = {25150045, 25150046};
	private static final int[] blockANpcs = {18329, 18330, 18331, 18333};
	private static final int[] blockBNpcs = {18334, 18335, 18336, 18337, 18338};
	private static final int _intervalOfFrintezzaSongs = 30000;
	private static final NpcLocation frintezzaSpawn = new NpcLocation(174240, -89805, -5022, 16048, 29045);
	private static final NpcLocation scarletSpawnWeak = new NpcLocation(174234, -88015, -5116, 48028, 29046);
	private static final NpcLocation[] portraitSpawns = {new NpcLocation(175880, -88696, -5104, 35048, 29048), new NpcLocation(175816, -87160, -5104, 28205, 29049), new NpcLocation(172648, -87176, -5104, 64817, 29048), new NpcLocation(172600, -88664, -5104, 57730, 29049)};
	private static final NpcLocation[] demonSpawns = {new NpcLocation(175880, -88696, -5104, 35048, 29050), new NpcLocation(175816, -87160, -5104, 28205, 29051), new NpcLocation(172648, -87176, -5104, 64817, 29051), new NpcLocation(172600, -88664, -5104, 57730, 29050)};
	private static final long battleStartDelay = 300000;
	private final NpcInstance[] portraits = new NpcInstance[4];
	private final NpcInstance[] demons = new NpcInstance[4];
	private final DeathListener _deathListener;
	private final CurrentHpListener _currentHpListener;
	private final ZoneListener _zoneListener;
	private NpcInstance _frintezzaDummy;
	private NpcInstance frintezza;
	private NpcInstance weakScarlet;
	private NpcInstance strongScarlet;
	private int _scarletMorph;
	private ScheduledFuture<?> musicTask;
	
	public Frintezza()
	{
		_deathListener = new DeathListener();
		_currentHpListener = new CurrentHpListener();
		_zoneListener = new ZoneListener();
	}
	
	@Override
	protected void onCreate()
	{
		super.onCreate();
		getZone("[Frintezza]").addListener(_zoneListener);
		for(NpcInstance n : getNpcs())
		{
			n.addListener((Listener) _deathListener);
		}
		blockUnblockNpcs(true, blockANpcs);
	}
	
	private NpcInstance spawn(NpcLocation loc)
	{
		return addSpawnWithoutRespawn(loc.npcId, loc, 0);
	}
	
	private void showSocialActionMovie(NpcInstance target, int dist, int yaw, int pitch, int time, int duration, int socialAction)
	{
		if(target == null)
		{
			return;
		}
		for(Player pc : getPlayers())
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
	
	private void cleanUp()
	{
		startCollapseTimer(900000);
		for(Player p : getPlayers())
		{
			p.sendPacket(new SystemMessage(2106).addNumber(15));
		}
		for(NpcInstance n : getNpcs())
		{
			n.deleteMe();
		}
	}
	
	private void blockUnblockNpcs(boolean block, int[] npcArray)
	{
		for(NpcInstance n : getNpcs())
		{
			if(!ArrayUtils.contains(npcArray, n.getNpcId()))
				continue;
			if(block)
			{
				n.block();
				n.setIsInvul(true);
				continue;
			}
			n.unblock();
			n.setIsInvul(false);
		}
	}
	
	@Override
	protected void onCollapse()
	{
		super.onCollapse();
		if(musicTask != null)
		{
			musicTask.cancel(true);
		}
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
	
	public class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
		}
		
		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			if(cha.isNpc() && (cha.getNpcId() == 29046 || cha.getNpcId() == 29047))
			{
				cha.teleToLocation(new Location(174240, -88020, -5112));
				((NpcInstance) cha).getAggroList().clear(true);
				cha.setCurrentHpMp((double) cha.getMaxHp(), (double) cha.getMaxMp());
				cha.broadcastCharInfo();
			}
		}
	}
	
	private class DeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature self, Creature killer)
		{
			if(self.isNpc())
			{
				if(self.getNpcId() == 18328)
				{
					for(int i = 0;i < hallADoors.length;++i)
					{
						openDoor(hallADoors[i]);
					}
					blockUnblockNpcs(false, blockANpcs);
					for(NpcInstance n : getNpcs())
					{
						if(!ArrayUtils.contains(blockANpcs, n.getNpcId()))
							continue;
						n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getPlayers().get(Rnd.get(getPlayers().size())), 200);
					}
				}
				else if(ArrayUtils.contains(blockANpcs, self.getNpcId()))
				{
					for(NpcInstance n : getNpcs())
					{
						if(!ArrayUtils.contains(blockANpcs, n.getNpcId()) || n.isDead())
							continue;
						return;
					}
					for(int i = 0;i < corridorADoors.length;++i)
					{
						openDoor(corridorADoors[i]);
					}
					blockUnblockNpcs(true, blockBNpcs);
				}
				else if(self.getNpcId() == 18339)
				{
					for(NpcInstance n : getNpcs())
					{
						if(n.getNpcId() != 18339 || n.isDead())
							continue;
						return;
					}
					for(int i = 0;i < hallBDoors.length;++i)
					{
						openDoor(hallBDoors[i]);
					}
					blockUnblockNpcs(false, blockBNpcs);
				}
				else if(ArrayUtils.contains(blockBNpcs, self.getNpcId()))
				{
					if(Rnd.chance(10))
					{
						((NpcInstance) self).dropItem(killer.getPlayer(), 8556, 1);
					}
					for(NpcInstance n : getNpcs())
					{
						if(!ArrayUtils.contains(blockBNpcs, n.getNpcId()) && !ArrayUtils.contains(blockANpcs, n.getNpcId()) || n.isDead())
							continue;
						return;
					}
					for(int i = 0;i < corridorBDoors.length;++i)
					{
						openDoor(corridorBDoors[i]);
					}
					ThreadPoolManager.getInstance().schedule(new FrintezzaStart(), 300000);
				}
				else
				{
					if(self.getNpcId() == 29046)
					{
						self.decayMe();
						return;
					}
					if(self.getNpcId() == 29047)
					{
						ThreadPoolManager.getInstance().schedule(new Die(1), 10);
						setReenterTime(System.currentTimeMillis());
					}
				}
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
						for(Player pc : getPlayers())
						{
							pc.leaveMovieMode();
						}
						cleanUp();
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
						for(Player pc : getPlayers())
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
						for(Player pc : getPlayers())
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
						for(Player pc : getPlayers())
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
						for(Player pc : getPlayers())
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
	
	private class SongEffectLaunched extends RunnableImpl
	{
		private final List<Creature> _targets;
		private final int _song;
		private final int _currentTime;
		
		public SongEffectLaunched(List<Creature> targets, int song, int currentTimeOfSong)
		{
			_targets = targets;
			_song = song;
			_currentTime = currentTimeOfSong;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(frintezza == null)
			{
				return;
			}
			if(_currentTime > _intervalOfFrintezzaSongs)
			{
				return;
			}
			SongEffectLaunched songLaunched = new SongEffectLaunched(_targets, _song, _currentTime + _intervalOfFrintezzaSongs / 10);
			ThreadPoolManager.getInstance().schedule(songLaunched, (long) (_intervalOfFrintezzaSongs / 10));
			frintezza.callSkill(SkillTable.getInstance().getInfo(5008, _song), _targets, false);
		}
	}
	
	private class Music extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(frintezza == null)
			{
				return;
			}
			int song = Math.max(1, Math.min(4, getSong()));
			String song_name;
			switch(song)
			{
				case 1:
				{
					song_name = "Requiem of Hatred";
					break;
				}
				case 2:
				{
					song_name = "Frenetic Toccata";
					break;
				}
				case 3:
				{
					song_name = "Fugue of Jubilation";
					break;
				}
				case 4:
				{
					song_name = "Mournful Chorale Prelude";
					break;
				}
				default:
				{
					return;
				}
			}
			if(!frintezza.isBlocked())
			{
				frintezza.broadcastPacket(new ExShowScreenMessage(song_name, 3000, 0, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
				frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5007, song, _intervalOfFrintezzaSongs, 0));
				ThreadPoolManager.getInstance().schedule(new SongEffectLaunched(getSongTargets(song), song, 10000), 10000);
			}
			musicTask = ThreadPoolManager.getInstance().schedule(new Music(), (long) (_intervalOfFrintezzaSongs + Rnd.get(10000)));
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
					if(portraits[i] != null && !portraits[i].isDead())
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
				for(Player pc : getPlayers())
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
			return Rnd.get(2, 4);
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
						closeDoor(corridorBDoors[1]);
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
						Earthquake eq = new Earthquake(weakScarlet.getLoc(), 50, 6);
						for(Player pc : getPlayers())
						{
							pc.broadcastPacket(eq);
						}
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
						for(Player pc : getPlayers())
						{
							pc.leaveMovieMode();
						}
						ThreadPoolManager.getInstance().schedule(new Spawn(23), 2000);
						break;
					}
					case 23:
					{
						blockAll(false);
						spawn(new NpcLocation(174056, -76024, -5104, 0, 29061));
						_scarletMorph = 1;
						musicTask = ThreadPoolManager.getInstance().schedule(new Music(), 5000);
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private class FrintezzaStart extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			ThreadPoolManager.getInstance().schedule(new Spawn(1), 1000);
		}
	}
}