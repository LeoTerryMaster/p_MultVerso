package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _370_AnElderSowsSeeds extends Quest implements ScriptFile
{
	private static final int CASIAN = 30612;
	private static final int[] MOBS = {20082, 20084, 20086, 20089, 20090};
	private static final int SPB_PAGE = 5916;
	private static final int TELEPORT_SCROLL = 736;
	private static final int[] CHAPTERS = {5917, 5918, 5919, 5920};
	
	public _370_AnElderSowsSeeds()
	{
		super(false);
		addStartNpc(CASIAN);
		for(int npcId : MOBS)
		{
			addKillId(npcId);
		}
		addQuestItem(SPB_PAGE);
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
		if(event.equalsIgnoreCase("30612-1.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30612-6.htm"))
		{
			if(st.getQuestItemsCount(CHAPTERS[0]) > 0 && st.getQuestItemsCount(CHAPTERS[1]) > 0 && st.getQuestItemsCount(CHAPTERS[2]) > 0 && st.getQuestItemsCount(CHAPTERS[3]) > 0)
			{
				long mincount = st.getQuestItemsCount(CHAPTERS[0]);
				for(int itemId : CHAPTERS)
				{
					mincount = Math.min(mincount, st.getQuestItemsCount(itemId));
				}
				for(int itemId : CHAPTERS)
				{
					st.takeItems(itemId, mincount);
				}
				st.giveItems(57, 3600 * mincount);
				htmltext = "30612-8.htm";
			}
			else
			{
				htmltext = "30612-4.htm";
			}
		}
		else if(event.equalsIgnoreCase("30612-9.htm"))
		{
			st.giveItems(TELEPORT_SCROLL, 1);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(st.getState() == 1)
		{
			if(st.getPlayer().getLevel() < 28)
			{
				htmltext = "30612-0a.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "30612-0.htm";
			}
		}
		else if(cond == 1)
		{
			htmltext = "30612-4.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() != 2)
		{
			return null;
		}
		if(Rnd.chance(Math.min((int) (15.0 * st.getRateQuestsReward()), 100)))
		{
			st.giveItems(SPB_PAGE, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}