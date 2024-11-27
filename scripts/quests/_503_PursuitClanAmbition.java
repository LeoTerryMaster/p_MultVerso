package quests;

import l2.commons.dbutils.DbUtils;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class _503_PursuitClanAmbition extends Quest implements ScriptFile
{
	private final int G_Let_Martien = 3866;
	private final int Th_Wyrm_Eggs = 3842;
	private final int Drake_Eggs = 3841;
	private final int Bl_Wyrm_Eggs = 3840;
	private final int Mi_Drake_Eggs = 3839;
	private final int Brooch = 3843;
	private final int Bl_Anvil_Coin = 3871;
	private final short G_Let_Balthazar = 3867;
	private final short Recipe_Power_Stone = 3838;
	private final short Power_Stones = 3846;
	private final short Nebulite_Crystals = 3844;
	private final short Broke_Power_Stone = 3845;
	private final int G_Let_Rodemai = 3868;
	private final int Imp_Keys = 3847;
	private final int Scepter_Judgement = 3869;
	private final int Proof_Aspiration = 3870;
	private final int[] EggList = {3839, 3840, 3841, 3842};
	private final int Gustaf = 30760;
	private final int Martien = 30645;
	private final int Athrea = 30758;
	private final int Kalis = 30759;
	private final int Fritz = 30761;
	private final int Lutz = 30762;
	private final int Kurtz = 30763;
	private final int Kusto = 30512;
	private final int Balthazar = 30764;
	private final int Rodemai = 30868;
	private final int Coffer = 30765;
	private final int Cleo = 30766;
	private final int ThunderWyrm1 = 20282;
	private final int ThunderWyrm2 = 20243;
	private final int Drake1 = 20137;
	private final int Drake2 = 20285;
	private final int BlitzWyrm = 27178;
	private final int GraveGuard = 20668;
	private final int GraveKeymaster = 27179;
	private final int ImperialGravekeeper = 27181;
	private final int GiantSoldier = 20654;
	private final int GiantScout = 20656;
	
	public _503_PursuitClanAmbition()
	{
		super(2);
		addStartNpc(30760);
		addTalkId(30645);
		addTalkId(30758);
		addTalkId(30759);
		addTalkId(30761);
		addTalkId(30762);
		addTalkId(30763);
		addTalkId(30512);
		addTalkId(30764);
		addTalkId(30868);
		addTalkId(30765);
		addTalkId(30766);
		addKillId(20282, 20243, 20137, 20285, 27178, 20654, 20656, 20668, 27179, 27181);
		addAttackId(27181);
		int i = 3839;
		while(i <= 3848)
		{
			addQuestItem(i++);
		}
		i = 3866;
		while(i <= 3869)
		{
			addQuestItem(i++);
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
	
	public void suscribe_members(QuestState st)
	{
		int clan = st.getPlayer().getClan().getClanId();
		Connection con = null;
		PreparedStatement offline = null;
		PreparedStatement insertion = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT obj_Id FROM characters WHERE clanid=? AND online=0");
			insertion = con.prepareStatement("REPLACE INTO character_quests (char_id,name,var,value) VALUES (?,?,?,?)");
			offline.setInt(1, clan);
			rs = offline.executeQuery();
			while(rs.next())
			{
				int char_id = rs.getInt("obj_Id");
				try
				{
					insertion.setInt(1, char_id);
					insertion.setString(2, getName());
					insertion.setString(3, "<state>");
					insertion.setString(4, "Started");
					insertion.executeUpdate();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(insertion);
			DbUtils.closeQuietly(con, offline, rs);
		}
	}
	
	public void offlineMemberExit(QuestState st)
	{
		int clan = st.getPlayer().getClan().getClanId();
		Connection con = null;
		PreparedStatement offline = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("DELETE FROM character_quests WHERE name=? AND char_id IN (SELECT obj_id FROM characters WHERE clanId=? AND online=0)");
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
	
	public Player getLeader(QuestState st)
	{
		Player player = st.getPlayer();
		if(player == null)
		{
			return null;
		}
		Clan clan = player.getClan();
		if(clan == null)
		{
			return null;
		}
		return clan.getLeader().getPlayer();
	}
	
	public int getLeaderVar(QuestState st, String var)
	{
		boolean cond = "cond".equalsIgnoreCase(var);
		try
		{
			Player leader = getLeader(st);
			if(leader != null)
			{
				if(cond)
				{
					return leader.getQuestState(getName()).getCond();
				}
				return leader.getQuestState(getName()).getInt(var);
			}
		}
		catch(Exception e)
		{
			return -1;
		}
		Clan clan = st.getPlayer().getClan();
		if(clan == null)
		{
			return -1;
		}
		int leaderId = clan.getLeaderId();
		Connection con = null;
		PreparedStatement offline = null;
		ResultSet rs = null;
		int i;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT value FROM character_quests WHERE char_id=? AND var=? AND name=?");
			offline.setInt(1, leaderId);
			offline.setString(2, var);
			offline.setString(3, getName());
			rs = offline.executeQuery();
			int val = -1;
			if(rs.next())
			{
				val = rs.getInt("value");
				if(cond && (val & Integer.MIN_VALUE) != 0)
				{
					val &= Integer.MAX_VALUE;
					for(i = 1;i < 32;++i)
					{
						if((val >>= 1) != 0)
							continue;
						int n = i;
						return n;
					}
				}
			}
			i = val;
			return i;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			i = -1;
			return i;
		}
		finally
		{
			DbUtils.closeQuietly(con, offline, rs);
		}
	}
	
	public void setLeaderVar(QuestState st, String var, String value)
	{
		Clan clan = st.getPlayer().getClan();
		if(clan == null)
		{
			return;
		}
		Player leader = clan.getLeader().getPlayer();
		if(leader != null)
		{
			if("cond".equalsIgnoreCase(var))
			{
				leader.getQuestState(getName()).setCond(Integer.parseInt(value));
			}
			else
			{
				leader.getQuestState(getName()).set(var, value);
			}
		}
		else
		{
			int leaderId = st.getPlayer().getClan().getLeaderId();
			Connection con = null;
			PreparedStatement offline = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				offline = con.prepareStatement("UPDATE character_quests SET value=? WHERE char_id=? AND var=? AND name=?");
				offline.setString(1, value);
				offline.setInt(2, leaderId);
				offline.setString(3, var);
				offline.setString(4, getName());
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
	}
	
	public boolean checkEggs(QuestState st)
	{
		int count = 0;
		for(int item : EggList)
		{
			if(st.getQuestItemsCount(item) <= 9)
				continue;
			++count;
		}
		return count > 3;
	}
	
	public void giveItem(int item, long maxcount, QuestState st)
	{
		Player player = st.getPlayer();
		if(player == null)
		{
			return;
		}
		Player leader = getLeader(st);
		if(leader == null)
		{
			return;
		}
		if(player.getDistance(leader) > (double) Config.ALT_PARTY_DISTRIBUTION_RANGE)
		{
			return;
		}
		QuestState qs = leader.getQuestState(getClass());
		if(qs == null)
		{
			return;
		}
		long count = qs.getQuestItemsCount(item);
		if(count < maxcount)
		{
			qs.giveItems(item, 1);
			if(count == maxcount - 1)
			{
				qs.playSound("ItemSound.quest_middle");
			}
			else
			{
				qs.playSound("ItemSound.quest_itemget");
			}
		}
	}
	
	public String exit503(boolean completed, QuestState st)
	{
		if(completed)
		{
			st.giveItems(3870, 1);
			st.addExpAndSp(0, 250000);
			st.unset("cond");
			st.unset("Fritz");
			st.unset("Lutz");
			st.unset("Kurtz");
			st.unset("ImpGraveKeeper");
			st.exitCurrentQuest(false);
		}
		else
		{
			st.exitCurrentQuest(true);
		}
		st.takeItems(3869, -1);
		try
		{
			List<Player> members = st.getPlayer().getClan().getOnlineMembers(0);
			for(Player player : members)
			{
				QuestState qs;
				if(player == null || (qs = player.getQuestState(getName())) == null)
					continue;
				qs.exitCurrentQuest(true);
			}
			offlineMemberExit(st);
		}
		catch(Exception e)
		{
			return "You dont have any members in your Clan, so you can't finish the Pursuit of Aspiration";
		}
		return "Congratulations, you have finished the Pursuit of Clan Ambition";
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("30760-08.htm"))
		{
			st.giveItems(3866, 1);
			st.setCond(1);
			st.set("Fritz", "1");
			st.set("Lutz", "1");
			st.set("Kurtz", "1");
			st.set("ImpGraveKeeper", "1");
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("30760-12.htm"))
		{
			st.giveItems(3867, 1);
			st.setCond(4);
		}
		else if(event.equalsIgnoreCase("30760-16.htm"))
		{
			st.giveItems(3868, 1);
			st.setCond(7);
		}
		else if(event.equalsIgnoreCase("30760-20.htm"))
		{
			exit503(true, st);
		}
		else if(event.equalsIgnoreCase("30760-22.htm"))
		{
			st.setCond(13);
		}
		else if(event.equalsIgnoreCase("30760-23.htm"))
		{
			exit503(true, st);
		}
		else if(event.equalsIgnoreCase("30645-03.htm"))
		{
			st.takeItems(3866, -1);
			st.setCond(2);
			suscribe_members(st);
			List<Player> members = st.getPlayer().getClan().getOnlineMembers(st.getPlayer().getObjectId());
			for(Player player : members)
			{
				newQuestState(player, 2);
			}
		}
		else if(event.equalsIgnoreCase("30763-03.htm"))
		{
			if(st.getInt("Kurtz") == 1)
			{
				htmltext = "30763-02.htm";
				st.giveItems(3839, 6);
				st.giveItems(3843, 1);
				st.set("Kurtz", "2");
			}
		}
		else if(event.equalsIgnoreCase("30762-03.htm"))
		{
			int lutz = st.getInt("Lutz");
			if(lutz == 1)
			{
				htmltext = "30762-02.htm";
				st.giveItems(3839, 4);
				st.giveItems(3840, 3);
				st.set("Lutz", "2");
			}
			st.addSpawn(27178, npc.getLoc().x, npc.getLoc().y, npc.getLoc().z, Location.getRandomHeading(), 300, 120000);
			st.addSpawn(27178, npc.getLoc().x, npc.getLoc().y, npc.getLoc().z, Location.getRandomHeading(), 300, 120000);
		}
		else if(event.equalsIgnoreCase("30761-03.htm"))
		{
			int fritz = st.getInt("Fritz");
			if(fritz == 1)
			{
				htmltext = "30761-02.htm";
				st.giveItems(3840, 3);
				st.set("Fritz", "2");
			}
			st.addSpawn(27178, npc.getLoc().x, npc.getLoc().y, npc.getLoc().z, Location.getRandomHeading(), 300, 120000);
			st.addSpawn(27178, npc.getLoc().x, npc.getLoc().y, npc.getLoc().z, Location.getRandomHeading(), 300, 120000);
		}
		else if(event.equalsIgnoreCase("30512-03.htm"))
		{
			st.takeItems(3843, -1);
			st.giveItems(3871, 1);
			st.set("Kurtz", "3");
		}
		else if(event.equalsIgnoreCase("30764-03.htm"))
		{
			st.takeItems(3867, -1);
			st.setCond(5);
			st.set("Kurtz", "3");
		}
		else if(event.equalsIgnoreCase("30764-05.htm"))
		{
			st.takeItems(3867, -1);
			st.setCond(5);
		}
		else if(event.equalsIgnoreCase("30764-06.htm"))
		{
			st.takeItems(3871, -1);
			st.set("Kurtz", "4");
			st.giveItems(3838, 1);
		}
		else if(event.equalsIgnoreCase("30868-04.htm"))
		{
			st.takeItems(3868, -1);
			st.setCond(8);
		}
		else if(event.equalsIgnoreCase("30868-06a.htm"))
		{
			st.setCond(10);
		}
		else if(event.equalsIgnoreCase("30868-10.htm"))
		{
			st.setCond(12);
		}
		else if(event.equalsIgnoreCase("30766-04.htm"))
		{
			st.setCond(9);
			NpcInstance n = st.findTemplate(30766);
			if(n != null)
			{
				Functions.npcSay(n, "Blood and Honour");
			}
			if((n = st.findTemplate(30759)) != null)
			{
				Functions.npcSay(n, "Ambition and Power");
			}
			if((n = st.findTemplate(30758)) != null)
			{
				Functions.npcSay(n, "War and Death");
			}
		}
		else if(event.equalsIgnoreCase("30766-08.htm"))
		{
			st.takeItems(3869, -1);
			exit503(false, st);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int id = st.getState();
		String htmltext = "noquest";
		boolean isLeader = st.getPlayer().isClanLeader();
		if(id == 1 && npcId == 30760)
		{
			if(st.getPlayer().getClan() != null)
			{
				if(isLeader)
				{
					int clanLevel = st.getPlayer().getClan().getLevel();
					if(st.getQuestItemsCount(3870) > 0)
					{
						htmltext = "30760-03.htm";
						st.exitCurrentQuest(true);
					}
					else if(clanLevel > 3)
					{
						htmltext = "30760-04.htm";
					}
					else
					{
						htmltext = "30760-02.htm";
						st.exitCurrentQuest(true);
					}
				}
				else
				{
					htmltext = "30760-04t.htm";
					st.exitCurrentQuest(true);
				}
			}
			else
			{
				htmltext = "30760-01.htm";
				st.exitCurrentQuest(true);
			}
			return htmltext;
		}
		if(st.getPlayer().getClan() != null && st.getPlayer().getClan().getLevel() == 5)
		{
			return "completed";
		}
		if(isLeader)
		{
			if(st.getCond() == 0)
			{
				st.setCond(1);
			}
			if(st.get("Kurtz") == null)
			{
				st.set("Kurtz", "1");
			}
			if(st.get("Lutz") == null)
			{
				st.set("Lutz", "1");
			}
			if(st.get("Fritz") == null)
			{
				st.set("Fritz", "1");
			}
			int cond = st.getCond();
			int kurtz = st.getInt("Kurtz");
			int lutz = st.getInt("Lutz");
			int fritz = st.getInt("Fritz");
			if(npcId == 30760)
			{
				htmltext = cond == 1 ? "30760-09.htm" : cond == 2 ? "30760-10.htm" : cond == 3 ? "30760-11.htm" : cond == 4 ? "30760-13.htm" : cond == 5 ? "30760-14.htm" : cond == 6 ? "30760-15.htm" : cond == 7 ? "30760-17.htm" : cond == 12 ? "30760-19.htm" : cond == 13 ? "30760-24.htm" : "30760-18.htm";
			}
			else if(npcId == 30645)
			{
				if(cond == 1)
				{
					htmltext = "30645-02.htm";
				}
				else if(cond == 2)
				{
					if(checkEggs(st) && kurtz > 1 && lutz > 1 && fritz > 1)
					{
						htmltext = "30645-05.htm";
						st.setCond(3);
						for(int item : EggList)
						{
							st.takeItems(item, -1);
						}
					}
					else
					{
						htmltext = "30645-04.htm";
					}
				}
				else
				{
					htmltext = cond == 3 ? "30645-07.htm" : "30645-08.htm";
				}
			}
			else if(npcId == 30762 && cond == 2)
			{
				htmltext = "30762-01.htm";
			}
			else if(npcId == 30763 && cond == 2)
			{
				htmltext = "30763-01.htm";
			}
			else if(npcId == 30761 && cond == 2)
			{
				htmltext = "30761-01.htm";
			}
			else if(npcId == 30512)
			{
				htmltext = kurtz == 1 ? "30512-01.htm" : kurtz == 2 ? "30512-02.htm" : "30512-04.htm";
			}
			else if(npcId == 30764)
			{
				if(cond == 4)
				{
					htmltext = kurtz > 2 ? "30764-04.htm" : "30764-02.htm";
				}
				else if(cond == 5)
				{
					if(st.getQuestItemsCount(3846) > 9 && st.getQuestItemsCount(3844) > 9)
					{
						htmltext = "30764-08.htm";
						st.takeItems(3846, -1);
						st.takeItems(3844, -1);
						st.takeItems(3843, -1);
						st.setCond(6);
					}
					else
					{
						htmltext = "30764-07.htm";
					}
				}
				else if(cond == 6)
				{
					htmltext = "30764-09.htm";
				}
			}
			else if(npcId == 30868)
			{
				if(cond == 7)
				{
					htmltext = "30868-02.htm";
				}
				else if(cond == 8)
				{
					htmltext = "30868-05.htm";
				}
				else if(cond == 9)
				{
					htmltext = "30868-06.htm";
				}
				else if(cond == 10)
				{
					htmltext = "30868-08.htm";
				}
				else if(cond == 11)
				{
					htmltext = "30868-09.htm";
				}
				else if(cond == 12)
				{
					htmltext = "30868-11.htm";
				}
			}
			else if(npcId == 30766)
			{
				if(cond == 8)
				{
					htmltext = "30766-02.htm";
				}
				else if(cond == 9)
				{
					htmltext = "30766-05.htm";
				}
				else if(cond == 10)
				{
					htmltext = "30766-06.htm";
				}
				else if(cond == 11 || cond == 12 || cond == 13)
				{
					htmltext = "30766-07.htm";
				}
			}
			else if(npcId == 30765)
			{
				if(st.getCond() == 10)
				{
					if(st.getQuestItemsCount(3847) < 6)
					{
						htmltext = "30765-03a.htm";
					}
					else if(st.getInt("ImpGraveKeeper") == 3)
					{
						htmltext = "30765-02.htm";
						st.setCond(11);
						st.takeItems(3847, 6);
						st.giveItems(3869, 1);
					}
					else
					{
						htmltext = "<html><head><body>(You and your Clan didn't kill the Imperial Gravekeeper by your own, do it try again.)</body></html>";
					}
				}
				else
				{
					htmltext = "<html><head><body>(You already have the Scepter of Judgement.)</body></html>";
				}
			}
			else if(npcId == 30759)
			{
				htmltext = "30759-01.htm";
			}
			else if(npcId == 30758)
			{
				htmltext = "30758-01.htm";
			}
			return htmltext;
		}
		int cond = getLeaderVar(st, "cond");
		if(npcId == 30645 && (cond == 1 || cond == 2 || cond == 3))
		{
			htmltext = "30645-01.htm";
		}
		else if(npcId == 30868)
		{
			if(cond == 9 || cond == 10)
			{
				htmltext = "30868-07.htm";
			}
			else if(cond == 7)
			{
				htmltext = "30868-01.htm";
			}
		}
		else if(npcId == 30764 && cond == 4)
		{
			htmltext = "30764-01.htm";
		}
		else if(npcId == 30766 && cond == 8)
		{
			htmltext = "30766-01.htm";
		}
		else if(npcId == 30512 && cond > 2 && cond < 6)
		{
			htmltext = "30512-01a.htm";
		}
		else if(npcId == 30765 && cond == 10)
		{
			htmltext = "30765-01.htm";
		}
		else if(npcId == 30760)
		{
			if(cond == 3)
			{
				htmltext = "30760-11t.htm";
			}
			else if(cond == 4)
			{
				htmltext = "30760-15t.htm";
			}
			else if(cond == 12)
			{
				htmltext = "30760-19t.htm";
			}
			else if(cond == 13)
			{
				htmltext = "30766-24t.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(NpcInstance npc, QuestState st)
	{
		if((double) (npc.getMaxHp() / 2) > npc.getCurrentHp() && Rnd.chance(4))
		{
			int ImpGraveKepperStat = getLeaderVar(st, "ImpGraveKeeper");
			if(ImpGraveKepperStat == 1)
			{
				for(int i = 1;i <= 4;++i)
				{
					st.addSpawn(27180, 120000);
				}
				setLeaderVar(st, "ImpGraveKeeper", "2");
			}
			else
			{
				Player player;
				List players = World.getAroundPlayers(npc, 900, 200);
				if(players.size() > 0 && (player = (Player) players.get(Rnd.get(players.size()))) != null)
				{
					player.teleToLocation(185462, 20342, -3250);
				}
			}
		}
		return null;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = getLeaderVar(st, "cond");
		block:
		switch(cond)
		{
			case 2:
			{
				switch(npcId)
				{
					case 20282:
					{
						if(!Rnd.chance(20))
							break;
						giveItem(3842, 10, st);
						break block;
					}
					case 20243:
					{
						if(!Rnd.chance(15))
							break;
						giveItem(3842, 10, st);
						break block;
					}
					case 20137:
					{
						if(!Rnd.chance(20))
							break;
						giveItem(3841, 10, st);
						break block;
					}
					case 20285:
					{
						if(!Rnd.chance(25))
							break;
						giveItem(3841, 10, st);
						break block;
					}
					case 27178:
					{
						giveItem(3840, 10, st);
					}
				}
				break;
			}
			case 5:
			{
				int chance = 0;
				switch(npcId)
				{
					case 20654:
					{
						chance = 25;
						break;
					}
					case 20656:
					{
						chance = 35;
					}
				}
				if(chance <= 0 || !Rnd.chance(chance))
					break;
				switch(Rnd.get(3))
				{
					case 0:
					{
						if(getLeaderVar(st, "Kurtz") < 4)
						{
							return null;
						}
						giveItem(3845, 40, st);
						break block;
					}
					case 1:
					{
						giveItem(3846, 10, st);
						break block;
					}
					case 2:
					{
						giveItem(3844, 10, st);
					}
				}
				break;
			}
			case 10:
			{
				switch(npcId)
				{
					case 20668:
					{
						if(!Rnd.chance(15))
							break block;
						st.addSpawn(27179, 120000);
						break block;
					}
					case 27179:
					{
						if(!Rnd.chance(80))
							break block;
						giveItem(3847, 6, st);
						break block;
					}
					case 27181:
					{
						NpcInstance spawnedNpc = st.addSpawn(30765, 120000);
						Functions.npcSay(spawnedNpc, "Curse of the gods on the one that defiles the property of the empire!");
						setLeaderVar(st, "ImpGraveKeeper", "3");
					}
				}
			}
		}
		return null;
	}
}