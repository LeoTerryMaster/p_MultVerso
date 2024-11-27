package quests;

import l2.commons.dbutils.DbUtils;
import l2.commons.util.Rnd;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.tables.SkillTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;

public class _501_ProofOfClanAlliance extends Quest implements ScriptFile
{
	private static final int SIR_KRISTOF_RODEMAI = 30756;
	private static final int STATUE_OF_OFFERING = 30757;
	private static final int WITCH_ATHREA = 30758;
	private static final int WITCH_KALIS = 30759;
	private static final int HERB_OF_HARIT = 3832;
	private static final int HERB_OF_VANOR = 3833;
	private static final int HERB_OF_OEL_MAHUM = 3834;
	private static final int BLOOD_OF_EVA = 3835;
	private static final int SYMBOL_OF_LOYALTY = 3837;
	private static final int PROOF_OF_ALLIANCE = 3874;
	private static final int VOUCHER_OF_FAITH = 3873;
	private static final int ANTIDOTE_RECIPE = 3872;
	private static final int POTION_OF_RECOVERY = 3889;
	private static final int[] CHESTS = {27173, 27174, 27175, 27176, 27177};
	private static final int[][] MOBS = {{20685, 3833}, {20644, 3832}, {20576, 3834}};
	private static final int RATE = 35;
	private static final int RETRY_PRICE = 10000;
	
	public _501_ProofOfClanAlliance()
	{
		super(0);
		addStartNpc(30756);
		addStartNpc(30757);
		addStartNpc(30758);
		addTalkId(30759);
		addQuestItem(3837);
		addQuestItem(3872);
		for(int[] i : MOBS)
		{
			addKillId(i[0]);
			addQuestItem(i[1]);
		}
		for(int i : CHESTS)
		{
			addKillId(i);
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
	
	public QuestState getLeader(QuestState st)
	{
		Clan clan = st.getPlayer().getClan();
		QuestState leader = null;
		if(clan != null && clan.getLeader() != null && clan.getLeader().getPlayer() != null)
		{
			leader = clan.getLeader().getPlayer().getQuestState(getName());
		}
		return leader;
	}
	
	public void removeQuestFromMembers(QuestState st, boolean leader)
	{
		removeQuestFromOfflineMembers(st);
		removeQuestFromOnlineMembers(st, leader);
	}
	
	public void removeQuestFromOfflineMembers(QuestState st)
	{
		if(st.getPlayer() == null || st.getPlayer().getClan() == null)
		{
			st.exitCurrentQuest(true);
			return;
		}
		int clan = st.getPlayer().getClan().getClanId();
		Connection con = null;
		PreparedStatement offline = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("DELETE FROM character_quests WHERE name = ? AND char_id IN (SELECT obj_id FROM characters WHERE clanId = ? AND online = 0)");
			offline.setString(1, getName());
			offline.setInt(2, clan);
			offline.executeUpdate();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, offline);
		}
	}
	
