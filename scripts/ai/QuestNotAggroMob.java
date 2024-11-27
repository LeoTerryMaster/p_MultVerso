package ai;

import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.QuestEventType;
import l2.gameserver.model.quest.QuestState;

import java.util.List;

public class QuestNotAggroMob extends DefaultAI
{
	public QuestNotAggroMob(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	public boolean thinkActive()
	{
		return false;
	}
	
	@Override
	public void onEvtAttacked(Creature attacker, int dam)
	{
		List<QuestState> quests;
		NpcInstance actor = getActor();
		Player player = attacker.getPlayer();
		if(player != null && (quests = player.getQuestsForEvent(actor, QuestEventType.ATTACKED_WITH_QUEST)) != null)
		{
			for(QuestState qs : quests)
			{
				qs.getQuest().notifyAttack(actor, qs);
			}
		}
	}
	
	@Override
	public void onEvtAggression(Creature attacker, int d)
	{
	}
}