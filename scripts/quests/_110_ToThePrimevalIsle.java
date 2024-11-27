package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _110_ToThePrimevalIsle extends Quest implements ScriptFile
{
	int ANTON = 31338;
	int MARQUEZ = 32113;
	int ANCIENT_BOOK = 8777;
	
	public _110_ToThePrimevalIsle()
	{
		super(false);
		addStartNpc(ANTON);
		addTalkId(ANTON);
		addTalkId(MARQUEZ);
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
			htmltext = "scroll_seller_anton_q0110_05.htm";
			st.setCond(1);
			st.giveItems(ANCIENT_BOOK, 1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("2") && st.getQuestItemsCount(ANCIENT_BOOK) > 0)
		{
			htmltext = "marquez_q0110_05.htm";
			st.playSound("ItemSound.quest_finish");
			st.giveItems(57, 169380);
			st.takeItems(ANCIENT_BOOK, -1);
			st.exitCurrentQuest(false);
		}
		else if(event.equals("3"))
		{
			htmltext = "marquez_q0110_06.htm";
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getCond();
		if(id == 1)
		{
			if(st.getPlayer().getLevel() >= 75)
			{
				htmltext = "scroll_seller_anton_q0110_01.htm";
			}
			else
			{
				st.exitCurrentQuest(true);
				htmltext = "scroll_seller_anton_q0110_02.htm";
			}
		}
		else if(npcId == ANTON)
		{
			if(cond == 1)
			{
				htmltext = "scroll_seller_anton_q0110_07.htm";
			}
		}
		else if(id == 2 && npcId == MARQUEZ && cond == 1)
		{
			htmltext = st.getQuestItemsCount(ANCIENT_BOOK) == 0 ? "marquez_q0110_07.htm" : "marquez_q0110_01.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		return null;
	}
}