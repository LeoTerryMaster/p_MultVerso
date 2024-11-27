package quests;

import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _219_TestimonyOfFate extends Quest implements ScriptFile
{
	private static final int Kaira = 30476;
	private static final int Metheus = 30614;
	private static final int Ixia = 30463;
	private static final int AldersSpirit = 30613;
	private static final int Roa = 30114;
	private static final int Norman = 30210;
	private static final int Thifiell = 30358;
	private static final int Arkenia = 30419;
	private static final int BloodyPixy = 31845;
	private static final int BlightTreant = 31850;
	private static final int KairasLetter = 3173;
	private static final int MetheussFuneralJar = 3174;
	private static final int KasandrasRemains = 3175;
	private static final int HerbalismTextbook = 3176;
	private static final int IxiasList = 3177;
	private static final int MedusasIchor = 3178;
	private static final int MarshSpiderFluids = 3179;
	private static final int DeadSeekerDung = 3180;
	private static final int TyrantsBlood = 3181;
	private static final int NightshadeRoot = 3182;
	private static final int Belladonna = 3183;
	private static final int AldersSkull1 = 3184;
	private static final int AldersSkull2 = 3185;
	private static final int AldersReceipt = 3186;
	private static final int RevelationsManuscript = 3187;
	private static final int KairasRecommendation = 3189;
	private static final int KairasInstructions = 3188;
	private static final int PalusCharm = 3190;
	private static final int ThifiellsLetter = 3191;
	private static final int ArkeniasNote = 3192;
	private static final int PixyGarnet = 3193;
	private static final int BlightTreantSeed = 3199;
	private static final int GrandissSkull = 3194;
	private static final int KarulBugbearSkull = 3195;
	private static final int BrekaOverlordSkull = 3196;
	private static final int LetoOverlordSkull = 3197;
	private static final int BlackWillowLeaf = 3200;
	private static final int RedFairyDust = 3198;
	private static final int BlightTreantSap = 3201;
	private static final int ArkeniasLetter = 1246;
	private static final int MarkofFate = 3172;
	private static final int HangmanTree = 20144;
	private static final int Medusa = 20158;
	private static final int MarshSpider = 20233;
	private static final int DeadSeeker = 20202;
	private static final int Tyrant = 20192;
	private static final int TyrantKingpin = 20193;
	private static final int MarshStakatoWorker = 20230;
	private static final int MarshStakato = 20157;
	private static final int MarshStakatoSoldier = 20232;
	private static final int MarshStakatoDrone = 20234;
	private static final int Grandis = 20554;
	private static final int KarulBugbear = 20600;
	private static final int BrekaOrcOverlord = 20270;
	private static final int LetoLizardmanOverlord = 20582;
	private static final int BlackWillowLurker = 27079;
	private static final int[][] DROPLIST_COND = {{6, 0, 20158, 3177, 3178, 10, 100, 1}, {6, 0, 20233, 3177, 3179, 10, 100, 1}, {6, 0, 20202, 3177, 3180, 10, 100, 1}, {6, 0, 20192, 3177, 3181, 10, 100, 1}, {6, 0, 20193, 3177, 3181, 10, 100, 1}, {6, 0, 20230, 3177, 3182, 10, 100, 1}, {6, 0, 20157, 3177, 3182, 10, 100, 1}, {6, 0, 20232, 3177, 3182, 10, 100, 1}, {6, 0, 20234, 3177, 3182, 10, 100, 1}, {17, 0, 20554, 3193, 3194, 10, 100, 1}, {17, 0, 20600, 3193, 3195, 10, 100, 1}, {17, 0, 20270, 3193, 3196, 10, 100, 1}, {17, 0, 20582, 3193, 3197, 10, 100, 1}, {17, 0, 27079, 3199, 3200, 10, 100, 1}};
	
	public _219_TestimonyOfFate()
	{
		super(false);
		addStartNpc(30476);
		addTalkId(30614);
		addTalkId(30463);
		addTalkId(30613);
		addTalkId(30114);
		addTalkId(30210);
		addTalkId(30358);
		addTalkId(30419);
		addTalkId(31845);
		addTalkId(31850);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
		}
		addKillId(20144);
		addQuestItem(3173, 3174, 3175, 3177, 3183, 3184, 3185, 3186, 3187, 3189, 3188, 3191, 3190, 3192, 3193, 3199, 3198, 3201, 1246, 3178, 3179, 3180, 3181, 3182, 3194, 3195, 3196, 3197, 3200);
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
		if(event.equalsIgnoreCase("30476-05.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(3173, 1);
			if(!st.getPlayer().getVarB("dd2"))
			{
				st.giveItems(7562, 72);
				st.getPlayer().setVar("dd2", "1", -1);
			}
		}
		else if(event.equalsIgnoreCase("30114-04.htm"))
		{
			st.takeItems(3185, 1);
			st.giveItems(3186, 1);
			st.setCond(12);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("30476-12.htm"))
		{
			if(st.getPlayer().getLevel() >= 38)
			{
				st.takeItems(3187, -1);
				st.giveItems(3189, 1);
				st.setCond(15);
				st.setState(2);
			}
			else
			{
				htmltext = "30476-13.htm";
				st.takeItems(3187, -1);
				st.giveItems(3188, 1);
				st.setCond(14);
				st.setState(2);
			}
		}
		else if(event.equalsIgnoreCase("30419-02.htm"))
		{
			st.takeItems(3191, -1);
			st.giveItems(3192, 1);
			st.setCond(17);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("31845-02.htm"))
		{
			st.giveItems(3193, 1);
		}
		else if(event.equalsIgnoreCase("31850-02.htm"))
		{
			st.giveItems(3199, 1);
		}
		else if(event.equalsIgnoreCase("30419-05.htm"))
		{
			st.takeItems(3192, -1);
			st.takeItems(3198, -1);
			st.takeItems(3201, -1);
			st.giveItems(1246, 1);
			st.setCond(18);
			st.setState(2);
		}
		if(event.equalsIgnoreCase("AldersSpirit_Fail"))
		{
			NpcInstance isQuest = GameObjectsStorage.getByNpcId(30613);
			if(isQuest != null)
			{
				isQuest.deleteMe();
			}
			st.setCond(9);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30476)
		{
			if(st.getQuestItemsCount(3172) != 0)
			{
				htmltext = "completed";
				st.exitCurrentQuest(true);
			}
			else if(cond == 0)
			{
				if(st.getPlayer().getRace() == Race.darkelf && st.getPlayer().getLevel() >= 37)
				{
					htmltext = "30476-03.htm";
				}
				else if(st.getPlayer().getRace() == Race.darkelf)
				{
					htmltext = "30476-02.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "30476-01.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 2)
			{
				htmltext = "30476-06.htm";
			}
			else if(cond == 9 || cond == 10)
			{
				NpcInstance AldersSpiritObject = GameObjectsStorage.getByNpcId(30613);
				if(AldersSpiritObject == null)
				{
					st.takeItems(3184, -1);
					if(st.getQuestItemsCount(3185) == 0)
					{
						st.giveItems(3185, 1);
					}
					htmltext = "30476-09.htm";
					st.setCond(10);
					st.setState(2);
					st.addSpawn(30613);
					st.startQuestTimer("AldersSpirit_Fail", 300000);
				}
				else
				{
					htmltext = "<html><head><body>I am borrowed, approach in some minutes</body></html>";
				}
			}
			else if(cond == 13)
			{
				htmltext = "30476-11.htm";
			}
			else if(cond == 14)
			{
				if(st.getQuestItemsCount(3188) != 0 && st.getPlayer().getLevel() < 38)
				{
					htmltext = "30476-14.htm";
				}
				else if(st.getQuestItemsCount(3188) != 0 && st.getPlayer().getLevel() >= 38)
				{
					st.giveItems(3189, 1);
					st.takeItems(3188, 1);
					htmltext = "30476-15.htm";
					st.setCond(15);
					st.setState(2);
				}
			}
			else if(cond == 15)
			{
				htmltext = "30476-16.htm";
			}
			else if(cond == 16 || cond == 17)
			{
				htmltext = "30476-17.htm";
			}
			else if(st.getQuestItemsCount(3174) > 0 || st.getQuestItemsCount(3175) > 0)
			{
				htmltext = "30476-07.htm";
			}
			else if(st.getQuestItemsCount(3176) > 0 || st.getQuestItemsCount(3177) > 0)
			{
				htmltext = "30476-08.htm";
			}
			else if(st.getQuestItemsCount(3185) > 0 || st.getQuestItemsCount(3186) > 0)
			{
				htmltext = "30476-10.htm";
			}
		}
		else if(npcId == 30614)
		{
			if(cond == 1)
			{
				htmltext = "30614-01.htm";
				st.takeItems(3173, -1);
				st.giveItems(3174, 1);
				st.setCond(2);
				st.setState(2);
			}
			else if(cond == 2)
			{
				htmltext = "30614-02.htm";
			}
			else if(cond == 3)
			{
				st.takeItems(3175, -1);
				st.giveItems(3176, 1);
				htmltext = "30614-03.htm";
				st.setCond(5);
				st.setState(2);
			}
			else if(cond == 8)
			{
				st.takeItems(3183, -1);
				st.giveItems(3184, 1);
				htmltext = "30614-05.htm";
				st.setCond(9);
				st.setState(2);
			}
			else if(st.getQuestItemsCount(3176) > 0 || st.getQuestItemsCount(3177) > 0)
			{
				htmltext = "30614-04.htm";
			}
			else if(st.getQuestItemsCount(3184) > 0 || st.getQuestItemsCount(3185) > 0 || st.getQuestItemsCount(3186) > 0 || st.getQuestItemsCount(3187) > 0 || st.getQuestItemsCount(3188) > 0 || st.getQuestItemsCount(3189) > 0)
			{
				htmltext = "30614-06.htm";
			}
		}
		else if(npcId == 30463)
		{
			if(cond == 5)
			{
				st.takeItems(3176, -1);
				st.giveItems(3177, 1);
				htmltext = "30463-01.htm";
				st.setCond(6);
				st.setState(2);
			}
			else if(cond == 6)
			{
				htmltext = "30463-02.htm";
			}
			else if(cond == 7 && st.getQuestItemsCount(3178) >= 10 && st.getQuestItemsCount(3179) >= 10 && st.getQuestItemsCount(3180) >= 10 && st.getQuestItemsCount(3181) >= 10 && st.getQuestItemsCount(3182) >= 10)
			{
				st.takeItems(3178, -1);
				st.takeItems(3179, -1);
				st.takeItems(3180, -1);
				st.takeItems(3181, -1);
				st.takeItems(3182, -1);
				st.takeItems(3177, -1);
				st.giveItems(3183, 1);
				htmltext = "30463-03.htm";
				st.setCond(8);
				st.setState(2);
			}
			else if(cond == 7)
			{
				htmltext = "30463-02.htm";
				st.setCond(6);
			}
			else if(cond == 8)
			{
				htmltext = "30463-04.htm";
			}
			else if(st.getQuestItemsCount(3184) > 0 || st.getQuestItemsCount(3185) > 0 || st.getQuestItemsCount(3186) > 0 || st.getQuestItemsCount(3187) > 0 || st.getQuestItemsCount(3188) > 0 || st.getQuestItemsCount(3189) > 0)
			{
				htmltext = "30463-05.htm";
			}
		}
		else if(npcId == 30613)
		{
			htmltext = "30613-02.htm";
			st.setCond(11);
			st.setState(2);
			st.cancelQuestTimer("AldersSpirit_Fail");
			NpcInstance isQuest = GameObjectsStorage.getByNpcId(30613);
			if(isQuest != null)
			{
				isQuest.deleteMe();
			}
		}
		else if(npcId == 30114)
		{
			if(cond == 11)
			{
				htmltext = "30114-01.htm";
			}
			else if(cond == 12)
			{
				htmltext = "30114-05.htm";
			}
			else if(st.getQuestItemsCount(3187) > 0 || st.getQuestItemsCount(3188) > 0 || st.getQuestItemsCount(3189) > 0)
			{
				htmltext = "30114-06.htm";
			}
		}
		else if(npcId == 30210)
		{
			if(cond == 12)
			{
				st.takeItems(3186, -1);
				st.giveItems(3187, 1);
				htmltext = "30210-01.htm";
				st.setCond(13);
				st.setState(2);
			}
			else if(cond == 13)
			{
				htmltext = "30210-02.htm";
			}
		}
		else if(npcId == 30358)
		{
			if(cond == 15)
			{
				st.takeItems(3189, -1);
				st.giveItems(3191, 1);
				st.giveItems(3190, 1);
				htmltext = "30358-01.htm";
				st.setCond(16);
				st.setState(2);
			}
			else if(cond == 16)
			{
				htmltext = "30358-02.htm";
			}
			else if(cond == 17)
			{
				htmltext = "30358-03.htm";
			}
			else if(cond == 18)
			{
				if(!st.getPlayer().getVarB("prof2.2"))
				{
					st.addExpAndSp(68183, 1750);
					st.getPlayer().setVar("prof2.2", "1", -1);
				}
				st.takeItems(1246, -1);
				st.takeItems(3190, -1);
				st.giveItems(3172, 1);
				htmltext = "30358-04.htm";
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
		}
		else if(npcId == 30419)
		{
			if(cond == 16)
			{
				htmltext = "30419-01.htm";
			}
			else if(cond == 17)
			{
				if(st.getQuestItemsCount(3198) < 1 || st.getQuestItemsCount(3201) < 1)
				{
					htmltext = "30419-03.htm";
				}
				else if(st.getQuestItemsCount(3198) >= 1 && st.getQuestItemsCount(3201) >= 1)
				{
					htmltext = "30419-04.htm";
				}
			}
			else if(cond == 18)
			{
				htmltext = "30419-06.htm";
			}
		}
		else if(npcId == 31845 && cond == 17)
		{
			if(st.getQuestItemsCount(3198) == 0 && st.getQuestItemsCount(3193) == 0)
			{
				htmltext = "31845-01.htm";
			}
			else if(st.getQuestItemsCount(3198) == 0 && st.getQuestItemsCount(3193) > 0 && (st.getQuestItemsCount(3194) < 10 || st.getQuestItemsCount(3195) < 10 || st.getQuestItemsCount(3196) < 10 || st.getQuestItemsCount(3197) < 10))
			{
				htmltext = "31845-03.htm";
			}
			else if(st.getQuestItemsCount(3198) == 0 && st.getQuestItemsCount(3193) > 0 && st.getQuestItemsCount(3194) >= 10 && st.getQuestItemsCount(3195) >= 10 && st.getQuestItemsCount(3196) >= 10 && st.getQuestItemsCount(3197) >= 10)
			{
				st.takeItems(3194, -1);
				st.takeItems(3195, -1);
				st.takeItems(3196, -1);
				st.takeItems(3197, -1);
				st.takeItems(3193, -1);
				st.giveItems(3198, 1);
				htmltext = "31845-04.htm";
			}
			else if(st.getQuestItemsCount(3198) != 0)
			{
				htmltext = "31845-05.htm";
			}
		}
		else if(npcId == 31850 && cond == 17)
		{
			if(st.getQuestItemsCount(3201) == 0 && st.getQuestItemsCount(3199) == 0)
			{
				htmltext = "31850-01.htm";
			}
			else if(st.getQuestItemsCount(3201) == 0 && st.getQuestItemsCount(3199) > 0 && st.getQuestItemsCount(3200) == 0)
			{
				htmltext = "31850-03.htm";
			}
			else if(st.getQuestItemsCount(3201) == 0 && st.getQuestItemsCount(3199) > 0 && st.getQuestItemsCount(3200) > 0)
			{
				st.takeItems(3200, -1);
				st.takeItems(3199, -1);
				st.giveItems(3201, 1);
				htmltext = "31850-04.htm";
			}
			else if(st.getQuestItemsCount(3201) > 0)
			{
				htmltext = "31850-05.htm";
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
		if(cond == 2 && npcId == 20144)
		{
			st.takeItems(3174, -1);
			st.giveItems(3175, 1);
			st.playSound("ItemSound.quest_middle");
			st.setCond(3);
			st.setState(2);
		}
		else if(cond == 6 && st.getQuestItemsCount(3178) >= 10 && st.getQuestItemsCount(3179) >= 10 && st.getQuestItemsCount(3180) >= 10 && st.getQuestItemsCount(3181) >= 10 && st.getQuestItemsCount(3182) >= 10)
		{
			st.setCond(7);
			st.setState(2);
		}
		return null;
	}
}