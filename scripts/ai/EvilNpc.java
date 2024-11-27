package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;
import l2.gameserver.tables.SkillTable;

public class EvilNpc extends DefaultAI
{
	private static final String[] _txt = {"отстань!", "уймись!", "я тебе отомщу, потом будешь прощения просить!", "у тебя будут неприятности!", "я на тебя пожалуюсь, тебя арестуют!"};
	private long _lastAction;
	
	public EvilNpc(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker == null || attacker.getPlayer() == null)
		{
			return;
		}
		if(System.currentTimeMillis() - _lastAction > 3000)
		{
			int chance = Rnd.get(0, 100);
			if(chance < 2)
			{
				attacker.getPlayer().setKarma(attacker.getPlayer().getKarma() + 5);
			}
			else if(chance < 4)
			{
				actor.doCast(SkillTable.getInstance().getInfo(4578, 1), attacker, true);
			}
			else
			{
				actor.doCast(SkillTable.getInstance().getInfo(4185, 7), attacker, true);
			}
			Functions.npcSay(actor, attacker.getName() + ", " + _txt[Rnd.get(_txt.length)]);
			_lastAction = System.currentTimeMillis();
		}
	}
}