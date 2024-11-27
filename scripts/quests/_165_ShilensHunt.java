package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _165_ShilensHunt extends Quest implements ScriptFile
{
	private static final int DARK_BEZOAR = 1160;
	private static final int LESSER_HEALING_POTION = 1060;
	
	public _165_ShilensHunt()
	{
		super(false);
		addStartNpc(30348);
		addTalkId(30348);
		addKillId(20456);
		addKillId(20529);
		addKillId(20532);
		addKillId(20536);
		addQuestItem(1160);
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
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "30348-03.htm";
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
			if(st.getPlayer().getRace() != Race.darkelf)
			{
				htmltext = "30348-00.htm";
			}
			else
			{
				if(st.getPlayer().getLevel() >= 3)
				{
					htmltext = "30348-02.htm";
					return htmltext;
				}
				htmltext = "30348-01.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(cond == 1 || st.getQuestItemsCount(1160) < 13)
		{
			htmltext = "30348-04.htm";
		}
		else if(cond == 2)
		{
			htmltext = "30348-05.htm";
			st.takeItems(1160, -1);
			st.giveItems(1060, 5);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1 && st.getQuestItemsCount(1160) < 13 && Rnd.chance(90))
		{
			st.giveItems(1160, 1);
			if(st.getQuestItemsCount(1160) == 13)
			{
				st.setCond(2);
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}