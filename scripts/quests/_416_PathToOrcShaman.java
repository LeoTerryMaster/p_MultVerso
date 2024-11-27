package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;

public class _416_PathToOrcShaman extends Quest implements ScriptFile
{
	private static final int Hestui = 30585;
	private static final int HestuiTotemSpirit = 30592;
	private static final int SeerUmos = 30502;
	private static final int DudaMaraTotemSpirit = 30593;
	private static final int SeerMoira = 31979;
	private static final int GandiTotemSpirit = 32057;
	private static final int LeopardCarcass = 32090;
	private static final int FireCharm = 1616;
	private static final int KashaBearPelt = 1617;
	private static final int KashaBladeSpiderHusk = 1618;
	private static final int FieryEgg1st = 1619;
	private static final int HestuiMask = 1620;
	private static final int FieryEgg2nd = 1621;
	private static final int TotemSpiritClaw = 1622;
	private static final int TatarusLetterOfRecommendation = 1623;
	private static final int FlameCharm = 1624;
	private static final int GrizzlyBlood = 1625;
	private static final int BloodCauldron = 1626;
	private static final int SpiritNet = 1627;
	private static final int BoundDurkaSpirit = 1628;
	private static final int DurkaParasite = 1629;
	private static final int TotemSpiritBlood = 1630;
	private static final int MaskOfMedium = 1631;
	private static final int KashaBear = 20479;
	private static final int KashaBladeSpider = 20478;
	private static final int ScarletSalamander = 20415;
	private static final int GrizzlyBear = 20335;
	private static final int VenomousSpider = 20038;
	private static final int ArachnidTracker = 20043;
	private static final int QuestMonsterDurkaSpirit = 27056;
	private static final int QuestBlackLeopard = 27319;
	private static final int[][] DROPLIST_COND = {{1, 0, 20479, 1616, 1617, 1, 70, 1}, {1, 0, 20478, 1616, 1618, 1, 70, 1}, {1, 0, 20415, 1616, 1619, 1, 70, 1}, {6, 7, 20335, 1624, 1625, 3, 70, 1}};
	
