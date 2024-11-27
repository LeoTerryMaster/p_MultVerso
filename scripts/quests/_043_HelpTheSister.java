package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _043_HelpTheSister extends Quest implements ScriptFile
{
	private static final int COOPER = 30829;
	private static final int GALLADUCCI = 30097;
	private static final int CRAFTED_DAGGER = 220;
	private static final int MAP_PIECE = 7550;
	private static final int MAP = 7551;
	private static final int PET_TICKET = 7584;
	private static final int SPECTER = 20171;
	private static final int SORROW_MAIDEN = 20197;
	private static final int MAX_COUNT = 30;
	
	public _043_HelpTheSister()
	{
		super(false);
		addStartNpc(30829);
		addTalkId(30097);
		addKillId(20171);
		addKillId(20197);
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
			htmltext = "pet_manager_cooper_q0043_0104.htm";
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("3") && st.getQuestItemsCount(220) > 0)
		{
			htmltext = "pet_manager_cooper_q0043_0201.htm";
			st.takeItems(220, 1);
			st.setCond(2);
		}
		else if(event.equals("4") && st.getQuestItemsCount(7550) >= 30)
		{
			htmltext = "pet_manager_cooper_q0043_0301.htm";
			st.takeItems(7550, 30);
			st.giveItems(7551, 1);
			st.setCond(4);
		}
		else if(event.equals("5") && st.getQuestItemsCount(7551) > 0)
		{
			htmltext = "galladuchi_q0043_0401.htm";
			st.takeItems(7551, 1);
			st.setCond(5);
		}
		else if(event.equals("7"))
		{
			htmltext = "pet_manager_cooper_q0043_0501.htm";
			st.giveItems(7584, 1);
			st.setCond(0);
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
			if(st.getPlayer().getLevel() >= 26)
			{
				htmltext = "pet_manager_cooper_q0043_0101.htm";
			}
			else
			{
				st.exitCurrentQuest(true);
				htmltext = "pet_manager_cooper_q0043_0103.htm";
			}
		}
		else if(id == 2)
		{
			int cond = st.getCond();
			if(npcId == 30829)
			{
				if(cond == 1)
				{
					htmltext = st.getQuestItemsCount(220) == 0 ? "pet_manager_cooper_q0043_0106.htm" : "pet_manager_cooper_q0043_0105.htm";
				}
				else if(cond == 2)
				{
					htmltext = "pet_manager_cooper_q0043_0204.htm";
				}
				else if(cond == 3)
				{
					htmltext = "pet_manager_cooper_q0043_0203.htm";
				}
				else if(cond == 4)
				{
					htmltext = "pet_manager_cooper_q0043_0303.htm";
				}
				else if(cond == 5)
				{
					htmltext = "pet_manager_cooper_q0043_0401.htm";
				}
			}
			else if(npcId == 30097 && cond == 4 && st.getQuestItemsCount(7551) > 0)
			{
				htmltext = "galladuchi_q0043_0301.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		long pieces;
		int cond = st.getCond();
		if(cond == 2 && (pieces = st.getQuestItemsCount(7550)) < 30)
		{
			st.giveItems(7550, 1);
			if(pieces < 29)
			{
				st.playSound("ItemSound.quest_itemget");
			}
			else
			{
				st.playSound("ItemSound.quest_middle");
				st.setCond(3);
			}
		}
		return null;
	}
}