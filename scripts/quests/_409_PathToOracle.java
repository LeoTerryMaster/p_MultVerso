package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _409_PathToOracle extends Quest implements ScriptFile
{
	public final int MANUEL = 30293;
	public final int ALLANA = 30424;
	public final int PERRIN = 30428;
	public final int LIZARDMAN_WARRIOR = 27032;
	public final int LIZARDMAN_SCOUT = 27033;
	public final int LIZARDMAN = 27034;
	public final int TAMIL = 27035;
	public final int CRYSTAL_MEDALLION_ID = 1231;
	public final int MONEY_OF_SWINDLER_ID = 1232;
	public final int DAIRY_OF_ALLANA_ID = 1233;
	public final int LIZARD_CAPTAIN_ORDER_ID = 1234;
	public final int LEAF_OF_ORACLE_ID = 1235;
	public final int HALF_OF_DAIRY_ID = 1236;
	public final int TAMATOS_NECKLACE_ID = 1275;
	public final int weapon_exchange_coupon_d_grade = 8593;
	
	public _409_PathToOracle()
	{
		super(false);
		addStartNpc(30293);
		addTalkId(30424);
		addTalkId(30428);
		addKillId(27032);
		addKillId(27033);
		addKillId(27034);
		addKillId(27035);
		addQuestItem(1232, 1233, 1234, 1231, 1236, 1275);
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
			if(event.equalsIgnoreCase("1"))
			{
				if(st.getPlayer().getClassId().getId() != 25)
				{
					htmltext = st.getPlayer().getClassId().getId() == 29 ? "father_manuell_q0409_02a.htm" : "father_manuell_q0409_02.htm";
				}
				else if(st.getPlayer().getLevel() < 18)
				{
					htmltext = "father_manuell_q0409_03.htm";
				}
				else if(st.getQuestItemsCount(1235) > 0)
				{
					htmltext = "father_manuell_q0409_04.htm";
				}
				else
				{
					st.setCond(1);
					st.setState(2);
					st.playSound("ItemSound.quest_accept");
					st.giveItems(1231, 1);
					htmltext = "father_manuell_q0409_05.htm";
				}
			}
		}
		else if(event.equalsIgnoreCase("allana_q0409_08.htm"))
		{
			st.addSpawn(27032);
			st.addSpawn(27033);
			st.addSpawn(27034);
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("30424_1"))
		{
			htmltext = "";
		}
		else if(event.equalsIgnoreCase("30428_1"))
		{
			htmltext = "perrin_q0409_02.htm";
		}
		else if(event.equalsIgnoreCase("30428_2"))
		{
			htmltext = "perrin_q0409_03.htm";
		}
		else if(event.equalsIgnoreCase("30428_3"))
		{
			st.addSpawn(27035);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30293)
		{
			if(cond < 1)
			{
				htmltext = "father_manuell_q0409_01.htm";
			}
			else if(st.getQuestItemsCount(1231) > 0)
			{
				if(st.getQuestItemsCount(1232) < 1 && st.getQuestItemsCount(1233) < 1 && st.getQuestItemsCount(1234) < 1 && st.getQuestItemsCount(1236) < 1)
				{
					htmltext = "father_manuell_q0409_09.htm";
				}
				else if(st.getQuestItemsCount(1232) > 0 && st.getQuestItemsCount(1233) > 0 && st.getQuestItemsCount(1234) > 0 && st.getQuestItemsCount(1236) < 1)
				{
					htmltext = "father_manuell_q0409_08.htm";
					st.takeItems(1232, 1);
					st.takeItems(1233, -1);
					st.takeItems(1234, -1);
					st.takeItems(1231, -1);
					if(st.getPlayer().getClassId().getLevel() == 1)
					{
						st.giveItems(1235, 1);
						if(!st.getPlayer().getVarB("prof1"))
						{
							st.getPlayer().setVar("prof1", "1", -1);
							st.addExpAndSp(3200, 1890);
							st.giveItems(8593, 15);
						}
					}
					st.playSound("ItemSound.quest_finish");
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "father_manuell_q0409_07.htm";
				}
			}
		}
		else if(npcId == 30424)
		{
			if(st.getQuestItemsCount(1231) > 0)
			{
				if(st.getQuestItemsCount(1232) < 1 && st.getQuestItemsCount(1233) < 1 && st.getQuestItemsCount(1234) < 1 && st.getQuestItemsCount(1236) < 1)
				{
					htmltext = cond > 2 ? "allana_q0409_05.htm" : "allana_q0409_01.htm";
				}
				else if(st.getQuestItemsCount(1232) < 1 && st.getQuestItemsCount(1233) < 1 && st.getQuestItemsCount(1234) > 0 && st.getQuestItemsCount(1236) < 1)
				{
					htmltext = "allana_q0409_02.htm";
					st.giveItems(1236, 1);
					st.setCond(4);
				}
				else if(st.getQuestItemsCount(1232) < 1 && st.getQuestItemsCount(1233) < 1 && st.getQuestItemsCount(1234) > 0 && st.getQuestItemsCount(1236) > 0)
				{
					htmltext = st.getQuestItemsCount(1275) < 1 ? "allana_q0409_06.htm" : "allana_q0409_03.htm";
				}
				else if(st.getQuestItemsCount(1232) > 0 && st.getQuestItemsCount(1233) < 1 && st.getQuestItemsCount(1234) > 0 && st.getQuestItemsCount(1236) > 0)
				{
					htmltext = "allana_q0409_04.htm";
					st.takeItems(1236, -1);
					st.giveItems(1233, 1);
					st.setCond(7);
				}
				else if(st.getQuestItemsCount(1232) > 0 && st.getQuestItemsCount(1234) > 0 && st.getQuestItemsCount(1236) < 1 && st.getQuestItemsCount(1233) > 0)
				{
					htmltext = "allana_q0409_05.htm";
				}
			}
		}
		else if(npcId == 30428 && st.getQuestItemsCount(1231) > 0 && st.getQuestItemsCount(1234) > 0)
		{
			if(st.getQuestItemsCount(1275) > 0)
			{
				htmltext = "perrin_q0409_04.htm";
				st.takeItems(1275, -1);
				st.giveItems(1232, 1);
				st.setCond(6);
			}
			else
			{
				htmltext = st.getQuestItemsCount(1232) > 0 ? "perrin_q0409_05.htm" : cond > 4 ? "perrin_q0409_06.htm" : "perrin_q0409_01.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 27032 | npcId == 27033 | npcId == 27034)
		{
			if(cond == 2 && st.getQuestItemsCount(1234) < 1)
			{
				st.giveItems(1234, 1);
				st.playSound("ItemSound.quest_middle");
				st.setCond(3);
			}
		}
		else if(npcId == 27035 && cond == 4 && st.getQuestItemsCount(1275) < 1)
		{
			st.giveItems(1275, 1);
			st.playSound("ItemSound.quest_middle");
			st.setCond(5);
		}
		return null;
	}
}