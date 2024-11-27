package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _292_BrigandsSweep extends Quest implements ScriptFile
{
	private static final int Spiron = 30532;
	private static final int Balanki = 30533;
	private static final int GoblinBrigand = 20322;
	private static final int GoblinBrigandLeader = 20323;
	private static final int GoblinBrigandLieutenant = 20324;
	private static final int GoblinSnooper = 20327;
	private static final int GoblinLord = 20528;
	private static final int GoblinNecklace = 1483;
	private static final int GoblinPendant = 1484;
	private static final int GoblinLordPendant = 1485;
	private static final int SuspiciousMemo = 1486;
	private static final int SuspiciousContract = 1487;
	private static final int Chance = 10;
	private static final int[][] DROPLIST_COND = {{1, 0, GoblinBrigand, 0, GoblinNecklace, 0, 40, 1}, {1, 0, GoblinBrigandLeader, 0, GoblinNecklace, 0, 40, 1}, {1, 0, GoblinSnooper, 0, GoblinNecklace, 0, 40, 1}, {1, 0, GoblinBrigandLieutenant, 0, GoblinPendant, 0, 40, 1}, {1, 0, GoblinLord, 0, GoblinLordPendant, 0, 40, 1}};
	
	public _292_BrigandsSweep()
	{
		super(false);
		addStartNpc(Spiron);
		addTalkId(Balanki);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
		}
		addQuestItem(SuspiciousMemo);
		addQuestItem(SuspiciousContract);
		addQuestItem(GoblinNecklace);
		addQuestItem(GoblinPendant);
		addQuestItem(GoblinLordPendant);
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
		if(event.equalsIgnoreCase("elder_spiron_q0292_03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("elder_spiron_q0292_06.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == Spiron)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.dwarf)
				{
					htmltext = "elder_spiron_q0292_00.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getPlayer().getLevel() < 5)
				{
					htmltext = "elder_spiron_q0292_01.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "elder_spiron_q0292_02.htm";
				}
			}
			else if(cond == 1)
			{
				long reward = st.getQuestItemsCount(GoblinNecklace) * 12 + st.getQuestItemsCount(GoblinPendant) * 36 + st.getQuestItemsCount(GoblinLordPendant) * 33 + st.getQuestItemsCount(SuspiciousContract) * 100;
				if(reward == 0)
				{
					return "elder_spiron_q0292_04.htm";
				}
				htmltext = st.getQuestItemsCount(SuspiciousContract) != 0 ? "elder_spiron_q0292_10.htm" : st.getQuestItemsCount(SuspiciousMemo) == 0 ? "elder_spiron_q0292_05.htm" : st.getQuestItemsCount(SuspiciousMemo) == 1 ? "elder_spiron_q0292_08.htm" : "elder_spiron_q0292_09.htm";
				st.takeItems(GoblinNecklace, -1);
				st.takeItems(GoblinPendant, -1);
				st.takeItems(GoblinLordPendant, -1);
				st.takeItems(SuspiciousContract, -1);
				st.giveItems(57, reward);
			}
		}
		else if(npcId == Balanki && cond == 1)
		{
			if(st.getQuestItemsCount(SuspiciousContract) == 0)
			{
				htmltext = "balanki_q0292_01.htm";
			}
			else
			{
				st.takeItems(SuspiciousContract, -1);
				st.giveItems(57, 120);
				htmltext = "balanki_q0292_02.htm";
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
		if(st.getQuestItemsCount(SuspiciousContract) == 0 && Rnd.chance(Chance))
		{
			if(st.getQuestItemsCount(SuspiciousMemo) < 3)
			{
				st.giveItems(SuspiciousMemo, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else
			{
				st.takeItems(SuspiciousMemo, -1);
				st.giveItems(SuspiciousContract, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		return null;
	}
}