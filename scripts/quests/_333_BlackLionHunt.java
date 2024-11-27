package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _333_BlackLionHunt extends Quest implements ScriptFile
{
	private final int BLACK_LION_MARK = 1369;
	private final int CARGO_BOX1 = 3440;
	private final int UNDEAD_ASH = 3848;
	private final int BLOODY_AXE_INSIGNIAS = 3849;
	private final int DELU_FANG = 3850;
	private final int STAKATO_TALONS = 3851;
	private final int SOPHIAS_LETTER1 = 3671;
	private final int SOPHIAS_LETTER2 = 3672;
	private final int SOPHIAS_LETTER3 = 3673;
	private final int SOPHIAS_LETTER4 = 3674;
	private final int LIONS_CLAW = 3675;
	private final int LIONS_EYE = 3676;
	private final int GUILD_COIN = 3677;
	private final int COMPLETE_STATUE = 3461;
	private final int COMPLETE_TABLET = 3466;
	private final int ALACRITY_POTION = 735;
	private final int SCROLL_ESCAPE = 736;
	private final int SOULSHOT_D = 1463;
	private final int SPIRITSHOT_D = 2510;
	private final int HEALING_POTION = 1061;
	private final int OPEN_BOX_PRICE = 650;
	private final int GLUDIO_APPLE = 3444;
	private final int CORN_MEAL = 3445;
	private final int WOLF_PELTS = 3446;
	private final int MONNSTONE = 3447;
	private final int GLUDIO_WEETS_FLOWER = 3448;
	private final int SPIDERSILK_ROPE = 3449;
	private final int ALEXANDRIT = 3450;
	private final int SILVER_TEA = 3451;
	private final int GOLEM_PART = 3452;
	private final int FIRE_EMERALD = 3453;
	private final int SILK_FROCK = 3454;
	private final int PORCELAN_URN = 3455;
	private final int IMPERIAL_DIAMOND = 3456;
	private final int STATUE_SHILIEN_HEAD = 3457;
	private final int STATUE_SHILIEN_TORSO = 3458;
	private final int STATUE_SHILIEN_ARM = 3459;
	private final int STATUE_SHILIEN_LEG = 3460;
	private final int FRAGMENT_ANCIENT_TABLE1 = 3462;
	private final int FRAGMENT_ANCIENT_TABLE2 = 3463;
	private final int FRAGMENT_ANCIENT_TABLE3 = 3464;
	private final int FRAGMENT_ANCIENT_TABLE4 = 3465;
	private final int Sophya = 30735;
	private final int Redfoot = 30736;
	private final int Rupio = 30471;
	private final int Undrias = 30130;
	private final int Lockirin = 30531;
	private final int Morgan = 30737;
	int[] statue_list = {STATUE_SHILIEN_HEAD, STATUE_SHILIEN_TORSO, STATUE_SHILIEN_ARM, STATUE_SHILIEN_LEG};
	int[] tablet_list = {FRAGMENT_ANCIENT_TABLE1, FRAGMENT_ANCIENT_TABLE2, FRAGMENT_ANCIENT_TABLE3, FRAGMENT_ANCIENT_TABLE4};
	int[][] DROPLIST = {{20160, 1, 1, 67, 29, UNDEAD_ASH}, {20171, 1, 1, 76, 31, UNDEAD_ASH}, {20197, 1, 1, 89, 25, UNDEAD_ASH}, {20200, 1, 1, 60, 28, UNDEAD_ASH}, {20201, 1, 1, 70, 29, UNDEAD_ASH}, {20202, 1, 0, 60, 24, UNDEAD_ASH}, {20198, 1, 1, 60, 35, UNDEAD_ASH}, {20207, 2, 1, 69, 29, BLOODY_AXE_INSIGNIAS}, {20208, 2, 1, 67, 32, BLOODY_AXE_INSIGNIAS}, {20209, 2, 1, 62, 33, BLOODY_AXE_INSIGNIAS}, {20210, 2, 1, 78, 23, BLOODY_AXE_INSIGNIAS}, {20211, 2, 1, 71, 22, BLOODY_AXE_INSIGNIAS}, {20251, 3, 1, 70, 30, DELU_FANG}, {20252, 3, 1, 67, 28, DELU_FANG}, {20253, 3, 1, 65, 26, DELU_FANG}, {27151, 3, 1, 69, 31, DELU_FANG}, {20157, 4, 1, 66, 32, STAKATO_TALONS}, {20230, 4, 1, 68, 26, STAKATO_TALONS}, {20232, 4, 1, 67, 28, STAKATO_TALONS}, {20234, 4, 1, 69, 32, STAKATO_TALONS}, {27152, 4, 1, 69, 32, STAKATO_TALONS}};
	
	public _333_BlackLionHunt()
	{
		super(false);
		addStartNpc(Sophya);
		addTalkId(Redfoot);
		addTalkId(Rupio);
		addTalkId(Undrias);
		addTalkId(Lockirin);
		addTalkId(Morgan);
		for(int i = 0;i < DROPLIST.length;++i)
		{
			addKillId(DROPLIST[i][0]);
		}
		addQuestItem(LIONS_CLAW, LIONS_EYE, GUILD_COIN, UNDEAD_ASH, BLOODY_AXE_INSIGNIAS, DELU_FANG, STAKATO_TALONS, SOPHIAS_LETTER1, SOPHIAS_LETTER2, SOPHIAS_LETTER3, SOPHIAS_LETTER4);
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
	
	public void giveRewards(QuestState st, int item, long count)
	{
		st.giveItems(57, 35 * count);
		st.takeItems(item, count);
		if(count >= 20)
		{
			st.giveItems(LIONS_CLAW, count / 20 * (long) st.getRateQuestsReward());
		}
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int part = st.getInt("part");
		if(event.equalsIgnoreCase("start"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			return "30735-01.htm";
		}
		if(event.equalsIgnoreCase("p1_t"))
		{
			st.set("part", "1");
			st.giveItems(SOPHIAS_LETTER1, 1);
			return "30735-02.htm";
		}
		if(event.equalsIgnoreCase("p2_t"))
		{
			st.set("part", "2");
			st.giveItems(SOPHIAS_LETTER2, 1);
			return "30735-03.htm";
		}
		if(event.equalsIgnoreCase("p3_t"))
		{
			st.set("part", "3");
			st.giveItems(SOPHIAS_LETTER3, 1);
			return "30735-04.htm";
		}
		if(event.equalsIgnoreCase("p4_t"))
		{
			st.set("part", "4");
			st.giveItems(SOPHIAS_LETTER4, 1);
			return "30735-05.htm";
		}
		if(event.equalsIgnoreCase("exit"))
		{
			st.exitCurrentQuest(true);
			return "30735-exit.htm";
		}
		if(event.equalsIgnoreCase("continue"))
		{
			long claw = st.getQuestItemsCount(LIONS_CLAW) / 10;
			long check_eye = st.getQuestItemsCount(LIONS_EYE);
			if(claw <= 0)
				return "30735-start.htm";
			st.giveItems(LIONS_EYE, claw);
			long eye = st.getQuestItemsCount(LIONS_EYE);
			st.takeItems(LIONS_CLAW, claw * 10);
			int ala_count = 3;
			int soul_count = 100;
			int soe_count = 20;
			int heal_count = 20;
			int spir_count = 50;
			if(eye > 9)
			{
				ala_count = 4;
				soul_count = 400;
				soe_count = 30;
				heal_count = 50;
				spir_count = 200;
			}
			else if(eye > 4)
			{
				spir_count = 100;
				soul_count = 200;
				heal_count = 25;
			}
			while(claw > 0)
			{
				int n = Rnd.get(5);
				if(n == 0)
				{
					st.giveItems(ALACRITY_POTION, Math.round((double) ala_count * st.getRateQuestsReward()));
				}
				else if(n == 1)
				{
					st.giveItems(SOULSHOT_D, Math.round((double) soul_count * st.getRateQuestsReward()));
				}
				else if(n == 2)
				{
					st.giveItems(SCROLL_ESCAPE, Math.round((double) soe_count * st.getRateQuestsReward()));
				}
				else if(n == 3)
				{
					st.giveItems(SPIRITSHOT_D, Math.round((double) spir_count * st.getRateQuestsReward()));
				}
				else if(n == 4)
				{
					st.giveItems(HEALING_POTION, Math.round((double) heal_count * st.getRateQuestsReward()));
				}
				--claw;
			}
			if(check_eye <= 0)
				return "30735-06.htm";
			return "30735-06.htm";
		}
		if(event.equalsIgnoreCase("leave"))
		{
			int order = part == 1 ? SOPHIAS_LETTER1 : part == 2 ? SOPHIAS_LETTER2 : part == 3 ? SOPHIAS_LETTER3 : part == 4 ? SOPHIAS_LETTER4 : 0;
			st.set("part", "0");
			if(order <= 0)
				return "30735-07.htm";
			st.takeItems(order, 1);
			return "30735-07.htm";
		}
		if(event.equalsIgnoreCase("f_info"))
		{
			int text = st.getInt("text");
			if(text >= 4)
				return "red_foor-01.htm";
			st.set("text", String.valueOf(text + 1));
			return "red_foor_text_" + Rnd.get(1, 19) + ".htm";
		}
		if(event.equalsIgnoreCase("f_give"))
		{
			if(st.getQuestItemsCount(CARGO_BOX1) <= 0)
				return "red_foor-no_box.htm";
			if(st.getQuestItemsCount(57) < (long) OPEN_BOX_PRICE)
				return "red_foor-no_adena.htm";
			st.takeItems(CARGO_BOX1, 1);
			st.takeItems(57, (long) OPEN_BOX_PRICE);
			int rand = Rnd.get(1, 162);
			if(rand < 21)
			{
				st.giveItems(GLUDIO_APPLE, 1);
				return "red_foor-02.htm";
			}
			if(rand < 41)
			{
				st.giveItems(CORN_MEAL, 1);
				return "red_foor-03.htm";
			}
			if(rand < 61)
			{
				st.giveItems(WOLF_PELTS, 1);
				return "red_foor-04.htm";
			}
			if(rand < 74)
			{
				st.giveItems(MONNSTONE, 1);
				return "red_foor-05.htm";
			}
			if(rand < 86)
			{
				st.giveItems(GLUDIO_WEETS_FLOWER, 1);
				return "red_foor-06.htm";
			}
			if(rand < 98)
			{
				st.giveItems(SPIDERSILK_ROPE, 1);
				return "red_foor-07.htm";
			}
			if(rand < 99)
			{
				st.giveItems(ALEXANDRIT, 1);
				return "red_foor-08.htm";
			}
			if(rand < 109)
			{
				st.giveItems(SILVER_TEA, 1);
				return "red_foor-09.htm";
			}
			if(rand < 119)
			{
				st.giveItems(GOLEM_PART, 1);
				return "red_foor-10.htm";
			}
			if(rand < 123)
			{
				st.giveItems(FIRE_EMERALD, 1);
				return "red_foor-11.htm";
			}
			if(rand < 127)
			{
				st.giveItems(SILK_FROCK, 1);
				return "red_foor-12.htm";
			}
			if(rand < 131)
			{
				st.giveItems(PORCELAN_URN, 1);
				return "red_foor-13.htm";
			}
			if(rand < 132)
			{
				st.giveItems(IMPERIAL_DIAMOND, 1);
				return "red_foor-13.htm";
			}
			if(rand < 147)
			{
				int random_stat = Rnd.get(4);
				if(random_stat == 3)
				{
					st.giveItems(STATUE_SHILIEN_HEAD, 1);
					return "red_foor-14.htm";
				}
				if(random_stat == 0)
				{
					st.giveItems(STATUE_SHILIEN_TORSO, 1);
					return "red_foor-14.htm";
				}
				if(random_stat == 1)
				{
					st.giveItems(STATUE_SHILIEN_ARM, 1);
					return "red_foor-14.htm";
				}
				if(random_stat != 2)
					return event;
				st.giveItems(STATUE_SHILIEN_LEG, 1);
				return "red_foor-14.htm";
			}
			if(rand > 162)
				return event;
			int random_tab = Rnd.get(4);
			if(random_tab == 0)
			{
				st.giveItems(FRAGMENT_ANCIENT_TABLE1, 1);
				return "red_foor-15.htm";
			}
			if(random_tab == 1)
			{
				st.giveItems(FRAGMENT_ANCIENT_TABLE2, 1);
				return "red_foor-15.htm";
			}
			if(random_tab == 2)
			{
				st.giveItems(FRAGMENT_ANCIENT_TABLE3, 1);
				return "red_foor-15.htm";
			}
			if(random_tab != 3)
				return event;
			st.giveItems(FRAGMENT_ANCIENT_TABLE4, 1);
			return "red_foor-15.htm";
		}
		if(event.equalsIgnoreCase("r_give_statue") || event.equalsIgnoreCase("r_give_tablet"))
		{
			int[] items = statue_list;
			int item = COMPLETE_STATUE;
			String pieces = "rupio-01.htm";
			String brockes = "rupio-02.htm";
			String complete = "rupio-03.htm";
			if(event.equalsIgnoreCase("r_give_tablet"))
			{
				items = tablet_list;
				item = COMPLETE_TABLET;
				pieces = "rupio-04.htm";
				brockes = "rupio-05.htm";
				complete = "rupio-06.htm";
			}
			int count = 0;
			int id;
			for(id = items[0];id <= items[items.length - 1];++id)
			{
				if(st.getQuestItemsCount(id) <= 0)
					continue;
				++count;
			}
			if(count > 3)
			{
				for(id = items[0];id <= items[items.length - 1];++id)
				{
					st.takeItems(id, 1);
				}
				if(!Rnd.chance(2))
					return brockes;
				st.giveItems(item, 1);
				return complete;
			}
			if(count >= 4 || count == 0)
				return "rupio-07.htm";
			return pieces;
		}
		if(event.equalsIgnoreCase("l_give"))
		{
			if(st.getQuestItemsCount(COMPLETE_TABLET) <= 0)
				return "lockirin-02.htm";
			st.takeItems(COMPLETE_TABLET, 1);
			st.giveItems(57, 30000);
			return "lockirin-01.htm";
		}
		if(event.equalsIgnoreCase("u_give"))
		{
			if(st.getQuestItemsCount(COMPLETE_STATUE) <= 0)
				return "undiras-02.htm";
			st.takeItems(COMPLETE_STATUE, 1);
			st.giveItems(57, 30000);
			return "undiras-01.htm";
		}
		if(event.equalsIgnoreCase("m_give"))
		{
			if(st.getQuestItemsCount(CARGO_BOX1) <= 0)
				return "morgan-03.htm";
			long coins = st.getQuestItemsCount(GUILD_COIN);
			long count = coins / 40;
			if(count > 2)
			{
				count = 2;
			}
			st.giveItems(GUILD_COIN, 1);
			st.giveItems(57, (1 + count) * 100);
			st.takeItems(CARGO_BOX1, 1);
			int rand = Rnd.get(0, 3);
			if(rand == 0)
			{
				return "morgan-01.htm";
			}
			if(rand != 1)
				return "morgan-02.htm";
			return "morgan-02.htm";
		}
		if(event.equalsIgnoreCase("start_parts"))
		{
			return "30735-08.htm";
		}
		if(event.equalsIgnoreCase("m_reward"))
		{
			return "morgan-05.htm";
		}
		if(event.equalsIgnoreCase("u_info"))
		{
			return "undiras-03.htm";
		}
		if(event.equalsIgnoreCase("l_info"))
		{
			return "lockirin-03.htm";
		}
		if(event.equalsIgnoreCase("p_redfoot"))
		{
			return "30735-09.htm";
		}
		if(event.equalsIgnoreCase("p_trader_info"))
		{
			return "30735-10.htm";
		}
		if(event.equalsIgnoreCase("start_chose_parts"))
		{
			return "30735-11.htm";
		}
		if(event.equalsIgnoreCase("p1_explanation"))
		{
			return "30735-12.htm";
		}
		if(event.equalsIgnoreCase("p2_explanation"))
		{
			return "30735-13.htm";
		}
		if(event.equalsIgnoreCase("p3_explanation"))
		{
			return "30735-14.htm";
		}
		if(event.equalsIgnoreCase("p4_explanation"))
		{
			return "30735-15.htm";
		}
		if(event.equalsIgnoreCase("f_more_help"))
		{
			return "red_foor-16.htm";
		}
		if(!event.equalsIgnoreCase("r_exit"))
			return event;
		return "30735-16.htm";
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 0)
		{
			st.setCond(0);
			st.set("part", "0");
			st.set("text", "0");
			if(npcId == Sophya)
			{
				if(st.getQuestItemsCount(BLACK_LION_MARK) > 0)
				{
					if(st.getPlayer().getLevel() > 24)
					{
						return "30735-17.htm";
					}
					st.exitCurrentQuest(true);
					return "30735-18.htm";
				}
				st.exitCurrentQuest(true);
				return "30735-19.htm";
			}
		}
		else
		{
			int part = st.getInt("part");
			if(npcId == Sophya)
			{
				int item;
				if(part == 1)
				{
					item = UNDEAD_ASH;
				}
				else if(part == 2)
				{
					item = BLOODY_AXE_INSIGNIAS;
				}
				else if(part == 3)
				{
					item = DELU_FANG;
				}
				else if(part == 4)
				{
					item = STAKATO_TALONS;
				}
				else
				{
					return "30735-20.htm";
				}
				long count = st.getQuestItemsCount(item);
				long box = st.getQuestItemsCount(CARGO_BOX1);
				if(box > 0 && count > 0)
				{
					giveRewards(st, item, count);
					return "30735-21.htm";
				}
				if(box > 0)
				{
					return "30735-22.htm";
				}
				if(count > 0)
				{
					giveRewards(st, item, count);
					return "30735-23.htm";
				}
				return "30735-24.htm";
			}
			if(npcId == Redfoot)
			{
				if(st.getQuestItemsCount(CARGO_BOX1) > 0)
				{
					return "red_foor_text_20.htm";
				}
				return "red_foor_text_21.htm";
			}
			if(npcId == Rupio)
			{
				int i;
				int count = 0;
				for(i = 3457;i <= 3460;++i)
				{
					if(st.getQuestItemsCount(i) <= 0)
						continue;
					++count;
				}
				for(i = 3462;i <= 3465;++i)
				{
					if(st.getQuestItemsCount(i) <= 0)
						continue;
					++count;
				}
				if(count > 0)
				{
					return "rupio-08.htm";
				}
				return "rupio-07.htm";
			}
			if(npcId == Undrias)
			{
				if(st.getQuestItemsCount(COMPLETE_STATUE) > 0)
				{
					return "undiras-04.htm";
				}
				int count = 0;
				for(int i = 3457;i <= 3460;++i)
				{
					if(st.getQuestItemsCount(i) <= 0)
						continue;
					++count;
				}
				if(count > 0)
				{
					return "undiras-05.htm";
				}
				return "undiras-02.htm";
			}
			if(npcId == Lockirin)
			{
				if(st.getQuestItemsCount(COMPLETE_TABLET) > 0)
				{
					return "lockirin-04.htm";
				}
				int count = 0;
				for(int i = 3462;i <= 3465;++i)
				{
					if(st.getQuestItemsCount(i) <= 0)
						continue;
					++count;
				}
				if(count > 0)
				{
					return "lockirin-05.htm";
				}
				return "lockirin-06.htm";
			}
			if(npcId == Morgan)
			{
				if(st.getQuestItemsCount(CARGO_BOX1) > 0)
				{
					return "morgan-06.htm";
				}
				return "morgan-07.htm";
			}
		}
		String htmltext = "noquest";
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		boolean on_npc = false;
		int part = 0;
		int allowDrop = 0;
		int chancePartItem = 0;
		int chanceBox = 0;
		int partItem = 0;
		for(int i = 0;i < DROPLIST.length;++i)
		{
			if(DROPLIST[i][0] != npcId)
				continue;
			part = DROPLIST[i][1];
			allowDrop = DROPLIST[i][2];
			chancePartItem = DROPLIST[i][3];
			chanceBox = DROPLIST[i][4];
			partItem = DROPLIST[i][5];
			on_npc = true;
		}
		if(on_npc)
		{
			int rand = Rnd.get(1, 100);
			int rand2 = Rnd.get(1, 100);
			if(allowDrop == 1 && st.getInt("part") == part && rand < chancePartItem)
			{
				st.giveItems(partItem, npcId == 27152 ? 8 : 1);
				st.playSound("ItemSound.quest_itemget");
				if(rand2 < chanceBox)
				{
					st.giveItems(CARGO_BOX1, 1);
					if(rand > chancePartItem)
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
		}
		if(Rnd.chance(4) && (npcId == 20251 || npcId == 20252 || npcId == 20253))
		{
			st.addSpawn(21105);
			st.addSpawn(21105);
		}
		if(npcId == 20157 || npcId == 20230 || npcId == 20232 || npcId == 20234)
		{
			if(Rnd.chance(2))
			{
				st.addSpawn(27152);
			}
			if(Rnd.chance(15))
			{
				st.giveItems(CARGO_BOX1, 1);
			}
		}
		return null;
	}
}