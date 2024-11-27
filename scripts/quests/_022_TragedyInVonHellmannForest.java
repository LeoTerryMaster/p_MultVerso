package quests;

import l2.commons.util.Rnd;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;

public class _022_TragedyInVonHellmannForest extends Quest implements ScriptFile
{
	private static final int umul = 31527;
	private static final int grandmagister_tifaren = 31334;
	private static final int highpriest_innocentin = 31328;
	private static final int rune_ghost2 = 31528;
	private static final int rune_ghost3 = 31529;
	private static final int ghost_of_umul = 27217;
	private static final int q_cross_of_einhasad2 = 7141;
	private static final int q_lost_elfs_skull = 7142;
	private static final int q_letter_of_innocentin = 7143;
	private static final int q_calling_treasure1 = 7144;
	private static final int q_calling_treasure2 = 7145;
	private static final int q_seal_report_box = 7146;
	private static final int q_report_box = 7147;
	private static final int oppressed_one = 21553;
	private static final int oppressed_one_a = 21554;
	private static final int agent_of_slaughter = 21555;
	private static final int agent_of_slaughter_a = 21556;
	private static final int sacrificed_one = 21561;
	
	public _022_TragedyInVonHellmannForest()
	{
		super(false);
		addStartNpc(31334);
		addTalkId(31334, 31528, 31328, 31529, 31527);
		addAttackId(27217);
		addKillId(27217);
		addKillId(21553, 21554, 21555, 21556, 21561);
		addQuestItem(7142);
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
		int GetMemoState = st.getInt("tragedy_of_helman_forest");
		int spawned_rune_ghost2 = st.getInt("spawned_rune_ghost2");
		int spawned_ghost_of_umul = st.getInt("spawned_ghost_of_umul");
		if(event.equalsIgnoreCase("quest_accept"))
		{
			st.setCond(1);
			st.set("tragedy_of_helman_forest", String.valueOf(1), true);
			st.set("spawned_rune_ghost2", String.valueOf(0), true);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "grandmagister_tifaren_q0022_04.htm";
		}
		else if(event.equalsIgnoreCase("reply_2") && GetMemoState == 1)
		{
			if(st.getQuestItemsCount(7141) >= 1)
			{
				htmltext = "grandmagister_tifaren_q0022_06.htm";
			}
			else
			{
				st.setCond(2);
				st.playSound("ItemSound.quest_middle");
				htmltext = "grandmagister_tifaren_q0022_07.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_3") && GetMemoState == 1)
		{
			if(st.getQuestItemsCount(7141) >= 1)
			{
				st.setCond(4);
				st.set("tragedy_of_helman_forest", String.valueOf(2), true);
				st.playSound("ItemSound.quest_middle");
				htmltext = "grandmagister_tifaren_q0022_08.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_5"))
		{
			if(GetMemoState == 2 && st.getQuestItemsCount(7141) >= 1 && st.getQuestItemsCount(7142) >= 1)
			{
				if(spawned_rune_ghost2 == 0)
				{
					st.setCond(7);
					st.set("spawned_rune_ghost2", String.valueOf(1), true);
					st.set("tragedy_of_helman_forest", String.valueOf(4), true);
					st.set("rune_ghost2_player_name", st.getPlayer().getName(), true);
					st.takeItems(7142, 1);
					st.playSound("ItemSound.quest_middle");
					NpcInstance ghost2 = st.addSpawn(31528, 38354, -49777, -1128);
					Functions.npcSay(ghost2, "Did you call me, " + st.getPlayer().getName());
					st.startQuestTimer("despawn_rune_ghost2", 120000, ghost2);
					htmltext = "grandmagister_tifaren_q0022_13.htm";
				}
				else
				{
					st.setCond(6);
					st.playSound("ItemSound.quest_middle");
					htmltext = "grandmagister_tifaren_q0022_14.htm";
				}
			}
			else if(GetMemoState == 4 && st.getQuestItemsCount(7141) >= 1)
			{
				if(spawned_rune_ghost2 == 0)
				{
					NpcInstance ghost2 = st.addSpawn(31528, 38354, -49777, -1128);
					st.startQuestTimer("despawn_rune_ghost2", 120000, ghost2);
					htmltext = "grandmagister_tifaren_q0022_13.htm";
				}
				else
				{
					st.setCond(6);
					st.playSound("ItemSound.quest_middle");
					htmltext = "grandmagister_tifaren_q0022_14.htm";
				}
			}
		}
		else
		{
			if(event.equalsIgnoreCase("despawn_rune_ghost2"))
			{
				Functions.npcSay(npc, "I'm confused! Maybe it's time to go back.");
				st.unset("spawned_rune_ghost2");
				if(npc != null)
				{
					npc.deleteMe();
				}
				if(!st.isRunningQuestTimer("despawn_rune_ghost2_2"))
				{
					st.cancelQuestTimer("despawn_rune_ghost2_2");
				}
				return null;
			}
			if(event.equalsIgnoreCase("despawn_rune_ghost2_2"))
			{
				Functions.npcSay(npc, "My train of thought is chaotic. It goes back to the beginning of time...");
				st.unset("spawned_rune_ghost2");
				if(npc != null)
				{
					npc.deleteMe();
				}
				if(!st.isRunningQuestTimer("despawn_rune_ghost2"))
				{
					st.cancelQuestTimer("despawn_rune_ghost2");
				}
				return null;
			}
			if(event.equalsIgnoreCase("reply_6"))
			{
				st.playSound("AmbSound.d_horror_03");
				htmltext = "rune_ghost2_q0022_04.htm";
			}
			else if(event.equalsIgnoreCase("reply_7"))
			{
				st.setCond(8);
				st.set("tragedy_of_helman_forest", String.valueOf(5), true);
				st.startQuestTimer("despawn_rune_ghost2_2", 3000, npc);
				htmltext = "rune_ghost2_q0022_08.htm";
			}
			else if(event.equalsIgnoreCase("reply_8") && GetMemoState == 5)
			{
				st.takeItems(7141, -1);
				st.set("tragedy_of_helman_forest", String.valueOf(6), true);
				htmltext = "highpriest_innocentin_q0022_03.htm";
			}
			else if(event.equalsIgnoreCase("reply_10") && GetMemoState == 6)
			{
				st.setCond(9);
				st.set("tragedy_of_helman_forest", String.valueOf(7), true);
				st.giveItems(7143, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "highpriest_innocentin_q0022_09.htm";
			}
			else if(event.equalsIgnoreCase("reply_17") && GetMemoState == 12 && st.getQuestItemsCount(7147) >= 1)
			{
				st.setCond(15);
				st.set("tragedy_of_helman_forest", String.valueOf(13), true);
				st.takeItems(7147, -1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "highpriest_innocentin_q0022_11.htm";
			}
			else if(event.equalsIgnoreCase("reply_19") && GetMemoState == 13)
			{
				st.setCond(16);
				st.set("tragedy_of_helman_forest", String.valueOf(14), true);
				st.playSound("ItemSound.quest_middle");
				htmltext = "highpriest_innocentin_q0022_19.htm";
			}
			else if(event.equalsIgnoreCase("reply_12") && GetMemoState == 7 && st.getQuestItemsCount(7143) >= 1)
			{
				st.takeItems(7143, -1);
				st.set("tragedy_of_helman_forest", String.valueOf(8), true);
				htmltext = "rune_ghost3_q0022_03.htm";
			}
			else if(event.equalsIgnoreCase("reply_14") && GetMemoState == 8)
			{
				st.set("tragedy_of_helman_forest", String.valueOf(9), true);
				htmltext = "rune_ghost3_q0022_08.htm";
			}
			else if(event.equalsIgnoreCase("reply_15") && GetMemoState == 9)
			{
				st.setCond(10);
				st.set("tragedy_of_helman_forest", String.valueOf(10), true);
				st.giveItems(7144, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "rune_ghost3_q0022_11.htm";
			}
			else if(event.equalsIgnoreCase("reply_16"))
			{
				if(spawned_ghost_of_umul == 0)
				{
					st.set("spawned_ghost_of_umul", String.valueOf(1), true);
					st.set("umul", String.valueOf(0), true);
					st.playSound("SkillSound3.antaras_fear");
					NpcInstance ghost_umul = st.addSpawn(27217, 34706, -54590, -2054);
					if(ghost_umul != null)
					{
						ghost_umul.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, st.getPlayer(), 20000);
					}
					st.startQuestTimer("ghost_of_umul_1", 90000, ghost_umul);
					st.startQuestTimer("despawn_ghost_of_umul", 120000, ghost_umul);
					htmltext = "umul_q0022_02.htm";
				}
				else
				{
					htmltext = "umul_q0022_03.htm";
				}
			}
			else
			{
				if(event.equalsIgnoreCase("ghost_of_umul_1"))
				{
					st.set("umul", String.valueOf(1), true);
					return null;
				}
				if(event.equalsIgnoreCase("despawn_ghost_of_umul"))
				{
					st.unset("spawned_ghost_of_umul");
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
		QuestState qs = st.getPlayer().getQuestState(_021_HiddenTruth.class);
		int GetMemoState = st.getInt("tragedy_of_helman_forest");
		int spawned_rune_ghost2 = st.getInt("spawned_rune_ghost2");
		String rune_ghost2_player_name = st.get("rune_ghost2_player_name");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 31334)
					break;
				if(qs != null && qs.isCompleted() && st.getPlayer().getLevel() >= 63)
				{
					htmltext = "grandmagister_tifaren_q0022_01.htm";
					break;
				}
				htmltext = "grandmagister_tifaren_q0022_03.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId == 31334)
				{
					if(GetMemoState == 1)
					{
						htmltext = "grandmagister_tifaren_q0022_05.htm";
						break;
					}
					if(GetMemoState == 2)
					{
						if(st.getQuestItemsCount(7141) >= 1 && st.getQuestItemsCount(7142) > 0)
						{
							if(spawned_rune_ghost2 == 0)
							{
								htmltext = "grandmagister_tifaren_q0022_10.htm";
								break;
							}
							htmltext = "grandmagister_tifaren_q0022_11.htm";
							break;
						}
						htmltext = "grandmagister_tifaren_q0022_09.htm";
						break;
					}
					if(GetMemoState == 4 && st.getQuestItemsCount(7141) >= 1)
					{
						if(spawned_rune_ghost2 == 1)
						{
							if(rune_ghost2_player_name == st.getPlayer().getName())
							{
								htmltext = "grandmagister_tifaren_q0022_15.htm";
								break;
							}
							st.setCond(6);
							htmltext = "grandmagister_tifaren_q0022_16.htm";
							st.playSound("ItemSound.quest_middle");
							break;
						}
						htmltext = "grandmagister_tifaren_q0022_17.htm";
						break;
					}
					if(GetMemoState != 5 || st.getQuestItemsCount(7141) < 1)
						break;
					htmltext = "grandmagister_tifaren_q0022_19.htm";
					break;
				}
				if(npcId == 31528)
				{
					if(rune_ghost2_player_name != st.getPlayer().getName())
					{
						htmltext = "rune_ghost2_q0022_01a.htm";
						st.playSound("AmbSound.d_horror_15");
						break;
					}
					if(rune_ghost2_player_name != st.getPlayer().getName())
						break;
					htmltext = "rune_ghost2_q0022_01.htm";
					st.playSound("AmbSound.d_horror_15");
					break;
				}
				if(npcId == 31328)
				{
					if(GetMemoState < 5 && st.getQuestItemsCount(7141) == 0)
					{
						st.setCond(3);
						st.giveItems(7141, 1);
						htmltext = "highpriest_innocentin_q0022_01.htm";
						st.playSound("ItemSound.quest_middle");
						break;
					}
					if(GetMemoState < 5 && st.getQuestItemsCount(7141) >= 1)
					{
						htmltext = "highpriest_innocentin_q0022_01b.htm";
						break;
					}
					if(GetMemoState == 5)
					{
						htmltext = "highpriest_innocentin_q0022_02.htm";
						break;
					}
					if(GetMemoState == 6)
					{
						htmltext = "highpriest_innocentin_q0022_04.htm";
						break;
					}
					if(GetMemoState == 7)
					{
						htmltext = "highpriest_innocentin_q0022_09a.htm";
						break;
					}
					if(GetMemoState == 12 && st.getQuestItemsCount(7147) >= 1)
					{
						htmltext = "highpriest_innocentin_q0022_10.htm";
						break;
					}
					if(GetMemoState == 13)
					{
						htmltext = "highpriest_innocentin_q0022_12.htm";
						break;
					}
					if(GetMemoState == 14 && st.getPlayer().getLevel() >= 64)
					{
						st.unset("rune_ghost2_player_name");
						st.unset("tragedy_of_helman_forest");
						st.addExpAndSp(345966, 31578);
						st.exitCurrentQuest(false);
						st.playSound("ItemSound.quest_finish");
						htmltext = "highpriest_innocentin_q0022_20.htm";
						break;
					}
					if(GetMemoState != 14 || st.getPlayer().getLevel() >= 64)
						break;
					st.unset("rune_ghost2_player_name");
					st.unset("tragedy_of_helman_forest");
					st.addExpAndSp(345966, 31578);
					st.exitCurrentQuest(false);
					st.playSound("ItemSound.quest_finish");
					htmltext = "highpriest_innocentin_q0022_21.htm";
					break;
				}
				if(npcId == 31529)
				{
					if(GetMemoState == 7 && st.getQuestItemsCount(7143) >= 1)
					{
						htmltext = "rune_ghost3_q0022_01.htm";
						break;
					}
					if(GetMemoState == 8)
					{
						htmltext = "rune_ghost3_q0022_03a.htm";
						break;
					}
					if(GetMemoState == 9)
					{
						htmltext = "rune_ghost3_q0022_10.htm";
						break;
					}
					if(GetMemoState == 11 && st.getQuestItemsCount(7144) >= 1)
					{
						htmltext = "rune_ghost3_q0022_14.htm";
						break;
					}
					if(GetMemoState == 11 && st.getQuestItemsCount(7145) >= 1 && st.getQuestItemsCount(7146) == 0)
					{
						st.setCond(12);
						st.playSound("ItemSound.quest_middle");
						htmltext = "rune_ghost3_q0022_15.htm";
						break;
					}
					if(GetMemoState == 11 && st.getQuestItemsCount(7145) >= 1 && st.getQuestItemsCount(7146) >= 1)
					{
						st.setCond(14);
						st.set("tragedy_of_helman_forest", String.valueOf(12), true);
						st.giveItems(7147, 1);
						st.takeItems(7146, -1);
						st.takeItems(7145, -1);
						st.playSound("ItemSound.quest_middle");
						htmltext = "rune_ghost3_q0022_16.htm";
						break;
					}
					if(GetMemoState != 12 || st.getQuestItemsCount(7147) < 1)
						break;
					htmltext = "rune_ghost3_q0022_17.htm";
					break;
				}
				if(npcId != 31527)
					break;
				if((GetMemoState == 10 || GetMemoState == 11) && st.getQuestItemsCount(7144) >= 1)
				{
					st.playSound("AmbSound.dd_horror_01");
					htmltext = "umul_q0022_01.htm";
					break;
				}
				if(GetMemoState == 11 && st.getQuestItemsCount(7145) >= 1 && st.getQuestItemsCount(7146) == 0)
				{
					st.setCond(13);
					st.giveItems(7146, 1);
					st.playSound("ItemSound.quest_middle");
					htmltext = "umul_q0022_04.htm";
					break;
				}
				if(GetMemoState != 11 || st.getQuestItemsCount(7145) < 1 || st.getQuestItemsCount(7146) < 1)
					break;
				htmltext = "umul_q0022_05.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int GetMemoState = st.getInt("tragedy_of_helman_forest");
		int spawned_ghost_of_umul = st.getInt("spawned_ghost_of_umul");
		if(npcId == 27217)
		{
			if(GetMemoState == 10 && st.getQuestItemsCount(7144) >= 1)
			{
				st.set("tragedy_of_helman_forest", String.valueOf(11), true);
			}
			else if(GetMemoState == 11 && st.getQuestItemsCount(7144) >= 1 && Rnd.get(100) < 5 && spawned_ghost_of_umul == 1)
			{
				st.setCond(11);
				st.takeItems(7144, -1);
				st.giveItems(7145, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int GetMemoState = st.getInt("tragedy_of_helman_forest");
		if(npcId == 27217)
		{
			st.unset("spawned_ghost_of_umul");
		}
		else if((npcId == 21553 || npcId == 21554 || npcId == 21555 || npcId == 21556 || npcId == 21561) && GetMemoState == 2 && st.getQuestItemsCount(7142) == 0 && Rnd.get(100) < 10)
		{
			st.giveItems(7142, 1);
			st.playSound("ItemSound.quest_itemget");
			st.setCond(5);
		}
		return null;
	}
}