package ai;

import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.ai.Mystic;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Playable;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

public class PaganGuard extends Mystic
{
	public PaganGuard(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}
	
	@Override
	protected boolean canSeeInSilentMove(Playable target)
	{
		if(target.isSilentMoving())
		{
			return getActor().getParameter("canSeeInSilentMove", true);
		}
		return true;
	}
	
	@Override
	public boolean checkAggression(Creature target)
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
		{
			return false;
		}
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE || !isGlobalAggro())
		{
			return false;
		}
		if(target.isAlikeDead() || !target.isPlayable())
		{
			return false;
		}
		if(!target.isInRangeZ(actor.getSpawnedLoc(), (long) actor.getAggroRange()))
		{
			return false;
		}
		if(target.isPlayable() && !canSeeInSilentMove((Playable) target))
		{
			return false;
		}
		if(actor.getNpcId() == 18343 && (Functions.getItemCount((Playable) target, 8067) != 0 || Functions.getItemCount((Playable) target, 8064) != 0))
		{
			return false;
		}
		if(!GeoEngine.canSeeTarget(actor, target, false))
		{
			return false;
		}
		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			actor.getAggroList().addDamageHate(target, 0, 1);
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		return true;
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}