package ai.residences.clanhall;

import l2.commons.collections.CollectionUtils;
import l2.gameserver.Config;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.ai.Fighter;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.model.Creature;
import l2.gameserver.model.World;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.residences.clanhall.CTBBossInstance;
import l2.gameserver.utils.Location;

import java.util.List;

public abstract class MatchFighter extends Fighter
{
	public MatchFighter(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean thinkActive()
	{
		CTBBossInstance actor = getActor();
		if(actor.isActionsDisabled())
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
		long now = System.currentTimeMillis();
		if(now - _checkAggroTimestamp > (long) Config.AGGRO_CHECK_INTERVAL)
		{
			_checkAggroTimestamp = now;
			List<Creature> chars = World.getAroundCharacters(actor);
			CollectionUtils.eqSort(chars, _nearestTargetComparator);
			for(Creature target : chars)
			{
				if(!checkAggression(target))
					continue;
				actor.getAggroList().addDamageHate(target, 0, 2);
				if(target.isSummon())
				{
					actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);
				}
				startRunningTask(AI_TASK_ATTACK_DELAY);
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				return true;
			}
		}
		return randomWalk();
	}
	
	@Override
	protected boolean checkAggression(Creature target)
	{
		CTBBossInstance actor = getActor();
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
		{
			return false;
		}
		if(target.isAlikeDead() || target.isInvul())
		{
			return false;
		}
		if(!actor.isAttackable(target))
		{
			return false;
		}
		if(!GeoEngine.canSeeTarget(actor, target, false))
		{
			return false;
		}
		actor.getAggroList().addDamageHate(target, 0, 2);
		if(target.isSummon() || target.isPet())
		{
			actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);
		}
		startRunningTask(AI_TASK_ATTACK_DELAY);
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		return true;
	}
	
	@Override
	protected boolean canAttackCharacter(Creature target)
	{
		CTBBossInstance actor = getActor();
		return actor.isAttackable(target);
	}
	
	@Override
	public void onEvtSpawn()
	{
		super.onEvtSpawn();
		CTBBossInstance actor = getActor();
		int x = (int) ((double) actor.getX() + 800.0 * Math.cos(actor.headingToRadians(actor.getHeading() - 32768)));
		int y = (int) ((double) actor.getY() + 800.0 * Math.sin(actor.headingToRadians(actor.getHeading() - 32768)));
		actor.setSpawnedLoc(new Location(x, y, actor.getZ()));
		addTaskMove(actor.getSpawnedLoc(), true);
		doTask();
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
	
	@Override
	public CTBBossInstance getActor()
	{
		return (CTBBossInstance) super.getActor();
	}
}