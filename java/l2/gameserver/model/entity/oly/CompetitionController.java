package l2.gameserver.model.entity.oly;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.StringHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.entity.oly.participants.SinglePlayerParticipant;
import l2.gameserver.model.entity.oly.participants.TeamParticipant;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.PlaySound;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.TimeUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

public class CompetitionController
{
	public static final int COMPETITION_PAUSE = 30000;
	public static final int STADIUM_TELEPORT_DELAY = 45;
	public static final int COMPETITION_PREPARATION_DELAY = 60;
	public static final int COMPETITION_DELAY = 300;
	public static final int BACKPORT_DELAY = 20;
	private static final Logger _log = LoggerFactory.getLogger(CompetitionController.class);
	private static final String GET_COMP_RECORDS = "SELECT `oc`.`char_id` AS `char_obj_id`, `on1`.`char_name` AS `char_name`, `on1`.`class_id` AS `char_class_id`, `on2`.`char_id` AS `rival_obj_id`, `on2`.`char_name` AS `rival_name`, `on2`.`class_id` AS `rival_class_id`, `oc`.`result` AS `result`, `oc`.`rule` AS `rules`, `oc`.`elapsed_time` AS `elapsed_time`, `oc`.`mtime` AS `mtime` FROM `oly_comps` AS `oc` JOIN `oly_nobles` AS `on1` ON `oc`.`char_id` = `on1`.`char_id` JOIN `oly_nobles` AS `on2` ON `oc`.`rival_id` = `on2`.`char_id` WHERE `oc`.`char_id` = ? AND `oc`.`season` = ? ";
	private static final String ADD_COMP_RECORD = "INSERT INTO `oly_comps` (`season`, `char_id`, `rival_id`, `rule`, `result`, `elapsed_time`, `mtime`) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static CompetitionController _instance;
	private final ConcurrentLinkedQueue<Competition> _activeCompetitions = new ConcurrentLinkedQueue();
	private ScheduledFuture<?> _start_task;
	private int _start_fail_trys;
	
	private CompetitionController()
	{
	}
	
	public static final CompetitionController getInstance()
	{
		if(_instance == null)
		{
			_instance = new CompetitionController();
		}
		return _instance;
	}
	
	public boolean isActiveCompetitionInPrgress()
	{
		return !_activeCompetitions.isEmpty();
	}
	
	public Collection<Competition> getCompetitions()
	{
		return _activeCompetitions;
	}
	
	private final synchronized boolean TryCreateCompetitions(CompetitionType type, int cls_id)
	{
		if(!StadiumPool.getInstance().isStadiumAvailable())
		{
			_log.warn("OlyCompetitionController: not enough stadiums.");
			return false;
		}
		if(!ParticipantPool.getInstance().isEnough(type, cls_id))
		{
			ParticipantPool.getInstance().broadcastToEntrys(type, Msg.THE_MATCH_MAY_BE_DELAYED_DUE_TO_NOT_ENOUGH_COMBATANTS, cls_id);
			return false;
		}
		Player[][] participants = ParticipantPool.getInstance().retrieveEntrys(type, cls_id);
		while(OlyController.getInstance().isRegAllowed() && StadiumPool.getInstance().isStadiumAvailable() && participants != null && participants[0] != null && participants[1] != null)
		{
			Stadium stadium = StadiumPool.getInstance().pollStadium();
			if(stadium == null)
			{
				_log.error("OlyCompetitionController: stadium == null wtf?");
				return false;
			}
			StartCompetition(type, stadium, participants[0], participants[1]);
			participants = ParticipantPool.getInstance().retrieveEntrys(type, cls_id);
		}
		return true;
	}
	
	private void StartCompetition(CompetitionType type, Stadium stadium, Player[] p0, Player[] p1)
	{
		Competition comp = new Competition(type, stadium);
		if(type == CompetitionType.TEAM_CLASS_FREE)
		{
			comp.setPlayers(new Participant[] {new TeamParticipant(Participant.SIDE_BLUE, comp, p0), new TeamParticipant(Participant.SIDE_RED, comp, p1)});
		}
		else if(type == CompetitionType.CLASS_FREE || type == CompetitionType.CLASS_INDIVIDUAL)
		{
			comp.setPlayers(new Participant[] {new SinglePlayerParticipant(Participant.SIDE_BLUE, comp, p0[0]), new SinglePlayerParticipant(Participant.SIDE_RED, comp, p1[0])});
		}
		comp.scheduleTask(new StadiumTeleportTask(comp), 100);
		comp.start();
		_activeCompetitions.add(comp);
	}
	
