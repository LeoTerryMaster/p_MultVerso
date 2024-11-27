package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _373_SupplierOfReagents extends Quest implements ScriptFile
{
	private static final int bandor = 30166;
	private static final int alchemical_mixing_jar = 31149;
	private static final int credion = 20813;
	private static final int hallates_maiden = 20822;
	private static final int platinum_tribe_shaman = 20828;
	private static final int hallates_guardian = 21061;
	private static final int plat_protect_shaman = 21066;
	private static final int lava_wyrm = 21111;
	private static final int hames_orc_shaman = 21115;
	private static final int mixing_manual = 6317;
	private static final int dracoplasm = 6021;
	private static final int swift_attack_potion = 735;
	private static final int magma_dust = 6022;
	private static final int scroll_of_resurrection = 737;
	private static final int moon_dust = 6023;
	private static final int cursed_bone = 2508;
	private static final int necroplasm = 6024;
	private static final int enria = 4042;
	private static final int asofe = 4043;
	private static final int thons = 4044;
	private static final int demonplasm = 6025;
	private static final int rp_avadon_gloves_i = 4953;
	private static final int rp_avadon_boots_i = 4959;
	private static final int rp_shrnoens_gauntlet_i = 4960;
	private static final int rp_shrnoens_boots_i = 4958;
	private static final int inferno_dust = 6026;
	private static final int rp_blue_wolves_gloves_i = 4998;
	private static final int rp_blue_wolves_boots_i = 4992;
	private static final int draconic_essence = 6027;
	private static final int fire_essence = 6028;
	private static final int rp_doom_gloves_i = 4993;
	private static final int rp_doom_boots_i = 4999;
	private static final int lunargent = 6029;
	private static final int sealed_dark_crystal_leather_mail_pattern = 5478;
	private static final int sealed_tallum_leather_mail_pattern = 5479;
	private static final int sealed_leather_mail_of_nightmare_fabric = 5480;
	private static final int sealed_majestic_leather_mail_fabric = 5481;
	private static final int sealed_legging_of_dark_crystal_design = 5482;
	private static final int midnight_oil = 6030;
	private static final int sealed_dark_crystal_breastplate_pattern = 5520;
	private static final int sealed_tallum_plate_armor_pattern = 5521;
	private static final int sealed_armor_of_nightmare_pattern = 5522;
	private static final int sealed_majestic_platte_armor_pattern = 5523;
	private static final int sealed_dark_crystal_gaiters_pattern = 5524;
	private static final int demonic_essence = 6031;
	private static final int abyss_oil = 6032;
	private static final int hellfire_oil = 6033;
	private static final int nightmare_oil = 6034;
	private static final int wyrms_blood = 6011;
	private static final int lava_stone = 6012;
	private static final int moonstone_shard = 6013;
	private static final int decaying_bone = 6014;
	private static final int demons_blood = 6015;
	private static final int infernium_ore = 6016;
	private static final int blood_root = 6017;
	private static final int volcanic_ash = 6018;
	private static final int quicksilver = 6019;
	private static final int sulfur = 6020;
	private static final int ingredient_pouch1 = 6007;
	private static final int ingredient_pouch2 = 6008;
	private static final int ingredient_pouch3 = 6009;
	private static final int ingredient_box = 6010;
	private static final int tower_shield = 103;
	private static final int drake_leather_boots = 2437;
	private static final int square_shield = 630;
	private static final int shrnoens_gauntlet = 612;
	private static final int avadon_gloves = 2464;
	private static final int shrnoens_boots = 554;
	private static final int avadon_boots = 600;
	private static final int blue_wolves_boots = 2439;
	private static final int doom_boots = 601;
	private static final int blue_wolves_gloves = 2487;
	private static final int doom_gloves = 2475;
	private static final int wesleys_mixing_stone = 5904;
	private static final int pure_silver = 6320;
	
	public _373_SupplierOfReagents()
	{
		super(true);
		addStartNpc(30166);
		addTalkId(31149);
		addKillId(20813, 20822, 20828, 21061, 21066, 21111, 21115);
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
		int npcId = npc.getNpcId();
		int GetMemoState = st.getInt("reagent_supplier");
		int GetMemoStateEx = st.getInt("reagent_supplier_ex");
		if(npcId == 30166)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.setState(2);
				st.giveItems(5904, 1);
				st.giveItems(6317, 1);
				st.playSound("ItemSound.quest_accept");
				htmltext = "bandor_q0373_04.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
			{
				htmltext = "bandor_q0373_03.htm";
			}
			else if(event.equalsIgnoreCase("reply_2"))
			{
				htmltext = "bandor_q0373_06.htm";
			}
			else if(event.equalsIgnoreCase("reply_3"))
			{
				htmltext = "bandor_q0373_07.htm";
			}
			else if(event.equalsIgnoreCase("reply_4"))
			{
				htmltext = "bandor_q0373_08.htm";
			}
			else if(event.equalsIgnoreCase("reply_5"))
			{
				st.takeItems(5904, -1);
				st.takeItems(6317, -1);
				st.unset("reagent_supplier");
				st.unset("reagent_supplier_ex");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				htmltext = "bandor_q0373_09.htm";
			}
			else if(event.equalsIgnoreCase("reply_11"))
			{
				if(st.getQuestItemsCount(6021) >= 1)
				{
					st.giveItems(735, 1);
					st.takeItems(6021, 1);
					htmltext = "bandor_q0373_10.htm";
				}
				else
				{
					htmltext = "bandor_q0373_24.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_12"))
			{
				if(st.getQuestItemsCount(6022) >= 1)
				{
					st.giveItems(737, 2);
					st.takeItems(6022, 1);
					htmltext = "bandor_q0373_11.htm";
				}
				else
				{
					htmltext = "bandor_q0373_24.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_13"))
			{
				if(st.getQuestItemsCount(6023) >= 1)
				{
					st.giveItems(2508, 25);
					st.takeItems(6023, 1);
					htmltext = "bandor_q0373_12.htm";
				}
				else
				{
					htmltext = "bandor_q0373_24.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_14"))
			{
				if(st.getQuestItemsCount(6024) >= 1)
				{
					int i0 = Rnd.get(100);
					if(i0 < 32)
					{
						st.giveItems(4042, 1);
					}
					else if(i0 < 66)
					{
						st.giveItems(4043, 1);
					}
					else
					{
						st.giveItems(4044, 1);
					}
					st.giveItems(57, 500);
					st.takeItems(6024, 1);
					htmltext = "bandor_q0373_13.htm";
				}
				else
				{
					htmltext = "bandor_q0373_24.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_15"))
			{
				if(st.getQuestItemsCount(6025) >= 1)
				{
					int i0 = Rnd.get(100);
					if(i0 < 28)
					{
						st.giveItems(735, 20);
					}
					else if(i0 < 68)
					{
						st.giveItems(4953, 1);
					}
					else
					{
						st.giveItems(4959, 1);
					}
					st.takeItems(6025, 1);
					htmltext = "bandor_q0373_14.htm";
				}
				else
				{
					htmltext = "bandor_q0373_24.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_16"))
			{
				if(st.getQuestItemsCount(6026) >= 1)
				{
					int i0 = Rnd.get(100);
					if(i0 < 40)
					{
						st.giveItems(2508, 200);
					}
					else if(i0 < 70)
					{
						st.giveItems(4960, 1);
					}
					else
					{
						st.giveItems(4958, 1);
					}
					st.takeItems(6026, 1);
					htmltext = "bandor_q0373_15.htm";
				}
				else
				{
					htmltext = "bandor_q0373_24.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_17"))
			{
				if(st.getQuestItemsCount(6027) >= 1)
				{
					int i0 = Rnd.get(100);
					if(i0 < 40)
					{
						st.giveItems(4998, 1);
					}
					else if(i0 < 80)
					{
						st.giveItems(4992, 1);
					}
					else
					{
						st.giveItems(737, 20);
					}
					st.takeItems(6027, 1);
					htmltext = "bandor_q0373_16.htm";
				}
				else
				{
					htmltext = "bandor_q0373_24.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_18"))
			{
				if(st.getQuestItemsCount(6028) >= 1)
				{
					int i0 = Rnd.get(100);
					if(i0 < 90)
					{
						st.giveItems(4993, 1);
						st.giveItems(4999, 1);
					}
					else
					{
						st.giveItems(4042, 2);
					}
					st.takeItems(6028, 1);
					htmltext = "bandor_q0373_17.htm";
				}
				else
				{
					htmltext = "bandor_q0373_24.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_19"))
			{
				if(st.getQuestItemsCount(6029) >= 1)
				{
					int i0 = Rnd.get(100);
					if(i0 < 20)
					{
						st.giveItems(5478, 2);
					}
					else if(i0 < 40)
					{
						st.giveItems(5479, 2);
					}
					else if(i0 < 60)
					{
						st.giveItems(5480, 2);
					}
					else if(i0 < 80)
					{
						st.giveItems(5481, 2);
					}
					else
					{
						st.giveItems(5482, 2);
					}
					st.giveItems(57, 8000);
					st.takeItems(6029, 1);
					htmltext = "bandor_q0373_18.htm";
				}
				else
				{
					htmltext = "bandor_q0373_24.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_20"))
			{
				if(st.getQuestItemsCount(6030) >= 1)
				{
					int i0 = Rnd.get(100);
					if(i0 < 20)
					{
						st.giveItems(5520, 3);
					}
					else if(i0 < 40)
					{
						st.giveItems(5521, 3);
					}
					else if(i0 < 60)
					{
						st.giveItems(5522, 3);
					}
					else if(i0 < 80)
					{
						st.giveItems(5523, 3);
					}
					else
					{
						st.giveItems(5524, 3);
					}
					st.giveItems(57, 8000);
					st.takeItems(6030, 1);
					htmltext = "bandor_q0373_19.htm";
				}
				else
				{
					htmltext = "bandor_q0373_24.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_21"))
			{
				if(st.getQuestItemsCount(6031) >= 1)
				{
					int i0 = Rnd.get(100);
					if(i0 < 40)
					{
						st.giveItems(57, 32000);
					}
					else if(i0 < 80)
					{
						st.giveItems(57, 24500);
					}
					else
					{
						st.giveItems(57, 16000);
					}
					st.takeItems(6031, 1);
					htmltext = "bandor_q0373_20.htm";
				}
				else
				{
					htmltext = "bandor_q0373_24.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_22"))
			{
				if(st.getQuestItemsCount(6032) >= 1)
				{
					int i0 = Rnd.get(100);
					if(i0 < 30)
					{
						st.giveItems(103, 1);
					}
					else if(i0 < 60)
					{
						st.giveItems(2437, 1);
					}
					else
					{
						st.giveItems(630, 1);
					}
					st.giveItems(57, 5000);
					st.takeItems(6032, 1);
					htmltext = "bandor_q0373_21.htm";
				}
				else
				{
					htmltext = "bandor_q0373_24.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_23"))
			{
				if(st.getQuestItemsCount(6033) >= 1)
				{
					int i0 = Rnd.get(100);
					if(i0 < 25)
					{
						st.giveItems(612, 1);
					}
					else if(i0 < 50)
					{
						st.giveItems(2464, 1);
					}
					else if(i0 < 75)
					{
						st.giveItems(554, 1);
					}
					else
					{
						st.giveItems(600, 1);
					}
					st.takeItems(6033, 1);
					htmltext = "bandor_q0373_22.htm";
				}
				else
				{
					htmltext = "bandor_q0373_24.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_24"))
			{
				if(st.getQuestItemsCount(6034) >= 1)
				{
					int i0 = Rnd.get(100);
					if(i0 < 17)
					{
						st.giveItems(2439, 1);
					}
					else if(i0 < 34)
					{
						st.giveItems(601, 1);
					}
					else if(i0 < 51)
					{
						st.giveItems(2487, 1);
					}
					else if(i0 < 68)
					{
						st.giveItems(2475, 1);
					}
					else
					{
						st.giveItems(4992, 1);
						st.giveItems(4998, 1);
					}
					st.giveItems(57, 19000);
					st.takeItems(6034, 1);
					htmltext = "bandor_q0373_23.htm";
				}
				else
				{
					htmltext = "bandor_q0373_24.htm";
				}
			}
		}
		else if(npcId == 31149)
		{
			if(event.equalsIgnoreCase("reply_1"))
			{
				st.set("reagent_supplier", String.valueOf(0), true);
				st.set("reagent_supplier_ex", String.valueOf(0), true);
				htmltext = "alchemical_mixing_jar_q0373_02.htm";
			}
			else if(event.equalsIgnoreCase("reply_11"))
			{
				if(st.getQuestItemsCount(6011) >= 10)
				{
					st.set("reagent_supplier", String.valueOf(11), true);
					htmltext = "alchemical_mixing_jar_q0373_03.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_12"))
			{
				if(st.getQuestItemsCount(6012) >= 10)
				{
					st.set("reagent_supplier", String.valueOf(12), true);
					htmltext = "alchemical_mixing_jar_q0373_04.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_13"))
			{
				if(st.getQuestItemsCount(6013) >= 10)
				{
					st.set("reagent_supplier", String.valueOf(13), true);
					htmltext = "alchemical_mixing_jar_q0373_05.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_14"))
			{
				if(st.getQuestItemsCount(6014) >= 10)
				{
					st.set("reagent_supplier", String.valueOf(14), true);
					htmltext = "alchemical_mixing_jar_q0373_06.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_15"))
			{
				if(st.getQuestItemsCount(6015) >= 10)
				{
					st.set("reagent_supplier", String.valueOf(15), true);
					htmltext = "alchemical_mixing_jar_q0373_07.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_16"))
			{
				if(st.getQuestItemsCount(6016) >= 10)
				{
					st.set("reagent_supplier", String.valueOf(16), true);
					htmltext = "alchemical_mixing_jar_q0373_08.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_17"))
			{
				if(st.getQuestItemsCount(6021) >= 10)
				{
					st.set("reagent_supplier", String.valueOf(17), true);
					htmltext = "alchemical_mixing_jar_q0373_09.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_18"))
			{
				if(st.getQuestItemsCount(6022) >= 10)
				{
					st.set("reagent_supplier", String.valueOf(18), true);
					htmltext = "alchemical_mixing_jar_q0373_10.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_19"))
			{
				if(st.getQuestItemsCount(6023) >= 10)
				{
					st.set("reagent_supplier", String.valueOf(19), true);
					htmltext = "alchemical_mixing_jar_q0373_11.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_20"))
			{
				if(st.getQuestItemsCount(6024) >= 10)
				{
					st.set("reagent_supplier", String.valueOf(20), true);
					htmltext = "alchemical_mixing_jar_q0373_12.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_21"))
			{
				if(st.getQuestItemsCount(6025) >= 10)
				{
					st.set("reagent_supplier", String.valueOf(21), true);
					htmltext = "alchemical_mixing_jar_q0373_13.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_22"))
			{
				if(st.getQuestItemsCount(6026) >= 10)
				{
					st.set("reagent_supplier", String.valueOf(22), true);
					htmltext = "alchemical_mixing_jar_q0373_14.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_23"))
			{
				if(st.getQuestItemsCount(6028) >= 1)
				{
					st.set("reagent_supplier", String.valueOf(23), true);
					htmltext = "alchemical_mixing_jar_q0373_15.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_24"))
			{
				if(st.getQuestItemsCount(6029) >= 1)
				{
					st.set("reagent_supplier", String.valueOf(24), true);
					htmltext = "alchemical_mixing_jar_q0373_16.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_2"))
			{
				htmltext = "alchemical_mixing_jar_q0373_18.htm";
			}
			else if(event.equalsIgnoreCase("reply_31"))
			{
				if(st.getQuestItemsCount(6017) >= 1)
				{
					st.set("reagent_supplier", String.valueOf(GetMemoState + 1100), true);
					htmltext = "alchemical_mixing_jar_q0373_19.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					if(GetMemoState == 11)
					{
						st.takeItems(6011, 10);
					}
					else if(GetMemoState == 12)
					{
						st.takeItems(6012, 10);
					}
					else if(GetMemoState == 13)
					{
						st.takeItems(6013, 10);
					}
					else if(GetMemoState == 14)
					{
						st.takeItems(6014, 10);
					}
					else if(GetMemoState == 15)
					{
						st.takeItems(6015, 10);
					}
					else if(GetMemoState == 16)
					{
						st.takeItems(6016, 10);
					}
					else if(GetMemoState == 17)
					{
						st.takeItems(6021, 10);
					}
					else if(GetMemoState == 18)
					{
						st.takeItems(6022, 10);
					}
					else if(GetMemoState == 19)
					{
						st.takeItems(6023, 10);
					}
					else if(GetMemoState == 20)
					{
						st.takeItems(6024, 10);
					}
					else if(GetMemoState == 21)
					{
						st.takeItems(6025, 10);
					}
					else if(GetMemoState == 22)
					{
						st.takeItems(6026, 10);
					}
					else if(GetMemoState == 23)
					{
						st.takeItems(6028, 1);
					}
					else if(GetMemoState == 24)
					{
						st.takeItems(6029, 1);
					}
					htmltext = "alchemical_mixing_jar_q0373_25.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_32"))
			{
				if(st.getQuestItemsCount(6018) >= 1)
				{
					st.set("reagent_supplier", String.valueOf(GetMemoState + 1200), true);
					htmltext = "alchemical_mixing_jar_q0373_20.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					if(GetMemoState == 11)
					{
						st.takeItems(6011, 10);
					}
					else if(GetMemoState == 12)
					{
						st.takeItems(6012, 10);
					}
					else if(GetMemoState == 13)
					{
						st.takeItems(6013, 10);
					}
					else if(GetMemoState == 14)
					{
						st.takeItems(6014, 10);
					}
					else if(GetMemoState == 15)
					{
						st.takeItems(6015, 10);
					}
					else if(GetMemoState == 16)
					{
						st.takeItems(6016, 10);
					}
					else if(GetMemoState == 17)
					{
						st.takeItems(6021, 10);
					}
					else if(GetMemoState == 18)
					{
						st.takeItems(6022, 10);
					}
					else if(GetMemoState == 19)
					{
						st.takeItems(6023, 10);
					}
					else if(GetMemoState == 20)
					{
						st.takeItems(6024, 10);
					}
					else if(GetMemoState == 21)
					{
						st.takeItems(6025, 10);
					}
					else if(GetMemoState == 22)
					{
						st.takeItems(6026, 10);
					}
					else if(GetMemoState == 23)
					{
						st.takeItems(6028, 1);
					}
					else if(GetMemoState == 24)
					{
						st.takeItems(6029, 1);
					}
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_33"))
			{
				if(st.getQuestItemsCount(6019) >= 1)
				{
					st.set("reagent_supplier", String.valueOf(GetMemoState + 1300), true);
					htmltext = "alchemical_mixing_jar_q0373_21.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					if(GetMemoState == 11)
					{
						st.takeItems(6011, 10);
					}
					else if(GetMemoState == 12)
					{
						st.takeItems(6012, 10);
					}
					else if(GetMemoState == 13)
					{
						st.takeItems(6013, 10);
					}
					else if(GetMemoState == 14)
					{
						st.takeItems(6014, 10);
					}
					else if(GetMemoState == 15)
					{
						st.takeItems(6015, 10);
					}
					else if(GetMemoState == 16)
					{
						st.takeItems(6016, 10);
					}
					else if(GetMemoState == 17)
					{
						st.takeItems(6021, 10);
					}
					else if(GetMemoState == 18)
					{
						st.takeItems(6022, 10);
					}
					else if(GetMemoState == 19)
					{
						st.takeItems(6023, 10);
					}
					else if(GetMemoState == 20)
					{
						st.takeItems(6024, 10);
					}
					else if(GetMemoState == 21)
					{
						st.takeItems(6025, 10);
					}
					else if(GetMemoState == 22)
					{
						st.takeItems(6026, 10);
					}
					else if(GetMemoState == 23)
					{
						st.takeItems(6028, 1);
					}
					else if(GetMemoState == 24)
					{
						st.takeItems(6029, 1);
					}
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_34"))
			{
				if(st.getQuestItemsCount(6020) >= 1)
				{
					st.set("reagent_supplier", String.valueOf(GetMemoState + 1400), true);
					htmltext = "alchemical_mixing_jar_q0373_22.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					if(GetMemoState == 11)
					{
						st.takeItems(6011, 10);
					}
					else if(GetMemoState == 12)
					{
						st.takeItems(6012, 10);
					}
					else if(GetMemoState == 13)
					{
						st.takeItems(6013, 10);
					}
					else if(GetMemoState == 14)
					{
						st.takeItems(6014, 10);
					}
					else if(GetMemoState == 15)
					{
						st.takeItems(6015, 10);
					}
					else if(GetMemoState == 16)
					{
						st.takeItems(6016, 10);
					}
					else if(GetMemoState == 17)
					{
						st.takeItems(6021, 10);
					}
					else if(GetMemoState == 18)
					{
						st.takeItems(6022, 10);
					}
					else if(GetMemoState == 19)
					{
						st.takeItems(6023, 10);
					}
					else if(GetMemoState == 20)
					{
						st.takeItems(6024, 10);
					}
					else if(GetMemoState == 21)
					{
						st.takeItems(6025, 10);
					}
					else if(GetMemoState == 22)
					{
						st.takeItems(6026, 10);
					}
					else if(GetMemoState == 23)
					{
						st.takeItems(6028, 1);
					}
					else if(GetMemoState == 24)
					{
						st.takeItems(6029, 1);
					}
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_35"))
			{
				if(st.getQuestItemsCount(6031) >= 1)
				{
					st.set("reagent_supplier", String.valueOf(GetMemoState + 1500), true);
					htmltext = "alchemical_mixing_jar_q0373_23.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					if(GetMemoState == 11)
					{
						st.takeItems(6011, 10);
					}
					else if(GetMemoState == 12)
					{
						st.takeItems(6012, 10);
					}
					else if(GetMemoState == 13)
					{
						st.takeItems(6013, 10);
					}
					else if(GetMemoState == 14)
					{
						st.takeItems(6014, 10);
					}
					else if(GetMemoState == 15)
					{
						st.takeItems(6015, 10);
					}
					else if(GetMemoState == 16)
					{
						st.takeItems(6016, 10);
					}
					else if(GetMemoState == 17)
					{
						st.takeItems(6021, 10);
					}
					else if(GetMemoState == 18)
					{
						st.takeItems(6022, 10);
					}
					else if(GetMemoState == 19)
					{
						st.takeItems(6023, 10);
					}
					else if(GetMemoState == 20)
					{
						st.takeItems(6024, 10);
					}
					else if(GetMemoState == 21)
					{
						st.takeItems(6025, 10);
					}
					else if(GetMemoState == 22)
					{
						st.takeItems(6026, 10);
					}
					else if(GetMemoState == 23)
					{
						st.takeItems(6028, 1);
					}
					else if(GetMemoState == 24)
					{
						st.takeItems(6029, 1);
					}
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_36"))
			{
				if(st.getQuestItemsCount(6030) >= 1)
				{
					st.set("reagent_supplier", String.valueOf(GetMemoState + 1600), true);
					htmltext = "alchemical_mixing_jar_q0373_24.htm";
					st.playSound("SkillSound5.liquid_mix_01");
				}
				else
				{
					if(GetMemoState == 11)
					{
						st.takeItems(6011, 10);
					}
					else if(GetMemoState == 12)
					{
						st.takeItems(6012, 10);
					}
					else if(GetMemoState == 13)
					{
						st.takeItems(6013, 10);
					}
					else if(GetMemoState == 14)
					{
						st.takeItems(6014, 10);
					}
					else if(GetMemoState == 15)
					{
						st.takeItems(6015, 10);
					}
					else if(GetMemoState == 16)
					{
						st.takeItems(6016, 10);
					}
					else if(GetMemoState == 17)
					{
						st.takeItems(6021, 10);
					}
					else if(GetMemoState == 18)
					{
						st.takeItems(6022, 10);
					}
					else if(GetMemoState == 19)
					{
						st.takeItems(6023, 10);
					}
					else if(GetMemoState == 20)
					{
						st.takeItems(6024, 10);
					}
					else if(GetMemoState == 21)
					{
						st.takeItems(6025, 10);
					}
					else if(GetMemoState == 22)
					{
						st.takeItems(6026, 10);
					}
					else if(GetMemoState == 23)
					{
						st.takeItems(6028, 1);
					}
					else if(GetMemoState == 24)
					{
						st.takeItems(6029, 1);
					}
					htmltext = "alchemical_mixing_jar_q0373_17.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_3"))
			{
				htmltext = GetMemoState == 1324 ? "alchemical_mixing_jar_q0373_26a.htm" : "alchemical_mixing_jar_q0373_26.htm";
			}
			else if(event.equalsIgnoreCase("reply_4"))
			{
				st.set("reagent_supplier_ex", String.valueOf(1), true);
				htmltext = "alchemical_mixing_jar_q0373_27.htm";
			}
			else if(event.equalsIgnoreCase("reply_5"))
			{
				if(Rnd.get(100) < 33)
				{
					st.set("reagent_supplier_ex", String.valueOf(3), true);
					htmltext = "alchemical_mixing_jar_q0373_28a.htm";
				}
				else
				{
					st.set("reagent_supplier_ex", String.valueOf(0), true);
					htmltext = "alchemical_mixing_jar_q0373_28a.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_6"))
			{
				if(Rnd.get(100) < 20)
				{
					st.set("reagent_supplier_ex", String.valueOf(5), true);
					htmltext = "alchemical_mixing_jar_q0373_29a.htm";
				}
				else
				{
					st.set("reagent_supplier_ex", String.valueOf(0), true);
					htmltext = "alchemical_mixing_jar_q0373_29a.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_7") && GetMemoStateEx != 0)
			{
				if(GetMemoState == 1111)
				{
					if(st.getQuestItemsCount(6011) >= 10 && st.getQuestItemsCount(6017) >= 1)
					{
						st.takeItems(6011, 10);
						st.takeItems(6017, 1);
						st.giveItems(6021, (long) GetMemoStateEx);
						st.set("reagent_supplier", String.valueOf(0), true);
						st.set("reagent_supplier_ex", String.valueOf(0), true);
						htmltext = "alchemical_mixing_jar_q0373_30.htm";
						st.playSound("SkillSound5.liquid_success_01");
					}
					else
					{
						htmltext = "alchemical_mixing_jar_q0373_44.htm";
						st.playSound("SkillSound5.liquid_fail_01");
					}
				}
				else if(GetMemoState == 1212)
				{
					if(st.getQuestItemsCount(6012) >= 10 && st.getQuestItemsCount(6018) >= 1)
					{
						st.takeItems(6012, 10);
						st.takeItems(6018, 1);
						st.giveItems(6022, (long) GetMemoStateEx);
						st.set("reagent_supplier", String.valueOf(0), true);
						st.set("reagent_supplier_ex", String.valueOf(0), true);
						htmltext = "alchemical_mixing_jar_q0373_31.htm";
						st.playSound("SkillSound5.liquid_success_01");
					}
					else
					{
						htmltext = "alchemical_mixing_jar_q0373_44.htm";
						st.playSound("SkillSound5.liquid_fail_01");
					}
				}
				else if(GetMemoState == 1213)
				{
					if(st.getQuestItemsCount(6013) >= 10 && st.getQuestItemsCount(6018) >= 1)
					{
						st.takeItems(6013, 10);
						st.takeItems(6018, 1);
						st.giveItems(6023, (long) GetMemoStateEx);
						st.set("reagent_supplier", String.valueOf(0), true);
						st.set("reagent_supplier_ex", String.valueOf(0), true);
						htmltext = "alchemical_mixing_jar_q0373_32.htm";
						st.playSound("SkillSound5.liquid_success_01");
					}
					else
					{
						htmltext = "alchemical_mixing_jar_q0373_44.htm";
						st.playSound("SkillSound5.liquid_fail_01");
					}
				}
				else if(GetMemoState == 1114)
				{
					if(st.getQuestItemsCount(6014) >= 10 && st.getQuestItemsCount(6017) >= 1)
					{
						st.takeItems(6014, 10);
						st.takeItems(6017, 1);
						st.giveItems(6024, (long) GetMemoStateEx);
						st.set("reagent_supplier", String.valueOf(0), true);
						st.set("reagent_supplier_ex", String.valueOf(0), true);
						htmltext = "alchemical_mixing_jar_q0373_33.htm";
						st.playSound("SkillSound5.liquid_success_01");
					}
					else
					{
						htmltext = "alchemical_mixing_jar_q0373_44.htm";
						st.playSound("SkillSound5.liquid_fail_01");
					}
				}
				else if(GetMemoState == 1115)
				{
					if(st.getQuestItemsCount(6015) >= 10 && st.getQuestItemsCount(6017) >= 1)
					{
						st.takeItems(6015, 10);
						st.takeItems(6017, 1);
						st.giveItems(6025, (long) GetMemoStateEx);
						st.set("reagent_supplier", String.valueOf(0), true);
						st.set("reagent_supplier_ex", String.valueOf(0), true);
						htmltext = "alchemical_mixing_jar_q0373_34.htm";
						st.playSound("SkillSound5.liquid_success_01");
					}
					else
					{
						htmltext = "alchemical_mixing_jar_q0373_44.htm";
						st.playSound("SkillSound5.liquid_fail_01");
					}
				}
				else if(GetMemoState == 1216)
				{
					if(st.getQuestItemsCount(6016) >= 10 && st.getQuestItemsCount(6018) >= 1)
					{
						st.takeItems(6016, 10);
						st.takeItems(6018, 1);
						st.giveItems(6026, (long) GetMemoStateEx);
						st.set("reagent_supplier", String.valueOf(0), true);
						st.set("reagent_supplier_ex", String.valueOf(0), true);
						htmltext = "alchemical_mixing_jar_q0373_35.htm";
						st.playSound("SkillSound5.liquid_success_01");
					}
					else
					{
						htmltext = "alchemical_mixing_jar_q0373_44.htm";
						st.playSound("SkillSound5.liquid_fail_01");
					}
				}
				else if(GetMemoState == 1317)
				{
					if(st.getQuestItemsCount(6021) >= 10 && st.getQuestItemsCount(6019) >= 1)
					{
						st.takeItems(6021, 10);
						st.takeItems(6019, 1);
						st.giveItems(6027, (long) GetMemoStateEx);
						st.set("reagent_supplier", String.valueOf(0), true);
						st.set("reagent_supplier_ex", String.valueOf(0), true);
						htmltext = "alchemical_mixing_jar_q0373_36.htm";
						st.playSound("SkillSound5.liquid_success_01");
					}
					else
					{
						htmltext = "alchemical_mixing_jar_q0373_44.htm";
						st.playSound("SkillSound5.liquid_fail_01");
					}
				}
				else if(GetMemoState == 1418)
				{
					if(st.getQuestItemsCount(6022) >= 10 && st.getQuestItemsCount(6020) >= 1)
					{
						st.takeItems(6022, 10);
						st.takeItems(6020, 1);
						st.giveItems(6028, (long) GetMemoStateEx);
						st.set("reagent_supplier", String.valueOf(0), true);
						st.set("reagent_supplier_ex", String.valueOf(0), true);
						htmltext = "alchemical_mixing_jar_q0373_37.htm";
						st.playSound("SkillSound5.liquid_success_01");
					}
					else
					{
						htmltext = "alchemical_mixing_jar_q0373_44.htm";
						st.playSound("SkillSound5.liquid_fail_01");
					}
				}
				else if(GetMemoState == 1319)
				{
					if(st.getQuestItemsCount(6023) >= 10 && st.getQuestItemsCount(6019) >= 1)
					{
						st.takeItems(6023, 10);
						st.takeItems(6019, 1);
						st.giveItems(6029, (long) GetMemoStateEx);
						st.set("reagent_supplier", String.valueOf(0), true);
						st.set("reagent_supplier_ex", String.valueOf(0), true);
						htmltext = "alchemical_mixing_jar_q0373_38.htm";
						st.playSound("SkillSound5.liquid_success_01");
					}
					else
					{
						htmltext = "alchemical_mixing_jar_q0373_44.htm";
						st.playSound("SkillSound5.liquid_fail_01");
					}
				}
				else if(GetMemoState == 1320)
				{
					if(st.getQuestItemsCount(6024) >= 10 && st.getQuestItemsCount(6019) >= 1)
					{
						st.takeItems(6024, 10);
						st.takeItems(6019, 1);
						st.giveItems(6030, (long) GetMemoStateEx);
						st.set("reagent_supplier", String.valueOf(0), true);
						st.set("reagent_supplier_ex", String.valueOf(0), true);
						htmltext = "alchemical_mixing_jar_q0373_39.htm";
						st.playSound("SkillSound5.liquid_success_01");
					}
					else
					{
						htmltext = "alchemical_mixing_jar_q0373_44.htm";
						st.playSound("SkillSound5.liquid_fail_01");
					}
				}
				else if(GetMemoState == 1421)
				{
					if(st.getQuestItemsCount(6025) >= 10 && st.getQuestItemsCount(6020) >= 1)
					{
						st.takeItems(6025, 10);
						st.takeItems(6020, 1);
						st.giveItems(6031, (long) GetMemoStateEx);
						st.set("reagent_supplier", String.valueOf(0), true);
						st.set("reagent_supplier_ex", String.valueOf(0), true);
						htmltext = "alchemical_mixing_jar_q0373_40.htm";
						st.playSound("SkillSound5.liquid_success_01");
					}
					else
					{
						htmltext = "alchemical_mixing_jar_q0373_44.htm";
						st.playSound("SkillSound5.liquid_fail_01");
					}
				}
				else if(GetMemoState == 1422)
				{
					if(st.getQuestItemsCount(6026) >= 10 && st.getQuestItemsCount(6020) >= 1)
					{
						st.takeItems(6026, 10);
						st.takeItems(6020, 1);
						st.giveItems(6032, (long) GetMemoStateEx);
						st.set("reagent_supplier", String.valueOf(0), true);
						st.set("reagent_supplier_ex", String.valueOf(0), true);
						htmltext = "alchemical_mixing_jar_q0373_41.htm";
						st.playSound("SkillSound5.liquid_success_01");
					}
					else
					{
						htmltext = "alchemical_mixing_jar_q0373_44.htm";
						st.playSound("SkillSound5.liquid_fail_01");
					}
				}
				else if(GetMemoState == 1523)
				{
					if(st.getQuestItemsCount(6028) >= 1 && st.getQuestItemsCount(6031) >= 1)
					{
						st.takeItems(6028, 1);
						st.takeItems(6031, 1);
						st.giveItems(6033, (long) GetMemoStateEx);
						st.set("reagent_supplier", String.valueOf(0), true);
						st.set("reagent_supplier_ex", String.valueOf(0), true);
						htmltext = "alchemical_mixing_jar_q0373_42.htm";
						st.playSound("SkillSound5.liquid_success_01");
					}
					else
					{
						htmltext = "alchemical_mixing_jar_q0373_44.htm";
						st.playSound("SkillSound5.liquid_fail_01");
					}
				}
				else if(GetMemoState == 1624)
				{
					if(st.getQuestItemsCount(6029) >= 1 && st.getQuestItemsCount(6030) >= 1)
					{
						st.takeItems(6029, 1);
						st.takeItems(6030, 1);
						st.giveItems(6034, (long) GetMemoStateEx);
						st.set("reagent_supplier", String.valueOf(0), true);
						st.set("reagent_supplier_ex", String.valueOf(0), true);
						htmltext = "alchemical_mixing_jar_q0373_43.htm";
						st.playSound("SkillSound5.liquid_success_01");
					}
					else
					{
						htmltext = "alchemical_mixing_jar_q0373_44.htm";
						st.playSound("SkillSound5.liquid_fail_01");
					}
				}
				else if(GetMemoState == 1324)
				{
					if(st.getQuestItemsCount(6320) == 0)
					{
						if(st.getQuestItemsCount(6029) >= 1 && st.getQuestItemsCount(6019) >= 1)
						{
							st.takeItems(6029, 1);
							st.takeItems(6019, 1);
							st.giveItems(6320, 1);
							st.set("reagent_supplier", String.valueOf(0), true);
							st.set("reagent_supplier_ex", String.valueOf(0), true);
							htmltext = "alchemical_mixing_jar_q0373_46.htm";
							st.playSound("SkillSound5.liquid_success_01");
						}
						else
						{
							htmltext = "alchemical_mixing_jar_q0373_44.htm";
							st.playSound("SkillSound5.liquid_fail_01");
						}
					}
					else
					{
						htmltext = "alchemical_mixing_jar_q0373_44.htm";
					}
				}
				else if(GetMemoState == 1324)
				{
					htmltext = "alchemical_mixing_jar_q0373_44.htm";
				}
				else
				{
					int i2 = GetMemoState % 100;
					if(i2 == 11)
					{
						st.takeItems(6011, 10);
					}
					else if(i2 == 12)
					{
						st.takeItems(6012, 10);
					}
					else if(i2 == 13)
					{
						st.takeItems(6013, 10);
					}
					else if(i2 == 14)
					{
						st.takeItems(6014, 10);
					}
					else if(i2 == 15)
					{
						st.takeItems(6015, 10);
					}
					else if(i2 == 16)
					{
						st.takeItems(6016, 10);
					}
					else if(i2 == 17)
					{
						st.takeItems(6021, 10);
					}
					else if(i2 == 18)
					{
						st.takeItems(6022, 10);
					}
					else if(i2 == 19)
					{
						st.takeItems(6023, 10);
					}
					else if(i2 == 20)
					{
						st.takeItems(6024, 10);
					}
					else if(i2 == 21)
					{
						st.takeItems(6025, 10);
					}
					else if(i2 == 22)
					{
						st.takeItems(6026, 10);
					}
					else if(i2 == 23)
					{
						st.takeItems(6028, 1);
					}
					else if(i2 == 24)
					{
						st.takeItems(6029, 1);
					}
					int i1 = GetMemoState / 100;
					if(i1 == 11)
					{
						st.takeItems(6017, 1);
					}
					else if(i1 == 12)
					{
						st.takeItems(6018, 1);
					}
					else if(i1 == 13)
					{
						st.takeItems(6019, 1);
					}
					else if(i1 == 14)
					{
						st.takeItems(6020, 1);
					}
					else if(i1 == 15)
					{
						st.takeItems(6031, 1);
					}
					else if(i1 == 16)
					{
						st.takeItems(6030, 1);
					}
					htmltext = "alchemical_mixing_jar_q0373_44.htm";
					st.playSound("SkillSound5.liquid_fail_01");
				}
			}
			else if(event.equalsIgnoreCase("reply_7") && GetMemoStateEx == 0)
			{
				int i2 = GetMemoState % 100;
				if(i2 == 11)
				{
					st.takeItems(6011, 10);
				}
				else if(i2 == 12)
				{
					st.takeItems(6012, 10);
				}
				else if(i2 == 13)
				{
					st.takeItems(6013, 10);
				}
				else if(i2 == 14)
				{
					st.takeItems(6014, 10);
				}
				else if(i2 == 15)
				{
					st.takeItems(6015, 10);
				}
				else if(i2 == 16)
				{
					st.takeItems(6016, 10);
				}
				else if(i2 == 17)
				{
					st.takeItems(6021, 10);
				}
				else if(i2 == 18)
				{
					st.takeItems(6022, 10);
				}
				else if(i2 == 19)
				{
					st.takeItems(6023, 10);
				}
				else if(i2 == 20)
				{
					st.takeItems(6024, 10);
				}
				else if(i2 == 21)
				{
					st.takeItems(6025, 10);
				}
				else if(i2 == 22)
				{
					st.takeItems(6026, 10);
				}
				else if(i2 == 23)
				{
					st.takeItems(6028, 1);
				}
				else if(i2 == 24)
				{
					st.takeItems(6029, 1);
				}
				int i1 = GetMemoState / 100;
				if(i1 == 11)
				{
					st.takeItems(6017, 1);
				}
				else if(i1 == 12)
				{
					st.takeItems(6018, 1);
				}
				else if(i1 == 13)
				{
					st.takeItems(6019, 1);
				}
				else if(i1 == 14)
				{
					st.takeItems(6020, 1);
				}
				else if(i1 == 15)
				{
					st.takeItems(6031, 1);
				}
				else if(i1 == 16)
				{
					st.takeItems(6030, 1);
				}
				htmltext = "alchemical_mixing_jar_q0373_45.htm";
				st.playSound("SkillSound5.liquid_fail_01");
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("reagent_supplier");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 30166)
					break;
				if(st.getPlayer().getLevel() < 57)
				{
					htmltext = "bandor_q0373_01.htm";
					st.exitCurrentQuest(true);
					break;
				}
				htmltext = "bandor_q0373_02.htm";
				break;
			}
			case 2:
			{
				if(npcId == 30166)
				{
					if(GetMemoState != 0)
						break;
					htmltext = "bandor_q0373_05.htm";
					break;
				}
				if(npcId != 31149 || GetMemoState < 0)
					break;
				htmltext = "alchemical_mixing_jar_q0373_01.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == 20813)
		{
			int i42 = Rnd.get(1000);
			if(i42 < 618)
			{
				st.rollAndGive(6014, 1, 100.0);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(i42 < 1000)
			{
				st.rollAndGive(6019, 1, 100.0);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(npcId == 20822)
		{
			int i43 = Rnd.get(100);
			if(i43 < 45)
			{
				st.rollAndGive(6007, 1, 100.0);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(i43 < 65)
			{
				st.rollAndGive(6018, 1, 100.0);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(npcId == 20828)
		{
			int i44 = Rnd.get(1000);
			if(i44 < 658)
			{
				st.rollAndGive(6008, 1, 100.0);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(i44 < 100)
			{
				st.rollAndGive(6019, 2, 100.0);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(npcId == 21061)
		{
			int i45 = Rnd.get(1000);
			if(i45 < 766)
			{
				st.rollAndGive(6015, 3, 100.0);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(i45 < 876)
			{
				st.rollAndGive(6013, 1, 100.0);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(npcId == 21066)
		{
			int i46 = Rnd.get(1000);
			if(i46 < 444)
			{
				st.rollAndGive(6010, 1, 100.0);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(npcId == 21111)
		{
			int i47 = Rnd.get(1000);
			if(i47 < 666)
			{
				st.rollAndGive(6011, 1, 100.0);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(i47 < 989)
			{
				st.rollAndGive(6012, 1, 100.0);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(npcId == 21115 && Rnd.get(1000) < 616)
		{
			st.rollAndGive(6009, 1, 100.0);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}