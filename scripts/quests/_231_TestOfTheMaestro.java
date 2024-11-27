package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _231_TestOfTheMaestro extends Quest implements ScriptFile
{
	private static final int Lockirin = 30531;
	private static final int Balanki = 30533;
	private static final int Arin = 30536;
	private static final int Filaur = 30535;
	private static final int Spiron = 30532;
	private static final int Croto = 30671;
	private static final int Kamur = 30675;
	private static final int Dubabah = 30672;
	private static final int Toma = 30556;
	private static final int Lorain = 30673;
	private static final int RecommendationOfBalanki = 2864;
	private static final int RecommendationOfFilaur = 2865;
	private static final int RecommendationOfArin = 2866;
	private static final int LetterOfSolderDetachment = 2868;
	private static final int PaintOfKamuru = 2869;
	private static final int NecklaceOfKamuru = 2870;
	private static final int PaintOfTeleportDevice = 2871;
	private static final int TeleportDevice = 2872;
	private static final int ArchitectureOfCruma = 2873;
	private static final int ReportOfCruma = 2874;
	private static final int IngredientsOfAntidote = 2875;
	private static final int StingerWaspNeedle = 2876;
	private static final int MarshSpidersWeb = 2877;
	private static final int BloodOfLeech = 2878;
	private static final int BrokenTeleportDevice = 2916;
	private static final int DD = 7562;
	private static final int MarkOfMaestro = 2867;
	private static final int QuestMonsterEvilEyeLord = 27133;
	private static final int GiantMistLeech = 20225;
	private static final int StingerWasp = 20229;
	private static final int MarshSpider = 20233;
	private static final int[][] DROPLIST_COND = {{4, 5, 27133, 0, 2870, 1, 100, 1}, {13, 0, 20225, 0, 2878, 10, 100, 1}, {13, 0, 20229, 0, 2876, 10, 100, 1}, {13, 0, 20233, 0, 2877, 10, 100, 1}};
	
	public _231_TestOfTheMaestro()
	{
		super(false);
		addStartNpc(30531);
		addTalkId(30533);
		addTalkId(30536);
		addTalkId(30535);
		addTalkId(30532);
		addTalkId(30671);
		addTalkId(30675);
		addTalkId(30672);
		addTalkId(30556);
		addTalkId(30673);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
			addQuestItem(DROPLIST_COND[i][4]);
		}
		addQuestItem(2869, 2868, 2871, 2916, 2872, 2873, 2875, 2864, 2865, 2866, 2874);
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
	
	public void recommendationCount(QuestState st)
	{
		if(st.getQuestItemsCount(2866) != 0 && st.getQuestItemsCount(2865) != 0 && st.getQuestItemsCount(2864) != 0)
		{
			st.setCond(17);
		}
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("30531-04.htm"))
		{
			if(!st.getPlayer().getVarB("dd3"))
			{
				st.giveItems(7562, 23);
				st.getPlayer().setVar("dd3", "1", -1);
			}
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30533-02.htm"))
		{
			st.setCond(2);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("30671-02.htm"))
		{
			st.giveItems(2869, 1);
			st.setCond(3);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("30556-05.htm"))
		{
			st.takeItems(2871, -1);
			st.giveItems(2916, 1);
			st.setCond(9);
			st.setState(2);
			st.getPlayer().teleToLocation(140352, -194133, -2028);
		}
		else if(event.equalsIgnoreCase("30673-04.htm"))
		{
			st.takeItems(2878, -1);
			st.takeItems(2876, -1);
			st.takeItems(2877, -1);
			st.takeItems(2875, -1);
			st.giveItems(2874, 1);
			st.setCond(15);
			st.setState(2);
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
		if(npcId == 30531)
		{
			if(st.getQuestItemsCount(2867) != 0)
			{
				htmltext = "completed";
				st.exitCurrentQuest(true);
			}
			else if(cond == 0)
			{
				if(st.getPlayer().getClassId().getId() == 56)
				{
					if(st.getPlayer().getLevel() >= 39)
					{
						htmltext = "30531-03.htm";
					}
					else
					{
						htmltext = "30531-01.htm";
						st.exitCurrentQuest(true);
					}
				}
				else
				{
					htmltext = "30531-02.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond >= 1 && cond <= 16)
			{
				htmltext = "30531-05.htm";
			}
			else if(cond == 17)
			{
				if(!st.getPlayer().getVarB("prof2.3"))
				{
					st.addExpAndSp(154499, 37500);
					st.getPlayer().setVar("prof2.3", "1", -1);
				}
				htmltext = "30531-06.htm";
				st.takeItems(2864, -1);
				st.takeItems(2865, -1);
				st.takeItems(2866, -1);
				st.giveItems(2867, 1);
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
		}
		else if(npcId == 30533)
		{
			if((cond == 1 || cond == 11 || cond == 16) && st.getQuestItemsCount(2864) == 0)
			{
				htmltext = "30533-01.htm";
			}
			else if(cond == 2)
			{
				htmltext = "30533-03.htm";
			}
			else if(cond == 6)
			{
				st.takeItems(2868, -1);
				st.giveItems(2864, 1);
				htmltext = "30533-04.htm";
				st.setCond(7);
				recommendationCount(st);
				st.setState(2);
			}
			else if(cond == 7 || cond == 17)
			{
				htmltext = "30533-05.htm";
			}
		}
		else if(npcId == 30536)
		{
			if((cond == 1 || cond == 7 || cond == 16) && st.getQuestItemsCount(2866) == 0)
			{
				st.giveItems(2871, 1);
				htmltext = "30536-01.htm";
				st.setCond(8);
				st.setState(2);
			}
			else if(cond == 8)
			{
				htmltext = "30536-02.htm";
			}
			else if(cond == 10)
			{
				st.takeItems(2872, -1);
				st.giveItems(2866, 1);
				htmltext = "30536-03.htm";
				st.setCond(11);
				recommendationCount(st);
				st.setState(2);
			}
			else if(cond == 11 || cond == 17)
			{
				htmltext = "30536-04.htm";
			}
		}
		else if(npcId == 30535)
		{
			if((cond == 1 || cond == 7 || cond == 11) && st.getQuestItemsCount(2865) == 0)
			{
				st.giveItems(2873, 1);
				htmltext = "30535-01.htm";
				st.setCond(12);
				st.setState(2);
			}
			else if(cond == 12)
			{
				htmltext = "30535-02.htm";
			}
			else if(cond == 15)
			{
				st.takeItems(2874, 1);
				st.giveItems(2865, 1);
				st.setCond(16);
				htmltext = "30535-03.htm";
				recommendationCount(st);
				st.setState(2);
			}
			else if(cond > 15)
			{
				htmltext = "30535-04.htm";
			}
		}
		else if(npcId == 30671)
		{
			if(cond == 2)
			{
				htmltext = "30671-01.htm";
			}
			else if(cond == 3)
			{
				htmltext = "30671-03.htm";
			}
			else if(cond == 5)
			{
				st.takeItems(2870, -1);
				st.takeItems(2869, -1);
				st.giveItems(2868, 1);
				htmltext = "30671-04.htm";
				st.setCond(6);
				st.setState(2);
			}
			else if(cond == 6)
			{
				htmltext = "30671-05.htm";
			}
		}
		else if(npcId == 30672 && cond == 3)
		{
			htmltext = "30672-01.htm";
		}
		else if(npcId == 30675 && cond == 3)
		{
			htmltext = "30675-01.htm";
			st.setCond(4);
			st.setState(2);
		}
		else if(npcId == 30556)
		{
			if(cond == 8)
			{
				htmltext = "30556-01.htm";
			}
			else if(cond == 9)
			{
				st.takeItems(2916, -1);
				st.giveItems(2872, 5);
				htmltext = "30556-06.htm";
				st.setCond(10);
				st.setState(2);
			}
			else if(cond == 10)
			{
				htmltext = "30556-07.htm";
			}
		}
		else if(npcId == 30673)
		{
			if(cond == 12)
			{
				st.takeItems(2873, -1);
				st.giveItems(2875, 1);
				st.setCond(13);
				htmltext = "30673-01.htm";
			}
			else if(cond == 13)
			{
				htmltext = "30673-02.htm";
			}
			else if(cond == 14)
			{
				htmltext = "30673-03.htm";
			}
			else if(cond == 15)
			{
				htmltext = "30673-05.htm";
			}
		}
		else if(npcId == 30532 && (cond == 1 || cond == 7 || cond == 11 || cond == 16))
		{
			htmltext = "30532-01.htm";
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
		if(cond == 13 && st.getQuestItemsCount(2878) >= 10 && st.getQuestItemsCount(2876) >= 10 && st.getQuestItemsCount(2877) >= 10)
		{
			st.setCond(14);
			st.setState(2);
		}
		return null;
	}
}