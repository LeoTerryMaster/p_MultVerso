package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;

public class KashasEye extends DefaultAI
{
	public KashasEye(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
	
	@Override
	protected void onEvtAggression(Creature attacker, int aggro)
	{
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
		NpcInstance actor = getActor();
		if(actor != null && killer != null && actor != killer && Rnd.chance(35))
		{
			actor.setDisplayId(Rnd.get(18812, 18814));
		}
	}
}