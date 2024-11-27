package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _645_GhostsOfBatur extends Quest implements ScriptFile
{
	private static final int Karuda = 32017;
	private static final int CursedGraveGoods = 8089;
	private static final int[][] REWARDS = {{1878, 18}, {1879, 7}, {1880, 4}, {1881, 6}, {1882, 10}, {1883, 2}};
	private static final int[] MOBS = {22007, 22009, 22010, 22011, 22012, 22013, 22014, 22015, 22016};
	
	public _645_GhostsOfBatur()
	{
		super(false);
		addStartNpc(32017);
		addTalkId(32017);
		for(int i : MOBS)
		{
			addKillId(i);
		}
		addQuestItem(8089);
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
		if(event.equalsIgnoreCase("32017-03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(st.getCond() == 2)
		{
			if(st.getQuestItemsCount(8089) >= 180)
			{
				for(int i = 0;i < REWARDS.length;++i)
				{
					if(!event.equalsIgnoreCase(String.valueOf(REWARDS[i][0])))
						continue;
					st.takeItems(8089, -1);
					st.giveItems(REWARDS[i][0], (long) REWARDS[i][1], true);
					htmltext = "32017-07.htm";
					st.playSound("ItemSound.quest_finish");
					st.exitCurrentQuest(true);
				}
			}
			else
			{
				htmltext = "32017-04.htm";
				st.setCond(1);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() < 21 || st.getPlayer().getLevel() > 32)
			{
				htmltext = "32017-02.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "32017-01.htm";
			}
		}
		else if(cond == 1)
		{
			htmltext = "32017-04.htm";
		}
		else if(cond == 2)
		{
			htmltext = st.getQuestItemsCount(8089) >= 180 ? "32017-05.htm" : "32017-01.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1 && st.getQuestItemsCount(8089) < 180 && st.rollAndGive(8089, 1, 2, 180, 70.0))
		{
			st.setCond(2);
			st.setState(2);
		}
		return null;
	}
}