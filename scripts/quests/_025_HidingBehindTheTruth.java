package quests;

import l2.commons.util.Rnd;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;

public class _025_HidingBehindTheTruth extends Quest implements ScriptFile
{
	private static final int falsepriest_agripel = 31348;
	private static final int falsepriest_benedict = 31349;
	private static final int broken_desk2 = 31533;
	private static final int broken_desk3 = 31534;
	private static final int broken_desk4 = 31535;
	private static final int q_forest_box1 = 31536;
	private static final int maid_of_ridia = 31532;
	private static final int shadow_hardin = 31522;
	private static final int q_forest_stone2 = 31531;
	private static final int q_lost_map = 7063;
	private static final int q_lost_contract = 7066;
	private static final int earing_of_blessing = 874;
	private static final int ring_of_blessing = 905;
	private static final int necklace_of_blessing = 936;
	private static final int q_lost_jewel_key = 7157;
	private static final int q_ridias_dress = 7155;
	private static final int q_triols_totem2 = 7156;
	private static final int q_triols_totem3 = 7158;
	private static final int triyol_zzolda = 27218;
	
	public _025_HidingBehindTheTruth()
	{
		super(false);
		addStartNpc(31349);
		addTalkId(31348, 31522, 31533, 31534, 31535, 31532, 31531, 31536);
		addAttackId(27218);
		addKillId(27218);
		addQuestItem(7158);
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
		int GetMemoState = st.getInt("man_behind_the_truth");
		int GetMemoStateEx = st.getInt("man_behind_the_truth_ex");
		int spawned_triyol_zzolda = st.getInt("spawned_triyol_zzolda");
		String triyol_zzolda_player_name = st.get("triyol_zzolda_player_name");
		if(event.equalsIgnoreCase("quest_accept"))
		{
			st.setCond(1);
			st.set("man_behind_the_truth", String.valueOf(1), true);
			st.set("spawned_triyol_zzolda", String.valueOf(0), true);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "falsepriest_benedict_q0025_03.htm";
		}
		else if(event.equalsIgnoreCase("reply_1"))
		{
			if(GetMemoState == 1 && st.getQuestItemsCount(7156) >= 1)
			{
				htmltext = "falsepriest_benedict_q0025_04.htm";
			}
			else if(GetMemoState == 1 && st.getQuestItemsCount(7156) == 0)
			{
				st.setCond(2);
				st.playSound("ItemSound.quest_middle");
				htmltext = "falsepriest_benedict_q0025_05.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_3"))
		{
			if(GetMemoState == 1 && st.getQuestItemsCount(7156) >= 1)
			{
				st.setCond(4);
				st.set("man_behind_the_truth", String.valueOf(2), true);
				st.playSound("ItemSound.quest_middle");
				htmltext = "falsepriest_benedict_q0025_10.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_4"))
		{
			if(GetMemoState == 2)
			{
				st.set("man_behind_the_truth", String.valueOf(3), true);
				st.takeItems(7156, -1);
				htmltext = "falsepriest_agripel_q0025_02.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_6"))
		{
			if(GetMemoState == 3)
			{
				st.setCond(5);
				st.set("man_behind_the_truth", String.valueOf(6), true);
				st.giveItems(7157, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "falsepriest_agripel_q0025_08.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_25"))
		{
			if(GetMemoState == 20 && st.getQuestItemsCount(7158) >= 1)
			{
				st.set("man_behind_the_truth", String.valueOf(21), true);
				st.takeItems(7158, -1);
				htmltext = "falsepriest_agripel_q0025_10.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_21"))
		{
			if(GetMemoState == 21)
			{
				st.set("man_behind_the_truth", String.valueOf(22), true);
				htmltext = "falsepriest_agripel_q0025_13.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_23"))
		{
			if(GetMemoState == 22)
			{
				st.setCond(17);
				st.set("man_behind_the_truth", String.valueOf(23), true);
				st.playSound("ItemSound.quest_middle");
				htmltext = "falsepriest_agripel_q0025_16.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_24"))
		{
			if(GetMemoState == 22)
			{
				st.setCond(18);
				st.set("man_behind_the_truth", String.valueOf(24), true);
				st.playSound("ItemSound.quest_middle");
				htmltext = "falsepriest_agripel_q0025_17.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_7"))
		{
			if(GetMemoState == 6 && st.getQuestItemsCount(7157) >= 1)
			{
				st.setCond(6);
				st.set("man_behind_the_truth", String.valueOf(7), true);
				st.set("man_behind_the_truth_ex", String.valueOf(20), true);
				st.playSound("ItemSound.quest_middle");
				htmltext = "shadow_hardin_q0025_04.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_17"))
		{
			if(GetMemoState == 16)
			{
				st.set("man_behind_the_truth", String.valueOf(19), true);
				htmltext = "shadow_hardin_q0025_10.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_19"))
		{
			if(GetMemoState == 19)
			{
				st.setCond(16);
				st.set("man_behind_the_truth", String.valueOf(20), true);
				st.playSound("ItemSound.quest_middle");
				htmltext = "shadow_hardin_q0025_13.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_23a"))
		{
			if(GetMemoState == 24)
			{
				st.giveItems(874, 1);
				st.giveItems(936, 1);
				st.takeItems(7063, -1);
				st.addExpAndSp(572277, 53750);
				st.unset("man_behind_the_truth");
				st.unset("man_behind_the_truth_ex");
				st.exitCurrentQuest(false);
				st.playSound("ItemSound.quest_finish");
				htmltext = "shadow_hardin_q0025_16.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_8"))
		{
			int i0 = GetMemoState % 1000;
			if(i0 >= 100)
			{
				htmltext = "broken_desk2_q0025_03.htm";
			}
			else if(Rnd.get(60) > GetMemoStateEx)
			{
				st.set("man_behind_the_truth_ex", String.valueOf(GetMemoStateEx + 20), true);
				st.set("man_behind_the_truth", String.valueOf(GetMemoState + 100), true);
				htmltext = "broken_desk2_q0025_04.htm";
			}
			else
			{
				st.set("man_behind_the_truth", String.valueOf(8), true);
				htmltext = "broken_desk2_q0025_05.htm";
				st.playSound("AmdSound.dd_horror_02");
			}
		}
		else if(event.equalsIgnoreCase("reply_9"))
		{
			if(GetMemoState == 8 && st.getQuestItemsCount(7158) == 0)
			{
				if(spawned_triyol_zzolda == 0)
				{
					st.setCond(7);
					st.set("spawned_triyol_zzolda", String.valueOf(1), true);
					st.set("triyol_zzolda_player_name", st.getPlayer().getName(), true);
					NpcInstance zzolda = st.addSpawn(27218, 47142, -35941, -1623);
					if(zzolda != null)
					{
						zzolda.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, st.getPlayer(), 20000);
					}
					st.startQuestTimer("2501", 500, zzolda);
					st.startQuestTimer("2502", 120000, zzolda);
					htmltext = "broken_desk2_q0025_07.htm";
				}
				else
				{
					htmltext = triyol_zzolda_player_name == st.getPlayer().getName() ? "broken_desk2_q0025_08.htm" : "broken_desk2_q0025_09.htm";
				}
			}
			if(GetMemoState == 8 && st.getQuestItemsCount(7158) >= 1)
			{
				htmltext = "broken_desk2_q0025_10.htm";
			}
		}
		else
		{
			if(event.equalsIgnoreCase("2501"))
			{
				Functions.npcSay(npc, "That box was sealed by my master, " + st.getPlayer().getName() + "! Don't touch it!");
				return null;
			}
			if(event.equalsIgnoreCase("2502"))
			{
				st.unset("spawned_triyol_zzolda");
				if(npc != null)
				{
					npc.deleteMe();
				}
				return null;
			}
			if(event.equalsIgnoreCase("reply_10"))
			{
				if(GetMemoState == 8 && st.getQuestItemsCount(7158) >= 1 && st.getQuestItemsCount(7157) >= 1)
				{
					st.setCond(9);
					st.set("man_behind_the_truth", String.valueOf(9), true);
					st.giveItems(7066, 1);
					st.takeItems(7157, -1);
					htmltext = "broken_desk2_q0025_11.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_8a"))
			{
				int i0 = GetMemoState % 10000;
				if(i0 >= 1000)
				{
					htmltext = "broken_desk3_q0025_03.htm";
				}
				else if(Rnd.get(60) > GetMemoStateEx)
				{
					st.set("man_behind_the_truth_ex", String.valueOf(GetMemoStateEx + 20), true);
					st.set("man_behind_the_truth", String.valueOf(GetMemoState + 1000), true);
					htmltext = "broken_desk3_q0025_04.htm";
				}
				else
				{
					st.set("man_behind_the_truth", String.valueOf(8), true);
					htmltext = "broken_desk3_q0025_05.htm";
					st.playSound("AmdSound.dd_horror_02");
				}
			}
			else if(event.equalsIgnoreCase("reply_9a"))
			{
				if(GetMemoState == 8 && st.getQuestItemsCount(7158) == 0)
				{
					if(spawned_triyol_zzolda == 0)
					{
						st.setCond(7);
						st.set("spawned_triyol_zzolda", String.valueOf(1), true);
						st.set("triyol_zzolda_player_name", st.getPlayer().getName(), true);
						NpcInstance zzolda = st.addSpawn(27218, 50055, -47020, -3396);
						if(zzolda != null)
						{
							zzolda.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, st.getPlayer(), 20000);
						}
						st.startQuestTimer("2501", 500, zzolda);
						st.startQuestTimer("2502", 120000, zzolda);
						htmltext = "broken_desk3_q0025_07.htm";
					}
					else
					{
						htmltext = triyol_zzolda_player_name == st.getPlayer().getName() ? "broken_desk3_q0025_08.htm" : "broken_desk3_q0025_09.htm";
					}
				}
				if(GetMemoState == 8 && st.getQuestItemsCount(7158) >= 1)
				{
					htmltext = "broken_desk3_q0025_10.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_10a"))
			{
				if(GetMemoState == 8 && st.getQuestItemsCount(7158) >= 1 && st.getQuestItemsCount(7157) >= 1)
				{
					st.setCond(9);
					st.set("man_behind_the_truth", String.valueOf(9), true);
					st.giveItems(7066, 1);
					st.takeItems(7157, -1);
					htmltext = "broken_desk3_q0025_11.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_8b"))
			{
				if(GetMemoState >= 10000)
				{
					htmltext = "broken_desk4_q0025_03.htm";
				}
				else if(Rnd.get(60) > GetMemoStateEx)
				{
					st.set("man_behind_the_truth_ex", String.valueOf(GetMemoStateEx + 20), true);
					st.set("man_behind_the_truth", String.valueOf(GetMemoState + 10000), true);
					htmltext = "broken_desk4_q0025_04.htm";
				}
				else
				{
					st.set("man_behind_the_truth", String.valueOf(8), true);
					htmltext = "broken_desk4_q0025_05.htm";
					st.playSound("AmdSound.dd_horror_02");
				}
			}
			else if(event.equalsIgnoreCase("reply_9b"))
			{
				if(GetMemoState == 8 && st.getQuestItemsCount(7158) == 0)
				{
					if(spawned_triyol_zzolda == 0)
					{
						st.setCond(7);
						st.set("spawned_triyol_zzolda", String.valueOf(1), true);
						st.set("triyol_zzolda_player_name", st.getPlayer().getName(), true);
						NpcInstance zzolda = st.addSpawn(27218, 59712, -47568, -2720);
						if(zzolda != null)
						{
							zzolda.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, st.getPlayer(), 20000);
						}
						st.startQuestTimer("2501", 500, zzolda);
						st.startQuestTimer("2502", 120000, zzolda);
						htmltext = "broken_desk4_q0025_07.htm";
					}
					else
					{
						htmltext = triyol_zzolda_player_name == st.getPlayer().getName() ? "broken_desk4_q0025_08.htm" : "broken_desk4_q0025_09.htm";
					}
				}
				if(GetMemoState == 8 && st.getQuestItemsCount(7158) >= 1)
				{
					htmltext = "broken_desk4_q0025_10.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_10b"))
			{
				if(GetMemoState == 8 && st.getQuestItemsCount(7158) >= 1 && st.getQuestItemsCount(7157) >= 1)
				{
					st.setCond(9);
					st.set("man_behind_the_truth", String.valueOf(9), true);
					st.giveItems(7066, 1);
					st.takeItems(7157, -1);
					htmltext = "broken_desk4_q0025_11.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_11"))
			{
				if(GetMemoState == 9 && st.getQuestItemsCount(7066) >= 1)
				{
					st.takeItems(7066, -1);
					st.set("man_behind_the_truth", String.valueOf(10), true);
					htmltext = "maid_of_ridia_q0025_02.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_13"))
			{
				if(GetMemoState == 10)
				{
					st.setCond(11);
					st.set("man_behind_the_truth", String.valueOf(11), true);
					st.playSound("SkillSound5.horror_01");
					htmltext = "maid_of_ridia_q0025_07.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_14"))
			{
				if(GetMemoState == 13)
				{
					if(GetMemoStateEx <= 3)
					{
						st.set("man_behind_the_truth_ex", String.valueOf(GetMemoStateEx + 1), true);
						st.playSound("ChrSound.FDElf_Cry");
						htmltext = "maid_of_ridia_q0025_11.htm";
					}
					else
					{
						st.set("man_behind_the_truth", String.valueOf(14), true);
						htmltext = "maid_of_ridia_q0025_12.htm";
					}
				}
			}
			else if(event.equalsIgnoreCase("reply_15"))
			{
				if(GetMemoState == 14)
				{
					st.set("man_behind_the_truth", String.valueOf(15), true);
					htmltext = "maid_of_ridia_q0025_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_16"))
			{
				if(GetMemoState == 15)
				{
					st.setCond(15);
					st.set("man_behind_the_truth", String.valueOf(16), true);
					htmltext = "maid_of_ridia_q0025_21.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_22"))
			{
				if(GetMemoState == 23)
				{
					st.giveItems(874, 1);
					st.giveItems(905, 2);
					st.takeItems(7063, -1);
					st.addExpAndSp(572277, 53750);
					st.playSound("ItemSound.quest_finish");
					st.unset("man_behind_the_truth");
					st.unset("man_behind_the_truth_ex");
					htmltext = "maid_of_ridia_q0025_25.htm";
					st.exitCurrentQuest(false);
				}
			}
			else if(event.equalsIgnoreCase("reply_1a"))
			{
				if(GetMemoState == 11)
				{
					st.setCond(12);
					NpcInstance box1 = st.addSpawn(31536, 60104, -35820, -681);
					st.startQuestTimer("2503", 20000, box1);
					st.playSound("ItemSound.quest_middle");
					htmltext = "q_forest_stone2_q0025_02.htm";
				}
			}
			else
			{
				if(event.equalsIgnoreCase("2503"))
				{
					if(npc != null)
					{
						npc.deleteMe();
					}
					return null;
				}
				if(event.equalsIgnoreCase("2504"))
				{
					if(npc != null)
					{
						npc.deleteMe();
					}
					return null;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		QuestState qs = st.getPlayer().getQuestState(_024_InhabitantsOfTheForestOfTheDead.class);
		int GetMemoState = st.getInt("man_behind_the_truth");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 31349)
					break;
				if(qs != null && qs.isCompleted() && st.getPlayer().getLevel() >= 66)
				{
					htmltext = "falsepriest_benedict_q0025_01.htm";
					break;
				}
				htmltext = "falsepriest_benedict_q0025_02.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId == 31349)
				{
					if(GetMemoState == 1)
					{
						htmltext = "falsepriest_benedict_q0025_03a.htm";
						break;
					}
					if(GetMemoState != 2)
						break;
					htmltext = "falsepriest_benedict_q0025_11.htm";
					break;
				}
				if(npcId == 31348)
				{
					if(GetMemoState == 2)
					{
						htmltext = "falsepriest_agripel_q0025_01.htm";
						break;
					}
					if(GetMemoState == 3)
					{
						htmltext = "falsepriest_agripel_q0025_03.htm";
						break;
					}
					if(GetMemoState == 6)
					{
						htmltext = "falsepriest_agripel_q0025_08a.htm";
						break;
					}
					if(GetMemoState == 20 && st.getQuestItemsCount(7158) >= 1)
					{
						htmltext = "falsepriest_agripel_q0025_09.htm";
						break;
					}
					if(GetMemoState == 21)
					{
						htmltext = "falsepriest_agripel_q0025_10a.htm";
						break;
					}
					if(GetMemoState == 22)
					{
						htmltext = "falsepriest_agripel_q0025_15.htm";
						break;
					}
					if(GetMemoState == 23)
					{
						htmltext = "falsepriest_agripel_q0025_18.htm";
						break;
					}
					if(GetMemoState != 24)
						break;
					htmltext = "falsepriest_agripel_q0025_19.htm";
					break;
				}
				if(npcId == 31522)
				{
					if(GetMemoState == 1 && st.getQuestItemsCount(7156) == 0)
					{
						st.setCond(3);
						st.giveItems(7156, 1);
						st.playSound("ItemSound.quest_middle");
						htmltext = "shadow_hardin_q0025_01.htm";
						break;
					}
					if(GetMemoState == 1 && st.getQuestItemsCount(7156) >= 1)
					{
						htmltext = "shadow_hardin_q0025_02.htm";
						break;
					}
					if(GetMemoState == 6 && st.getQuestItemsCount(7157) >= 1)
					{
						htmltext = "shadow_hardin_q0025_03.htm";
						break;
					}
					if(GetMemoState % 100 == 7)
					{
						htmltext = "shadow_hardin_q0025_05.htm";
						break;
					}
					if(GetMemoState == 9 && st.getQuestItemsCount(7066) >= 1)
					{
						st.setCond(10);
						st.playSound("ItemSound.quest_middle");
						htmltext = "shadow_hardin_q0025_06.htm";
						break;
					}
					if(GetMemoState == 16)
					{
						htmltext = "shadow_hardin_q0025_06a.htm";
						break;
					}
					if(GetMemoState == 19)
					{
						htmltext = "shadow_hardin_q0025_12.htm";
						break;
					}
					if(GetMemoState == 20)
					{
						htmltext = "shadow_hardin_q0025_14.htm";
						break;
					}
					if(GetMemoState == 24)
					{
						htmltext = "shadow_hardin_q0025_15.htm";
						break;
					}
					if(GetMemoState != 23)
						break;
					htmltext = "shadow_hardin_q0025_15a.htm";
					break;
				}
				if(npcId == 31533)
				{
					if(GetMemoState % 100 == 7)
					{
						htmltext = "broken_desk2_q0025_01.htm";
						break;
					}
					if(GetMemoState % 100 >= 9)
					{
						htmltext = "broken_desk2_q0025_02.htm";
						break;
					}
					if(GetMemoState != 8)
						break;
					htmltext = "broken_desk2_q0025_06.htm";
					break;
				}
				if(npcId == 31534)
				{
					if(GetMemoState % 100 == 7)
					{
						htmltext = "broken_desk3_q0025_01.htm";
						break;
					}
					if(GetMemoState % 100 >= 9)
					{
						htmltext = "broken_desk3_q0025_02.htm";
						break;
					}
					if(GetMemoState != 8)
						break;
					htmltext = "broken_desk3_q0025_06.htm";
					break;
				}
				if(npcId == 31535)
				{
					if(GetMemoState % 100 == 7)
					{
						htmltext = "broken_desk4_q0025_01.htm";
						break;
					}
					if(GetMemoState % 100 >= 9)
					{
						htmltext = "broken_desk4_q0025_02.htm";
						break;
					}
					if(GetMemoState != 8)
						break;
					htmltext = "broken_desk4_q0025_06.htm";
					break;
				}
				if(npcId == 31532)
				{
					if(GetMemoState == 9 && st.getQuestItemsCount(7066) >= 1)
					{
						htmltext = "maid_of_ridia_q0025_01.htm";
						break;
					}
					if(GetMemoState == 10)
					{
						htmltext = "maid_of_ridia_q0025_03.htm";
						break;
					}
					if(GetMemoState == 11)
					{
						st.playSound("SkillSound5.horror_01");
						htmltext = "maid_of_ridia_q0025_08.htm";
						break;
					}
					if(GetMemoState == 12 && st.getQuestItemsCount(7155) >= 1)
					{
						st.setCond(14);
						st.takeItems(7155, -1);
						st.set("man_behind_the_truth", String.valueOf(13), true);
						st.playSound("ItemSound.quest_middle");
						htmltext = "maid_of_ridia_q0025_09.htm";
						break;
					}
					if(GetMemoState == 13)
					{
						st.set("man_behind_the_truth_ex", String.valueOf(0), true);
						st.playSound("ChrSound.FDElf_Cry");
						htmltext = "maid_of_ridia_q0025_10.htm";
						break;
					}
					if(GetMemoState == 14)
					{
						htmltext = "maid_of_ridia_q0025_13.htm";
						break;
					}
					if(GetMemoState == 15)
					{
						htmltext = "maid_of_ridia_q0025_18.htm";
						break;
					}
					if(GetMemoState == 16)
					{
						htmltext = "maid_of_ridia_q0025_22.htm";
						break;
					}
					if(GetMemoState == 23)
					{
						htmltext = "maid_of_ridia_q0025_23.htm";
						break;
					}
					if(GetMemoState != 24)
						break;
					htmltext = "maid_of_ridia_q0025_24.htm";
					break;
				}
				if(npcId == 31531)
				{
					if(GetMemoState == 11)
					{
						htmltext = "q_forest_stone2_q0025_01.htm";
						break;
					}
					if(GetMemoState != 12)
						break;
					htmltext = "q_forest_stone2_q0025_03.htm";
					break;
				}
				if(npcId != 31536 || GetMemoState != 11)
					break;
				st.setCond(13);
				st.set("man_behind_the_truth", String.valueOf(12), true);
				st.giveItems(7155, 1);
				st.playSound("ItemSound.quest_middle");
				st.startQuestTimer("2503", 3000, npc);
				htmltext = "q_forest_box1_q0025_01.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int GetMemoState = st.getInt("man_behind_the_truth");
		String triyol_zzolda_player_name = st.get("triyol_zzolda_player_name");
		double triyol_zzolda_hp_precent = npc.getCurrentHpPercents();
		if(npcId == 27218 && triyol_zzolda_hp_precent <= 30.0 && GetMemoState == 8 && st.getQuestItemsCount(7158) == 0 && triyol_zzolda_player_name == st.getPlayer().getName())
		{
			st.setCond(8);
			st.giveItems(7158, 1);
			st.playSound("ItemSound.quest_itemget");
			Functions.npcSay(npc, "You've ended my immortal life! You're protected by the feudal lord, aren't you?");
			st.unset("spawned_triyol_zzolda");
			st.unset("triyol_zzolda_player_name");
			if(!st.isRunningQuestTimer("2502") && npc != null)
			{
				npc.deleteMe();
			}
		}
		return null;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == 27218)
		{
			st.unset("spawned_triyol_zzolda");
		}
		st.unset("triyol_zzolda_player_name");
		return null;
	}
}