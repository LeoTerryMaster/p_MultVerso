package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _272_WrathOfAncestors extends Quest implements ScriptFile
{
	private static final int Livina = 30572;
	private static final int GraveRobbersHead = 1474;
	private static final int GoblinGraveRobber = 20319;
	private static final int GoblinTombRaiderLeader = 20320;
	private static final int[][] DROPLIST_COND = {{1, 2, 20319, 0, 1474, 50, 100, 1}, {1, 2, 20320, 0, 1474, 50, 100, 1}};
	
	public _272_WrathOfAncestors()
	{
		super(false);
		addStartNpc(30572);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
		}
		addQuestItem(1474);
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
		if(event.equals("1"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "seer_livina_q0272_03.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId != 30572)
			return htmltext;
		if(cond == 0)
		{
			if(st.getPlayer().getRace() != Race.orc)
			{
				htmltext = "seer_livina_q0272_00.htm";
				st.exitCurrentQuest(true);
				return htmltext;
			}
			else
			{
				if(st.getPlayer().getLevel() >= 5)
					return "seer_livina_q0272_02.htm";
				htmltext = "seer_livina_q0272_01.htm";
				st.exitCurrentQuest(true);
			}
			return htmltext;
		}
		else
		{
			if(cond == 1)
			{
				return "seer_livina_q0272_04.htm";
			}
			if(cond != 2)
				return htmltext;
			st.takeItems(1474, -1);
			st.giveItems(57, 1500);
			htmltext = "seer_livina_q0272_05.htm";
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
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