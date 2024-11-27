package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _380_BringOutTheFlavorOfIngredients extends Quest implements ScriptFile
{
	private static final int Rollant = 30069;
	private static final int RitronsFruit = 5895;
	private static final int MoonFaceFlower = 5896;
	private static final int LeechFluids = 5897;
	private static final int Antidote = 1831;
	private static final int RitronsDessertRecipe = 5959;
	private static final int RitronJelly = 5960;
	private static final int RitronsDessertRecipeChance = 55;
	private static final int DireWolf = 20205;
	private static final int KadifWerewolf = 20206;
	private static final int GiantMistLeech = 20225;
	private static final int[][] DROPLIST_COND = {{1, 0, 20205, 0, 5895, 4, 10, 1}, {1, 0, 20206, 0, 5896, 20, 50, 1}, {1, 0, 20225, 0, 5897, 10, 50, 1}};
	
	public _380_BringOutTheFlavorOfIngredients()
	{
		super(false);
		addStartNpc(30069);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
			addQuestItem(DROPLIST_COND[i][4]);
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
		if(event.equalsIgnoreCase("rollant_q0380_05.htm"))
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("rollant_q0380_12.htm"))
		{
			st.giveItems(5959, 1);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30069)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 24)
				{
					htmltext = "rollant_q0380_02.htm";
				}
				else
				{
					htmltext = "rollant_q0380_01.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "rollant_q0380_06.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(1831) >= 2)
			{
				st.takeItems(1831, 2);
				st.takeItems(5895, -1);
				st.takeItems(5896, -1);
				st.takeItems(5897, -1);
				htmltext = "rollant_q0380_07.htm";
				st.setCond(3);
				st.setState(2);
			}
			else if(cond == 2)
			{
				htmltext = "rollant_q0380_06.htm";
			}
			else if(cond == 3)
			{
				htmltext = "rollant_q0380_08.htm";
				st.setCond(4);
			}
			else if(cond == 4)
			{
				htmltext = "rollant_q0380_09.htm";
				st.setCond(5);
			}
			if(cond == 5)
			{
				htmltext = "rollant_q0380_10.htm";
				st.setCond(6);
			}
			if(cond == 6)
			{
				st.giveItems(5960, 1);
				if(Rnd.chance(55))
				{
					htmltext = "rollant_q0380_11.htm";
				}
				else
				{
					htmltext = "rollant_q0380_14.htm";
					st.playSound("ItemSound.quest_finish");
					st.exitCurrentQuest(true);
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			if(cond != DROPLIST_COND[i][0] || npcId != DROPLIST_COND[i][2] || DROPLIST_COND[i][3] != 0 && st.getQuestItemsCount(DROPLIST_COND[i][3]) <= 0)
				continue;
			if(DROPLIST_COND[i][5] == 0)
			{
				st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], (double) DROPLIST_COND[i][6]);
				continue;
			}
			if(!st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], DROPLIST_COND[i][7], DROPLIST_COND[i][5], (double) DROPLIST_COND[i][6]) || DROPLIST_COND[i][1] == cond || DROPLIST_COND[i][1] == 0)
				continue;
			st.setCond(Integer.valueOf(DROPLIST_COND[i][1]).intValue());
			st.setState(2);
		}
		if(cond == 1 && st.getQuestItemsCount(5895) >= 4 && st.getQuestItemsCount(5896) >= 20 && st.getQuestItemsCount(5897) >= 10)
		{
			st.setCond(2);
			st.setState(2);
		}
		return null;
	}
}