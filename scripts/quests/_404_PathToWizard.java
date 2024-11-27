package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _404_PathToWizard extends Quest implements ScriptFile
{
	public final int PARINA = 30391;
	public final int EARTH_SNAKE = 30409;
	public final int WASTELAND_LIZARDMAN = 30410;
	public final int FLAME_SALAMANDER = 30411;
	public final int WIND_SYLPH = 30412;
	public final int WATER_UNDINE = 30413;
	public final int RED_BEAR = 20021;
	public final int RATMAN_WARRIOR = 20359;
	public final int WATER_SEER = 27030;
	public final int MAP_OF_LUSTER_ID = 1280;
	public final int KEY_OF_FLAME_ID = 1281;
	public final int FLAME_EARING_ID = 1282;
	public final int BROKEN_BRONZE_MIRROR_ID = 1283;
	public final int WIND_FEATHER_ID = 1284;
	public final int WIND_BANGEL_ID = 1285;
	public final int RAMAS_DIARY_ID = 1286;
	public final int SPARKLE_PEBBLE_ID = 1287;
	public final int WATER_NECKLACE_ID = 1288;
	public final int RUST_GOLD_COIN_ID = 1289;
	public final int RED_SOIL_ID = 1290;
	public final int EARTH_RING_ID = 1291;
	public final int BEAD_OF_SEASON_ID = 1292;
	
	public _404_PathToWizard()
	{
		super(false);
		addStartNpc(30391);
		addTalkId(30409);
		addTalkId(30410);
		addTalkId(30411);
		addTalkId(30412);
		addTalkId(30413);
		addKillId(20021);
		addKillId(20359);
		addKillId(27030);
		addQuestItem(1281, 1280, 1284, 1283, 1287, 1286, 1290, 1289, 1282, 1285, 1288, 1291);
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
			if(st.getPlayer().getClassId().getId() == 10)
			{
				if(st.getPlayer().getLevel() >= 18)
				{
					if(st.getQuestItemsCount(1292) > 0)
					{
						htmltext = "parina_q0404_03.htm";
					}
					else
					{
						htmltext = "parina_q0404_08.htm";
						st.setCond(1);
						st.setState(2);
						st.playSound("ItemSound.quest_accept");
					}
				}
				else
				{
					htmltext = "parina_q0404_02.htm";
				}
			}
			else
			{
				htmltext = st.getPlayer().getClassId().getId() == 11 ? "parina_q0404_02a.htm" : "parina_q0404_01.htm";
			}
		}
		else if(event.equalsIgnoreCase("30410_1") && st.getQuestItemsCount(1284) < 1)
		{
			htmltext = "lizardman_of_wasteland_q0404_03.htm";
			st.giveItems(1284, 1);
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
		if(npcId == 30391)
		{
			if(cond == 0)
			{
				htmltext = "parina_q0404_04.htm";
			}
			else if(cond > 0 && st.getQuestItemsCount(1282) < 1 | st.getQuestItemsCount(1285) < 1 | st.getQuestItemsCount(1288) < 1 | st.getQuestItemsCount(1291) < 1)
			{
				htmltext = "parina_q0404_05.htm";
			}
			else if(cond > 0 && st.getQuestItemsCount(1282) > 0 && st.getQuestItemsCount(1285) > 0 && st.getQuestItemsCount(1288) > 0 && st.getQuestItemsCount(1291) > 0)
			{
				htmltext = "parina_q0404_06.htm";
				st.takeItems(1282, st.getQuestItemsCount(1282));
				st.takeItems(1285, st.getQuestItemsCount(1285));
				st.takeItems(1288, st.getQuestItemsCount(1288));
				st.takeItems(1291, st.getQuestItemsCount(1291));
				if(st.getPlayer().getClassId().getLevel() == 1)
				{
					if(st.getQuestItemsCount(1292) < 1)
					{
						st.giveItems(1292, 1);
					}
					if(!st.getPlayer().getVarB("prof1"))
					{
						st.getPlayer().setVar("prof1", "1", -1);
						st.addExpAndSp(3200, 2020);
					}
				}
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
		}
		else if(npcId == 30411)
		{
			if(cond > 0 && st.getQuestItemsCount(1280) < 1 && st.getQuestItemsCount(1282) < 1)
			{
				st.giveItems(1280, 1);
				htmltext = "flame_salamander_q0404_01.htm";
				st.setCond(2);
			}
			else if(cond > 0 && st.getQuestItemsCount(1280) > 0 && st.getQuestItemsCount(1281) < 1)
			{
				htmltext = "flame_salamander_q0404_02.htm";
			}
			else if(cond == 3 && st.getQuestItemsCount(1280) > 0 && st.getQuestItemsCount(1281) > 0)
			{
				st.takeItems(1281, -1);
				st.takeItems(1280, -1);
				if(st.getQuestItemsCount(1282) < 1)
				{
					st.giveItems(1282, 1);
				}
				htmltext = "flame_salamander_q0404_03.htm";
				st.setCond(4);
			}
			else if(cond > 0 && st.getQuestItemsCount(1282) > 0)
			{
				htmltext = "flame_salamander_q0404_04.htm";
			}
		}
		else if(npcId == 30412)
		{
			if(cond == 4 && st.getQuestItemsCount(1282) > 0 && st.getQuestItemsCount(1283) < 1 && st.getQuestItemsCount(1285) < 1)
			{
				st.giveItems(1283, 1);
				htmltext = "wind_sylph_q0404_01.htm";
				st.setCond(5);
			}
			else if(cond > 0 && st.getQuestItemsCount(1283) > 0 && st.getQuestItemsCount(1284) < 1)
			{
				htmltext = "wind_sylph_q0404_02.htm";
			}
			else if(cond > 0 && st.getQuestItemsCount(1283) > 0 && st.getQuestItemsCount(1284) > 0)
			{
				st.takeItems(1284, st.getQuestItemsCount(1284));
				st.takeItems(1283, st.getQuestItemsCount(1283));
				if(st.getQuestItemsCount(1285) < 1)
				{
					st.giveItems(1285, 1);
				}
				htmltext = "wind_sylph_q0404_03.htm";
				st.setCond(7);
			}
			else if(cond > 0 && st.getQuestItemsCount(1285) > 0)
			{
				htmltext = "wind_sylph_q0404_04.htm";
			}
		}
		else if(npcId == 30410)
		{
			if(cond > 0 && st.getQuestItemsCount(1283) > 0 && st.getQuestItemsCount(1284) < 1)
			{
				htmltext = "lizardman_of_wasteland_q0404_01.htm";
			}
			else if(cond > 0 && st.getQuestItemsCount(1283) > 0 && st.getQuestItemsCount(1284) > 0)
			{
				htmltext = "lizardman_of_wasteland_q0404_04.htm";
			}
		}
		else if(npcId == 30413)
		{
			if(cond == 7 && st.getQuestItemsCount(1285) > 0 && st.getQuestItemsCount(1286) < 1 && st.getQuestItemsCount(1288) < 1)
			{
				st.giveItems(1286, 1);
				htmltext = "water_undine_q0404_01.htm";
				st.setCond(8);
			}
			else if(cond > 0 && st.getQuestItemsCount(1286) > 0 && st.getQuestItemsCount(1287) < 2)
			{
				htmltext = "water_undine_q0404_02.htm";
			}
			else if(cond == 9 && st.getQuestItemsCount(1286) > 0 && st.getQuestItemsCount(1287) > 1)
			{
				st.takeItems(1287, -1);
				st.takeItems(1286, -1);
				if(st.getQuestItemsCount(1288) < 1)
				{
					st.giveItems(1288, 1);
				}
				htmltext = "water_undine_q0404_03.htm";
				st.setCond(10);
			}
			else if(cond > 0 && st.getQuestItemsCount(1288) > 0)
			{
				htmltext = "water_undine_q0404_04.htm";
			}
		}
		else if(npcId == 30409)
		{
			if(cond > 0 && st.getQuestItemsCount(1288) > 0 && st.getQuestItemsCount(1289) < 1 && st.getQuestItemsCount(1291) < 1)
			{
				st.giveItems(1289, 1);
				htmltext = "earth_snake_q0404_01.htm";
				st.setCond(11);
			}
			else if(cond > 0 && st.getQuestItemsCount(1289) > 0 && st.getQuestItemsCount(1290) < 1)
			{
				htmltext = "earth_snake_q0404_02.htm";
			}
			else if(cond == 12 && st.getQuestItemsCount(1289) > 0 && st.getQuestItemsCount(1290) > 0)
			{
				st.takeItems(1290, st.getQuestItemsCount(1290));
				st.takeItems(1289, st.getQuestItemsCount(1289));
				if(st.getQuestItemsCount(1291) < 1)
				{
					st.giveItems(1291, 1);
				}
				htmltext = "earth_snake_q0404_04.htm";
				st.setCond(13);
			}
			else if(cond > 0 && st.getQuestItemsCount(1291) > 0)
			{
				htmltext = "earth_snake_q0404_04.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 20359)
		{
			if(cond == 2)
			{
				st.giveItems(1281, 1);
				st.playSound("ItemSound.quest_middle");
				st.setCond(3);
			}
		}
		else if(npcId == 27030)
		{
			if(cond == 8 && st.getQuestItemsCount(1287) < 2)
			{
				st.giveItems(1287, 1);
				if(st.getQuestItemsCount(1287) == 2)
				{
					st.playSound("ItemSound.quest_middle");
					st.setCond(9);
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == 20021 && cond == 11)
		{
			st.giveItems(1290, 1);
			st.playSound("ItemSound.quest_middle");
			st.setCond(12);
		}
		return null;
	}
}