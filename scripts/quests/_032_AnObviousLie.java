package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _032_AnObviousLie extends Quest implements ScriptFile
{
	private static final int maximilian = 30120;
	private static final int gentler = 30094;
	private static final int miki_the_cat = 31706;
	private static final int crocodile = 20135;
	private static final int q_map_of_gentler = 7165;
	private static final int q_medicinal_herb = 7166;
	private static final int spirit_ore = 3031;
	private static final int thread = 1868;
	private static final int suede = 1866;
	private static final int racoon_ear = 7680;
	private static final int cat_ear = 6843;
	private static final int rabbit_ear = 7683;
	
	public _032_AnObviousLie()
	{
		super(true);
		addStartNpc(30120);
		addTalkId(30120, 30094, 31706);
		addKillId(20135);
		addQuestItem(7166, 7165);
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
		int GetHTMLCookie = st.getInt("blatant_lie_cookie");
		int npcId = npc.getNpcId();
		if(npcId == 30120)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.set("blatant_lie", String.valueOf(11), true);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "maximilian_q0032_0104.htm";
			}
		}
		else if(npcId == 30094)
		{
			if(event.equalsIgnoreCase("reply_1") && GetHTMLCookie == 1)
			{
				st.setCond(2);
				st.set("blatant_lie", String.valueOf(21), true);
				st.giveItems(7165, 1);
				htmltext = "gentler_q0032_0201.htm";
				st.playSound("ItemSound.quest_middle");
			}
			else if(event.equalsIgnoreCase("reply_1") && GetHTMLCookie == 3)
			{
				if(st.getQuestItemsCount(7166) >= 20)
				{
					st.setCond(5);
					st.set("blatant_lie", String.valueOf(41), true);
					st.takeItems(7166, 20);
					htmltext = "gentler_q0032_0401.htm";
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					htmltext = "gentler_q0032_0402.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_1") && GetHTMLCookie == 4)
			{
				if(st.getQuestItemsCount(3031) >= 500)
				{
					st.setCond(6);
					st.set("blatant_lie", String.valueOf(51), true);
					st.takeItems(3031, 500);
					htmltext = "gentler_q0032_0501.htm";
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					htmltext = "gentler_q0032_0502.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_1") && GetHTMLCookie == 6)
			{
				st.setCond(8);
				st.set("blatant_lie", String.valueOf(71), true);
				htmltext = "gentler_q0032_0701.htm";
				st.playSound("ItemSound.quest_middle");
			}
			else if(event.equalsIgnoreCase("reply_3") && GetHTMLCookie == 7)
			{
				htmltext = "gentler_q0032_0801.htm";
			}
			else if(event.equalsIgnoreCase("reply_11") && GetHTMLCookie == 7)
			{
				if(st.getQuestItemsCount(1868) >= 1000 && st.getQuestItemsCount(1866) >= 500)
				{
					st.takeItems(1868, 1000);
					st.takeItems(1866, 500);
					st.giveItems(6843, 1);
					st.unset("blatant_lie");
					st.unset("blatant_lie_cookie");
					st.playSound("ItemSound.quest_finish");
					st.exitCurrentQuest(false);
					htmltext = "gentler_q0032_0802.htm";
				}
				else
				{
					htmltext = "gentler_q0032_0803.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_12") && GetHTMLCookie == 7)
			{
				if(st.getQuestItemsCount(1868) >= 1000 && st.getQuestItemsCount(1866) >= 500)
				{
					st.takeItems(1868, 1000);
					st.takeItems(1866, 500);
					st.giveItems(7680, 1);
					st.unset("blatant_lie");
					st.unset("blatant_lie_cookie");
					st.playSound("ItemSound.quest_finish");
					st.exitCurrentQuest(false);
					htmltext = "gentler_q0032_0802.htm";
				}
				else
				{
					htmltext = "gentler_q0032_0803.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_13") && GetHTMLCookie == 7)
			{
				if(st.getQuestItemsCount(1868) >= 1000 && st.getQuestItemsCount(1866) >= 500)
				{
					st.takeItems(1868, 1000);
					st.takeItems(1866, 500);
					st.giveItems(7683, 1);
					st.unset("blatant_lie");
					st.unset("blatant_lie_cookie");
					st.playSound("ItemSound.quest_finish");
					st.exitCurrentQuest(false);
					htmltext = "gentler_q0032_0802.htm";
				}
				else
				{
					htmltext = "gentler_q0032_0803.htm";
				}
			}
		}
		else if(npcId == 31706)
		{
			if(event.equalsIgnoreCase("reply_1") && GetHTMLCookie == 2)
			{
				st.setCond(3);
				st.set("blatant_lie", String.valueOf(31), true);
				st.takeItems(7165, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "miki_the_cat_q0032_0301.htm";
			}
			else if(event.equalsIgnoreCase("reply_1") && GetHTMLCookie == 5)
			{
				st.setCond(7);
				st.set("blatant_lie", String.valueOf(61), true);
				st.playSound("ItemSound.quest_middle");
				htmltext = "miki_the_cat_q0032_0601.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("blatant_lie");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 30120)
					break;
				if(st.getPlayer().getLevel() >= 45)
				{
					htmltext = "maximilian_q0032_0101.htm";
					break;
				}
				st.exitCurrentQuest(true);
				htmltext = "maximilian_q0032_0103.htm";
				break;
			}
			case 2:
			{
				if(npcId == 30120)
				{
					if(GetMemoState != 11)
						break;
					htmltext = "maximilian_q0032_0105.htm";
					break;
				}
				if(npcId == 30094)
				{
					if(GetMemoState == 11)
					{
						st.set("blatant_lie_cookie", String.valueOf(1), true);
						htmltext = "gentler_q0032_0101.htm";
						break;
					}
					if(GetMemoState == 21)
					{
						htmltext = "gentler_q0032_0202.htm";
						break;
					}
					if(GetMemoState == 32)
					{
						if(st.getQuestItemsCount(7166) >= 20)
						{
							st.set("blatant_lie_cookie", String.valueOf(3), true);
							htmltext = "gentler_q0032_0301.htm";
							break;
						}
						htmltext = "gentler_q0032_0302.htm";
						break;
					}
					if(GetMemoState == 41)
					{
						if(st.getQuestItemsCount(3031) >= 500)
						{
							st.set("blatant_lie_cookie", String.valueOf(4), true);
							htmltext = "gentler_q0032_0403.htm";
							break;
						}
						htmltext = "gentler_q0032_0404.htm";
						break;
					}
					if(GetMemoState == 51)
					{
						htmltext = "gentler_q0032_0503.htm";
						break;
					}
					if(GetMemoState == 61)
					{
						st.set("blatant_lie_cookie", String.valueOf(6), true);
						htmltext = "gentler_q0032_0601.htm";
						break;
					}
					if(GetMemoState != 71)
						break;
					if(st.getQuestItemsCount(1868) >= 1000 && st.getQuestItemsCount(1866) >= 500)
					{
						st.set("blatant_lie_cookie", String.valueOf(7), true);
						htmltext = "gentler_q0032_0702.htm";
						break;
					}
					htmltext = "gentler_q0032_0703.htm";
					break;
				}
				if(npcId != 31706)
					break;
				if(st.getQuestItemsCount(7165) >= 1 && GetMemoState == 21)
				{
					st.set("blatant_lie_cookie", String.valueOf(2), true);
					htmltext = "miki_the_cat_q0032_0201.htm";
					break;
				}
				if(GetMemoState == 31)
				{
					htmltext = "miki_the_cat_q0032_0303.htm";
					break;
				}
				if(GetMemoState == 51)
				{
					st.set("blatant_lie_cookie", String.valueOf(5), true);
					htmltext = "miki_the_cat_q0032_0501.htm";
					break;
				}
				if(GetMemoState != 61)
					break;
				htmltext = "miki_the_cat_q0032_0602.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("blatant_lie");
		int npcId = npc.getNpcId();
		int i4 = Rnd.get(500);
		if(GetMemoState == 31 && npcId == 20135 && i4 < 500)
		{
			if(st.getQuestItemsCount(7166) + 1 >= 20)
			{
				if(st.getQuestItemsCount(7166) <= 20)
				{
					st.giveItems(7166, 20 - st.getQuestItemsCount(7166));
					st.playSound("ItemSound.quest_middle");
				}
				st.setCond(4);
				st.set("blatant_lie", String.valueOf(32), true);
			}
			else
			{
				st.giveItems(7166, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}