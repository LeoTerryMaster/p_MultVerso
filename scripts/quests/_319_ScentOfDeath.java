package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _319_ScentOfDeath extends Quest implements ScriptFile
{
	private static final int MINALESS = 30138;
	private static final int HealingPotion = 1060;
	private static final int ZombieSkin = 1045;
	private static final int[][] DROPLIST_COND = {{1, 2, 20015, 0, 1045, 5, 20, 1}, {1, 2, 20020, 0, 1045, 5, 25, 1}};
	
	public _319_ScentOfDeath()
	{
		super(false);
		addStartNpc(30138);
		addTalkId(30138);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
		}
		addQuestItem(1045);
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
		if(event.equalsIgnoreCase("mina_q0319_04.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		String htmltext = event;
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
		if(npcId == 30138)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 11)
				{
					htmltext = "mina_q0319_02.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "mina_q0319_03.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "mina_q0319_05.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(1045) >= 5)
			{
				htmltext = "mina_q0319_06.htm";
				st.takeItems(1045, -1);
				st.giveItems(57, 3350);
				st.giveItems(1060, 1);
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "mina_q0319_05.htm";
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