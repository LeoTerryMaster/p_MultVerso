package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _294_CovertBusiness extends Quest implements ScriptFile
{
	public static int BatFang = 1491;
	public static int RingOfRaccoon = 1508;
	public static int BarbedBat = 20370;
	public static int BladeBat = 20480;
	public static int Keef = 30534;
	
	public _294_CovertBusiness()
	{
		super(false);
		addStartNpc(Keef);
		addTalkId(Keef);
		addKillId(BarbedBat);
		addKillId(BladeBat);
		addQuestItem(BatFang);
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
		if(event.equalsIgnoreCase("elder_keef_q0294_03.htm"))
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
		String htmltext;
		int id = st.getState();
		if(id == 1)
		{
			if(st.getPlayer().getRace() != Race.dwarf)
			{
				htmltext = "elder_keef_q0294_00.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				if(st.getPlayer().getLevel() >= 10)
				{
					htmltext = "elder_keef_q0294_02.htm";
					return htmltext;
				}
				htmltext = "elder_keef_q0294_01.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(st.getQuestItemsCount(BatFang) < 100)
		{
			htmltext = "elder_keef_q0294_04.htm";
		}
		else
		{
			if(st.getQuestItemsCount(RingOfRaccoon) < 1)
			{
				st.giveItems(RingOfRaccoon, 1);
				htmltext = "elder_keef_q0294_05.htm";
			}
			else
			{
				st.giveItems(57, 2400);
				htmltext = "elder_keef_q0294_06.htm";
			}
			st.addExpAndSp(0, 600);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			st.rollAndGive(BatFang, 1, 2, 100, 100.0);
		}
		return null;
	}
}