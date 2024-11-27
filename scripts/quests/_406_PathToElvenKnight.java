package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _406_PathToElvenKnight extends Quest implements ScriptFile
{
	private static final int Sorius = 30327;
	private static final int Kluto = 30317;
	private static final int SoriussLetter = 1202;
	private static final int KlutoBox = 1203;
	private static final int TopazPiece = 1205;
	private static final int EmeraldPiece = 1206;
	private static final int KlutosMemo = 1276;
	private static final int ElvenKnightBrooch = 1204;
	private static final int TrackerSkeleton = 20035;
	private static final int TrackerSkeletonLeader = 20042;
	private static final int SkeletonScout = 20045;
	private static final int SkeletonBowman = 20051;
	private static final int RagingSpartoi = 20060;
	private static final int OlMahumNovice = 20782;
	private static final int[][] DROPLIST_COND = {{1, 2, 20035, 0, 1205, 20, 70, 1}, {1, 2, 20042, 0, 1205, 20, 70, 1}, {1, 2, 20045, 0, 1205, 20, 70, 1}, {1, 2, 20051, 0, 1205, 20, 70, 1}, {1, 2, 20060, 0, 1205, 20, 70, 1}, {4, 5, 20782, 0, 1206, 20, 50, 1}};
	
	public _406_PathToElvenKnight()
	{
		super(false);
		addStartNpc(30327);
		addTalkId(30317);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
		}
		addQuestItem(1205, 1206, 1202, 1276, 1203);
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
		if(event.equalsIgnoreCase("master_sorius_q0406_05.htm"))
		{
			if(st.getPlayer().getClassId().getId() == 18)
			{
				if(st.getQuestItemsCount(1204) > 0)
				{
					htmltext = "master_sorius_q0406_04.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getPlayer().getLevel() < 18)
				{
					htmltext = "master_sorius_q0406_03.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(st.getPlayer().getClassId().getId() == 19)
			{
				htmltext = "master_sorius_q0406_02a.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "master_sorius_q0406_02.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(event.equalsIgnoreCase("master_sorius_q0406_06.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("blacksmith_kluto_q0406_02.htm"))
		{
			st.takeItems(1202, -1);
			st.giveItems(1276, 1);
			st.setCond(4);
			st.setState(2);
		}
		else
		{
			htmltext = "noquest";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30327)
		{
			if(cond == 0)
			{
				htmltext = "master_sorius_q0406_01.htm";
			}
			else if(cond == 1)
			{
				htmltext = st.getQuestItemsCount(1205) == 0 ? "master_sorius_q0406_07.htm" : "master_sorius_q0406_08.htm";
			}
			else if(cond == 2)
			{
				st.takeItems(1205, -1);
				st.giveItems(1202, 1);
				htmltext = "master_sorius_q0406_09.htm";
				st.setCond(3);
				st.setState(2);
			}
			else if(cond == 3 || cond == 4 || cond == 5)
			{
				htmltext = "master_sorius_q0406_11.htm";
			}
			else if(cond == 6)
			{
				st.takeItems(1203, -1);
				if(st.getPlayer().getClassId().getLevel() == 1)
				{
					st.giveItems(1204, 1);
					if(!st.getPlayer().getVarB("prof1"))
					{
						st.getPlayer().setVar("prof1", "1", -1);
						st.addExpAndSp(3200, 2280);
					}
				}
				st.exitCurrentQuest(true);
				st.playSound("ItemSound.quest_finish");
				htmltext = "master_sorius_q0406_10.htm";
			}
		}
		else if(npcId == 30317)
		{
			if(cond == 3)
			{
				htmltext = "blacksmith_kluto_q0406_01.htm";
			}
			else if(cond == 4)
			{
				htmltext = st.getQuestItemsCount(1206) == 0 ? "blacksmith_kluto_q0406_03.htm" : "blacksmith_kluto_q0406_04.htm";
			}
			else if(cond == 5)
			{
				st.takeItems(1206, -1);
				st.takeItems(1276, -1);
				st.giveItems(1203, 1);
				htmltext = "blacksmith_kluto_q0406_05.htm";
				st.setCond(6);
				st.setState(2);
			}
			else if(cond == 6)
			{
				htmltext = "blacksmith_kluto_q0406_06.htm";
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
		return null;
	}
}