	public void FinishCompetition(Competition comp)
	{
		if(comp == null)
		{
			return;
		}
		try
		{
			comp.finish();
			if(comp.getState() != CompetitionState.INIT)
			{
				comp.teleportParticipantsBack();
			}
			StadiumPool.getInstance().putStadium(comp.getStadium());
			_activeCompetitions.remove(comp);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private boolean RunComps(CompetitionType type)
	{
		if(type == CompetitionType.CLASS_INDIVIDUAL)
		{
			for(ClassId cid : ClassId.values())
			{
				if(cid.level() != 3)
					continue;
				TryCreateCompetitions(CompetitionType.CLASS_INDIVIDUAL, cid.getId());
			}
			boolean ret = false;
			return ret;
		}
		return TryCreateCompetitions(type, 0);
	}
	
	public void scheduleStartTask()
	{
		if(OlyController.getInstance().isRegAllowed())
		{
			_start_task = ThreadPoolManager.getInstance().schedule(new CompetitionStarterTask(), Math.min(30000 * (_start_fail_trys + 1), 60000));
		}
	}
	
	public void cancelStartTask()
	{
		if(_start_task != null)
		{
			_start_task.cancel(true);
			_start_task = null;
		}
	}
	
	public synchronized SystemMessage AddParticipationRequest(CompetitionType type, Player[] players)
	{
		if(!OlyController.getInstance().isRegAllowed())
		{
			return Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS;
		}
		for(Player noble : players)
		{
			if(!noble.isNoble())
			{
				return new SystemMessage(1501).addName(noble);
			}
			if(noble.isInDuel())
			{
				return new SystemMessage(1599);
			}
			if(noble.getBaseClassId() != noble.getClassId().getId() || noble.getClassId().getLevel() < 4)
			{
				return new SystemMessage(1500).addName(noble);
			}
			if(ParticipantPool.getInstance().isRegistred(noble))
			{
				return new SystemMessage(1502).addName(noble);
			}
			if((double) noble.getInventoryLimit() * 0.8 <= (double) noble.getInventory().getSize())
			{
				return new SystemMessage(1691).addName(noble);
			}
			if(noble.isCursedWeaponEquipped())
			{
				return new SystemMessage(1857).addName(noble).addItemName(noble.getCursedWeaponEquippedId());
			}
			if(NoblesController.getInstance().getPointsOf(noble.getObjectId()) < 1)
			{
				return new SystemMessage(2941);
			}
			if(Config.OLY_RESTRICT_CLASS_IDS.length > 0 && ArrayUtils.contains(Config.OLY_RESTRICT_CLASS_IDS, noble.getActiveClassId()))
			{
				return new SystemMessage(SystemMsg.S1).addString(new CustomMessage("olympiad.restrictedclasses", noble, new Object[0]).toString());
			}
			if(Config.OLY_RESTRICT_HWID && noble.getNetConnection().getHwid() != null && ParticipantPool.getInstance().isHWIDRegistred(noble.getNetConnection().getHwid()))
			{
				return new SystemMessage(SystemMsg.S1).addString(new CustomMessage("olympiad.iphwid.check", noble, new Object[0]).toString());
			}
			if(!Config.OLY_RESTRICT_IP || noble.getNetConnection().getIpAddr() == null || !ParticipantPool.getInstance().isIPRegistred(noble.getNetConnection().getIpAddr()))
				continue;
			return new SystemMessage(SystemMsg.S1).addString(new CustomMessage("olympiad.iphwid.check", noble, new Object[0]).toString());
		}
		ParticipantPool.getInstance().createEntry(type, players);
		switch(type)
		{
			case CLASS_INDIVIDUAL:
			{
				return Msg.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES;
			}
			case CLASS_FREE:
			{
				return Msg.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES;
			}
			case TEAM_CLASS_FREE:
			{
				return Msg.YOU_HAVE_REGISTERED_ON_THE_WAITING_LIST_FOR_THE_NON_CLASS_LIMITED_TEAM_MATCH_EVENT;
			}
		}
		return null;
	}
	
	public void scheduleFinishCompetition(Competition comp, int count_down, long delay)
	{
		comp.scheduleTask(new FinishCompetitionTask(comp, count_down), delay);
	}
	
	public void scheduleCompetitionPreparation(Competition comp)
	{
		comp.scheduleTask(new CompetitionPreparationTask(comp), 1000);
	}
	
	public void addCompetitionResult(int season, NoblesController.NobleRecord winner, int win_points, NoblesController.NobleRecord looser, int loose_points, CompetitionType type, boolean tie, boolean disconn, long elapsed_time)
	{
		if(winner == null || looser == null || type == null)
		{
			return;
		}
		if(disconn)
		{
			Log.add(String.format("CompetitionResult: %s(%d) - %d disconnected against %s(%d) in %s", looser.char_name, looser.char_id, loose_points, winner.char_name, winner.char_id, type.name()), "olympiad");
		}
		else if(!tie)
		{
			Log.add(String.format("CompetitionResult: %s(%d) + %d win against %s(%d) - %d in %s", winner.char_name, winner.char_id, win_points, looser.char_name, looser.char_id, loose_points, type.name()), "olympiad");
		}
		else
		{
			Log.add(String.format("CompetitionResult: %s(%d) tie against %s(%d) in %s", winner.char_name, winner.char_id, looser.char_name, looser.char_id, type.name()), "olympiad");
		}
		Connection conn = null;
		PreparedStatement pstmt = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			pstmt = conn.prepareStatement("INSERT INTO `oly_comps` (`season`, `char_id`, `rival_id`, `rule`, `result`, `elapsed_time`, `mtime`) VALUES (?, ?, ?, ?, ?, ?, ?)");
			if(!disconn)
			{
				pstmt.setInt(1, season);
				pstmt.setInt(2, winner.char_id);
				pstmt.setInt(3, looser.char_id);
				pstmt.setInt(4, type.getTypeIdx());
				pstmt.setByte(5, (byte) (tie ? 0 : 1));
				pstmt.setInt(6, (int) elapsed_time);
				pstmt.setInt(7, (int) (System.currentTimeMillis() / 1000));
				pstmt.executeUpdate();
			}
			pstmt.setInt(1, season);
			pstmt.setInt(2, looser.char_id);
			pstmt.setInt(3, winner.char_id);
			pstmt.setInt(4, type.getTypeIdx());
			pstmt.setByte(5, (byte) (tie ? 0 : -1));
			pstmt.setInt(6, (int) elapsed_time);
			pstmt.setInt(7, (int) (System.currentTimeMillis() / 1000));
			pstmt.executeUpdate();
		}
		catch(Exception e)
		{
			_log.warn("Can't save competition result", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, pstmt);
		}
	}
	
	public Collection<CompetitionResults> getCompetitionResults(int obj_id, int season)
	{
		ArrayList<CompetitionResults> result = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			pstmt = conn.prepareStatement("SELECT `oc`.`char_id` AS `char_obj_id`, `on1`.`char_name` AS `char_name`, `on1`.`class_id` AS `char_class_id`, `on2`.`char_id` AS `rival_obj_id`, `on2`.`char_name` AS `rival_name`, `on2`.`class_id` AS `rival_class_id`, `oc`.`result` AS `result`, `oc`.`rule` AS `rules`, `oc`.`elapsed_time` AS `elapsed_time`, `oc`.`mtime` AS `mtime` FROM `oly_comps` AS `oc` JOIN `oly_nobles` AS `on1` ON `oc`.`char_id` = `on1`.`char_id` JOIN `oly_nobles` AS `on2` ON `oc`.`rival_id` = `on2`.`char_id` WHERE `oc`.`char_id` = ? AND `oc`.`season` = ? ");
			pstmt.setInt(1, obj_id);
			pstmt.setInt(2, season);
			rset = pstmt.executeQuery();
			while(rset.next())
			{
				result.add(new CompetitionResults(rset.getInt("char_obj_id"), rset.getInt("char_class_id"), rset.getString("char_name"), rset.getInt("rival_obj_id"), rset.getInt("rival_class_id"), rset.getString("rival_name"), CompetitionType.getTypeOf(rset.getByte("rules")), rset.getByte("result"), rset.getInt("elapsed_time"), rset.getLong("mtime")));
			}
		}
		catch(Exception e)
		{
			_log.warn("Can't load competitions records", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, pstmt, rset);
		}
		return result;
	}
	
