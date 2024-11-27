package quests;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _688_DefeatTheElrokianRaiders extends Quest implements ScriptFile
{
	private static final int DROP_CHANCE = 50;
	private static final int DINOSAUR_FANG_NECKLACE = 8785;
	
	public _688_DefeatTheElrokianRaiders()
	{
		super(false);
		addStartNpc(32105);
		addTalkId(32105);
		addKillId(22214);
		addQuestItem(DINOSAUR_FANG_NECKLACE);
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
		long count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
		if(event.equalsIgnoreCase("32105-03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("32105-08.htm"))
		{
			if(count > 0)
			{
				st.takeItems(DINOSAUR_FANG_NECKLACE, -1);
				st.giveItems(57, count * 3000);
			}
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		else if(event.equalsIgnoreCase("32105-06.htm"))
		{
			st.takeItems(DINOSAUR_FANG_NECKLACE, -1);
			st.giveItems(57, count * 3000);
		}
		else if(event.equalsIgnoreCase("32105-07.htm"))
		{
			if(count >= 100)
			{
				st.takeItems(DINOSAUR_FANG_NECKLACE, 100);
				st.giveItems(57, 450000);
			}
			else
			{
				htmltext = "32105-04.htm";
			}
		}
		else if(event.equalsIgnoreCase("None"))
		{
			htmltext = null;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		long count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 75)
			{
				htmltext = "32105-01.htm";
			}
			else
			{
				htmltext = "32105-00.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(cond == 1)
		{
			htmltext = count == 0 ? "32105-04.htm" : "32105-05.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		long count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
		if(st.getCond() == 1 && count < 100 && Rnd.chance(DROP_CHANCE))
		{
			long numItems = (int) Config.RATE_QUESTS_REWARD;
			if(count + numItems > 100)
			{
				numItems = 100 - count;
			}
			if(count + numItems >= 100)
			{
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
			st.giveItems(DINOSAUR_FANG_NECKLACE, numItems);
		}
		return null;
	}
}