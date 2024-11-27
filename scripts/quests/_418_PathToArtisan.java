package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _418_PathToArtisan extends Quest implements ScriptFile
{
	private static final int Silvera = 30527;
	private static final int Kluto = 30317;
	private static final int Pinter = 30298;
	private static final int SilverasRing = 1632;
	private static final int BoogleRatmanTooth = 1636;
	private static final int BoogleRatmanLeadersTooth = 1637;
	private static final int PassCertificate1st = 1633;
	private static final int KlutosLetter = 1638;
	private static final int FootprintOfThief = 1639;
	private static final int StolenSecretBox = 1640;
	private static final int PassCertificate2nd = 1634;
	private static final int SecretBox = 1641;
	private static final int FinalPassCertificate = 1635;
	private static final int BoogleRatman = 20389;
	private static final int BoogleRatmanLeader = 20390;
	private static final int VukuOrcFighter = 20017;
	private static final int[][] DROPLIST_COND = {{1, 0, 20389, 1632, 1636, 10, 35, 1}, {1, 0, 20390, 1632, 1637, 2, 25, 1}, {5, 6, 20017, 1639, 1640, 1, 20, 1}};
	
	public _418_PathToArtisan()
	{
		super(false);
		addStartNpc(30527);
		addTalkId(30317, 30298);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
			addQuestItem(DROPLIST_COND[i][4]);
		}
		addQuestItem(1632, 1633, 1641, 1638, 1639, 1634);
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
		if(event.equalsIgnoreCase("blacksmith_silvery_q0418_06.htm"))
		{
			st.giveItems(1632, 1);
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("blacksmith_kluto_q0418_04.htm") || event.equalsIgnoreCase("blacksmith_kluto_q0418_07.htm"))
		{
			st.giveItems(1638, 1);
			st.setCond(4);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("blacksmith_pinter_q0418_03.htm"))
		{
			st.takeItems(1638, -1);
			st.giveItems(1639, 1);
			st.setCond(5);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("blacksmith_pinter_q0418_06.htm"))
		{
			st.takeItems(1640, -1);
			st.takeItems(1639, -1);
			st.giveItems(1641, 1);
			st.giveItems(1634, 1);
			st.setCond(7);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("blacksmith_kluto_q0418_10.htm") || event.equalsIgnoreCase("blacksmith_kluto_q0418_12.htm"))
		{
			st.takeItems(1633, -1);
			st.takeItems(1634, -1);
			st.takeItems(1641, -1);
			if(st.getPlayer().getClassId().getLevel() == 1)
			{
				st.giveItems(1635, 1);
				if(!st.getPlayer().getVarB("prof1"))
				{
					st.getPlayer().setVar("prof1", "1", -1);
					st.addExpAndSp(3200, 2790);
				}
			}
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
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
		if(npcId == 30527)
		{
			if(st.getQuestItemsCount(1635) != 0)
			{
				htmltext = "blacksmith_silvery_q0418_04.htm";
				st.exitCurrentQuest(true);
			}
			else if(cond == 0)
			{
				if(st.getPlayer().getClassId().getId() != 53)
				{
					htmltext = st.getPlayer().getClassId().getId() == 56 ? "blacksmith_silvery_q0418_02a.htm" : "blacksmith_silvery_q0418_02.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getPlayer().getLevel() < 18)
				{
					htmltext = "blacksmith_silvery_q0418_03.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "blacksmith_silvery_q0418_01.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "blacksmith_silvery_q0418_07.htm";
			}
			else if(cond == 2)
			{
				st.takeItems(1636, -1);
				st.takeItems(1637, -1);
				st.takeItems(1632, -1);
				st.giveItems(1633, 1);
				htmltext = "blacksmith_silvery_q0418_08.htm";
				st.setCond(3);
			}
			else if(cond == 3)
			{
				htmltext = "blacksmith_silvery_q0418_09.htm";
			}
		}
		else if(npcId == 30317)
		{
			if(cond == 3)
			{
				htmltext = "blacksmith_kluto_q0418_01.htm";
			}
			else if(cond == 4 || cond == 5)
			{
				htmltext = "blacksmith_kluto_q0418_08.htm";
			}
			else if(cond == 7)
			{
				htmltext = "blacksmith_kluto_q0418_09.htm";
			}
		}
		else if(npcId == 30298)
		{
			if(cond == 4)
			{
				htmltext = "blacksmith_pinter_q0418_01.htm";
			}
			else if(cond == 5)
			{
				htmltext = "blacksmith_pinter_q0418_04.htm";
			}
			else if(cond == 6)
			{
				htmltext = "blacksmith_pinter_q0418_05.htm";
			}
			else if(cond == 7)
			{
				htmltext = "blacksmith_pinter_q0418_07.htm";
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
		if(cond == 1 && st.getQuestItemsCount(1636) >= 10 && st.getQuestItemsCount(1637) >= 2)
		{
			st.setCond(2);
			st.setState(2);
		}
		return null;
	}
}