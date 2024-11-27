package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _411_PathToAssassin extends Quest implements ScriptFile
{
	public final int TRISKEL = 30416;
	public final int LEIKAN = 30382;
	public final int ARKENIA = 30419;
	public final int MOONSTONE_BEAST = 20369;
	public final int CALPICO = 27036;
	public final int SHILENS_CALL_ID = 1245;
	public final int ARKENIAS_LETTER_ID = 1246;
	public final int LEIKANS_NOTE_ID = 1247;
	public final int ONYX_BEASTS_MOLAR_ID = 1248;
	public final int LEIKANS_KNIFE_ID = 1249;
	public final int SHILENS_TEARS_ID = 1250;
	public final int ARKENIA_RECOMMEND_ID = 1251;
	public final int IRON_HEART_ID = 1252;
	
	public _411_PathToAssassin()
	{
		super(false);
		addStartNpc(30416);
		addTalkId(30382);
		addTalkId(30419);
		addKillId(20369);
		addKillId(27036);
		addQuestItem(1245, 1247, 1249, 1251, 1246, 1248, 1250);
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
			if(st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 31 && st.getQuestItemsCount(1252) < 1)
			{
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				st.giveItems(1245, 1);
				htmltext = "triskel_q0411_05.htm";
			}
			else if(st.getPlayer().getClassId().getId() != 31)
			{
				if(st.getPlayer().getClassId().getId() == 35)
				{
					htmltext = "triskel_q0411_02a.htm";
				}
				else
				{
					htmltext = "triskel_q0411_02.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(st.getPlayer().getLevel() < 18 && st.getPlayer().getClassId().getId() == 31)
			{
				htmltext = "triskel_q0411_03.htm";
				st.exitCurrentQuest(true);
			}
			else if(st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 31 && st.getQuestItemsCount(1252) > 0)
			{
				htmltext = "triskel_q0411_04.htm";
			}
		}
		else if(event.equalsIgnoreCase("30419_1"))
		{
			htmltext = "arkenia_q0411_05.htm";
			st.takeItems(1245, -1);
			st.giveItems(1246, 1);
			st.setCond(2);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("30382_1"))
		{
			htmltext = "guard_leikan_q0411_03.htm";
			st.takeItems(1246, -1);
			st.giveItems(1247, 1);
			st.setCond(3);
			st.playSound("ItemSound.quest_middle");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30416)
		{
			if(cond < 1)
			{
				htmltext = st.getQuestItemsCount(1252) < 1 ? "triskel_q0411_01.htm" : "triskel_q0411_04.htm";
			}
			else if(cond == 7)
			{
				htmltext = "triskel_q0411_06.htm";
				st.takeItems(1251, -1);
				if(st.getPlayer().getClassId().getLevel() == 1)
				{
					st.giveItems(1252, 1);
					if(!st.getPlayer().getVarB("prof1"))
					{
						st.getPlayer().setVar("prof1", "1", -1);
						st.addExpAndSp(3200, 3930);
					}
				}
				st.exitCurrentQuest(true);
				st.playSound("ItemSound.quest_finish");
			}
			else if(cond == 2)
			{
				htmltext = "triskel_q0411_07.htm";
			}
			else if(cond == 1)
			{
				htmltext = "triskel_q0411_11.htm";
			}
			else if(cond > 2 && cond < 7)
			{
				if(cond > 2 && cond < 5)
				{
					htmltext = "triskel_q0411_08.htm";
				}
				else if(cond > 4 && cond < 7)
				{
					htmltext = st.getQuestItemsCount(1250) < 1 ? "triskel_q0411_09.htm" : "triskel_q0411_10.htm";
				}
			}
		}
		else if(npcId == 30419)
		{
			if(cond == 1 && st.getQuestItemsCount(1245) > 0)
			{
				htmltext = "arkenia_q0411_01.htm";
			}
			else if(cond == 2 && st.getQuestItemsCount(1246) > 0)
			{
				htmltext = "arkenia_q0411_07.htm";
			}
			else if(cond > 2 && cond < 5 && st.getQuestItemsCount(1247) > 0)
			{
				htmltext = "arkenia_q0411_10.htm";
			}
			else if(cond == 5 && st.getQuestItemsCount(1249) > 0)
			{
				htmltext = "arkenia_q0411_11.htm";
			}
			else if(cond == 6 && st.getQuestItemsCount(1250) > 0)
			{
				htmltext = "arkenia_q0411_08.htm";
				st.takeItems(1250, -1);
				st.takeItems(1249, -1);
				st.giveItems(1251, 1);
				st.setCond(7);
				st.playSound("ItemSound.quest_middle");
			}
			else if(cond == 7)
			{
				htmltext = "arkenia_q0411_09.htm";
			}
		}
		else if(npcId == 30382)
		{
			if(cond == 2 && st.getQuestItemsCount(1246) > 0)
			{
				htmltext = "guard_leikan_q0411_01.htm";
			}
			else if(cond > 2 && cond < 4 && st.getQuestItemsCount(1248) < 1)
			{
				htmltext = "guard_leikan_q0411_05.htm";
				if(cond == 4)
				{
					st.setCond(3);
				}
			}
			else if(cond > 2 && cond < 4 && st.getQuestItemsCount(1248) < 10)
			{
				htmltext = "guard_leikan_q0411_06.htm";
				if(cond == 4)
				{
					st.setCond(3);
				}
			}
			else if(cond == 4 && st.getQuestItemsCount(1248) > 9)
			{
				htmltext = "guard_leikan_q0411_07.htm";
				st.takeItems(1248, -1);
				st.takeItems(1247, -1);
				st.giveItems(1249, 1);
				st.setCond(5);
				st.playSound("ItemSound.quest_middle");
			}
			else if(cond > 4 && cond < 7 && st.getQuestItemsCount(1250) < 1)
			{
				htmltext = "guard_leikan_q0411_09.htm";
				if(cond == 6)
				{
					st.setCond(5);
				}
			}
			else if(cond == 6 && st.getQuestItemsCount(1250) > 0)
			{
				htmltext = "guard_leikan_q0411_08.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 27036)
		{
			if(cond == 5 && st.getQuestItemsCount(1249) > 0 && st.getQuestItemsCount(1250) < 1)
			{
				st.giveItems(1250, 1);
				st.playSound("ItemSound.quest_middle");
				st.setCond(6);
			}
		}
		else if(npcId == 20369 && cond == 3 && st.getQuestItemsCount(1247) > 0 && st.getQuestItemsCount(1248) < 10)
		{
			st.giveItems(1248, 1);
			if(st.getQuestItemsCount(1248) > 9)
			{
				st.playSound("ItemSound.quest_middle");
				st.setCond(4);
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}