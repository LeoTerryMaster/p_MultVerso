package l2.gameserver.ai;

import l2.commons.lang.reference.HardReference;
import l2.commons.lang.reference.HardReferences;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Summon;
import l2.gameserver.model.World;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.PositionUtils;

import java.util.concurrent.ScheduledFuture;

public class SummonAI extends PlayableAI
{
	private HardReference<Playable> _runAwayTargetRef = HardReferences.emptyRef();
	
	public SummonAI(Summon actor)
	{
		super(actor);
	}
	
	@Override
	protected void thinkActive()
	{
		Summon actor = getActor();
		clearNextAction();
		if(actor.isDepressed())
		{
			setAttackTarget(actor.getPlayer());
			changeIntention(CtrlIntention.AI_INTENTION_ATTACK, actor.getPlayer(), null);
			thinkAttack(true);
		}
		else if(actor.isFollowMode())
		{
			changeIntention(CtrlIntention.AI_INTENTION_FOLLOW, actor.getPlayer(), null);
			thinkFollow();
		}
		super.thinkActive();
	}
	
	@Override
	protected void thinkAttack(boolean checkRange)
	{
		Summon actor = getActor();
		if(actor.isDepressed())
		{
			setAttackTarget(actor.getPlayer());
		}
		super.thinkAttack(checkRange);
	}
	
	private void tryRunAway()
	{
		Summon actor = getActor();
		if(!actor.isDead() && !actor.isDepressed())
		{
			Player owner = actor.getPlayer();
			Playable runAwayTarget = _runAwayTargetRef.get();
			if(owner != null && runAwayTarget != null && !owner.isDead() && !owner.isOutOfControl())
			{
				if(runAwayTarget.isInCombat() && actor.getDistance(runAwayTarget) < (double) actor.getActingRange())
				{
					int radius = getIndentRange(actor.getActingRange());
					Location ownerLoc = owner.getLoc();
					Location targetLoc = runAwayTarget.getLoc();
					double radian = PositionUtils.convertHeadingToRadian((32768 + PositionUtils.getHeadingTo(ownerLoc, targetLoc)) % 65535);
					Location ne = new Location((int) (0.5 + (double) ownerLoc.getX() + (double) radius * Math.sin(radian)), (int) (0.5 + (double) ownerLoc.getY() + (double) radius * Math.cos(radian)), ownerLoc.getZ()).correctGeoZ();
					actor.moveToLocation(ne, 0, true);
					return;
				}
				_runAwayTargetRef = HardReferences.emptyRef();
			}
			else
			{
				_runAwayTargetRef = HardReferences.emptyRef();
			}
		}
	}
	
	@Override
	protected void onEvtArrived()
	{
		if(!setNextIntention())
		{
			if(getIntention() == CtrlIntention.AI_INTENTION_INTERACT || getIntention() == CtrlIntention.AI_INTENTION_PICK_UP || getIntention() == CtrlIntention.AI_INTENTION_FOLLOW)
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
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		Summon actor = getActor();
		if(attacker != null && actor.getPlayer().isDead() && !actor.isDepressed())
		{
			Attack(attacker, false, false);
		}
		if(attacker != null && attacker.isPlayable())
		{
			_runAwayTargetRef = (HardReference<Playable>) attacker.getRef();
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	public Summon getActor()
	{
		return (Summon) super.getActor();
	}
	
	@Override
	protected ScheduledFuture<?> scheduleThinkFollowTask()
	{
		return ThreadPoolManager.getInstance().schedule(new ThinkFollowForSummon(getActor()), Config.MOVE_TASK_QUANTUM_NPC);
	}
	
	protected static class ThinkFollowForSummon extends RunnableImpl
	{
		private final HardReference<? extends Playable> _actorRef;
		
		public ThinkFollowForSummon(Summon actor)
		{
			_actorRef = actor.getRef();
		}
		
		@Override
		public void runImpl() throws Exception
		{
			Summon actor = (Summon) _actorRef.get();
			if(actor == null)
			{
				return;
			}
			SummonAI actorAI = actor.getAI();
			if(actorAI.getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
			{
				if(actorAI.getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
				{
					actor.setFollowMode(false);
				}
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
			boolean incZ = PlayableAI.isThinkImplyZ(actor, target);
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
			if(dist > followRange)
			{
				if(!actor.isFollowing() || actor.getFollowTarget() != target)
				{
					actor.moveToRelative(target, CharacterAI.getIndentRange(followIndent), followRange);
				}
			}
			else
			{
				actorAI.tryRunAway();
			}
			actorAI._followTask = actorAI.scheduleThinkFollowTask();
		}
	}
}