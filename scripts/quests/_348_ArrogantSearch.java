package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.RadarControl;
import l2.gameserver.scripts.ScriptFile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class _348_ArrogantSearch extends Quest implements ScriptFile
{
	private static final int ARK_GUARDIAN_ELBEROTH = 27182;
	private static final int ARK_GUARDIAN_SHADOWFANG = 27183;
	private static final int ANGEL_KILLER = 27184;
	private static final int PLATINUM_TRIBE_SHAMAN = 20828;
	private static final int PLATINUM_TRIBE_OVERLORD = 20829;
	private static final int LESSER_GIANT_MAGE = 20657;
	private static final int LESSER_GIANT_ELDER = 20658;
	private static final int GUARDIAN_ANGEL = 20859;
	private static final int SEAL_ANGEL = 20860;
	private static final int HANELLIN = 30864;
	private static final int HOLY_ARK_OF_SECRECY_1 = 30977;
	private static final int HOLY_ARK_OF_SECRECY_2 = 30978;
	private static final int HOLY_ARK_OF_SECRECY_3 = 30979;
	private static final int ARK_GUARDIANS_CORPSE = 30980;
	private static final int HARNE = 30144;
	private static final int CLAUDIA_ATHEBALT = 31001;
	private static final int MARTIEN = 30645;
	private static final int TITANS_POWERSTONE = 4287;
	private static final int HANELLINS_FIRST_LETTER = 4288;
	private static final int HANELLINS_SECOND_LETTER = 4289;
	private static final int HANELLINS_THIRD_LETTER = 4290;
	private static final int FIRST_KEY_OF_ARK = 4291;
	private static final int SECOND_KEY_OF_ARK = 4292;
	private static final int THIRD_KEY_OF_ARK = 4293;
	private static final int WHITE_FABRIC_1 = 4294;
	private static final int BLOODED_FABRIC = 4295;
	private static final int HANELLINS_WHITE_FLOWER = 4394;
	private static final int HANELLINS_RED_FLOWER = 4395;
	private static final int HANELLINS_YELLOW_FLOWER = 4396;
	private static final int BOOK_OF_SAINT = 4397;
	private static final int BLOOD_OF_SAINT = 4398;
	private static final int BRANCH_OF_SAINT = 4399;
	private static final int WHITE_FABRIC_0 = 4400;
	private static final int WHITE_FABRIC_2 = 5232;
	private static final int ANTIDOTE = 1831;
	private static final int HEALING_POTION = 1061;
	private static final Map<Integer, Integer[]> ARKS = new HashMap<>();
	private static final Map<Integer, String[]> ARKS_TEXT = new HashMap<>();
	private static final Map<Integer, Integer[]> ARK_OWNERS = new HashMap<>();
	private static final Map<Integer, String[]> ARK_OWNERS_TEXT = new HashMap<>();
	private static final Map<Integer, Integer[]> DROPS = new HashMap<>();
	
	static
	{
		ARKS.put(30977, new Integer[] {4291, 0, 4398});
		ARKS.put(30978, new Integer[] {4292, 27182, 4397});
		ARKS.put(30979, new Integer[] {4293, 27183, 4399});
		ARKS_TEXT.put(30977, new String[] {"30977-01.htm", "30977-02.htm", "30977-03.htm"});
		ARKS_TEXT.put(30978, new String[] {"That doesn't belong to you.  Don't touch it!", "30978-02.htm", "30978-03.htm"});
		ARKS_TEXT.put(30979, new String[] {"Get off my sight, you infidels!", "30979-02.htm", "30979-03.htm"});
		ARK_OWNERS.put(30144, new Integer[] {4288, 4398, -418, 44174, -3568});
		ARK_OWNERS.put(31001, new Integer[] {4289, 4397, 181472, 7158, -2725});
		ARK_OWNERS.put(30645, new Integer[] {4290, 4399, 50693, 158674, 376});
		ARK_OWNERS_TEXT.put(30144, new String[] {"30144-01.htm", "30144-02.htm", "30144-03.htm"});
		ARK_OWNERS_TEXT.put(31001, new String[] {"31001-01.htm", "31001-02.htm", "31001-03.htm"});
		ARK_OWNERS_TEXT.put(30645, new String[] {"30645-01.htm", "30645-02.htm", "30645-03.htm"});
		DROPS.put(20657, new Integer[] {2, 4287, 1, 10, 0});
		DROPS.put(20658, new Integer[] {2, 4287, 1, 10, 0});
		DROPS.put(27184, new Integer[] {5, 4291, 1, 100, 0});
		DROPS.put(27182, new Integer[] {5, 4292, 1, 100, 0});
		DROPS.put(27183, new Integer[] {5, 4293, 1, 100, 0});
		DROPS.put(20828, new Integer[] {25, 4295, Integer.MAX_VALUE, 10, 4294});
		DROPS.put(20829, new Integer[] {25, 4295, Integer.MAX_VALUE, 10, 4294});
		DROPS.put(20859, new Integer[] {25, 4295, Integer.MAX_VALUE, 25, 5232});
		DROPS.put(20860, new Integer[] {25, 4295, Integer.MAX_VALUE, 25, 5232});
	}
	
	public _348_ArrogantSearch()
	{
		super(true);
		addStartNpc(30864);
		addTalkId(30980);
		Iterator<Integer> iterator = ARK_OWNERS.keySet().iterator();
		int i;
		while(iterator.hasNext())
		{
			i = iterator.next();
			addTalkId(i);
		}
		iterator = ARKS.keySet().iterator();
		while(iterator.hasNext())
		{
			i = iterator.next();
			addTalkId(i);
		}
		iterator = DROPS.keySet().iterator();
		while(iterator.hasNext())
		{
			i = iterator.next();
			addKillId(i);
		}
		addQuestItem(4288, 4289, 4290, 4394, 4395, 4396, 4397, 4294, 4398, 4399, 4400, 5232, 4291, 4292, 4293);
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
		if(event.equals("30864_02"))
		{
			st.setCond(2);
			htmltext = "30864-03.htm";
		}
		else if(event.equals("30864_04a"))
		{
			st.setCond(4);
			st.takeItems(4287, -1);
			htmltext = "30864-04c.htm";
			st.set("companions", "0");
		}
		else if(event.equals("30864_04b"))
		{
			st.setCond(3);
			st.set("companions", "1");
			st.takeItems(4287, -1);
			htmltext = "not yet implemented";
		}
		if(event.equals("30864-09a.htm"))
		{
			st.setCond(29);
			st.giveItems(5232, 10);
		}
		if(event.equals("30864-10a.htm"))
		{
			if(st.getQuestItemsCount(5232) < 10)
			{
				st.giveItems(5232, 10 - st.getQuestItemsCount(5232));
			}
			htmltext = "30864-10.htm";
		}
		if(event.equals("30864-10b.htm"))
		{
			if(st.getQuestItemsCount(4295) > 1)
			{
				long count = st.takeItems(4295, -1);
				st.giveItems(57, count * 1000, true);
				htmltext = "30864-10.htm";
			}
			else
			{
				htmltext = "30864-11.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int id = st.getState();
		int cond = st.getCond();
		if(npcId == 30864)
		{
			if(id == 1)
			{
				if(st.getQuestItemsCount(4295) >= 1)
				{
					htmltext = "30864-Baium.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					st.setCond(0);
					if(st.getPlayer().getLevel() < 60)
					{
						htmltext = "30864-01.htm";
						st.exitCurrentQuest(true);
					}
					else if(cond == 0)
					{
						st.setState(2);
						st.setCond(1);
						htmltext = "30864-02.htm";
					}
				}
			}
			else if(cond == 1)
			{
				htmltext = "30864-02.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(4287) == 0)
			{
				htmltext = "30864-03a.htm";
			}
			else if(cond == 2)
			{
				htmltext = "30864-04.htm";
			}
			else if(cond == 4)
			{
				st.setCond(5);
				st.giveItems(4288, 1);
				st.giveItems(4289, 1);
				st.giveItems(4290, 1);
				htmltext = "30864-05.htm";
			}
			else if(cond == 5 && st.getQuestItemsCount(4397) + st.getQuestItemsCount(4398) + st.getQuestItemsCount(4399) < 3)
			{
				htmltext = "30864-05.htm";
			}
			else if(cond == 5)
			{
				htmltext = "30864-06.htm";
				st.takeItems(4397, -1);
				st.takeItems(4398, -1);
				st.takeItems(4399, -1);
				st.setCond(22);
			}
			else if(cond == 22 && st.getQuestItemsCount(1831) < 5 && st.getQuestItemsCount(1061) < 1)
			{
				htmltext = "30864-06a.htm";
			}
			else if(cond == 22)
			{
				st.takeItems(1831, 5);
				st.takeItems(1061, 1);
				if(st.getInt("companions") == 0)
				{
					st.setCond(25);
					htmltext = "30864-07.htm";
					st.giveItems(4294, 1);
				}
				else
				{
					st.setCond(23);
					htmltext = "not implemented yet";
					st.giveItems(4400, 3);
				}
			}
			else if(cond == 25 && st.getQuestItemsCount(4295) < 1)
			{
				if(st.getQuestItemsCount(4294) < 1)
				{
					st.giveItems(4294, 1);
				}
				htmltext = "30864-07a.htm";
			}
			else if(cond == 26 && st.getQuestItemsCount(4295) < 1)
			{
				if(st.getQuestItemsCount(5232) < 1)
				{
					st.giveItems(5232, 1);
				}
				htmltext = "30864-07a.htm";
			}
			else if(cond == 25 && st.getQuestItemsCount(4295) > 0 || cond == 28)
			{
				if(cond != 28)
				{
					st.setCond(28);
				}
				htmltext = "30864-09.htm";
			}
			else if(cond == 29)
			{
				htmltext = "30864-10.htm";
			}
		}
		else if(cond == 5)
		{
			if(ARK_OWNERS.containsKey(npcId))
			{
				if(st.getQuestItemsCount(ARK_OWNERS.get(npcId)[0].intValue()) == 1)
				{
					st.takeItems(ARK_OWNERS.get(npcId)[0].intValue(), 1);
					htmltext = ARK_OWNERS_TEXT.get(npcId)[0];
					st.getPlayer().sendPacket(new RadarControl(0, 1, ARK_OWNERS.get(npcId)[2].intValue(), ARK_OWNERS.get(npcId)[3].intValue(), ARK_OWNERS.get(npcId)[4].intValue()));
				}
				else if(st.getQuestItemsCount(ARK_OWNERS.get(npcId)[1].intValue()) < 1)
				{
					htmltext = ARK_OWNERS_TEXT.get(npcId)[1];
					st.getPlayer().sendPacket(new RadarControl(0, 1, ARK_OWNERS.get(npcId)[2].intValue(), ARK_OWNERS.get(npcId)[3].intValue(), ARK_OWNERS.get(npcId)[4].intValue()));
				}
				else
				{
					htmltext = ARK_OWNERS_TEXT.get(npcId)[2];
				}
			}
			else if(ARKS.containsKey(npcId))
			{
				if(st.getQuestItemsCount(ARKS.get(npcId)[0].intValue()) == 0)
				{
					if(ARKS.get(npcId)[1] != 0)
					{
						st.addSpawn(ARKS.get(npcId)[1].intValue(), 120000);
					}
					return ARKS_TEXT.get(npcId)[0];
				}
				if(st.getQuestItemsCount(ARKS.get(npcId)[2].intValue()) == 1)
				{
					htmltext = ARKS_TEXT.get(npcId)[2];
				}
				else
				{
					htmltext = ARKS_TEXT.get(npcId)[1];
					st.takeItems(ARKS.get(npcId)[0].intValue(), 1);
					st.giveItems(ARKS.get(npcId)[2].intValue(), 1);
				}
			}
			else if(npcId == 30980)
			{
				if(st.getQuestItemsCount(4291) == 0 && st.getInt("angelKillerIsDefeated") == 0)
				{
					st.addSpawn(27184, 120000);
					htmltext = "30980-01.htm";
				}
				else if(st.getQuestItemsCount(4291) == 0 && st.getInt("angelKillerIsDefeated") == 1)
				{
					st.giveItems(4291, 1);
					htmltext = "30980-02.htm";
				}
				else
				{
					htmltext = "30980-03.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		Integer[] drop = DROPS.get(npcId);
		if(drop != null)
		{
			int cond = drop[0];
			int item = drop[1];
			int max = drop[2];
			int chance = drop[3];
			int take = drop[4];
			if(st.getCond() >= cond && st.getQuestItemsCount(item) < (long) max && (take == 0 || st.getQuestItemsCount(take) > 0) && Rnd.chance(chance))
			{
				st.giveItems(item, 1);
				st.playSound("ItemSound.quest_itemget");
				if(take != 0)
				{
					st.takeItems(take, 1);
				}
				QuestState FatesWhisper;
				if(4295 == item && st.getQuestItemsCount(4295) >= 30 && (FatesWhisper = st.getPlayer().getQuestState(_234_FatesWhisper.class)) != null && FatesWhisper.getCond() == 8)
				{
					FatesWhisper.setCond(9);
				}
			}
		}
		if(npcId == 27184)
		{
			return "Ha, that was fun! If you wish to find the key, search the corpse";
		}
		return null;
	}
}