package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.Summon;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _422_RepentYourSins extends Quest implements ScriptFile
{
	private static final int SCAVENGER_WERERAT_SKULL = 4326;
	private static final int TUREK_WARHOUND_TAIL = 4327;
	private static final int TYRANT_KINGPIN_HEART = 4328;
	private static final int TRISALIM_TARANTULAS_VENOM_SAC = 4329;
	private static final int MANUAL_OF_MANACLES = 4331;
	private static final int PENITENTS_MANACLES = 4425;
	private static final int PENITENTS_MANACLES1 = 4330;
	private static final int PENITENTS_MANACLES2 = 4426;
	private static final int SILVER_NUGGET = 1873;
	private static final int ADAMANTINE_NUGGET = 1877;
	private static final int BLACKSMITHS_FRAME = 1892;
	private static final int COKES = 1879;
	private static final int STEEL = 1880;
	private static final int Black_Judge = 30981;
	private static final int Katari = 30668;
	private static final int Piotur = 30597;
	private static final int Casian = 30612;
	private static final int Joan = 30718;
	private static final int Pushkin = 30300;
	private static final int Sin_Eater = 12564;
	private static final int SCAVENGER_WERERAT = 20039;
	private static final int TUREK_WARHOUND = 20494;
	private static final int TYRANT_KINGPIN = 20193;
	private static final int TRISALIM_TARANTULA = 20561;
	
	public _422_RepentYourSins()
	{
		super(false);
		addStartNpc(30981);
		addTalkId(30668);
		addTalkId(30597);
		addTalkId(30612);
		addTalkId(30718);
		addTalkId(30300);
		addKillId(20039);
		addKillId(20494);
		addKillId(20193);
		addKillId(20561);
		addQuestItem(4326);
		addQuestItem(4327);
		addQuestItem(4328);
		addQuestItem(4329);
		addQuestItem(4331);
		addQuestItem(4425);
		addQuestItem(4330);
	}
	
	public int findPetLvl(QuestState st)
	{
		ItemInstance item = st.getPlayer().getInventory().getItemByItemId(4425);
		if(item == null)
		{
			return 0;
		}
		Summon pet = st.getPlayer().getPet();
		if(pet == null)
		{
			return item.getEnchantLevel();
		}
		if(pet.getNpcId() != 12564)
		{
			return 0;
		}
		return pet.getLevel();
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
		if(event.equalsIgnoreCase("Start"))
		{
			st.playSound("ItemSound.quest_accept");
			st.setState(2);
			if(st.getPlayer().getLevel() <= 20)
			{
				st.setCond(1);
				st.setCond(2);
				return "black_judge_q0422_03.htm";
			}
			if(st.getPlayer().getLevel() <= 30)
			{
				st.setCond(3);
				return "black_judge_q0422_04.htm";
			}
			if(st.getPlayer().getLevel() <= 40)
			{
				st.setCond(4);
				return "black_judge_q0422_05.htm";
			}
			st.setCond(5);
			return "black_judge_q0422_06.htm";
		}
		if(event.equalsIgnoreCase("1"))
		{
			if(st.getQuestItemsCount(4330) >= 1)
			{
				st.takeItems(4330, -1);
			}
			if(st.getQuestItemsCount(4425) >= 1)
			{
				st.takeItems(4425, -1);
			}
			st.setCond(16);
			st.set("level", String.valueOf(st.getPlayer().getLevel()));
			st.giveItems(4425, 1);
			return "black_judge_q0422_11.htm";
		}
		if(event.equalsIgnoreCase("2"))
		{
			return "black_judge_q0422_14.htm";
		}
		if(event.equalsIgnoreCase("3"))
		{
			int plevel = findPetLvl(st);
			int level = st.getPlayer().getLevel();
			int olevel = st.getInt("level");
			Summon pet = st.getPlayer().getPet();
			if(pet != null)
			{
				if(pet.getNpcId() == 12564)
				{
					return "black_judge_q0422_15t.htm";
				}
			}
			else
			{
				int Pk_remove = level > olevel ? plevel - level : plevel - olevel;
				if(Pk_remove < 0)
				{
					Pk_remove = 0;
				}
				Pk_remove = Rnd.get(10 + Pk_remove) + 1;
				if(st.getPlayer().getPkKills() <= Pk_remove)
				{
					st.getPlayer().setPkKills(0);
					st.playSound("ItemSound.quest_finish");
					if(st.getQuestItemsCount(4426) < 1)
					{
						st.giveItems(4426, 1);
					}
					st.exitCurrentQuest(true);
					return "black_judge_q0422_15.htm";
				}
				st.takeItems(4425, 1);
				int Pk_new = st.getPlayer().getPkKills() - Pk_remove;
				st.getPlayer().setPkKills(Pk_new);
				st.set("level", "0");
				return "black_judge_q0422_16.htm";
			}
		}
		if(event.equalsIgnoreCase("4"))
		{
			return "black_judge_q0422_17.htm";
		}
		if(event.equalsIgnoreCase("Quit"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
			return "black_judge_q0422_18.htm";
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		int npcId = npc.getNpcId();
		int id = st.getState();
		if(npcId == 30981)
		{
			if(id == 1)
			{
				if(st.getPlayer().getPkKills() >= 1 && st.getPlayer().getLevel() <= 85)
				{
					return "black_judge_q0422_02.htm";
				}
				st.exitCurrentQuest(true);
				return "black_judge_q0422_01.htm";
			}
			if(cond <= 9)
			{
				return "black_judge_q0422_07.htm";
			}
			if(cond <= 13 && cond > 9 && st.getQuestItemsCount(4331) < 1)
			{
				if(st.getQuestItemsCount(4426) < 1)
				{
					st.setCond(14);
					st.giveItems(4331, 1);
					return "black_judge_q0422_08.htm";
				}
				st.takeItems(4426, -1);
				if(st.getQuestItemsCount(4425) < 1)
				{
					st.giveItems(4425, 1, false);
				}
				st.setCond(16);
				cond = 16;
			}
			if(cond == 14 && st.getQuestItemsCount(4331) > 0)
			{
				return "black_judge_q0422_09.htm";
			}
			if(cond == 15 && st.getQuestItemsCount(4330) > 0)
			{
				return "black_judge_q0422_10.htm";
			}
			if(cond >= 16)
			{
				if(st.getQuestItemsCount(4425) > 0)
				{
					int plevel = findPetLvl(st);
					int level = st.getPlayer().getLevel();
					if(st.getInt("level") > level)
					{
						level = st.getInt("level");
					}
					if(plevel > 0)
					{
						if(plevel > level)
						{
							return "black_judge_q0422_13.htm";
						}
						return "black_judge_q0422_12.htm";
					}
					return "black_judge_q0422_12.htm";
				}
				return "black_judge_q0422_16t.htm";
			}
		}
		if(npcId == 30668)
		{
			if(cond == 2)
			{
				st.setCond(6);
				return "katari_q0422_01.htm";
			}
			if(cond == 6 && st.getQuestItemsCount(4326) < 10)
			{
				return "katari_q0422_02.htm";
			}
			if(cond == 10)
			{
				return "katari_q0422_04.htm";
			}
			st.setCond(10);
			st.takeItems(4326, -1);
			return "katari_q0422_03.htm";
		}
		if(npcId == 30597)
		{
			if(cond == 3)
			{
				st.setCond(7);
				return "piotur_q0422_01.htm";
			}
			if(cond == 7 && st.getQuestItemsCount(4327) < 10)
			{
				return "piotur_q0422_02.htm";
			}
			if(cond == 11)
			{
				return "piotur_q0422_04.htm";
			}
			st.setCond(11);
			st.takeItems(4327, -1);
			return "piotur_q0422_03.htm";
		}
		if(npcId == 30612)
		{
			if(cond == 4)
			{
				st.setCond(8);
				return "sage_kasian_q0422_01.htm";
			}
			if(cond == 8)
			{
				if(st.getQuestItemsCount(4328) < 1)
				{
					return "sage_kasian_q0422_02.htm";
				}
				st.setCond(12);
				st.takeItems(4328, -1);
				return "sage_kasian_q0422_03.htm";
			}
			if(cond == 12)
			{
				return "sage_kasian_q0422_04.htm";
			}
		}
		if(npcId == 30718)
		{
			if(cond == 5)
			{
				st.setCond(9);
				return "magister_joan_q0422_01.htm";
			}
			if(cond == 9 && st.getQuestItemsCount(4329) < 3)
			{
				return "magister_joan_q0422_02.htm";
			}
			if(st.getQuestItemsCount(4329) >= 3)
			{
				st.setCond(13);
				st.takeItems(4329, -1);
				return "magister_joan_q0422_03.htm";
			}
			if(cond == 13)
			{
				return "magister_joan_q0422_04.htm";
			}
		}
		if(npcId == 30300 && cond >= 14)
		{
			if(st.getQuestItemsCount(4331) == 1)
			{
				if(st.getQuestItemsCount(1873) < 10 || st.getQuestItemsCount(1880) < 5 || st.getQuestItemsCount(1877) < 2 || st.getQuestItemsCount(1879) < 10 || st.getQuestItemsCount(1892) < 1)
				{
					return "blacksmith_pushkin_q0422_02.htm";
				}
				if(st.getQuestItemsCount(1873) >= 10 && st.getQuestItemsCount(1880) >= 5 && st.getQuestItemsCount(1877) >= 2 && st.getQuestItemsCount(1879) >= 10 && st.getQuestItemsCount(1892) >= 1)
				{
					st.setCond(15);
					st.takeItems(4331, 1);
					st.takeItems(1873, 10);
					st.takeItems(1877, 2);
					st.takeItems(1879, 10);
					st.takeItems(1880, 5);
					st.takeItems(1892, 1);
					st.giveItems(4330, 1);
					st.playSound("ItemSound.quest_middle");
					return "blacksmith_pushkin_q0422_02.htm";
				}
			}
			if(st.getQuestItemsCount(4330) > 0 || st.getQuestItemsCount(4425) > 0 || st.getQuestItemsCount(4426) > 0)
			{
				return "blacksmith_pushkin_q0422_03.htm";
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
		int cond = st.getCond();
		int npcId = npc.getNpcId();
		long skulls = st.getQuestItemsCount(4326);
		long tails = st.getQuestItemsCount(4327);
		long heart2 = st.getQuestItemsCount(4328);
		long sacs = st.getQuestItemsCount(4329);
		if(npcId == 20039 && cond == 6 && skulls < 10)
		{
			st.giveItems(4326, 1);
			if(skulls == 10)
			{
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		if(npcId == 20494 && cond == 7 && tails < 10)
		{
			st.giveItems(4327, 1);
			if(tails == 10)
			{
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		if(npcId == 20193 && cond == 8 && heart2 < 1)
		{
			st.giveItems(4328, 1);
			st.playSound("ItemSound.quest_middle");
		}
		if(npcId == 20561 && cond == 9 && sacs < 3)
		{
			st.giveItems(4329, 1);
			if(skulls == 3)
			{
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}