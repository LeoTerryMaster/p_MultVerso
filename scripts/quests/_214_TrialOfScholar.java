package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _214_TrialOfScholar extends Quest implements ScriptFile
{
	private static final int Sylvain = 30070;
	private static final int Lucas = 30071;
	private static final int Valkon = 30103;
	private static final int Dieter = 30111;
	private static final int Jurek = 30115;
	private static final int Edroc = 30230;
	private static final int Raut = 30316;
	private static final int Poitan = 30458;
	private static final int Mirien = 30461;
	private static final int Maria = 30608;
	private static final int Creta = 30609;
	private static final int Cronos = 30610;
	private static final int Triff = 30611;
	private static final int Casian = 30612;
	private static final int Monster_Eye_Destroyer = 20068;
	private static final int Medusa = 20158;
	private static final int Ghoul = 20201;
	private static final int Shackle = 20235;
	private static final int Breka_Orc_Shaman = 20269;
	private static final int Fettered_Soul = 20552;
	private static final int Grandis = 20554;
	private static final int Enchanted_Gargoyle = 20567;
	private static final int Leto_Lizardman_Warrior = 20580;
	private static final int Dimensional_Diamond = 7562;
	private static final int Mark_of_Scholar = 2674;
	private static final int Miriens_1st_Sigil = 2675;
	private static final int Miriens_2nd_Sigil = 2676;
	private static final int Miriens_3rd_Sigil = 2677;
	private static final int Miriens_Instruction = 2678;
	private static final int Marias_1st_Letter = 2679;
	private static final int Marias_2nd_Letter = 2680;
	private static final int Lucass_Letter = 2681;
	private static final int Lucillas_Handbag = 2682;
	private static final int Cretas_1st_Letter = 2683;
	private static final int Cretas_Painting1 = 2684;
	private static final int Cretas_Painting2 = 2685;
	private static final int Cretas_Painting3 = 2686;
	private static final int Brown_Scroll_Scrap = 2687;
	private static final int Crystal_of_Purity1 = 2688;
	private static final int High_Priests_Sigil = 2689;
	private static final int Grand_Magisters_Sigil = 2690;
	private static final int Cronos_Sigil = 2691;
	private static final int Sylvains_Letter = 2692;
	private static final int Symbol_of_Sylvain = 2693;
	private static final int Jureks_List = 2694;
	private static final int Monster_Eye_Destroyer_Skin = 2695;
	private static final int Shamans_Necklace = 2696;
	private static final int Shackles_Scalp = 2697;
	private static final int Symbol_of_Jurek = 2698;
	private static final int Cronos_Letter = 2699;
	private static final int Dieters_Key = 2700;
	private static final int Cretas_2nd_Letter = 2701;
	private static final int Dieters_Letter = 2702;
	private static final int Dieters_Diary = 2703;
	private static final int Rauts_Letter_Envelope = 2704;
	private static final int Triffs_Ring = 2705;
	private static final int Scripture_Chapter_1 = 2706;
	private static final int Scripture_Chapter_2 = 2707;
	private static final int Scripture_Chapter_3 = 2708;
	private static final int Scripture_Chapter_4 = 2709;
	private static final int Valkons_Request = 2710;
	private static final int Poitans_Notes = 2711;
	private static final int Strong_Liquor = 2713;
	private static final int Crystal_of_Purity2 = 2714;
	private static final int Casians_List = 2715;
	private static final int Ghouls_Skin = 2716;
	private static final int Medusas_Blood = 2717;
	private static final int Fettered_Souls_Ichor = 2718;
	private static final int Enchanted_Gargoyles_Nail = 2719;
	private static final int Symbol_of_Cronos = 2720;
	
	public _214_TrialOfScholar()
	{
		super(false);
		addStartNpc(30461);
		addTalkId(30070);
		addTalkId(30071);
		addTalkId(30103);
		addTalkId(30111);
		addTalkId(30115);
		addTalkId(30230);
		addTalkId(30316);
		addTalkId(30458);
		addTalkId(30608);
		addTalkId(30609);
		addTalkId(30610);
		addTalkId(30611);
		addTalkId(30612);
		addKillId(20068);
		addKillId(20158);
		addKillId(20201);
		addKillId(20235);
		addKillId(20269);
		addKillId(20552);
		addKillId(20554);
		addKillId(20567);
		addKillId(20580);
		addQuestItem(2708);
		addQuestItem(2687);
		addQuestItem(2695);
		addQuestItem(2696);
		addQuestItem(2697);
		addQuestItem(2716);
		addQuestItem(2717);
		addQuestItem(2718);
		addQuestItem(2719);
	}
	
	private static boolean Check_cond17_items(QuestState st)
	{
		if(st.getQuestItemsCount(2695) < 5)
		{
			return false;
		}
		if(st.getQuestItemsCount(2696) < 5)
		{
			return false;
		}
		return st.getQuestItemsCount(2697) >= 2;
	}
	
	private static boolean Check_cond29_items(QuestState st)
	{
		if(st.getQuestItemsCount(2716) < 10)
		{
			return false;
		}
		if(st.getQuestItemsCount(2717) < 12)
		{
			return false;
		}
		if(st.getQuestItemsCount(2718) < 5)
		{
			return false;
		}
		return st.getQuestItemsCount(2719) >= 5;
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		if(event.equalsIgnoreCase("magister_mirien_q0214_04.htm") && _state == 1)
		{
			st.giveItems(2675, 1);
			if(!st.getPlayer().getVarB("dd1"))
			{
				st.giveItems(7562, 168);
				st.getPlayer().setVar("dd1", "1", -1);
			}
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("sylvain_q0214_02.htm") && _state == 2)
		{
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2689, 1);
			st.giveItems(2692, 1);
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("marya_q0214_02.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(2692) == 0)
			{
				return null;
			}
			st.takeItems(2692, -1);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2679, 1);
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("astrologer_creta_q0214_05.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(2680) == 0)
			{
				return null;
			}
			st.takeItems(2680, -1);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2683, 1);
			st.setCond(6);
		}
		else if(event.equalsIgnoreCase("marya_q0214_08.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(2683) == 0)
			{
				return null;
			}
			st.takeItems(2683, -1);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2682, 1);
			st.setCond(7);
		}
		else if(event.equalsIgnoreCase("astrologer_creta_q0214_09.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(2682) == 0)
			{
				return null;
			}
			st.takeItems(2682, -1);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2684, 1);
			st.setCond(8);
		}
		else if(event.equalsIgnoreCase("lucas_q0214_04.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(2685) == 0)
			{
				return null;
			}
			st.takeItems(2685, -1);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2686, 1);
			st.setCond(10);
		}
		else if(event.equalsIgnoreCase("marya_q0214_14.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(2686) == 0 || st.getQuestItemsCount(2687) < 5)
			{
				return null;
			}
			st.takeItems(2686, -1);
			st.takeItems(2687, -1);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2688, 1);
			st.setCond(13);
		}
		else if(event.equalsIgnoreCase("valkon_q0214_04.htm") && _state == 2 && st.getQuestItemsCount(2710) == 0)
		{
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2710, 1);
		}
		else if(event.equalsIgnoreCase("jurek_q0214_03.htm") && _state == 2 && st.getQuestItemsCount(2690) == 0 && st.getQuestItemsCount(2698) == 0)
		{
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2690, 1);
			st.giveItems(2694, 1);
			st.setCond(16);
		}
		else if(event.equalsIgnoreCase("magister_mirien_q0214_10.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(2676) == 0 || st.getQuestItemsCount(2698) == 0)
			{
				return null;
			}
			st.takeItems(2676, -1);
			st.takeItems(2698, -1);
			if(st.getPlayer().getLevel() < 36)
			{
				st.giveItems(2678, 1);
				return "magister_mirien_q0214_09.htm";
			}
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2677, 1);
			st.setCond(19);
		}
		else if(event.equalsIgnoreCase("sage_cronos_q0214_10.htm") && _state == 2)
		{
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2691, 1);
			st.giveItems(2699, 1);
			st.setCond(20);
		}
		else if(event.equalsIgnoreCase("dieter_q0214_05.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(2699) == 0)
			{
				return null;
			}
			st.takeItems(2699, -1);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2700, 1);
			st.setCond(21);
		}
		else if(event.equalsIgnoreCase("astrologer_creta_q0214_14.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(2700) == 0)
			{
				return null;
			}
			st.takeItems(2700, -1);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2701, 1);
			st.setCond(22);
		}
		else if(event.equalsIgnoreCase("dieter_q0214_09.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(2701) == 0)
			{
				return null;
			}
			st.takeItems(2701, -1);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2702, 1);
			st.giveItems(2703, 1);
			st.setCond(23);
		}
		else if(event.equalsIgnoreCase("trader_edroc_q0214_02.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(2702) == 0)
			{
				return null;
			}
			st.takeItems(2702, -1);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2704, 1);
			st.setCond(24);
		}
		else if(event.equalsIgnoreCase("warehouse_keeper_raut_q0214_02.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(2704) == 0)
			{
				return null;
			}
			st.takeItems(2704, -1);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2706, 1);
			st.giveItems(2713, 1);
			st.setCond(25);
		}
		else if(event.equalsIgnoreCase("drunkard_treaf_q0214_04.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(2713) == 0)
			{
				return null;
			}
			st.takeItems(2713, -1);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2705, 1);
			st.setCond(26);
		}
		else if(event.equalsIgnoreCase("sage_kasian_q0214_04.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(2715) == 0)
			{
				st.playSound("ItemSound.quest_middle");
				st.giveItems(2715, 1);
			}
			st.setCond(28);
		}
		else if(event.equalsIgnoreCase("sage_kasian_q0214_07.htm") && _state == 2)
		{
			st.takeItems(2715, -1);
			st.takeItems(2716, -1);
			st.takeItems(2717, -1);
			st.takeItems(2718, -1);
			st.takeItems(2719, -1);
			st.takeItems(2711, -1);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2709, 1);
			st.setCond(30);
		}
		else if(event.equalsIgnoreCase("sage_cronos_q0214_14.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(2706) == 0)
			{
				return null;
			}
			if(st.getQuestItemsCount(2707) == 0)
			{
				return null;
			}
			if(st.getQuestItemsCount(2708) == 0)
			{
				return null;
			}
			if(st.getQuestItemsCount(2709) == 0)
			{
				return null;
			}
			if(st.getQuestItemsCount(2691) == 0)
			{
				return null;
			}
			st.takeItems(2706, -1);
			st.takeItems(2707, -1);
			st.takeItems(2708, -1);
			st.takeItems(2709, -1);
			st.takeItems(2691, -1);
			st.takeItems(2705, -1);
			st.takeItems(2703, 1);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(2720, 1);
			st.setCond(31);
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		if(st.getQuestItemsCount(2674) > 0)
		{
			st.exitCurrentQuest(true);
			return "completed";
		}
		int _state = st.getState();
		int npcId = npc.getNpcId();
		if(_state == 1)
		{
			if(npcId != 30461)
			{
				return "noquest";
			}
			int class_id = st.getPlayer().getClassId().getId();
			if(class_id != 11 && class_id != 26 && class_id != 39)
			{
				st.exitCurrentQuest(true);
				return "magister_mirien_q0214_01.htm";
			}
			if(st.getPlayer().getLevel() < 35)
			{
				st.exitCurrentQuest(true);
				return "magister_mirien_q0214_02.htm";
			}
			st.setCond(0);
			return "magister_mirien_q0214_03.htm";
		}
		if(_state != 2)
		{
			return "noquest";
		}
		if(npcId == 30461)
		{
			if(st.getQuestItemsCount(2675) > 0)
			{
				if(st.getQuestItemsCount(2693) == 0)
				{
					return "magister_mirien_q0214_05.htm";
				}
				st.takeItems(2675, -1);
				st.takeItems(2693, -1);
				st.playSound("ItemSound.quest_middle");
				st.giveItems(2676, 1);
				st.setCond(15);
				return "magister_mirien_q0214_06.htm";
			}
			if(st.getQuestItemsCount(2676) > 0)
			{
				return st.getQuestItemsCount(2698) > 0 ? "magister_mirien_q0214_08.htm" : "magister_mirien_q0214_07.htm";
			}
			if(st.getQuestItemsCount(2678) > 0)
			{
				if(st.getPlayer().getLevel() < 36)
				{
					return "magister_mirien_q0214_11.htm";
				}
				st.takeItems(2678, -1);
				st.playSound("ItemSound.quest_middle");
				st.giveItems(2677, 1);
				st.setCond(19);
				return "magister_mirien_q0214_12.htm";
			}
			if(st.getQuestItemsCount(2677) > 0)
			{
				if(st.getQuestItemsCount(2720) == 0)
				{
					return "magister_mirien_q0214_13.htm";
				}
				st.takeItems(2720, -1);
				st.takeItems(2677, -1);
				if(!st.getPlayer().getVarB("prof2.1"))
				{
					st.addExpAndSp(80265, 30000);
					st.getPlayer().setVar("prof2.1", "1", -1);
				}
				st.giveItems(2674, 1);
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				return "magister_mirien_q0214_14.htm";
			}
		}
		if(npcId == 30070)
		{
			if(st.getQuestItemsCount(2675) > 0)
			{
				if(st.getQuestItemsCount(2689) < 1)
				{
					return st.getQuestItemsCount(2693) > 0 ? "sylvain_q0214_05.htm" : "sylvain_q0214_01.htm";
				}
				if(st.getQuestItemsCount(2688) < 1)
				{
					return "sylvain_q0214_03.htm";
				}
				st.takeItems(2689, -1);
				st.takeItems(2688, -1);
				st.playSound("ItemSound.quest_middle");
				st.giveItems(2693, 1);
				st.setCond(14);
				return "sylvain_q0214_04.htm";
			}
			if(st.getQuestItemsCount(2676) > 0 || st.getQuestItemsCount(2677) > 0)
			{
				return "sylvain_q0214_06.htm";
			}
		}
		if(npcId == 30071)
		{
			if(st.getQuestItemsCount(2675) > 0 && st.getQuestItemsCount(2689) > 0)
			{
				if(st.getQuestItemsCount(2679) > 0)
				{
					st.takeItems(2679, -1);
					st.playSound("ItemSound.quest_middle");
					st.giveItems(2681, 1);
					st.setCond(4);
					return "lucas_q0214_01.htm";
				}
				if(st.getQuestItemsCount(2680) > 0)
				{
					return "lucas_q0214_02.htm";
				}
				if(st.getQuestItemsCount(2683) > 0)
				{
					return "lucas_q0214_02.htm";
				}
				if(st.getQuestItemsCount(2682) > 0)
				{
					return "lucas_q0214_02.htm";
				}
				if(st.getQuestItemsCount(2684) > 0)
				{
					return "lucas_q0214_02.htm";
				}
				if(st.getQuestItemsCount(2681) > 0)
				{
					return "lucas_q0214_02.htm";
				}
				if(st.getQuestItemsCount(2685) > 0)
				{
					return "lucas_q0214_03.htm";
				}
				if(st.getQuestItemsCount(2686) > 0)
				{
					return st.getQuestItemsCount(2687) < 5 ? "lucas_q0214_05.htm" : "lucas_q0214_06.htm";
				}
			}
			else
			{
				if(st.getQuestItemsCount(2693) > 0)
				{
					return "lucas_q0214_07.htm";
				}
				if(st.getQuestItemsCount(2676) > 0)
				{
					return "lucas_q0214_07.htm";
				}
				if(st.getQuestItemsCount(2677) > 0)
				{
					return "lucas_q0214_07.htm";
				}
				if(st.getQuestItemsCount(2688) > 0)
				{
					return "lucas_q0214_07.htm";
				}
			}
		}
		if(npcId == 30103 && st.getQuestItemsCount(2705) > 0)
		{
			long Valkons_Request_count = st.getQuestItemsCount(2710);
			long Scripture_Chapter_2_count = st.getQuestItemsCount(2707);
			if(st.getQuestItemsCount(2714) == 0)
			{
				if(Scripture_Chapter_2_count == 0)
				{
					return Valkons_Request_count > 0 ? "valkon_q0214_05.htm" : "valkon_q0214_01.htm";
				}
				if(Valkons_Request_count == 0)
				{
					return "valkon_q0214_07.htm";
				}
			}
			else if(Valkons_Request_count == 0 && Scripture_Chapter_2_count == 0)
			{
				st.takeItems(2714, -1);
				st.playSound("ItemSound.quest_middle");
				st.giveItems(2707, 1);
				return "valkon_q0214_06.htm";
			}
		}
		if(npcId == 30111)
		{
			if(st.getQuestItemsCount(2677) > 0 && st.getQuestItemsCount(2691) > 0)
			{
				if(st.getQuestItemsCount(2699) > 0)
				{
					return "dieter_q0214_01.htm";
				}
				if(st.getQuestItemsCount(2700) > 0)
				{
					return "dieter_q0214_06.htm";
				}
				if(st.getQuestItemsCount(2701) > 0)
				{
					return "dieter_q0214_07.htm";
				}
				if(st.getQuestItemsCount(2703) > 0)
				{
					if(st.getQuestItemsCount(2702) > 0)
					{
						return "dieter_q0214_10.htm";
					}
					if(st.getQuestItemsCount(2704) > 0)
					{
						return "dieter_q0214_11.htm";
					}
					if(st.getQuestItemsCount(2706) == 0)
					{
						return "dieter_q0214_12.htm";
					}
					if(st.getQuestItemsCount(2707) == 0)
					{
						return "dieter_q0214_12.htm";
					}
					if(st.getQuestItemsCount(2708) == 0)
					{
						return "dieter_q0214_12.htm";
					}
					if(st.getQuestItemsCount(2709) == 0)
					{
						return "dieter_q0214_12.htm";
					}
					return "dieter_q0214_13.htm";
				}
			}
			if(st.getQuestItemsCount(2720) > 0)
			{
				return "dieter_q0214_15.htm";
			}
		}
		if(npcId == 30115)
		{
			if(st.getQuestItemsCount(2676) > 0)
			{
				long Grand_Magisters_Sigil_count = st.getQuestItemsCount(2690);
				long Symbol_of_Jurek_count = st.getQuestItemsCount(2698);
				if(Grand_Magisters_Sigil_count == 0 && Symbol_of_Jurek_count == 0)
				{
					return "jurek_q0214_01.htm";
				}
				if(st.getQuestItemsCount(2694) > 0)
				{
					if(!Check_cond17_items(st))
					{
						return "jurek_q0214_04.htm";
					}
					if(Grand_Magisters_Sigil_count > 0)
					{
						st.takeItems(2694, -1);
						st.takeItems(2695, -1);
						st.takeItems(2696, -1);
						st.takeItems(2697, -1);
						st.takeItems(2690, -1);
						st.playSound("ItemSound.quest_middle");
						st.giveItems(2698, 1);
						st.setCond(18);
						return "jurek_q0214_05.htm";
					}
				}
				if(Symbol_of_Jurek_count > 0 && Grand_Magisters_Sigil_count == 0)
				{
					return "jurek_q0214_06.htm";
				}
			}
			if(st.getQuestItemsCount(2675) > 0 || st.getQuestItemsCount(2677) > 0)
			{
				return "jurek_q0214_07.htm";
			}
		}
		if(npcId == 30230 && st.getQuestItemsCount(2703) > 0)
		{
			if(st.getQuestItemsCount(2702) > 0)
			{
				return "trader_edroc_q0214_01.htm";
			}
			if(st.getQuestItemsCount(2704) > 0)
			{
				return "trader_edroc_q0214_03.htm";
			}
			if(st.getQuestItemsCount(2713) > 0 || st.getQuestItemsCount(2705) > 0)
			{
				return "trader_edroc_q0214_04.htm";
			}
		}
		if(npcId == 30316 && st.getQuestItemsCount(2703) > 0)
		{
			if(st.getQuestItemsCount(2704) > 0)
			{
				return "warehouse_keeper_raut_q0214_01.htm";
			}
			if(st.getQuestItemsCount(2706) > 0)
			{
				if(st.getQuestItemsCount(2713) > 0)
				{
					return "warehouse_keeper_raut_q0214_04.htm";
				}
				if(st.getQuestItemsCount(2705) > 0)
				{
					return "warehouse_keeper_raut_q0214_05.htm";
				}
			}
		}
		if(npcId == 30458 && st.getQuestItemsCount(2705) > 0)
		{
			long Poitans_Notes_count = st.getQuestItemsCount(2711);
			long Casians_List_count = st.getQuestItemsCount(2715);
			if(st.getQuestItemsCount(2709) == 0)
			{
				if(Poitans_Notes_count > 0)
				{
					return Casians_List_count > 0 ? "blacksmith_poitan_q0214_03.htm" : "valkon_q0214_02.htm";
				}
				if(Casians_List_count == 0)
				{
					st.playSound("ItemSound.quest_middle");
					st.giveItems(2711, 1);
					return "blacksmith_poitan_q0214_01.htm";
				}
			}
			else if(Poitans_Notes_count == 0 && Casians_List_count == 0)
			{
				return "blacksmith_poitan_q0214_04.htm";
			}
		}
		if(npcId == 30608)
		{
			if(st.getQuestItemsCount(2675) > 0 && st.getQuestItemsCount(2689) > 0)
			{
				if(st.getQuestItemsCount(2692) > 0)
				{
					return "marya_q0214_01.htm";
				}
				if(st.getQuestItemsCount(2679) > 0)
				{
					return "marya_q0214_03.htm";
				}
				if(st.getQuestItemsCount(2681) > 0)
				{
					st.takeItems(2681, -1);
					st.playSound("ItemSound.quest_middle");
					st.giveItems(2680, 1);
					st.setCond(5);
					return "marya_q0214_04.htm";
				}
				if(st.getQuestItemsCount(2680) > 0)
				{
					return "marya_q0214_05.htm";
				}
				if(st.getQuestItemsCount(2683) > 0)
				{
					return "marya_q0214_06.htm";
				}
				if(st.getQuestItemsCount(2682) > 0)
				{
					return "marya_q0214_09.htm";
				}
				if(st.getQuestItemsCount(2684) > 0)
				{
					st.takeItems(2684, 1);
					st.playSound("ItemSound.quest_middle");
					st.giveItems(2685, 1);
					st.setCond(9);
					return "marya_q0214_10.htm";
				}
				if(st.getQuestItemsCount(2685) > 0)
				{
					return "marya_q0214_11.htm";
				}
				if(st.getQuestItemsCount(2686) > 0)
				{
					if(st.getQuestItemsCount(2687) < 5)
					{
						st.setCond(11);
						return "marya_q0214_12.htm";
					}
					return "marya_q0214_13.htm";
				}
				if(st.getQuestItemsCount(2688) > 0)
				{
					return "marya_q0214_15.htm";
				}
			}
			if(st.getQuestItemsCount(2693) > 0 || st.getQuestItemsCount(2676) > 0)
			{
				return "marya_q0214_16.htm";
			}
			if(st.getQuestItemsCount(2677) > 0)
			{
				if(st.getQuestItemsCount(2710) == 0)
				{
					return "marya_q0214_17.htm";
				}
				st.takeItems(2710, 1);
				st.playSound("ItemSound.quest_middle");
				st.giveItems(2714, 1);
				return "marya_q0214_18.htm";
			}
		}
		if(npcId == 30609)
		{
			if(st.getQuestItemsCount(2675) > 0 && st.getQuestItemsCount(2689) > 0)
			{
				if(st.getQuestItemsCount(2680) > 0)
				{
					return "astrologer_creta_q0214_01.htm";
				}
				if(st.getQuestItemsCount(2683) > 0)
				{
					return "astrologer_creta_q0214_06.htm";
				}
				if(st.getQuestItemsCount(2682) > 0)
				{
					return "astrologer_creta_q0214_07.htm";
				}
				if(st.getQuestItemsCount(2684) > 0)
				{
					return "astrologer_creta_q0214_10.htm";
				}
				if(st.getQuestItemsCount(2685) > 0)
				{
					return "astrologer_creta_q0214_10.htm";
				}
				if(st.getQuestItemsCount(2686) > 0)
				{
					return "astrologer_creta_q0214_10.htm";
				}
			}
			if(st.getQuestItemsCount(2688) > 0)
			{
				return "astrologer_creta_q0214_11.htm";
			}
			if(st.getQuestItemsCount(2693) > 0)
			{
				return "astrologer_creta_q0214_11.htm";
			}
			if(st.getQuestItemsCount(2676) > 0)
			{
				return "astrologer_creta_q0214_11.htm";
			}
			if(st.getQuestItemsCount(2677) > 0)
			{
				return st.getQuestItemsCount(2700) > 0 ? "astrologer_creta_q0214_12.htm" : "astrologer_creta_q0214_15.htm";
			}
		}
		if(npcId == 30610 && st.getQuestItemsCount(2677) > 0)
		{
			if(st.getQuestItemsCount(2691) > 0)
			{
				if(st.getQuestItemsCount(2706) == 0)
				{
					return "sage_cronos_q0214_11.htm";
				}
				if(st.getQuestItemsCount(2707) == 0)
				{
					return "sage_cronos_q0214_11.htm";
				}
				if(st.getQuestItemsCount(2708) == 0)
				{
					return "sage_cronos_q0214_11.htm";
				}
				if(st.getQuestItemsCount(2709) == 0)
				{
					return "sage_cronos_q0214_11.htm";
				}
				return "sage_cronos_q0214_12.htm";
			}
			return st.getQuestItemsCount(2720) > 0 ? "sage_cronos_q0214_15.htm" : "sage_cronos_q0214_01.htm";
		}
		if(npcId == 30611)
		{
			if(st.getQuestItemsCount(2703) > 0 && st.getQuestItemsCount(2706) > 0 && st.getQuestItemsCount(2713) > 0)
			{
				return "drunkard_treaf_q0214_01.htm";
			}
			if(st.getQuestItemsCount(2705) > 0)
			{
				return "drunkard_treaf_q0214_05.htm";
			}
			if(st.getQuestItemsCount(2720) > 0)
			{
				return "drunkard_treaf_q0214_05.htm";
			}
		}
		if(npcId == 30612 && st.getQuestItemsCount(2705) > 0)
		{
			long Casians_List_count = st.getQuestItemsCount(2715);
			if(st.getQuestItemsCount(2711) > 0)
			{
				if(Casians_List_count > 0)
				{
					return Check_cond29_items(st) ? "sage_kasian_q0214_06.htm" : "sage_kasian_q0214_05.htm";
				}
				if(st.getQuestItemsCount(2706) > 0 && st.getQuestItemsCount(2707) > 0 && st.getQuestItemsCount(2708) > 0)
				{
					return "sage_kasian_q0214_02.htm";
				}
				st.setCond(27);
				return "sage_kasian_q0214_01.htm";
			}
			if(Casians_List_count == 0 && st.getQuestItemsCount(2706) > 0 && st.getQuestItemsCount(2707) > 0 && st.getQuestItemsCount(2708) > 0 && st.getQuestItemsCount(2709) > 0)
			{
				return "sage_kasian_q0214_08.htm";
			}
		}
		return "noquest";
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() != 2)
		{
			return null;
		}
		int npcId = npc.getNpcId();
		if(npcId == 20554 && st.getQuestItemsCount(2677) > 0 && st.getQuestItemsCount(2691) > 0 && st.getQuestItemsCount(2705) > 0)
		{
			st.rollAndGive(2708, 1, 1, 1, 30.0);
		}
		if(npcId == 20580 && st.getQuestItemsCount(2675) > 0 && st.getQuestItemsCount(2689) > 0 && st.getQuestItemsCount(2686) > 0 && st.rollAndGive(2687, 1, 1, 5, 50.0))
		{
			st.setCond(12);
		}
		if(npcId == 20068 && st.getQuestItemsCount(2676) > 0 && st.getQuestItemsCount(2690) > 0 && st.getQuestItemsCount(2694) > 0 && st.rollAndGive(2695, 1, 1, 5, 50.0) && Check_cond17_items(st))
		{
			st.setCond(17);
		}
		if(npcId == 20269 && st.getQuestItemsCount(2676) > 0 && st.getQuestItemsCount(2690) > 0 && st.getQuestItemsCount(2694) > 0 && st.rollAndGive(2696, 1, 1, 5, 50.0) && Check_cond17_items(st))
		{
			st.setCond(17);
		}
		if(npcId == 20235 && st.getQuestItemsCount(2676) > 0 && st.getQuestItemsCount(2690) > 0 && st.getQuestItemsCount(2694) > 0 && st.rollAndGive(2697, 1, 1, 2, 50.0) && Check_cond17_items(st))
		{
			st.setCond(17);
		}
		if(npcId == 20201 && st.getQuestItemsCount(2705) > 0 && st.getQuestItemsCount(2711) > 0 && st.getQuestItemsCount(2715) > 0 && st.rollAndGive(2716, 1, 1, 10, 100.0) && Check_cond29_items(st))
		{
			st.setCond(29);
		}
		if(npcId == 20158 && st.getQuestItemsCount(2705) > 0 && st.getQuestItemsCount(2711) > 0 && st.getQuestItemsCount(2715) > 0 && st.rollAndGive(2717, 1, 1, 12, 100.0) && Check_cond29_items(st))
		{
			st.setCond(29);
		}
		if(npcId == 20552 && st.getQuestItemsCount(2705) > 0 && st.getQuestItemsCount(2711) > 0 && st.getQuestItemsCount(2715) > 0 && st.rollAndGive(2718, 1, 1, 5, 100.0) && Check_cond29_items(st))
		{
			st.setCond(29);
		}
		if(npcId == 20567 && st.getQuestItemsCount(2705) > 0 && st.getQuestItemsCount(2711) > 0 && st.getQuestItemsCount(2715) > 0 && st.rollAndGive(2719, 1, 1, 5, 100.0) && Check_cond29_items(st))
		{
			st.setCond(29);
		}
		return null;
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
}