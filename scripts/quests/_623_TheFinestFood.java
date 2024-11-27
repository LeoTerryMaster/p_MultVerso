package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _623_TheFinestFood extends Quest implements ScriptFile
{
	public static final int HOT_SPRINGS_BUFFALO = 21315;
	public static final int HOT_SPRINGS_FLAVA = 21316;
	public static final int HOT_SPRINGS_ANTELOPE = 21318;
	public static final int LEAF_OF_FLAVA = 7199;
	public static final int BUFFALO_MEAT = 7200;
	public static final int ANTELOPE_HORN = 7201;
	public final int JEREMY = 31521;
	
	public _623_TheFinestFood()
	{
		super(true);
		addStartNpc(31521);
		addTalkId(31521);
		addKillId(21315);
		addKillId(21316);
		addKillId(21318);
		addQuestItem(7200);
		addQuestItem(7199);
		addQuestItem(7201);
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
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "jeremy_q0623_0104.htm";
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("623_3"))
		{
			htmltext = "jeremy_q0623_0201.htm";
			st.takeItems(7199, -1);
			st.takeItems(7200, -1);
			st.takeItems(7201, -1);
			int i1 = Rnd.get(100);
			if(i1 < 12)
			{
				st.giveItems(57, 25000);
				st.giveItems(6849, 1, false);
			}
			else if(i1 < 24)
			{
				st.giveItems(57, 65000);
				st.giveItems(6847, 1, false);
			}
			else if(i1 < 34)
			{
				st.giveItems(57, 25000);
				st.giveItems(6851, 1, false);
			}
			else
			{
				st.giveItems(57, 73000);
				st.addExpAndSp(230000, 18250);
			}
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int id = st.getState();
		if(id == 1)
		{
			st.setCond(0);
		}
		if(summ(st) >= 300)
		{
			st.setCond(2);
		}
		int cond = st.getCond();
		String htmltext = "noquest";
		if(npcId == 31521)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 71)
				{
					htmltext = "jeremy_q0623_0101.htm";
				}
				else
				{
					htmltext = "jeremy_q0623_0103.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1 && summ(st) < 300)
			{
				htmltext = "jeremy_q0623_0106.htm";
			}
			else if(cond == 2 && summ(st) >= 300)
			{
				htmltext = "jeremy_q0623_0105.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		int npcId = npc.getNpcId();
		if(cond == 1)
		{
			if(npcId == 21315)
			{
				if(st.getQuestItemsCount(7200) < 100)
				{
					st.rollAndGive(7200, 1, 100.0);
					if(st.getQuestItemsCount(7200) == 100)
					{
						if(summ(st) >= 300)
						{
							st.setCond(2);
						}
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 21316)
			{
				if(st.getQuestItemsCount(7199) < 100)
				{
					st.rollAndGive(7199, 1, 100.0);
					if(st.getQuestItemsCount(7199) == 100)
					{
						if(summ(st) >= 300)
						{
							st.setCond(2);
						}
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 21318 && st.getQuestItemsCount(7201) < 100)
			{
				st.rollAndGive(7201, 1, 100.0);
				if(st.getQuestItemsCount(7201) == 100)
				{
					if(summ(st) >= 300)
					{
						st.setCond(2);
					}
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
	
	private long summ(QuestState st)
	{
		return st.getQuestItemsCount(7199) + st.getQuestItemsCount(7200) + st.getQuestItemsCount(7201);
	}
}