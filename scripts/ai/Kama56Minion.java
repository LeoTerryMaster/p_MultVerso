package ai;

import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;

public class Kama56Minion extends Fighter
{
	public Kama56Minion(NpcInstance actor)
	{
		super(actor);
		actor.setIsInvul(true);
	}
	
	@Override
	protected void onEvtAggression(Creature attacker, int aggro)
	{
		if(aggro < 10000000)
		{
			return;
		}
		super.onEvtAggression(attacker, aggro);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
	}
}