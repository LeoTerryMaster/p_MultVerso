package ai.moveroute;

import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;

public class NotAggressiveNpc extends MoveRouteDefaultAI
{
	public NotAggressiveNpc(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
	}
	
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
	}
}