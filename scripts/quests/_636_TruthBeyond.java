package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _636_TruthBeyond extends Quest implements ScriptFile
{
	private static final int priest_eliyah = 31329;
	private static final int falsepriest_flauron = 32010;
	private static final int q_mark_of_heresy = 8067;
	private static final int q_mark_of_sacrifice = 8064;
	private static final int q_faded_mark_of_sac = 8065;
	
	public _636_TruthBeyond()
	{
		super(false);
		addStartNpc(31329);
		addTalkId(32010);
	}
	
	@Override
	public void onLoad()
	{
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int npcId = npc.getNpcId();
		if(npcId == 31329)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.set("truth_behind_the_door", String.valueOf(1), true);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "priest_eliyah_q0636_05.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
			{
				htmltext = "priest_eliyah_q0636_04.htm";
			}
		}
		else if(npcId == 32010 && event.equalsIgnoreCase("reply_1"))
		{
			st.giveItems(8064, 1);
			st.unset("truth_behind_the_door");
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
			htmltext = "falsepriest_flauron_q0636_02.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("truth_behind_the_door");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 31329)
					break;
				if(st.getPlayer().getLevel() >= 73 && st.getQuestItemsCount(8064) == 0 && st.getQuestItemsCount(8065) == 0 && st.getQuestItemsCount(8067) == 0)
				{
					htmltext = "priest_eliyah_q0636_01.htm";
					break;
				}
				if(st.getPlayer().getLevel() >= 73 && (st.getQuestItemsCount(8064) >= 1 || st.getQuestItemsCount(8065) >= 1 || st.getQuestItemsCount(8067) >= 1))
				{
					htmltext = "priest_eliyah_q0636_02.htm";
					st.exitCurrentQuest(true);
					break;
				}
				if(st.getPlayer().getLevel() >= 73)
					break;
				htmltext = "priest_eliyah_q0636_03.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId == 31329)
				{
					if(GetMemoState != 1)
						break;
					htmltext = "priest_eliyah_q0636_06.htm";
					break;
				}
				if(npcId != 32010 || GetMemoState != 1)
					break;
				htmltext = "falsepriest_flauron_q0636_01.htm";
			}
		}
		return htmltext;
	}
}