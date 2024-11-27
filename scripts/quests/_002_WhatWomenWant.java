package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.scripts.ScriptFile;

public class _002_WhatWomenWant extends Quest implements ScriptFile
{
	int ARUJIEN = 30223;
	int MIRABEL = 30146;
	int HERBIEL = 30150;
	int GREENIS = 30157;
	int ARUJIENS_LETTER1 = 1092;
	int ARUJIENS_LETTER2 = 1093;
	int ARUJIENS_LETTER3 = 1094;
	int POETRY_BOOK = 689;
	int GREENIS_LETTER = 693;
	int MYSTICS_EARRING = 113;
	
	public _002_WhatWomenWant()
	{
		super(false);
		addStartNpc(ARUJIEN);
		addTalkId(MIRABEL);
		addTalkId(HERBIEL);
		addTalkId(GREENIS);
		addQuestItem(GREENIS_LETTER, ARUJIENS_LETTER3, ARUJIENS_LETTER1, ARUJIENS_LETTER2, POETRY_BOOK);
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
			htmltext = "arujien_q0002_04.htm";
			st.giveItems(ARUJIENS_LETTER1, 1, false);
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("2_1"))
		{
			htmltext = "arujien_q0002_08.htm";
			st.takeItems(ARUJIENS_LETTER3, -1);
			st.giveItems(POETRY_BOOK, 1, false);
			st.setCond(4);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("2_2"))
		{
			htmltext = "arujien_q0002_09.htm";
			st.takeItems(ARUJIENS_LETTER3, -1);
			st.giveItems(57, 450, true);
			if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("ng1"))
			{
				st.getPlayer().sendPacket(new ExShowScreenMessage("  Delivery duty complete.\nGo find the Newbie Guide.", 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
			}
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == ARUJIEN)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.elf && st.getPlayer().getRace() != Race.human)
				{
					htmltext = "arujien_q0002_00.htm";
				}
				else if(st.getPlayer().getLevel() >= 2)
				{
					htmltext = "arujien_q0002_02.htm";
				}
				else
				{
					htmltext = "arujien_q0002_01.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1 && st.getQuestItemsCount(ARUJIENS_LETTER1) > 0)
			{
				htmltext = "arujien_q0002_05.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(ARUJIENS_LETTER2) > 0)
			{
				htmltext = "arujien_q0002_06.htm";
			}
			else if(cond == 3 && st.getQuestItemsCount(ARUJIENS_LETTER3) > 0)
			{
				htmltext = "arujien_q0002_07.htm";
			}
			else if(cond == 4 && st.getQuestItemsCount(POETRY_BOOK) > 0)
			{
				htmltext = "arujien_q0002_11.htm";
			}
			else if(cond == 5 && st.getQuestItemsCount(GREENIS_LETTER) > 0)
			{
				htmltext = "arujien_q0002_09.htm";
				st.takeItems(GREENIS_LETTER, -1);
				st.giveItems(MYSTICS_EARRING, 1, false);
				if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("ng1"))
				{
					st.getPlayer().sendPacket(new ExShowScreenMessage("  Delivery duty complete.\nGo find the Newbie Guide.", 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
				}
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(false);
			}
		}
		else if(npcId == MIRABEL)
		{
			if(cond == 1 && st.getQuestItemsCount(ARUJIENS_LETTER1) > 0)
			{
				htmltext = "mint_q0002_01.htm";
				st.takeItems(ARUJIENS_LETTER1, -1);
				st.giveItems(ARUJIENS_LETTER2, 1, false);
				st.setCond(2);
				st.playSound("ItemSound.quest_middle");
			}
			else if(cond == 2)
			{
				htmltext = "mint_q0002_02.htm";
			}
		}
		else if(npcId == HERBIEL)
		{
			if(cond == 2 && st.getQuestItemsCount(ARUJIENS_LETTER2) > 0)
			{
				htmltext = "green_q0002_01.htm";
				st.takeItems(ARUJIENS_LETTER2, -1);
				st.giveItems(ARUJIENS_LETTER3, 1, false);
				st.setCond(3);
				st.playSound("ItemSound.quest_middle");
			}
			else if(cond == 3)
			{
				htmltext = "green_q0002_02.htm";
			}
		}
		else if(npcId == GREENIS)
		{
			if(cond == 4 && st.getQuestItemsCount(POETRY_BOOK) > 0)
			{
				htmltext = "grain_q0002_02.htm";
				st.takeItems(POETRY_BOOK, -1);
				st.giveItems(GREENIS_LETTER, 1, false);
				st.setCond(5);
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				htmltext = cond == 5 && st.getQuestItemsCount(GREENIS_LETTER) > 0 ? "grain_q0002_03.htm" : "grain_q0002_01.htm";
			}
		}
		return htmltext;
	}
}