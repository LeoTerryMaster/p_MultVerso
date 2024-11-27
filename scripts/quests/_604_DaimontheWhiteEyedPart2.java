package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;

public class _604_DaimontheWhiteEyedPart2 extends Quest implements ScriptFile
{
	private static final int eye_of_argos = 31683;
	private static final int daimons_altar = 31541;
	private static final int daemon_of_hundred_eyes = 25290;
	private static final int q_unfinished_s_crystal = 7192;
	private static final int q_summon_crystal = 7193;
	private static final int q_essense_of_daimon = 7194;
	private static final int dye_i2m2_c = 4595;
	private static final int dye_i2w2_c = 4596;
	private static final int dye_m2i2_c = 4597;
	private static final int dye_m2w2_c = 4598;
	private static final int dye_w2i2_c = 4599;
	private static final int dye_w2m2_c = 4600;
	
	public _604_DaimontheWhiteEyedPart2()
	{
		super(true);
		addStartNpc(31683);
		addTalkId(31541);
		addKillId(25290);
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
		int spawned_daemon_of_hundred_eyes = st.getInt("spawned_daemon_of_hundred_eyes");
		int GetHTMLCookie = st.getInt("daemon_of_hundred_eyes_second_cookie");
		int npcId = npc.getNpcId();
		if(npcId == 31683)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.set("daemon_of_hundred_eyes_second", String.valueOf(11), true);
				st.set("spawned_daemon_of_hundred_eyes", String.valueOf(0), true);
				st.takeItems(7192, -1);
				st.giveItems(7193, 1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "eye_of_argos_q0604_0104.htm";
			}
			else if(event.equalsIgnoreCase("reply_3") && GetHTMLCookie == 2)
			{
				if(st.getQuestItemsCount(7194) >= 1)
				{
					int i1 = Rnd.get(1000);
					st.takeItems(7194, 1);
					if(i1 < 167)
					{
						st.giveItems(4595, 5);
					}
					else if(i1 < 334)
					{
						st.giveItems(4596, 5);
					}
					else if(i1 < 501)
					{
						st.giveItems(4597, 5);
					}
					else if(i1 < 668)
					{
						st.giveItems(4598, 5);
					}
					else if(i1 < 835)
					{
						st.giveItems(4599, 5);
					}
					else if(i1 < 1000)
					{
						st.giveItems(4600, 5);
					}
					st.unset("daemon_of_hundred_eyes_second");
					st.unset("daemon_of_hundred_eyes_second_cookie");
					st.playSound("ItemSound.quest_finish");
					st.exitCurrentQuest(true);
					htmltext = "eye_of_argos_q0604_0301.htm";
				}
				else
				{
					htmltext = "eye_of_argos_q0604_0302.htm";
				}
			}
		}
		else if(npcId == 31541)
		{
			if(event.equalsIgnoreCase("60401"))
			{
				Functions.npcSay(npc, "Can light exist without darkness?");
				st.set("spawned_daemon_of_hundred_eyes", String.valueOf(0), true);
				if(npc != null)
				{
					npc.deleteMe();
				}
				return null;
			}
			if(event.equalsIgnoreCase("reply_1") && GetHTMLCookie == 1)
			{
				if(st.getQuestItemsCount(7193) >= 1)
				{
					if(spawned_daemon_of_hundred_eyes == 0)
					{
						st.setCond(2);
						st.set("daemon_of_hundred_eyes_second", String.valueOf(21), true);
						st.takeItems(7193, 1);
						st.set("spawned_daemon_of_hundred_eyes", String.valueOf(1), true);
						NpcInstance daemon_eyes = st.addSpawn(25290, 186320, -43904, -3175);
						Functions.npcSay(daemon_eyes, "Who is calling me?");
						st.startQuestTimer("60401", 1200000, daemon_eyes);
						st.playSound("ItemSound.quest_middle");
						htmltext = "daimons_altar_q0604_0201.htm";
					}
					else
					{
						htmltext = "daimons_altar_q0604_0202.htm";
					}
				}
				else
				{
					htmltext = "daimons_altar_q0604_0203.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("daemon_of_hundred_eyes_second");
		int spawned_daemon_of_hundred_eyes = st.getInt("spawned_daemon_of_hundred_eyes");
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
					if(st.getQuestItemsCount(7192) >= 1)
					{
						htmltext = "eye_of_argos_q0604_0101.htm";
						break;
					}
					htmltext = "eye_of_argos_q0604_0102.htm";
					st.exitCurrentQuest(true);
					break;
				}
				htmltext = "eye_of_argos_q0604_0103.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId == 31683)
				{
					if(GetMemoState == 11)
					{
						htmltext = "eye_of_argos_q0604_0105.htm";
						break;
					}
					if(GetMemoState < 22)
						break;
					if(st.getQuestItemsCount(7194) >= 1)
					{
						st.set("daemon_of_hundred_eyes_second_cookie", String.valueOf(2), true);
						htmltext = "eye_of_argos_q0604_0201.htm";
						break;
					}
					htmltext = "eye_of_argos_q0604_0202.htm";
					break;
				}
				if(npcId != 31541)
					break;
				if(st.getQuestItemsCount(7193) >= 1 && GetMemoState == 11)
				{
					st.set("daemon_of_hundred_eyes_second_cookie", String.valueOf(1), true);
					htmltext = "daimons_altar_q0604_0101.htm";
					break;
				}
				if(GetMemoState == 21)
				{
					if(spawned_daemon_of_hundred_eyes == 0)
					{
						st.set("spawned_daemon_of_hundred_eyes", String.valueOf(1), true);
						NpcInstance daemon_eyes = st.addSpawn(25290, 186320, -43904, -3175);
						Functions.npcSay(daemon_eyes, "Who is calling me?");
						st.startQuestTimer("60401", 1200000, daemon_eyes);
						htmltext = "daimons_altar_q0604_0201.htm";
						break;
					}
					htmltext = "daimons_altar_q0604_0202.htm";
					break;
				}
				if(GetMemoState < 22)
					break;
				htmltext = "daimons_altar_q0604_0204.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("daemon_of_hundred_eyes_second");
		int npcId = npc.getNpcId();
		if(GetMemoState >= 11 && GetMemoState <= 21 && npcId == 25290 && Rnd.get(1000) < 1000)
		{
			if(st.getQuestItemsCount(7194) + 1 >= 1)
			{
				st.setCond(3);
				st.set("daemon_of_hundred_eyes_second", String.valueOf(22), true);
				st.set("spawned_daemon_of_hundred_eyes", String.valueOf(0), true);
				st.giveItems(7194, 1 - st.getQuestItemsCount(7194));
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.giveItems(7194, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}