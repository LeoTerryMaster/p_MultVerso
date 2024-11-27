package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _402_PathToKnight extends Quest implements ScriptFile
{
	public final int SIR_KLAUS_VASPER = 30417;
	public final int BIOTIN = 30031;
	public final int LEVIAN = 30037;
	public final int GILBERT = 30039;
	public final int RAYMOND = 30289;
	public final int SIR_COLLIN_WINDAWOOD = 30311;
	public final int BATHIS = 30332;
	public final int BEZIQUE = 30379;
	public final int SIR_ARON_TANFORD = 30653;
	public final int BUGBEAR_RAIDER = 20775;
	public final int UNDEAD_PRIEST = 27024;
	public final int POISON_SPIDER = 20038;
	public final int ARACHNID_TRACKER = 20043;
	public final int ARACHNID_PREDATOR = 20050;
	public final int LANGK_LIZARDMAN = 20030;
	public final int LANGK_LIZARDMAN_SCOUT = 20027;
	public final int LANGK_LIZARDMAN_WARRIOR = 20024;
	public final int GIANT_SPIDER = 20103;
	public final int TALON_SPIDER = 20106;
	public final int BLADE_SPIDER = 20108;
	public final int SILENT_HORROR = 20404;
	public final int SWORD_OF_RITUAL = 1161;
	public final int COIN_OF_LORDS1 = 1162;
	public final int COIN_OF_LORDS2 = 1163;
	public final int COIN_OF_LORDS3 = 1164;
	public final int COIN_OF_LORDS4 = 1165;
	public final int COIN_OF_LORDS5 = 1166;
	public final int COIN_OF_LORDS6 = 1167;
	public final int GLUDIO_GUARDS_MARK1 = 1168;
	public final int BUGBEAR_NECKLACE = 1169;
	public final int EINHASAD_CHURCH_MARK1 = 1170;
	public final int EINHASAD_CRUCIFIX = 1171;
	public final int GLUDIO_GUARDS_MARK2 = 1172;
	public final int POISON_SPIDER_LEG1 = 1173;
	public final int EINHASAD_CHURCH_MARK2 = 1174;
	public final int LIZARDMAN_TOTEM = 1175;
	public final int GLUDIO_GUARDS_MARK3 = 1176;
	public final int GIANT_SPIDER_HUSK = 1177;
	public final int EINHASAD_CHURCH_MARK3 = 1178;
	public final int HORRIBLE_SKULL = 1179;
	public final int MARK_OF_ESQUIRE = 1271;
	public final int[][] DROPLIST = {{20775, 1168, 1169, 10, 100}, {27024, 1170, 1171, 12, 100}, {20038, 1172, 1173, 20, 100}, {20043, 1172, 1173, 20, 100}, {20050, 1172, 1173, 20, 100}, {20030, 1174, 1175, 20, 50}, {20027, 1174, 1175, 20, 100}, {20024, 1174, 1175, 20, 100}, {20103, 1176, 1177, 20, 40}, {20106, 1176, 1177, 20, 40}, {20108, 1176, 1177, 20, 40}, {20404, 1178, 1179, 10, 100}};
	
	public _402_PathToKnight()
	{
		super(false);
		addStartNpc(30417);
		addTalkId(30031);
		addTalkId(30037);
		addTalkId(30039);
		addTalkId(30289);
		addTalkId(30311);
		addTalkId(30332);
		addTalkId(30379);
		addTalkId(30653);
		for(int[] element : DROPLIST)
		{
			addKillId(element[0]);
		}
		addQuestItem(1169, 1171, 1173, 1175, 1177, 1179);
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
		Integer classid = st.getPlayer().getClassId().getId();
		int level = st.getPlayer().getLevel();
		long squire = st.getQuestItemsCount(1271);
		long coin1 = st.getQuestItemsCount(1162);
		long coin2 = st.getQuestItemsCount(1163);
		long coin3 = st.getQuestItemsCount(1164);
		long coin4 = st.getQuestItemsCount(1165);
		long coin5 = st.getQuestItemsCount(1166);
		long coin6 = st.getQuestItemsCount(1167);
		long guards_mark1 = st.getQuestItemsCount(1168);
		long guards_mark2 = st.getQuestItemsCount(1172);
		long guards_mark3 = st.getQuestItemsCount(1176);
		long church_mark1 = st.getQuestItemsCount(1170);
		long church_mark2 = st.getQuestItemsCount(1174);
		long church_mark3 = st.getQuestItemsCount(1178);
		if(event.equalsIgnoreCase("sir_karrel_vasper_q0402_02a.htm"))
		{
			if(classid != 0 || level < 18)
			{
				htmltext = "sir_karrel_vasper_q0402_02.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = st.getQuestItemsCount(1161) > 0 ? "sir_karrel_vasper_q0402_04.htm" : "sir_karrel_vasper_q0402_05.htm";
			}
		}
		else if(event.equalsIgnoreCase("sir_karrel_vasper_q0402_08.htm"))
		{
			if(st.getCond() == 0 && classid == 0 && level >= 18)
			{
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				st.giveItems(1271, 1);
			}
		}
		else if(event.equalsIgnoreCase("captain_bathia_q0402_02.htm"))
		{
			if(squire > 0 && guards_mark1 < 1 && coin1 < 1)
			{
				st.giveItems(1168, 1);
			}
		}
		else if(event.equalsIgnoreCase("bishop_raimund_q0402_03.htm"))
		{
			if(squire > 0 && church_mark1 < 1 && coin2 < 1)
			{
				st.giveItems(1170, 1);
			}
		}
		else if(event.equalsIgnoreCase("captain_bezique_q0402_02.htm"))
		{
			if(squire > 0 && guards_mark2 < 1 && coin3 < 1)
			{
				st.giveItems(1172, 1);
			}
		}
		else if(event.equalsIgnoreCase("levian_q0402_02.htm"))
		{
			if(squire > 0 && church_mark2 < 1 && coin4 < 1)
			{
				st.giveItems(1174, 1);
			}
		}
		else if(event.equalsIgnoreCase("gilbert_q0402_02.htm"))
		{
			if(squire > 0 && guards_mark3 < 1 && coin5 < 1)
			{
				st.giveItems(1176, 1);
			}
		}
		else if(event.equalsIgnoreCase("quilt_q0402_02.htm"))
		{
			if(squire > 0 && church_mark3 < 1 && coin6 < 1)
			{
				st.giveItems(1178, 1);
			}
		}
		else if(event.equalsIgnoreCase("sir_karrel_vasper_q0402_13.htm") | event.equalsIgnoreCase("sir_karrel_vasper_q0402_14.htm") && squire > 0 && coin1 + coin2 + coin3 + coin4 + coin5 + coin6 >= 3)
		{
			for(int i = 1162;i < 1179;++i)
			{
				st.takeItems(i, -1);
			}
			st.takeItems(1271, -1);
			if(st.getPlayer().getClassId().getLevel() == 1)
			{
				st.giveItems(1161, 1);
				if(!st.getPlayer().getVarB("prof1"))
				{
					st.getPlayer().setVar("prof1", "1", -1);
					st.addExpAndSp(3200, 1500);
				}
			}
			st.exitCurrentQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		long squire = st.getQuestItemsCount(1271);
		long coin1 = st.getQuestItemsCount(1162);
		long coin2 = st.getQuestItemsCount(1163);
		long coin3 = st.getQuestItemsCount(1164);
		long coin4 = st.getQuestItemsCount(1165);
		long coin5 = st.getQuestItemsCount(1166);
		long coin6 = st.getQuestItemsCount(1167);
		long guards_mark1 = st.getQuestItemsCount(1168);
		long guards_mark2 = st.getQuestItemsCount(1172);
		long guards_mark3 = st.getQuestItemsCount(1176);
		long church_mark1 = st.getQuestItemsCount(1170);
		long church_mark2 = st.getQuestItemsCount(1174);
		long church_mark3 = st.getQuestItemsCount(1178);
		if(npcId == 30417)
		{
			if(cond == 0)
			{
				htmltext = "sir_karrel_vasper_q0402_01.htm";
			}
			else if(cond == 1 && squire > 0)
			{
				if(coin1 + coin2 + coin3 + coin4 + coin5 + coin6 < 3)
				{
					htmltext = "sir_karrel_vasper_q0402_09.htm";
				}
				else if(coin1 + coin2 + coin3 + coin4 + coin5 + coin6 == 3)
				{
					htmltext = "sir_karrel_vasper_q0402_10.htm";
				}
				else if(coin1 + coin2 + coin3 + coin4 + coin5 + coin6 > 3 && coin1 + coin2 + coin3 + coin4 + coin5 + coin6 < 6)
				{
					htmltext = "sir_karrel_vasper_q0402_11.htm";
				}
				else if(coin1 + coin2 + coin3 + coin4 + coin5 + coin6 == 6)
				{
					htmltext = "sir_karrel_vasper_q0402_12.htm";
					for(int i = 1162;i < 1179;++i)
					{
						st.takeItems(i, -1);
					}
					st.takeItems(1271, -1);
					st.giveItems(1161, 1);
					st.unset("cond");
					st.exitCurrentQuest(true);
					st.playSound("ItemSound.quest_finish");
				}
			}
		}
		else if(npcId == 30332 && cond == 1 && squire > 0)
		{
			if(guards_mark1 < 1 && coin1 < 1)
			{
				htmltext = "captain_bathia_q0402_01.htm";
			}
			else if(guards_mark1 > 0)
			{
				if(st.getQuestItemsCount(1169) < 10)
				{
					htmltext = "captain_bathia_q0402_03.htm";
				}
				else
				{
					htmltext = "captain_bathia_q0402_04.htm";
					st.takeItems(1169, -1);
					st.takeItems(1168, 1);
					st.giveItems(1162, 1);
					st.playSound("ItemSound.quest_middle");
				}
			}
			else if(coin1 > 0)
			{
				htmltext = "captain_bathia_q0402_05.htm";
			}
		}
		else if(npcId == 30289 && cond == 1 && squire > 0)
		{
			if(church_mark1 < 1 && coin2 < 1)
			{
				htmltext = "bishop_raimund_q0402_01.htm";
			}
			else if(church_mark1 > 0)
			{
				if(st.getQuestItemsCount(1171) < 12)
				{
					htmltext = "bishop_raimund_q0402_04.htm";
				}
				else
				{
					htmltext = "bishop_raimund_q0402_05.htm";
					st.takeItems(1171, -1);
					st.takeItems(1170, 1);
					st.giveItems(1163, 1);
					st.playSound("ItemSound.quest_middle");
				}
			}
			else if(coin2 > 0)
			{
				htmltext = "bishop_raimund_q0402_06.htm";
			}
		}
		else if(npcId == 30379 && cond == 1 && squire > 0)
		{
			if(coin3 < 1 && guards_mark2 < 1)
			{
				htmltext = "captain_bezique_q0402_01.htm";
			}
			else if(guards_mark2 > 0)
			{
				if(st.getQuestItemsCount(1173) < 20)
				{
					htmltext = "captain_bezique_q0402_03.htm";
				}
				else
				{
					htmltext = "captain_bezique_q0402_04.htm";
					st.takeItems(1173, -1);
					st.takeItems(1172, 1);
					st.giveItems(1164, 1);
					st.playSound("ItemSound.quest_middle");
				}
			}
			else if(coin3 > 0)
			{
				htmltext = "captain_bezique_q0402_05.htm";
			}
		}
		else if(npcId == 30037 && cond == 1 && squire > 0)
		{
			if(coin4 < 1 && church_mark2 < 1)
			{
				htmltext = "levian_q0402_01.htm";
			}
			else if(church_mark2 > 0)
			{
				if(st.getQuestItemsCount(1175) < 20)
				{
					htmltext = "levian_q0402_03.htm";
				}
				else
				{
					htmltext = "levian_q0402_04.htm";
					st.takeItems(1175, -1);
					st.takeItems(1174, 1);
					st.giveItems(1165, 1);
					st.playSound("ItemSound.quest_middle");
				}
			}
			else if(coin4 > 0)
			{
				htmltext = "levian_q0402_05.htm";
			}
		}
		else if(npcId == 30039 && cond == 1 && squire > 0)
		{
			if(guards_mark3 < 1 && coin5 < 1)
			{
				htmltext = "gilbert_q0402_01.htm";
			}
			else if(guards_mark3 > 0)
			{
				if(st.getQuestItemsCount(1177) < 20)
				{
					htmltext = "gilbert_q0402_03.htm";
				}
				else
				{
					htmltext = "gilbert_q0402_04.htm";
					st.takeItems(1177, -1);
					st.takeItems(1176, 1);
					st.giveItems(1166, 1);
					st.playSound("ItemSound.quest_middle");
				}
			}
			else if(coin5 > 0)
			{
				htmltext = "gilbert_q0402_05.htm";
			}
		}
		else if(npcId == 30031 && cond == 1 && squire > 0)
		{
			if(church_mark3 < 1 && coin6 < 1)
			{
				htmltext = "quilt_q0402_01.htm";
			}
			else if(church_mark3 > 0)
			{
				if(st.getQuestItemsCount(1179) < 10)
				{
					htmltext = "quilt_q0402_03.htm";
				}
				else
				{
					htmltext = "quilt_q0402_04.htm";
					st.takeItems(1179, -1);
					st.takeItems(1178, 1);
					st.giveItems(1167, 1);
					st.playSound("ItemSound.quest_middle");
				}
			}
			else if(coin6 > 0)
			{
				htmltext = "quilt_q0402_05.htm";
			}
		}
		else if(npcId == 30311 && cond == 1 && squire > 0)
		{
			htmltext = "sir_collin_windawood_q0402_01.htm";
		}
		else if(npcId == 30653 && cond == 1 && squire > 0)
		{
			htmltext = "sir_aron_tanford_q0402_01.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		for(int[] element : DROPLIST)
		{
			if(st.getCond() <= 0 || npcId != element[0] || st.getQuestItemsCount(element[1]) <= 0 || st.getQuestItemsCount(element[2]) >= (long) element[3] || !Rnd.chance(element[4]))
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