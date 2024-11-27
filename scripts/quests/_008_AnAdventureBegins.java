package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _008_AnAdventureBegins extends Quest implements ScriptFile
{
	int JASMINE = 30134;
	int ROSELYN = 30355;
	int HARNE = 30144;
	int ROSELYNS_NOTE = 7573;
	int SCROLL_OF_ESCAPE_GIRAN = 7126;
	int MARK_OF_TRAVELER = 7570;
	
	public _008_AnAdventureBegins()
	{
		super(false);
		addStartNpc(JASMINE);
		addTalkId(JASMINE);
		addTalkId(ROSELYN);
		addTalkId(HARNE);
		addQuestItem(ROSELYNS_NOTE);
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
		if(event.equalsIgnoreCase("jasmine_q0008_0104.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("sentry_roseline_q0008_0201.htm"))
		{
			st.giveItems(ROSELYNS_NOTE, 1);
			st.setCond(2);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("harne_q0008_0301.htm"))
		{
			st.takeItems(ROSELYNS_NOTE, -1);
			st.setCond(3);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("jasmine_q0008_0401.htm"))
		{
			st.giveItems(SCROLL_OF_ESCAPE_GIRAN, 1);
			st.giveItems(MARK_OF_TRAVELER, 1);
			st.setCond(0);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
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
		if(npcId == JASMINE)
		{
			if(cond == 0 && st.getPlayer().getRace() == Race.darkelf)
			{
				if(st.getPlayer().getLevel() >= 3)
				{
					htmltext = "jasmine_q0008_0101.htm";
				}
				else
				{
					htmltext = "jasmine_q0008_0102.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "jasmine_q0008_0105.htm";
			}
			else if(cond == 3)
			{
				htmltext = "jasmine_q0008_0301.htm";
			}
		}
		else if(npcId == ROSELYN)
		{
			htmltext = st.getQuestItemsCount(ROSELYNS_NOTE) == 0 ? "sentry_roseline_q0008_0101.htm" : "sentry_roseline_q0008_0202.htm";
		}
		else if(npcId == HARNE)
		{
			if(cond == 2 && st.getQuestItemsCount(ROSELYNS_NOTE) > 0)
			{
				htmltext = "harne_q0008_0201.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(ROSELYNS_NOTE) == 0)
			{
				htmltext = "harne_q0008_0302.htm";
			}
			else if(cond == 3)
			{
				htmltext = "harne_q0008_0303.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		return null;
	}
}