package ai;

import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.QuestEventType;
import l2.gameserver.model.quest.QuestState;

import java.util.List;

public class AttackMobNotPlayerFighter extends Fighter
{
	public AttackMobNotPlayerFighter(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker == null)
		{
			return;
		}
		Player player = attacker.getPlayer();
		List<QuestState> quests = player.getQuestsForEvent(actor, QuestEventType.ATTACKED_WITH_QUEST);
		if(player != null && quests != null)
		{
			for(QuestState qs : quests)
			{
				qs.getQuest().notifyAttack(actor, qs);
			}
		}
		onEvtAggression(attacker, damage);
	}
	
	@Override
	protected void onEvtAggression(Creature attacker, int aggro)
	{
		NpcInstance actor = getActor();
		if(attacker == null)
		{
			return;
		}
		if(!actor.isRunning())
		{
			startRunningTask(AI_TASK_ATTACK_DELAY);
		}
		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
	}
}