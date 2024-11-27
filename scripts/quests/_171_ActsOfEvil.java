package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Location;

public class _171_ActsOfEvil extends Quest implements ScriptFile
{
	private static final int Alvah = 30381;
	private static final int Tyra = 30420;
	private static final int Arodin = 30207;
	private static final int Rolento = 30437;
	private static final int Neti = 30425;
	private static final int Burai = 30617;
	private static final int BladeMold = 4239;
	private static final int OlMahumCaptainHead = 4249;
	private static final int TyrasBill = 4240;
	private static final int RangerReportPart1 = 4241;
	private static final int RangerReportPart2 = 4242;
	private static final int RangerReportPart3 = 4243;
	private static final int RangerReportPart4 = 4244;
	private static final int WeaponsTradeContract = 4245;
	private static final int AttackDirectives = 4246;
	private static final int CertificateOfTheSilverScaleGuild = 4247;
	private static final int RolentoCargobox = 4248;
	private static final int TurekOrcArcher = 20496;
	private static final int TurekOrcSkirmisher = 20497;
	private static final int TurekOrcSupplier = 20498;
	private static final int TurekOrcFootman = 20499;
	private static final int TumranBugbear = 20062;
	private static final int OlMahumGeneral = 20438;
	private static final int OlMahumCaptain = 20066;
	private static final int OlMahumSupportTroop = 27190;
	private static final int[][] DROPLIST_COND = {{2, 0, 20496, 0, 4239, 20, 50, 1}, {2, 0, 20497, 0, 4239, 20, 50, 1}, {2, 0, 20498, 0, 4239, 20, 50, 1}, {2, 0, 20499, 0, 4239, 20, 50, 1}, {10, 0, 20438, 0, 4249, 30, 100, 1}, {10, 0, 20066, 0, 4249, 30, 100, 1}};
	private static final int CHANCE2 = 100;
	private static final int CHANCE21 = 20;
	private static final int CHANCE22 = 20;
	private static final int CHANCE23 = 20;
	private static final int CHANCE24 = 10;
	private static final int CHANCE25 = 10;
	public NpcInstance OlMahumSupportTroop_Spawn;
	
	public _171_ActsOfEvil()
	{
		super(false);
		addStartNpc(30381);
		addTalkId(30207);
		addTalkId(30420);
		addTalkId(30437);
		addTalkId(30425);
		addTalkId(30617);
		addKillId(20062);
		addKillId(20438);
		addKillId(27190);
		addQuestItem(4248, 4240, 4247, 4241, 4242, 4243, 4244, 4245, 4246, 4239, 4249);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
		}
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
	
	private void Despawn_OlMahumSupportTroop()
	{
		if(OlMahumSupportTroop_Spawn != null)
		{
			OlMahumSupportTroop_Spawn.deleteMe();
		}
		OlMahumSupportTroop_Spawn = null;
	}
	
