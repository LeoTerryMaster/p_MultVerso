package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _603_DaimontheWhiteEyedPart1 extends Quest implements ScriptFile
{
	private static final int eye_of_argos = 31683;
	private static final int ancient_lithography1 = 31548;
	private static final int ancient_lithography2 = 31549;
	private static final int ancient_lithography3 = 31550;
	private static final int ancient_lithography4 = 31551;
	private static final int ancient_lithography5 = 31552;
	private static final int buffalo_slave = 21299;
	private static final int bandersnatch_slave = 21297;
	private static final int grendel_slave = 21304;
	private static final int q_dark_evil_spirit = 7190;
	private static final int q_broken_crystal = 7191;
	private static final int q_unfinished_s_crystal = 7192;
	
	public _603_DaimontheWhiteEyedPart1()
	{
		super(true);
		addStartNpc(31683);
		addTalkId(31548, 31549, 31550, 31551, 31552);
		addKillId(21299, 21297, 21304);
		addQuestItem(7190);
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
		if(npcId == 31683)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.set("daemon_of_hundred_eyes_first", String.valueOf(11), true);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "eye_of_argos_q0603_0104.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
			{
				if(st.getQuestItemsCount(7191) >= 5)
				{
					st.setCond(7);
					st.set("daemon_of_hundred_eyes_first", String.valueOf(71), true);
					st.takeItems(7191, 5);
					st.playSound("ItemSound.quest_middle");
					htmltext = "eye_of_argos_q0603_0701.htm";
				}
				else
				{
					htmltext = "eye_of_argos_q0603_0702.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_3"))
			{
				if(st.getQuestItemsCount(7190) >= 200)
				{
					st.takeItems(7190, -1);
					st.giveItems(7192, 1);
					st.unset("daemon_of_hundred_eyes_first");
					st.playSound("ItemSound.quest_finish");
					st.exitCurrentQuest(false);
					htmltext = "eye_of_argos_q0603_0801.htm";
				}
				else
				{
					htmltext = "eye_of_argos_q0603_0802.htm";
				}
			}
		}
		else if(npcId == 31548)
		{
			if(event.equalsIgnoreCase("reply_1"))
			{
				st.setCond(2);
				st.set("daemon_of_hundred_eyes_first", String.valueOf(21), true);
				st.giveItems(7191, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "ancient_lithography1_q0603_0201.htm";
			}
		}
		else if(npcId == 31549)
		{
			if(event.equalsIgnoreCase("reply_1"))
			{
				st.setCond(3);
				st.set("daemon_of_hundred_eyes_first", String.valueOf(31), true);
				st.giveItems(7191, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "ancient_lithography2_q0603_0301.htm";
			}
		}
		else if(npcId == 31550)
		{
			if(event.equalsIgnoreCase("reply_1"))
			{
				st.setCond(4);
				st.set("daemon_of_hundred_eyes_first", String.valueOf(41), true);
				st.giveItems(7191, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "ancient_lithography3_q0603_0401.htm";
			}
		}
		else if(npcId == 31551)
		{
			if(event.equalsIgnoreCase("reply_1"))
			{
				st.setCond(5);
				st.set("daemon_of_hundred_eyes_first", String.valueOf(51), true);
				st.giveItems(7191, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "ancient_lithography4_q0603_0501.htm";
			}
		}
		else if(npcId == 31552 && event.equalsIgnoreCase("reply_1"))
		{
			st.setCond(6);
			st.set("daemon_of_hundred_eyes_first", String.valueOf(61), true);
			st.giveItems(7191, 1);
			st.playSound("ItemSound.quest_middle");
			htmltext = "ancient_lithography5_q0603_0601.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("daemon_of_hundred_eyes_first");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 31683)
					break;
				if(st.getPlayer().getLevel() >= 73)
				{
					htmltext = "eye_of_argos_q0603_0101.htm";
					break;
				}
				htmltext = "eye_of_argos_q0603_0103.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId == 31683)
				{
					if(GetMemoState == 11)
					{
						htmltext = "eye_of_argos_q0603_0105.htm";
						break;
					}
					if(st.getQuestItemsCount(7191) >= 1 && GetMemoState == 61)
					{
						htmltext = "eye_of_argos_q0603_0601.htm";
						break;
					}
					if(GetMemoState > 72 || GetMemoState < 71)
						break;
					if(GetMemoState == 72 && st.getQuestItemsCount(7190) >= 200)
					{
						htmltext = "eye_of_argos_q0603_0703.htm";
						break;
					}
					htmltext = "eye_of_argos_q0603_0704.htm";
					break;
				}
				if(npcId == 31548)
				{
					if(GetMemoState == 11)
					{
						htmltext = "ancient_lithography1_q0603_0101.htm";
						break;
					}
					if(GetMemoState != 21)
						break;
					htmltext = "ancient_lithography1_q0603_0203.htm";
					break;
				}
				if(npcId == 31549)
				{
					if(st.getQuestItemsCount(7191) >= 1 && GetMemoState == 21)
					{
						htmltext = "ancient_lithography2_q0603_0201.htm";
						break;
					}
					if(GetMemoState != 31)
						break;
					htmltext = "ancient_lithography2_q0603_0303.htm";
					break;
				}
				if(npcId == 31550)
				{
					if(st.getQuestItemsCount(7191) >= 1 && GetMemoState == 31)
					{
						htmltext = "ancient_lithography3_q0603_0301.htm";
						break;
					}
					if(GetMemoState != 41)
						break;
					htmltext = "ancient_lithography3_q0603_0403.htm";
					break;
				}
				if(npcId == 31551)
				{
					if(st.getQuestItemsCount(7191) >= 1 && GetMemoState == 41)
					{
						htmltext = "ancient_lithography4_q0603_0401.htm";
						break;
					}
					if(GetMemoState != 51)
						break;
					htmltext = "ancient_lithography4_q0603_0503.htm";
					break;
				}
				if(npcId != 31552)
					break;
				if(st.getQuestItemsCount(7191) >= 1 && GetMemoState == 51)
				{
					htmltext = "ancient_lithography5_q0603_0501.htm";
					break;
				}
				if(GetMemoState != 61)
					break;
				htmltext = "ancient_lithography5_q0603_0603.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("daemon_of_hundred_eyes_first");
		int npcId = npc.getNpcId();
		if(GetMemoState == 71)
		{
			if(npcId == 21299)
			{
				int i42 = Rnd.get(1000);
				if(i42 < 519)
				{
					if(st.getQuestItemsCount(7190) + 1 >= 200)
					{
						if(st.getQuestItemsCount(7190) < 200)
						{
							st.setCond(8);
							st.set("daemon_of_hundred_eyes_first", String.valueOf(72), true);
							st.giveItems(7190, 200 - st.getQuestItemsCount(7190));
							st.playSound("ItemSound.quest_middle");
						}
					}
					else
					{
						st.giveItems(7190, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 21297)
			{
				int i43 = Rnd.get(1000);
				if(i43 < 500)
				{
					if(st.getQuestItemsCount(7190) + 1 >= 200)
					{
						if(st.getQuestItemsCount(7190) < 200)
						{
							st.setCond(8);
							st.set("daemon_of_hundred_eyes_first", String.valueOf(72), true);
							st.giveItems(7190, 200 - st.getQuestItemsCount(7190));
							st.playSound("ItemSound.quest_middle");
						}
					}
					else
					{
						st.giveItems(7190, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 21304 && Rnd.get(1000) < 673)
			{
				if(st.getQuestItemsCount(7190) + 1 >= 200)
				{
					if(st.getQuestItemsCount(7190) < 200)
					{
						st.setCond(8);
						st.set("daemon_of_hundred_eyes_first", String.valueOf(72), true);
						st.giveItems(7190, 200 - st.getQuestItemsCount(7190));
						st.playSound("ItemSound.quest_middle");
					}
				}
				else
				{
					st.giveItems(7190, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return null;
	}
}