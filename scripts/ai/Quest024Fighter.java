package ai;

import l2.gameserver.ai.Fighter;
import l2.gameserver.instancemanager.QuestManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import quests._024_InhabitantsOfTheForestOfTheDead;

public class Quest024Fighter extends Fighter
{
	public Quest024Fighter(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean thinkActive()
	{
		Quest q = QuestManager.getQuest(_024_InhabitantsOfTheForestOfTheDead.class);
		if(q != null)
		{
			for(Player player : World.getAroundPlayers(getActor(), 300, 200))
			{
				QuestState questState = player.getQuestState(_024_InhabitantsOfTheForestOfTheDead.class);
				if(questState == null || questState.getCond() != 3)
					continue;
				q.notifyEvent("see_creature", questState, getActor());
			}
		}
		return super.thinkActive();
	}
}