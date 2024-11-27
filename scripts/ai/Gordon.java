package ai;

import ai.moveroute.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;

public class Gordon extends Fighter
{
	public Gordon(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return false;
	}
	
	@Override
	public boolean checkAggression(Creature target)
	{
		if(!target.isPlayable())
		{
			return false;
		}
		if(!target.isCursedWeaponEquipped())
		{
			return false;
		}
		return super.checkAggression(target);
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}