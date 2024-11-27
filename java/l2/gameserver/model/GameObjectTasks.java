package l2.gameserver.model;

import l2.commons.lang.reference.HardReference;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.entity.oly.HeroController;
import l2.gameserver.network.l2.s2c.MagicSkillLaunched;
import l2.gameserver.network.l2.s2c.SystemMessage;

import java.util.List;

public class GameObjectTasks
{
	
	public static class NotifyAITask extends RunnableImpl
	{
		private final CtrlEvent _evt;
		private final Object _agr0;
		private final Object _agr1;
		private final HardReference<? extends Creature> _charRef;
		
		public NotifyAITask(Creature cha, CtrlEvent evt, Object agr0, Object agr1)
		{
			_charRef = cha.getRef();
			_evt = evt;
			_agr0 = agr0;
			_agr1 = agr1;
		}
		
		public NotifyAITask(Creature cha, CtrlEvent evt, Object arg0)
		{
			this(cha, evt, arg0, null);
		}
		
		public NotifyAITask(Creature cha, CtrlEvent evt)
		{
			this(cha, evt, null, null);
		}
		
		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if(character == null || !character.hasAI())
			{
				return;
			}
			character.getAI().notifyEvent(_evt, _agr0, _agr1);
		}
	}
	
	public static class MagicLaunchedTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _charRef;
		public boolean _forceUse;
		
		public MagicLaunchedTask(Creature cha, boolean forceUse)
		{
			_charRef = cha.getRef();
			_forceUse = forceUse;
		}
		
		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if(character == null)
			{
				return;
			}
			Skill castingSkill = character.getCastingSkill();
			Creature castingTarget = character.getCastingTarget();
			if(castingSkill == null || castingTarget == null)
			{
				character.clearCastVars();
				return;
			}
			if(!castingSkill.checkCondition(character, castingTarget, _forceUse, false, false))
			{
				character.abortCast(true, false);
				return;
			}
			List<Creature> targets = castingSkill.getTargets(character, castingTarget, _forceUse);
			character.broadcastPacket(new MagicSkillLaunched(character, castingSkill, targets));
		}
	}
	
	public static class MagicUseTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _charRef;
		public boolean _forceUse;
		
		public MagicUseTask(Creature cha, boolean forceUse)
		{
			_charRef = cha.getRef();
			_forceUse = forceUse;
		}
		
		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if(character == null)
			{
				return;
			}
			Skill castingSkill = character.getCastingSkill();
			Creature castingTarget = character.getCastingTarget();
			if(castingSkill == null || castingTarget == null)
			{
				character.clearCastVars();
				return;
			}
			character.onMagicUseTimer(castingTarget, castingSkill, _forceUse);
		}
	}
	
	public static class ActReadyTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _charRef;
		
		public ActReadyTask(Creature cha)
		{
			_charRef = cha.getRef();
		}
		
		@Override
		public void runImpl() throws Exception
		{
			Creature character = _charRef.get();
			if(character == null)
			{
				return;
			}
			character.getAI().notifyEvent(CtrlEvent.EVT_READY_TO_ACT);
		}
	}
	
	public static class HitTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _charRef;
		private final HardReference<? extends Creature> _targetRef;
		boolean _crit;
		boolean _miss;
		boolean _shld;
		boolean _soulshot;
		boolean _unchargeSS;
		boolean _notify;
		int _damage;
		long _actDelay;
		
		public HitTask(Creature cha, Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS, boolean notify)
		{
			_charRef = cha.getRef();
			_targetRef = target.getRef();
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
			_unchargeSS = unchargeSS;
			_notify = notify;
			_actDelay = 0;
		}
		
		public HitTask(Creature cha, Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS, boolean notify, long actDelay)
		{
			_charRef = cha.getRef();
			_targetRef = target.getRef();
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
			_unchargeSS = unchargeSS;
			_notify = notify;
			_actDelay = actDelay;
		}
		
		@Override
		public void runImpl()
		{
			Creature target;
			Creature character = _charRef.get();
			if(character == null || (target = _targetRef.get()) == null)
			{
				return;
			}
			if(character.isAttackAborted())
			{
				return;
			}
			character.onHitTimer(target, _damage, _crit, _miss, _soulshot, _shld, _unchargeSS);
			if(_notify)
			{
				if(_actDelay > 0)
				{
					ThreadPoolManager.getInstance().schedule(new ActReadyTask(character), _actDelay);
				}
				else
				{
					character.getAI().notifyEvent(CtrlEvent.EVT_READY_TO_ACT);
				}
			}
		}
	}
	
	public static class CastEndTimeTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _charRef;
		
		public CastEndTimeTask(Creature character)
		{
			_charRef = character.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if(character == null)
			{
				return;
			}
			character.onCastEndTime();
		}
	}
	
	public static class AltMagicUseTask extends RunnableImpl
	{
		public final Skill _skill;
		private final HardReference<? extends Creature> _charRef;
		private final HardReference<? extends Creature> _targetRef;
		
		public AltMagicUseTask(Creature character, Creature target, Skill skill)
		{
			_charRef = character.getRef();
			_targetRef = target.getRef();
			_skill = skill;
		}
		
		@Override
		public void runImpl()
		{
			Creature target;
			Creature cha = _charRef.get();
			if(cha == null || (target = _targetRef.get()) == null)
			{
				return;
			}
			cha.altOnMagicUseTimer(target, _skill);
		}
	}
	
	public static class EndCustomHeroTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public EndCustomHeroTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
			{
				return;
			}
			if(player.getVar("CustomHeroEndTime") == null || HeroController.getInstance().isCurrentHero(player))
			{
				return;
			}
			player.setHero(false);
			HeroController.removeSkills(player);
			player.broadcastUserInfo(true);
		}
	}
	
	public static class EndStandUpTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public EndStandUpTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
			{
				return;
			}
			player.sittingTaskLaunched = false;
			player.setSitting(false);
			if(!player.getAI().setNextIntention())
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			}
		}
	}
	
	public static class EndSitDownTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public EndSitDownTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
			{
				return;
			}
			player.sittingTaskLaunched = false;
			player.getAI().clearNextAction();
		}
	}
	
	public static class UnJailTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public UnJailTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
			{
				return;
			}
			player.unblock();
			player.standUp();
			player.teleToLocation(17817, 170079, -3530, ReflectionManager.DEFAULT);
		}
	}
	
	public static class KickTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public KickTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
			{
				return;
			}
			player.setOfflineMode(false);
			player.kick();
		}
	}
	
	public static class WaterTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public WaterTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
			{
				return;
			}
			if(player.isDead() || !player.isInWater())
			{
				player.stopWaterTask();
				return;
			}
			double reduceHp = player.getMaxHp() < 100 ? 1.0 : (double) (player.getMaxHp() / 100);
			player.reduceCurrentHp(reduceHp, player, null, false, false, true, false, false, false, false);
			player.sendPacket(new SystemMessage(297).addNumber((long) reduceHp));
		}
	}
	
	public static class HourlyTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public HourlyTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
			{
				return;
			}
			int hoursInGame = player.getHoursInGame();
			player.sendPacket(new SystemMessage(764).addNumber(hoursInGame));
		}
	}
	
	public static class PvPFlagTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public PvPFlagTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
			{
				return;
			}
			long diff = Math.abs(System.currentTimeMillis() - player.getlastPvpAttack());
			if(diff > (long) (Config.PVP_TIME + Config.PVP_BLINKING_UNFLAG_TIME))
			{
				player.stopPvPFlag();
			}
			else if(diff > (long) Config.PVP_TIME)
			{
				player.updatePvPFlag(2);
			}
			else
			{
				player.updatePvPFlag(1);
			}
		}
	}
	
	public static class SoulConsumeTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;
		
		public SoulConsumeTask(Player player)
		{
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
			{
				return;
			}
			player.setConsumedSouls(player.getConsumedSouls() + 1, null);
		}
	}
	
	public static class DeleteTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _ref;
		
		public DeleteTask(Creature c)
		{
			_ref = c.getRef();
		}
		
		@Override
		public void runImpl()
		{
			Creature c = _ref.get();
			if(c != null)
			{
				c.deleteMe();
			}
		}
	}
}