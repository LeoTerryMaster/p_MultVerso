package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Drop;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class _232_TestOfLord extends Quest implements ScriptFile
{
	private static final int Somak = 30510;
	private static final int Manakia = 30515;
	private static final int Jakal = 30558;
	private static final int Sumari = 30564;
	private static final int Kakai = 30565;
	private static final int Varkees = 30566;
	private static final int Tantus = 30567;
	private static final int Hatos = 30568;
	private static final int Takuna = 30641;
	private static final int Chianta = 30642;
	private static final int First_Orc = 30643;
	private static final int Ancestor_Martankus = 30649;
	private static final int Marsh_Spider = 20233;
	private static final int Breka_Orc_Shaman = 20269;
	private static final int Breka_Orc_Overlord = 20270;
	private static final int Enchanted_Monstereye = 20564;
	private static final int Timak_Orc = 20583;
	private static final int Timak_Orc_Archer = 20584;
	private static final int Timak_Orc_Soldier = 20585;
	private static final int Timak_Orc_Warrior = 20586;
	private static final int Timak_Orc_Shaman = 20587;
	private static final int Timak_Orc_Overlord = 20588;
	private static final int Ragna_Orc_Overlord = 20778;
	private static final int Ragna_Orc_Seer = 20779;
	private static final int MARK_OF_LORD = 3390;
	private static final int BONE_ARROW = 1341;
	private static final int Dimensional_Diamond = 7562;
	private static final int TIMAK_ORC_SKULL = 3403;
	private static final int BREKA_ORC_FANG = 3398;
	private static final int RAGNA_ORC_HEAD = 3414;
	private static final int RAGNA_CHIEF_NOTICE = 3415;
	private static final int MARSH_SPIDER_FEELER = 3407;
	private static final int MARSH_SPIDER_FEET = 3408;
	private static final int CORNEA_OF_EN_MONSTEREYE = 3410;
	private static final int ORDEAL_NECKLACE = 3391;
	private static final int VARKEES_CHARM = 3392;
	private static final int TANTUS_CHARM = 3393;
	private static final int HATOS_CHARM = 3394;
	private static final int TAKUNA_CHARM = 3395;
	private static final int CHIANTA_CHARM = 3396;
	private static final int MANAKIAS_ORDERS = 3397;
	private static final int MANAKIAS_AMULET = 3399;
	private static final int HUGE_ORC_FANG = 3400;
	private static final int SUMARIS_LETTER = 3401;
	private static final int URUTU_BLADE = 3402;
	private static final int SWORD_INTO_SKULL = 3404;
	private static final int NERUGA_AXE_BLADE = 3405;
	private static final int AXE_OF_CEREMONY = 3406;
	private static final int HANDIWORK_SPIDER_BROOCH = 3409;
	private static final int MONSTEREYE_WOODCARVING = 3411;
	private static final int BEAR_FANG_NECKLACE = 3412;
	private static final int MARTANKUS_CHARM = 3413;
	private static final int IMMORTAL_FLAME = 3416;
	private static final Map<Integer, Drop> DROPLIST = new HashMap<>();
	
	public _232_TestOfLord()
	{
		super(false);
		addStartNpc(Kakai);
		addTalkId(Somak);
		addTalkId(Manakia);
		addTalkId(Jakal);
		addTalkId(Sumari);
		addTalkId(Varkees);
		addTalkId(Tantus);
		addTalkId(Hatos);
		addTalkId(Takuna);
		addTalkId(Chianta);
		addTalkId(First_Orc);
		addTalkId(Ancestor_Martankus);
		DROPLIST.put(Timak_Orc, new Drop(1, 10, 50).addItem(TIMAK_ORC_SKULL));
		DROPLIST.put(Timak_Orc_Archer, new Drop(1, 10, 55).addItem(TIMAK_ORC_SKULL));
		DROPLIST.put(Timak_Orc_Soldier, new Drop(1, 10, 60).addItem(TIMAK_ORC_SKULL));
		DROPLIST.put(Timak_Orc_Warrior, new Drop(1, 10, 65).addItem(TIMAK_ORC_SKULL));
		DROPLIST.put(Timak_Orc_Shaman, new Drop(1, 10, 70).addItem(TIMAK_ORC_SKULL));
		DROPLIST.put(Timak_Orc_Overlord, new Drop(1, 10, 75).addItem(TIMAK_ORC_SKULL));
		DROPLIST.put(Breka_Orc_Shaman, new Drop(1, 20, 40).addItem(BREKA_ORC_FANG));
		DROPLIST.put(Breka_Orc_Overlord, new Drop(1, 20, 50).addItem(BREKA_ORC_FANG));
		DROPLIST.put(Ragna_Orc_Overlord, new Drop(4, 1, 100).addItem(RAGNA_ORC_HEAD));
		DROPLIST.put(Ragna_Orc_Seer, new Drop(4, 1, 100).addItem(RAGNA_CHIEF_NOTICE));
		DROPLIST.put(Marsh_Spider, new Drop(1, 10, 100).addItem(MARSH_SPIDER_FEELER).addItem(MARSH_SPIDER_FEET));
		DROPLIST.put(Enchanted_Monstereye, new Drop(1, 20, 90).addItem(CORNEA_OF_EN_MONSTEREYE));
		Iterator<Integer> iterator = DROPLIST.keySet().iterator();
		while(iterator.hasNext())
		{
			int kill_id = iterator.next();
			addKillId(kill_id);
		}
		for(Drop drop : DROPLIST.values())
		{
			for(int item_id : drop.itemList)
			{
				if(isQuestItem(item_id))
					continue;
				addQuestItem(item_id);
			}
		}
		addQuestItem(ORDEAL_NECKLACE);
		addQuestItem(VARKEES_CHARM);
		addQuestItem(TANTUS_CHARM);
		addQuestItem(HATOS_CHARM);
		addQuestItem(TAKUNA_CHARM);
		addQuestItem(CHIANTA_CHARM);
		addQuestItem(MANAKIAS_ORDERS);
		addQuestItem(MANAKIAS_AMULET);
		addQuestItem(HUGE_ORC_FANG);
		addQuestItem(SUMARIS_LETTER);
		addQuestItem(URUTU_BLADE);
		addQuestItem(SWORD_INTO_SKULL);
		addQuestItem(NERUGA_AXE_BLADE);
		addQuestItem(AXE_OF_CEREMONY);
		addQuestItem(HANDIWORK_SPIDER_BROOCH);
		addQuestItem(MONSTEREYE_WOODCARVING);
		addQuestItem(BEAR_FANG_NECKLACE);
		addQuestItem(MARTANKUS_CHARM);
		addQuestItem(IMMORTAL_FLAME);
	}
	
	private static void spawn_First_Orc(QuestState st)
	{
		st.addSpawn(First_Orc, 21036, -107690, -3038);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int state = st.getState();
		if(state == 1)
		{
			if(event.equalsIgnoreCase("30565-05.htm"))
			{
				st.giveItems(ORDEAL_NECKLACE, 1);
				if(!st.getPlayer().getVarB("dd3"))
				{
					st.giveItems(Dimensional_Diamond, 92);
					st.getPlayer().setVar("dd3", "1", -1);
				}
				st.setState(2);
				st.setCond(1);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if(state == 2)
		{
			if(event.equalsIgnoreCase("30565-12.htm") && st.getQuestItemsCount(IMMORTAL_FLAME) > 0)
			{
				st.takeItems(IMMORTAL_FLAME, -1);
				st.giveItems(MARK_OF_LORD, 1);
				if(!st.getPlayer().getVarB("prof2.3"))
				{
					st.addExpAndSp(92955, 16250);
					st.getPlayer().setVar("prof2.3", "1", -1);
				}
				st.playSound("ItemSound.quest_finish");
				st.unset("cond");
				st.exitCurrentQuest(true);
			}
			else if(event.equalsIgnoreCase("30565-08.htm"))
			{
				st.takeItems(SWORD_INTO_SKULL, -1);
				st.takeItems(AXE_OF_CEREMONY, -1);
				st.takeItems(MONSTEREYE_WOODCARVING, -1);
				st.takeItems(HANDIWORK_SPIDER_BROOCH, -1);
				st.takeItems(ORDEAL_NECKLACE, -1);
				st.takeItems(HUGE_ORC_FANG, -1);
				st.giveItems(BEAR_FANG_NECKLACE, 1);
				st.playSound("ItemSound.quest_middle");
				st.setCond(3);
			}
			else if(event.equalsIgnoreCase("30566-02.htm"))
			{
				st.giveItems(VARKEES_CHARM, 1);
			}
			else if(event.equalsIgnoreCase("30567-02.htm"))
			{
				st.giveItems(TANTUS_CHARM, 1);
			}
			else if(event.equalsIgnoreCase("30558-02.htm") && st.getQuestItemsCount(57) >= 1000)
			{
				st.takeItems(57, 1000);
				st.giveItems(NERUGA_AXE_BLADE, 1);
				st.playSound("ItemSound.quest_middle");
			}
			else if(event.equalsIgnoreCase("30568-02.htm"))
			{
				st.giveItems(HATOS_CHARM, 1);
			}
			else if(event.equalsIgnoreCase("30641-02.htm"))
			{
				st.giveItems(TAKUNA_CHARM, 1);
			}
			else if(event.equalsIgnoreCase("30642-02.htm"))
			{
				st.giveItems(CHIANTA_CHARM, 1);
			}
			else if(event.equalsIgnoreCase("30649-04.htm") && st.getQuestItemsCount(BEAR_FANG_NECKLACE) > 0)
			{
				st.takeItems(BEAR_FANG_NECKLACE, -1);
				st.giveItems(MARTANKUS_CHARM, 1);
				st.playSound("ItemSound.quest_middle");
				st.setCond(4);
			}
			else if(event.equalsIgnoreCase("30649-07.htm"))
			{
				st.setCond(6);
				spawn_First_Orc(st);
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		if(st.getQuestItemsCount(MARK_OF_LORD) > 0)
		{
			st.exitCurrentQuest(true);
			return "completed";
		}
		int _state = st.getState();
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(_state == 1)
		{
			if(npcId != Kakai)
			{
				return "noquest";
			}
			if(st.getPlayer().getRace() != Race.orc)
			{
				st.exitCurrentQuest(true);
				return "30565-01.htm";
			}
			if(st.getPlayer().getClassId().getId() != 50)
			{
				st.exitCurrentQuest(true);
				return "30565-02.htm";
			}
			if(st.getPlayer().getLevel() < 39)
			{
				st.exitCurrentQuest(true);
				return "30565-03.htm";
			}
			st.setCond(0);
			return "30565-04.htm";
		}
		if(_state != 2 || cond < 1)
		{
			return "noquest";
		}
		long ORDEAL_NECKLACE_COUNT = st.getQuestItemsCount(ORDEAL_NECKLACE);
		long HUGE_ORC_FANG_COUNT = st.getQuestItemsCount(HUGE_ORC_FANG);
		long SWORD_INTO_SKULL_COUNT = st.getQuestItemsCount(SWORD_INTO_SKULL);
		long AXE_OF_CEREMONY_COUNT = st.getQuestItemsCount(AXE_OF_CEREMONY);
		long MONSTEREYE_WOODCARVING_COUNT = st.getQuestItemsCount(MONSTEREYE_WOODCARVING);
		long HANDIWORK_SPIDER_BROOCH_COUNT = st.getQuestItemsCount(HANDIWORK_SPIDER_BROOCH);
		long BEAR_FANG_NECKLACE_COUNT = st.getQuestItemsCount(BEAR_FANG_NECKLACE);
		long MARTANKUS_CHARM_COUNT = st.getQuestItemsCount(MARTANKUS_CHARM);
		long IMMORTAL_FLAME_COUNT = st.getQuestItemsCount(IMMORTAL_FLAME);
		long VARKEES_CHARM_COUNT = st.getQuestItemsCount(VARKEES_CHARM);
		long MANAKIAS_AMULET_COUNT = st.getQuestItemsCount(MANAKIAS_AMULET);
		long MANAKIAS_ORDERS_COUNT = st.getQuestItemsCount(MANAKIAS_ORDERS);
		long BREKA_ORC_FANG_COUNT = st.getQuestItemsCount(BREKA_ORC_FANG);
		long TANTUS_CHARM_COUNT = st.getQuestItemsCount(TANTUS_CHARM);
		long NERUGA_AXE_BLADE_COUNT = st.getQuestItemsCount(NERUGA_AXE_BLADE);
		long HATOS_CHARM_COUNT = st.getQuestItemsCount(HATOS_CHARM);
		long URUTU_BLADE_COUNT = st.getQuestItemsCount(URUTU_BLADE);
		long TIMAK_ORC_SKULL_COUNT = st.getQuestItemsCount(TIMAK_ORC_SKULL);
		long SUMARIS_LETTER_COUNT = st.getQuestItemsCount(SUMARIS_LETTER);
		long TAKUNA_CHARM_COUNT = st.getQuestItemsCount(TAKUNA_CHARM);
		if(npcId == Kakai)
		{
			if(ORDEAL_NECKLACE_COUNT > 0)
			{
				return cond1Complete(st) ? "30565-07.htm" : "30565-06.htm";
			}
			if(BEAR_FANG_NECKLACE_COUNT > 0)
			{
				return "30565-09.htm";
			}
			if(MARTANKUS_CHARM_COUNT > 0)
			{
				return "30565-10.htm";
			}
			if(IMMORTAL_FLAME_COUNT > 0)
			{
				return "30565-11.htm";
			}
		}
		if(npcId == Varkees && ORDEAL_NECKLACE_COUNT > 0)
		{
			if(HUGE_ORC_FANG_COUNT > 0)
			{
				return "30566-05.htm";
			}
			if(VARKEES_CHARM_COUNT == 0)
			{
				return "30566-01.htm";
			}
			if(MANAKIAS_AMULET_COUNT == 0)
			{
				return "30566-03.htm";
			}
			st.takeItems(VARKEES_CHARM, -1);
			st.takeItems(MANAKIAS_AMULET, -1);
			st.giveItems(HUGE_ORC_FANG, 1);
			if(cond1Complete(st))
			{
				st.playSound("ItemSound.quest_jackpot");
				st.setCond(2);
			}
			else
			{
				st.playSound("ItemSound.quest_middle");
			}
			return "30566-04.htm";
		}
		if(npcId == Manakia && ORDEAL_NECKLACE_COUNT > 0)
		{
			if(VARKEES_CHARM_COUNT > 0 && HUGE_ORC_FANG_COUNT == 0)
			{
				if(MANAKIAS_AMULET_COUNT == 0)
				{
					if(MANAKIAS_ORDERS_COUNT == 0)
					{
						st.giveItems(MANAKIAS_ORDERS, 1);
						return "30515-01.htm";
					}
					if(BREKA_ORC_FANG_COUNT < 20)
					{
						return "30515-02.htm";
					}
					st.takeItems(MANAKIAS_ORDERS, -1);
					st.takeItems(BREKA_ORC_FANG, -1);
					st.giveItems(MANAKIAS_AMULET, 1);
					st.playSound("ItemSound.quest_middle");
					return "30515-03.htm";
				}
				if(MANAKIAS_ORDERS_COUNT == 0)
				{
					return "30515-04.htm";
				}
			}
			else if(VARKEES_CHARM_COUNT == 0 && HUGE_ORC_FANG_COUNT > 0 && MANAKIAS_AMULET_COUNT == 0 && MANAKIAS_ORDERS_COUNT == 0)
			{
				return "30515-05.htm";
			}
		}
		if(npcId == Tantus)
		{
			if(AXE_OF_CEREMONY_COUNT == 0)
			{
				if(TANTUS_CHARM_COUNT == 0)
				{
					return "30567-01.htm";
				}
				if(NERUGA_AXE_BLADE_COUNT == 0 || st.getQuestItemsCount(BONE_ARROW) < 1000)
				{
					return "30567-03.htm";
				}
				st.takeItems(TANTUS_CHARM, -1);
				st.takeItems(NERUGA_AXE_BLADE, -1);
				st.takeItems(BONE_ARROW, 1000);
				st.giveItems(AXE_OF_CEREMONY, 1);
				if(cond1Complete(st))
				{
					st.playSound("ItemSound.quest_jackpot");
					st.setCond(2);
				}
				else
				{
					st.playSound("ItemSound.quest_middle");
				}
				return "30567-04.htm";
			}
			if(TANTUS_CHARM_COUNT == 0)
			{
				return "30567-05.htm";
			}
		}
		if(npcId == Jakal)
		{
			if(TANTUS_CHARM_COUNT > 0 && AXE_OF_CEREMONY_COUNT == 0)
			{
				if(NERUGA_AXE_BLADE_COUNT > 0)
				{
					return "30558-04.htm";
				}
				return st.getQuestItemsCount(57) < 1000 ? "30558-03.htm" : "30558-01.htm";
			}
			if(TANTUS_CHARM_COUNT == 0 && AXE_OF_CEREMONY_COUNT > 0)
			{
				return "30558-05.htm";
			}
		}
		if(npcId == Hatos)
		{
			if(SWORD_INTO_SKULL_COUNT == 0)
			{
				if(HATOS_CHARM_COUNT == 0)
				{
					return "30568-01.htm";
				}
				if(URUTU_BLADE_COUNT == 0 || TIMAK_ORC_SKULL_COUNT < 10)
				{
					return "30568-03.htm";
				}
				st.takeItems(HATOS_CHARM, -1);
				st.takeItems(URUTU_BLADE, -1);
				st.takeItems(TIMAK_ORC_SKULL, -1);
				st.giveItems(SWORD_INTO_SKULL, 1);
				if(cond1Complete(st))
				{
					st.playSound("ItemSound.quest_jackpot");
					st.setCond(2);
				}
				else
				{
					st.playSound("ItemSound.quest_middle");
				}
				return "30568-04.htm";
			}
			if(HATOS_CHARM_COUNT == 0)
			{
				return "30568-05.htm";
			}
		}
		if(npcId == Sumari)
		{
			if(HATOS_CHARM_COUNT > 0 && SWORD_INTO_SKULL_COUNT == 0)
			{
				if(URUTU_BLADE_COUNT == 0)
				{
					if(SUMARIS_LETTER_COUNT > 0)
					{
						return "30564-02.htm";
					}
					st.giveItems(SUMARIS_LETTER, 1);
					st.playSound("ItemSound.quest_middle");
					return "30564-01.htm";
				}
				if(SUMARIS_LETTER_COUNT == 0)
				{
					return "30564-03.htm";
				}
			}
			else if(HATOS_CHARM_COUNT == 0 && SWORD_INTO_SKULL_COUNT > 0 && URUTU_BLADE_COUNT == 0 && SUMARIS_LETTER_COUNT == 0)
			{
				return "30564-04.htm";
			}
		}
		if(npcId == Somak)
		{
			if(SWORD_INTO_SKULL_COUNT == 0)
			{
				if(URUTU_BLADE_COUNT == 0 && HATOS_CHARM_COUNT > 0 && SUMARIS_LETTER_COUNT > 0)
				{
					st.takeItems(SUMARIS_LETTER, -1);
					st.giveItems(URUTU_BLADE, 1);
					st.playSound("ItemSound.quest_middle");
					return "30510-01.htm";
				}
				if(URUTU_BLADE_COUNT > 0 && HATOS_CHARM_COUNT > 0 && SUMARIS_LETTER_COUNT == 0)
				{
					return "30510-02.htm";
				}
			}
			else if(URUTU_BLADE_COUNT == 0 && HATOS_CHARM_COUNT == 0 && SUMARIS_LETTER_COUNT == 0)
			{
				return "30510-03.htm";
			}
		}
		if(npcId == Takuna)
		{
			if(HANDIWORK_SPIDER_BROOCH_COUNT == 0)
			{
				if(TAKUNA_CHARM_COUNT == 0)
				{
					return "30641-01.htm";
				}
				if(st.getQuestItemsCount(MARSH_SPIDER_FEELER) < 10 || st.getQuestItemsCount(MARSH_SPIDER_FEET) < 10)
				{
					return "30641-03.htm";
				}
				st.takeItems(MARSH_SPIDER_FEELER, -1);
				st.takeItems(MARSH_SPIDER_FEET, -1);
				st.takeItems(TAKUNA_CHARM, -1);
				st.giveItems(HANDIWORK_SPIDER_BROOCH, 1);
				if(cond1Complete(st))
				{
					st.playSound("ItemSound.quest_jackpot");
					st.setCond(2);
				}
				else
				{
					st.playSound("ItemSound.quest_middle");
				}
				return "30641-04.htm";
			}
			if(TAKUNA_CHARM_COUNT == 0)
			{
				return "30641-05.htm";
			}
		}
		if(npcId == Chianta)
		{
			long CHIANTA_CHARM_COUNT = st.getQuestItemsCount(CHIANTA_CHARM);
			if(MONSTEREYE_WOODCARVING_COUNT == 0)
			{
				if(CHIANTA_CHARM_COUNT == 0)
				{
					return "30642-01.htm";
				}
				if(st.getQuestItemsCount(CORNEA_OF_EN_MONSTEREYE) < 20)
				{
					return "30642-03.htm";
				}
				st.takeItems(CORNEA_OF_EN_MONSTEREYE, -1);
				st.takeItems(CHIANTA_CHARM, -1);
				st.giveItems(MONSTEREYE_WOODCARVING, 1);
				if(cond1Complete(st))
				{
					st.playSound("ItemSound.quest_jackpot");
					st.setCond(2);
				}
				else
				{
					st.playSound("ItemSound.quest_middle");
				}
				return "30642-04.htm";
			}
			if(CHIANTA_CHARM_COUNT == 0)
			{
				return "30642-05.htm";
			}
		}
		if(npcId == Ancestor_Martankus)
		{
			if(BEAR_FANG_NECKLACE_COUNT > 0)
			{
				return "30649-01.htm";
			}
			if(MARTANKUS_CHARM_COUNT > 0)
			{
				if(cond == 5 || st.getQuestItemsCount(RAGNA_CHIEF_NOTICE) > 0 && st.getQuestItemsCount(RAGNA_ORC_HEAD) > 0)
				{
					st.takeItems(MARTANKUS_CHARM, -1);
					st.takeItems(RAGNA_ORC_HEAD, -1);
					st.takeItems(RAGNA_CHIEF_NOTICE, -1);
					st.giveItems(IMMORTAL_FLAME, 1);
					st.playSound("ItemSound.quest_middle");
					return "30649-06.htm";
				}
				return "30649-05.htm";
			}
			if(cond == 6 || cond == 7)
			{
				return "30649-08.htm";
			}
		}
		if(npcId == First_Orc && st.getQuestItemsCount(IMMORTAL_FLAME) > 0)
		{
			st.setCond(7);
			return "30643-01.htm";
		}
		return "noquest";
	}
	
	private boolean cond1Complete(QuestState st)
	{
		long HUGE_ORC_FANG_COUNT = st.getQuestItemsCount(HUGE_ORC_FANG);
		long SWORD_INTO_SKULL_COUNT = st.getQuestItemsCount(SWORD_INTO_SKULL);
		long AXE_OF_CEREMONY_COUNT = st.getQuestItemsCount(AXE_OF_CEREMONY);
		long MONSTEREYE_WOODCARVING_COUNT = st.getQuestItemsCount(MONSTEREYE_WOODCARVING);
		long HANDIWORK_SPIDER_BROOCH_COUNT = st.getQuestItemsCount(HANDIWORK_SPIDER_BROOCH);
		return HUGE_ORC_FANG_COUNT > 0 && SWORD_INTO_SKULL_COUNT > 0 && AXE_OF_CEREMONY_COUNT > 0 && MONSTEREYE_WOODCARVING_COUNT > 0 && HANDIWORK_SPIDER_BROOCH_COUNT > 0;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getState() != 2)
		{
			return null;
		}
		int npcId = npc.getNpcId();
		Drop _drop = DROPLIST.get(npcId);
		if(_drop == null)
		{
			return null;
		}
		int cond = qs.getCond();
		for(int item_id : _drop.itemList)
		{
			long ORDEAL_NECKLACE_COUNT = qs.getQuestItemsCount(ORDEAL_NECKLACE);
			if(item_id == TIMAK_ORC_SKULL && (ORDEAL_NECKLACE_COUNT <= 0 || qs.getQuestItemsCount(HATOS_CHARM) <= 0 || qs.getQuestItemsCount(SWORD_INTO_SKULL) != 0) || item_id == BREKA_ORC_FANG && (ORDEAL_NECKLACE_COUNT <= 0 || qs.getQuestItemsCount(VARKEES_CHARM) <= 0 || qs.getQuestItemsCount(MANAKIAS_ORDERS) <= 0) || npcId == Marsh_Spider && (ORDEAL_NECKLACE_COUNT <= 0 || qs.getQuestItemsCount(TAKUNA_CHARM) <= 0) || npcId == Enchanted_Monstereye && (ORDEAL_NECKLACE_COUNT <= 0 || qs.getQuestItemsCount(CHIANTA_CHARM) <= 0))
				continue;
			long count = qs.getQuestItemsCount(item_id);
			if(cond != _drop.condition || count >= (long) _drop.maxcount || !Rnd.chance(_drop.chance))
				continue;
			qs.giveItems(item_id, 1);
			if(count + 1 == (long) _drop.maxcount)
			{
				if(cond == 4 && qs.getQuestItemsCount(RAGNA_ORC_HEAD) > 0 && qs.getQuestItemsCount(RAGNA_CHIEF_NOTICE) > 0)
				{
					qs.setCond(5);
				}
				qs.playSound("ItemSound.quest_middle");
				continue;
			}
			qs.playSound("ItemSound.quest_itemget");
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