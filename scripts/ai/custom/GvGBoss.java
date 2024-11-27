package ai.custom;

import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

public class GvGBoss extends Fighter
{
	boolean phrase1;
	boolean phrase2;
	boolean phrase3;
	
	public GvGBoss(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(actor.getCurrentHpPercents() < 50.0 && !phrase1)
		{
			phrase1 = true;
			Functions.npcSay(actor, "Вам не удастся похитить сокровища Геральда!");
		}
		else if(actor.getCurrentHpPercents() < 30.0 && !phrase2)
		{
			phrase2 = true;
			Functions.npcSay(actor, "Я тебе череп проломлю!");
		}
		else if(actor.getCurrentHpPercents() < 5.0 && !phrase3)
		{
			phrase3 = true;
			Functions.npcSay(actor, "Вы все погибнете в страшных муках! Уничтожу!");
		}
		super.onEvtAttacked(attacker, damage);
	}
}