	public void removeQuestFromOnlineMembers(QuestState st, boolean leader)
	{
		if(st.getPlayer() == null || st.getPlayer().getClan() == null)
		{
			st.exitCurrentQuest(true);
			return;
		}
		Player pleader = null;
		QuestState l;
		if(leader && (l = getLeader(st)) != null)
		{
			pleader = l.getPlayer();
		}
		if(pleader != null)
		{
			pleader.stopImmobilized();
			pleader.getEffectList().stopEffect(4082);
		}
		for(Player pl : st.getPlayer().getClan().getOnlineMembers(st.getPlayer().getClan().getLeaderId()))
		{
			if(pl == null || pl.getQuestState(getName()) == null)
				continue;
			pl.getQuestState(getName()).exitCurrentQuest(true);
		}
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(st.getPlayer() == null || st.getPlayer().getClan() == null)
		{
			st.exitCurrentQuest(true);
			return "noquest";
		}
		QuestState leader = getLeader(st);
		if(leader == null)
		{
			removeQuestFromMembers(st, true);
			return "Quest Failed";
		}
		String htmltext = event;
		if(st.getPlayer().isClanLeader())
		{
			if(event.equalsIgnoreCase("30756-03.htm"))
			{
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
			}
			else if(event.equalsIgnoreCase("30759-03.htm"))
			{
				st.setCond(2);
				st.set("dead_list", " ");
			}
			else if(event.equalsIgnoreCase("30759-07.htm"))
			{
				st.takeItems(3837, -1);
				st.giveItems(3872, 1);
				st.addNotifyOfDeath(st.getPlayer(), false);
				st.setCond(3);
				st.set("chest_count", "0");
				st.set("chest_game", "0");
				st.set("chest_try", "0");
				st.startQuestTimer("poison_timer", 3600000);
				st.getPlayer().altUseSkill(SkillTable.getInstance().getInfo(4082, 1), st.getPlayer());
				st.getPlayer().startImmobilized();
				htmltext = "30759-07.htm";
			}
		}
		if(event.equalsIgnoreCase("poison_timer"))
		{
			removeQuestFromMembers(st, true);
			htmltext = "30759-09.htm";
		}
		else if(event.equalsIgnoreCase("chest_timer"))
		{
			htmltext = "";
			if(leader.getInt("chest_game") < 2)
			{
				stop_chest_game(st);
			}
		}
		else if(event.equalsIgnoreCase("30757-04.htm"))
		{
			ArrayList<String> deadlist = new ArrayList<>();
			deadlist.addAll(Arrays.asList(leader.get("dead_list").split(" ")));
			deadlist.add(st.getPlayer().getName());
			String deadstr = "";
			for(String s : deadlist)
			{
				deadstr = deadstr + s + " ";
			}
			leader.set("dead_list", deadstr);
			st.addNotifyOfDeath(leader.getPlayer(), false);
			if(Rnd.chance(50))
			{
				st.getPlayer().reduceCurrentHp(st.getPlayer().getCurrentHp() * 8.0, st.getPlayer(), null, true, true, false, false, false, false, false);
			}
			st.giveItems(3837, 1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30757-05.htm"))
		{
			st.exitCurrentQuest(true);
		}
		else if(event.equalsIgnoreCase("30758-03.htm"))
		{
			start_chest_game(st);
		}
		else if(event.equalsIgnoreCase("30758-07.htm"))
		{
			if(st.getQuestItemsCount(57) < 10000)
			{
				htmltext = "30758-06.htm";
			}
			else
			{
				st.takeItems(57, 10000);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(st.getPlayer() == null || st.getPlayer().getClan() == null)
		{
			st.exitCurrentQuest(true);
			return htmltext;
		}
		QuestState leader = getLeader(st);
		if(leader == null)
		{
			removeQuestFromMembers(st, true);
			return "Quest Failed";
		}
		int npcId = npc.getNpcId();
		if(npcId == 30756)
		{
			if(!st.getPlayer().isClanLeader())
			{
				st.exitCurrentQuest(true);
				return "30756-10.htm";
			}
			if(st.getPlayer().getClan().getLevel() <= 2)
			{
				st.exitCurrentQuest(true);
				return "30756-08.htm";
			}
			if(st.getPlayer().getClan().getLevel() >= 4)
			{
				st.exitCurrentQuest(true);
				return "30756-09.htm";
			}
			if(st.getQuestItemsCount(3873) > 0)
			{
				st.playSound("ItemSound.quest_fanfare_2");
				st.takeItems(3873, -1);
				st.giveItems(3874, 1);
				st.addExpAndSp(0, 120000);
				htmltext = "30756-07.htm";
				st.exitCurrentQuest(true);
				return htmltext;
			}
			if(cond == 1 || cond == 2)
			{
				return "30756-06.htm";
			}
			if(st.getQuestItemsCount(3874) == 0)
			{
				st.setCond(0);
				return "30756-01.htm";
			}
			st.exitCurrentQuest(true);
			return htmltext;
		}
		if(npcId == 30759)
		{
			if(st.getPlayer().isClanLeader())
			{
				if(cond == 1)
				{
					return "30759-01.htm";
				}
				if(cond == 2)
				{
					htmltext = "30759-05.htm";
					if(st.getQuestItemsCount(3837) != 3)
						return htmltext;
					int deads = 0;
					try
					{
						deads = st.get("dead_list").split(" ").length;
						return htmltext;
					}
					finally
					{
						if(deads == 3)
						{
							htmltext = "30759-06.htm";
						}
					}
				}
				if(cond != 3)
					return htmltext;
				if(st.getQuestItemsCount(3832) > 0 && st.getQuestItemsCount(3833) > 0 && st.getQuestItemsCount(3834) > 0 && st.getQuestItemsCount(3835) > 0 && st.getQuestItemsCount(3872) > 0)
				{
					st.takeItems(3872, 1);
					st.takeItems(3832, 1);
					st.takeItems(3833, 1);
					st.takeItems(3834, 1);
					st.takeItems(3835, 1);
					st.giveItems(3889, 1);
					st.giveItems(3873, 1);
					st.cancelQuestTimer("poison_timer");
					removeQuestFromMembers(st, false);
					st.getPlayer().stopImmobilized();
					st.getPlayer().getEffectList().stopEffect(4082);
					st.setCond(4);
					st.playSound("ItemSound.quest_finish");
					return "30759-08.htm";
				}
				if(st.getQuestItemsCount(3873) != 0)
					return htmltext;
				return "30759-10.htm";
			}
			if(leader.getCond() != 3)
				return htmltext;
			return "30759-11.htm";
		}
		if(npcId == 30757)
		{
			if(st.getPlayer().isClanLeader())
			{
				return "30757-03.htm";
			}
			if(st.getPlayer().getLevel() <= 39)
			{
				st.exitCurrentQuest(true);
				return "30757-02.htm";
			}
			int deads;
			String[] dlist2;
			try
			{
				dlist2 = leader.get("dead_list").split(" ");
				deads = dlist2.length;
			}
			catch(Exception e)
			{
				removeQuestFromMembers(st, true);
				return "Who are you?";
			}
			if(deads >= 3)
				return htmltext;
			String[] e2 = dlist2;
			int n = e2.length;
			int n2 = 0;
			while(n2 < n)
			{
				String str = e2[n2];
				if(st.getPlayer().getName().equalsIgnoreCase(str))
				{
					return "you cannot die again!";
				}
				++n2;
			}
			return "30757-01.htm";
		}
		if(npcId != 30758)
			return htmltext;
		if(st.getPlayer().isClanLeader())
		{
			return "30757-03.htm";
		}
		String[] dlist;
		try
		{
			dlist = leader.get("dead_list").split(" ");
		}
		catch(Exception e)
		{
			st.exitCurrentQuest(true);
			return "Who are you?";
		}
		Boolean flag = false;
		if(dlist != null)
		{
			for(String str : dlist)
			{
				if(!st.getPlayer().getName().equalsIgnoreCase(str))
					continue;
				flag = true;
			}
		}
		if(!flag.booleanValue())
		{
			st.exitCurrentQuest(true);
			return "Who are you?";
		}
		int game_state = leader.getInt("chest_game");
		if(game_state == 0)
		{
			if(leader.getInt("chest_try") != 0)
				return "30758-05.htm";
			return "30758-01.htm";
		}
		if(game_state == 1)
		{
			return "30758-09.htm";
		}
		if(game_state != 2)
			return htmltext;
		st.playSound("ItemSound.quest_finish");
		st.giveItems(3835, 1);
		st.cancelQuestTimer("chest_timer");
		stop_chest_game(st);
		leader.set("chest_game", "3");
		return "30758-08.htm";
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getPlayer() == null || st.getPlayer().getClan() == null)
		{
			st.exitCurrentQuest(true);
			return "noquest";
		}
		QuestState leader = getLeader(st);
		if(leader == null)
		{
			removeQuestFromMembers(st, true);
			return "Quest Failed";
		}
		int npcId = npc.getNpcId();
		if(!leader.isRunningQuestTimer("poison_timer"))
		{
			stop_chest_game(st);
			return "Quest Failed";
		}
		for(int[] m : MOBS)
		{
			if(npcId != m[0] || st.getInt(String.valueOf(m[1])) != 0 || !Rnd.chance(35))
				continue;
			st.giveItems(m[1], 1);
			leader.set(String.valueOf(m[1]), "1");
			st.playSound("ItemSound.quest_middle");
			return null;
		}
		for(int i : CHESTS)
		{
			if(npcId != i)
				continue;
			if(!leader.isRunningQuestTimer("chest_timer"))
			{
				stop_chest_game(st);
				return "Time is up!";
			}
			if(Rnd.chance(25))
			{
				Functions.npcSay(npc, "###### BINGO! ######");
				int count = leader.getInt("chest_count");
				if(count < 4)
				{
					leader.set("chest_count", String.valueOf(++count));
				}
				if(count >= 4)
				{
					stop_chest_game(st);
					leader.set("chest_game", "2");
					leader.cancelQuestTimer("chest_timer");
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
			return null;
		}
		return null;
	}
	
	public void start_chest_game(QuestState st)
	{
		if(st.getPlayer() == null || st.getPlayer().getClan() == null)
		{
			st.exitCurrentQuest(true);
			return;
		}
		QuestState leader = getLeader(st);
		if(leader == null)
		{
			removeQuestFromMembers(st, true);
			return;
		}
		leader.set("chest_game", "1");
		leader.set("chest_count", "0");
		int attempts = leader.getInt("chest_try");
		leader.set("chest_try", String.valueOf(attempts + 1));
		for(NpcInstance npc : GameObjectsStorage.getAllByNpcId(CHESTS, false))
		{
			npc.deleteMe();
		}
		for(int n = 1;n <= 5;++n)
		{
			for(int i : CHESTS)
			{
				leader.addSpawn(i, 102100, 103450, -3400, 0, 100, 60000);
			}
		}
		leader.startQuestTimer("chest_timer", 60000);
	}
	
	public void stop_chest_game(QuestState st)
	{
		QuestState leader = getLeader(st);
		if(leader == null)
		{
			removeQuestFromMembers(st, true);
			return;
		}
		for(NpcInstance npc : GameObjectsStorage.getAllByNpcId(CHESTS, false))
		{
			npc.deleteMe();
		}
		leader.set("chest_game", "0");
	}
	
	@Override
	public String onDeath(Creature npc, Creature pc, QuestState st)
	{
		if(st.getPlayer() == null || st.getPlayer().getClan() == null)
		{
			st.exitCurrentQuest(true);
			return null;
		}
		QuestState leader = getLeader(st);
		if(leader == null)
		{
			removeQuestFromMembers(st, true);
			return null;
		}
		if(st.getPlayer() == pc)
		{
			leader.cancelQuestTimer("poison_timer");
			leader.cancelQuestTimer("chest_timer");
			removeQuestFromMembers(st, true);
		}
		return null;
	}
}