package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

public class OlMahumGeneral extends Fighter
{
	private boolean _firstTimeAttacked = true;
	
	public OlMahumGeneral(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(_firstTimeAttacked)
		{
			_firstTimeAttacked = false;
			if(Rnd.chance(25))
			{
				Functions.npcSay(actor, "We shall see about that!");
			}
		}
		else if(Rnd.chance(10))
		{
			Functions.npcSay(actor, "I will definitely repay this humiliation!");
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		_firstTimeAttacked = true;
		super.onEvtDead(killer);
	}
}