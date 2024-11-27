package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _228_TestOfMagus extends Quest implements ScriptFile
{
	private static final int Rukal = 30629;
	private static final int Parina = 30391;
	private static final int Casian = 30612;
	private static final int Salamander = 30411;
	private static final int Sylph = 30412;
	private static final int Undine = 30413;
	private static final int Snake = 30409;
	private static final int RukalsLetter = 2841;
	private static final int ParinasLetter = 2842;
	private static final int LilacCharm = 2843;
	private static final int GoldenSeed1st = 2844;
	private static final int GoldenSeed2st = 2845;
	private static final int GoldenSeed3st = 2846;
	private static final int ScoreOfElements = 2847;
	private static final int ToneOfWater = 2856;
	private static final int ToneOfFire = 2857;
	private static final int ToneOfWind = 2858;
	private static final int ToneOfEarth = 2859;
	private static final int UndineCharm = 2862;
	private static final int DazzlingDrop = 2848;
	private static final int SalamanderCharm = 2860;
	private static final int FlameCrystal = 2849;
	private static final int SylphCharm = 2861;
	private static final int HarpysFeather = 2850;
	private static final int WyrmsWingbone = 2851;
	private static final int WindsusMane = 2852;
	private static final int SerpentCharm = 2863;
	private static final int EnchantedMonsterEyeShell = 2853;
	private static final int EnchantedStoneGolemPowder = 2854;
	private static final int EnchantedIronGolemScrap = 2855;
	private static final int MarkOfMagus = 2840;
	private static final int SingingFlowerPhantasm = 27095;
	private static final int SingingFlowerNightmare = 27096;
	private static final int SingingFlowerDarkling = 27097;
	private static final int Harpy = 20145;
	private static final int Wyrm = 20176;
	private static final int Windsus = 20553;
	private static final int EnchantedMonstereye = 20564;
	private static final int EnchantedStoneGolem = 20565;
	private static final int EnchantedIronGolem = 20566;
	private static final int QuestMonsterGhostFire = 27098;
	private static final int MarshStakatoWorker = 20230;
	private static final int ToadLord = 20231;
	private static final int MarshStakato = 20157;
	private static final int MarshStakatoSoldier = 20232;
	private static final int MarshStakatoDrone = 20234;
	private static final int[][] DROPLIST_COND = {{3, 0, 27095, 2843, 2844, 10, 100, 1}, {3, 0, 27096, 2843, 2845, 10, 100, 1}, {3, 0, 27097, 2843, 2846, 10, 100, 1}, {5, 0, 20145, 2861, 2850, 20, 50, 2}, {5, 0, 20176, 2861, 2851, 10, 50, 2}, {5, 0, 20553, 2861, 2852, 10, 50, 2}, {5, 0, 20564, 2863, 2853, 10, 100, 2}, {5, 0, 20565, 2863, 2854, 10, 100, 2}, {5, 0, 20566, 2863, 2855, 10, 100, 2}, {5, 0, 27098, 2860, 2849, 5, 50, 1}, {5, 0, 20230, 2862, 2848, 20, 30, 2}, {5, 0, 20231, 2862, 2848, 20, 30, 2}, {5, 0, 20157, 2862, 2848, 20, 30, 2}, {5, 0, 20232, 2862, 2848, 20, 40, 2}, {5, 0, 20234, 2862, 2848, 20, 50, 2}};
	
	public _228_TestOfMagus()
	{
		super(false);
		addStartNpc(30629);
		addTalkId(30391);
		addTalkId(30612);
		addTalkId(30412);
		addTalkId(30409);
		addTalkId(30413);
		addTalkId(30411);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
		}
		addQuestItem(2841, 2842, 2843, 2858, 2861, 2863, 2859, 2862, 2857, 2860, 2856, 2847, 2844, 2845, 2846, 2850, 2851, 2852, 2853, 2854, 2855, 2849, 2848);
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
	
	public void checkBooks(QuestState st)
	{
		if(st.getQuestItemsCount(2856) != 0 && st.getQuestItemsCount(2857) != 0 && st.getQuestItemsCount(2858) != 0 && st.getQuestItemsCount(2859) != 0)
		{
			st.setCond(6);
			st.setState(2);
		}
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("30629-04.htm"))
		{
			st.giveItems(2841, 1);
			st.setCond(1);
			st.setState(2);
			if(!st.getPlayer().getVarB("dd3"))
			{
				st.giveItems(7562, 122);
				st.getPlayer().setVar("dd3", "1", -1);
			}
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30391-02.htm"))
		{
			st.takeItems(2841, -1);
			st.giveItems(2842, 1);
			st.setCond(2);
			st.setState(2);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("30612-02.htm"))
		{
			st.takeItems(2842, -1);
			st.giveItems(2843, 1);
			st.setCond(3);
			st.setState(2);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("30629-10.htm"))
		{
			st.takeItems(2843, -1);
			st.takeItems(2844, -1);
			st.takeItems(2845, -1);
			st.takeItems(2846, -1);
			st.giveItems(2847, 1);
			st.setCond(5);
			st.setState(2);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("30412-02.htm"))
		{
			st.giveItems(2861, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("30409-03.htm"))
		{
			st.giveItems(2863, 1);
			st.playSound("ItemSound.quest_middle");
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30629)
		{
			if(st.getQuestItemsCount(2840) != 0)
			{
				htmltext = "completed";
				st.exitCurrentQuest(true);
			}
			else if(cond == 0)
			{
				if(st.getPlayer().getClassId().getId() == 11 || st.getPlayer().getClassId().getId() == 26 || st.getPlayer().getClassId().getId() == 39)
				{
					if(st.getPlayer().getLevel() >= 39)
					{
						htmltext = "30629-03.htm";
					}
					else
					{
						htmltext = "30629-02.htm";
						st.exitCurrentQuest(true);
					}
				}
				else
				{
					htmltext = "30629-01.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "30629-05.htm";
			}
			else if(cond == 2)
			{
				htmltext = "30629-06.htm";
			}
			else if(cond == 3)
			{
				htmltext = "30629-07.htm";
			}
			else if(cond == 4)
			{
				htmltext = "30629-08.htm";
			}
			else if(cond == 5)
			{
				htmltext = "30629-11.htm";
			}
			else if(cond == 6)
			{
				st.takeItems(2847, -1);
				st.takeItems(2856, -1);
				st.takeItems(2857, -1);
				st.takeItems(2858, -1);
				st.takeItems(2859, -1);
				st.giveItems(2840, 1);
				htmltext = "30629-12.htm";
				if(!st.getPlayer().getVarB("prof2.3"))
				{
					st.addExpAndSp(164032, 17500);
					st.getPlayer().setVar("prof2.3", "1", -1);
				}
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
		}
		else if(npcId == 30391)
		{
			if(cond == 1)
			{
				htmltext = "30391-01.htm";
			}
			else if(cond == 2)
			{
				htmltext = "30391-03.htm";
			}
			else if(cond == 3 || cond == 4)
			{
				htmltext = "30391-04.htm";
			}
			else if(cond >= 5)
			{
				htmltext = "30391-05.htm";
			}
		}
		else if(npcId == 30612)
		{
			if(cond == 2)
			{
				htmltext = "30612-01.htm";
			}
			else if(cond == 3)
			{
				htmltext = "30612-03.htm";
			}
			else if(cond == 4)
			{
				htmltext = "30612-04.htm";
			}
			else if(cond >= 5)
			{
				htmltext = "30612-05.htm";
			}
		}
		else if(npcId == 30411 && cond == 5)
		{
			if(st.getQuestItemsCount(2857) == 0)
			{
				if(st.getQuestItemsCount(2860) == 0)
				{
					htmltext = "30411-01.htm";
					st.giveItems(2860, 1);
					st.playSound("ItemSound.quest_middle");
				}
				else if(st.getQuestItemsCount(2849) < 5)
				{
					htmltext = "30411-02.htm";
				}
				else
				{
					st.takeItems(2860, -1);
					st.takeItems(2849, -1);
					st.giveItems(2857, 1);
					htmltext = "30411-03.htm";
					checkBooks(st);
					st.playSound("ItemSound.quest_middle");
				}
			}
			else
			{
				htmltext = "30411-04.htm";
			}
		}
		else if(npcId == 30412 && cond == 5)
		{
			if(st.getQuestItemsCount(2858) == 0)
			{
				if(st.getQuestItemsCount(2861) == 0)
				{
					htmltext = "30412-01.htm";
				}
				else if(st.getQuestItemsCount(2850) < 20 || st.getQuestItemsCount(2851) < 10 || st.getQuestItemsCount(2852) < 10)
				{
					htmltext = "30412-03.htm";
				}
				else
				{
					st.takeItems(2861, -1);
					st.takeItems(2850, -1);
					st.takeItems(2851, -1);
					st.takeItems(2852, -1);
					st.giveItems(2858, 1);
					htmltext = "30412-04.htm";
					checkBooks(st);
					st.playSound("ItemSound.quest_middle");
				}
			}
			else
			{
				htmltext = "30412-05.htm";
			}
		}
		else if(npcId == 30409 && cond == 5)
		{
			if(st.getQuestItemsCount(2859) == 0)
			{
				if(st.getQuestItemsCount(2863) == 0)
				{
					htmltext = "30409-01.htm";
				}
				else if(st.getQuestItemsCount(2853) < 10 || st.getQuestItemsCount(2854) < 10 || st.getQuestItemsCount(2855) < 10)
				{
					htmltext = "30409-04.htm";
				}
				else
				{
					st.takeItems(2863, -1);
					st.takeItems(20564, -1);
					st.takeItems(2854, -1);
					st.takeItems(2855, -1);
					st.giveItems(2859, 1);
					htmltext = "30409-05.htm";
					checkBooks(st);
					st.playSound("ItemSound.quest_middle");
				}
			}
			else
			{
				htmltext = "30409-06.htm";
			}
		}
		else if(npcId == 30413 && cond == 5)
		{
			if(st.getQuestItemsCount(2856) == 0)
			{
				if(st.getQuestItemsCount(2862) == 0)
				{
					htmltext = "30413-01.htm";
					st.giveItems(2862, 1);
					st.playSound("ItemSound.quest_middle");
				}
				else if(st.getQuestItemsCount(2848) < 20)
				{
					htmltext = "30413-02.htm";
				}
				else
				{
					st.takeItems(2862, -1);
					st.takeItems(2848, -1);
					st.giveItems(2856, 1);
					htmltext = "30413-03.htm";
					checkBooks(st);
					st.playSound("ItemSound.quest_middle");
				}
			}
			else
			{
				htmltext = "30413-04.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			if(cond != DROPLIST_COND[i][0] || npcId != DROPLIST_COND[i][2] || DROPLIST_COND[i][3] != 0 && st.getQuestItemsCount(DROPLIST_COND[i][3]) <= 0)
				continue;
			if(DROPLIST_COND[i][5] == 0)
			{
				st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], (double) DROPLIST_COND[i][6]);
				continue;
			}
			if(!st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], DROPLIST_COND[i][7], DROPLIST_COND[i][5], (double) DROPLIST_COND[i][6]) || DROPLIST_COND[i][1] == cond || DROPLIST_COND[i][1] == 0)
				continue;
			st.setCond(Integer.valueOf(DROPLIST_COND[i][1]).intValue());
			st.setState(2);
		}
		if(cond == 3 && st.getQuestItemsCount(2844) != 0 && st.getQuestItemsCount(2845) != 0 && st.getQuestItemsCount(2846) != 0)
		{
			st.setCond(4);
			st.setState(2);
		}
		return null;
	}
}