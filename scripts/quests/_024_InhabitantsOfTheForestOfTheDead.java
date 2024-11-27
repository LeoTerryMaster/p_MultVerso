package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;

public class _024_InhabitantsOfTheForestOfTheDead extends Quest implements ScriptFile
{
	private static final int day_dorian = 31389;
	private static final int q_forest_stone2 = 31531;
	private static final int maid_of_ridia = 31532;
	private static final int shadow_hardin = 31522;
	private static final int q_letter_of_ridia = 7065;
	private static final int q_ridia_hairpin = 7148;
	private static final int q_triols_totem1 = 7151;
	private static final int q_lost_flower = 7152;
	private static final int q_silver_cross = 7153;
	private static final int q_broken_silver_cross = 7154;
	private static final int q_triols_totem2 = 7156;
	private static final int bone_snatcher = 21557;
	private static final int bone_snatcher_a = 21558;
	private static final int bone_shaper = 21560;
	private static final int bone_collector = 21563;
	private static final int skull_collector = 21564;
	private static final int bone_animator = 21565;
	private static final int skull_animator = 21566;
	private static final int bone_slayer = 21567;
	
	public _024_InhabitantsOfTheForestOfTheDead()
	{
		super(false);
		addStartNpc(31389);
		addTalkId(31531, 31532, 31522);
		addKillId(21557, 21558, 21560, 21563, 21564, 21565, 21566, 21567);
		addQuestItem(7151);
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
		int GetMemoState = st.getInt("people_of_lost_forest");
		if(event.startsWith("see_creature"))
		{
			if(st.getQuestItemsCount(7153) > 0)
			{
				st.setCond(4);
				st.takeItems(7153, -1);
				st.giveItems(7154, 1);
				Functions.npcSay(npc, "That sign!");
			}
			return null;
		}
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			st.setCond(1);
			st.set("people_of_lost_forest", String.valueOf(1), true);
			st.giveItems(7152, 1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "day_dorian_q0024_03.htm";
		}
		else if(event.equalsIgnoreCase("reply_1"))
		{
			st.set("people_of_lost_forest", String.valueOf(3), true);
			htmltext = "day_dorian_q0024_08.htm";
		}
		else if(event.equalsIgnoreCase("reply_3"))
		{
			if(GetMemoState == 3)
			{
				st.setCond(3);
				st.set("people_of_lost_forest", String.valueOf(4), true);
				st.giveItems(7153, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "day_dorian_q0024_13.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_5"))
		{
			st.playSound("InterfaceSound.charstat_open_01");
			htmltext = "day_dorian_q0024_18.htm";
		}
		else if(event.equalsIgnoreCase("reply_6"))
		{
			if(GetMemoState == 4 && st.getQuestItemsCount(7154) >= 1)
			{
				st.setCond(5);
				st.set("people_of_lost_forest", String.valueOf(5), true);
				st.takeItems(7154, -1);
				htmltext = "day_dorian_q0024_19.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_1a"))
		{
			st.setCond(2);
			st.set("people_of_lost_forest", String.valueOf(2), true);
			st.takeItems(7152, -1);
			st.playSound("ItemSound.quest_middle");
			htmltext = "q_forest_stone2_q0024_02.htm";
		}
		else if(event.equalsIgnoreCase("reply_7"))
		{
			if(GetMemoState == 5)
			{
				st.setCond(6);
				st.set("people_of_lost_forest", String.valueOf(6), true);
				st.giveItems(7065, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "maid_of_ridia_q0024_04.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_8"))
		{
			if((GetMemoState == 6 || GetMemoState == 7) && st.getQuestItemsCount(7148) >= 1)
			{
				st.takeItems(7065, -1);
				st.takeItems(7148, -1);
				st.set("people_of_lost_forest", String.valueOf(8), true);
				htmltext = "maid_of_ridia_q0024_06.htm";
			}
			else if((GetMemoState == 6 || GetMemoState == 7) && st.getQuestItemsCount(7148) == 0)
			{
				st.setCond(7);
				st.set("people_of_lost_forest", String.valueOf(7), true);
				st.playSound("ItemSound.quest_middle");
				htmltext = "maid_of_ridia_q0024_07.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_10"))
		{
			if(GetMemoState == 8)
			{
				st.set("people_of_lost_forest", String.valueOf(9), true);
				htmltext = "maid_of_ridia_q0024_10.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_11"))
		{
			if(GetMemoState == 9)
			{
				st.set("people_of_lost_forest", String.valueOf(10), true);
				htmltext = "maid_of_ridia_q0024_14.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_12"))
		{
			if(GetMemoState == 10)
			{
				st.setCond(9);
				st.set("people_of_lost_forest", String.valueOf(11), true);
				st.playSound("ItemSound.quest_middle");
				htmltext = "maid_of_ridia_q0024_19.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_14"))
		{
			if(GetMemoState == 11 && st.getQuestItemsCount(7151) >= 1)
			{
				st.set("people_of_lost_forest", String.valueOf(12), true);
				st.takeItems(7151, -1);
				htmltext = "shadow_hardin_q0024_03.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_16"))
		{
			if(GetMemoState == 12)
			{
				st.setCond(11);
				st.set("people_of_lost_forest", String.valueOf(13), true);
				htmltext = "shadow_hardin_q0024_08.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_18"))
		{
			if(GetMemoState == 13)
			{
				st.set("people_of_lost_forest", String.valueOf(14), true);
				htmltext = "shadow_hardin_q0024_17.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_19") && GetMemoState == 14)
		{
			st.giveItems(7156, 1);
			st.addExpAndSp(242105, 22529);
			st.unset("people_of_lost_forest");
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
			htmltext = "shadow_hardin_q0024_21.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		QuestState qs = st.getPlayer().getQuestState(_023_LidiasHeart.class);
		int GetMemoState = st.getInt("people_of_lost_forest");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 31389)
					break;
				if(qs != null && qs.isCompleted() && st.getPlayer().getLevel() >= 65)
				{
					htmltext = "day_dorian_q0024_01.htm";
					break;
				}
				htmltext = "day_dorian_q0024_02.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId == 31389)
				{
					if(GetMemoState == 1)
					{
						htmltext = "day_dorian_q0024_04.htm";
						break;
					}
					if(GetMemoState == 2)
					{
						htmltext = "day_dorian_q0024_05.htm";
						break;
					}
					if(GetMemoState == 3)
					{
						htmltext = "day_dorian_q0024_09.htm";
						break;
					}
					if(GetMemoState == 4 && st.getQuestItemsCount(7154) >= 1)
					{
						htmltext = "day_dorian_q0024_15.htm";
						break;
					}
					if(GetMemoState == 5)
					{
						htmltext = "day_dorian_q0024_20.htm";
						break;
					}
					if(GetMemoState == 7 && st.getQuestItemsCount(7148) == 0)
					{
						st.setCond(8);
						st.giveItems(7148, 1);
						st.playSound("ItemSound.quest_middle");
						htmltext = "day_dorian_q0024_21.htm";
						break;
					}
					if((GetMemoState != 7 || st.getQuestItemsCount(7148) < 1) && GetMemoState != 6)
						break;
					htmltext = "day_dorian_q0024_22.htm";
					break;
				}
				if(npcId == 31531)
				{
					if(GetMemoState == 1 && st.getQuestItemsCount(7152) >= 1)
					{
						st.playSound("AmdSound.d_wind_loot_02");
						htmltext = "q_forest_stone2_q0024_01.htm";
						break;
					}
					if(GetMemoState != 2)
						break;
					htmltext = "q_forest_stone2_q0024_03.htm";
					break;
				}
				if(npcId == 31532)
				{
					if(GetMemoState == 5)
					{
						htmltext = "maid_of_ridia_q0024_01.htm";
						break;
					}
					if(GetMemoState == 6 && st.getQuestItemsCount(7065) >= 1)
					{
						htmltext = "maid_of_ridia_q0024_05.htm";
						break;
					}
					if(GetMemoState == 7)
					{
						htmltext = "maid_of_ridia_q0024_07a.htm";
						break;
					}
					if(GetMemoState == 8)
					{
						htmltext = "maid_of_ridia_q0024_08.htm";
						break;
					}
					if(GetMemoState == 9)
					{
						htmltext = "maid_of_ridia_q0024_11.htm";
						break;
					}
					if(GetMemoState != 11)
						break;
					htmltext = "maid_of_ridia_q0024_20.htm";
					break;
				}
				if(npcId != 31522)
					break;
				if(GetMemoState == 11 && st.getQuestItemsCount(7151) >= 1)
				{
					htmltext = "shadow_hardin_q0024_01.htm";
					break;
				}
				if(GetMemoState == 12)
				{
					htmltext = "shadow_hardin_q0024_04.htm";
					break;
				}
				if(GetMemoState == 13)
				{
					htmltext = "shadow_hardin_q0024_09.htm";
					break;
				}
				if(GetMemoState != 14)
					break;
				htmltext = "shadow_hardin_q0024_18.htm";
				break;
			}
			case 3:
			{
				if(npcId != 31522)
					break;
				htmltext = "shadow_hardin_q0024_22.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int GetMemoState = st.getInt("people_of_lost_forest");
		if((npcId == 21557 || npcId == 21558 || npcId == 21560 || npcId == 21563 || npcId == 21564 || npcId == 21565 || npcId == 21566 || npcId == 21567) && GetMemoState == 11 && Rnd.get(100) < 10)
		{
			st.setCond(10);
			st.giveItems(7151, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}