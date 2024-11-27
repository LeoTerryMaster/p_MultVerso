package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _217_TestimonyOfTrust extends Quest implements ScriptFile
{
	private static final int MARK_OF_TRUST_ID = 2734;
	private static final int LETTER_TO_ELF_ID = 1558;
	private static final int LETTER_TO_DARKELF_ID = 1556;
	private static final int LETTER_TO_DWARF_ID = 2737;
	private static final int LETTER_TO_ORC_ID = 2738;
	private static final int LETTER_TO_SERESIN_ID = 2739;
	private static final int SCROLL_OF_DARKELF_TRUST_ID = 2740;
	private static final int SCROLL_OF_ELF_TRUST_ID = 2741;
	private static final int SCROLL_OF_DWARF_TRUST_ID = 2742;
	private static final int SCROLL_OF_ORC_TRUST_ID = 2743;
	private static final int RECOMMENDATION_OF_HOLLIN_ID = 2744;
	private static final int ORDER_OF_OZZY_ID = 2745;
	private static final int BREATH_OF_WINDS_ID = 2746;
	private static final int SEED_OF_VERDURE_ID = 2747;
	private static final int LETTER_OF_THIFIELL_ID = 2748;
	private static final int BLOOD_OF_GUARDIAN_BASILISK_ID = 2749;
	private static final int GIANT_APHID_ID = 2750;
	private static final int STAKATOS_FLUIDS_ID = 2751;
	private static final int BASILISK_PLASMA_ID = 2752;
	private static final int HONEY_DEW_ID = 2753;
	private static final int STAKATO_ICHOR_ID = 2754;
	private static final int ORDER_OF_CLAYTON_ID = 2755;
	private static final int PARASITE_OF_LOTA_ID = 2756;
	private static final int LETTER_TO_MANAKIA_ID = 2757;
	private static final int LETTER_OF_MANAKIA_ID = 2758;
	private static final int LETTER_TO_NICHOLA_ID = 2759;
	private static final int ORDER_OF_NICHOLA_ID = 2760;
	private static final int HEART_OF_PORTA_ID = 2761;
	private static final int RewardExp = 39571;
	private static final int RewardSP = 2500;
	
	public _217_TestimonyOfTrust()
	{
		super(false);
		addStartNpc(30191);
		addTalkId(30031);
		addTalkId(30154);
		addTalkId(30358);
		addTalkId(30464);
		addTalkId(30515);
		addTalkId(30531);
		addTalkId(30565);
		addTalkId(30621);
		addTalkId(30657);
		addKillId(20013);
		addKillId(20157);
		addKillId(20019);
		addKillId(20213);
		addKillId(20230);
		addKillId(20232);
		addKillId(20234);
		addKillId(20036);
		addKillId(20044);
		addKillId(27120);
		addKillId(27121);
		addKillId(20550);
		addKillId(20553);
		addKillId(20082);
		addKillId(20084);
		addKillId(20086);
		addKillId(20087);
		addKillId(20088);
		addQuestItem(2740, 2741, 2742, 2743, 2746, 2747, 2745, 1558, 2755, 2752, 2754, 2753, 1556, 2748, 2739, 2738, 2758, 2757, 2756, 2737, 2759, 2761, 2760, 2744, 2749, 2751, 2750);
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
		if(event.equalsIgnoreCase("1"))
		{
			if(!st.getPlayer().getVarB("dd2"))
			{
				st.giveItems(7562, 96);
				st.getPlayer().setVar("dd2", "1", -1);
			}
			htmltext = "hollin_q0217_04.htm";
			st.setCond(1);
			st.set("id", "0");
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(1558, 1);
			st.giveItems(1556, 1);
		}
		else if(event.equalsIgnoreCase("30154_1"))
		{
			htmltext = "ozzy_q0217_02.htm";
		}
		else if(event.equalsIgnoreCase("30154_2"))
		{
			htmltext = "ozzy_q0217_03.htm";
			st.takeItems(1558, 1);
			st.giveItems(2745, 1);
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("30358_1"))
		{
			htmltext = "tetrarch_thifiell_q0217_02.htm";
			st.takeItems(1556, 1);
			st.giveItems(2748, 1);
			st.setCond(5);
		}
		else if(event.equalsIgnoreCase("30657_1"))
		{
			if(st.getPlayer().getLevel() >= 38)
			{
				htmltext = "cardinal_seresin_q0217_03.htm";
				st.takeItems(2739, 1);
				st.giveItems(2738, 1);
				st.giveItems(2737, 1);
				st.setCond(12);
			}
			else
			{
				htmltext = "cardinal_seresin_q0217_02.htm";
			}
		}
		else if(event.equalsIgnoreCase("30565_1"))
		{
			htmltext = "kakai_the_lord_of_flame_q0217_02.htm";
			st.takeItems(2738, 1);
			st.giveItems(2757, 1);
			st.setCond(13);
		}
		else if(event.equalsIgnoreCase("30515_1"))
		{
			htmltext = "seer_manakia_q0217_02.htm";
			st.takeItems(2757, 1);
			st.setCond(14);
		}
		else if(event.equalsIgnoreCase("30531_1"))
		{
			htmltext = "first_elder_lockirin_q0217_02.htm";
			st.takeItems(2737, 1);
			st.giveItems(2759, 1);
			st.setCond(18);
		}
		else if(event.equalsIgnoreCase("30621_1"))
		{
			htmltext = "maestro_nikola_q0217_02.htm";
			st.takeItems(2759, 1);
			st.giveItems(2760, 1);
			st.setCond(19);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		if(st.getQuestItemsCount(2734) > 0)
		{
			st.exitCurrentQuest(true);
			return "completed";
		}
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30191)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() == Race.human)
				{
					if(st.getPlayer().getLevel() >= 37)
					{
						htmltext = "hollin_q0217_03.htm";
					}
					else
					{
						htmltext = "hollin_q0217_01.htm";
						st.exitCurrentQuest(true);
					}
				}
				else
				{
					htmltext = "hollin_q0217_02.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 9 && st.getQuestItemsCount(2741) > 0 && st.getQuestItemsCount(2740) > 0)
			{
				htmltext = "hollin_q0217_05.htm";
				st.takeItems(2740, 1);
				st.takeItems(2741, 1);
				st.giveItems(2739, 1);
				st.setCond(10);
			}
			else if(cond == 22 && st.getQuestItemsCount(2742) > 0 && st.getQuestItemsCount(2743) > 0)
			{
				htmltext = "hollin_q0217_06.htm";
				st.takeItems(2742, 1);
				st.takeItems(2743, 1);
				st.giveItems(2744, 1);
				st.setCond(23);
			}
			else if(cond == 19)
			{
				htmltext = "hollin_q0217_07.htm";
			}
			else if(cond == 1)
			{
				htmltext = "hollin_q0217_08.htm";
			}
			else if(cond == 8)
			{
				htmltext = "hollin_q0217_09.htm";
			}
		}
		else if(npcId == 30154)
		{
			if(cond == 1 && st.getQuestItemsCount(1558) > 0)
			{
				htmltext = "ozzy_q0217_01.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(2745) > 0)
			{
				htmltext = "ozzy_q0217_04.htm";
			}
			else if(cond == 3 && st.getQuestItemsCount(2746) > 0 && st.getQuestItemsCount(2747) > 0)
			{
				htmltext = "ozzy_q0217_05.htm";
				st.takeItems(2746, 1);
				st.takeItems(2747, 1);
				st.takeItems(2745, 1);
				st.giveItems(2741, 1);
				st.setCond(4);
			}
			else if(cond == 4)
			{
				htmltext = "ozzy_q0217_06.htm";
			}
		}
		else if(npcId == 30358)
		{
			if(cond == 4 && st.getQuestItemsCount(1556) > 0)
			{
				htmltext = "tetrarch_thifiell_q0217_01.htm";
			}
			else if(cond == 8 && st.getQuestItemsCount(2754) + st.getQuestItemsCount(2753) + st.getQuestItemsCount(2752) == 3)
			{
				st.takeItems(2752, 1);
				st.takeItems(2754, 1);
				st.takeItems(2753, 1);
				st.giveItems(2740, 1);
				st.setCond(9);
				htmltext = "tetrarch_thifiell_q0217_03.htm";
			}
			else if(cond == 7)
			{
				htmltext = "tetrarch_thifiell_q0217_04.htm";
			}
			else if(cond == 5)
			{
				htmltext = "tetrarch_thifiell_q0217_05.htm";
			}
		}
		else if(npcId == 30464)
		{
			if(cond == 5 && st.getQuestItemsCount(2748) > 0)
			{
				htmltext = "magister_clayton_q0217_01.htm";
				st.takeItems(2748, 1);
				st.giveItems(2755, 1);
				st.setCond(6);
			}
			else if(cond == 6 && st.getQuestItemsCount(2755) > 0 && st.getQuestItemsCount(2754) + st.getQuestItemsCount(2753) + st.getQuestItemsCount(2752) < 3)
			{
				htmltext = "magister_clayton_q0217_02.htm";
			}
			else if(cond == 7 && st.getQuestItemsCount(2755) > 0 && st.getQuestItemsCount(2754) + st.getQuestItemsCount(2753) + st.getQuestItemsCount(2752) == 3)
			{
				st.takeItems(2755, 1);
				st.setCond(8);
				htmltext = "magister_clayton_q0217_03.htm";
			}
		}
		else if(npcId == 30657)
		{
			if((cond == 10 || cond == 11) && st.getQuestItemsCount(2739) > 0 && st.getPlayer().getLevel() >= 38)
			{
				htmltext = "cardinal_seresin_q0217_01.htm";
			}
			else if((cond == 10 || cond == 11) && st.getPlayer().getLevel() < 38)
			{
				htmltext = "cardinal_seresin_q0217_02.htm";
				if(cond == 10)
				{
					st.setCond(11);
				}
			}
			else if(cond == 18)
			{
				htmltext = "cardinal_seresin_q0217_05.htm";
			}
		}
		else if(npcId == 30565)
		{
			if(cond == 12 && st.getQuestItemsCount(2738) > 0)
			{
				htmltext = "kakai_the_lord_of_flame_q0217_01.htm";
			}
			else if(cond == 13)
			{
				htmltext = "kakai_the_lord_of_flame_q0217_03.htm";
			}
			else if(cond == 16)
			{
				htmltext = "kakai_the_lord_of_flame_q0217_04.htm";
				st.takeItems(2758, 1);
				st.giveItems(2743, 1);
				st.setCond(17);
			}
			else if(cond >= 17)
			{
				htmltext = "kakai_the_lord_of_flame_q0217_05.htm";
			}
		}
		else if(npcId == 30515)
		{
			if(cond == 13 && st.getQuestItemsCount(2757) > 0)
			{
				htmltext = "seer_manakia_q0217_01.htm";
			}
			else if(cond == 14 && st.getQuestItemsCount(2756) < 10)
			{
				htmltext = "seer_manakia_q0217_03.htm";
			}
			else if(cond == 15 && st.getQuestItemsCount(2756) == 10)
			{
				htmltext = "seer_manakia_q0217_04.htm";
				st.takeItems(2756, -1);
				st.giveItems(2758, 1);
				st.setCond(16);
			}
			else if(cond == 16)
			{
				htmltext = "seer_manakia_q0217_05.htm";
			}
		}
		else if(npcId == 30531)
		{
			if(cond == 17 && st.getQuestItemsCount(2737) > 0)
			{
				htmltext = "first_elder_lockirin_q0217_01.htm";
			}
			else if(cond == 18)
			{
				htmltext = "first_elder_lockirin_q0217_03.htm";
			}
			else if(cond == 21)
			{
				htmltext = "first_elder_lockirin_q0217_04.htm";
				st.giveItems(2742, 1);
				st.setCond(22);
			}
			else if(cond == 22)
			{
				htmltext = "first_elder_lockirin_q0217_05.htm";
			}
		}
		else if(npcId == 30621)
		{
			if(cond == 18 && st.getQuestItemsCount(2759) > 0)
			{
				htmltext = "maestro_nikola_q0217_01.htm";
			}
			else if(cond == 19 && st.getQuestItemsCount(2761) < 1)
			{
				htmltext = "maestro_nikola_q0217_03.htm";
			}
			else if(cond == 20 && st.getQuestItemsCount(2761) >= 1)
			{
				htmltext = "maestro_nikola_q0217_04.htm";
				st.takeItems(2761, -1);
				st.takeItems(2760, 1);
				st.setCond(21);
			}
			else if(cond == 21)
			{
				htmltext = "maestro_nikola_q0217_05.htm";
			}
		}
		else if(npcId == 30031 && cond == 23 && st.getQuestItemsCount(2744) > 0)
		{
			htmltext = "quilt_q0217_01.htm";
			st.takeItems(2744, -1);
			st.giveItems(2734, 1);
			if(!st.getPlayer().getVarB("prof2.2"))
			{
				st.addExpAndSp(39571, 2500);
				st.getPlayer().setVar("prof2.2", "1", -1);
			}
			st.playSound("ItemSound.quest_finish");
			st.unset("cond");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 20036 || npcId == 20044)
		{
			if(cond == 2 && st.getQuestItemsCount(2746) == 0)
			{
				st.set("id", String.valueOf(st.getInt("id") + 1));
				if(Rnd.chance(st.getInt("id") * 33))
				{
					st.addSpawn(27120);
					st.playSound("Itemsound.quest_before_battle");
				}
			}
		}
		else if(npcId == 20013 || npcId == 20019)
		{
			if(cond == 2 && st.getQuestItemsCount(2747) == 0)
			{
				st.set("id", String.valueOf(st.getInt("id") + 1));
				if(Rnd.chance(st.getInt("id") * 33))
				{
					st.addSpawn(27121);
					st.playSound("Itemsound.quest_before_battle");
				}
			}
		}
		else if(npcId == 27120)
		{
			if(cond == 2 && st.getQuestItemsCount(2746) == 0)
			{
				if(st.getQuestItemsCount(2747) > 0)
				{
					st.giveItems(2746, 1);
					st.setCond(3);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.giveItems(2746, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 27121)
		{
			if(cond == 2 && st.getQuestItemsCount(2747) == 0)
			{
				if(st.getQuestItemsCount(2746) > 0)
				{
					st.giveItems(2747, 1);
					st.setCond(3);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.giveItems(2747, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 20550)
		{
			if(cond == 6 && st.getQuestItemsCount(2749) < 10 && st.getQuestItemsCount(2755) > 0 && st.getQuestItemsCount(2752) == 0)
			{
				if(st.getQuestItemsCount(2749) == 9)
				{
					st.takeItems(2749, -1);
					st.giveItems(2752, 1);
					if(st.getQuestItemsCount(2754) + st.getQuestItemsCount(2752) + st.getQuestItemsCount(2753) == 3)
					{
						st.setCond(7);
					}
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.giveItems(2749, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 20157 || npcId == 20230 || npcId == 20232 || npcId == 20234)
		{
			if(cond == 6 && st.getQuestItemsCount(2751) < 10 && st.getQuestItemsCount(2755) > 0 && st.getQuestItemsCount(2754) == 0)
			{
				if(st.getQuestItemsCount(2751) == 9)
				{
					st.takeItems(2751, -1);
					st.giveItems(2754, 1);
					if(st.getQuestItemsCount(2754) + st.getQuestItemsCount(2752) + st.getQuestItemsCount(2753) == 3)
					{
						st.setCond(7);
					}
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.giveItems(2751, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 20082 || npcId == 20086 || npcId == 20087 || npcId == 20084 || npcId == 20088)
		{
			if(cond == 6 && st.getQuestItemsCount(2750) < 10 && st.getQuestItemsCount(2755) > 0 && st.getQuestItemsCount(2753) == 0)
			{
				if(st.getQuestItemsCount(2750) == 9)
				{
					st.takeItems(2750, -1);
					st.giveItems(2753, 1);
					if(st.getQuestItemsCount(2754) + st.getQuestItemsCount(2752) + st.getQuestItemsCount(2753) == 3)
					{
						st.setCond(7);
					}
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.giveItems(2750, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 20553)
		{
			if(cond == 14 && st.getQuestItemsCount(2756) < 10 && Rnd.chance(50))
			{
				if(st.getQuestItemsCount(2756) == 9)
				{
					st.giveItems(2756, 1);
					st.setCond(15);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.giveItems(2756, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 20213 && cond == 19 && st.getQuestItemsCount(2761) < 1)
		{
			st.giveItems(2761, 1);
			st.setCond(20);
			st.playSound("ItemSound.quest_middle");
		}
		return null;
	}
}