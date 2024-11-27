package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _159_ProtectHeadsprings extends Quest implements ScriptFile
{
	int PLAGUE_DUST_ID = 1035;
	int HYACINTH_CHARM1_ID = 1071;
	int HYACINTH_CHARM2_ID = 1072;
	
	public _159_ProtectHeadsprings()
	{
		super(false);
		addStartNpc(30154);
		addKillId(27017);
		addQuestItem(PLAGUE_DUST_ID, HYACINTH_CHARM1_ID, HYACINTH_CHARM2_ID);
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
			if(st.getQuestItemsCount(HYACINTH_CHARM1_ID) == 0)
			{
				st.giveItems(HYACINTH_CHARM1_ID, 1);
				htmltext = "30154-04.htm";
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
			if(st.getPlayer().getRace() != Race.elf)
			{
				htmltext = "30154-00.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				if(st.getPlayer().getLevel() >= 12)
				{
					htmltext = "30154-03.htm";
					return htmltext;
				}
				htmltext = "30154-02.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(cond == 1)
		{
			htmltext = "30154-05.htm";
		}
		else if(cond == 2)
		{
			st.takeItems(PLAGUE_DUST_ID, -1);
			st.takeItems(HYACINTH_CHARM1_ID, -1);
			st.giveItems(HYACINTH_CHARM2_ID, 1);
			st.setCond(3);
			htmltext = "30154-06.htm";
		}
		else if(cond == 3)
		{
			htmltext = "30154-07.htm";
		}
		else if(cond == 4)
		{
			st.takeItems(PLAGUE_DUST_ID, -1);
			st.takeItems(HYACINTH_CHARM2_ID, -1);
			st.giveItems(57, 18250);
			st.playSound("ItemSound.quest_finish");
			htmltext = "30154-08.htm";
			st.exitCurrentQuest(false);
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1 && Rnd.chance(60))
		{
			st.giveItems(PLAGUE_DUST_ID, 1);
			st.setCond(2);
			st.playSound("ItemSound.quest_middle");
		}
		else if(cond == 3 && Rnd.chance(60))
		{
			if(st.getQuestItemsCount(PLAGUE_DUST_ID) == 4)
			{
				st.giveItems(PLAGUE_DUST_ID, 1);
				st.setCond(4);
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.giveItems(PLAGUE_DUST_ID, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}