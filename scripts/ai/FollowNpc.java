package ai;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.model.instances.NpcInstance;

import java.util.concurrent.ScheduledFuture;

public class FollowNpc extends DefaultAI
{
	private boolean _thinking;
	private ScheduledFuture<?> _followTask;
	
	public FollowNpc(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean randomWalk()
	{
		return getActor() instanceof MonsterInstance;
	}
	
	@Override
	protected void onEvtThink()
	{
		NpcInstance actor = getActor();
		if(_thinking || actor.isActionsDisabled() || actor.isAfraid() || actor.isDead() || actor.isMovementDisabled())
		{
			return;
		}
		_thinking = true;
		try
		{
			if(!(Config.BLOCK_ACTIVE_TASKS || getIntention() != CtrlIntention.AI_INTENTION_ACTIVE && getIntention() != CtrlIntention.AI_INTENTION_IDLE))
			{
				thinkActive();
			}
			else if(getIntention() == CtrlIntention.AI_INTENTION_FOLLOW)
			{
				thinkFollow();
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			_thinking = false;
		}
	}
	
	protected void thinkFollow()
	{
		NpcInstance actor = getActor();
		Creature target = actor.getFollowTarget();
		if(target == null || target.isAlikeDead() || actor.getDistance(target) > 4000.0 || actor.isMovementDisabled())
		{
			clientActionFailed();
			return;
		}
		if(actor.isFollowing() && actor.getFollowTarget() == target)
		{
			clientActionFailed();
			return;
		}
		if(actor.isInRange(target, 100))
		{
			clientActionFailed();
		}
		if(_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}
		_followTask = ThreadPoolManager.getInstance().schedule(new ThinkFollow(), 500);
	}
	
	protected class ThinkFollow extends RunnableImpl
	{
		public NpcInstance getActor()
		{
			return FollowNpc.this.getActor();
		}
		
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			if(actor == null)
			{
				return;
			}
			Creature target = actor.getFollowTarget();
			if(target == null || target.isAlikeDead() || actor.getDistance(target) > 4000.0)
			{
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				return;
			}
			if(!(actor.isInRange(target, 150) || actor.isFollowing() && actor.getFollowTarget() == target))
			{
				actor.moveToRelative(target, 100, 150, false);
			}
			_followTask = ThreadPoolManager.getInstance().schedule(this, 500);
		}
	}
}