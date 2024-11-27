package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _010_IntoTheWorld extends Quest implements ScriptFile
{
	int VERY_EXPENSIVE_NECKLACE = 7574;
	int SCROLL_OF_ESCAPE_GIRAN = 7126;
	int MARK_OF_TRAVELER = 7570;
	int BALANKI = 30533;
	int REED = 30520;
	int GERALD = 30650;
	
	public _010_IntoTheWorld()
	{
		super(false);
		addStartNpc(BALANKI);
		addTalkId(BALANKI);
		addTalkId(REED);
		addTalkId(GERALD);
		addQuestItem(VERY_EXPENSIVE_NECKLACE);
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
		if(event.equalsIgnoreCase("elder_balanki_q0010_0104.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("warehouse_chief_reed_q0010_0201.htm"))
		{
			st.giveItems(VERY_EXPENSIVE_NECKLACE, 1);
			st.setCond(2);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("gerald_priest_of_earth_q0010_0301.htm"))
		{
			st.takeItems(VERY_EXPENSIVE_NECKLACE, -1);
			st.setCond(3);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("warehouse_chief_reed_q0010_0401.htm"))
		{
			st.setCond(4);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("elder_balanki_q0010_0501.htm"))
		{
			st.giveItems(SCROLL_OF_ESCAPE_GIRAN, 1);
			st.giveItems(MARK_OF_TRAVELER, 1);
			st.exitCurrentQuest(false);
			st.playSound("ItemSound.quest_finish");
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
		if(npcId == BALANKI)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() == Race.dwarf && st.getPlayer().getLevel() >= 3)
				{
					htmltext = "elder_balanki_q0010_0101.htm";
				}
				else
				{
					htmltext = "elder_balanki_q0010_0102.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "elder_balanki_q0010_0105.htm";
			}
			else if(cond == 4)
			{
				htmltext = "elder_balanki_q0010_0401.htm";
			}
		}
		else if(npcId == REED)
		{
			if(cond == 1)
			{
				htmltext = "warehouse_chief_reed_q0010_0101.htm";
			}
			else if(cond == 2)
			{
				htmltext = "warehouse_chief_reed_q0010_0202.htm";
			}
			else if(cond == 3)
			{
				htmltext = "warehouse_chief_reed_q0010_0301.htm";
			}
			else if(cond == 4)
			{
				htmltext = "warehouse_chief_reed_q0010_0402.htm";
			}
		}
		else if(npcId == GERALD)
		{
			htmltext = cond == 2 && st.getQuestItemsCount(VERY_EXPENSIVE_NECKLACE) > 0 ? "gerald_priest_of_earth_q0010_0201.htm" : cond == 3 ? "gerald_priest_of_earth_q0010_0302.htm" : "gerald_priest_of_earth_q0010_0303.htm";
		}
		return htmltext;
	}
}