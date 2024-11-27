package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _377_GiantsExploration2 extends Quest implements ScriptFile
{
	private static final int DROP_RATE = 20;
	private static final int ANC_BOOK = 5955;
	private static final int DICT2 = 5892;
	private static final int[][] EXCHANGE = {{5945, 5946, 5947, 5948, 5949}, {5950, 5951, 5952, 5953, 5954}};
	private static final int HR_SOBLING = 31147;
	private static final int[] MOBS = {20654, 20656, 20657, 20658};
	
	public _377_GiantsExploration2()
	{
		super(true);
		addStartNpc(31147);
		addKillId(MOBS);
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
		if(event.equalsIgnoreCase("yes"))
		{
			htmltext = "Starting.htm";
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("0"))
		{
			htmltext = "ext_msg.htm";
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		else if(event.equalsIgnoreCase("show"))
		{
			htmltext = "no_items.htm";
			for(int[] i : EXCHANGE)
			{
				long count = Long.MAX_VALUE;
				for(int j : i)
				{
					count = Math.min(count, st.getQuestItemsCount(j));
				}
				if(count <= 0)
					continue;
				htmltext = "tnx4items.htm";
				for(int j : i)
				{
					st.takeItems(j, count);
				}
				int n = 0;
				while((long) n < count)
				{
					int luck = Rnd.get(100);
					int item = luck > 75 ? 5420 : luck > 50 ? 5422 : luck > 25 ? 5336 : 5338;
					st.giveItems(item, 1);
					++n;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int id = st.getState();
		if(st.getQuestItemsCount(5892) == 0)
		{
			st.exitCurrentQuest(true);
		}
		else if(id == 1)
		{
			htmltext = "start.htm";
			if(st.getPlayer().getLevel() < 56)
			{
				st.exitCurrentQuest(true);
				htmltext = "error_1.htm";
			}
		}
		else if(id == 2)
		{
			htmltext = st.getQuestItemsCount(5955) != 0 ? "checkout.htm" : "checkout2.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			st.rollAndGive(5955, 1, 20.0);
		}
		return null;
	}
}