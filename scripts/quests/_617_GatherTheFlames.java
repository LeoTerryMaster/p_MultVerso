package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _617_GatherTheFlames extends Quest implements ScriptFile
{
	public static final int[] Recipes = {6881, 6883, 6885, 6887, 7580, 6891, 6893, 6895, 6897, 6899};
	private static final int VULCAN = 31539;
	private static final int HILDA = 31271;
	private static final int TORCH = 7264;
	private static final int[][] DROPLIST = {{21376, 48}, {21377, 48}, {21378, 48}, {21652, 48}, {21380, 48}, {21381, 51}, {21653, 51}, {21383, 51}, {21394, 51}, {21385, 51}, {21386, 51}, {21388, 53}, {21655, 53}, {21387, 53}, {21390, 56}, {21656, 56}, {21395, 56}, {21389, 56}, {21391, 56}, {21392, 56}, {21393, 58}, {21657, 58}, {21382, 60}, {21379, 60}, {21654, 64}, {21384, 64}};
	
	public _617_GatherTheFlames()
	{
		super(true);
		addStartNpc(31539);
		addStartNpc(31271);
		for(int[] element : DROPLIST)
		{
			addKillId(element[0]);
		}
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
		if(event.equalsIgnoreCase("warsmith_vulcan_q0617_03.htm"))
		{
			if(st.getPlayer().getLevel() < 74)
			{
				return "warsmith_vulcan_q0617_02.htm";
			}
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("blacksmith_hilda_q0617_03.htm"))
		{
			if(st.getPlayer().getLevel() < 74)
			{
				return "blacksmith_hilda_q0617_02.htm";
			}
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("warsmith_vulcan_q0617_08.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.takeItems(7264, -1);
			st.exitCurrentQuest(true);
		}
		else if(event.equalsIgnoreCase("warsmith_vulcan_q0617_07.htm"))
		{
			if(st.getQuestItemsCount(7264) < 1000)
			{
				return "warsmith_vulcan_q0617_05.htm";
			}
			st.takeItems(7264, 1000);
			st.giveItems(Recipes[Rnd.get(Recipes.length)], 1);
			st.playSound("ItemSound.quest_middle");
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 31539)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 74)
				{
					htmltext = "warsmith_vulcan_q0617_02.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "warsmith_vulcan_q0617_01.htm";
				}
			}
			else
			{
				htmltext = st.getQuestItemsCount(7264) < 1000 ? "warsmith_vulcan_q0617_05.htm" : "warsmith_vulcan_q0617_04.htm";
			}
		}
		else if(npcId == 31271)
		{
			htmltext = cond < 1 ? st.getPlayer().getLevel() < 74 ? "blacksmith_hilda_q0617_02.htm" : "blacksmith_hilda_q0617_01.htm" : "blacksmith_hilda_q0617_04.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		for(int[] element : DROPLIST)
		{
			if(npc.getNpcId() != element[0])
				continue;
			st.rollAndGive(7264, 1, (double) element[1]);
			return null;
		}
		return null;
	}
}