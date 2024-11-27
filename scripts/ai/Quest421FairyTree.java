package ai;

import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.QuestEventType;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.tables.SkillTable;

import java.util.List;

public class Quest421FairyTree extends Fighter
{
	private static final Skill SKILL1 = SkillTable.getInstance().getInfo(4167, 1);
	
	public Quest421FairyTree(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker != null)
		{
			Player player;
			List<QuestState> quests;
			if(attacker.isPlayer())
			{
				SKILL1.getEffects(actor, attacker, false, false);
			}
			else if(attacker.isPet() && (player = attacker.getPlayer()) != null && (quests = player.getQuestsForEvent(actor, QuestEventType.ATTACKED_WITH_QUEST)) != null)
			{
				for(QuestState qs : quests)
				{
					qs.getQuest().notifyAttack(actor, qs);
				}
			}
		}
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}