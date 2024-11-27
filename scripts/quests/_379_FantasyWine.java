package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _379_FantasyWine extends Quest implements ScriptFile
{
	public final int HARLAN = 30074;
	public final int Enku_Orc_Champion = 20291;
	public final int Enku_Orc_Shaman = 20292;
	public final int LEAF_OF_EUCALYPTUS = 5893;
	public final int STONE_OF_CHILL = 5894;
	public final int[] REWARD = {5956, 5957, 5958};
	
	public _379_FantasyWine()
	{
		super(false);
		addStartNpc(30074);
		addKillId(20291);
		addKillId(20292);
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
		if(event.equalsIgnoreCase("hitsran_q0379_06.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("reward"))
		{
			st.takeItems(5893, -1);
			st.takeItems(5894, -1);
			int rand = Rnd.get(100);
			if(rand < 25)
			{
				st.giveItems(REWARD[0], 1);
				htmltext = "hitsran_q0379_11.htm";
			}
			else if(rand < 50)
			{
				st.giveItems(REWARD[1], 1);
				htmltext = "hitsran_q0379_12.htm";
			}
			else
			{
				st.giveItems(REWARD[2], 1);
				htmltext = "hitsran_q0379_13.htm";
			}
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		else if(event.equalsIgnoreCase("hitsran_q0379_05.htm"))
		{
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = 0;
		if(id != 1)
		{
			cond = st.getCond();
		}
		String htmltext = "noquest";
		if(npcId == 30074)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 20)
				{
					htmltext = "hitsran_q0379_01.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "hitsran_q0379_02.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = st.getQuestItemsCount(5893) < 80 && st.getQuestItemsCount(5894) < 100 ? "hitsran_q0379_07.htm" : st.getQuestItemsCount(5893) == 80 && st.getQuestItemsCount(5894) < 100 ? "hitsran_q0379_08.htm" : st.getQuestItemsCount(5893) < 80 && st.getQuestItemsCount(5894) == 100 ? "hitsran_q0379_09.htm" : "hitsran_q0379_02.htm";
			}
			else if(cond == 2)
			{
				if(st.getQuestItemsCount(5893) >= 80 && st.getQuestItemsCount(5894) >= 100)
				{
					htmltext = "hitsran_q0379_10.htm";
				}
				else
				{
					st.setCond(1);
					if(st.getQuestItemsCount(5893) < 80 && st.getQuestItemsCount(5894) < 100)
					{
						htmltext = "hitsran_q0379_07.htm";
					}
					else if(st.getQuestItemsCount(5893) >= 80 && st.getQuestItemsCount(5894) < 100)
					{
						htmltext = "hitsran_q0379_08.htm";
					}
					else if(st.getQuestItemsCount(5893) < 80 && st.getQuestItemsCount(5894) >= 100)
					{
						htmltext = "hitsran_q0379_09.htm";
					}
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(st.getCond() == 1)
		{
			if(npcId == 20291 && st.getQuestItemsCount(5893) < 80)
			{
				st.giveItems(5893, 1);
			}
			else if(npcId == 20292 && st.getQuestItemsCount(5894) < 100)
			{
				st.giveItems(5894, 1);
			}
			if(st.getQuestItemsCount(5893) >= 80 && st.getQuestItemsCount(5894) >= 100)
			{
				st.playSound("ItemSound.quest_middle");
				st.setCond(2);
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}