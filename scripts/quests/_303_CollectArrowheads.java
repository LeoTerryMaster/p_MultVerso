package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _303_CollectArrowheads extends Quest implements ScriptFile
{
	int ORCISH_ARROWHEAD = 963;
	
	public _303_CollectArrowheads()
	{
		super(false);
		addStartNpc(30029);
		addTalkId(30029);
		addKillId(20361);
		addQuestItem(ORCISH_ARROWHEAD);
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
		if(event.equalsIgnoreCase("minx_q0303_04.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext;
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 10)
			{
				htmltext = "minx_q0303_03.htm";
			}
			else
			{
				htmltext = "minx_q0303_02.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(st.getQuestItemsCount(ORCISH_ARROWHEAD) < 10)
		{
			htmltext = "minx_q0303_05.htm";
		}
		else
		{
			st.takeItems(ORCISH_ARROWHEAD, -1);
			st.giveItems(57, 1000);
			st.addExpAndSp(2000, 0);
			htmltext = "minx_q0303_06.htm";
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getQuestItemsCount(ORCISH_ARROWHEAD) < 10)
		{
			st.giveItems(ORCISH_ARROWHEAD, 1);
			if(st.getQuestItemsCount(ORCISH_ARROWHEAD) == 10)
			{
				st.setCond(2);
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}