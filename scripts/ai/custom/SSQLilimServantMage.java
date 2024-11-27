package ai.custom;

import l2.commons.util.Rnd;
import l2.gameserver.ai.Mystic;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

public class SSQLilimServantMage extends Mystic
{
	private boolean _attacked;
	
	public SSQLilimServantMage(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		super.onEvtAttacked(attacker, damage);
		if(Rnd.chance(30) && !_attacked)
		{
			Functions.npcSay(getActor(), "Who dares enter this place?");
			_attacked = true;
		}
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		if(Rnd.chance(30))
		{
			Functions.npcSay(getActor(), "Lord Shilen... some day... you will accomplish... this mission...");
		}
		super.onEvtDead(killer);
	}
}