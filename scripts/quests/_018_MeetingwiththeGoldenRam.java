package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _018_MeetingwiththeGoldenRam extends Quest implements ScriptFile
{
	private static final int SUPPLY_BOX = 7245;
	
	public _018_MeetingwiththeGoldenRam()
	{
		super(false);
		addStartNpc(31314);
		addTalkId(31314);
		addTalkId(31315);
		addTalkId(31555);
		addQuestItem(7245);
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
		if(event.equals("warehouse_chief_donal_q0018_0104.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("freighter_daisy_q0018_0201.htm"))
		{
			st.setCond(2);
			st.giveItems(7245, 1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("supplier_abercrombie_q0018_0301.htm"))
		{
			st.takeItems(7245, -1);
			st.giveItems(57, 15000);
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
		if(npcId == 31314)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 66)
				{
					htmltext = "warehouse_chief_donal_q0018_0101.htm";
				}
				else
				{
					htmltext = "warehouse_chief_donal_q0018_0103.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "warehouse_chief_donal_q0018_0105.htm";
			}
		}
		else if(npcId == 31315)
		{
			if(cond == 1)
			{
				htmltext = "freighter_daisy_q0018_0101.htm";
			}
			else if(cond == 2)
			{
				htmltext = "freighter_daisy_q0018_0202.htm";
			}
		}
		else if(npcId == 31555 && cond == 2 && st.getQuestItemsCount(7245) == 1)
		{
			htmltext = "supplier_abercrombie_q0018_0201.htm";
		}
		return htmltext;
	}
}