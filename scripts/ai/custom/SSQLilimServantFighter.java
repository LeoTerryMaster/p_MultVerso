package ai.custom;

import l2.commons.util.Rnd;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

public class SSQLilimServantFighter extends Fighter
{
	private boolean _attacked;
	
	public SSQLilimServantFighter(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if(Rnd.chance(30) && !_attacked)
		{
			Functions.npcSay(getActor(), Rnd.chance(50) ? "Those who are afraid should get away and those who are brave should fight!" : "This place once belonged to Lord Shilen.");
			_attacked = true;
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		if(Rnd.chance(30))
		{
			Functions.npcSay(getActor(), Rnd.chance(50) ? "Why are you getting in our way?" : "Shilen... our Shilen!");
		}
		super.onEvtDead(killer);
	}
}