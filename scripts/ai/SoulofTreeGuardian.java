package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ai.Mystic;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

public class SoulofTreeGuardian extends Mystic
{
	private boolean _firstTimeAttacked = true;
	
	public SoulofTreeGuardian(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtSpawn()
	{
		_firstTimeAttacked = true;
		super.onEvtSpawn();
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(_firstTimeAttacked)
		{
			_firstTimeAttacked = false;
			if(Rnd.chance(10))
			{
				Functions.npcSayInRangeCustomMessage(actor, 500, "scripts.ai.SoulofTreeGuardian.WE_MUST_PROTECT", (Object[]) new Object[0]);
			}
		}
		else if(Rnd.chance(10))
		{
			Functions.npcSayInRangeCustomMessage(actor, 500, "scripts.ai.SoulofTreeGuardian.GET_OUT", (Object[]) new Object[0]);
		}
	}
}