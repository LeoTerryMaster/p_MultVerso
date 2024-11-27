package l2.gameserver.ai;

import l2.commons.lang.reference.HardReference;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Summon;
import l2.gameserver.model.World;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.MyTargetSelected;
import l2.gameserver.utils.Location;

import java.util.concurrent.ScheduledFuture;

public class PlayableAI extends CharacterAI
{
	protected Object _intention_arg0;
	protected Object _intention_arg1;
	protected Skill _skill;
	protected boolean _forceUse;
	protected ScheduledFuture<?> _followTask;
	private volatile int thinking;
	private NextAction _nextAction;
	private Object _nextAction_arg0;
	private Object _nextAction_arg1;
	private boolean _nextAction_arg2;
	private boolean _nextAction_arg3;
	private boolean _dontMove;
	
	public PlayableAI(Playable actor)
	{
		super(actor);
	}
	
	protected static boolean isThinkImplyZ(Playable actor, GameObject target)
	{
		if(actor.isFlying() || actor.isInWater())
		{
			return true;
		}
		if(target != null)
		{
			if(target.isDoor())
			{
				return false;
			}
			Creature creature;
			if(target.isCreature() && ((creature = (Creature) target).isInWater() || creature.isFlying()))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		super.changeIntention(intention, arg0, arg1);
		_intention_arg0 = arg0;
		_intention_arg1 = arg1;
	}
	
	@Override
	public void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_intention_arg0 = null;
		_intention_arg1 = null;
		super.setIntention(intention, arg0, arg1);
	}
	
	@Override
	protected void onIntentionCast(Skill skill, Creature target)
	{
		_skill = skill;
		super.onIntentionCast(skill, target);
	}
	
	public NextAction getNextAction()
	{
		return _nextAction;
	}
	
	public boolean isIntendingInteract(GameObject withTarget)
	{
		return getIntention() == CtrlIntention.AI_INTENTION_INTERACT && _intention_arg0 == withTarget;
	}
	
	@Override
	public void setNextAction(NextAction action, Object arg0, Object arg1, boolean arg2, boolean arg3)
	{
		_nextAction = action;
		_nextAction_arg0 = arg0;
		_nextAction_arg1 = arg1;
		_nextAction_arg2 = arg2;
		_nextAction_arg3 = arg3;
	}
	
