package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _161_FruitsOfMothertree extends Quest implements ScriptFile
{
	private static final int ANDELLRIAS_LETTER_ID = 1036;
	private static final int MOTHERTREE_FRUIT_ID = 1037;
	
	public _161_FruitsOfMothertree()
	{
		super(false);
		addStartNpc(30362);
		addTalkId(30371);
		addQuestItem(1037, 1036);
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
			st.set("id", "0");
			htmltext = "30362-04.htm";
			st.giveItems(1036, 1);
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int id = st.getState();
		if(id == 1)
		{
			st.setState(2);
			st.setCond(0);
			st.set("id", "0");
		}
		String htmltext = "noquest";
		if(npcId == 30362 && st.getCond() == 0)
		{
			if(st.getCond() < 15)
			{
				if(st.getPlayer().getRace() != Race.elf)
				{
					htmltext = "30362-00.htm";
				}
				else
				{
					if(st.getPlayer().getLevel() >= 3)
					{
						return "30362-03.htm";
					}
					htmltext = "30362-02.htm";
					st.exitCurrentQuest(true);
				}
			}
			else
			{
				htmltext = "30362-02.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(npcId == 30362 && st.getCond() > 0)
		{
			if(st.getQuestItemsCount(1036) == 1 && st.getQuestItemsCount(1037) == 0)
			{
				htmltext = "30362-05.htm";
			}
			else if(st.getQuestItemsCount(1037) == 1)
			{
				htmltext = "30362-06.htm";
				st.giveItems(875, 2);
				st.addExpAndSp(600, 0);
				st.takeItems(1037, 1);
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(false);
			}
		}
		else if(npcId == 30371 && st.getCond() == 1)
		{
			if(st.getQuestItemsCount(1036) == 1)
			{
				if(st.getInt("id") != 161)
				{
					st.set("id", "161");
					htmltext = "30371-01.htm";
					st.giveItems(1037, 1);
					st.takeItems(1036, 1);
				}
			}
			else if(st.getQuestItemsCount(1037) == 1)
			{
				htmltext = "30371-02.htm";
			}
		}
		return htmltext;
	}
}