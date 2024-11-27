package l2.gameserver.ai;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.items.attachment.FlagItemAttachment;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ActionFail;

public class PlayerAI extends PlayableAI
{
	public PlayerAI(Player actor)
	{
		super(actor);
	}
	
	@Override
	protected void onIntentionRest()
	{
		changeIntention(CtrlIntention.AI_INTENTION_REST, null, null);
		setAttackTarget(null);
		clientStopMoving();
	}
	
	@Override
	protected void onIntentionActive()
	{
		clearNextAction();
		changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
	}
	
	@Override
	public void onIntentionInteract(GameObject object)
	{
		Player actor = getActor();
		if(actor.getSittingTask())
		{
			setNextAction(NextAction.INTERACT, object, null, false, false);
			return;
		}
		if(actor.isSitting())
		{
			actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
			clientActionFailed();
			return;
		}
		super.onIntentionInteract(object);
	}
	
	@Override
	public void onIntentionPickUp(GameObject object)
	{
		Player actor = getActor();
		if(actor.getSittingTask())
		{
			setNextAction(NextAction.PICKUP, object, null, false, false);
			return;
		}
		if(actor.isSitting())
		{
			actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
			clientActionFailed();
			return;
		}
		super.onIntentionPickUp(object);
	}
	
	@Override
	protected void thinkAttack(boolean checkRange)
	{
		Player actor = getActor();
		if(actor.isInFlyingTransform())
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			return;
		}
		FlagItemAttachment attachment = actor.getActiveWeaponFlagAttachment();
		if(attachment != null && !attachment.canAttack(actor))
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return;
		}
		if(actor.isFrozen())
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFail.STATIC);
			return;
		}
		super.thinkAttack(checkRange);
	}
	
	@Override
	protected void thinkCast(boolean checkRange)
	{
		Player actor = getActor();
		FlagItemAttachment attachment = actor.getActiveWeaponFlagAttachment();
		if(attachment != null && !attachment.canCast(actor, _skill))
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return;
		}
		if(actor.isFrozen())
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFail.STATIC);
			return;
		}
		super.thinkCast(checkRange);
	}
	
	@Override
	public void Attack(GameObject target, boolean forceUse, boolean dontMove)
	{
		Player actor = getActor();
		if(actor.isInFlyingTransform())
		{
			actor.sendActionFailed();
			return;
		}
		if(System.currentTimeMillis() - actor.getLastAttackPacket() < (long) Config.ATTACK_PACKET_DELAY)
		{
			actor.sendActionFailed();
			return;
		}
		actor.setLastAttackPacket();
		if(actor.getSittingTask())
		{
			setNextAction(NextAction.ATTACK, target, null, forceUse, false);
			return;
		}
		if(actor.isSitting())
		{
			actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
			clientActionFailed();
			return;
		}
		super.Attack(target, forceUse, dontMove);
	}
	
	@Override
	public void Cast(Skill skill, Creature target, boolean forceUse, boolean dontMove)
	{
		Player actor = getActor();
		if(!(skill.altUse() || skill.isToggle() || skill.getSkillType() == Skill.SkillType.CRAFT && Config.ALLOW_TALK_WHILE_SITTING))
		{
			if(actor.getSittingTask())
			{
				setNextAction(NextAction.CAST, skill, target, forceUse, dontMove);
				clientActionFailed();
				return;
			}
			if(skill.getSkillType() == Skill.SkillType.SUMMON && actor.getPrivateStoreType() != 0)
			{
				actor.sendPacket(Msg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS);
				clientActionFailed();
				return;
			}
			if(actor.isSitting())
			{
				if(skill.getSkillType() == Skill.SkillType.TRANSFORMATION)
				{
					actor.sendPacket(Msg.YOU_CANNOT_TRANSFORM_WHILE_SITTING);
				}
				else
				{
					actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
				}
				clientActionFailed();
				return;
			}
		}
		super.Cast(skill, target, forceUse, dontMove);
	}
	
	@Override
	public Player getActor()
	{
		return (Player) super.getActor();
	}
}