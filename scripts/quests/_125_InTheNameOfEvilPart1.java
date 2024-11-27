package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.tables.SkillTable;

public class _125_InTheNameOfEvilPart1 extends Quest implements ScriptFile
{
	private static final int mushika = 32114;
	private static final int shaman_caracawe = 32117;
	private static final int ulu_kaimu_stone = 32119;
	private static final int balu_kaimu_stone = 32120;
	private static final int jiuta_kaimu_stone = 32121;
	private static final int ornithomimus_leader = 22200;
	private static final int ornithomimus = 22201;
	private static final int ornithomimus_s = 22202;
	private static final int deinonychus_leader = 22203;
	private static final int deinonychus = 22204;
	private static final int deinonychus_s = 22205;
	private static final int ornithomimus_n = 22219;
	private static final int deinonychus_n = 22220;
	private static final int ornithomimus_leader2 = 22224;
	private static final int deinonychus_leader2 = 22225;
	private static final int q_claw_of_ornithomimus = 8779;
	private static final int q_bone_of_deinonychus = 8780;
	private static final int q_muzzle_pattem = 8781;
	private static final int q_piece_of_gazk = 8782;
	
	public _125_InTheNameOfEvilPart1()
	{
		super(false);
		addStartNpc(32114);
		addTalkId(32117, 32119, 32120, 32121);
		addKillId(22200, 22201, 22202, 22203, 22204, 22205, 22219, 22220, 22224, 22225);
		addQuestItem(8779, 8780);
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
		int GetMemoState = st.getInt("name_of_cruel_god_one");
		int GetMemoStateEx = st.getInt("name_of_cruel_god_one_ex");
		if(event.equalsIgnoreCase("quest_accept"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "mushika_q0125_08.htm";
		}
		else if(event.equalsIgnoreCase("reply_5"))
		{
			st.set("name_of_cruel_god_one", String.valueOf(1), true);
			htmltext = "mushika_q0125_11.htm";
		}
		else if(event.equalsIgnoreCase("reply_6") && GetMemoState == 1)
		{
			st.setCond(2);
			st.set("name_of_cruel_god_one", String.valueOf(2), true);
			st.giveItems(8782, 1);
			st.playSound("ItemSound.quest_middle");
			htmltext = "mushika_q0125_12.htm";
		}
		else if(event.equalsIgnoreCase("reply_13") && GetMemoState == 2)
		{
			st.setCond(3);
			st.set("name_of_cruel_god_one", String.valueOf(3), true);
			st.playSound("ItemSound.quest_middle");
			htmltext = "shaman_caracawe_q0125_09.htm";
		}
		else if(event.equalsIgnoreCase("reply_17") && GetMemoState == 4)
		{
			st.setCond(5);
			st.set("name_of_cruel_god_one", String.valueOf(5), true);
			st.playSound("ItemSound.quest_middle");
			htmltext = "shaman_caracawe_q0125_17.htm";
		}
		else if(event.equalsIgnoreCase("reply_19") && GetMemoState == 5)
		{
			st.set("name_of_cruel_god_one_ex", String.valueOf(0), true);
			htmltext = "ulu_kaimu_stone_q0125_04.htm";
		}
		else if(event.equalsIgnoreCase("reply_1") && GetMemoState == 5)
		{
			st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 1), true);
			htmltext = "ulu_kaimu_stone_q0125_05.htm";
		}
		else if(event.equalsIgnoreCase("reply_2") && GetMemoState == 5)
		{
			st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 10), true);
			htmltext = "ulu_kaimu_stone_q0125_06.htm";
		}
		else if(event.equalsIgnoreCase("reply_3") && GetMemoState == 5)
		{
			st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 100), true);
			htmltext = "ulu_kaimu_stone_q0125_07.htm";
		}
		else if(event.equalsIgnoreCase("reply_4") && GetMemoState == 5)
		{
			if(GetMemoStateEx != 111)
			{
				htmltext = "ulu_kaimu_stone_q0125_08.htm";
			}
			else
			{
				st.set("name_of_cruel_god_one_ex", String.valueOf(0), true);
				st.set("name_of_cruel_god_one", String.valueOf(6), true);
				htmltext = "ulu_kaimu_stone_q0125_09.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_26") && GetMemoState == 6)
		{
			st.setCond(6);
			st.set("name_of_cruel_god_one", String.valueOf(7), true);
			st.playSound("ItemSound.quest_middle");
			htmltext = "ulu_kaimu_stone_q0125_20.htm";
		}
		else if(event.equalsIgnoreCase("reply_27") && GetMemoState == 7)
		{
			st.set("name_of_cruel_god_one_ex", String.valueOf(0), true);
			htmltext = "balu_kaimu_stone_q0125_04.htm";
		}
		else if(event.equalsIgnoreCase("reply_1a") && GetMemoState == 7)
		{
			st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 1), true);
			htmltext = "balu_kaimu_stone_q0125_05.htm";
		}
		else if(event.equalsIgnoreCase("reply_2a") && GetMemoState == 7)
		{
			st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 10), true);
			htmltext = "balu_kaimu_stone_q0125_06.htm";
		}
		else if(event.equalsIgnoreCase("reply_3a") && GetMemoState == 7)
		{
			st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 100), true);
			htmltext = "balu_kaimu_stone_q0125_07.htm";
		}
		else if(event.equalsIgnoreCase("reply_4a") && GetMemoState == 7)
		{
			if(GetMemoStateEx != 111)
			{
				htmltext = "balu_kaimu_stone_q0125_08.htm";
			}
			else
			{
				st.set("name_of_cruel_god_one_ex", String.valueOf(0), true);
				st.set("name_of_cruel_god_one", String.valueOf(8), true);
				htmltext = "balu_kaimu_stone_q0125_09.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_34") && GetMemoState == 8)
		{
			st.setCond(7);
			st.set("name_of_cruel_god_one", String.valueOf(9), true);
			st.playSound("ItemSound.quest_middle");
			htmltext = "balu_kaimu_stone_q0125_19.htm";
		}
		else if(event.equalsIgnoreCase("reply_35") && GetMemoState == 9)
		{
			st.set("name_of_cruel_god_one_ex", String.valueOf(0), true);
			htmltext = "jiuta_kaimu_stone_q0125_04.htm";
		}
		else if(event.equalsIgnoreCase("reply_1b") && GetMemoState == 9)
		{
			st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 1), true);
			htmltext = "jiuta_kaimu_stone_q0125_05.htm";
		}
		else if(event.equalsIgnoreCase("reply_2b") && GetMemoState == 9)
		{
			st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 10), true);
			htmltext = "jiuta_kaimu_stone_q0125_06.htm";
		}
		else if(event.equalsIgnoreCase("reply_3b") && GetMemoState == 9)
		{
			st.set("name_of_cruel_god_one_ex", String.valueOf(GetMemoStateEx + 100), true);
			htmltext = "jiuta_kaimu_stone_q0125_07.htm";
		}
		else if(event.equalsIgnoreCase("reply_4b") && GetMemoState == 9)
		{
			if(GetMemoStateEx != 111)
			{
				htmltext = "jiuta_kaimu_stone_q0125_08.htm";
			}
			else
			{
				st.set("name_of_cruel_god_one_ex", String.valueOf(0), true);
				st.set("name_of_cruel_god_one", String.valueOf(10), true);
				htmltext = "jiuta_kaimu_stone_q0125_09.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_38") && GetMemoState == 10)
		{
			st.set("name_of_cruel_god_one", String.valueOf(11), true);
			htmltext = "jiuta_kaimu_stone_q0125_13.htm";
		}
		else if(event.equalsIgnoreCase("reply_41") && GetMemoState == 11 && st.getQuestItemsCount(8782) >= 1)
		{
			st.set("name_of_cruel_god_one", String.valueOf(12), true);
			htmltext = "jiuta_kaimu_stone_q0125_19.htm";
		}
		else if(event.equalsIgnoreCase("reply_42"))
		{
			st.set("name_of_cruel_god_one", String.valueOf(13), true);
			htmltext = "jiuta_kaimu_stone_q0125_21.htm";
		}
		else if(event.equalsIgnoreCase("reply_43") && GetMemoState == 13 && st.getQuestItemsCount(8782) >= 1)
		{
			st.setCond(8);
			st.set("name_of_cruel_god_one", String.valueOf(14), true);
			st.giveItems(8781, 1);
			st.takeItems(8782, -1);
			st.playSound("ItemSound.quest_middle");
			htmltext = "jiuta_kaimu_stone_q0125_23.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		QuestState qs = st.getPlayer().getQuestState(_124_MeetingTheElroki.class);
		int GetMemoState = st.getInt("name_of_cruel_god_one");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 32114)
					break;
				if(qs != null && qs.isCompleted())
				{
					if(st.getPlayer().getLevel() >= 76)
					{
						htmltext = "mushika_q0125_01.htm";
						break;
					}
					htmltext = "mushika_q0125_02.htm";
					st.exitCurrentQuest(true);
					break;
				}
				htmltext = "mushika_q0125_04.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId == 32114)
				{
					if(GetMemoState < 1)
					{
						htmltext = "mushika_q0125_09.htm";
						break;
					}
					if(GetMemoState == 1)
					{
						htmltext = "mushika_q0125_11a.htm";
						break;
					}
					if(GetMemoState == 2)
					{
						htmltext = "mushika_q0125_13.htm";
						break;
					}
					if(GetMemoState >= 3 && GetMemoState <= 13)
					{
						htmltext = "mushika_q0125_14.htm";
						break;
					}
					if(GetMemoState != 14 || st.getQuestItemsCount(8781) < 1)
						break;
					st.takeItems(8781, -1);
					st.unset("name_of_cruel_god_one");
					st.unset("name_of_cruel_god_one_ex");
					st.playSound("ItemSound.quest_finish");
					st.exitCurrentQuest(false);
					htmltext = "mushika_q0125_15.htm";
					break;
				}
				if(npcId == 32117)
				{
					if(GetMemoState == 2)
					{
						htmltext = "shaman_caracawe_q0125_01.htm";
						break;
					}
					if(GetMemoState < 2)
					{
						htmltext = "shaman_caracawe_q0125_02.htm";
						break;
					}
					if(GetMemoState == 3 && st.getQuestItemsCount(8779) >= 2 && st.getQuestItemsCount(8780) >= 2)
					{
						st.takeItems(8779, -1);
						st.takeItems(8780, -1);
						st.set("name_of_cruel_god_one", String.valueOf(4), true);
						htmltext = "shaman_caracawe_q0125_11.htm";
						break;
					}
					if(GetMemoState == 3 && (st.getQuestItemsCount(8779) < 2 || st.getQuestItemsCount(8780) < 2))
					{
						htmltext = "shaman_caracawe_q0125_12.htm";
						break;
					}
					if(GetMemoState == 4)
					{
						htmltext = "shaman_caracawe_q0125_14.htm";
						break;
					}
					if(GetMemoState == 5)
					{
						htmltext = "shaman_caracawe_q0125_18.htm";
						break;
					}
					if(GetMemoState >= 6 && GetMemoState < 13)
					{
						htmltext = "shaman_caracawe_q0125_19.htm";
						break;
					}
					if(GetMemoState != 14 || st.getQuestItemsCount(8781) < 1)
						break;
					htmltext = "shaman_caracawe_q0125_20.htm";
					break;
				}
				if(npcId == 32119)
				{
					if(GetMemoState == 5)
					{
						npc.doCast(SkillTable.getInstance().getInfo(5089, 1), st.getPlayer(), true);
						htmltext = "ulu_kaimu_stone_q0125_01.htm";
						break;
					}
					if(GetMemoState < 5)
					{
						htmltext = "ulu_kaimu_stone_q0125_02.htm";
						break;
					}
					if(GetMemoState > 7)
					{
						htmltext = "ulu_kaimu_stone_q0125_03.htm";
						break;
					}
					if(GetMemoState == 6)
					{
						htmltext = "ulu_kaimu_stone_q0125_11.htm";
						break;
					}
					if(GetMemoState != 7)
						break;
					htmltext = "ulu_kaimu_stone_q0125_21.htm";
					break;
				}
				if(npcId == 32120)
				{
					if(GetMemoState == 7)
					{
						npc.doCast(SkillTable.getInstance().getInfo(5089, 1), st.getPlayer(), true);
						htmltext = "balu_kaimu_stone_q0125_01.htm";
						break;
					}
					if(GetMemoState < 7)
					{
						htmltext = "balu_kaimu_stone_q0125_02.htm";
						break;
					}
					if(GetMemoState > 9)
					{
						htmltext = "balu_kaimu_stone_q0125_03.htm";
						break;
					}
					if(GetMemoState == 8)
					{
						htmltext = "balu_kaimu_stone_q0125_11.htm";
						break;
					}
					if(GetMemoState != 9)
						break;
					htmltext = "balu_kaimu_stone_q0125_20.htm";
					break;
				}
				if(npcId != 32121)
					break;
				if(GetMemoState == 9)
				{
					npc.doCast(SkillTable.getInstance().getInfo(5089, 1), st.getPlayer(), true);
					htmltext = "jiuta_kaimu_stone_q0125_01.htm";
					break;
				}
				if(GetMemoState < 9)
				{
					htmltext = "jiuta_kaimu_stone_q0125_02.htm";
					break;
				}
				if(GetMemoState > 14)
				{
					htmltext = "jiuta_kaimu_stone_q0125_03.htm";
					break;
				}
				if(GetMemoState == 10)
				{
					htmltext = "jiuta_kaimu_stone_q0125_11.htm";
					break;
				}
				if(GetMemoState == 11)
				{
					htmltext = "jiuta_kaimu_stone_q0125_14.htm";
					break;
				}
				if(GetMemoState == 12 && st.getQuestItemsCount(8782) >= 1)
				{
					htmltext = "jiuta_kaimu_stone_q0125_20.htm";
					break;
				}
				if(GetMemoState == 13 && st.getQuestItemsCount(8782) >= 1)
				{
					htmltext = "jiuta_kaimu_stone_q0125_22.htm";
					break;
				}
				if(GetMemoState != 14 || st.getQuestItemsCount(8781) < 1)
					break;
				htmltext = "jiuta_kaimu_stone_q0125_24.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("name_of_cruel_god_one");
		int npcId = npc.getNpcId();
		if(GetMemoState == 3)
		{
			if(npcId == 22200 || npcId == 22202)
			{
				int i42 = Rnd.get(1000);
				if(i42 < 661 && st.getQuestItemsCount(8779) <= 1)
				{
					if(st.getQuestItemsCount(8779) < 1)
					{
						st.giveItems(8779, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					else if(st.getQuestItemsCount(8779) >= 1)
					{
						st.giveItems(8779, 1);
						if(st.getQuestItemsCount(8780) >= 2)
						{
							st.setCond(4);
							st.playSound("ItemSound.quest_middle");
						}
					}
				}
			}
			else if(npcId == 22201)
			{
				int i43 = Rnd.get(1000);
				if(i43 < 330 && st.getQuestItemsCount(8779) <= 1)
				{
					if(st.getQuestItemsCount(8779) < 1)
					{
						st.giveItems(8779, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					else if(st.getQuestItemsCount(8779) >= 1)
					{
						st.giveItems(8779, 1);
						if(st.getQuestItemsCount(8780) >= 2)
						{
							st.setCond(4);
							st.playSound("ItemSound.quest_middle");
						}
					}
				}
			}
			else if(npcId == 22203 || npcId == 22205)
			{
				int i44 = Rnd.get(1000);
				if(i44 < 651 && st.getQuestItemsCount(8780) <= 1)
				{
					if(st.getQuestItemsCount(8780) < 1)
					{
						st.giveItems(8780, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					else if(st.getQuestItemsCount(8780) >= 1)
					{
						st.giveItems(8780, 1);
						if(st.getQuestItemsCount(8779) >= 2)
						{
							st.setCond(4);
							st.playSound("ItemSound.quest_middle");
						}
					}
				}
			}
			else if(npcId == 22204)
			{
				int i45 = Rnd.get(1000);
				if(i45 < 326 && st.getQuestItemsCount(8780) <= 1)
				{
					if(st.getQuestItemsCount(8780) < 1)
					{
						st.giveItems(8780, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					else if(st.getQuestItemsCount(8780) >= 1)
					{
						st.giveItems(8780, 1);
						if(st.getQuestItemsCount(8779) >= 2)
						{
							st.setCond(4);
							st.playSound("ItemSound.quest_middle");
						}
					}
				}
			}
			else if(npcId == 22219 || npcId == 22224)
			{
				int i46 = Rnd.get(1000);
				if(i46 < 327 && st.getQuestItemsCount(8779) <= 1)
				{
					if(st.getQuestItemsCount(8779) < 1)
					{
						st.giveItems(8779, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					else if(st.getQuestItemsCount(8779) >= 1)
					{
						st.giveItems(8779, 1);
						if(st.getQuestItemsCount(8780) >= 2)
						{
							st.setCond(4);
							st.playSound("ItemSound.quest_middle");
						}
					}
				}
			}
			else if((npcId == 22220 || npcId == 22225) && Rnd.get(1000) < 319 && st.getQuestItemsCount(8780) <= 1)
			{
				if(st.getQuestItemsCount(8780) < 1)
				{
					st.giveItems(8780, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if(st.getQuestItemsCount(8780) >= 1)
				{
					st.giveItems(8780, 1);
					if(st.getQuestItemsCount(8779) >= 2)
					{
						st.setCond(4);
						st.playSound("ItemSound.quest_middle");
					}
				}
			}
		}
		return null;
	}
}