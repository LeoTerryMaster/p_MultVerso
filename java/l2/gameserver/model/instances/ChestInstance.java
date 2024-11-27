package l2.gameserver.model.instances;

import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.templates.npc.NpcTemplate;

public class ChestInstance extends MonsterInstance
{
	public ChestInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	public void tryOpen(Player opener, Skill skill)
	{
		getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, opener, 100);
	}
	
	@Override
	public boolean canChampion()
	{
		return false;
	}
}