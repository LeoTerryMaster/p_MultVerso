package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _013_ParcelDelivery extends Quest implements ScriptFile
{
	private static final int PACKAGE = 7263;
	
	public _013_ParcelDelivery()
	{
		super(false);
		addStartNpc(31274);
		addTalkId(31274);
		addTalkId(31539);
		addQuestItem(7263);
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
		if(event.equalsIgnoreCase("mineral_trader_fundin_q0013_0104.htm"))
		{
			st.setCond(1);
			st.giveItems(7263, 1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("warsmith_vulcan_q0013_0201.htm"))
		{
			st.takeItems(7263, -1);
			st.giveItems(57, 82656, true);
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
		if(npcId == 31274)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 74)
				{
					htmltext = "mineral_trader_fundin_q0013_0101.htm";
				}
				else
				{
					htmltext = "mineral_trader_fundin_q0013_0103.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "mineral_trader_fundin_q0013_0105.htm";
			}
		}
		else if(npcId == 31539 && cond == 1 && st.getQuestItemsCount(7263) == 1)
		{
			htmltext = "warsmith_vulcan_q0013_0101.htm";
		}
		return htmltext;
	}
}