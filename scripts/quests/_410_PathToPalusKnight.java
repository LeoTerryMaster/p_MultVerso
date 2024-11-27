package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _410_PathToPalusKnight extends Quest implements ScriptFile
{
	public final int VIRGIL = 30329;
	public final int KALINTA = 30422;
	public final int POISON_SPIDER = 20038;
	public final int ARACHNID_TRACKER = 20043;
	public final int LYCANTHROPE = 20049;
	public final int PALLUS_TALISMAN_ID = 1237;
	public final int LYCANTHROPE_SKULL_ID = 1238;
	public final int VIRGILS_LETTER_ID = 1239;
	public final int MORTE_TALISMAN_ID = 1240;
	public final int PREDATOR_CARAPACE_ID = 1241;
	public final int TRIMDEN_SILK_ID = 1242;
	public final int COFFIN_ETERNAL_REST_ID = 1243;
	public final int GAZE_OF_ABYSS_ID = 1244;
	
	public _410_PathToPalusKnight()
	{
		super(false);
		addStartNpc(30329);
		addTalkId(30422);
		addKillId(20038);
		addKillId(20043);
		addKillId(20049);
		addQuestItem(1237, 1239, 1243, 1240, 1241, 1242, 1238);
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
		if(event.equalsIgnoreCase("1"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "master_virgil_q0410_06.htm";
			st.giveItems(1237, 1);
		}
		else if(event.equalsIgnoreCase("410_1"))
		{
			if(st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 31 && st.getQuestItemsCount(1244) == 0)
			{
				htmltext = "master_virgil_q0410_05.htm";
			}
			else if(st.getPlayer().getClassId().getId() != 31)
			{
				htmltext = st.getPlayer().getClassId().getId() == 32 ? "master_virgil_q0410_02a.htm" : "master_virgil_q0410_03.htm";
			}
			else if(st.getPlayer().getLevel() < 18 && st.getPlayer().getClassId().getId() == 31)
			{
				htmltext = "master_virgil_q0410_02.htm";
			}
			else if(st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 31 && st.getQuestItemsCount(1244) == 1)
			{
				htmltext = "master_virgil_q0410_04.htm";
			}
		}
		else if(event.equalsIgnoreCase("30329_2"))
		{
			htmltext = "master_virgil_q0410_10.htm";
			st.takeItems(1237, -1);
			st.takeItems(1238, -1);
			st.giveItems(1239, 1);
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("30422_1"))
		{
			htmltext = "kalinta_q0410_02.htm";
			st.takeItems(1239, -1);
			st.giveItems(1240, 1);
			st.setCond(4);
		}
		else if(event.equalsIgnoreCase("30422_2"))
		{
			htmltext = "kalinta_q0410_06.htm";
			st.takeItems(1240, -1);
			st.takeItems(1242, -1);
			st.takeItems(1241, -1);
			st.giveItems(1243, 1);
			st.setCond(6);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30329)
		{
			if(cond < 1)
			{
				htmltext = "master_virgil_q0410_01.htm";
			}
			else if(st.getQuestItemsCount(1237) > 0)
			{
				if(st.getQuestItemsCount(1238) < 1)
				{
					htmltext = "master_virgil_q0410_07.htm";
				}
				else if(st.getQuestItemsCount(1238) > 0 && st.getQuestItemsCount(1238) < 13)
				{
					htmltext = "master_virgil_q0410_08.htm";
				}
				else if(st.getQuestItemsCount(1238) > 12)
				{
					htmltext = "master_virgil_q0410_09.htm";
				}
			}
			else if(st.getQuestItemsCount(1243) > 0)
			{
				htmltext = "master_virgil_q0410_11.htm";
				st.takeItems(1243, -1);
				if(st.getPlayer().getClassId().getLevel() == 1)
				{
					st.giveItems(1244, 1);
					if(!st.getPlayer().getVarB("prof1"))
					{
						st.getPlayer().setVar("prof1", "1", -1);
						st.addExpAndSp(3200, 3050);
					}
				}
				st.exitCurrentQuest(true);
				st.playSound("ItemSound.quest_finish");
			}
			else if(st.getQuestItemsCount(1240) > 0 | st.getQuestItemsCount(1239) > 0)
			{
				htmltext = "master_virgil_q0410_12.htm";
			}
		}
		else if(npcId == 30422 && cond > 0)
		{
			if(st.getQuestItemsCount(1239) > 0)
			{
				htmltext = "kalinta_q0410_01.htm";
			}
			else if(st.getQuestItemsCount(1240) > 0)
			{
				if(st.getQuestItemsCount(1242) < 1 && st.getQuestItemsCount(1241) < 1)
				{
					htmltext = "kalinta_q0410_03.htm";
				}
				else if(st.getQuestItemsCount(1242) < 1 | st.getQuestItemsCount(1241) < 1)
				{
					htmltext = "kalinta_q0410_04.htm";
				}
				else if(st.getQuestItemsCount(1242) > 4 && st.getQuestItemsCount(1241) > 0)
				{
					htmltext = "kalinta_q0410_05.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 20049)
		{
			if(cond == 1 && st.getQuestItemsCount(1237) > 0 && st.getQuestItemsCount(1238) < 13)
			{
				st.giveItems(1238, 1);
				if(st.getQuestItemsCount(1238) > 12)
				{
					st.playSound("ItemSound.quest_middle");
					st.setCond(2);
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 20038)
		{
			if(cond == 4 && st.getQuestItemsCount(1240) > 0 && st.getQuestItemsCount(1241) < 1)
			{
				st.giveItems(1241, 1);
				st.playSound("ItemSound.quest_middle");
				if(st.getQuestItemsCount(1242) > 4)
				{
					st.setCond(5);
				}
			}
		}
		else if(npcId == 20043 && cond == 4 && st.getQuestItemsCount(1240) > 0 && st.getQuestItemsCount(1242) < 5)
		{
			st.giveItems(1242, 1);
			if(st.getQuestItemsCount(1242) > 4)
			{
				st.playSound("ItemSound.quest_middle");
				if(st.getQuestItemsCount(1241) > 0)
				{
					st.setCond(5);
				}
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}