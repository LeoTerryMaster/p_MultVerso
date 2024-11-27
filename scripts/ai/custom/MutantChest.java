package ai.custom;

import l2.commons.util.Rnd;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

public class MutantChest extends Fighter
{
	public MutantChest(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		if(Rnd.chance(30))
		{
			Functions.npcSay(actor, "Враги! Всюду враги! Все сюда, враги здесь!");
		}
		actor.deleteMe();
	}
}