	public boolean setNextIntention()
	{
		NextAction nextAction = _nextAction;
		Object nextAction_arg0 = _nextAction_arg0;
		Object nextAction_arg1 = _nextAction_arg1;
		boolean nextAction_arg2 = _nextAction_arg2;
		boolean nextAction_arg3 = _nextAction_arg3;
		Playable actor = getActor();
		if(nextAction == null || actor.isActionsDisabled())
		{
			return false;
		}
		switch(nextAction)
		{
			case ATTACK:
			{
				if(nextAction_arg0 == null)
				{
					return false;
				}
				Creature target = (Creature) nextAction_arg0;
				_forceUse = nextAction_arg2;
				_dontMove = nextAction_arg3;
				clearNextAction();
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				break;
			}
			case CAST:
			{
				if(nextAction_arg0 == null || nextAction_arg1 == null)
				{
					return false;
				}
				Skill skill = (Skill) nextAction_arg0;
				Creature target = (Creature) nextAction_arg1;
				_forceUse = nextAction_arg2;
				_dontMove = nextAction_arg3;
				clearNextAction();
				if(!skill.checkCondition(actor, target, _forceUse, _dontMove, true))
				{
					if(skill.getSkillNextAction() == Skill.SkillNextAction.ATTACK && !actor.equals(target) && !_forceUse)
					{
						setNextAction(NextAction.ATTACK, target, null, false, false);
						return setNextIntention();
					}
					return false;
				}
				setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
				break;
			}
			case MOVE:
			{
				if(nextAction_arg0 == null || nextAction_arg1 == null)
				{
					return false;
				}
				Location loc = (Location) nextAction_arg0;
				Integer offset = (Integer) nextAction_arg1;
				clearNextAction();
				actor.moveToLocation(loc, offset, nextAction_arg2);
				break;
			}
			case REST:
			{
				actor.sitDown(null);
				break;
			}
			case INTERACT:
			{
				if(nextAction_arg0 == null)
				{
					return false;
				}
				GameObject object = (GameObject) nextAction_arg0;
				clearNextAction();
				onIntentionInteract(object);
				break;
			}
			case EQUIP:
			{
				if(nextAction_arg0 == null)
				{
					return false;
				}
				ItemInstance item = (ItemInstance) nextAction_arg0;
				item.getTemplate().getHandler().useItem(getActor(), item, nextAction_arg2);
				clearNextAction();
				break;
			}
			case PICKUP:
			{
				if(nextAction_arg0 == null)
				{
					return false;
				}
				GameObject object = (GameObject) nextAction_arg0;
				clearNextAction();
				onIntentionPickUp(object);
				break;
			}
			default:
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void clearNextAction()
	{
		_nextAction = null;
		_nextAction_arg0 = null;
		_nextAction_arg1 = null;
		_nextAction_arg2 = false;
		_nextAction_arg3 = false;
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
		if(!setNextIntention())
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
	
	@Override
	protected void onEvtReadyToAct()
	{
		if(!setNextIntention())
		{
			onEvtThink();
		}
	}
	
	@Override
	protected void onEvtArrived()
	{
		if(!setNextIntention())
		{
			if(getIntention() == CtrlIntention.AI_INTENTION_INTERACT || getIntention() == CtrlIntention.AI_INTENTION_PICK_UP)
			{
				onEvtThink();
			}
			else
			{
				changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			}
		}
	}
	
	@Override
	protected void onEvtArrivedTarget()
	{
		switch(getIntention())
		{
			case AI_INTENTION_ATTACK:
			{
				thinkAttack(false);
				break;
			}
			case AI_INTENTION_CAST:
			{
				thinkCast(false);
				break;
			}
			case AI_INTENTION_FOLLOW:
			{
				thinkFollow();
				break;
			}
			default:
			{
				onEvtThink();
			}
		}
	}
	
	@Override
	protected final void onEvtThink()
	{
		Playable actor = getActor();
		if(actor.isActionsDisabled())
		{
			return;
		}
		try
		{
			if(thinking++ > 1)
			{
				return;
			}
			switch(getIntention())
			{
				case AI_INTENTION_ACTIVE:
				{
					thinkActive();
					return;
				}
				case AI_INTENTION_ATTACK:
				{
					thinkAttack(true);
					return;
				}
				case AI_INTENTION_CAST:
				{
					thinkCast(true);
					return;
				}
				case AI_INTENTION_PICK_UP:
				{
					thinkPickUp();
					return;
				}
				case AI_INTENTION_INTERACT:
				{
					thinkInteract();
					return;
				}
				case AI_INTENTION_FOLLOW:
				{
					thinkFollow();
				}
			}
			return;
		}
		catch(Exception e)
		{
			_log.error("", e);
			return;
		}
		finally
		{
			--thinking;
		}
	}
	
	protected void thinkActive()
	{
	}
	
	protected void thinkAttack(boolean checkRange)
	{
		Playable actor = getActor();
		Player player = actor.getPlayer();
		if(player == null)
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
		else if(!actor.isActionsDisabled() && !actor.isAttackingDisabled())
		{
			boolean isPosessed = actor instanceof Summon && ((Summon) actor).isDepressed();
			Creature attack_target = getAttackTarget();
			if(attack_target != null && !attack_target.isDead())
			{
				block:
				{
					if(!isPosessed)
					{
						if(_forceUse)
						{
							if(!attack_target.isAttackable(actor))
							{
								break block;
							}
						}
						else if(!attack_target.isAutoAttackable(actor))
						{
							break block;
						}
					}
					
					if(!checkRange)
					{
						clientStopMoving();
						actor.doAttack(attack_target);
						return;
					}
					
					int clientClipRange = player.getNetConnection() != null ? player.getNetConnection().getPawnClippingRange() : GameClient.DEFAULT_PAWN_CLIPPING_RANGE;
					int collisions = (int) (actor.getColRadius() + attack_target.getColRadius());
					boolean incZ = isThinkImplyZ(actor, attack_target);
					int dist = (int) (!incZ ? actor.getDistance(attack_target) : actor.getDistance3D(attack_target)) - collisions;
					boolean useActAsAtkRange = attack_target.isDoor();
					int atkRange = !useActAsAtkRange ? actor.getPhysicalAttackRange() : attack_target.getActingRange();
					boolean canSee = dist < clientClipRange && GeoEngine.canSeeTarget(actor, attack_target, incZ);
					if(canSee || atkRange <= 256 && Math.abs(actor.getZ() - attack_target.getZ()) <= 256)
					{
						if(dist <= atkRange)
						{
							if(!canSee)
							{
								actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
								setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
								actor.sendActionFailed();
								return;
							}
							
							clientStopMoving(false);
							actor.doAttack(attack_target);
						}
						else if(!_dontMove)
						{
							int moveIndent = getIndentRange(atkRange) + (!useActAsAtkRange ? collisions : 0);
							int moveRange = Math.max(moveIndent, atkRange + (!useActAsAtkRange ? collisions : 0));
							ThreadPoolManager.getInstance().execute(new RunnableImpl()
							{
								@Override
								public void runImpl() throws Exception
								{
									actor.moveToRelative(attack_target, moveIndent, moveRange);
								}
							});
						}
						else
						{
							actor.sendActionFailed();
						}
						
						return;
					}
					
					actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
					setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					actor.sendActionFailed();
					return;
				}
			}
			
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
		}
		else
		{
			actor.sendActionFailed();
		}
	}
	
	protected void thinkCast(boolean checkRange)
	{
		Playable actor = getActor();
		Creature target = getAttackTarget();
		if(_skill.getSkillType() == Skill.SkillType.CRAFT || _skill.isToggle())
		{
			if(_skill.checkCondition(actor, target, _forceUse, _dontMove, true))
			{
				actor.doCast(_skill, target, _forceUse);
			}
			return;
		}
		if(target == null || target.isDead() != _skill.getCorpse() && !_skill.isNotTargetAoE())
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return;
		}
		if(!checkRange)
		{
			if(_skill.getSkillNextAction() == Skill.SkillNextAction.ATTACK && !actor.equals(target) && !_forceUse)
			{
				setNextAction(NextAction.ATTACK, target, null, false, false);
			}
			else
			{
				clearNextAction();
			}
			clientStopMoving();
			if(_skill.checkCondition(actor, target, _forceUse, _dontMove, true))
			{
				actor.doCast(_skill, target, _forceUse);
			}
			else
			{
				setNextIntention();
				if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				{
					thinkAttack(true);
				}
			}
			return;
		}
		int collisions = (int) (actor.getColRadius() + target.getColRadius());
		boolean incZ = isThinkImplyZ(actor, target);
		int dist = (int) (!incZ ? actor.getDistance(target) : actor.getDistance3D(target)) - collisions;
		boolean useActAsCastRange = target.isDoor();
		int castRange = !useActAsCastRange ? Math.max(16, actor.getMagicalAttackRange(_skill)) : target.getActingRange();
		boolean canSee;
		switch(_skill.getSkillType())
		{
			case TAKECASTLE:
			{
				canSee = true;
				break;
			}
			default:
			{
				canSee = GeoEngine.canSeeTarget(actor, target, actor.isFlying());
			}
		}
		boolean noRangeSkill;
		boolean bl = noRangeSkill = _skill.getCastRange() == 32767;
		if(!(noRangeSkill || canSee || castRange <= 256 && Math.abs(actor.getZ() - target.getZ()) <= 256))
		{
			actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return;
		}
		if(noRangeSkill || dist <= castRange)
		{
			if(!noRangeSkill && !canSee)
			{
				actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				actor.sendActionFailed();
				return;
			}
			if(_skill.getSkillNextAction() == Skill.SkillNextAction.ATTACK && !actor.equals(target) && !_forceUse)
			{
				setNextAction(NextAction.ATTACK, target, null, false, false);
			}
			else
			{
				clearNextAction();
			}
			if(_skill.checkCondition(actor, target, _forceUse, _dontMove, true))
			{
				clientStopMoving(false);
				actor.doCast(_skill, target, _forceUse);
			}
			else
			{
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				actor.sendActionFailed();
			}
		}
		else if(!_dontMove)
		{
			int moveIndent = getIndentRange(castRange) + (!useActAsCastRange ? collisions : 0);
			int moveRange = Math.max(moveIndent, castRange + (!useActAsCastRange ? collisions : 0));
			ThreadPoolManager.getInstance().execute(new RunnableImpl()
			{
				
				@Override
				public void runImpl() throws Exception
				{
					actor.moveToRelative(target, moveIndent, moveRange);
				}
			});
		}
		else
		{
			actor.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
		}
	}
	
	protected void thinkPickUp()
	{
		Playable actor = getActor();
		GameObject target = (GameObject) _intention_arg0;
		if(target == null)
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			return;
		}
		if(actor.isInRange(target, (long) (target.getActingRange() + 16)) && Math.abs(actor.getZ() - target.getZ()) < 64)
		{
			if(actor.isPlayer() || actor.isPet())
			{
				actor.doPickupItem(target);
			}
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
		else
		{
			Location moveToLoc = new Location(target.getX() & -8, target.getY() & -8, target.getZ());
			ThreadPoolManager.getInstance().execute(new RunnableImpl()
			{
				
				@Override
				public void runImpl()
				{
					actor.moveToLocation(moveToLoc, 0, true);
					setNextAction(NextAction.PICKUP, target, null, false, false);
				}
			});
		}
	}
	
	protected void thinkInteract()
	{
		Playable actor = getActor();
		GameObject target = (GameObject) _intention_arg0;
		if(target == null)
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			return;
		}
		boolean incZ = isThinkImplyZ(actor, target);
		int dist = (int) (!incZ ? actor.getDistance(target) : actor.getDistance3D(target));
		int actRange;
		if(dist <= (actRange = target.getActingRange()))
		{
			if(actor.isPlayer())
			{
				((Player) actor).doInteract(target);
			}
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
		else
		{
			int moveIndent = getIndentRange(actRange);
			int moveRange = actRange;
			ThreadPoolManager.getInstance().execute(new RunnableImpl()
			{
				
				@Override
				public void runImpl() throws Exception
				{
					actor.moveToRelative(target, moveIndent, moveRange);
				}
			});
			setNextAction(NextAction.INTERACT, target, null, false, false);
		}
	}
	
	protected void thinkFollow()
	{
		Playable actor = getActor();
		Creature target = (Creature) _intention_arg0;
		if(target == null || target.isAlikeDead())
		{
			clientActionFailed();
			return;
		}
		if(actor.isFollowing() && actor.getFollowTarget() == target)
		{
			clientActionFailed();
			return;
		}
		int clientClipRange = actor.getPlayer() != null && actor.getPlayer().getNetConnection() != null ? actor.getPlayer().getNetConnection().getPawnClippingRange() : GameClient.DEFAULT_PAWN_CLIPPING_RANGE;
		int collisions = (int) (actor.getColRadius() + target.getColRadius());
		boolean incZ = isThinkImplyZ(actor, target);
		int dist = (int) (!incZ ? actor.getDistance(target) : actor.getDistance3D(target)) - collisions;
		int followRange = actor.getActingRange() + target.getActingRange();
		if(dist > clientClipRange)
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			clientActionFailed();
			clientStopMoving();
			return;
		}
		if(dist <= followRange + 16 || actor.isMovementDisabled())
		{
			clientActionFailed();
		}
		if(_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}
		_followTask = scheduleThinkFollowTask();
	}
	
	protected ScheduledFuture<?> scheduleThinkFollowTask()
	{
		return ThreadPoolManager.getInstance().schedule(new ThinkFollow(getActor()), Config.MOVE_TASK_QUANTUM_PC);
	}
	
	@Override
	protected void onIntentionInteract(GameObject target)
	{
		Playable actor = getActor();
		if(actor.isActionsDisabled())
		{
			setNextAction(NextAction.INTERACT, target, null, false, false);
			clientActionFailed();
			return;
		}
		clearNextAction();
		changeIntention(CtrlIntention.AI_INTENTION_INTERACT, target, null);
		onEvtThink();
	}
	
	@Override
	protected void onIntentionPickUp(GameObject object)
	{
		Playable actor = getActor();
		if(actor.isActionsDisabled())
		{
			setNextAction(NextAction.PICKUP, object, null, false, false);
			clientActionFailed();
			return;
		}
		clearNextAction();
		changeIntention(CtrlIntention.AI_INTENTION_PICK_UP, object, null);
		onEvtThink();
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		clearNextAction();
		super.onEvtDead(killer);
	}
	
	@Override
	protected void onEvtFakeDeath()
	{
		clearNextAction();
		super.onEvtFakeDeath();
	}
	
	public void lockTarget(Creature target)
	{
		Playable actor = getActor();
		if(target == null || target.isDead())
		{
			actor.setAggressionTarget(null);
		}
		else if(actor.getAggressionTarget() == null)
		{
			GameObject actorStoredTarget = actor.getTarget();
			actor.setAggressionTarget(target);
			actor.setTarget(target);
			clearNextAction();
			if(actorStoredTarget != target)
			{
				actor.sendPacket(new MyTargetSelected(target.getObjectId(), 0));
			}
		}
	}
	
	@Override
	public void Attack(GameObject target, boolean forceUse, boolean dontMove)
	{
		Playable actor = getActor();
		if(target.isCreature() && (actor.isActionsDisabled() || actor.isAttackingDisabled()))
		{
			setNextAction(NextAction.ATTACK, target, null, forceUse, false);
			actor.sendActionFailed();
			return;
		}
		_dontMove = dontMove;
		_forceUse = forceUse;
		clearNextAction();
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
	}
	
	@Override
	public void Cast(Skill skill, Creature target, boolean forceUse, boolean dontMove)
	{
		Playable actor = getActor();
		if(skill.altUse() || skill.isToggle())
		{
			if((skill.isToggle() || skill.isHandler()) && (actor.isOutOfControl() || actor.isStunned() || actor.isSleeping() || actor.isParalyzed() || actor.isAlikeDead()))
			{
				clientActionFailed();
			}
			else
			{
				actor.altUseSkill(skill, target);
			}
			return;
		}
		if(actor.isActionsDisabled())
		{
			setNextAction(NextAction.CAST, skill, target, forceUse, dontMove);
			clientActionFailed();
			return;
		}
		_forceUse = forceUse;
		_dontMove = dontMove;
		clearNextAction();
		setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}
	
	@Override
	public Playable getActor()
	{
		return (Playable) super.getActor();
	}
	
	@Override
	protected void onEvtForgetObject(GameObject object)
	{
		super.onEvtForgetObject(object);
		if(getIntention() == CtrlIntention.AI_INTENTION_FOLLOW && _intention_arg0 == object)
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
	
	protected static class ThinkFollow extends RunnableImpl
	{
		private final HardReference<? extends Playable> _actorRef;
		
		public ThinkFollow(Playable actor)
		{
			_actorRef = actor.getRef();
		}
		
		@Override
		public void runImpl() throws Exception
		{
			Playable actor = _actorRef.get();
			if(actor == null)
			{
				return;
			}
			PlayableAI actorAI = (PlayableAI) actor.getAI();
			if(actorAI.getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
			{
				return;
			}
			Creature target = (Creature) actorAI._intention_arg0;
			if(target == null || target.isAlikeDead())
			{
				actorAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				return;
			}
			int clientClipRange = actor.getPlayer() != null && actor.getPlayer().getNetConnection() != null ? actor.getPlayer().getNetConnection().getPawnClippingRange() : GameClient.DEFAULT_PAWN_CLIPPING_RANGE;
			int collisions = (int) (actor.getColRadius() + target.getColRadius());
			boolean incZ = isThinkImplyZ(actor, target);
			int dist = (int) (!incZ ? actor.getDistance(target) : actor.getDistance3D(target)) - collisions;
			int followIndent = Math.min(clientClipRange, target.getActingRange());
			int followRange = actor.getActingRange();
			if(dist > clientClipRange || dist > 2 << World.SHIFT_BY)
			{
				actorAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				actorAI.clientStopMoving();
				return;
			}
			Player player = actor.getPlayer();
			if(player == null || player.isLogoutStarted() || (actor.isPet() || actor.isSummon()) && player.getPet() != actor)
			{
				actorAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				actorAI.clientStopMoving();
				return;
			}
			if(!(dist <= followRange || actor.isFollowing() && actor.getFollowTarget() == target))
			{
				actor.moveToRelative(target, followIndent + collisions, followRange + collisions);
			}
			actorAI._followTask = actorAI.scheduleThinkFollowTask();
		}
	}
}