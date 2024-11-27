package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _369_CollectorOfJewels extends Quest implements ScriptFile
{
	private static final int magister_nell = 30376;
	private static final int salamander_lakin = 20609;
	private static final int salamander_rowin = 20612;
	private static final int undine_lakin = 20616;
	private static final int undine_rowin = 20619;
	private static final int roxide = 20747;
	private static final int death_fire = 20749;
	private static final int flair_shard = 5882;
	private static final int freezing_shard = 5883;
	
	public _369_CollectorOfJewels()
	{
		super(false);
		addStartNpc(magister_nell);
		addKillId(20609, 20612, 20616, 20619, 20747, 20749);
		addQuestItem(flair_shard, freezing_shard);
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
		if(npcId == magister_nell)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.set("man_collect_element_gem", String.valueOf(1), true);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "magister_nell_q0369_03.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
			{
				st.setCond(3);
				st.set("man_collect_element_gem", String.valueOf(3), true);
				htmltext = "magister_nell_q0369_07.htm";
			}
			else if(event.equalsIgnoreCase("reply_2"))
			{
				st.takeItems(flair_shard, -1);
				st.takeItems(freezing_shard, -1);
				st.unset("man_collect_element_gem");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				htmltext = "magister_nell_q0369_08.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("man_collect_element_gem");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != magister_nell)
					break;
				if(st.getPlayer().getLevel() < 25)
				{
					htmltext = "magister_nell_q0369_01.htm";
					st.exitCurrentQuest(true);
					break;
				}
				htmltext = "magister_nell_q0369_02.htm";
				break;
			}
			case 2:
			{
				if(npcId != magister_nell)
					break;
				if((st.getQuestItemsCount(freezing_shard) < 50 || st.getQuestItemsCount(flair_shard) < 50) && GetMemoState == 1)
				{
					htmltext = "magister_nell_q0369_04.htm";
					break;
				}
				if(st.getQuestItemsCount(freezing_shard) >= 50 && st.getQuestItemsCount(flair_shard) >= 50 && GetMemoState == 1)
				{
					st.giveItems(57, 12500);
					st.takeItems(flair_shard, -1);
					st.takeItems(freezing_shard, -1);
					st.set("man_collect_element_gem", String.valueOf(2), true);
					htmltext = "magister_nell_q0369_05.htm";
					break;
				}
				if(GetMemoState == 2)
				{
					htmltext = "magister_nell_q0369_09.htm";
					break;
				}
				if(GetMemoState == 3 && (st.getQuestItemsCount(freezing_shard) < 200 || st.getQuestItemsCount(flair_shard) < 200))
				{
					htmltext = "magister_nell_q0369_10.htm";
					break;
				}
				if(GetMemoState != 3 || st.getQuestItemsCount(freezing_shard) < 200 || st.getQuestItemsCount(flair_shard) < 200)
					break;
				st.giveItems(57, 76000);
				st.takeItems(flair_shard, -1);
				st.takeItems(freezing_shard, -1);
				st.unset("man_collect_element_gem");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				htmltext = "magister_nell_q0369_11.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("man_collect_element_gem");
		int npcId = npc.getNpcId();
		if(npcId == 20609)
		{
			if(Rnd.get(100) < 75)
			{
				st.giveItems(flair_shard, 1);
				if(GetMemoState == 1 && st.getQuestItemsCount(freezing_shard) >= 50 && st.getQuestItemsCount(flair_shard) >= 49)
				{
					st.setCond(2);
					st.playSound("ItemSound.quest_middle");
				}
				else if(GetMemoState == 3 && st.getQuestItemsCount(freezing_shard) >= 200 && st.getQuestItemsCount(flair_shard) >= 199)
				{
					st.setCond(4);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 20612)
		{
			if(Rnd.get(100) < 91)
			{
				st.giveItems(flair_shard, 1);
				if(GetMemoState == 1 && st.getQuestItemsCount(freezing_shard) >= 50 && st.getQuestItemsCount(flair_shard) >= 49)
				{
					st.setCond(2);
					st.playSound("ItemSound.quest_middle");
				}
				else if(GetMemoState == 3 && st.getQuestItemsCount(freezing_shard) >= 200 && st.getQuestItemsCount(flair_shard) >= 199)
				{
					st.setCond(4);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 20616)
		{
			if(Rnd.get(100) < 80)
			{
				st.giveItems(flair_shard, 1);
				if(GetMemoState == 1 && st.getQuestItemsCount(freezing_shard) >= 50 && st.getQuestItemsCount(flair_shard) >= 49)
				{
					st.setCond(2);
					st.playSound("ItemSound.quest_middle");
				}
				else if(GetMemoState == 3 && st.getQuestItemsCount(freezing_shard) >= 200 && st.getQuestItemsCount(flair_shard) >= 199)
				{
					st.setCond(4);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 20619)
		{
			if(Rnd.get(100) < 87)
			{
				st.giveItems(flair_shard, 1);
				if(GetMemoState == 1 && st.getQuestItemsCount(freezing_shard) >= 50 && st.getQuestItemsCount(flair_shard) >= 49)
				{
					st.setCond(2);
					st.playSound("ItemSound.quest_middle");
				}
				else if(GetMemoState == 3 && st.getQuestItemsCount(freezing_shard) >= 200 && st.getQuestItemsCount(flair_shard) >= 199)
				{
					st.setCond(4);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 20747 || npcId == 20749)
		{
			if(Rnd.get(100) < 2)
			{
				if(GetMemoState == 1 && st.getQuestItemsCount(freezing_shard) >= 49)
				{
					st.giveItems(freezing_shard, 1);
				}
				else if(GetMemoState == 3 && st.getQuestItemsCount(freezing_shard) >= 199)
				{
					st.giveItems(freezing_shard, 1);
				}
				else
				{
					st.giveItems(freezing_shard, 2);
				}
				if(GetMemoState == 1 && st.getQuestItemsCount(freezing_shard) >= 49 && st.getQuestItemsCount(flair_shard) >= 50)
				{
					st.setCond(2);
					st.playSound("ItemSound.quest_middle");
				}
				else if(GetMemoState == 3 && st.getQuestItemsCount(freezing_shard) >= 199 && st.getQuestItemsCount(flair_shard) >= 200)
				{
					st.setCond(4);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else
			{
				st.giveItems(freezing_shard, 1);
				if(GetMemoState == 1 && st.getQuestItemsCount(freezing_shard) >= 49 && st.getQuestItemsCount(flair_shard) >= 50)
				{
					st.setCond(2);
					st.playSound("ItemSound.quest_middle");
				}
				else if(GetMemoState == 3 && st.getQuestItemsCount(freezing_shard) >= 199 && st.getQuestItemsCount(flair_shard) >= 200)
				{
					st.setCond(4);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return null;
	}
}