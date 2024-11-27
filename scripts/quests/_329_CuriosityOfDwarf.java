package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _329_CuriosityOfDwarf extends Quest implements ScriptFile
{
	private final int GOLEM_HEARTSTONE = 1346;
	private final int BROKEN_HEARTSTONE = 1365;
	
	public _329_CuriosityOfDwarf()
	{
		super(false);
		addStartNpc(30437);
		addKillId(20083);
		addKillId(20085);
		addQuestItem(BROKEN_HEARTSTONE);
		addQuestItem(GOLEM_HEARTSTONE);
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
		if(event.equalsIgnoreCase("trader_rolento_q0329_03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("trader_rolento_q0329_06.htm"))
		{
			st.exitCurrentQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int id = st.getState();
		if(id == 1)
		{
			st.setCond(0);
		}
		String htmltext;
		if(st.getCond() == 0)
		{
			if(st.getPlayer().getLevel() >= 33)
			{
				htmltext = "trader_rolento_q0329_02.htm";
			}
			else
			{
				htmltext = "trader_rolento_q0329_01.htm";
				st.exitCurrentQuest(true);
			}
		}
		else
		{
			long heart2 = st.getQuestItemsCount(GOLEM_HEARTSTONE);
			long broken = st.getQuestItemsCount(BROKEN_HEARTSTONE);
			if(broken + heart2 > 0)
			{
				st.giveItems(57, 50 * broken + 1000 * heart2);
				st.takeItems(BROKEN_HEARTSTONE, -1);
				st.takeItems(GOLEM_HEARTSTONE, -1);
				htmltext = "trader_rolento_q0329_05.htm";
			}
			else
			{
				htmltext = "trader_rolento_q0329_04.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int n = Rnd.get(1, 100);
		if(npcId == 20085)
		{
			if(n < 5)
			{
				st.giveItems(GOLEM_HEARTSTONE, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(n < 58)
			{
				st.giveItems(BROKEN_HEARTSTONE, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(npcId == 20083)
		{
			if(n < 6)
			{
				st.giveItems(GOLEM_HEARTSTONE, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(n < 56)
			{
				st.giveItems(BROKEN_HEARTSTONE, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}