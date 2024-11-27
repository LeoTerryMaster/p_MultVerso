package l2.gameserver.model;

import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.GameTimeController;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.instancemanager.games.FishingChampionShipManager;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.network.l2.s2c.ExFishingEnd;
import l2.gameserver.network.l2.s2c.ExFishingHpRegen;
import l2.gameserver.network.l2.s2c.ExFishingStart;
import l2.gameserver.network.l2.s2c.ExFishingStartCombat;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.templates.FishTemplate;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Location;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Fishing
{
	public static final int FISHING_NONE = 0;
	public static final int FISHING_STARTED = 1;
	public static final int FISHING_WAITING = 2;
	public static final int FISHING_COMBAT = 3;
	private final Player _fisher;
	private final AtomicInteger _state;
	private final Location _fishLoc = new Location();
	private int _time;
	private int _stop;
	private int _gooduse;
	private int _anim;
	private int _combatMode = -1;
	private int _deceptiveMode;
	private int _fishCurHP;
	private FishTemplate _fish;
	private int _lureId;
	private Future<?> _fishingTask;
	
	public Fishing(Player fisher)
	{
		_fisher = fisher;
		_state = new AtomicInteger(0);
	}
	
	private static void showMessage(Player fisher, int dmg, int pen, Skill.SkillType skillType, int messageId)
	{
		switch(messageId)
		{
			case 1:
			{
				if(skillType == Skill.SkillType.PUMPING)
				{
					fisher.sendPacket(new SystemMessage(1465).addNumber(dmg));
					if(pen != 50)
						break;
					fisher.sendPacket(new SystemMessage(1672).addNumber(pen));
					break;
				}
				fisher.sendPacket(new SystemMessage(1467).addNumber(dmg));
				if(pen != 50)
					break;
				fisher.sendPacket(new SystemMessage(1671).addNumber(pen));
				break;
			}
			case 2:
			{
				if(skillType == Skill.SkillType.PUMPING)
				{
					fisher.sendPacket(new SystemMessage(1466).addNumber(dmg));
					break;
				}
				fisher.sendPacket(new SystemMessage(1468).addNumber(dmg));
				break;
			}
			case 3:
			{
				if(skillType == Skill.SkillType.PUMPING)
				{
					fisher.sendPacket(new SystemMessage(1465).addNumber(dmg));
					if(pen != 50)
						break;
					fisher.sendPacket(new SystemMessage(1672).addNumber(pen));
					break;
				}
				fisher.sendPacket(new SystemMessage(1467).addNumber(dmg));
				if(pen != 50)
					break;
				fisher.sendPacket(new SystemMessage(1671).addNumber(pen));
				break;
			}
		}
	}
	
	public static void spawnPenaltyMonster(Player fisher)
	{
		int npcId = 18319 + Math.min(fisher.getLevel() / 11, 7);
		MonsterInstance npc = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(npcId));
		npc.setSpawnedLoc(Location.findPointToStay(fisher, 100, 120));
		npc.setReflection(fisher.getReflection());
		npc.setHeading(fisher.getHeading() - 32768);
		npc.spawnMe(npc.getSpawnedLoc());
		npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, fisher, Rnd.get(1, 100));
	}
	
	public static int getRandomFishType(int lureId, int fishLvl, int dist)
	{
		int type;
		int check = Rnd.get(100);
		switch(lureId)
		{
			case 7807:
			{
				if(check <= 54)
				{
					type = 5;
					break;
				}
				if(check <= 77)
				{
					type = 4;
					break;
				}
				type = 6;
				break;
			}
			case 7808:
			{
				if(check <= 54)
				{
					type = 4;
					break;
				}
				if(check <= 77)
				{
					type = 6;
					break;
				}
				type = 5;
				break;
			}
			case 7809:
			{
				if(check <= 54)
				{
					type = 6;
					break;
				}
				if(check <= 77)
				{
					type = 5;
					break;
				}
				type = 4;
				break;
			}
			case 8486:
			{
				if(check <= 33)
				{
					type = 4;
					break;
				}
				if(check <= 66)
				{
					type = 5;
					break;
				}
				type = 6;
				break;
			}
			case 7610:
			case 7611:
			case 7612:
			case 7613:
			case 8496:
			case 8497:
			case 8498:
			case 8499:
			case 8500:
			case 8501:
			case 8502:
			case 8503:
			case 8504:
			{
				type = 3;
				break;
			}
			case 8548:
			{
				if(check <= 32)
				{
					type = 1;
					break;
				}
				if(check <= 64)
				{
					type = 2;
					break;
				}
				if(check <= 96)
				{
					type = 0;
					break;
				}
				if(dist == 4 && fishLvl > 19)
				{
					type = 10;
					break;
				}
				type = 0;
				break;
			}
			case 6519:
			case 6520:
			case 6521:
			case 8505:
			case 8507:
			{
				if(check <= 54)
				{
					type = 1;
					break;
				}
				if(check <= 74)
				{
					type = 0;
					break;
				}
				if(check <= 94)
				{
					type = 2;
					break;
				}
				type = 3;
				break;
			}
			case 6522:
			case 6523:
			case 6524:
			case 8508:
			case 8510:
			{
				if(check <= 54)
				{
					type = 0;
					break;
				}
				if(check <= 74)
				{
					type = 1;
					break;
				}
				if(check <= 94)
				{
					type = 2;
					break;
				}
				type = 3;
				break;
			}
			case 6525:
			case 6526:
			case 6527:
			case 8511:
			case 8513:
			{
				if(check <= 55)
				{
					type = 2;
					break;
				}
				if(check <= 74)
				{
					type = 1;
					break;
				}
				if(check <= 94)
				{
					type = 0;
					break;
				}
				type = 3;
				break;
			}
			case 8484:
			{
				if(check <= 33)
				{
					type = 0;
					break;
				}
				if(check <= 66)
				{
					type = 1;
					break;
				}
				type = 2;
				break;
			}
			case 8506:
			{
				if(check <= 54)
				{
					type = 8;
					break;
				}
				if(check <= 77)
				{
					type = 7;
					break;
				}
				type = 9;
				break;
			}
			case 8509:
			{
				if(check <= 54)
				{
					type = 7;
					break;
				}
				if(check <= 77)
				{
					type = 9;
					break;
				}
				type = 8;
				break;
			}
			case 8512:
			{
				if(check <= 54)
				{
					type = 9;
					break;
				}
				if(check <= 77)
				{
					type = 8;
					break;
				}
				type = 7;
				break;
			}
			case 8485:
			{
				if(check <= 33)
				{
					type = 7;
					break;
				}
				if(check <= 66)
				{
					type = 8;
					break;
				}
				type = 9;
				break;
			}
			default:
			{
				type = 1;
			}
		}
		return type;
	}
	
	public static int getRandomFishLvl(Player player)
	{
		Effect effect = player.getEffectList().getEffectByStackType("fishPot");
		int skilllvl = effect != null ? (int) effect.getSkill().getPower() : player.getSkillLevel(1315);
		if(skilllvl <= 0)
		{
			return 1;
		}
		int check = Rnd.get(100);
		int randomlvl;
		if(check < 50)
		{
			randomlvl = skilllvl;
		}
		else if(check <= 85)
		{
			randomlvl = skilllvl - 1;
			if(randomlvl <= 0)
			{
				randomlvl = 1;
			}
		}
		else
		{
			randomlvl = skilllvl + 1;
		}
		randomlvl = Math.min(27, Math.max(1, randomlvl));
		return randomlvl;
	}
	
	public static int getFishGroup(int lureId)
	{
		switch(lureId)
		{
			case 7807:
			case 7808:
			case 7809:
			case 8486:
			{
				return 0;
			}
			case 8485:
			case 8506:
			case 8509:
			case 8512:
			{
				return 2;
			}
		}
		return 1;
	}
	
	public static int getLureGrade(int lureId)
	{
		switch(lureId)
		{
			case 6519:
			case 6522:
			case 6525:
			case 8505:
			case 8508:
			case 8511:
			{
				return 0;
			}
			case 6520:
			case 6523:
			case 6526:
			case 7610:
			case 7611:
			case 7612:
			case 7613:
			case 7807:
			case 7808:
			case 7809:
			case 8484:
			case 8485:
			case 8486:
			case 8496:
			case 8497:
			case 8498:
			case 8499:
			case 8500:
			case 8501:
			case 8502:
			case 8503:
			case 8504:
			case 8506:
			case 8509:
			case 8512:
			case 8548:
			{
				return 1;
			}
			case 6521:
			case 6524:
			case 6527:
			case 8507:
			case 8510:
			case 8513:
			{
				return 2;
			}
		}
		return -1;
	}
	
	public static boolean isNightLure(int lureId)
	{
		switch(lureId)
		{
			case 8505:
			case 8508:
			case 8511:
			{
				return true;
			}
			case 8496:
			case 8497:
			case 8498:
			case 8499:
			case 8500:
			case 8501:
			case 8502:
			case 8503:
			case 8504:
			{
				return true;
			}
			case 8506:
			case 8509:
			case 8512:
			{
				return true;
			}
			case 8510:
			case 8513:
			{
				return true;
			}
			case 8485:
			{
				return true;
			}
		}
		return false;
	}
	
	public void setFish(FishTemplate fish)
	{
		_fish = fish;
	}
	
	public int getLureId()
	{
		return _lureId;
	}
	
	public void setLureId(int lureId)
	{
		_lureId = lureId;
	}
	
	public Location getFishLoc()
	{
		return _fishLoc;
	}
	
	public void setFishLoc(Location loc)
	{
		_fishLoc.x = loc.x;
		_fishLoc.y = loc.y;
		_fishLoc.z = loc.z;
	}
	
	public void startFishing()
	{
		if(!_state.compareAndSet(0, 1))
		{
			return;
		}
		_fisher.setFishing(true);
		_fisher.broadcastCharInfo();
		_fisher.broadcastPacket(new ExFishingStart(_fisher, _fish.getType(), _fisher.getFishLoc(), isNightLure(_lureId)));
		_fisher.sendPacket(Msg.STARTS_FISHING);
		startLookingForFishTask();
	}
	
	public void stopFishing()
	{
		if(_state.getAndSet(0) == 0)
		{
			return;
		}
		stopFishingTask();
		_fisher.setFishing(false);
		_fisher.broadcastPacket(new ExFishingEnd(_fisher, false));
		_fisher.broadcastCharInfo();
		_fisher.sendPacket(Msg.CANCELS_FISHING);
	}
	
	public void endFishing(boolean win)
	{
		if(!_state.compareAndSet(3, 0))
		{
			return;
		}
		stopFishingTask();
		_fisher.setFishing(false);
		_fisher.broadcastPacket(new ExFishingEnd(_fisher, win));
		_fisher.broadcastCharInfo();
		_fisher.sendPacket(Msg.ENDS_FISHING);
	}
	
	private void stopFishingTask()
	{
		if(_fishingTask != null)
		{
			_fishingTask.cancel(false);
			_fishingTask = null;
		}
	}
	
	private void startLookingForFishTask()
	{
		if(!_state.compareAndSet(1, 2))
		{
			return;
		}
		long checkDelay = 10000;
		switch(_fish.getGroup())
		{
			case 0:
			{
				checkDelay = Math.round((double) _fish.getGutsCheckTime() * 1.33);
				break;
			}
			case 1:
			{
				checkDelay = _fish.getGutsCheckTime();
				break;
			}
			case 2:
			{
				checkDelay = Math.round((double) _fish.getGutsCheckTime() * 0.66);
			}
		}
		_fishingTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new LookingForFishTask(), 10000, checkDelay);
	}
	
	public boolean isInCombat()
	{
		return _state.get() == 3;
	}
	
	private void startFishCombat()
	{
		if(!_state.compareAndSet(2, 3))
		{
			return;
		}
		_stop = 0;
		_gooduse = 0;
		_anim = 0;
		_time = _fish.getCombatTime() / 1000;
		_fishCurHP = _fish.getHP();
		_combatMode = Rnd.chance(20) ? 1 : 0;
		switch(getLureGrade(_lureId))
		{
			case 0:
			case 1:
			{
				_deceptiveMode = 0;
				break;
			}
			case 2:
			{
				_deceptiveMode = Rnd.chance(10) ? 1 : 0;
			}
		}
		ExFishingStartCombat efsc = new ExFishingStartCombat(_fisher, _time, _fish.getHP(), _combatMode, _fish.getGroup(), _deceptiveMode);
		_fisher.broadcastPacket(efsc);
		_fisher.sendPacket(Msg.SUCCEEDED_IN_GETTING_A_BITE);
		_fishingTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new FishCombatTask(), 1000, 1000);
	}
	
	private void changeHp(int hp, int pen)
	{
		_fishCurHP -= hp;
		if(_fishCurHP < 0)
		{
			_fishCurHP = 0;
		}
		_fisher.broadcastPacket(new ExFishingHpRegen(_fisher, _time, _fishCurHP, _combatMode, _gooduse, _anim, pen, _deceptiveMode));
		_gooduse = 0;
		_anim = 0;
		if(_fishCurHP > _fish.getHP() * 2)
		{
			_fishCurHP = _fish.getHP() * 2;
			doDie(false);
		}
		else if(_fishCurHP == 0)
		{
			doDie(true);
		}
	}
	
	private void doDie(boolean win)
	{
		stopFishingTask();
		if(win)
		{
			if(!_fisher.isInPeaceZone() && Rnd.chance(5))
			{
				win = false;
				_fisher.sendPacket(Msg.YOU_HAVE_CAUGHT_A_MONSTER);
				spawnPenaltyMonster(_fisher);
			}
			else
			{
				_fisher.sendPacket(Msg.SUCCEEDED_IN_FISHING);
				ItemFunctions.addItem(_fisher, _fish.getId(), 1, true);
				FishingChampionShipManager.getInstance().newFish(_fisher, _lureId);
			}
		}
		endFishing(win);
	}
	
	public void useFishingSkill(int dmg, int pen, Skill.SkillType skillType)
	{
		if(!isInCombat())
		{
			return;
		}
		int mode = skillType == Skill.SkillType.REELING && !GameTimeController.getInstance().isNowNight() ? 1 : skillType == Skill.SkillType.PUMPING && GameTimeController.getInstance().isNowNight() ? 1 : 0;
		_anim = mode + 1;
		if(Rnd.chance(10))
		{
			_fisher.sendPacket(Msg.FISH_HAS_RESISTED);
			_gooduse = 0;
			changeHp(0, pen);
			return;
		}
		if(_combatMode == mode)
		{
			if(_deceptiveMode == 0)
			{
				showMessage(_fisher, dmg, pen, skillType, 1);
				_gooduse = 1;
				changeHp(dmg, pen);
			}
			else
			{
				showMessage(_fisher, dmg, pen, skillType, 2);
				_gooduse = 2;
				changeHp(-dmg, pen);
			}
		}
		else if(_deceptiveMode == 0)
		{
			showMessage(_fisher, dmg, pen, skillType, 2);
			_gooduse = 2;
			changeHp(-dmg, pen);
		}
		else
		{
			showMessage(_fisher, dmg, pen, skillType, 3);
			_gooduse = 1;
			changeHp(dmg, pen);
		}
	}
	
	private class FishCombatTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(_fishCurHP >= _fish.getHP() * 2)
			{
				_fisher.sendPacket(Msg.THE_FISH_GOT_AWAY);
				doDie(false);
			}
			else if(_time <= 0)
			{
				_fisher.sendPacket(Msg.TIME_IS_UP_SO_THAT_FISH_GOT_AWAY);
				doDie(false);
			}
			else
			{
				_time--;
				if(_combatMode == 1 && _deceptiveMode == 0 || _combatMode == 0 && _deceptiveMode == 1)
				{
					_fishCurHP = _fishCurHP + _fish.getHpRegen();
				}
				if(_stop == 0)
				{
					_stop = 1;
					if(Rnd.chance(30))
					{
						_combatMode = _combatMode == 0 ? 1 : 0;
					}
					if(_fish.getGroup() == 2 && Rnd.chance(10))
					{
						_deceptiveMode = _deceptiveMode == 0 ? 1 : 0;
					}
				}
				else
				{
					_stop--;
				}
				ExFishingHpRegen efhr = new ExFishingHpRegen(_fisher, _time, _fishCurHP, _combatMode, 0, _anim, 0, _deceptiveMode);
				if(_anim != 0)
				{
					_fisher.broadcastPacket(efhr);
				}
				else
				{
					_fisher.sendPacket(efhr);
				}
			}
		}
	}
	
	protected class LookingForFishTask extends RunnableImpl
	{
		private final long _endTaskTime;
		
		protected LookingForFishTask()
		{
			_endTaskTime = System.currentTimeMillis() + (long) _fish.getWaitTime() + 10000;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(System.currentTimeMillis() >= _endTaskTime)
			{
				_fisher.sendPacket(Msg.BAITS_HAVE_BEEN_LOST_BECAUSE_THE_FISH_GOT_AWAY);
				stopFishingTask();
				endFishing(false);
				return;
			}
			if(!GameTimeController.getInstance().isNowNight() && isNightLure(_lureId))
			{
				_fisher.sendPacket(Msg.BAITS_HAVE_BEEN_LOST_BECAUSE_THE_FISH_GOT_AWAY);
				stopFishingTask();
				endFishing(false);
				return;
			}
			int check = Rnd.get(1000);
			if(_fish.getFishGuts() > check)
			{
				stopFishingTask();
				startFishCombat();
			}
		}
	}
}