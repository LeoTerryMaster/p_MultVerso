package quests;

import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;

public class _114_ResurrectionOfAnOldManager extends Quest implements ScriptFile
{
	private static final int NEWYEAR = 31961;
	private static final int YUMI = 32041;
	private static final int STONES = 32046;
	private static final int WENDY = 32047;
	private static final int BOX = 32050;
	private static final int GUARDIAN = 27318;
	private static final int DETECTOR = 8090;
	private static final int DETECTOR2 = 8091;
	private static final int STARSTONE = 8287;
	private static final int LETTER = 8288;
	private static final int STARSTONE2 = 8289;
	private NpcInstance GUARDIAN_SPAWN;
	
	public _114_ResurrectionOfAnOldManager()
	{
		super(false);
		addStartNpc(32041);
		addTalkId(32047);
		addTalkId(32050);
		addTalkId(32046);
		addTalkId(31961);
		addFirstTalkId(32046);
		addKillId(27318);
		addQuestItem(8090);
		addQuestItem(8091);
		addQuestItem(8287);
		addQuestItem(8288);
		addQuestItem(8289);
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
		if(event.equalsIgnoreCase("head_blacksmith_newyear_q0114_02.htm"))
		{
			st.setCond(22);
			st.takeItems(8288, 1);
			st.giveItems(8289, 1);
			st.playSound("ItemSound.quest_middle");
		}
		String htmltext = event;
		if(event.equalsIgnoreCase("collecter_yumi_q0114_04.htm"))
		{
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			st.setCond(1);
			st.set("talk", "0");
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_08.htm"))
		{
			st.set("talk", "1");
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_09.htm"))
		{
			st.setCond(2);
			st.playSound("ItemSound.quest_middle");
			st.set("talk", "0");
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_12.htm"))
		{
			int choice = st.getInt("choice");
			if(choice == 1)
			{
				htmltext = "collecter_yumi_q0114_12.htm";
			}
			else if(choice == 2)
			{
				htmltext = "collecter_yumi_q0114_13.htm";
			}
			else if(choice == 3)
			{
				htmltext = "collecter_yumi_q0114_14.htm";
			}
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_15.htm"))
		{
			st.set("talk", "1");
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_23.htm"))
		{
			st.set("talk", "2");
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_26.htm"))
		{
			st.setCond(6);
			st.playSound("ItemSound.quest_middle");
			st.set("talk", "0");
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_31.htm"))
		{
			st.setCond(17);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(8090, 1);
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_34.htm"))
		{
			st.takeItems(8091, 1);
			st.set("talk", "1");
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_38.htm"))
		{
			int choice = st.getInt("choice");
			if(choice > 1)
			{
				htmltext = "collecter_yumi_q0114_37.htm";
			}
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_40.htm"))
		{
			st.setCond(21);
			st.giveItems(8288, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_39.htm"))
		{
			st.setCond(20);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("pavel_atlanta_q0114_03.htm"))
		{
			st.setCond(19);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("pavel_atlanta_q0114_07.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_01.htm"))
		{
			if(st.getInt("talk") + st.getInt("talk1") == 2)
			{
				htmltext = "chaos_secretary_wendy_q0114_05.htm";
			}
			else if(st.getInt("talk") + st.getInt("talk1") + st.getInt("talk2") == 6)
			{
				htmltext = "chaos_secretary_wendy_q0114_06a.htm";
			}
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_02.htm"))
		{
			if(st.getInt("talk") == 0)
			{
				st.set("talk", "1");
			}
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_03.htm"))
		{
			if(st.getInt("talk1") == 0)
			{
				st.set("talk1", "1");
			}
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_06.htm"))
		{
			st.setCond(3);
			st.playSound("ItemSound.quest_middle");
			st.set("talk", "0");
			st.set("choice", "1");
			st.unset("talk1");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_07.htm"))
		{
			st.setCond(4);
			st.playSound("ItemSound.quest_middle");
			st.set("talk", "0");
			st.set("choice", "2");
			st.unset("talk1");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_09.htm"))
		{
			st.setCond(5);
			st.playSound("ItemSound.quest_middle");
			st.set("talk", "0");
			st.set("choice", "3");
			st.unset("talk1");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_14ab.htm"))
		{
			st.setCond(7);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_14b.htm"))
		{
			st.setCond(10);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_12c.htm"))
		{
			if(st.getInt("talk") == 0)
			{
				st.set("talk", "1");
			}
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_15b.htm"))
		{
			if(GUARDIAN_SPAWN == null || !st.getPlayer().knowsObject(GUARDIAN_SPAWN) || !GUARDIAN_SPAWN.isVisible())
			{
				GUARDIAN_SPAWN = st.addSpawn(27318, 96977, -110625, -3280, 900000);
				Functions.npcSay(GUARDIAN_SPAWN, "You, " + st.getPlayer().getName() + ", you attacked Wendy. Prepare to die!");
				GUARDIAN_SPAWN.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, st.getPlayer(), 999);
			}
			else
			{
				htmltext = "chaos_secretary_wendy_q0114_17b.htm";
			}
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_20b.htm"))
		{
			st.setCond(12);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_17c.htm"))
		{
			st.set("talk", "2");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_20c.htm"))
		{
			st.setCond(13);
			st.playSound("ItemSound.quest_middle");
			st.set("talk", "0");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_23c.htm"))
		{
			st.setCond(15);
			st.playSound("ItemSound.quest_middle");
			st.takeItems(8287, 1);
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_16a.htm"))
		{
			st.set("talk", "2");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_20a.htm"))
		{
			if(st.getCond() == 7)
			{
				st.setCond(8);
				st.set("talk", "0");
				st.playSound("ItemSound.quest_middle");
			}
			else if(st.getCond() == 8)
			{
				st.setCond(9);
				st.playSound("ItemSound.quest_middle");
				htmltext = "chaos_secretary_wendy_q0114_21a.htm";
			}
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_21a.htm"))
		{
			st.setCond(9);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_29c.htm"))
		{
			st.giveItems(8289, 1);
			st.takeItems(57, 3000);
			st.setCond(26);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("chaos_box2_q0114_01r.htm"))
		{
			st.playSound("ItemSound.armor_wood_3");
			st.set("talk", "1");
		}
		else if(event.equalsIgnoreCase("chaos_box2_q0114_03.htm"))
		{
			st.setCond(14);
			st.giveItems(8287, 1);
			st.playSound("ItemSound.quest_middle");
			st.set("talk", "0");
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		QuestState st = player.getQuestState(getName());
		if(st == null || st.isCompleted())
		{
			return "";
		}
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 32046 && cond == 17)
		{
			st.playSound("ItemSound.quest_middle");
			st.takeItems(8090, 1);
			st.giveItems(8091, 1);
			st.setCond(18);
			player.sendPacket(new ExShowScreenMessage("THE_RADIO_SIGNAL_DETECTOR_IS_RESPONDING_A_SUSPICIOUS_PILE_OF_STONES_CATCHES_YOUR_EYE", 4500, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
		}
		return "";
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int id = st.getState();
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		int talk = st.getInt("talk");
		int talk1 = st.getInt("talk1");
		if(npcId == 32041)
		{
			if(id == 1)
			{
				QuestState Pavel = st.getPlayer().getQuestState(_121_PavelTheGiants.class);
				if(Pavel == null)
				{
					return "collecter_yumi_q0114_01.htm";
				}
				if(st.getPlayer().getLevel() >= 49 && Pavel.getState() == 3)
				{
					htmltext = "collecter_yumi_q0114_02.htm";
				}
				else
				{
					htmltext = "collecter_yumi_q0114_01.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = talk == 0 ? "collecter_yumi_q0114_04.htm" : "collecter_yumi_q0114_08.htm";
			}
			else if(cond == 2)
			{
				htmltext = "collecter_yumi_q0114_10.htm";
			}
			else if(cond == 3 || cond == 4 || cond == 5)
			{
				htmltext = talk == 0 ? "collecter_yumi_q0114_11.htm" : talk == 1 ? "collecter_yumi_q0114_15.htm" : "collecter_yumi_q0114_23.htm";
			}
			else if(cond == 6)
			{
				htmltext = "collecter_yumi_q0114_27.htm";
			}
			else if(cond == 9 || cond == 12 || cond == 16)
			{
				htmltext = "collecter_yumi_q0114_28.htm";
			}
			else if(cond == 17)
			{
				htmltext = "collecter_yumi_q0114_32.htm";
			}
			else if(cond == 19)
			{
				htmltext = talk == 0 ? "collecter_yumi_q0114_33.htm" : "collecter_yumi_q0114_34.htm";
			}
			else if(cond == 20)
			{
				htmltext = "collecter_yumi_q0114_39.htm";
			}
			else if(cond == 21)
			{
				htmltext = "collecter_yumi_q0114_40z.htm";
			}
			else if(cond == 22 || cond == 26)
			{
				htmltext = "collecter_yumi_q0114_41.htm";
				st.setCond(27);
				st.playSound("ItemSound.quest_middle");
			}
			else if(cond == 27)
			{
				htmltext = "collecter_yumi_q0114_42.htm";
			}
		}
		else if(npcId == 32047)
		{
			if(cond == 2)
			{
				if(talk + talk1 < 2)
				{
					htmltext = "chaos_secretary_wendy_q0114_01.htm";
				}
				else if(talk + talk1 == 2)
				{
					htmltext = "chaos_secretary_wendy_q0114_05.htm";
				}
			}
			else if(cond == 3)
			{
				htmltext = "chaos_secretary_wendy_q0114_06b.htm";
			}
			else if(cond == 4 || cond == 5)
			{
				htmltext = "chaos_secretary_wendy_q0114_08.htm";
			}
			else if(cond == 6)
			{
				int choice = st.getInt("choice");
				if(choice == 1)
				{
					htmltext = talk == 0 ? "chaos_secretary_wendy_q0114_11a.htm" : talk == 1 ? "chaos_secretary_wendy_q0114_17c.htm" : "chaos_secretary_wendy_q0114_16a.htm";
				}
				else if(choice == 2)
				{
					htmltext = "chaos_secretary_wendy_q0114_11b.htm";
				}
				else if(choice == 3)
				{
					htmltext = talk == 0 ? "chaos_secretary_wendy_q0114_11c.htm" : talk == 1 ? "chaos_secretary_wendy_q0114_12c.htm" : "chaos_secretary_wendy_q0114_17c.htm";
				}
			}
			else if(cond == 7)
			{
				htmltext = talk == 0 ? "chaos_secretary_wendy_q0114_11c.htm" : talk == 1 ? "chaos_secretary_wendy_q0114_12c.htm" : "chaos_secretary_wendy_q0114_17c.htm";
			}
			else if(cond == 8)
			{
				htmltext = "chaos_secretary_wendy_q0114_16a.htm";
			}
			else if(cond == 9)
			{
				htmltext = "chaos_secretary_wendy_q0114_25c.htm";
			}
			else if(cond == 10)
			{
				htmltext = "chaos_secretary_wendy_q0114_18b.htm";
			}
			else if(cond == 11)
			{
				htmltext = "chaos_secretary_wendy_q0114_19b.htm";
			}
			else if(cond == 12)
			{
				htmltext = "chaos_secretary_wendy_q0114_25c.htm";
			}
			else if(cond == 13)
			{
				htmltext = "chaos_secretary_wendy_q0114_20c.htm";
			}
			else if(cond == 14)
			{
				htmltext = "chaos_secretary_wendy_q0114_22c.htm";
			}
			else if(cond == 15)
			{
				htmltext = "chaos_secretary_wendy_q0114_24c.htm";
				st.setCond(16);
				st.playSound("ItemSound.quest_middle");
			}
			else if(cond == 16)
			{
				htmltext = "chaos_secretary_wendy_q0114_25c.htm";
			}
			else if(cond == 20)
			{
				htmltext = "chaos_secretary_wendy_q0114_26c.htm";
			}
			else if(cond == 26)
			{
				htmltext = "chaos_secretary_wendy_q0114_32c.htm";
			}
		}
		else if(npcId == 32050)
		{
			if(cond == 13)
			{
				htmltext = talk == 0 ? "chaos_box2_q0114_01.htm" : "chaos_box2_q0114_02.htm";
			}
			else if(cond == 14)
			{
				htmltext = "chaos_box2_q0114_04.htm";
			}
		}
		else if(npcId == 32046)
		{
			if(cond == 18)
			{
				htmltext = "pavel_atlanta_q0114_02.htm";
			}
			else if(cond == 19)
			{
				htmltext = "pavel_atlanta_q0114_03.htm";
			}
			else if(cond == 27)
			{
				htmltext = "pavel_atlanta_q0114_04.htm";
			}
		}
		else if(npcId == 31961)
		{
			if(cond == 21)
			{
				htmltext = "head_blacksmith_newyear_q0114_01.htm";
			}
			else if(cond == 22)
			{
				htmltext = "head_blacksmith_newyear_q0114_03.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() != 2)
		{
			return null;
		}
		int npcId = npc.getNpcId();
		if(st.getCond() == 10 && npcId == 27318)
		{
			Functions.npcSay(npc, "This enemy is far too powerful for me to fight. I must withdraw");
			st.setCond(11);
			st.playSound("ItemSound.quest_middle");
		}
		return null;
	}
}