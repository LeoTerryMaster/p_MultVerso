package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _271_ProofOfValor extends Quest implements ScriptFile
{
	private static final int RUKAIN = 30577;
	private static final int KASHA_WOLF_FANG_ID = 1473;
	private static final int NECKLACE_OF_VALOR_ID = 1507;
	private static final int NECKLACE_OF_COURAGE_ID = 1506;
	private static final int[][] DROPLIST_COND = {{1, 2, 20475, 0, 1473, 50, 25, 2}};
	
	public _271_ProofOfValor()
	{
		super(false);
		addStartNpc(30577);
		addTalkId(30577);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
		}
		addQuestItem(1473);
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
		if(event.equalsIgnoreCase("praetorian_rukain_q0271_03.htm"))
		{
			st.playSound("ItemSound.quest_accept");
			if(st.getQuestItemsCount(1506) > 0 || st.getQuestItemsCount(1507) > 0)
			{
				htmltext = "praetorian_rukain_q0271_07.htm";
			}
			st.setCond(1);
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
		if(npcId == 30577)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.orc)
				{
					htmltext = "praetorian_rukain_q0271_00.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getPlayer().getLevel() < 4)
				{
					htmltext = "praetorian_rukain_q0271_01.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getQuestItemsCount(1506) > 0 || st.getQuestItemsCount(1507) > 0)
				{
					htmltext = "praetorian_rukain_q0271_06.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "praetorian_rukain_q0271_02.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "praetorian_rukain_q0271_04.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(1473) == 50)
			{
				st.takeItems(1473, -1);
				if(Rnd.chance(14))
				{
					st.takeItems(1507, -1);
					st.giveItems(1507, 1);
				}
				else
				{
					st.takeItems(1506, -1);
					st.giveItems(1506, 1);
				}
				htmltext = "praetorian_rukain_q0271_05.htm";
				st.exitCurrentQuest(true);
			}
			else if(cond == 2 && st.getQuestItemsCount(1473) < 50)
			{
				htmltext = "praetorian_rukain_q0271_04.htm";
				st.setCond(1);
				st.setState(2);
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