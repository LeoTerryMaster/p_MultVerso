package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _044_HelpTheSon extends Quest implements ScriptFile
{
	private static final int LUNDY = 30827;
	private static final int DRIKUS = 30505;
	private static final int WORK_HAMMER = 168;
	private static final int GEMSTONE_FRAGMENT = 7552;
	private static final int GEMSTONE = 7553;
	private static final int PET_TICKET = 7585;
	private static final int MAILLE_GUARD = 20921;
	private static final int MAILLE_SCOUT = 20920;
	private static final int MAILLE_LIZARDMAN = 20919;
	
	public _044_HelpTheSon()
	{
		super(false);
		addStartNpc(30827);
		addTalkId(30505);
		addKillId(20921);
		addKillId(20920);
		addKillId(20919);
		addQuestItem(7552);
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
			htmltext = "pet_manager_lundy_q0044_0104.htm";
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("3") && st.getQuestItemsCount(168) > 0)
		{
			htmltext = "pet_manager_lundy_q0044_0201.htm";
			st.takeItems(168, 1);
			st.setCond(2);
		}
		else if(event.equals("4") && st.getQuestItemsCount(7552) >= 30)
		{
			htmltext = "pet_manager_lundy_q0044_0301.htm";
			st.takeItems(7552, -1);
			st.giveItems(7553, 1);
			st.setCond(4);
		}
		else if(event.equals("5") && st.getQuestItemsCount(7553) > 0)
		{
			htmltext = "high_prefect_drikus_q0044_0401.htm";
			st.takeItems(7553, 1);
			st.setCond(5);
		}
		else if(event.equals("7"))
		{
			htmltext = "pet_manager_lundy_q0044_0501.htm";
			st.giveItems(7585, 1);
			st.exitCurrentQuest(false);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int id = st.getState();
		if(id == 1)
		{
			if(st.getPlayer().getLevel() >= 24)
			{
				htmltext = "pet_manager_lundy_q0044_0101.htm";
			}
			else
			{
				st.exitCurrentQuest(true);
				htmltext = "pet_manager_lundy_q0044_0103.htm";
			}
		}
		else if(id == 2)
		{
			int cond = st.getCond();
			if(npcId == 30827)
			{
				if(cond == 1)
				{
					htmltext = st.getQuestItemsCount(168) == 0 ? "pet_manager_lundy_q0044_0106.htm" : "pet_manager_lundy_q0044_0105.htm";
				}
				else if(cond == 2)
				{
					htmltext = "pet_manager_lundy_q0044_0204.htm";
				}
				else if(cond == 3)
				{
					htmltext = "pet_manager_lundy_q0044_0203.htm";
				}
				else if(cond == 4)
				{
					htmltext = "pet_manager_lundy_q0044_0303.htm";
				}
				else if(cond == 5)
				{
					htmltext = "pet_manager_lundy_q0044_0401.htm";
				}
			}
			else if(npcId == 30505)
			{
				if(cond == 4 && st.getQuestItemsCount(7553) > 0)
				{
					htmltext = "high_prefect_drikus_q0044_0301.htm";
				}
				else if(cond == 5)
				{
					htmltext = "high_prefect_drikus_q0044_0403.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 2 && st.getQuestItemsCount(7552) < 30)
		{
			st.giveItems(7552, 1);
			if(st.getQuestItemsCount(7552) >= 30)
			{
				st.playSound("ItemSound.quest_middle");
				st.setCond(3);
				st.playSound("ItemSound.quest_itemget");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}