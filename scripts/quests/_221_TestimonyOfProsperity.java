package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _221_TestimonyOfProsperity extends Quest implements ScriptFile
{
	private static final int Parman = 30104;
	private static final int Bright = 30466;
	private static final int Emily = 30620;
	private static final int Piotur = 30597;
	private static final int Wilford = 30005;
	private static final int Lilith = 30368;
	private static final int Lockirin = 30531;
	private static final int Spiron = 30532;
	private static final int Shari = 30517;
	private static final int Balanki = 30533;
	private static final int Mion = 30519;
	private static final int Redbonnet = 30553;
	private static final int Keef = 30534;
	private static final int Torocco = 30555;
	private static final int Filaur = 30535;
	private static final int Bolter = 30554;
	private static final int Arin = 30536;
	private static final int Toma = 30556;
	private static final int Nikola = 30621;
	private static final int BoxOfTitan = 30622;
	private static final int RingOfTestimony1st = 3239;
	private static final int BrightsList = 3264;
	private static final int MandragoraPetal = 3265;
	private static final int CrimsonMoss = 3266;
	private static final int MandragoraBouquet = 3267;
	private static final int EmilysRecipe = 3243;
	private static final int BlessedSeed = 3242;
	private static final int CrystalBrooch = 3428;
	private static final int LilithsElvenWafer = 3244;
	private static final int CollectionLicense = 3246;
	private static final int Lockirins1stNotice = 3247;
	private static final int ContributionOfShari = 3252;
	private static final int ReceiptOfContribution1st = 3258;
	private static final int Lockirins2stNotice = 3248;
	private static final int ContributionOfMion = 3253;
	private static final int MarysesRequest = 3255;
	private static final int ContributionOfMaryse = 3254;
	private static final int ReceiptOfContribution2st = 3259;
	private static final int Lockirins3stNotice = 3249;
	private static final int ProcurationOfTorocco = 3263;
	private static final int ReceiptOfContribution3st = 3260;
	private static final int Lockirins4stNotice = 3250;
	private static final int ReceiptOfBolter = 3257;
	private static final int ReceiptOfContribution4st = 3261;
	private static final int Lockirins5stNotice = 3251;
	private static final int ContributionOfToma = 3256;
	private static final int ReceiptOfContribution5st = 3262;
	private static final int OldAccountBook = 3241;
	private static final int ParmansInstructions = 3268;
	private static final int ParmansLetter = 3269;
	private static final int RingOfTestimony2st = 3240;
	private static final int ClayDough = 3270;
	private static final int PatternOfKeyhole = 3271;
	private static final int NikolasList = 3272;
	private static final int RecipeTitanKey = 3023;
	private static final int MaphrTabletFragment = 3245;
	private static final int StakatoShell = 3273;
	private static final int ToadLordSac = 3274;
	private static final int SpiderThorn = 3275;
	private static final int KeyOfTitan = 3030;
	private static final int MarkOfProsperity = 3238;
	private static final int AnimalSkin = 1867;
	private static final int MandragoraSprout = 20154;
	private static final int MandragoraSapling = 20155;
	private static final int MandragoraBlossom = 20156;
	private static final int MandragoraSprout2 = 20223;
	private static final int GiantCrimsonAnt = 20228;
	private static final int MarshStakato = 20157;
	private static final int MarshStakatoWorker = 20230;
	private static final int MarshStakatoSoldier = 20232;
	private static final int MarshStakatoDrone = 20234;
	private static final int ToadLord = 20231;
	private static final int MarshSpider = 20233;
	private static final int[][] DROPLIST_COND = {{1, 0, 20154, 3264, 3265, 20, 60, 1}, {1, 0, 20155, 3264, 3265, 20, 80, 1}, {1, 0, 20156, 3264, 3265, 20, 100, 1}, {1, 0, 20223, 3264, 3265, 20, 30, 1}, {1, 0, 20228, 3264, 3266, 10, 100, 1}, {7, 0, 20157, 0, 3273, 20, 100, 1}, {7, 0, 20230, 0, 3273, 20, 100, 1}, {7, 0, 20232, 0, 3273, 20, 100, 1}, {7, 0, 20234, 0, 3273, 20, 100, 1}, {7, 0, 20231, 0, 3274, 10, 100, 1}, {7, 0, 20233, 0, 3275, 10, 100, 1}};
	
	public _221_TestimonyOfProsperity()
	{
		super(false);
		addStartNpc(30104);
		addTalkId(30466);
		addTalkId(30620);
		addTalkId(30597);
		addTalkId(30005);
		addTalkId(30368);
		addTalkId(30517);
		addTalkId(30519);
		addTalkId(30531);
		addTalkId(30532);
		addTalkId(30533);
		addTalkId(30534);
		addTalkId(30535);
		addTalkId(30536);
		addTalkId(30553);
		addTalkId(30554);
		addTalkId(30555);
		addTalkId(30556);
		addTalkId(30621);
		addTalkId(30622);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
		}
		addQuestItem(3239, 3264, 3267, 3242, 3243, 3428, 3244, 3246, 3247, 3248, 3249, 3250, 3251, 3258, 3259, 3260, 3261, 3262, 3241, 3252, 3254, 3253, 3263, 3257, 3256, 3255, 3268, 3240, 3245, 3271, 3270, 3272, 3265, 3266, 3273, 3274, 3275);
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
		if(event.equalsIgnoreCase("30104-04.htm"))
		{
			if(!st.getPlayer().getVarB("dd2"))
			{
				st.giveItems(7562, 50);
				st.getPlayer().setVar("dd2", "1", -1);
			}
			st.playSound("ItemSound.quest_accept");
			st.giveItems(3239, 1);
			st.setCond(1);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("30466-03.htm"))
		{
			st.giveItems(3264, 1);
		}
		else if(event.equalsIgnoreCase("30620-03.htm"))
		{
			st.takeItems(3267, -1);
			st.giveItems(3243, 1);
			htmltext = "30620-03.htm";
			if(st.getQuestItemsCount(3241) > 0 && st.getQuestItemsCount(3242) > 0 && st.getQuestItemsCount(3243) > 0 && st.getQuestItemsCount(3244) > 0)
			{
				st.setCond(2);
				st.setState(2);
			}
		}
		else if(event.equalsIgnoreCase("30597-02.htm"))
		{
			st.giveItems(3242, 1);
			if(st.getQuestItemsCount(3241) > 0 && st.getQuestItemsCount(3242) > 0 && st.getQuestItemsCount(3243) > 0 && st.getQuestItemsCount(3244) > 0)
			{
				st.setCond(2);
				st.setState(2);
			}
		}
		else if(event.equalsIgnoreCase("30005-04.htm"))
		{
			st.giveItems(3428, 1);
		}
		else if(event.equalsIgnoreCase("30368-03.htm"))
		{
			st.takeItems(3428, -1);
			st.giveItems(3244, 1);
			if(st.getQuestItemsCount(3241) > 0 && st.getQuestItemsCount(3242) > 0 && st.getQuestItemsCount(3243) > 0 && st.getQuestItemsCount(3244) > 0)
			{
				st.setCond(2);
				st.setState(2);
			}
		}
		else if(event.equalsIgnoreCase("30531-03.htm"))
		{
			st.giveItems(3246, 1);
			st.giveItems(3247, 1);
			st.giveItems(3248, 1);
			st.giveItems(3249, 1);
			st.giveItems(3250, 1);
			st.giveItems(3251, 1);
		}
		else if(event.equalsIgnoreCase("30555-02.htm"))
		{
			st.giveItems(3263, 1);
		}
		else if(event.equalsIgnoreCase("30534-03a.htm") && st.getQuestItemsCount(57) >= 5000)
		{
			htmltext = "30534-03b.htm";
			st.takeItems(57, 5000);
			st.takeItems(3263, -1);
			st.giveItems(3260, 1);
		}
		else if(event.equalsIgnoreCase("30104-07.htm"))
		{
			st.takeItems(3239, -1);
			st.takeItems(3241, -1);
			st.takeItems(3242, -1);
			st.takeItems(3243, -1);
			st.takeItems(3244, -1);
			if(st.getPlayer().getLevel() < 38)
			{
				st.giveItems(3268, 1);
				st.setCond(3);
				st.setState(2);
			}
			else
			{
				st.giveItems(3269, 1);
				st.giveItems(3240, 1);
				htmltext = "30104-08.htm";
				st.setCond(4);
				st.setState(2);
			}
		}
		else if(event.equalsIgnoreCase("30621-04.htm"))
		{
			st.giveItems(3270, 1);
			st.setCond(5);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("30622-02.htm"))
		{
			st.takeItems(3270, -1);
			st.giveItems(3271, 1);
			st.setCond(6);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("30622-04.htm"))
		{
			st.takeItems(3272, -1);
			st.takeItems(3030, 1);
			st.giveItems(3245, 1);
			st.setCond(9);
			st.setState(2);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30104)
		{
			if(st.getQuestItemsCount(3238) != 0)
			{
				htmltext = "completed";
				st.exitCurrentQuest(true);
			}
			else if(cond == 0)
			{
				if(st.getPlayer().getRace() == Race.dwarf)
				{
					if(st.getPlayer().getLevel() >= 37)
					{
						htmltext = "30104-03.htm";
					}
					else
					{
						htmltext = "30104-02.htm";
						st.exitCurrentQuest(true);
					}
				}
				else
				{
					htmltext = "30104-01.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "30104-05.htm";
			}
			else if(cond == 2)
			{
				if(st.getQuestItemsCount(3241) > 0 && st.getQuestItemsCount(3242) > 0 && st.getQuestItemsCount(3243) > 0 && st.getQuestItemsCount(3244) > 0)
				{
					htmltext = "30104-06.htm";
				}
			}
			else if(cond == 3 && st.getQuestItemsCount(3268) > 0)
			{
				if(st.getPlayer().getLevel() < 38)
				{
					htmltext = "30104-09.htm";
				}
				else
				{
					htmltext = "30104-10.htm";
					st.takeItems(3268, -1);
					st.giveItems(3240, 1);
					st.giveItems(3269, 1);
					st.setCond(4);
					st.setState(2);
				}
			}
			else if(cond == 4 && st.getQuestItemsCount(3240) > 0 && st.getQuestItemsCount(3269) > 0 && st.getQuestItemsCount(3245) == 0)
			{
				htmltext = "30104-11.htm";
			}
			else if(cond >= 5 && cond <= 7)
			{
				htmltext = "30104-12.htm";
			}
			else if(cond == 9)
			{
				if(!st.getPlayer().getVarB("prof2.2"))
				{
					st.addExpAndSp(1199958, 80080);
					st.giveItems(57, 217682);
					st.getPlayer().setVar("prof2.2", "1", -1);
				}
				st.takeItems(3240, -1);
				st.takeItems(3245, -1);
				st.giveItems(3238, 1);
				htmltext = "30104-13.htm";
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
		}
		else if(npcId == 30531)
		{
			if(st.getQuestItemsCount(3246) == 0)
			{
				htmltext = "30531-01.htm";
			}
			else if(st.getQuestItemsCount(3246) > 0)
			{
				if(st.getQuestItemsCount(3258) > 0 && st.getQuestItemsCount(3259) > 0 && st.getQuestItemsCount(3260) > 0 && st.getQuestItemsCount(3261) > 0 && st.getQuestItemsCount(3262) > 0)
				{
					htmltext = "30531-05.htm";
					st.takeItems(3246, -1);
					st.takeItems(3258, -1);
					st.takeItems(3259, -1);
					st.takeItems(3260, -1);
					st.takeItems(3261, -1);
					st.takeItems(3262, -1);
					st.giveItems(3241, 1);
					if(st.getQuestItemsCount(3241) > 0 && st.getQuestItemsCount(3242) > 0 && st.getQuestItemsCount(3243) > 0 && st.getQuestItemsCount(3244) > 0)
					{
						st.setCond(2);
					}
				}
				else
				{
					htmltext = "30531-04.htm";
				}
			}
			else if(cond >= 1 && st.getQuestItemsCount(3239) > 0 && st.getQuestItemsCount(3241) > 0 && st.getQuestItemsCount(3246) == 0)
			{
				htmltext = "30531-06.htm";
			}
			else if(cond >= 1 && st.getQuestItemsCount(3240) > 0)
			{
				htmltext = "30531-07.htm";
			}
		}
		else if(npcId == 30532 && cond == 1 && st.getQuestItemsCount(3246) > 0)
		{
			if(st.getQuestItemsCount(3247) > 0)
			{
				htmltext = "30532-01.htm";
				st.takeItems(3247, -1);
			}
			else if(st.getQuestItemsCount(3258) == 0 && st.getQuestItemsCount(3252) == 0)
			{
				htmltext = "30532-02.htm";
			}
			else if(st.getQuestItemsCount(3252) > 0)
			{
				st.takeItems(3252, -1);
				st.giveItems(3258, 1);
				htmltext = "30532-03.htm";
			}
			else if(st.getQuestItemsCount(3258) > 0)
			{
				htmltext = "30532-04.htm";
			}
		}
		else if(npcId == 30517 && cond == 1 && st.getQuestItemsCount(3246) > 0 && st.getQuestItemsCount(3247) == 0 && st.getQuestItemsCount(3258) == 0)
		{
			if(st.getQuestItemsCount(3252) == 0)
			{
				st.giveItems(3252, 1);
				htmltext = "30517-01.htm";
			}
			else
			{
				htmltext = "30517-02.htm";
			}
		}
		else if(npcId == 30533 && cond == 1 && st.getQuestItemsCount(3246) > 0)
		{
			if(st.getQuestItemsCount(3248) > 0)
			{
				htmltext = "30533-01.htm";
				st.takeItems(3248, -1);
			}
			else if(st.getQuestItemsCount(3259) == 0 && (st.getQuestItemsCount(3253) == 0 || st.getQuestItemsCount(3254) == 0))
			{
				htmltext = "30533-02.htm";
			}
			else if(st.getQuestItemsCount(3253) != 0 && st.getQuestItemsCount(3254) != 0)
			{
				htmltext = "30533-03.htm";
				st.takeItems(3254, -1);
				st.takeItems(3253, -1);
				st.giveItems(3259, 1);
			}
			else if(st.getQuestItemsCount(3259) > 0)
			{
				htmltext = "30533-04.htm";
			}
		}
		else if(npcId == 30519 && cond == 1 && st.getQuestItemsCount(3246) > 0 && st.getQuestItemsCount(3248) == 0 && st.getQuestItemsCount(3259) == 0)
		{
			if(st.getQuestItemsCount(3253) == 0)
			{
				htmltext = "30519-01.htm";
				st.giveItems(3253, 1);
			}
			else
			{
				htmltext = "30519-02.htm";
			}
		}
		else if(npcId == 30553 && cond == 1 && st.getQuestItemsCount(3246) > 0 && st.getQuestItemsCount(3248) == 0 && st.getQuestItemsCount(3259) == 0)
		{
			if(st.getQuestItemsCount(3255) == 0 && st.getQuestItemsCount(3254) == 0)
			{
				htmltext = "30553-01.htm";
				st.giveItems(3255, 1);
			}
			else if(st.getQuestItemsCount(3255) > 0 && st.getQuestItemsCount(3254) == 0)
			{
				if(st.getQuestItemsCount(1867) < 100)
				{
					htmltext = "30553-02.htm";
				}
				else
				{
					htmltext = "30553-03.htm";
					st.takeItems(1867, 100);
					st.takeItems(3255, -1);
					st.giveItems(3254, 1);
				}
			}
			else if(st.getQuestItemsCount(3254) > 0)
			{
				htmltext = "30553-04.htm";
			}
		}
		else if(npcId == 30534 && cond == 1 && st.getQuestItemsCount(3246) > 0)
		{
			if(st.getQuestItemsCount(3249) > 0)
			{
				htmltext = "30534-01.htm";
				st.takeItems(3249, -1);
			}
			else if(st.getQuestItemsCount(3260) == 0 && st.getQuestItemsCount(3263) == 0)
			{
				htmltext = "30534-02.htm";
			}
			else if(st.getQuestItemsCount(3260) == 0 && st.getQuestItemsCount(3263) > 0)
			{
				htmltext = "30534-03.htm";
			}
			else if(st.getQuestItemsCount(3260) > 0)
			{
				htmltext = "30534-04.htm";
			}
		}
		else if(npcId == 30555 && cond == 1 && st.getQuestItemsCount(3246) > 0 && st.getQuestItemsCount(3249) == 0 && st.getQuestItemsCount(3260) == 0)
		{
			if(st.getQuestItemsCount(3263) == 0)
			{
				htmltext = "30555-01.htm";
			}
			else if(st.getQuestItemsCount(3263) > 0)
			{
				htmltext = "30555-03.htm";
			}
		}
		else if(npcId == 30535 && cond == 1 && st.getQuestItemsCount(3246) > 0)
		{
			if(st.getQuestItemsCount(3250) > 0)
			{
				htmltext = "30535-01.htm";
				st.takeItems(3250, -1);
			}
			else if(st.getQuestItemsCount(3261) == 0 && st.getQuestItemsCount(3257) == 0)
			{
				htmltext = "30535-02.htm";
			}
			else if(st.getQuestItemsCount(3257) > 0 && st.getQuestItemsCount(3261) == 0)
			{
				htmltext = "30535-03.htm";
				st.takeItems(3257, -1);
				st.giveItems(3261, 1);
			}
			else if(st.getQuestItemsCount(3246) > 0 && st.getQuestItemsCount(3261) > 0 && st.getQuestItemsCount(3257) == 0 && st.getQuestItemsCount(3250) == 0)
			{
				htmltext = "30535-04.htm";
			}
		}
		else if(npcId == 30554 && cond == 1 && st.getQuestItemsCount(3246) > 0 && st.getQuestItemsCount(3250) == 0 && st.getQuestItemsCount(3261) == 0)
		{
			if(st.getQuestItemsCount(3257) == 0)
			{
				htmltext = "30554-01.htm";
				st.giveItems(3257, 1);
			}
			else
			{
				htmltext = "30554-02.htm";
			}
		}
		else if(npcId == 30536 && cond == 1 && st.getQuestItemsCount(3246) > 0)
		{
			if(st.getQuestItemsCount(3251) > 0)
			{
				htmltext = "30536-01.htm";
				st.takeItems(3251, -1);
			}
			else if(st.getQuestItemsCount(3262) == 0 && st.getQuestItemsCount(3256) == 0)
			{
				htmltext = "30536-02.htm";
			}
			else if(st.getQuestItemsCount(3262) == 0 && st.getQuestItemsCount(3256) > 0)
			{
				htmltext = "30536-03.htm";
				st.takeItems(3256, -1);
				st.giveItems(3262, 1);
			}
			else
			{
				htmltext = "30536-04.htm";
			}
		}
		else if(npcId == 30556 && cond == 1 && st.getQuestItemsCount(3246) > 0 && st.getQuestItemsCount(3251) == 0 && st.getQuestItemsCount(3262) == 0)
		{
			if(st.getQuestItemsCount(3256) == 0)
			{
				htmltext = "30556-01.htm";
				st.giveItems(3256, 1);
			}
			else
			{
				htmltext = "30556-02.htm";
			}
		}
		else if(npcId == 30597)
		{
			if(cond == 1)
			{
				htmltext = st.getQuestItemsCount(3242) == 0 ? "30597-01.htm" : "30597-03.htm";
			}
			else if(st.getQuestItemsCount(3240) > 0)
			{
				htmltext = "30597-04.htm";
			}
		}
		else if(npcId == 30005)
		{
			if(cond == 1)
			{
				if(st.getQuestItemsCount(3244) == 0 && st.getQuestItemsCount(3428) == 0)
				{
					htmltext = "30005-01.htm";
				}
				else if(st.getQuestItemsCount(3244) == 0 && st.getQuestItemsCount(3428) > 0)
				{
					htmltext = "30005-05.htm";
				}
				else if(st.getQuestItemsCount(3244) > 0)
				{
					htmltext = "30005-06.htm";
				}
			}
			else if(st.getQuestItemsCount(3240) > 0)
			{
				htmltext = "30005-07.htm";
			}
		}
		else if(npcId == 30368)
		{
			if(cond == 1)
			{
				if(st.getQuestItemsCount(3428) > 0 && st.getQuestItemsCount(3244) == 0)
				{
					htmltext = "30368-01.htm";
				}
				else if(st.getQuestItemsCount(3244) > 0 && st.getQuestItemsCount(3428) == 0)
				{
					htmltext = "30368-04.htm";
				}
			}
			else if(st.getQuestItemsCount(3240) > 0)
			{
				htmltext = "30368-05.htm";
			}
		}
		else if(npcId == 30466)
		{
			if(cond == 1)
			{
				if(st.getQuestItemsCount(3264) == 0 && st.getQuestItemsCount(3243) == 0 && st.getQuestItemsCount(3267) == 0)
				{
					htmltext = "30466-01.htm";
				}
				else if(st.getQuestItemsCount(3265) < 20 || st.getQuestItemsCount(3266) < 10)
				{
					htmltext = "30466-04.htm";
				}
				else if(st.getQuestItemsCount(3265) >= 20 || st.getQuestItemsCount(3266) >= 10)
				{
					st.takeItems(3264, -1);
					st.takeItems(3265, -1);
					st.takeItems(3266, -1);
					st.giveItems(3267, 1);
					htmltext = "30466-05.htm";
				}
				else if(st.getQuestItemsCount(3267) > 0 && st.getQuestItemsCount(3243) == 0)
				{
					htmltext = "30466-06.htm";
				}
				else if(st.getQuestItemsCount(3243) > 0)
				{
					htmltext = "30466-07.htm";
				}
			}
			else if(st.getQuestItemsCount(3240) > 0)
			{
				htmltext = "30466-08.htm";
			}
		}
		else if(npcId == 30620)
		{
			if(cond == 1)
			{
				htmltext = st.getQuestItemsCount(3267) != 0 ? "30620-01.htm" : "30620-04.htm";
			}
			else if(st.getQuestItemsCount(3240) > 0)
			{
				htmltext = "30620-05.htm";
			}
		}
		else if(npcId == 30621)
		{
			if(cond == 4)
			{
				htmltext = "30621-01.htm";
			}
			else if(cond == 5)
			{
				htmltext = "30621-05.htm";
			}
			else if(cond == 6)
			{
				st.takeItems(3271, -1);
				st.giveItems(3272, 1);
				st.giveItems(3023, 1);
				htmltext = "30621-06.htm";
				st.setCond(7);
				st.setState(2);
			}
			else if(cond == 7)
			{
				htmltext = "30621-07.htm";
			}
			else if(cond == 8 && st.getQuestItemsCount(3030) > 0)
			{
				htmltext = "30621-08.htm";
			}
			else if(cond == 9)
			{
				htmltext = "30621-09.htm";
			}
		}
		else if(npcId == 30622)
		{
			if(cond == 5)
			{
				htmltext = "30622-01.htm";
			}
			else if(cond == 8 && st.getQuestItemsCount(3030) > 0)
			{
				htmltext = "30622-03.htm";
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
		if(cond == 7 && st.getQuestItemsCount(3273) >= 20 && st.getQuestItemsCount(3274) >= 10 && st.getQuestItemsCount(3275) >= 10)
		{
			st.setCond(8);
			st.setState(2);
		}
		return null;
	}
}