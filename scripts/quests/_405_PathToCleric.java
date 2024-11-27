package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _405_PathToCleric extends Quest implements ScriptFile
{
	public final int GALLINT = 30017;
	public final int ZIGAUNT = 30022;
	public final int VIVYAN = 30030;
	public final int SIMPLON = 30253;
	public final int PRAGA = 30333;
	public final int LIONEL = 30408;
	public final int RUIN_ZOMBIE = 20026;
	public final int RUIN_ZOMBIE_LEADER = 20029;
	public final int LETTER_OF_ORDER1 = 1191;
	public final int LETTER_OF_ORDER2 = 1192;
	public final int BOOK_OF_LEMONIELL = 1193;
	public final int BOOK_OF_VIVI = 1194;
	public final int BOOK_OF_SIMLON = 1195;
	public final int BOOK_OF_PRAGA = 1196;
	public final int CERTIFICATE_OF_GALLINT = 1197;
	public final int PENDANT_OF_MOTHER = 1198;
	public final int NECKLACE_OF_MOTHER = 1199;
	public final int LEMONIELLS_COVENANT = 1200;
	public final int MARK_OF_FAITH = 1201;
	
	public _405_PathToCleric()
	{
		super(false);
		addStartNpc(30022);
		addTalkId(30017);
		addTalkId(30030);
		addTalkId(30253);
		addTalkId(30333);
		addTalkId(30408);
		addKillId(20026);
		addKillId(20029);
		addQuestItem(1200, 1192, 1196, 1194, 1195, 1191, 1199, 1198, 1197, 1193);
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
	
	public void checkBooks(QuestState st)
	{
		if(st.getQuestItemsCount(1196) + st.getQuestItemsCount(1194) + st.getQuestItemsCount(1195) >= 5)
		{
			st.setCond(2);
		}
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("1"))
		{
			if(st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 10 && st.getQuestItemsCount(1201) < 1)
			{
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				st.giveItems(1191, 1);
				htmltext = "gigon_q0405_05.htm";
			}
			else if(st.getPlayer().getClassId().getId() != 10)
			{
				htmltext = st.getPlayer().getClassId().getId() == 15 ? "gigon_q0405_02a.htm" : "gigon_q0405_02.htm";
			}
			else if(st.getPlayer().getLevel() < 18 && st.getPlayer().getClassId().getId() == 10)
			{
				htmltext = "gigon_q0405_03.htm";
			}
			else if(st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 10 && st.getQuestItemsCount(1201) > 0)
			{
				htmltext = "gigon_q0405_04.htm";
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
		if(npcId == 30022)
		{
			if(st.getQuestItemsCount(1201) > 0)
			{
				htmltext = "gigon_q0405_04.htm";
				st.exitCurrentQuest(true);
			}
			if(cond < 1 && st.getQuestItemsCount(1201) < 1)
			{
				htmltext = "gigon_q0405_01.htm";
			}
			else if(cond == 1 | cond == 2 && st.getQuestItemsCount(1191) > 0)
			{
				if(st.getQuestItemsCount(1194) > 0 && st.getQuestItemsCount(1195) > 2 && st.getQuestItemsCount(1196) > 0)
				{
					htmltext = "gigon_q0405_08.htm";
					st.takeItems(1196, -1);
					st.takeItems(1194, -1);
					st.takeItems(1195, -1);
					st.takeItems(1191, -1);
					st.giveItems(1192, 1);
					st.setCond(3);
				}
				else
				{
					htmltext = "gigon_q0405_06.htm";
				}
			}
			else if(cond < 6 && st.getQuestItemsCount(1192) > 0)
			{
				htmltext = "gigon_q0405_07.htm";
			}
			else if(cond == 6 && st.getQuestItemsCount(1192) > 0 && st.getQuestItemsCount(1200) > 0)
			{
				htmltext = "gigon_q0405_09.htm";
				st.takeItems(1200, -1);
				st.takeItems(1192, -1);
				if(!st.getPlayer().getVarB("q405"))
				{
					st.getPlayer().setVar("q405", "1", -1);
				}
				st.exitCurrentQuest(true);
				if(st.getPlayer().getClassId().getLevel() == 1)
				{
					st.giveItems(1201, 1);
					if(!st.getPlayer().getVarB("prof1"))
					{
						st.getPlayer().setVar("prof1", "1", -1);
						st.addExpAndSp(3200, 5610);
					}
				}
				st.playSound("ItemSound.quest_finish");
			}
		}
		else if(npcId == 30253 && cond == 1 && st.getQuestItemsCount(1191) > 0)
		{
			if(st.getQuestItemsCount(1195) < 1)
			{
				htmltext = "trader_simplon_q0405_01.htm";
				st.giveItems(1195, 3);
				checkBooks(st);
			}
			else if(st.getQuestItemsCount(1195) > 2)
			{
				htmltext = "trader_simplon_q0405_02.htm";
			}
		}
		else if(npcId == 30030 && cond == 1 && st.getQuestItemsCount(1191) > 0)
		{
			if(st.getQuestItemsCount(1194) < 1)
			{
				htmltext = "vivi_q0405_01.htm";
				st.giveItems(1194, 1);
				checkBooks(st);
			}
			else if(st.getQuestItemsCount(1194) > 0)
			{
				htmltext = "vivi_q0405_02.htm";
			}
		}
		else if(npcId == 30333 && cond == 1 && st.getQuestItemsCount(1191) > 0)
		{
			if(st.getQuestItemsCount(1196) < 1 && st.getQuestItemsCount(1199) < 1)
			{
				htmltext = "guard_praga_q0405_01.htm";
				st.giveItems(1199, 1);
			}
			else if(st.getQuestItemsCount(1196) < 1 && st.getQuestItemsCount(1199) > 0 && st.getQuestItemsCount(1198) < 1)
			{
				htmltext = "guard_praga_q0405_02.htm";
			}
			else if(st.getQuestItemsCount(1196) < 1 && st.getQuestItemsCount(1199) > 0 && st.getQuestItemsCount(1198) > 0)
			{
				htmltext = "guard_praga_q0405_03.htm";
				st.takeItems(1199, -1);
				st.takeItems(1198, -1);
				st.giveItems(1196, 1);
				checkBooks(st);
			}
			else if(st.getQuestItemsCount(1196) > 0)
			{
				htmltext = "guard_praga_q0405_04.htm";
			}
		}
		else if(npcId == 30408)
		{
			if(st.getQuestItemsCount(1192) < 1)
			{
				htmltext = "lemoniell_q0405_02.htm";
			}
			else if(cond == 3 && st.getQuestItemsCount(1192) == 1 && st.getQuestItemsCount(1193) < 1 && st.getQuestItemsCount(1200) < 1 && st.getQuestItemsCount(1197) < 1)
			{
				htmltext = "lemoniell_q0405_01.htm";
				st.giveItems(1193, 1);
				st.setCond(4);
			}
			else if(cond == 4 && st.getQuestItemsCount(1192) == 1 && st.getQuestItemsCount(1193) > 0 && st.getQuestItemsCount(1200) < 1 && st.getQuestItemsCount(1197) < 1)
			{
				htmltext = "lemoniell_q0405_03.htm";
			}
			else if(st.getQuestItemsCount(1192) == 1 && st.getQuestItemsCount(1193) < 1 && st.getQuestItemsCount(1200) < 1 && st.getQuestItemsCount(1197) > 0)
			{
				htmltext = "lemoniell_q0405_04.htm";
				st.takeItems(1197, -1);
				st.giveItems(1200, 1);
				st.setCond(6);
			}
			else if(st.getQuestItemsCount(1192) == 1 && st.getQuestItemsCount(1193) < 1 && st.getQuestItemsCount(1200) > 0 && st.getQuestItemsCount(1197) < 1)
			{
				htmltext = "lemoniell_q0405_05.htm";
			}
		}
		else if(npcId == 30017 && st.getQuestItemsCount(1192) > 0)
		{
			if(cond == 4 && st.getQuestItemsCount(1193) > 0 && st.getQuestItemsCount(1197) < 1)
			{
				htmltext = "gallin_q0405_01.htm";
				st.takeItems(1193, -1);
				st.giveItems(1197, 1);
				st.setCond(5);
			}
			else if(cond == 5 && st.getQuestItemsCount(1193) < 1 && st.getQuestItemsCount(1197) > 0)
			{
				htmltext = "gallin_q0405_02.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == 20026 | npcId == 20029 && st.getCond() == 1 && st.getQuestItemsCount(1198) < 1)
		{
			st.giveItems(1198, 1);
			st.playSound("ItemSound.quest_middle");
		}
		return null;
	}
}