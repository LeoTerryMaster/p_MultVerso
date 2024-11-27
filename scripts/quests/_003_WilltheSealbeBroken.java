package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _003_WilltheSealbeBroken extends Quest implements ScriptFile
{
	private static final int redry = 30141;
	private static final int onyx_beast = 20031;
	private static final int tainted_zombie = 20041;
	private static final int stink_zombie = 20046;
	private static final int least_succubus = 20048;
	private static final int least_succubus_turen = 20052;
	private static final int least_succubus_tilfo = 20057;
	private static final int scrl_of_ench_am_d = 956;
	private static final int onyx_beast_eye = 1081;
	private static final int taint_stone = 1082;
	private static final int succubus_blood = 1083;
	
	public _003_WilltheSealbeBroken()
	{
		super(false);
		addStartNpc(30141);
		addKillId(20031, 20041, 20046, 20048, 20052, 20057);
		addQuestItem(1081, 1082, 1083);
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
		int npcId = npc.getNpcId();
		if(npcId == 30141 && event.equalsIgnoreCase("quest_accept"))
		{
			st.setCond(1);
			st.set("release_darkelf_elder1", String.valueOf(1), true);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "redry_q0003_03.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("release_darkelf_elder1");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 30141)
					break;
				if(st.getPlayer().getLevel() >= 16)
				{
					if(st.getPlayer().getRace() != Race.darkelf)
					{
						st.exitCurrentQuest(true);
						htmltext = "redry_q0003_00.htm";
						break;
					}
					htmltext = "redry_q0003_02.htm";
					break;
				}
				htmltext = "redry_q0003_01.htm";
				break;
			}
			case 2:
			{
				if(npcId != 30141)
					break;
				if(GetMemoState == 1 && st.getQuestItemsCount(1081) >= 1 && st.getQuestItemsCount(1082) >= 1 && st.getQuestItemsCount(1083) >= 1)
				{
					st.giveItems(956, 1);
					st.takeItems(1081, -1);
					st.takeItems(1082, -1);
					st.takeItems(1083, -1);
					st.unset("release_darkelf_elder1");
					st.playSound("ItemSound.quest_finish");
					st.exitCurrentQuest(false);
					htmltext = "redry_q0003_06.htm";
					break;
				}
				htmltext = "redry_q0003_04.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("release_darkelf_elder1");
		int npcId = npc.getNpcId();
		if(GetMemoState == 1)
		{
			if(npcId == 20031)
			{
				st.giveItems(1081, 1);
				st.playSound("ItemSound.quest_middle");
				if(st.getQuestItemsCount(1082) >= 1 && st.getQuestItemsCount(1083) >= 1)
				{
					st.setCond(2);
				}
			}
			else if(npcId == 20041 || npcId == 20046)
			{
				st.giveItems(1082, 1);
				st.playSound("ItemSound.quest_middle");
				if(st.getQuestItemsCount(1081) >= 1 && st.getQuestItemsCount(1083) >= 1)
				{
					st.setCond(2);
				}
			}
			else if(npcId == 20048 || npcId == 20052 || npcId == 20057)
			{
				st.giveItems(1083, 1);
				st.playSound("ItemSound.quest_middle");
				if(st.getQuestItemsCount(1081) >= 1 && st.getQuestItemsCount(1082) >= 1)
				{
					st.setCond(2);
				}
			}
		}
		return null;
	}
}