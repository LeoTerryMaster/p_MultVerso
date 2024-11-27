package ai;

import l2.gameserver.ai.Guard;
import l2.gameserver.model.instances.NpcInstance;

public class GuardRndWalkAndAnim extends Guard
{
	public GuardRndWalkAndAnim(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean thinkActive()
	{
		if(super.thinkActive())
		{
			return true;
		}
		if(randomAnimation())
		{
			return true;
		}
		return randomWalk();
	}
}