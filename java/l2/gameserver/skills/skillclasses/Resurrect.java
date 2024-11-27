package l2.gameserver.skills.skillclasses;

import l2.gameserver.cache.Msg;
import l2.gameserver.listener.actor.player.OnAnswerListener;
import l2.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Zone;
import l2.gameserver.model.base.BaseStats;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.instances.PetInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.templates.StatsSet;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class Resurrect extends Skill
{
	private final boolean _canPet;
	
	public Resurrect(StatsSet set)
	{
		super(set);
		_canPet = set.getBool("canPet", false);
	}
	
	private boolean siegeCheck(Player player, Creature target, boolean forceUse)
	{
		boolean result = true;
		for(GlobalEvent e : player.getEvents())
		{
			if(e.canResurrect(player, target, forceUse))
				continue;
			result = false;
		}
		if(result)
		{
			SiegeEvent playerEvent = player.getEvent(SiegeEvent.class);
			SiegeEvent targetEvent = target.getEvent(SiegeEvent.class);
			boolean playerInZone = player.isInZone(Zone.ZoneType.SIEGE);
			boolean targetInZone = target.isInZone(Zone.ZoneType.SIEGE);
			if(playerEvent == null && playerInZone || targetEvent == null && targetInZone)
			{
				result = false;
			}
		}
		if(!result)
		{
			player.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}
		return true;
	}
	
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!activeChar.isPlayer())
		{
			return false;
		}
		if(target == null || target != activeChar && !target.isDead())
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		Player player = (Player) activeChar;
		Player pcTarget = target.getPlayer();
		if(pcTarget == null)
		{
			player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		if(player.isOlyParticipant() || pcTarget.isOlyParticipant())
		{
			player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		if(!siegeCheck(player, target, forceUse))
		{
			return false;
		}
		if(oneTarget())
		{
			if(target.isPet())
			{
				ReviveAnswerListener reviveAsk;
				Pair<Integer, OnAnswerListener> ask = pcTarget.getAskListener(false);
				ReviveAnswerListener reviveAnswerListener = reviveAsk = ask != null && ask.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener) ask.getValue() : null;
				if(reviveAsk != null)
				{
					if(reviveAsk.isForPet())
					{
						activeChar.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED);
					}
					else
					{
						activeChar.sendPacket(Msg.SINCE_THE_MASTER_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_THE_PET_HAS_BEEN_CANCELLED);
					}
					return false;
				}
				if(!_canPet && _targetType != Skill.SkillTargetType.TARGET_PET)
				{
					player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
					return false;
				}
			}
			else if(target.isPlayer())
			{
				ReviveAnswerListener reviveAsk;
				Pair<Integer, OnAnswerListener> ask = pcTarget.getAskListener(false);
				ReviveAnswerListener reviveAnswerListener = reviveAsk = ask != null && ask.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener) ask.getValue() : null;
				if(reviveAsk != null)
				{
					if(reviveAsk.isForPet())
					{
						activeChar.sendPacket(Msg.WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER);
					}
					else
					{
						activeChar.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED);
					}
					return false;
				}
				if(_targetType == Skill.SkillTargetType.TARGET_PET)
				{
					player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
					return false;
				}
				if(pcTarget.isFestivalParticipant())
				{
					player.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Resurrect", player));
					return false;
				}
			}
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		double wit_bonus;
		double percent = _power;
		if(percent < 100.0 && !isHandler() && (percent += (wit_bonus = _power * (BaseStats.WIT.calcBonus(activeChar) - 1.0)) > 20.0 ? 20.0 : wit_bonus) > 90.0)
		{
			percent = 90.0;
		}
		block0:
		for(Creature target : targets)
		{
			if(target == null || target.getPlayer() == null)
				continue;
			for(GlobalEvent e : target.getEvents())
			{
				if(e.canResurrect((Player) activeChar, target, true))
					continue;
				continue block0;
			}
			if(target.isPet() && _canPet)
			{
				if(target.getPlayer() == activeChar)
				{
					((PetInstance) target).doRevive(percent);
				}
				else
				{
					target.getPlayer().reviveRequest((Player) activeChar, percent, true);
				}
			}
			else
			{
				if(!target.isPlayer() || _targetType == Skill.SkillTargetType.TARGET_PET)
					continue;
				Player targetPlayer = (Player) target;
				Pair<Integer, OnAnswerListener> ask = targetPlayer.getAskListener(false);
				ReviveAnswerListener reviveAsk = ask != null && ask.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener) ask.getValue() : null;
				if(reviveAsk != null || targetPlayer.isFestivalParticipant())
					continue;
				targetPlayer.reviveRequest((Player) activeChar, percent, false);
			}
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}