package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;

public class _023_LidiasHeart extends Quest implements ScriptFile
{
	private static final int highpriest_innocentin = 31328;
	private static final int broken_desk1 = 31526;
	private static final int rune_ghost1 = 31524;
	private static final int q_forest_stone1 = 31523;
	private static final int day_violet = 31386;
	private static final int rust_box1 = 31530;
	private static final int q_lost_map = 7063;
	private static final int q_silversp_key1 = 7149;
	private static final int q_ridia_hairpin = 7148;
	private static final int q_ridia_diary = 7064;
	private static final int q_silver_spear = 7150;
	
	public _023_LidiasHeart()
	{
		super(false);
		addStartNpc(31328);
		addTalkId(31328, 31526, 31524, 31523, 31386, 31530);
		addQuestItem(7063, 7149, 7148, 7150, 7064);
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
		QuestState qs = st.getPlayer().getQuestState(_022_TragedyInVonHellmannForest.class);
		int spawned_rune_ghost1 = st.getInt("spawned_rune_ghost1");
		int GetMemoState = st.getInt("truth_of_ridia");
		int npcId = npc.getNpcId();
		if(npcId == 31328)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				if(st.getPlayer().getLevel() < 64 || qs == null)
				{
					htmltext = "highpriest_innocentin_q0023_02.htm";
				}
				else
				{
					st.setCond(1);
					st.set("truth_of_ridia", String.valueOf(1), true);
					st.set("spawned_rune_ghost1", String.valueOf(0), true);
					st.giveItems(7063, 1);
					st.giveItems(7149, 1);
					st.setState(2);
					st.playSound("ItemSound.quest_accept");
					htmltext = "highpriest_innocentin_q0023_03.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_1"))
			{
				htmltext = "highpriest_innocentin_q0023_05.htm";
			}
			else if(event.equalsIgnoreCase("reply_2"))
			{
				htmltext = "highpriest_innocentin_q0023_06.htm";
			}
			else if(event.equalsIgnoreCase("reply_3"))
			{
				if(GetMemoState == 1)
				{
					st.setCond(2);
					st.set("truth_of_ridia", String.valueOf(2), true);
					st.playSound("ItemSound.quest_middle");
					htmltext = "highpriest_innocentin_q0023_07.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_13"))
			{
				htmltext = "highpriest_innocentin_q0023_11.htm";
			}
			else if(event.equalsIgnoreCase("reply_15"))
			{
				if(GetMemoState == 5 || GetMemoState == 6)
				{
					st.setCond(5);
					st.set("truth_of_ridia", String.valueOf(6), true);
					htmltext = "highpriest_innocentin_q0023_12.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_14"))
			{
				if(GetMemoState == 5 || GetMemoState == 6)
				{
					st.set("truth_of_ridia", String.valueOf(7), true);
					htmltext = "highpriest_innocentin_q0023_13.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_17"))
			{
				htmltext = "highpriest_innocentin_q0023_16.htm";
			}
			else if(event.equalsIgnoreCase("reply_18"))
			{
				htmltext = "highpriest_innocentin_q0023_17.htm";
			}
			else if(event.equalsIgnoreCase("reply_19"))
			{
				htmltext = "highpriest_innocentin_q0023_18.htm";
			}
			else if(event.equalsIgnoreCase("reply_20"))
			{
				htmltext = "highpriest_innocentin_q0023_19.htm";
				st.playSound("AmbSound.mt_creak01");
			}
			else if(event.equalsIgnoreCase("reply_21"))
			{
				if(GetMemoState == 7)
				{
					st.setCond(6);
					st.set("truth_of_ridia", String.valueOf(8), true);
					htmltext = "highpriest_innocentin_q0023_20.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_16"))
			{
				st.setCond(5);
				htmltext = "highpriest_innocentin_q0023_21.htm";
			}
		}
		else if(npcId == 31526)
		{
			if(event.equalsIgnoreCase("reply_4"))
			{
				if(GetMemoState == 2 && st.getQuestItemsCount(7149) >= 1)
				{
					st.set("truth_of_ridia", String.valueOf(3), true);
					st.takeItems(7149, -1);
					htmltext = "broken_desk1_q0023_02.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_5"))
			{
				htmltext = "broken_desk1_q0023_04.htm";
			}
			else if(event.equalsIgnoreCase("reply_7"))
			{
				htmltext = "broken_desk1_q0023_05.htm";
			}
			else if(event.equalsIgnoreCase("reply_8"))
			{
				st.set("truth_of_ridia", String.valueOf(GetMemoState + 1), true);
				st.giveItems(7148, 1);
				htmltext = "broken_desk1_q0023_06.htm";
				if(st.getQuestItemsCount(7064) >= 1)
				{
					st.setCond(4);
				}
			}
			else if(event.equalsIgnoreCase("reply_6"))
			{
				htmltext = "broken_desk1_q0023_07a.htm";
			}
			else if(event.equalsIgnoreCase("reply_9"))
			{
				htmltext = "broken_desk1_q0023_08.htm";
				st.playSound("ItemSound.itemdrop_armor_leather");
			}
			else if(event.equalsIgnoreCase("reply_10"))
			{
				htmltext = "broken_desk1_q0023_09.htm";
			}
			else if(event.equalsIgnoreCase("reply_11"))
			{
				htmltext = "broken_desk1_q0023_10.htm";
				st.playSound("AmbSound.eg_dron_02");
			}
			else if(event.equalsIgnoreCase("reply_12"))
			{
				st.set("truth_of_ridia", String.valueOf(GetMemoState + 1), true);
				st.giveItems(7064, 1);
				htmltext = "broken_desk1_q0023_11.htm";
				if(st.getQuestItemsCount(7148) >= 1)
				{
					st.setCond(4);
				}
			}
			else if(event.equals("read_book"))
			{
				htmltext = "q_ridia_diary001.htm";
			}
		}
		else if(npcId == 31524)
		{
			if(event.equalsIgnoreCase("reply_23"))
			{
				htmltext = "rune_ghost1_q0023_02.htm";
				st.playSound("ChrSound.MHFighter_cry");
			}
			else if(event.equalsIgnoreCase("reply_24"))
			{
				htmltext = "rune_ghost1_q0023_03.htm";
			}
			else if(event.equalsIgnoreCase("reply_25"))
			{
				if(GetMemoState == 8)
				{
					st.setCond(7);
					st.set("truth_of_ridia", String.valueOf(9), true);
					st.takeItems(7064, -1);
					htmltext = "rune_ghost1_q0023_04.htm";
				}
			}
			else if(event.equalsIgnoreCase("2101"))
			{
				st.unset("spawned_rune_ghost1");
				if(npc != null)
				{
					npc.deleteMe();
				}
				return null;
			}
		}
		else if(npcId == 31523)
		{
			if(event.equalsIgnoreCase("reply_22"))
			{
				if(GetMemoState == 8 || GetMemoState == 9)
				{
					if(spawned_rune_ghost1 == 0)
					{
						st.set("spawned_rune_ghost1", String.valueOf(1), true);
						st.set("rune_ghost1_player_name", st.getPlayer().getName(), true);
						st.playSound("SkillSound5.horror_02");
						NpcInstance ghost1 = st.addSpawn(31524, 51432, -54570, -3136);
						Functions.npcSay(ghost1, "Who awoke me?");
						st.startQuestTimer("2101", 300000, ghost1);
						htmltext = "q_forest_stone1_q0023_02.htm";
					}
					else
					{
						st.playSound("SkillSound5.horror_02");
						htmltext = "q_forest_stone1_q0023_03.htm";
					}
				}
			}
			else if(event.equalsIgnoreCase("reply_26") && GetMemoState == 9)
			{
				st.setCond(8);
				st.set("truth_of_ridia", String.valueOf(10), true);
				st.giveItems(7149, 1);
				htmltext = "q_forest_stone1_q0023_06.htm";
			}
		}
		else if(npcId == 31530 && event.equalsIgnoreCase("reply_27") && GetMemoState == 11 && st.getQuestItemsCount(7149) >= 1)
		{
			st.setCond(10);
			st.giveItems(7150, 1);
			st.takeItems(7149, -1);
			st.playSound("ItemSound.itemdrop_weapon_spear");
			htmltext = "rust_box1_q0023_02.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		QuestState qs = st.getPlayer().getQuestState(_022_TragedyInVonHellmannForest.class);
		int GetMemoState = st.getInt("truth_of_ridia");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 31328)
					break;
				if(st.getPlayer().getLevel() >= 64 && qs != null && qs.isCompleted())
				{
					htmltext = "highpriest_innocentin_q0023_01.htm";
					break;
				}
				st.exitCurrentQuest(true);
				htmltext = "highpriest_innocentin_q0023_01a.htm";
				break;
			}
			case 2:
			{
				if(npcId == 31328)
				{
					if(GetMemoState == 1)
					{
						htmltext = "highpriest_innocentin_q0023_04.htm";
						break;
					}
					if(GetMemoState == 2)
					{
						htmltext = "highpriest_innocentin_q0023_08.htm";
						break;
					}
					if(GetMemoState == 5)
					{
						htmltext = "highpriest_innocentin_q0023_09.htm";
						break;
					}
					if(GetMemoState == 6)
					{
						htmltext = "highpriest_innocentin_q0023_14.htm";
						break;
					}
					if(GetMemoState == 7)
					{
						htmltext = "highpriest_innocentin_q0023_15.htm";
						break;
					}
					if(GetMemoState != 8)
						break;
					st.setCond(6);
					st.playSound("ItemSound.quest_middle");
					htmltext = "highpriest_innocentin_q0023_22.htm";
					break;
				}
				if(npcId == 31526)
				{
					if(GetMemoState == 2 && st.getQuestItemsCount(7149) >= 1)
					{
						st.setCond(3);
						st.playSound("ItemSound.quest_middle");
						htmltext = "broken_desk1_q0023_01.htm";
						break;
					}
					if(GetMemoState == 3)
					{
						htmltext = "broken_desk1_q0023_03.htm";
						break;
					}
					if(GetMemoState == 4 && st.getQuestItemsCount(7148) >= 1)
					{
						htmltext = "broken_desk1_q0023_07.htm";
						break;
					}
					if(GetMemoState == 4 && st.getQuestItemsCount(7064) >= 1)
					{
						htmltext = "broken_desk1_q0023_12.htm";
						break;
					}
					if(GetMemoState != 5 || st.getQuestItemsCount(7148) < 1 || st.getQuestItemsCount(7064) < 1)
						break;
					st.startQuestTimer("read_book", 120000, npc);
					htmltext = "broken_desk1_q0023_13.htm";
					break;
				}
				if(npcId == 31524)
				{
					if(GetMemoState == 8)
					{
						htmltext = "rune_ghost1_q0023_01.htm";
						break;
					}
					if(GetMemoState == 9 && st.getQuestItemsCount(7149) == 0)
					{
						htmltext = "rune_ghost1_q0023_05.htm";
						break;
					}
					if(GetMemoState != 9 && GetMemoState != 10 || st.getQuestItemsCount(7149) < 1)
						break;
					st.set("truth_of_ridia", String.valueOf(10), true);
					htmltext = "rune_ghost1_q0023_06.htm";
					break;
				}
				if(npcId == 31523)
				{
					if(GetMemoState == 8)
					{
						htmltext = "q_forest_stone1_q0023_01.htm";
						break;
					}
					if(GetMemoState == 9)
					{
						htmltext = "q_forest_stone1_q0023_04.htm";
						break;
					}
					if(GetMemoState != 10)
						break;
					htmltext = "q_forest_stone1_q0023_05.htm";
					break;
				}
				if(npcId == 31386)
				{
					if(GetMemoState == 10 && st.getQuestItemsCount(7149) >= 1)
					{
						st.setCond(9);
						st.set("truth_of_ridia", String.valueOf(11), true);
						st.playSound("ItemSound.quest_middle");
						htmltext = "day_violet_q0023_01.htm";
						break;
					}
					if(GetMemoState == 11 && st.getQuestItemsCount(7150) == 0)
					{
						htmltext = "day_violet_q0023_02.htm";
						break;
					}
					if(GetMemoState != 11 || st.getQuestItemsCount(7150) < 1)
						break;
					st.giveItems(57, 100000);
					st.takeItems(7150, -1);
					st.unset("truth_of_ridia");
					st.unset("spawned_rune_ghost1");
					st.playSound("ItemSound.quest_finish");
					st.exitCurrentQuest(false);
					htmltext = "day_violet_q0023_03.htm";
					break;
				}
				if(npcId != 31530)
					break;
				if(GetMemoState == 11 && st.getQuestItemsCount(7149) >= 1)
				{
					htmltext = "rust_box1_q0023_01.htm";
					break;
				}
				if(GetMemoState != 11 || st.getQuestItemsCount(7150) < 1)
					break;
				htmltext = "rust_box1_q0023_03.htm";
				break;
			}
			case 3:
			{
				if(npcId != 31386)
					break;
				htmltext = "day_violet_q0023_04.htm";
			}
		}
		return htmltext;
	}
}