package ai;

import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.MagicSkillUse;

public class SealDevice extends Fighter
{
	private boolean _firstAttack;
	
	public SealDevice(NpcInstance actor)
	{
		super(actor);
		actor.block();
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(!_firstAttack)
		{
			actor.broadcastPacket(new MagicSkillUse(actor, actor, 5980, 1, 0, 0));
			_firstAttack = true;
		}
	}
	
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
	}
}