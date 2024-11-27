package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;

public class _403_PathToRogue extends Quest implements ScriptFile
{
	public final int BEZIQUE = 30379;
	public final int NETI = 30425;
	public final int TRACKER_SKELETON = 20035;
	public final int TRACKER_SKELETON_LEADER = 20042;
	public final int SKELETON_SCOUT = 20045;
	public final int SKELETON_BOWMAN = 20051;
	public final int RUIN_SPARTOI = 20054;
	public final int RAGING_SPARTOI = 20060;
	public final int CATS_EYE_BANDIT = 27038;
	public final int BEZIQUES_LETTER_ID = 1180;
	public final int SPATOIS_BONES_ID = 1183;
	public final int HORSESHOE_OF_LIGHT_ID = 1184;
	public final int WANTED_BILL_ID = 1185;
	public final int STOLEN_JEWELRY_ID = 1186;
	public final int STOLEN_TOMES_ID = 1187;
	public final int STOLEN_RING_ID = 1188;
	public final int STOLEN_NECKLACE_ID = 1189;
	public final int BEZIQUES_RECOMMENDATION_ID = 1190;
	public final int NETIS_BOW_ID = 1181;
	public final int NETIS_DAGGER_ID = 1182;
	public final int[][] MobsTable = {{20035, 2}, {20042, 3}, {20045, 2}, {20051, 2}, {20054, 8}, {20060, 8}};
	public final int[] STOLEN_ITEM = {1186, 1187, 1188, 1189};
	
	public _403_PathToRogue()
	{
		super(false);
		addStartNpc(30379);
		addTalkId(30425);
		addKillId(27038);
		addAttackId(27038);
		for(int[] element : MobsTable)
		{
			addKillId(element[0]);
			addAttackId(element[0]);
		}
		addQuestItem(STOLEN_ITEM);
		addQuestItem(1181, 1182, 1185, 1184, 1180, 1183);
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
		if(event.equalsIgnoreCase("30379_2"))
		{
			htmltext = st.getPlayer().getClassId().getId() == 0 ? st.getPlayer().getLevel() >= 18 ? st.getQuestItemsCount(1190) > 0 ? "captain_bezique_q0403_04.htm" : "captain_bezique_q0403_05.htm" : "captain_bezique_q0403_03.htm" : st.getPlayer().getClassId().getId() == 7 ? "captain_bezique_q0403_02a.htm" : "captain_bezique_q0403_02.htm";
		}
		else if(event.equalsIgnoreCase("1"))
		{
			st.setCond(1);
			st.setState(2);
			st.giveItems(1180, 1);
			htmltext = "captain_bezique_q0403_06.htm";
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30425_1"))
		{
			st.takeItems(1180, 1);
			if(st.getQuestItemsCount(1181) < 1)
			{
				st.giveItems(1181, 1);
			}
			if(st.getQuestItemsCount(1182) < 1)
			{
				st.giveItems(1182, 1);
			}
			st.setCond(2);
			htmltext = "neti_q0403_05.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30379)
		{
			if(cond == 6 && st.getQuestItemsCount(1184) < 1 && st.getQuestItemsCount(1186) + st.getQuestItemsCount(1187) + st.getQuestItemsCount(1188) + st.getQuestItemsCount(1189) == 4)
			{
				htmltext = "captain_bezique_q0403_09.htm";
				st.takeItems(1181, 1);
				st.takeItems(1182, 1);
				st.takeItems(1185, 1);
				for(int i : STOLEN_ITEM)
				{
					st.takeItems(i, -1);
				}
				if(st.getPlayer().getClassId().getLevel() == 1)
				{
					st.giveItems(1190, 1);
					if(!st.getPlayer().getVarB("prof1"))
					{
						st.getPlayer().setVar("prof1", "1", -1);
						st.addExpAndSp(3200, 1130);
					}
				}
				st.exitCurrentQuest(true);
				st.playSound("ItemSound.quest_finish");
			}
			else if(cond == 1 && st.getQuestItemsCount(1184) < 1 && st.getQuestItemsCount(1180) > 0)
			{
				htmltext = "captain_bezique_q0403_07.htm";
			}
			else if(cond == 4 && st.getQuestItemsCount(1184) > 0)
			{
				htmltext = "captain_bezique_q0403_08.htm";
				st.takeItems(1184, 1);
				st.giveItems(1185, 1);
				st.setCond(5);
			}
			else
			{
				htmltext = cond > 1 && st.getQuestItemsCount(1181) > 0 && st.getQuestItemsCount(1182) > 0 && st.getQuestItemsCount(1185) < 1 ? "captain_bezique_q0403_10.htm" : cond == 5 && st.getQuestItemsCount(1185) > 0 ? "captain_bezique_q0403_11.htm" : "captain_bezique_q0403_01.htm";
			}
		}
		else if(npcId == 30425)
		{
			if(cond == 1 && st.getQuestItemsCount(1180) > 0)
			{
				htmltext = "neti_q0403_01.htm";
			}
			else if(cond == 2 | cond == 3 && st.getQuestItemsCount(1183) < 10)
			{
				htmltext = "neti_q0403_06.htm";
				st.setCond(2);
			}
			else if(cond == 3 && st.getQuestItemsCount(1183) > 9)
			{
				htmltext = "neti_q0403_07.htm";
				st.takeItems(1183, -1);
				st.giveItems(1184, 1);
				st.setCond(4);
			}
			else if(cond == 4 && st.getQuestItemsCount(1184) > 0)
			{
				htmltext = "neti_q0403_08.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		int netis_cond = st.getInt("netis_cond");
		if(netis_cond == 1 && st.getItemEquipped(7) == 1181 || st.getItemEquipped(7) == 1182)
		{
			Functions.npcSay(npc, "I must do something about this shameful incident...");
			switch(cond)
			{
				case 2:
				{
					for(int[] element : MobsTable)
					{
						if(npcId != element[0] || !Rnd.chance(10 * element[1]) || st.getQuestItemsCount(1183) >= 10)
							continue;
						st.giveItems(1183, 1);
						if(st.getQuestItemsCount(1183) == 10)
						{
							st.playSound("ItemSound.quest_middle");
							st.setCond(3);
							continue;
						}
						st.playSound("ItemSound.quest_itemget");
					}
					break;
				}
				case 5:
				{
					int n;
					if(npcId != 27038 || st.getQuestItemsCount(1185) <= 0 || st.getQuestItemsCount(STOLEN_ITEM[n = Rnd.get(4)]) != 0)
						break;
					st.giveItems(STOLEN_ITEM[n], 1);
					if(st.getQuestItemsCount(1186) + st.getQuestItemsCount(1187) + st.getQuestItemsCount(1188) + st.getQuestItemsCount(1189) < 4)
					{
						st.playSound("ItemSound.quest_itemget");
						break;
					}
					st.playSound("ItemSound.quest_middle");
					st.setCond(6);
				}
			}
		}
		return null;
	}
	
	@Override
	public String onAttack(NpcInstance npc, QuestState st)
	{
		int netis_cond = st.getInt("netis_cond");
		if(st.getItemEquipped(7) != 1181 && st.getItemEquipped(7) != 1182)
		{
			st.set("netis_cond", "0");
		}
		else if(netis_cond == 0)
		{
			st.set("netis_cond", "1");
			Functions.npcSay(npc, "You childish fool, do you think you can catch me?");
		}
		return null;
	}
}