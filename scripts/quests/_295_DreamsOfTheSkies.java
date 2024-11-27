package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _295_DreamsOfTheSkies extends Quest implements ScriptFile
{
	public static int FLOATING_STONE = 1492;
	public static int RING_OF_FIREFLY = 1509;
	public static int Arin = 30536;
	public static int MagicalWeaver = 20153;
	
	public _295_DreamsOfTheSkies()
	{
		super(false);
		addStartNpc(Arin);
		addTalkId(Arin);
		addKillId(MagicalWeaver);
		addQuestItem(FLOATING_STONE);
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
		if(event.equalsIgnoreCase("elder_arin_q0295_03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int id = st.getState();
		if(id == 1)
		{
			st.setCond(0);
		}
		String htmltext = "noquest";
		int cond;
		if((cond = st.getCond()) == 0)
		{
			if(st.getPlayer().getLevel() >= 11)
			{
				htmltext = "elder_arin_q0295_02.htm";
				return htmltext;
			}
			htmltext = "elder_arin_q0295_01.htm";
			st.exitCurrentQuest(true);
		}
		else if(cond == 1 || st.getQuestItemsCount(FLOATING_STONE) < 50)
		{
			htmltext = "elder_arin_q0295_04.htm";
		}
		else if(cond == 2 && st.getQuestItemsCount(FLOATING_STONE) == 50)
		{
			st.addExpAndSp(0, 500);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
			if(st.getQuestItemsCount(RING_OF_FIREFLY) < 1)
			{
				htmltext = "elder_arin_q0295_05.htm";
				st.giveItems(RING_OF_FIREFLY, 1);
			}
			else
			{
				htmltext = "elder_arin_q0295_06.htm";
				st.giveItems(57, 2400);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1 && st.getQuestItemsCount(FLOATING_STONE) < 50)
		{
			if(Rnd.chance(25))
			{
				st.giveItems(FLOATING_STONE, 1);
				if(st.getQuestItemsCount(FLOATING_STONE) == 50)
				{
					st.playSound("ItemSound.quest_middle");
					st.setCond(2);
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(st.getQuestItemsCount(FLOATING_STONE) >= 48)
			{
				st.giveItems(FLOATING_STONE, 50 - st.getQuestItemsCount(FLOATING_STONE));
				st.playSound("ItemSound.quest_middle");
				st.setCond(2);
			}
			else
			{
				st.giveItems(FLOATING_STONE, 2);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}