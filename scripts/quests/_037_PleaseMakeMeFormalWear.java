package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _037_PleaseMakeMeFormalWear extends Quest implements ScriptFile
{
	private static final int MYSTERIOUS_CLOTH = 7076;
	private static final int JEWEL_BOX = 7077;
	private static final int SEWING_KIT = 7078;
	private static final int DRESS_SHOES_BOX = 7113;
	private static final int SIGNET_RING = 7164;
	private static final int ICE_WINE = 7160;
	private static final int BOX_OF_COOKIES = 7159;
	
	public _037_PleaseMakeMeFormalWear()
	{
		super(false);
		addStartNpc(30842);
		addTalkId(30842);
		addTalkId(31520);
		addTalkId(31521);
		addTalkId(31627);
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
		if(event.equals("30842-1.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("31520-1.htm"))
		{
			st.giveItems(7164, 1);
			st.setCond(2);
		}
		else if(event.equals("31521-1.htm"))
		{
			st.takeItems(7164, 1);
			st.giveItems(7160, 1);
			st.setCond(3);
		}
		else if(event.equals("31627-1.htm"))
		{
			if(st.getQuestItemsCount(7160) > 0)
			{
				st.takeItems(7160, 1);
				st.setCond(4);
			}
			else
			{
				htmltext = "You don't have enough materials";
			}
		}
		else if(event.equals("31521-3.htm"))
		{
			st.giveItems(7159, 1);
			st.setCond(5);
		}
		else if(event.equals("31520-3.htm"))
		{
			st.takeItems(7159, 1);
			st.setCond(6);
		}
		else if(event.equals("31520-5.htm"))
		{
			st.takeItems(7076, 1);
			st.takeItems(7077, 1);
			st.takeItems(7078, 1);
			st.setCond(7);
		}
		else if(event.equals("31520-7.htm"))
		{
			if(st.getQuestItemsCount(7113) > 0)
			{
				st.takeItems(7113, 1);
				st.giveItems(6408, 1);
				st.unset("cond");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(false);
			}
			else
			{
				htmltext = "You don't have enough materials";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30842)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 60)
				{
					htmltext = "30842-0.htm";
				}
				else
				{
					htmltext = "30842-2.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "30842-1.htm";
			}
		}
		else if(npcId == 31520)
		{
			if(cond == 1)
			{
				htmltext = "31520-0.htm";
			}
			else if(cond == 2)
			{
				htmltext = "31520-1.htm";
			}
			else if(cond == 5 || cond == 6)
			{
				htmltext = st.getQuestItemsCount(7076) > 0 && st.getQuestItemsCount(7077) > 0 && st.getQuestItemsCount(7078) > 0 ? "31520-4.htm" : st.getQuestItemsCount(7159) > 0 ? "31520-2.htm" : "31520-3.htm";
			}
			else if(cond == 7)
			{
				htmltext = st.getQuestItemsCount(7113) > 0 ? "31520-6.htm" : "31520-5.htm";
			}
		}
		else if(npcId == 31521)
		{
			if(st.getQuestItemsCount(7164) > 0)
			{
				htmltext = "31521-0.htm";
			}
			else if(cond == 3)
			{
				htmltext = "31521-1.htm";
			}
			else if(cond == 4)
			{
				htmltext = "31521-2.htm";
			}
			else if(cond == 5)
			{
				htmltext = "31521-3.htm";
			}
		}
		else if(npcId == 31627)
		{
			htmltext = "31627-0.htm";
		}
		return htmltext;
	}
}