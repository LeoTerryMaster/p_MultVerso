package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _014_WhereaboutsoftheArchaeologist extends Quest implements ScriptFile
{
	private static final int LETTER_TO_ARCHAEOLOGIST = 7253;
	
	public _014_WhereaboutsoftheArchaeologist()
	{
		super(false);
		addStartNpc(31263);
		addTalkId(31538);
		addQuestItem(7253);
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
		if(event.equalsIgnoreCase("trader_liesel_q0014_0104.htm"))
		{
			st.setCond(1);
			st.giveItems(7253, 1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("explorer_ghost_a_q0014_0201.htm"))
		{
			st.takeItems(7253, -1);
			st.giveItems(57, 113228);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
			return "explorer_ghost_a_q0014_0201.htm";
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
		if(npcId == 31263)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 74)
				{
					htmltext = "trader_liesel_q0014_0101.htm";
				}
				else
				{
					htmltext = "trader_liesel_q0014_0103.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "trader_liesel_q0014_0104.htm";
			}
		}
		else if(npcId == 31538 && cond == 1 && st.getQuestItemsCount(7253) == 1)
		{
			htmltext = "explorer_ghost_a_q0014_0101.htm";
		}
		return htmltext;
	}
}