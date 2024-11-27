package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _124_MeetingTheElroki extends Quest implements ScriptFile
{
	private static final int marquez = 32113;
	private static final int mushika = 32114;
	private static final int asama = 32115;
	private static final int shaman_caracawe = 32117;
	private static final int egg_of_mantarasa = 32118;
	private static final int q_egg_of_mantarasa = 8778;
	
	public _124_MeetingTheElroki()
	{
		super(false);
		addStartNpc(32113);
		addTalkId(32114, 32115, 32117, 32118);
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
		int GetMemoState = st.getInt("encounter_with_crokian");
		if(event.equals("quest_accept"))
		{
			st.setCond(1);
			st.set("encounter_with_crokian", String.valueOf(0), true);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "marquez_q0124_04.htm";
		}
		if(event.equals("reply_2") && GetMemoState == 0)
		{
			st.setCond(2);
			st.set("encounter_with_crokian", String.valueOf(1), true);
			st.playSound("ItemSound.quest_middle");
			htmltext = "marquez_q0124_06.htm";
		}
		if(event.equals("reply_2a") && GetMemoState == 1)
		{
			st.setCond(3);
			st.set("encounter_with_crokian", String.valueOf(2), true);
			st.playSound("ItemSound.quest_middle");
			htmltext = "mushika_q0124_03.htm";
		}
		if(event.equals("reply_6") && GetMemoState == 2)
		{
			st.setCond(4);
			st.set("encounter_with_crokian", String.valueOf(3), true);
			st.playSound("ItemSound.quest_middle");
			htmltext = "asama_q0124_06.htm";
		}
		if(event.equals("reply_9") && GetMemoState == 3)
		{
			st.setCond(5);
			st.set("encounter_with_crokian", String.valueOf(4), true);
			st.playSound("ItemSound.quest_middle");
			htmltext = "shaman_caracawe_q0124_05.htm";
		}
		if(event.equals("reply_10") && GetMemoState == 4 && st.getQuestItemsCount(8778) < 1)
		{
			st.setCond(6);
			st.giveItems(8778, 1);
			st.playSound("ItemSound.quest_middle");
			htmltext = "egg_of_mantarasa_q0124_02.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("encounter_with_crokian");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 32113)
					break;
				if(st.getPlayer().getLevel() >= 75)
				{
					htmltext = "marquez_q0124_01.htm";
					break;
				}
				htmltext = "marquez_q0124_02.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId == 32113)
				{
					if(GetMemoState == 0)
					{
						htmltext = "marquez_q0124_05.htm";
						break;
					}
					if(GetMemoState == 1)
					{
						htmltext = "marquez_q0124_07.htm";
						break;
					}
					if(GetMemoState < 2 || GetMemoState > 5)
						break;
					htmltext = "marquez_q0124_08.htm";
					break;
				}
				if(npcId == 32114)
				{
					if(GetMemoState == 1)
					{
						htmltext = "mushika_q0124_01.htm";
						break;
					}
					if(GetMemoState < 1)
					{
						htmltext = "mushika_q0124_02.htm";
						break;
					}
					if(GetMemoState < 2)
						break;
					htmltext = "mushika_q0124_04.htm";
					break;
				}
				if(npcId == 32115)
				{
					if(GetMemoState == 2)
					{
						htmltext = "asama_q0124_01.htm";
						break;
					}
					if(GetMemoState < 2)
					{
						htmltext = "asama_q0124_02.htm";
						break;
					}
					if(GetMemoState == 3)
					{
						htmltext = "asama_q0124_07.htm";
						break;
					}
					if(GetMemoState == 4 && st.getQuestItemsCount(8778) >= 1)
					{
						st.giveItems(57, 71318);
						st.takeItems(8778, -1);
						st.playSound("ItemSound.quest_finish");
						st.unset("encounter_with_crokian");
						htmltext = "asama_q0124_08.htm";
						st.exitCurrentQuest(false);
						break;
					}
					if(GetMemoState != 4 || st.getQuestItemsCount(8778) >= 1)
						break;
					htmltext = "asama_q0124_09.htm";
					break;
				}
				if(npcId == 32117)
				{
					if(GetMemoState == 3)
					{
						htmltext = "shaman_caracawe_q0124_01.htm";
						break;
					}
					if(GetMemoState < 3)
					{
						htmltext = "shaman_caracawe_q0124_02.htm";
						break;
					}
					if(GetMemoState == 4 && st.getQuestItemsCount(8778) < 1)
					{
						htmltext = "shaman_caracawe_q0124_06.htm";
						break;
					}
					if(GetMemoState != 4 || st.getQuestItemsCount(8778) < 1)
						break;
					htmltext = "shaman_caracawe_q0124_07.htm";
					break;
				}
				if(npcId != 32118)
					break;
				if(GetMemoState == 4 && st.getQuestItemsCount(8778) < 1)
				{
					htmltext = "egg_of_mantarasa_q0124_01.htm";
					break;
				}
				if(GetMemoState == 4 && st.getQuestItemsCount(8778) >= 1)
				{
					htmltext = "egg_of_mantarasa_q0124_03.htm";
					break;
				}
				if(GetMemoState >= 4)
					break;
				htmltext = "egg_of_mantarasa_q0124_04.htm";
			}
		}
		return htmltext;
	}
}