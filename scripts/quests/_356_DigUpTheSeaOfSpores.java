package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _356_DigUpTheSeaOfSpores extends Quest implements ScriptFile
{
	private static final int GAUEN = 30717;
	private static final int SPORE_ZOMBIE = 20562;
	private static final int ROTTING_TREE = 20558;
	private static final int CARNIVORE_SPORE = 5865;
	private static final int HERBIBOROUS_SPORE = 5866;
	
	public _356_DigUpTheSeaOfSpores()
	{
		super(false);
		addStartNpc(30717);
		addKillId(20562);
		addKillId(20558);
		addQuestItem(5865);
		addQuestItem(5866);
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
		long carn = st.getQuestItemsCount(5865);
		long herb = st.getQuestItemsCount(5866);
		if(event.equalsIgnoreCase("magister_gauen_q0356_06.htm"))
		{
			if(st.getPlayer().getLevel() >= 43)
			{
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
			}
			else
			{
				htmltext = "magister_gauen_q0356_01.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if((event.equalsIgnoreCase("magister_gauen_q0356_20.htm") || event.equalsIgnoreCase("magister_gauen_q0356_17.htm")) && carn >= 50 && herb >= 50)
		{
			st.takeItems(5865, -1);
			st.takeItems(5866, -1);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
			if(event.equalsIgnoreCase("magister_gauen_q0356_17.htm"))
			{
				st.giveItems(57, 20950);
			}
			else
			{
				st.addExpAndSp(45500, 2600);
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
			htmltext = "magister_gauen_q0356_02.htm";
		}
		else if(cond != 3)
		{
			htmltext = "magister_gauen_q0356_07.htm";
		}
		else if(cond == 3)
		{
			htmltext = "magister_gauen_q0356_10.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		long carn = st.getQuestItemsCount(5865);
		long herb = st.getQuestItemsCount(5866);
		if(npcId == 20562)
		{
			if(carn < 50)
			{
				st.giveItems(5865, 1);
				if(carn == 49)
				{
					st.playSound("ItemSound.quest_middle");
					if(herb >= 50)
					{
						st.setCond(3);
						st.setState(2);
					}
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 20558 && herb < 50)
		{
			st.giveItems(5866, 1);
			if(herb == 49)
			{
				st.playSound("ItemSound.quest_middle");
				if(carn >= 50)
				{
					st.setCond(3);
					st.setState(2);
				}
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}