	public _416_PathToOrcShaman()
	{
		super(false);
		addStartNpc(30585);
		addTalkId(30592, 30502, 30593, 31979, 32057, 32090);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
			addQuestItem(DROPLIST_COND[i][4]);
		}
		addKillId(20038, 20043, 27056, 27319);
		addQuestItem(1616, 1620, 1621, 1622, 1623, 1624, 1626, 1627, 1628, 1629, 1630);
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
		if(event.equalsIgnoreCase("tataru_zu_hestui_q0416_06.htm"))
		{
			st.giveItems(1616, 1);
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("hestui_totem_spirit_q0416_03.htm"))
		{
			st.takeItems(1620, -1);
			st.takeItems(1621, -1);
			st.giveItems(1622, 1);
			st.setCond(4);
		}
		else if(event.equalsIgnoreCase("tataru_zu_hestui_q0416_11.htm"))
		{
			st.takeItems(1622, -1);
			st.giveItems(1623, 1);
			st.setCond(5);
		}
		else if(event.equalsIgnoreCase("tataru_zu_hestui_q0416_11c.htm"))
		{
			st.takeItems(1622, -1);
			st.setCond(12);
		}
		else if(event.equalsIgnoreCase("dudamara_totem_spirit_q0416_03.htm"))
		{
			st.takeItems(1626, -1);
			st.giveItems(1627, 1);
			st.setCond(9);
		}
		else if(event.equalsIgnoreCase("seer_umos_q0416_07.htm"))
		{
			st.takeItems(1630, -1);
			if(st.getPlayer().getClassId().getLevel() == 1)
			{
				st.giveItems(1631, 1);
				if(!st.getPlayer().getVarB("prof1"))
				{
					st.getPlayer().setVar("prof1", "1", -1);
					st.addExpAndSp(3200, 2600);
				}
			}
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		else if(event.equalsIgnoreCase("totem_spirit_gandi_q0416_02.htm"))
		{
			st.setCond(14);
		}
		else if(event.equalsIgnoreCase("dead_leopard_q0416_04.htm"))
		{
			st.setCond(18);
		}
		else if(event.equalsIgnoreCase("totem_spirit_gandi_q0416_05.htm"))
		{
			st.setCond(21);
		}
		if(event.equalsIgnoreCase("QuestMonsterDurkaSpirit_Fail"))
		{
			for(NpcInstance n : GameObjectsStorage.getAllByNpcId(27056, false))
			{
				n.deleteMe();
			}
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30585)
		{
			if(st.getQuestItemsCount(1631) != 0)
			{
				htmltext = "seer_umos_q0416_04.htm";
				st.exitCurrentQuest(true);
			}
			else if(cond == 0)
			{
				if(st.getPlayer().getClassId().getId() != 49)
				{
					htmltext = st.getPlayer().getClassId().getId() == 50 ? "tataru_zu_hestui_q0416_02a.htm" : "tataru_zu_hestui_q0416_02.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getPlayer().getLevel() < 18)
				{
					htmltext = "tataru_zu_hestui_q0416_03.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "tataru_zu_hestui_q0416_01.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "tataru_zu_hestui_q0416_07.htm";
			}
			else if(cond == 2)
			{
				htmltext = "tataru_zu_hestui_q0416_08.htm";
				st.takeItems(1617, -1);
				st.takeItems(1618, -1);
				st.takeItems(1619, -1);
				st.takeItems(1616, -1);
				st.giveItems(1620, 1);
				st.giveItems(1621, 1);
				st.setCond(3);
			}
			else if(cond == 3)
			{
				htmltext = "tataru_zu_hestui_q0416_09.htm";
			}
			else if(cond == 4)
			{
				htmltext = "tataru_zu_hestui_q0416_10.htm";
			}
			else if(cond == 5)
			{
				htmltext = "tataru_zu_hestui_q0416_12.htm";
			}
			else if(cond > 5)
			{
				htmltext = "tataru_zu_hestui_q0416_13.htm";
			}
		}
		else if(npcId == 30592)
		{
			if(cond == 3)
			{
				htmltext = "hestui_totem_spirit_q0416_01.htm";
			}
			else if(cond == 4)
			{
				htmltext = "hestui_totem_spirit_q0416_04.htm";
			}
		}
		else if(npcId == 30592 && st.getCond() > 0 && (st.getQuestItemsCount(1625) > 0 || st.getQuestItemsCount(1624) > 0 || st.getQuestItemsCount(1626) > 0 || st.getQuestItemsCount(1627) > 0 || st.getQuestItemsCount(1628) > 0 || st.getQuestItemsCount(1630) > 0 || st.getQuestItemsCount(1623) > 0))
		{
			htmltext = "hestui_totem_spirit_q0416_05.htm";
		}
		else if(npcId == 30502)
		{
			if(cond == 5)
			{
				st.takeItems(1623, -1);
				st.giveItems(1624, 1);
				htmltext = "seer_umos_q0416_01.htm";
				st.setCond(6);
			}
			else if(cond == 6)
			{
				htmltext = "seer_umos_q0416_02.htm";
			}
			else if(cond == 7)
			{
				st.takeItems(1625, -1);
				st.takeItems(1624, -1);
				st.giveItems(1626, 1);
				htmltext = "seer_umos_q0416_03.htm";
				st.setCond(8);
			}
			else if(cond == 8)
			{
				htmltext = "seer_umos_q0416_04.htm";
			}
			else if(cond == 9 || cond == 10)
			{
				htmltext = "seer_umos_q0416_05.htm";
			}
			else if(cond == 11)
			{
				htmltext = "seer_umos_q0416_06.htm";
			}
		}
		else if(npcId == 31979)
		{
			if(cond == 12)
			{
				htmltext = "seer_moirase_q0416_01.htm";
				st.setCond(13);
			}
			else if(cond > 12 && cond < 21)
			{
				htmltext = "seer_moirase_q0416_02.htm";
			}
			else if(cond == 21)
			{
				htmltext = "seer_moirase_q0416_03.htm";
				if(st.getPlayer().getClassId().getLevel() == 1)
				{
					st.giveItems(1631, 1);
					if(!st.getPlayer().getVarB("prof1"))
					{
						st.getPlayer().setVar("prof1", "1", -1);
						st.addExpAndSp(295862, 18194);
					}
				}
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
		}
		else if(npcId == 32057)
		{
			if(cond == 13)
			{
				htmltext = "totem_spirit_gandi_q0416_01.htm";
			}
			else if(cond > 13 && cond < 20)
			{
				htmltext = "totem_spirit_gandi_q0416_03.htm";
			}
			else if(cond == 20)
			{
				htmltext = "totem_spirit_gandi_q0416_04.htm";
			}
		}
		else if(npcId == 32090)
		{
			if(cond <= 14)
			{
				htmltext = "dead_leopard_q0416_01a.htm";
			}
			else if(cond == 15)
			{
				htmltext = "dead_leopard_q0416_01.htm";
				st.setCond(16);
			}
			else if(cond == 16)
			{
				htmltext = "dead_leopard_q0416_01.htm";
			}
			else if(cond == 17)
			{
				htmltext = "dead_leopard_q0416_02.htm";
			}
			else if(cond == 18)
			{
				htmltext = "dead_leopard_q0416_05.htm";
			}
			else if(cond == 19)
			{
				htmltext = "dead_leopard_q0416_06.htm";
				st.setCond(20);
			}
			else
			{
				htmltext = "dead_leopard_q0416_06.htm";
			}
		}
		else if(npcId == 30593)
		{
			if(cond == 8)
			{
				htmltext = "dudamara_totem_spirit_q0416_01.htm";
			}
			else if(cond == 9)
			{
				htmltext = "dudamara_totem_spirit_q0416_04.htm";
			}
			else if(cond == 10)
			{
				st.takeItems(1628, -1);
				st.giveItems(1630, 1);
				htmltext = "dudamara_totem_spirit_q0416_05.htm";
				st.setCond(11);
			}
			else if(cond == 11)
			{
				htmltext = "dudamara_totem_spirit_q0416_06.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			if(cond != DROPLIST_COND[i][0] || npcId != DROPLIST_COND[i][2] || DROPLIST_COND[i][3] != 0 && st.getQuestItemsCount(DROPLIST_COND[i][3]) <= 0)
				continue;
			if(DROPLIST_COND[i][5] == 0)
			{
				st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], (double) DROPLIST_COND[i][6]);
				continue;
			}
			if(!st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], DROPLIST_COND[i][7], DROPLIST_COND[i][5], (double) DROPLIST_COND[i][6]) || DROPLIST_COND[i][1] == cond || DROPLIST_COND[i][1] == 0)
				continue;
			st.setCond(DROPLIST_COND[i][1]);
		}
		if(st.getQuestItemsCount(1617) != 0 && st.getQuestItemsCount(1618) != 0 && st.getQuestItemsCount(1619) != 0)
		{
			st.setCond(2);
		}
		else if(cond == 9 && (npcId == 20038 || npcId == 20043))
		{
			if(st.getQuestItemsCount(1629) < 8)
			{
				st.giveItems(1629, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			if((st.getQuestItemsCount(1629) == 8 || st.getQuestItemsCount(1629) >= 5 && Rnd.chance((double) (st.getQuestItemsCount(1629) * 10))) && GameObjectsStorage.getByNpcId(27056) == null)
			{
				st.takeItems(1629, -1);
				st.addSpawn(27056);
				st.startQuestTimer("QuestMonsterDurkaSpirit_Fail", 300000);
			}
		}
		else if(npcId == 27056)
		{
			st.cancelQuestTimer("QuestMonsterDurkaSpirit_Fail");
			for(NpcInstance qnpc : GameObjectsStorage.getAllByNpcId(27056, false))
			{
				qnpc.deleteMe();
			}
			if(cond == 9)
			{
				st.takeItems(1627, -1);
				st.takeItems(1629, -1);
				st.giveItems(1628, 1);
				st.playSound("ItemSound.quest_middle");
				st.setCond(10);
			}
		}
		else if(npcId == 27319)
		{
			if(cond == 14 && Rnd.chance(50))
			{
				Functions.npcSayCustomMessage(GameObjectsStorage.getByNpcId(32090), new CustomMessage("quests._416_PathToOrcShaman.LeopardCarcass", st.getPlayer(), new Object[0]).toString(), (Object[]) new Object[] {st.getPlayer()});
				st.setCond(15);
			}
			else if(cond == 16 && Rnd.chance(50))
			{
				st.setCond(17);
			}
			else if(cond == 18 && Rnd.chance(50))
			{
				st.setCond(19);
			}
		}
		return null;
	}
}