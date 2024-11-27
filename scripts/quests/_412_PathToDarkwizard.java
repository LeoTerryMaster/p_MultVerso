package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _412_PathToDarkwizard extends Quest implements ScriptFile
{
	public final int CHARKEREN = 30415;
	public final int ANNIKA = 30418;
	public final int ARKENIA = 30419;
	public final int VARIKA = 30421;
	public final int MARSH_ZOMBIE = 20015;
	public final int MARSH_ZOMBIE_LORD = 20020;
	public final int MISERY_SKELETON = 20022;
	public final int SKELETON_SCOUT = 20045;
	public final int SKELETON_HUNTER = 20517;
	public final int SKELETON_HUNTER_ARCHER = 20518;
	public final int SEEDS_OF_DESPAIR_ID = 1254;
	public final int SEEDS_OF_ANGER_ID = 1253;
	public final int SEEDS_OF_HORROR_ID = 1255;
	public final int SEEDS_OF_LUNACY_ID = 1256;
	public final int FAMILYS_ASHES_ID = 1257;
	public final int KNEE_BONE_ID = 1259;
	public final int HEART_OF_LUNACY_ID = 1260;
	public final int JEWEL_OF_DARKNESS_ID = 1261;
	public final int LUCKY_KEY_ID = 1277;
	public final int CANDLE_ID = 1278;
	public final int HUB_SCENT_ID = 1279;
	public final int[][] DROPLIST = {{20015, 1277, 1257, 3}, {20020, 1277, 1257, 3}, {20517, 1278, 1259, 2}, {20518, 1278, 1259, 2}, {20022, 1278, 1259, 2}, {20045, 1279, 1260, 3}};
	
	public _412_PathToDarkwizard()
	{
		super(false);
		addStartNpc(30421);
		addTalkId(30415);
		addTalkId(30418);
		addTalkId(30419);
		addQuestItem(1253);
		addQuestItem(1277);
		addQuestItem(1255);
		addQuestItem(1278);
		addQuestItem(1256);
		addQuestItem(1279);
		addQuestItem(1254);
		addQuestItem(1257);
		addQuestItem(1259);
		addQuestItem(1260);
		for(int[] element : DROPLIST)
		{
			addKillId(element[0]);
		}
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
			if(st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 38 && st.getQuestItemsCount(1261) < 1)
			{
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				st.giveItems(1254, 1);
				htmltext = "varika_q0412_05.htm";
			}
			else if(st.getPlayer().getClassId().getId() != 38)
			{
				htmltext = st.getPlayer().getClassId().getId() == 39 ? "varika_q0412_02a.htm" : "varika_q0412_03.htm";
			}
			else if(st.getPlayer().getLevel() < 18 && st.getPlayer().getClassId().getId() == 38)
			{
				htmltext = "varika_q0412_02.htm";
			}
			else if(st.getPlayer().getLevel() >= 18 && st.getPlayer().getClassId().getId() == 38 && st.getQuestItemsCount(1261) > 0)
			{
				htmltext = "varika_q0412_04.htm";
			}
		}
		else if(event.equalsIgnoreCase("412_1"))
		{
			htmltext = st.getQuestItemsCount(1253) > 0 ? "varika_q0412_06.htm" : "varika_q0412_07.htm";
		}
		else if(event.equalsIgnoreCase("412_2"))
		{
			htmltext = st.getQuestItemsCount(1255) > 0 ? "varika_q0412_09.htm" : "varika_q0412_10.htm";
		}
		else if(event.equalsIgnoreCase("412_3"))
		{
			if(st.getQuestItemsCount(1256) > 0)
			{
				htmltext = "varika_q0412_12.htm";
			}
			else if(st.getQuestItemsCount(1256) < 1 && st.getQuestItemsCount(1254) > 0)
			{
				htmltext = "varika_q0412_13.htm";
			}
		}
		else if(event.equalsIgnoreCase("412_4"))
		{
			htmltext = "charkeren_q0412_03.htm";
			st.giveItems(1277, 1);
		}
		else if(event.equalsIgnoreCase("30418_1"))
		{
			htmltext = "annsery_q0412_02.htm";
			st.giveItems(1278, 1);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30421)
		{
			if(cond < 1)
			{
				htmltext = st.getQuestItemsCount(1261) < 1 ? "varika_q0412_01.htm" : "varika_q0412_04.htm";
			}
			else if(st.getQuestItemsCount(1254) > 0 && st.getQuestItemsCount(1255) > 0 && st.getQuestItemsCount(1256) > 0 && st.getQuestItemsCount(1253) > 0)
			{
				htmltext = "varika_q0412_16.htm";
				if(st.getPlayer().getClassId().getLevel() == 1)
				{
					st.giveItems(1261, 1);
					if(!st.getPlayer().getVarB("prof1"))
					{
						st.getPlayer().setVar("prof1", "1", -1);
						st.addExpAndSp(3200, 1650);
					}
				}
				st.exitCurrentQuest(true);
				st.playSound("ItemSound.quest_finish");
			}
			else if(st.getQuestItemsCount(1254) > 0)
			{
				if(st.getQuestItemsCount(1257) < 1 && st.getQuestItemsCount(1277) < 1 && st.getQuestItemsCount(1278) < 1 && st.getQuestItemsCount(1279) < 1 && st.getQuestItemsCount(1259) < 1 && st.getQuestItemsCount(1260) < 1)
				{
					htmltext = "varika_q0412_17.htm";
				}
				else if(st.getQuestItemsCount(1253) < 1)
				{
					htmltext = "varika_q0412_08.htm";
				}
				else if(st.getQuestItemsCount(1255) > 0)
				{
					htmltext = "varika_q0412_19.htm";
				}
				else if(st.getQuestItemsCount(1260) < 1)
				{
					htmltext = "varika_q0412_13.htm";
				}
			}
		}
		else if(npcId == 30419 && cond > 0 && st.getQuestItemsCount(1256) < 1)
		{
			if(st.getQuestItemsCount(1279) < 1 && st.getQuestItemsCount(1260) < 1)
			{
				htmltext = "arkenia_q0412_01.htm";
				st.giveItems(1279, 1);
			}
			else if(st.getQuestItemsCount(1279) > 0 && st.getQuestItemsCount(1260) < 3)
			{
				htmltext = "arkenia_q0412_02.htm";
			}
			else if(st.getQuestItemsCount(1279) > 0 && st.getQuestItemsCount(1260) >= 3)
			{
				htmltext = "arkenia_q0412_03.htm";
				st.giveItems(1256, 1);
				st.takeItems(1260, -1);
				st.takeItems(1279, -1);
			}
		}
		else if(npcId == 30415 && cond > 0)
		{
			if(st.getQuestItemsCount(1253) < 1)
			{
				if(st.getQuestItemsCount(1254) > 0 && st.getQuestItemsCount(1257) < 1 && st.getQuestItemsCount(1277) < 1)
				{
					htmltext = "charkeren_q0412_01.htm";
				}
				else if(st.getQuestItemsCount(1254) > 0 && st.getQuestItemsCount(1257) < 3 && st.getQuestItemsCount(1277) > 0)
				{
					htmltext = "charkeren_q0412_04.htm";
				}
				else if(st.getQuestItemsCount(1254) > 0 && st.getQuestItemsCount(1257) >= 3 && st.getQuestItemsCount(1277) > 0)
				{
					htmltext = "charkeren_q0412_05.htm";
					st.giveItems(1253, 1);
					st.takeItems(1257, -1);
					st.takeItems(1277, -1);
				}
			}
			else
			{
				htmltext = "charkeren_q0412_06.htm";
			}
		}
		else if(npcId == 30418 && cond > 0 && st.getQuestItemsCount(1255) < 1)
		{
			if(st.getQuestItemsCount(1254) > 0 && st.getQuestItemsCount(1278) < 1 && st.getQuestItemsCount(1259) < 1)
			{
				htmltext = "annsery_q0412_01.htm";
			}
			else if(st.getQuestItemsCount(1254) > 0 && st.getQuestItemsCount(1278) > 0 && st.getQuestItemsCount(1259) < 2)
			{
				htmltext = "annsery_q0412_03.htm";
			}
			else if(st.getQuestItemsCount(1254) > 0 && st.getQuestItemsCount(1278) > 0 && st.getQuestItemsCount(1259) >= 2)
			{
				htmltext = "annsery_q0412_04.htm";
				st.giveItems(1255, 1);
				st.takeItems(1278, -1);
				st.takeItems(1259, -1);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		for(int[] element : DROPLIST)
		{
			if(st.getCond() != 1 || npc.getNpcId() != element[0] || st.getQuestItemsCount(element[1]) <= 0 || !Rnd.chance(50) || st.getQuestItemsCount(element[2]) >= (long) element[3])
				continue;
			st.giveItems(element[2], 1);
			if(st.getQuestItemsCount(element[2]) == (long) element[3])
			{
				st.playSound("ItemSound.quest_middle");
				continue;
			}
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}