	private void Spawn_OlMahumSupportTroop(QuestState st)
	{
		OlMahumSupportTroop_Spawn = Functions.spawn(Location.findPointToStay(st.getPlayer(), 50, 100), 27190);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();
		if(event.equals("30381-02.htm") && cond == 0)
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("30207-02.htm") && cond == 1)
		{
			st.setCond(2);
			st.setState(2);
		}
		else if(event.equals("30381-04.htm") && cond == 4)
		{
			st.setCond(5);
			st.setState(2);
		}
		else if(event.equals("30381-07.htm") && cond == 6)
		{
			st.setCond(7);
			st.setState(2);
			st.takeItems(4245, -1);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equals("30437-03.htm") && cond == 8)
		{
			st.giveItems(4248, 1);
			st.giveItems(4247, 1);
			st.setCond(9);
			st.setState(2);
		}
		else if(event.equals("30617-04.htm") && cond == 9)
		{
			st.takeItems(4247, -1);
			st.takeItems(4246, -1);
			st.takeItems(4248, -1);
			st.setCond(10);
			st.setState(2);
		}
		else if(event.equals("Wait1"))
		{
			Despawn_OlMahumSupportTroop();
			st.cancelQuestTimer("Wait1");
			return null;
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
		if(npcId == 30381)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() <= 26)
				{
					htmltext = "30381-01a.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "30381-01.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "30381-02a.htm";
			}
			else if(cond == 4)
			{
				htmltext = "30381-03.htm";
			}
			else if(cond == 5)
			{
				if(st.getQuestItemsCount(4241) > 0 && st.getQuestItemsCount(4242) > 0 && st.getQuestItemsCount(4243) > 0 && st.getQuestItemsCount(4244) > 0)
				{
					htmltext = "30381-05.htm";
					st.takeItems(4241, -1);
					st.takeItems(4242, -1);
					st.takeItems(4243, -1);
					st.takeItems(4244, -1);
					st.setCond(6);
					st.setState(2);
				}
				else
				{
					htmltext = "30381-04a.htm";
				}
			}
			else if(cond == 6)
			{
				htmltext = st.getQuestItemsCount(4245) > 0 && st.getQuestItemsCount(4246) > 0 ? "30381-06.htm" : "30381-05a.htm";
			}
			else if(cond == 7)
			{
				htmltext = "30381-07a.htm";
			}
			else if(cond == 11)
			{
				htmltext = "30381-08.htm";
				st.giveItems(57, 90000);
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(false);
			}
		}
		else if(npcId == 30207)
		{
			if(cond == 1)
			{
				htmltext = "30207-01.htm";
			}
			else if(cond == 2)
			{
				htmltext = "30207-01a.htm";
			}
			else if(cond == 3)
			{
				if(st.getQuestItemsCount(4240) > 0)
				{
					st.takeItems(4240, -1);
					htmltext = "30207-03.htm";
					st.setCond(4);
					st.setState(2);
				}
				else
				{
					htmltext = "30207-01a.htm";
				}
			}
			else if(cond == 4)
			{
				htmltext = "30207-03a.htm";
			}
		}
		else if(npcId == 30420)
		{
			if(cond == 2)
			{
				if(st.getQuestItemsCount(4239) >= 20)
				{
					st.takeItems(4239, -1);
					st.giveItems(4240, 1);
					htmltext = "30420-01.htm";
					st.setCond(3);
					st.setState(2);
				}
				else
				{
					htmltext = "30420-01b.htm";
				}
			}
			else if(cond == 3)
			{
				htmltext = "30420-01a.htm";
			}
			else if(cond > 3)
			{
				htmltext = "30420-02.htm";
			}
		}
		else if(npcId == 30425)
		{
			if(cond == 7)
			{
				htmltext = "30425-01.htm";
				st.setCond(8);
				st.setState(2);
			}
			else if(cond == 8)
			{
				htmltext = "30425-02.htm";
			}
		}
		else if(npcId == 30437)
		{
			if(cond == 8)
			{
				htmltext = "30437-01.htm";
			}
			else if(cond == 9)
			{
				htmltext = "30437-03a.htm";
			}
		}
		else if(npcId == 30617)
		{
			if(cond == 9 && st.getQuestItemsCount(4247) > 0 && st.getQuestItemsCount(4248) > 0 && st.getQuestItemsCount(4246) > 0)
			{
				htmltext = "30617-01.htm";
			}
			if(cond == 10)
			{
				if(st.getQuestItemsCount(4249) >= 30)
				{
					htmltext = "30617-05.htm";
					st.giveItems(57, 8000);
					st.takeItems(4249, -1);
					st.setCond(11);
					st.setState(2);
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					htmltext = "30617-04a.htm";
				}
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
		if(npcId == 27190)
		{
			Despawn_OlMahumSupportTroop();
		}
		else if(cond == 2 && Rnd.chance(10))
		{
			if(OlMahumSupportTroop_Spawn == null)
			{
				Spawn_OlMahumSupportTroop(st);
			}
			else if(!st.isRunningQuestTimer("Wait1"))
			{
				st.startQuestTimer("Wait1", 300000);
			}
		}
		else if(cond == 5 && npcId == 20062)
		{
			if(st.getQuestItemsCount(4241) == 0 && Rnd.chance(100))
			{
				st.giveItems(4241, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(st.getQuestItemsCount(4242) == 0 && Rnd.chance(20))
			{
				st.giveItems(4242, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(st.getQuestItemsCount(4243) == 0 && Rnd.chance(20))
			{
				st.giveItems(4243, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(st.getQuestItemsCount(4244) == 0 && Rnd.chance(20))
			{
				st.giveItems(4244, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(cond == 6 && npcId == 20438)
		{
			if(st.getQuestItemsCount(4245) == 0 && Rnd.chance(10))
			{
				st.giveItems(4245, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(st.getQuestItemsCount(4246) == 0 && Rnd.chance(10))
			{
				st.giveItems(4246, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}