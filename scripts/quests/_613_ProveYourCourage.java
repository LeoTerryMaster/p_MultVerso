package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _613_ProveYourCourage extends Quest implements ScriptFile
{
	private static final int elder_ashas_barka_durai = 31377;
	private static final int ketra_hero_hekaton = 25299;
	private static final int q_barka_friendship_3 = 7223;
	private static final int q_hekaton_head = 7240;
	private static final int q_feather_of_valor = 7229;
	
	public _613_ProveYourCourage()
	{
		super(true);
		addStartNpc(31377);
		addKillId(25299);
		addQuestItem(7240);
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
		if(event.equals("quest_accept"))
		{
			st.setCond(1);
			st.set("prove_your_courage_varka", String.valueOf(11), true);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "elder_ashas_barka_durai_q0613_0104.htm";
		}
		else if(event.equals("reply_3"))
		{
			if(st.getQuestItemsCount(7240) >= 1)
			{
				st.takeItems(7240, -1);
				st.giveItems(7229, 1);
				st.addExpAndSp(10000, 0);
				st.unset("prove_your_courage_varka");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				htmltext = "elder_ashas_barka_durai_q0613_0201.htm";
			}
			else
			{
				htmltext = "elder_ashas_barka_durai_q0613_0202.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("prove_your_courage_varka");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 31377)
					break;
				if(st.getPlayer().getLevel() >= 75)
				{
					if(st.getQuestItemsCount(7223) >= 1)
					{
						htmltext = "elder_ashas_barka_durai_q0613_0101.htm";
						break;
					}
					st.exitCurrentQuest(true);
					htmltext = "elder_ashas_barka_durai_q0613_0102.htm";
					break;
				}
				st.exitCurrentQuest(true);
				htmltext = "elder_ashas_barka_durai_q0613_0103.htm";
				break;
			}
			case 2:
			{
				if(npcId != 31377 || GetMemoState < 11 || GetMemoState > 12)
					break;
				if(GetMemoState == 12 && st.getQuestItemsCount(7240) >= 1)
				{
					htmltext = "elder_ashas_barka_durai_q0613_0105.htm";
					break;
				}
				htmltext = "elder_ashas_barka_durai_q0613_0106.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("prove_your_courage_varka");
		int npcId = npc.getNpcId();
		if(GetMemoState == 11 && npcId == 25299 && Rnd.get(1000) < 1000)
		{
			if(st.getQuestItemsCount(7240) + 1 >= 1)
			{
				if(st.getQuestItemsCount(7240) < 1)
				{
					st.setCond(2);
					st.set("prove_your_courage_varka", String.valueOf(12), true);
					st.giveItems(7240, 1);
					st.playSound("ItemSound.quest_middle");
				}
			}
			else
			{
				st.giveItems(7240, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}