package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.Priest;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.MinionInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.utils.Location;

public class HealLeaderMinion extends Priest
{
	public HealLeaderMinion(NpcInstance actor)
	{
		super(actor);
		MAX_PURSUE_RANGE = 10000;
	}
	
	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
		{
			return true;
		}
		if(_def_think)
		{
			if(doTask())
			{
				clearTasks();
			}
			return true;
		}
		Creature desireBuffTarget = getBuffDesireTarget();
		if(desireBuffTarget == null)
		{
			return false;
		}
		if(actor.getDistance(desireBuffTarget) - desireBuffTarget.getColRadius() - actor.getColRadius() > 200.0)
		{
			moveOrTeleportToLocation(Location.findFrontPosition(desireBuffTarget, actor, 100, 150));
			return false;
		}
		if(!desireBuffTarget.isCurrentHpFull() && doTask())
		{
			return createNewTask();
		}
		return false;
	}
	
	private void moveOrTeleportToLocation(Location loc)
	{
		NpcInstance actor = getActor();
		actor.setRunning();
		if(actor.moveToLocation(loc, 0, true))
		{
			return;
		}
		clientStopMoving();
		_pathfindFails = 0;
		actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 600000));
		ThreadPoolManager.getInstance().schedule(new Teleport(loc), 500);
	}
	
	@Override
	protected boolean createNewTask()
	{
		clearTasks();
		NpcInstance actor = getActor();
		Creature desireBuffTarget = getBuffDesireTarget();
		if(actor.isDead() || desireBuffTarget == null)
		{
			return false;
		}
		if(!desireBuffTarget.isCurrentHpFull())
		{
			Skill skill = _healSkills[Rnd.get(_healSkills.length)];
			if((double) skill.getAOECastRange() < actor.getDistance(desireBuffTarget))
			{
				moveOrTeleportToLocation(Location.findFrontPosition(desireBuffTarget, actor, skill.getAOECastRange() - 30, skill.getAOECastRange() - 10));
			}
			addTaskBuff(desireBuffTarget, skill);
			return true;
		}
		return false;
	}
	
	private Creature getBuffDesireTarget()
	{
		NpcInstance actor = getActor();
		if(actor.isMinion())
		{
			return ((MinionInstance) actor).getLeader();
		}
		return null;
	}
	
	@Override
	protected void onIntentionAttack(Creature target)
	{
	}
}