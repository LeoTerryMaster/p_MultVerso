package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _611_AllianceWithVarkaSilenos extends Quest implements ScriptFile
{
	private static final int MARK_OF_VARKA_ALLIANCE1 = 7221;
	private static final int MARK_OF_VARKA_ALLIANCE2 = 7222;
	private static final int MARK_OF_VARKA_ALLIANCE3 = 7223;
	private static final int MARK_OF_VARKA_ALLIANCE4 = 7224;
	private static final int MARK_OF_VARKA_ALLIANCE5 = 7225;
	private static final int KB_SOLDIER = 7226;
	private static final int KB_CAPTAIN = 7227;
	private static final int KB_GENERAL = 7228;
	private static final int TOTEM_OF_VALOR = 7229;
	private static final int TOTEM_OF_WISDOM = 7230;
	private static final int RAIDER = 21327;
	private static final int FOOTMAN = 21324;
	private static final int SCOUT = 21328;
	private static final int WAR_HOUND = 21325;
	private static final int SHAMAN = 21329;
	private static final int SEER = 21338;
	private static final int WARRIOR = 21331;
	private static final int LIEUTENANT = 21332;
	private static final int ELITE_SOLDIER = 21335;
	private static final int MEDIUM = 21334;
	private static final int COMMAND = 21343;
	private static final int ELITE_GUARD = 21344;
	private static final int WHITE_CAPTAIN = 21336;
	private static final int BATTALION_COMMANDER_SOLDIER = 21340;
	private static final int GENERAL = 21339;
	private static final int GREAT_SEER = 21342;
	private static final int KETRA_PROPHET = 21347;
	private static final int DISCIPLE_OF_PROPHET = 21375;
	private static final int PROPHET_GUARDS = 21348;
	private static final int PROPHET_AIDE = 21349;
	private static final int HEAD_SHAMAN = 21345;
	private static final int HEAD_GUARDS = 21346;
	private final int[] VARKA_NPC_LIST = new int[20];
	
	public _611_AllianceWithVarkaSilenos()
	{
		super(true);
		addStartNpc(31378);
		VARKA_NPC_LIST[0] = 21350;
		VARKA_NPC_LIST[1] = 21351;
		VARKA_NPC_LIST[2] = 21353;
		VARKA_NPC_LIST[3] = 21354;
		VARKA_NPC_LIST[4] = 21355;
		VARKA_NPC_LIST[5] = 21357;
		VARKA_NPC_LIST[6] = 21358;
		VARKA_NPC_LIST[7] = 21360;
		VARKA_NPC_LIST[8] = 21361;
		VARKA_NPC_LIST[9] = 21362;
		VARKA_NPC_LIST[10] = 21364;
		VARKA_NPC_LIST[11] = 21365;
		VARKA_NPC_LIST[12] = 21366;
		VARKA_NPC_LIST[13] = 21368;
		VARKA_NPC_LIST[14] = 21369;
		VARKA_NPC_LIST[15] = 21370;
		VARKA_NPC_LIST[16] = 21371;
		VARKA_NPC_LIST[17] = 21372;
		VARKA_NPC_LIST[18] = 21373;
		VARKA_NPC_LIST[19] = 21374;
		for(int npcId : VARKA_NPC_LIST)
		{
			addKillId(npcId);
		}
		addKillId(21327);
		addKillId(21324);
		addKillId(21328);
		addKillId(21325);
		addKillId(21329);
		addKillId(21338);
		addKillId(21331);
		addKillId(21332);
		addKillId(21335);
		addKillId(21334);
		addKillId(21343);
		addKillId(21344);
		addKillId(21336);
		addKillId(21340);
		addKillId(21339);
		addKillId(21342);
		addKillId(21347);
		addKillId(21349);
		addKillId(21348);
		addKillId(21345);
		addKillId(21346);
		addQuestItem(7226);
		addQuestItem(7227);
		addQuestItem(7228);
	}
	
	private static void takeAllMarks(QuestState st)
	{
		st.takeItems(7221, -1);
		st.takeItems(7222, -1);
		st.takeItems(7223, -1);
		st.takeItems(7224, -1);
		st.takeItems(7225, -1);
	}
	
	private static void checkMarks(QuestState st)
	{
		if(st.getCond() == 0)
		{
			return;
		}
		if(st.getQuestItemsCount(7225) > 0)
		{
			st.setCond(6);
		}
		else if(st.getQuestItemsCount(7224) > 0)
		{
			st.setCond(5);
		}
		else if(st.getQuestItemsCount(7223) > 0)
		{
			st.setCond(4);
		}
		else if(st.getQuestItemsCount(7222) > 0)
		{
			st.setCond(3);
		}
		else if(st.getQuestItemsCount(7221) > 0)
		{
			st.setCond(2);
		}
		else
		{
			st.setCond(1);
		}
	}
	
	private static boolean CheckNextLevel(QuestState st, int soilder_count, int capitan_count, int general_count, int other_item, boolean take)
	{
		if(soilder_count > 0 && st.getQuestItemsCount(7226) < (long) soilder_count)
		{
			return false;
		}
		if(capitan_count > 0 && st.getQuestItemsCount(7227) < (long) capitan_count)
		{
			return false;
		}
		if(general_count > 0 && st.getQuestItemsCount(7228) < (long) general_count)
		{
			return false;
		}
		if(other_item > 0 && st.getQuestItemsCount(other_item) < 1)
		{
			return false;
		}
		if(take)
		{
			if(soilder_count > 0)
			{
				st.takeItems(7226, (long) soilder_count);
			}
			if(capitan_count > 0)
			{
				st.takeItems(7227, (long) capitan_count);
			}
			if(general_count > 0)
			{
				st.takeItems(7228, (long) general_count);
			}
			if(other_item > 0)
			{
				st.takeItems(other_item, 1);
			}
			takeAllMarks(st);
		}
		return true;
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
	
	public boolean isVarkaNpc(int npc)
	{
		for(int i : VARKA_NPC_LIST)
		{
			if(npc != i)
				continue;
			return true;
		}
		return false;
	}
	
	@Override
	public void onAbort(QuestState st)
	{
		takeAllMarks(st);
		st.setCond(0);
		st.getPlayer().updateKetraVarka();
		st.playSound("ItemSound.quest_middle");
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("herald_naran_q0611_04.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			return event;
		}
		checkMarks(st);
		int cond = st.getCond();
		if(event.equalsIgnoreCase("herald_naran_q0611_12.htm") && cond == 1 && CheckNextLevel(st, 100, 0, 0, 0, true))
		{
			st.giveItems(7221, 1);
			st.setCond(2);
			st.getPlayer().updateKetraVarka();
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("herald_naran_q0611_15.htm") && cond == 2 && CheckNextLevel(st, 200, 100, 0, 0, true))
		{
			st.giveItems(7222, 1);
			st.setCond(3);
			st.getPlayer().updateKetraVarka();
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("herald_naran_q0611_18.htm") && cond == 3 && CheckNextLevel(st, 300, 200, 100, 0, true))
		{
			st.giveItems(7223, 1);
			st.setCond(4);
			st.getPlayer().updateKetraVarka();
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("herald_naran_q0611_21.htm") && cond == 4 && CheckNextLevel(st, 300, 300, 200, 7229, true))
		{
			st.giveItems(7224, 1);
			st.setCond(5);
			st.getPlayer().updateKetraVarka();
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("herald_naran_q0611_23.htm") && cond == 5 && CheckNextLevel(st, 400, 400, 200, 7230, true))
		{
			st.giveItems(7225, 1);
			st.setCond(6);
			st.getPlayer().updateKetraVarka();
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("herald_naran_q0611_26.htm"))
		{
			takeAllMarks(st);
			st.setCond(0);
			st.getPlayer().updateKetraVarka();
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		if(st.getPlayer().getKetra() > 0)
		{
			st.exitCurrentQuest(true);
			return "herald_naran_q0611_02.htm";
		}
		int npcId = npc.getNpcId();
		checkMarks(st);
		if(st.getState() == 1)
		{
			st.setCond(0);
		}
		int cond = st.getCond();
		if(npcId == 31378)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 74)
				{
					st.exitCurrentQuest(true);
					return "herald_naran_q0611_03.htm";
				}
				return "herald_naran_q0611_01.htm";
			}
			if(cond == 1)
			{
				return CheckNextLevel(st, 100, 0, 0, 0, false) ? "herald_naran_q0611_11.htm" : "herald_naran_q0611_10.htm";
			}
			if(cond == 2)
			{
				return CheckNextLevel(st, 200, 100, 0, 0, false) ? "herald_naran_q0611_14.htm" : "herald_naran_q0611_13.htm";
			}
			if(cond == 3)
			{
				return CheckNextLevel(st, 300, 200, 100, 0, false) ? "herald_naran_q0611_17.htm" : "herald_naran_q0611_16.htm";
			}
			if(cond == 4)
			{
				return CheckNextLevel(st, 300, 300, 200, 7229, false) ? "herald_naran_q0611_20.htm" : "herald_naran_q0611_19.htm";
			}
			if(cond == 5)
			{
				return CheckNextLevel(st, 400, 400, 200, 7230, false) ? "herald_naran_q0611_27.htm" : "herald_naran_q0611_22.htm";
			}
			if(cond == 6)
			{
				return "herald_naran_q0611_24.htm";
			}
		}
		return "noquest";
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(isVarkaNpc(npcId))
		{
			if(st.getQuestItemsCount(7225) > 0)
			{
				takeAllMarks(st);
				st.giveItems(7224, 1);
				st.getPlayer().updateKetraVarka();
				checkMarks(st);
			}
			else if(st.getQuestItemsCount(7224) > 0)
			{
				takeAllMarks(st);
				st.giveItems(7223, 1);
				st.getPlayer().updateKetraVarka();
				checkMarks(st);
			}
			else if(st.getQuestItemsCount(7223) > 0)
			{
				takeAllMarks(st);
				st.giveItems(7222, 1);
				st.getPlayer().updateKetraVarka();
				checkMarks(st);
			}
			else if(st.getQuestItemsCount(7222) > 0)
			{
				takeAllMarks(st);
				st.giveItems(7221, 1);
				st.getPlayer().updateKetraVarka();
				checkMarks(st);
			}
			else if(st.getQuestItemsCount(7221) > 0)
			{
				takeAllMarks(st);
				st.getPlayer().updateKetraVarka();
				checkMarks(st);
			}
			else if(st.getPlayer().getVarka() > 0)
			{
				st.getPlayer().updateKetraVarka();
				st.exitCurrentQuest(true);
				return "herald_naran_q0611_26.htm";
			}
		}
		if(st.getQuestItemsCount(7225) > 0)
		{
			return null;
		}
		int cond = st.getCond();
		if(npcId == 21327 || npcId == 21324 || npcId == 21328 || npcId == 21325 || npcId == 21329)
		{
			if(cond > 0)
			{
				st.rollAndGive(7226, 1, 60.0);
			}
		}
		else if(npcId == 21338 || npcId == 21331 || npcId == 21332 || npcId == 21335 || npcId == 21334 || npcId == 21343 || npcId == 21344 || npcId == 21336)
		{
			if(cond > 1)
			{
				st.rollAndGive(7227, 1, 70.0);
			}
		}
		else if((npcId == 21340 || npcId == 21339 || npcId == 21342 || npcId == 21347 || npcId == 21375 || npcId == 21348 || npcId == 21345 || npcId == 21346) && cond > 2)
		{
			st.rollAndGive(7228, 1, 80.0);
		}
		return null;
	}
}