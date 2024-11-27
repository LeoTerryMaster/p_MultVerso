package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _654_JourneytoaSettlement extends Quest implements ScriptFile
{
	private static final int printessa_spirit = 31453;
	private static final int valley_antelope = 21294;
	private static final int antelope_slave = 21295;
	private static final int q_antelope_leather = 8072;
	private static final int q_ticket_to_frintessa = 8073;
	
	public _654_JourneytoaSettlement()
	{
		super(false);
		addStartNpc(31453);
		addKillId(21294, 21295);
		addQuestItem(8072);
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
		int GetMemoState = st.getInt("to_reach_an_ending");
		int npcId = npc.getNpcId();
		if(npcId == 31453)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.set("to_reach_an_ending", String.valueOf(1), true);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "printessa_spirit_q0654_03.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
			{
				if(GetMemoState == 1)
				{
					st.setCond(2);
					st.set("to_reach_an_ending", String.valueOf(2), true);
					st.playSound("ItemSound.quest_middle");
					htmltext = "printessa_spirit_q0654_04.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_2") && GetMemoState == 2 && st.getQuestItemsCount(8072) >= 1)
			{
				st.giveItems(8073, 1);
				st.takeItems(8072, -1);
				st.unset("to_reach_an_ending");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				htmltext = "printessa_spirit_q0654_07.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		QuestState qs = st.getPlayer().getQuestState(_119_LastImperialPrince.class);
		int GetMemoState = st.getInt("to_reach_an_ending");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 31453)
					break;
				if(st.getPlayer().getLevel() >= 74 && qs != null && qs.isCompleted())
				{
					htmltext = "printessa_spirit_q0654_01.htm";
					break;
				}
				st.exitCurrentQuest(true);
				htmltext = "printessa_spirit_q0654_02.htm";
				break;
			}
			case 2:
			{
				if(npcId != 31453)
					break;
				if(GetMemoState == 1)
				{
					st.setCond(2);
					st.set("to_reach_an_ending", String.valueOf(2), true);
					st.playSound("ItemSound.quest_middle");
					htmltext = "printessa_spirit_q0654_04.htm";
					break;
				}
				if(GetMemoState == 2 && st.getQuestItemsCount(8072) == 0)
				{
					htmltext = "printessa_spirit_q0654_05.htm";
					break;
				}
				if(GetMemoState != 2 || st.getQuestItemsCount(8072) < 1)
					break;
				htmltext = "printessa_spirit_q0654_06.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("to_reach_an_ending");
		int npcId = npc.getNpcId();
		if(GetMemoState == 2 && st.getQuestItemsCount(8072) == 0)
		{
			if(npcId == 21294)
			{
				if(Rnd.get(100) < 84)
				{
					st.setCond(3);
					st.giveItems(8072, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == 21295 && Rnd.get(1000) < 893)
			{
				st.setCond(3);
				st.giveItems(8072, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}