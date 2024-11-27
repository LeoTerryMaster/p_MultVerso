package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _637_ThroughOnceMore extends Quest implements ScriptFile
{
	private static final int falsepriest_flauron = 32010;
	private static final int bone_animator = 21565;
	private static final int skull_animator = 21566;
	private static final int bone_slayer = 21567;
	private static final int q_heart_of_reanimated = 8066;
	private static final int q_mark_of_sacrifice = 8064;
	private static final int q_faded_mark_of_sac = 8065;
	private static final int q_mark_of_heresy = 8067;
	private static final int q_key_of_anteroom = 8273;
	
	public _637_ThroughOnceMore()
	{
		super(false);
		addStartNpc(32010);
		addKillId(21565, 21566, 21567);
		addQuestItem(8066);
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
		if(npcId == 32010)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.set("beyond_the_door_again", String.valueOf(1), true);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "falsepriest_flauron_q0637_11.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
			{
				htmltext = "falsepriest_flauron_q0637_06.htm";
			}
			else if(event.equalsIgnoreCase("reply_2"))
			{
				htmltext = "falsepriest_flauron_q0637_07.htm";
			}
			else if(event.equalsIgnoreCase("reply_3"))
			{
				htmltext = "falsepriest_flauron_q0637_08.htm";
			}
			else if(event.equalsIgnoreCase("reply_4"))
			{
				htmltext = "falsepriest_flauron_q0637_09.htm";
			}
			else if(event.equalsIgnoreCase("reply_5"))
			{
				htmltext = "falsepriest_flauron_q0637_10.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("beyond_the_door_again");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 32010)
					break;
				if(st.getPlayer().getLevel() >= 73 && st.getQuestItemsCount(8065) >= 1 && st.getQuestItemsCount(8067) == 0)
				{
					htmltext = "falsepriest_flauron_q0637_01.htm";
					break;
				}
				htmltext = "falsepriest_flauron_q0637_02.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId != 32010)
					break;
				if(GetMemoState == 1 && st.getQuestItemsCount(8064) >= 1 && st.getQuestItemsCount(8065) == 0 && st.getQuestItemsCount(8067) == 0)
				{
					htmltext = "falsepriest_flauron_q0637_03.htm";
					break;
				}
				if(GetMemoState == 1 && st.getQuestItemsCount(8064) == 0 && st.getQuestItemsCount(8065) == 0 && st.getQuestItemsCount(8067) == 0)
				{
					htmltext = "falsepriest_flauron_q0637_04.htm";
					break;
				}
				if(GetMemoState == 1 && st.getQuestItemsCount(8067) >= 1)
				{
					htmltext = "falsepriest_flauron_q0637_05.htm";
					break;
				}
				if(GetMemoState == 1 && st.getQuestItemsCount(8066) < 10)
				{
					htmltext = "falsepriest_flauron_q0637_12.htm";
					break;
				}
				if(GetMemoState != 1 || st.getQuestItemsCount(8066) < 10)
					break;
				st.giveItems(8067, 1);
				st.giveItems(8273, 10);
				st.takeItems(8064, -1);
				st.takeItems(8065, -1);
				st.takeItems(8066, -1);
				st.unset("beyond_the_door_again");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				htmltext = "falsepriest_flauron_q0637_13.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("beyond_the_door_again");
		int npcId = npc.getNpcId();
		if(GetMemoState == 1 && st.getQuestItemsCount(8066) < 10)
		{
			if(npcId == 21565)
			{
				if(Rnd.get(100) < 84)
				{
					st.giveItems(8066, 1);
					if(st.getQuestItemsCount(8066) >= 9)
					{
						st.setCond(2);
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 21566)
			{
				if(Rnd.get(100) < 92)
				{
					st.giveItems(8066, 1);
					if(st.getQuestItemsCount(8066) >= 9)
					{
						st.setCond(2);
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 21567)
			{
				if(Rnd.get(100) < 10)
				{
					st.giveItems(8066, 1);
					if(st.getQuestItemsCount(8066) >= 9)
					{
						st.setCond(2);
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
				else
				{
					st.giveItems(8066, 1);
					if(st.getQuestItemsCount(8066) >= 9)
					{
						st.setCond(2);
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
		}
		return null;
	}
}