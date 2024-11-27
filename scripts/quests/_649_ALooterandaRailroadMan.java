package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _649_ALooterandaRailroadMan extends Quest implements ScriptFile
{
	private static final int OBI = 32052;
	private static final int THIEF_GUILD_MARK = 8099;
	private static final int[][] DROPLIST_COND = {{1, 2, 22017, 0, 8099, 200, 50, 1}, {1, 2, 22018, 0, 8099, 200, 50, 1}, {1, 2, 22019, 0, 8099, 200, 50, 1}, {1, 2, 22021, 0, 8099, 200, 50, 1}, {1, 2, 22022, 0, 8099, 200, 50, 1}, {1, 2, 22023, 0, 8099, 200, 50, 1}, {1, 2, 22024, 0, 8099, 200, 50, 1}, {1, 2, 22026, 0, 8099, 200, 50, 1}};
	
	public _649_ALooterandaRailroadMan()
	{
		super(true);
		addStartNpc(32052);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
		}
		addQuestItem(8099);
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
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "railman_obi_q0649_0103.htm";
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("649_3"))
		{
			if(st.getQuestItemsCount(8099) == 200)
			{
				htmltext = "railman_obi_q0649_0201.htm";
				st.takeItems(8099, -1);
				st.giveItems(57, 21698, true);
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
			else
			{
				st.setCond(1);
				htmltext = "railman_obi_q0649_0202.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = 0;
		if(id != 1)
		{
			cond = st.getCond();
		}
		String htmltext = "noquest";
		if(npcId == 32052)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 30)
				{
					htmltext = "railman_obi_q0649_0102.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "railman_obi_q0649_0101.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "railman_obi_q0649_0106.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(8099) == 200)
			{
				htmltext = "railman_obi_q0649_0105.htm";
			}
			else
			{
				htmltext = "railman_obi_q0649_0106.htm";
				st.setCond(1);
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