	public void showCompetitionList(Player player)
	{
		if(!OlyController.getInstance().isRegAllowed())
		{
			player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			return;
		}
		if(player.isOlyParticipant() || ParticipantPool.getInstance().isRegistred(player))
		{
			player.sendPacket(Msg.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
			return;
		}
		StringBuilder sb = new StringBuilder();
		for(Stadium stadium : StadiumPool.getInstance().getAllStadiums())
		{
			sb.append("<a action=\"bypass -h _olympiad?command=move_op_field&field=").append(stadium.getStadiumId() + 1).append("\">");
			sb.append(new CustomMessage("Olympiad.CompetitionState.ARENA", player)).append(stadium.getStadiumId() + 1);
			sb.append("&nbsp;&nbsp;&nbsp;");
			boolean isEmpty = true;
			for(Competition comp : _activeCompetitions)
			{
				if(comp.getStadium() != stadium || comp.getState() == CompetitionState.INIT)
					continue;
				sb.append(comp._participants[0].getName()).append(" : ").append(comp._participants[1].getName());
				sb.append("&nbsp;");
				switch(comp.getState())
				{
					case STAND_BY:
					{
						sb.append(new CustomMessage("Olympiad.CompetitionState.STAND_BY", player));
						break;
					}
					case PLAYING:
					{
						sb.append(new CustomMessage("Olympiad.CompetitionState.PLAYING", player));
						break;
					}
					case FINISH:
					{
						sb.append(new CustomMessage("Olympiad.CompetitionState.FINISH", player));
					}
				}
				isEmpty = false;
			}
			if(isEmpty)
			{
				sb.append(new CustomMessage("Olympiad.CompetitionState.EMPTY", player));
			}
			sb.append("</a><br>");
		}
		NpcHtmlMessage html = new NpcHtmlMessage(player, null);
		html.setFile("oly/arenas.htm");
		html.replace("%arenas%", sb.toString());
		player.sendPacket(html);
	}
	
	public void watchCompetition(Player player, int stadium_id)
	{
		if(!OlyController.getInstance().isRegAllowed())
		{
			player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			return;
		}
		if(player.getPet() != null || player.isMounted())
		{
			return;
		}
		if(player.isInStoreMode())
		{
			return;
		}
		if(stadium_id < 1 || stadium_id > 22)
		{
			return;
		}
		if(player.isOlyParticipant() || ParticipantPool.getInstance().isRegistred(player))
		{
			player.sendPacket(Msg.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
			return;
		}
		Stadium stadium = StadiumPool.getInstance().getStadium(stadium_id - 1);
		if(stadium.getObserverCount() > Config.OLY_MAX_SPECTATORS_PER_STADIUM)
		{
			player.sendMessage("To many observers on this stadium.");
			return;
		}
		if(player.isOlyObserver())
		{
			player.switchOlympiadObserverArena(stadium);
		}
		else
		{
			player.enterOlympiadObserverMode(stadium);
		}
	}
	
	public class CompetitionResults
	{
		int char_id;
		int rival_id;
		String char_name;
		String rival_name;
		byte result;
		int char_class_id;
		int rival_class_id;
		int elapsed_time;
		CompetitionType type;
		long mtime;
		
		private CompetitionResults(int _wid, int _wcid, String _wn, int _lid, int _lcid, String _ln, CompetitionType _type, byte _r, int _et, long _mtime)
		{
			char_id = _wid;
			char_class_id = _wcid;
			char_name = _wn;
			rival_id = _lid;
			rival_name = _ln;
			rival_class_id = _lcid;
			type = _type;
			result = _r;
			elapsed_time = _et;
			mtime = _mtime;
		}
		
		public String toString(Player player, MutableInt wins, MutableInt looses, MutableInt ties)
		{
			String main = null;
			if(result == 0)
			{
				main = StringHolder.getInstance().getNotNull(player, "hero.history.tie");
			}
			else if(result > 0)
			{
				main = StringHolder.getInstance().getNotNull(player, "hero.history.win");
			}
			else if(result < 2)
			{
				main = StringHolder.getInstance().getNotNull(player, "hero.history.loss");
			}
			if(result > 0)
			{
				wins.increment();
			}
			else if(result == 0)
			{
				ties.increment();
			}
			else if(result < 0)
			{
				looses.increment();
			}
			main = main.replace("%classId%", String.valueOf(rival_class_id));
			main = main.replace("%name%", rival_name);
			main = main.replace("%date%", TimeUtils.toHeroRecordFormat(mtime));
			main = main.replace("%time%", String.format("%02d:%02d", elapsed_time / 60, elapsed_time % 60));
			main = main.replace("%victory_count%", wins.toString());
			main = main.replace("%tie_count%", ties.toString());
			main = main.replace("%loss_count%", looses.toString());
			return main;
		}
	}
	
	public class FinishCompetitionTask implements Runnable
	{
		private final Competition _game;
		private int _countdown;
		
		public FinishCompetitionTask(Competition game, int countdown)
		{
			_game = game;
			_countdown = countdown;
		}
		
		@Override
		public void run()
		{
			if(_game.getState() != CompetitionState.FINISH)
			{
				_game.setState(CompetitionState.FINISH);
				_game.ValidateWinner();
				_game.scheduleTask(new FinishCompetitionTask(_game, 20), 100);
			}
			else if(_game.getState() == CompetitionState.FINISH)
			{
				if(_countdown > 0)
				{
					_game.broadcastPacket(new SystemMessage(1499).addNumber(_countdown), true, false);
					int dur = _countdown > 5 ? _countdown / 2 : 1;
					_countdown -= dur;
					_game.scheduleTask(new FinishCompetitionTask(_game, _countdown), dur * 1000);
				}
				else
				{
					getInstance().FinishCompetition(_game);
				}
			}
		}
	}
	
	public class CompetitionPreparationTask implements Runnable
	{
		private final Competition _game;
		private int _countdown;
		
		public CompetitionPreparationTask(Competition game)
		{
			this(game, 60);
		}
		
		public CompetitionPreparationTask(Competition game, int countdown)
		{
			_game = game;
			_countdown = countdown;
		}
		
		@Override
		public void run()
		{
			if(_countdown > 0)
			{
				if(_countdown < 10 || _countdown % 10 == 0)
				{
					_game.broadcastPacket(new SystemMessage(1495).addNumber(_countdown), true, true);
				}
				long delay = 1000;
				switch(_countdown)
				{
					case 55:
					case 60:
					{
						_countdown -= 5;
						delay = 5000;
						break;
					}
					case 20:
					case 30:
					case 40:
					case 50:
					{
						_countdown -= 10;
						delay = 10000;
						break;
					}
					case 10:
					{
						_countdown -= 5;
						delay = 5000;
						break;
					}
					case 5:
					{
						_game.applyBuffs();
					}
					case 1:
					case 2:
					case 3:
					case 4:
					{
						--_countdown;
						delay = 1000;
					}
				}
				_game.scheduleTask(new CompetitionPreparationTask(_game, _countdown), _countdown > 0 ? delay : 2000);
			}
			else
			{
				_game.getStadium().setZonesActive(true);
				_game.restoreHPCPMP();
				_game.broadcastEverybodyOlympiadUserInfo();
				_game.broadcastEverybodyEffectIcons();
				_game.broadcastPacket(new PlaySound("ns17_f"), true, true);
				_game.broadcastPacket(Msg.STARTS_THE_GAME, true, true);
				_game.setState(CompetitionState.PLAYING);
				getInstance().scheduleFinishCompetition(_game, -1, 300000);
			}
		}
	}
	
	public class StadiumTeleportTask implements Runnable
	{
		private final Competition _game;
		private int _countdown;
		
		public StadiumTeleportTask(Competition game)
		{
			this(game, 45);
		}
		
		public StadiumTeleportTask(Competition game, int countdown)
		{
			_game = game;
			_countdown = countdown;
			if(_game.getState() == null)
			{
				_game.setState(CompetitionState.INIT);
			}
		}
		
		@Override
		public void run()
		{
			if(_countdown > 0)
			{
				_game.broadcastPacket(new SystemMessage(1492).addNumber(_countdown), true, false);
				long delay = 1000;
				switch(_countdown)
				{
					case 30:
					case 45:
					{
						_countdown -= 15;
						delay = 15000;
						break;
					}
					case 15:
					{
						_countdown = 5;
						delay = 5000;
						break;
					}
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
					{
						--_countdown;
						delay = 1000;
					}
				}
				if(_game.ValidateParticipants())
				{
					return;
				}
				_game.scheduleTask(new StadiumTeleportTask(_game, _countdown), _countdown > 0 ? delay : 1000);
			}
			else
			{
				if(_game.ValidateParticipants())
				{
					return;
				}
				_game.getStadium().setZonesActive(false);
				_game.teleportParticipantsOnStadium();
				_game.setState(CompetitionState.STAND_BY);
				getInstance().scheduleCompetitionPreparation(_game);
				OlyController.getInstance().announceCompetition(_game.getType(), _game.getStadium().getStadiumId());
			}
		}
	}
	
	private class CompetitionStarterTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if(!OlyController.getInstance().isRegAllowed())
				{
					return;
				}
				for(CompetitionType type : CompetitionType.values())
				{
					if(RunComps(type))
					{
						getInstance()._start_fail_trys = 0;
						continue;
					}
					if(getInstance()._start_fail_trys >= 5)
						continue;
					getInstance()._start_fail_trys++;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				getInstance().scheduleStartTask();
			}
		}
	}
}