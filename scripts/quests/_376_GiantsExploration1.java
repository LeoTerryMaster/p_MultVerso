package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _376_GiantsExploration1 extends Quest implements ScriptFile
{
	private static final int DROP_RATE = 20;
	private static final int DROP_RATE_BOOK = 15;
	private static final int ANCIENT_PARCHMENT = 5944;
	private static final int DICT1 = 5891;
	private static final int DICT2 = 5892;
	private static final int MST_BK = 5890;
	private static final int[][] EXCHANGE = {{5937, 5938, 5939, 5940, 5941}, {5346, 5354}, {5932, 5933, 5934, 5935, 5936}, {5332, 5334}, {5922, 5923, 5924, 5925, 5926}, {5416, 5418}, {5927, 5928, 5929, 5930, 5931}, {5424, 5340}};
	private static final int HR_SOBLING = 31147;
	private static final int WF_CLIFF = 30182;
	private static final int[] MOBS = {20647, 20648, 20649, 20650};
	
	public _376_GiantsExploration1()
	{
		super(true);
		addStartNpc(31147);
		addTalkId(30182);
		addKillId(MOBS);
		addQuestItem(5891, 5890);
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
		int cond = st.getCond();
		if(event.equalsIgnoreCase("yes"))
		{
			htmltext = "Starting.htm";
			st.setState(2);
			st.setCond(1);
			st.giveItems(5891, 1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("no"))
		{
			htmltext = "ext_msg.htm";
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		else if(event.equalsIgnoreCase("show"))
		{
			htmltext = "no_items.htm";
			for(int i = 0;i < EXCHANGE.length;i += 2)
			{
				long count = Long.MAX_VALUE;
				for(int j : EXCHANGE[i])
				{
					count = Math.min(count, st.getQuestItemsCount(j));
				}
				if(count < 1)
					continue;
				htmltext = "tnx4items.htm";
				for(int j : EXCHANGE[i])
				{
					st.takeItems(j, count);
				}
				int l = 0;
				while((long) l < count)
				{
					int item = EXCHANGE[i + 1][Rnd.get(EXCHANGE[i + 1].length)];
					st.giveItems(item, 1);
					++l;
				}
			}
		}
		else if(event.equalsIgnoreCase("myst"))
		{
			if(st.getQuestItemsCount(5890) > 0)
			{
				if(cond == 1)
				{
					st.setState(2);
					st.setCond(2);
					htmltext = "go_part2.htm";
				}
				else if(cond == 2)
				{
					htmltext = "gogogo_2.htm";
				}
			}
			else
			{
				htmltext = "no_part2.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getCond();
		if(npcId == 31147)
		{
			if(id == 1)
			{
				if(st.getPlayer().getLevel() < 51)
				{
					st.exitCurrentQuest(true);
					htmltext = "error_1.htm";
				}
				else
				{
					htmltext = "start.htm";
				}
			}
			else if(id == 2)
			{
				htmltext = st.getQuestItemsCount(5944) != 0 ? "checkout2.htm" : "checkout.htm";
			}
		}
		else if(npcId == 30182 && cond == 2 & st.getQuestItemsCount(5890) > 0)
		{
			htmltext = "ok_part2.htm";
			st.takeItems(5890, -1);
			st.giveItems(5892, 1);
			st.setCond(3);
			st.playSound("ItemSound.quest_middle");
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond > 0)
		{
			st.rollAndGive(5944, 1, 1, 20.0);
			if(cond == 1)
			{
				st.rollAndGive(5890, 1, 1, 1, 15.0);
			}
		}
		return null;
	}
}