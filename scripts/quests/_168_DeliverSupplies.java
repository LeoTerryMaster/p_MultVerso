package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _168_DeliverSupplies extends Quest implements ScriptFile
{
	int JENNIES_LETTER_ID = 1153;
	int SENTRY_BLADE1_ID = 1154;
	int SENTRY_BLADE2_ID = 1155;
	int SENTRY_BLADE3_ID = 1156;
	int OLD_BRONZE_SWORD_ID = 1157;
	
	public _168_DeliverSupplies()
	{
		super(false);
		addStartNpc(30349);
		addTalkId(30349);
		addTalkId(30355);
		addTalkId(30357);
		addTalkId(30360);
		addQuestItem(SENTRY_BLADE1_ID, OLD_BRONZE_SWORD_ID, JENNIES_LETTER_ID, SENTRY_BLADE2_ID, SENTRY_BLADE3_ID);
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
		if(event.equals("1"))
		{
			st.set("id", "0");
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "30349-03.htm";
			st.giveItems(JENNIES_LETTER_ID, 1);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30349 && cond == 0)
		{
			if(0 < 15)
			{
				if(st.getPlayer().getRace() != Race.darkelf)
				{
					htmltext = "30349-00.htm";
				}
				else if(st.getPlayer().getLevel() >= 3)
				{
					htmltext = "30349-02.htm";
				}
				else
				{
					htmltext = "30349-01.htm";
					st.exitCurrentQuest(true);
				}
			}
			else
			{
				htmltext = "30349-01.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(npcId == 30349 && cond == 1 && st.getQuestItemsCount(JENNIES_LETTER_ID) > 0)
		{
			htmltext = "30349-04.htm";
		}
		else if(npcId == 30349 && cond == 2 && st.getQuestItemsCount(SENTRY_BLADE1_ID) == 1 && st.getQuestItemsCount(SENTRY_BLADE2_ID) == 1 && st.getQuestItemsCount(SENTRY_BLADE3_ID) == 1)
		{
			htmltext = "30349-05.htm";
			st.takeItems(SENTRY_BLADE1_ID, 1);
			st.setCond(3);
		}
		else if(npcId == 30349 && cond == 3 && st.getQuestItemsCount(SENTRY_BLADE1_ID) == 0 && (st.getQuestItemsCount(SENTRY_BLADE2_ID) == 1 || st.getQuestItemsCount(SENTRY_BLADE3_ID) == 1))
		{
			htmltext = "30349-07.htm";
		}
		else if(npcId == 30349 && cond == 4 && st.getQuestItemsCount(OLD_BRONZE_SWORD_ID) == 2)
		{
			htmltext = "30349-06.htm";
			st.takeItems(OLD_BRONZE_SWORD_ID, 2);
			st.unset("cond");
			st.playSound("ItemSound.quest_finish");
			st.giveItems(57, 820);
			st.exitCurrentQuest(false);
		}
		else if(npcId == 30360 && cond == 1 && st.getQuestItemsCount(JENNIES_LETTER_ID) == 1)
		{
			htmltext = "30360-01.htm";
			st.takeItems(JENNIES_LETTER_ID, 1);
			st.giveItems(SENTRY_BLADE1_ID, 1);
			st.giveItems(SENTRY_BLADE2_ID, 1);
			st.giveItems(SENTRY_BLADE3_ID, 1);
			st.setCond(2);
		}
		else if(npcId == 30360 && (cond == 2 || cond == 3) && st.getQuestItemsCount(SENTRY_BLADE1_ID) + st.getQuestItemsCount(SENTRY_BLADE2_ID) + st.getQuestItemsCount(SENTRY_BLADE3_ID) > 0)
		{
			htmltext = "30360-02.htm";
		}
		else if(npcId == 30355 && cond == 3 && st.getQuestItemsCount(SENTRY_BLADE2_ID) == 1 && st.getQuestItemsCount(SENTRY_BLADE1_ID) == 0)
		{
			htmltext = "30355-01.htm";
			st.takeItems(SENTRY_BLADE2_ID, 1);
			st.giveItems(OLD_BRONZE_SWORD_ID, 1);
			if(st.getQuestItemsCount(SENTRY_BLADE3_ID) == 0)
			{
				st.setCond(4);
			}
		}
		else if(npcId == 30355 && (cond == 4 || cond == 3) && st.getQuestItemsCount(SENTRY_BLADE2_ID) == 0)
		{
			htmltext = "30355-02.htm";
		}
		else if(npcId == 30357 && cond == 3 && st.getQuestItemsCount(SENTRY_BLADE3_ID) == 1 && st.getQuestItemsCount(SENTRY_BLADE1_ID) == 0)
		{
			htmltext = "30357-01.htm";
			st.takeItems(SENTRY_BLADE3_ID, 1);
			st.giveItems(OLD_BRONZE_SWORD_ID, 1);
			if(st.getQuestItemsCount(SENTRY_BLADE2_ID) == 0)
			{
				st.setCond(4);
			}
		}
		else if(npcId == 30357 && (cond == 4 || cond == 5) && st.getQuestItemsCount(SENTRY_BLADE3_ID) == 0)
		{
			htmltext = "30357-02.htm";
		}
		return htmltext;
	}
}