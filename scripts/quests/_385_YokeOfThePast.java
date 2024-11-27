package quests;

import l2.gameserver.model.base.Experience;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _385_YokeOfThePast extends Quest implements ScriptFile
{
	final int ANCIENT_SCROLL = 5902;
	final int BLANK_SCROLL = 5965;
	
	public _385_YokeOfThePast()
	{
		super(true);
		for(int npcId = 31095;npcId <= 31126;++npcId)
		{
			if(npcId == 31111 || npcId == 31112 || npcId == 31113)
				continue;
			addStartNpc(npcId);
		}
		int mobs = 21208;
		while(mobs < 21256)
		{
			addKillId(mobs++);
		}
		addQuestItem(5902);
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
	
	public boolean checkNPC(int npc)
	{
		return npc >= 31095 && npc <= 31126 && npc != 31100 && npc != 31111 && npc != 31112 && npc != 31113;
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("enter_necropolis1_q0385_05.htm"))
		{
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("enter_necropolis1_q0385_09.htm"))
		{
			htmltext = "enter_necropolis1_q0385_10.htm";
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		double rand = 60.0 * Experience.penaltyModifier((long) st.calculateLevelDiffForDrop(npc.getLevel(), st.getPlayer().getLevel()), 9.0) * npc.getTemplate().rateHp / 4.0;
		st.rollAndGive(5902, 1, rand);
		return null;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		if(checkNPC(npcId) && st.getCond() == 0)
		{
			if(st.getPlayer().getLevel() < 20)
			{
				htmltext = "enter_necropolis1_q0385_02.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "enter_necropolis1_q0385_01.htm";
			}
		}
		else if(st.getCond() == 1 && st.getQuestItemsCount(5902) == 0)
		{
			htmltext = "enter_necropolis1_q0385_11.htm";
		}
		else if(st.getCond() == 1 && st.getQuestItemsCount(5902) > 0)
		{
			htmltext = "enter_necropolis1_q0385_09.htm";
			st.giveItems(5965, st.getQuestItemsCount(5902));
			st.takeItems(5902, -1);
		}
		else
		{
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
}