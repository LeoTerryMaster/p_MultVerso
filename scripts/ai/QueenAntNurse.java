package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.ai.Priest;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.MinionInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.utils.Location;
import npc.model.QueenAntInstance;

public class QueenAntNurse extends Priest
{
	public QueenAntNurse(NpcInstance actor)
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
		Creature top_desire_target = getTopDesireTarget();
		if(top_desire_target == null)
		{
			return false;
		}
		if(actor.getDistance(top_desire_target) - top_desire_target.getColRadius() - actor.getColRadius() > 200.0)
		{
			moveOrTeleportToLocation(Location.findFrontPosition(top_desire_target, actor, 100, 150));
			return false;
		}
		if(!top_desire_target.isCurrentHpFull() && doTask())
		{
			return createNewTask();
		}
		return false;
	}
	
	@Override
	protected boolean createNewTask()
	{
		clearTasks();
		NpcInstance actor = getActor();
		Creature top_desire_target = getTopDesireTarget();
		if(actor.isDead() || top_desire_target == null)
		{
			return false;
		}
		if(!top_desire_target.isCurrentHpFull())
		{
			Skill skill = _healSkills[Rnd.get(_healSkills.length)];
			if((double) skill.getAOECastRange() < actor.getDistance(top_desire_target))
			{
				moveOrTeleportToLocation(Location.findFrontPosition(top_desire_target, actor, skill.getAOECastRange() - 30, skill.getAOECastRange() - 10));
			}
			addTaskBuff(top_desire_target, skill);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return true;
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
		ThreadPoolManager.getInstance().schedule(new DefaultAI.Teleport(loc), 500);
	}
	
	private Creature getTopDesireTarget()
	{
		NpcInstance actor = getActor();
		QueenAntInstance queen_ant = (QueenAntInstance) ((MinionInstance) actor).getLeader();
		if(queen_ant == null)
		{
			return null;
		}
		NpcInstance Larva = queen_ant.getLarva();
		if(Larva != null && Larva.getCurrentHpPercents() < 5.0)
		{
			return Larva;
		}
		return queen_ant;
	}
	
	@Override
	protected void onIntentionAttack(Creature target)
	{
	}
	
	@Override
	protected void onEvtClanAttacked(Creature attacked_member, Creature attacker, int damage)
	{
		if(doTask())
		{
			createNewTask();
		}
	}
}