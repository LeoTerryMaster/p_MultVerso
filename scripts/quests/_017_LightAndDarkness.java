package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _017_LightAndDarkness extends Quest implements ScriptFile
{
	public _017_LightAndDarkness()
	{
		super(false);
		addStartNpc(31517);
		addTalkId(31508);
		addTalkId(31509);
		addTalkId(31510);
		addTalkId(31511);
		addQuestItem(7168);
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
		if(event.equals("dark_presbyter_q0017_04.htm"))
		{
			st.setState(2);
			st.setCond(1);
			st.giveItems(7168, 4);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("blessed_altar1_q0017_02.htm"))
		{
			st.takeItems(7168, 1);
			st.setCond(2);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equals("blessed_altar2_q0017_02.htm"))
		{
			st.takeItems(7168, 1);
			st.setCond(3);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equals("blessed_altar3_q0017_02.htm"))
		{
			st.takeItems(7168, 1);
			st.setCond(4);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equals("blessed_altar4_q0017_02.htm"))
		{
			st.takeItems(7168, 1);
			st.setCond(5);
			st.playSound("ItemSound.quest_middle");
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 31517)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 61)
				{
					htmltext = "dark_presbyter_q0017_01.htm";
				}
				else
				{
					htmltext = "dark_presbyter_q0017_03.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond > 0 && cond < 5 && st.getQuestItemsCount(7168) > 0)
			{
				htmltext = "dark_presbyter_q0017_05.htm";
			}
			else if(cond > 0 && cond < 5 && st.getQuestItemsCount(7168) == 0)
			{
				htmltext = "dark_presbyter_q0017_06.htm";
				st.setCond(0);
				st.exitCurrentQuest(false);
			}
			else if(cond == 5 && st.getQuestItemsCount(7168) == 0)
			{
				htmltext = "dark_presbyter_q0017_07.htm";
				st.addExpAndSp(105527, 0);
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(false);
			}
		}
		else if(npcId == 31508)
		{
			if(cond == 1)
			{
				htmltext = st.getQuestItemsCount(7168) != 0 ? "blessed_altar1_q0017_01.htm" : "blessed_altar1_q0017_03.htm";
			}
			else if(cond == 2)
			{
				htmltext = "blessed_altar1_q0017_05.htm";
			}
		}
		else if(npcId == 31509)
		{
			if(cond == 2)
			{
				htmltext = st.getQuestItemsCount(7168) != 0 ? "blessed_altar2_q0017_01.htm" : "blessed_altar2_q0017_03.htm";
			}
			else if(cond == 3)
			{
				htmltext = "blessed_altar2_q0017_05.htm";
			}
		}
		else if(npcId == 31510)
		{
			if(cond == 3)
			{
				htmltext = st.getQuestItemsCount(7168) != 0 ? "blessed_altar3_q0017_01.htm" : "blessed_altar3_q0017_03.htm";
			}
			else if(cond == 4)
			{
				htmltext = "blessed_altar3_q0017_05.htm";
			}
		}
		else if(npcId == 31511)
		{
			if(cond == 4)
			{
				htmltext = st.getQuestItemsCount(7168) != 0 ? "blessed_altar4_q0017_01.htm" : "blessed_altar4_q0017_03.htm";
			}
			else if(cond == 5)
			{
				htmltext = "blessed_altar4_q0017_05.htm";
			}
		}
		return htmltext;
	}
}