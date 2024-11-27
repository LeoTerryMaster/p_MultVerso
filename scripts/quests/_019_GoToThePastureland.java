package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _019_GoToThePastureland extends Quest implements ScriptFile
{
	int VLADIMIR = 31302;
	int TUNATUN = 31537;
	int BEAST_MEAT = 7547;
	
	public _019_GoToThePastureland()
	{
		super(false);
		addStartNpc(VLADIMIR);
		addTalkId(VLADIMIR);
		addTalkId(TUNATUN);
		addQuestItem(BEAST_MEAT);
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
		if(event.equals("trader_vladimir_q0019_0104.htm"))
		{
			st.giveItems(BEAST_MEAT, 1);
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		if(event.equals("beast_herder_tunatun_q0019_0201.htm"))
		{
			st.takeItems(BEAST_MEAT, -1);
			st.giveItems(57, 30000);
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
		if(npcId == VLADIMIR)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 63)
				{
					htmltext = "trader_vladimir_q0019_0101.htm";
				}
				else
				{
					htmltext = "trader_vladimir_q0019_0103.htm";
					st.exitCurrentQuest(true);
				}
			}
			else
			{
				htmltext = "trader_vladimir_q0019_0105.htm";
			}
		}
		else if(npcId == TUNATUN)
		{
			if(st.getQuestItemsCount(BEAST_MEAT) >= 1)
			{
				htmltext = "beast_herder_tunatun_q0019_0101.htm";
			}
			else
			{
				htmltext = "beast_herder_tunatun_q0019_0202.htm";
				st.exitCurrentQuest(true);
			}
		}
		return htmltext;
	}
}