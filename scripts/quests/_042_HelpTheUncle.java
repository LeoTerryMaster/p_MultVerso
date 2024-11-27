package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _042_HelpTheUncle extends Quest implements ScriptFile
{
	private static final int WATERS = 30828;
	private static final int SOPHYA = 30735;
	private static final int TRIDENT = 291;
	private static final int MAP_PIECE = 7548;
	private static final int MAP = 7549;
	private static final int PET_TICKET = 7583;
	private static final int MONSTER_EYE_DESTROYER = 20068;
	private static final int MONSTER_EYE_GAZER = 20266;
	private static final int MAX_COUNT = 30;
	
	public _042_HelpTheUncle()
	{
		super(false);
		addStartNpc(30828);
		addTalkId(30828);
		addTalkId(30735);
		addKillId(20068);
		addKillId(20266);
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
			htmltext = "pet_manager_waters_q0042_0104.htm";
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("3") && st.getQuestItemsCount(291) > 0)
		{
			htmltext = "pet_manager_waters_q0042_0201.htm";
			st.takeItems(291, 1);
			st.setCond(2);
		}
		else if(event.equals("4") && st.getQuestItemsCount(7548) >= 30)
		{
			htmltext = "pet_manager_waters_q0042_0301.htm";
			st.takeItems(7548, 30);
			st.giveItems(7549, 1);
			st.setCond(4);
		}
		else if(event.equals("5") && st.getQuestItemsCount(7549) > 0)
		{
			htmltext = "sophia_q0042_0401.htm";
			st.takeItems(7549, 1);
			st.setCond(5);
		}
		else if(event.equals("7"))
		{
			htmltext = "pet_manager_waters_q0042_0501.htm";
			st.giveItems(7583, 1);
			st.unset("cond");
			st.exitCurrentQuest(false);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getCond();
		if(id == 1)
		{
			if(st.getPlayer().getLevel() >= 25)
			{
				htmltext = "pet_manager_waters_q0042_0101.htm";
			}
			else
			{
				htmltext = "pet_manager_waters_q0042_0103.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(id == 2)
		{
			if(npcId == 30828)
			{
				if(cond == 1)
				{
					htmltext = st.getQuestItemsCount(291) == 0 ? "pet_manager_waters_q0042_0106.htm" : "pet_manager_waters_q0042_0105.htm";
				}
				else if(cond == 2)
				{
					htmltext = "pet_manager_waters_q0042_0204.htm";
				}
				else if(cond == 3)
				{
					htmltext = "pet_manager_waters_q0042_0203.htm";
				}
				else if(cond == 4)
				{
					htmltext = "pet_manager_waters_q0042_0303.htm";
				}
				else if(cond == 5)
				{
					htmltext = "pet_manager_waters_q0042_0401.htm";
				}
			}
			else if(npcId == 30735)
			{
				if(cond == 4 && st.getQuestItemsCount(7549) > 0)
				{
					htmltext = "sophia_q0042_0301.htm";
				}
				else if(cond == 5)
				{
					htmltext = "sophia_q0042_0402.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 2)
		{
			long pieces = st.getQuestItemsCount(7548);
			if(pieces < 29)
			{
				st.giveItems(7548, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(pieces == 29)
			{
				st.giveItems(7548, 1);
				st.playSound("ItemSound.quest_middle");
				st.setCond(3);
			}
		}
		return null;
	}
}