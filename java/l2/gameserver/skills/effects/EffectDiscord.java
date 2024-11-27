package l2.gameserver.skills.effects;

import l2.commons.util.Rnd;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.instances.SummonInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.stats.Env;

import java.util.ArrayList;

public class EffectDiscord extends Effect
{
	public EffectDiscord(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean checkCondition()
	{
		int skilldiff = _effected.getLevel() - _skill.getMagicLevel();
		int lvldiff = _effected.getLevel() - _effector.getLevel();
		if(skilldiff > 10 || skilldiff > 5 && Rnd.chance(30) || Rnd.chance(Math.abs(lvldiff) * 2))
		{
			return false;
		}
		boolean multitargets = _skill.isAoE();
		if(!_effected.isMonster())
		{
			if(!multitargets)
			{
				getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			}
			return false;
		}
		if(_effected.isFearImmune() || _effected.isRaid())
		{
			if(!multitargets)
			{
				getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			}
			return false;
		}
		Player player = _effected.getPlayer();
		if(player != null)
		{
			SiegeEvent siegeEvent = player.getEvent(SiegeEvent.class);
			if(_effected.isSummon() && siegeEvent != null && siegeEvent.containsSiegeSummon((SummonInstance) _effected))
			{
				if(!multitargets)
				{
					getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
				}
				return false;
			}
		}
		if(_effected.isInZonePeace())
		{
			if(!multitargets)
			{
				getEffector().sendPacket(Msg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE);
			}
			return false;
		}
		return super.checkCondition();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startConfused();
		onActionTime();
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		if(!_effected.stopConfused())
		{
			_effected.abortAttack(true, true);
			_effected.abortCast(true, true);
			_effected.stopMove();
			_effected.getAI().setAttackTarget(null);
			_effected.setWalking();
			_effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		ArrayList<Creature> targetList = new ArrayList<>();
		for(Creature character : _effected.getAroundCharacters(900, 200))
		{
			if(!character.isNpc() || character == getEffected())
				continue;
			targetList.add(character);
		}
		if(targetList.isEmpty())
		{
			return true;
		}
		Creature target = targetList.get(Rnd.get(targetList.size()));
		_effected.setRunning();
		_effected.getAI().Attack(target, true, false);
		return false;
	}
}