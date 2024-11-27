package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _365_DevilsLegacy extends Quest implements ScriptFile
{
	private static final int RANDOLF = 30095;
	private static final int CHANCE_OF_DROP = 25;
	private static final int REWARD_PER_ONE = 5070;
	private static final int TREASURE_CHEST = 5873;
	int[] MOBS = {20836, 29027, 20845, 21629, 21630, 29026};
	
	public _365_DevilsLegacy()
	{
		super(false);
		addStartNpc(30095);
		addKillId(MOBS);
		addQuestItem(5873);
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
		if(event.equalsIgnoreCase("30095-1.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30095-5.htm"))
		{
			long count = st.getQuestItemsCount(5873);
			if(count > 0)
			{
				st.takeItems(5873, -1);
				long reward = count * 5070;
				st.giveItems(57, reward);
			}
			else
			{
				htmltext = "You don't have required items";
			}
		}
		else if(event.equalsIgnoreCase("30095-6.htm"))
		{
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
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 39)
			{
				htmltext = "30095-0.htm";
			}
			else
			{
				htmltext = "30095-0a.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(cond == 1)
		{
			htmltext = st.getQuestItemsCount(5873) == 0 ? "30095-2.htm" : "30095-4.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(Rnd.chance(25))
		{
			st.giveItems(5873, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}