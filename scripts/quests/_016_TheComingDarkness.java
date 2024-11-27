package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _016_TheComingDarkness extends Quest implements ScriptFile
{
	public final int HIERARCH = 31517;
	public final int[][] ALTAR_LIST = {{31512, 1}, {31513, 2}, {31514, 3}, {31515, 4}, {31516, 5}};
	public final int CRYSTAL_OF_SEAL = 7167;
	
	public _016_TheComingDarkness()
	{
		super(false);
		addStartNpc(31517);
		for(int[] element : ALTAR_LIST)
		{
			addTalkId(element[0]);
		}
		addQuestItem(7167);
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
		if(event.equalsIgnoreCase("31517-02.htm"))
		{
			st.setState(2);
			st.setCond(1);
			st.giveItems(7167, 5);
			st.playSound("ItemSound.quest_accept");
		}
		for(int[] element : ALTAR_LIST)
		{
			if(!event.equalsIgnoreCase(element[0] + "-02.htm"))
				continue;
			st.takeItems(7167, 1);
			st.setCond(Integer.valueOf(element[1] + 1).intValue());
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
		if(npcId == 31517)
		{
			if(cond < 1)
			{
				if(st.getPlayer().getLevel() < 61)
				{
					htmltext = "31517-00.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "31517-01.htm";
				}
			}
			else if(cond > 0 && cond < 6 && st.getQuestItemsCount(7167) > 0)
			{
				htmltext = "31517-02r.htm";
			}
			else if(cond > 0 && cond < 6 && st.getQuestItemsCount(7167) < 1)
			{
				htmltext = "31517-proeb.htm";
				st.exitCurrentQuest(false);
			}
			else if(cond > 5 && st.getQuestItemsCount(7167) < 1)
			{
				htmltext = "31517-03.htm";
				st.addExpAndSp(221958, 0);
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(false);
			}
		}
		for(int[] element : ALTAR_LIST)
		{
			if(npcId != element[0])
				continue;
			if(cond == element[1])
			{
				if(st.getQuestItemsCount(7167) > 0)
				{
					htmltext = element[0] + "-01.htm";
					continue;
				}
				htmltext = element[0] + "-03.htm";
				continue;
			}
			if(cond != element[1] + 1)
				continue;
			htmltext = element[0] + "-04.htm";
		}
		return htmltext;
	}
}