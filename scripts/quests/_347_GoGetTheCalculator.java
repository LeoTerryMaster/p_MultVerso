package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _347_GoGetTheCalculator extends Quest implements ScriptFile
{
	public final int blacksmith_bronp = 30526;
	public final int blacksmith_silvery = 30527;
	public final int elder_spiron = 30532;
	public final int elder_balanki = 30533;
	public final int gemstone_beast = 20540;
	public final int q_gemstone = 4286;
	public final int q_calculator = 4285;
	public final int calculator = 4393;
	
	public _347_GoGetTheCalculator()
	{
		super(false);
		addStartNpc(30526);
		addTalkId(30527, 30532, 30533);
		addKillId(20540);
		addQuestItem(4286);
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
		int GetMemoState = st.getInt("get_calculator");
		if(event.equalsIgnoreCase("quest_accept"))
		{
			st.setCond(1);
			st.set("get_calculator", String.valueOf(100), true);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "blacksmith_bronp_q0347_08.htm";
		}
		else if(event.equalsIgnoreCase("reply_7") && GetMemoState == 600 && st.getQuestItemsCount(4285) >= 1)
		{
			st.unset("get_calculator");
			st.takeItems(4285, -1);
			st.giveItems(4393, 1);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
			htmltext = "blacksmith_bronp_q0347_10.htm";
		}
		else if(event.equalsIgnoreCase("reply_8") && GetMemoState == 600 && st.getQuestItemsCount(4285) >= 1)
		{
			st.unset("get_calculator");
			st.takeItems(4285, -1);
			st.giveItems(57, 1000, true);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
			htmltext = "blacksmith_bronp_q0347_11.htm";
		}
		else if(event.equalsIgnoreCase("reply_3"))
		{
			st.set("get_calculator", String.valueOf(200 + GetMemoState), true);
			if(GetMemoState == 100)
			{
				st.setCond(3);
				st.playSound("ItemSound.quest_middle");
			}
			else if(GetMemoState == 200)
			{
				st.setCond(4);
				st.playSound("ItemSound.quest_middle");
			}
			htmltext = "elder_spiron_q0347_02.htm";
		}
		else if(event.equalsIgnoreCase("reply_2"))
		{
			if(st.getQuestItemsCount(57) >= 100)
			{
				st.set("get_calculator", String.valueOf(100 + GetMemoState), true);
				st.takeItems(57, 100);
				if(GetMemoState == 100)
				{
					st.setCond(2);
					st.playSound("ItemSound.quest_middle");
				}
				else if(GetMemoState == 300)
				{
					st.setCond(4);
					st.playSound("ItemSound.quest_middle");
				}
				htmltext = "elder_balanki_q0347_02.htm";
			}
			else
			{
				htmltext = "elder_balanki_q0347_03.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("get_calculator");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 30526)
					break;
				if(st.getPlayer().getLevel() >= 12)
				{
					htmltext = "blacksmith_bronp_q0347_01.htm";
					break;
				}
				htmltext = "blacksmith_bronp_q0347_02.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId == 30526)
				{
					if(st.getQuestItemsCount(4285) >= 1)
					{
						htmltext = "blacksmith_bronp_q0347_09.htm";
						break;
					}
					if(st.getQuestItemsCount(4285) == 0 && GetMemoState == 600)
					{
						htmltext = "blacksmith_bronp_q0347_12.htm";
						break;
					}
					if(st.getQuestItemsCount(4285) == 0 && GetMemoState == 100)
					{
						htmltext = "blacksmith_bronp_q0347_13.htm";
						break;
					}
					if(st.getQuestItemsCount(4285) == 0 && (GetMemoState == 200 || GetMemoState == 300))
					{
						htmltext = "blacksmith_bronp_q0347_14.htm";
						break;
					}
					if(st.getQuestItemsCount(4285) != 0 || GetMemoState != 400 && GetMemoState != 500 && GetMemoState != 600)
						break;
					htmltext = "blacksmith_bronp_q0347_15.htm";
					break;
				}
				if(npcId == 30527)
				{
					if(GetMemoState == 100 || GetMemoState == 200 || GetMemoState == 300)
					{
						htmltext = "blacksmith_silvery_q0347_01.htm";
						break;
					}
					if(GetMemoState == 400)
					{
						st.setCond(5);
						st.set("get_calculator", String.valueOf(500), true);
						st.playSound("ItemSound.quest_middle");
						htmltext = "blacksmith_silvery_q0347_02.htm";
						break;
					}
					if(GetMemoState == 500 && st.getQuestItemsCount(4286) >= 10)
					{
						st.setCond(6);
						st.set("get_calculator", String.valueOf(600), true);
						st.giveItems(4285, 1);
						st.takeItems(4286, -1);
						st.playSound("ItemSound.quest_middle");
						htmltext = "blacksmith_silvery_q0347_03.htm";
						break;
					}
					if(GetMemoState == 500 && st.getQuestItemsCount(4286) < 10)
					{
						htmltext = "blacksmith_silvery_q0347_04.htm";
						break;
					}
					if(GetMemoState != 600 && GetMemoState != 600)
						break;
					htmltext = "blacksmith_silvery_q0347_05.htm";
					break;
				}
				if(npcId == 30532)
				{
					if(GetMemoState == 100 || GetMemoState == 200)
					{
						htmltext = "elder_spiron_q0347_01.htm";
						break;
					}
					if(GetMemoState != 300 && GetMemoState != 400 && GetMemoState != 500 && GetMemoState != 600)
						break;
					htmltext = "elder_spiron_q0347_05.htm";
					break;
				}
				if(npcId != 30533)
					break;
				if(GetMemoState == 100 || GetMemoState == 300)
				{
					htmltext = "elder_balanki_q0347_01.htm";
					break;
				}
				if(GetMemoState != 200 && GetMemoState != 400 && GetMemoState != 500 && GetMemoState != 600)
					break;
				htmltext = "elder_balanki_q0347_04.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int GetMemoState = st.getInt("get_calculator");
		if(npcId == 20540 && GetMemoState == 500 && st.getQuestItemsCount(4286) < 10 && Rnd.get(10) <= 4)
		{
			st.giveItems(4286, 1);
			if(st.getQuestItemsCount(4286) >= 10)
			{
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