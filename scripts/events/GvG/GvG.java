package events.GvG;

import instances.GvGInstance;
import l2.commons.dbutils.DbUtils;
import l2.commons.lang.reference.HardReference;
import l2.commons.lang.reference.HardReferences;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.data.xml.holder.InstantZoneHolder;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.instancemanager.ServerVariables;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.model.entity.oly.ParticipantPool;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.templates.InstantZone;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GvG extends Functions implements ScriptFile
{
	public static final Location TEAM1_LOC = new Location(80296, 88504, -2880);
	public static final Location TEAM2_LOC = new Location(77704, 93400, -2880);
	public static final Location RETURN_LOC = new Location(43816, -48232, -822);
	private static final Logger _log = LoggerFactory.getLogger(GvG.class);
	private static final String EVENT_NAME = "GvG";
	private static final long regActiveTime = 600000;
	private static final List<HardReference<Player>> leaderList;
	private static boolean _active;
	private static boolean _isRegistrationActive;
	private static ScheduledFuture<?> _globalTask;
	private static ScheduledFuture<?> _regTask;
	private static ScheduledFuture<?> _countdownTask1;
	private static ScheduledFuture<?> _countdownTask2;
	private static ScheduledFuture<?> _countdownTask3;
	
	static
	{
		leaderList = new CopyOnWriteArrayList<>();
	}
	
	private static boolean isActive()
	{
		return IsActive("GvG");
	}
	
	private static void initTimer()
	{
		Calendar ci = Calendar.getInstance();
		String startTimeStr = Config.EVENT_GVG_START_TIME;
		String[] startTimeStrParts = startTimeStr.split(":");
		ci.set(11, Integer.parseInt(startTimeStrParts[0]));
		ci.set(12, startTimeStrParts.length > 1 ? Integer.parseInt(startTimeStrParts[1]) : 0);
		ci.set(13, startTimeStrParts.length > 2 ? Integer.parseInt(startTimeStrParts[2]) : 0);
		long delay = ci.getTimeInMillis() - System.currentTimeMillis();
		long day = 86400000;
		if(delay < 0)
		{
			delay += day;
		}
		if(_globalTask != null)
		{
			_globalTask.cancel(true);
		}
		_globalTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Launch(), delay, day);
		_log.info("Event 'GvG' will start at " + TimeUtils.toSimpleFormat(_globalTask.getDelay(TimeUnit.MILLISECONDS) + System.currentTimeMillis()) + ".");
	}
	
	private static boolean canBeStarted()
	{
		for(Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
		{
			if(c.getSiegeEvent() == null || !c.getSiegeEvent().isInProgress())
				continue;
			return false;
		}
		return true;
	}
	
	public static void activateEvent()
	{
		if(isActive() && canBeStarted())
		{
			_regTask = ThreadPoolManager.getInstance().schedule(new RegTask(), Config.EVENT_GVG_REG_TIME);
			if(Config.EVENT_GVG_REG_TIME > 120000)
			{
				if(Config.EVENT_GVG_REG_TIME > 300000)
				{
					_countdownTask3 = ThreadPoolManager.getInstance().schedule(new Countdown(5), Config.EVENT_GVG_REG_TIME - 300000);
				}
				_countdownTask1 = ThreadPoolManager.getInstance().schedule(new Countdown(2), Config.EVENT_GVG_REG_TIME - 120000);
				_countdownTask2 = ThreadPoolManager.getInstance().schedule(new Countdown(1), Config.EVENT_GVG_REG_TIME - 60000);
			}
			ServerVariables.set("GvG", "on");
			_log.info("Event 'GvG' activated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.AnnounceEventStartRegistration", null);
			Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.regtime", new String[] {String.valueOf(Config.EVENT_GVG_REG_TIME / 60000)});
			_active = true;
			_isRegistrationActive = true;
		}
		_log.info("Event 'GvG' will start next at " + TimeUtils.toSimpleFormat(_globalTask.getDelay(TimeUnit.MILLISECONDS) + System.currentTimeMillis()) + ".");
	}
	
	public static void deactivateEvent()
	{
		if(isActive())
		{
			stopTimers();
			ServerVariables.unset("GvG");
			_log.info("Event 'GvG' canceled.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.eventsiscanceled", null);
			_active = false;
			_isRegistrationActive = false;
			leaderList.clear();
		}
	}
	
	private static void stopTimers()
	{
		if(_regTask != null)
		{
			_regTask.cancel(false);
			_regTask = null;
		}
		if(_countdownTask1 != null)
		{
			_countdownTask1.cancel(false);
			_countdownTask1 = null;
		}
		if(_countdownTask2 != null)
		{
			_countdownTask2.cancel(false);
			_countdownTask2 = null;
		}
		if(_countdownTask3 != null)
		{
			_countdownTask3.cancel(false);
			_countdownTask3 = null;
		}
	}
	
	private static void prepare()
	{
		checkPlayers();
		shuffleGroups();
		if(isActive())
		{
			stopTimers();
			_active = false;
			_isRegistrationActive = false;
		}
		if(leaderList.size() < 2)
		{
			leaderList.clear();
			Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.tournamentcanceled", null);
			return;
		}
		Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.tiurnamentstated", null);
		start();
	}
	
	private static int checkPlayer(Player player, boolean doCheckLeadership)
	{
		if(!player.isOnline())
		{
			return 1;
		}
		if(!player.isInParty())
		{
			return 2;
		}
		if(doCheckLeadership && (player.getParty() == null || !player.getParty().isLeader(player)))
		{
			return 4;
		}
		if(player.getParty() == null || player.getParty().getMemberCount() < Config.EVENT_GVG_MIN_PARTY_SIZE)
		{
			return 3;
		}
		if(player.getLevel() < Config.EVENT_GVG_MIN_LEVEL || player.getLevel() > Config.EVENT_GVG_MAX_LEVEL)
		{
			return 5;
		}
		if(player.isMounted())
		{
			return 6;
		}
		if(player.isInDuel())
		{
			return 7;
		}
		if(player.getTeam() != TeamType.NONE)
		{
			return 8;
		}
		if(player.isOlyParticipant() || ParticipantPool.getInstance().isRegistred(player))
		{
			return 9;
		}
		if(player.isTeleporting())
		{
			return 10;
		}
		if(player.getParty().isInDimensionalRift())
		{
			return 11;
		}
		if(player.isCursedWeaponEquipped())
		{
			return 12;
		}
		if(!player.isInPeaceZone())
		{
			return 13;
		}
		if(player.isInObserverMode())
		{
			return 14;
		}
		return 0;
	}
	
	private static void shuffleGroups()
	{
		int rndindex = Rnd.get(leaderList.size());
		if(leaderList.size() % 2 != 0)
		{
			Player expelled = leaderList.remove(rndindex).get();
			if(expelled != null)
			{
				expelled.sendMessage("При формировании списка участников турнира ваша группа была отсеяна. Приносим извинения, попробуйте в следующий раз.");
			}
		}
		for(int i = 0;i < leaderList.size();++i)
		{
			int rndindex2 = Rnd.get(leaderList.size());
			leaderList.set(i, leaderList.set(rndindex2, leaderList.get(i)));
		}
	}
	
	private static void checkPlayers()
	{
		block0:
		for(Player player : HardReferences.unwrap(leaderList))
		{
			if(checkPlayer(player, true) != 0)
			{
				leaderList.remove(player.getRef());
				continue;
			}
			for(Player partymember : player.getParty().getPartyMembers())
			{
				if(checkPlayer(partymember, false) == 0)
					continue;
				player.sendMessage("Ваша группа была дисквалифицирована и снята с участия в турнире так как один или более членов группы нарушил условия участия");
				leaderList.remove(player.getRef());
				continue block0;
			}
		}
	}
	
	public static void updateWinner(Player winner)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO event_data(charId, score) VALUES (?,1) ON DUPLICATE KEY UPDATE score=score+1");
			statement.setInt(1, winner.getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	private static void start()
	{
		int instancedZoneId = 504;
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
		if(iz == null)
		{
			_log.warn("GvG: InstanceZone : " + instancedZoneId + " not found!");
			return;
		}
		for(int i = 0;i < leaderList.size();i += 2)
		{
			Player team1Leader = leaderList.get(i).get();
			Player team2Leader = leaderList.get(i + 1).get();
			GvGInstance r = new GvGInstance();
			r.setTeam1(team1Leader.getParty());
			r.setTeam2(team2Leader.getParty());
			r.init(iz);
			r.setReturnLoc(RETURN_LOC);
			for(Player member : team1Leader.getParty().getPartyMembers())
			{
				Functions.unRide(member);
				Functions.unSummonPet(member, true);
				member.setTransformation(0);
				member.setInstanceReuse(instancedZoneId, System.currentTimeMillis());
				member.dispelBuffs();
				member.teleToLocation(Location.findPointToStay(TEAM1_LOC, 0, 150, r.getGeoIndex()), r);
			}
			for(Player member : team2Leader.getParty().getPartyMembers())
			{
				Functions.unRide(member);
				Functions.unSummonPet(member, true);
				member.setTransformation(0);
				member.setInstanceReuse(instancedZoneId, System.currentTimeMillis());
				member.dispelBuffs();
				member.teleToLocation(Location.findPointToStay(TEAM2_LOC, 0, 150, r.getGeoIndex()), r);
			}
			r.start();
		}
		leaderList.clear();
		_log.info("GvG: Event started successfuly.");
	}
	
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive("GvG", true))
		{
			System.out.println("Event: GvG started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.AnnounceEventStarted", null);
			_log.info("Loaded Event: GvG");
			initTimer();
		}
		else
		{
			player.sendMessage("Event 'Groupe vs Groupe' already started.");
		}
		_active = true;
		show("admin/events/events.htm", player);
	}
	
	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive("GvG", false))
		{
			System.out.println("Event: GvG stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.AnnounceEventStoped", null);
		}
		else
		{
			player.sendMessage("Event 'Groupe vs Groupe' not started.");
		}
		_active = false;
		show("admin/events/events.htm", player);
	}
	
	@Override
	public void onLoad()
	{
		if(isActive())
		{
			_active = true;
			initTimer();
			_log.info("Loaded Event: GvG [state: activated]");
		}
		else
		{
			_log.info("Loaded Event: GvG [state: deactivated]");
		}
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	public void showStats()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(!isActive())
		{
			player.sendMessage("Groupe vs Groupe event is not launched");
			return;
		}
		StringBuilder string = new StringBuilder();
		String refresh = "<button value=\"Refresh\" action=\"bypass -h scripts_events.GvG.GvG:showStats\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		if(!leaderList.isEmpty())
		{
			int i = 0;
			for(Player leader : HardReferences.unwrap(leaderList))
			{
				if(!leader.isInParty())
					continue;
				string.append("*").append(leader.getName()).append("*").append(" | group members: ").append(leader.getParty().getMemberCount()).append("\n\n");
				++i;
			}
			String start = "<button value=\"Start Now\" action=\"bypass -h scripts_events.GvG.GvG:startNow\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
			show("There are " + i + " group leaders who registered for the event:\n\n" + string + "\n\n" + refresh + "\n\n" + start, player, null, (Object[]) new Object[0]);
		}
		else
		{
			show("There are no participants at the time\n\n" + refresh, player, null, (Object[]) new Object[0]);
		}
	}
	
	public void startNow()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(!isActive() || !canBeStarted())
		{
			player.sendMessage("Groupe vs Groupe event is not launched");
			return;
		}
		prepare();
	}
	
	public void addGroup()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!_isRegistrationActive)
		{
			player.sendMessage(new CustomMessage("scripts.event.gvg.notactived", player));
			return;
		}
		if(leaderList.contains(player.getRef()))
		{
			player.sendMessage(new CustomMessage("scripts.event.gvg.registred", player));
			return;
		}
		if(!player.isInParty())
		{
			player.sendMessage(new CustomMessage("scripts.event.gvg.notinparty", player));
			return;
		}
		if(!player.getParty().isLeader(player))
		{
			player.sendMessage(new CustomMessage("scripts.event.gvg.onlypartyleader", player));
			return;
		}
		if(player.getParty().isInCommandChannel())
		{
			player.sendMessage(new CustomMessage("scripts.event.gvg.removecommandchannel", player));
			return;
		}
		if(leaderList.size() >= Config.EVENT_GVG_GROUPS_LIMIT)
		{
			player.sendMessage(new CustomMessage("scripts.event.gvg.limitpartycount", player));
			return;
		}
		List<Player> party = player.getParty().getPartyMembers();
		String[] abuseReason = {"не находится в игре", "не находится в группе", "состоит в неполной группе. Минимальное кол-во членов группы - 6.", "не является лидером группы, подававшей заявку", "не соответствует требованиям уровней для турнира", "использует ездовое животное, что противоречит требованиям турнира", "находится в дуэли, что противоречит требованиям турнира", "принимает участие в другом эвенте, что противоречит требованиям турнира", "находится в списке ожидания Олимпиады или принимает участие в ней", "находится в состоянии телепортации, что противоречит требованиям турнира", "находится в Dimensional Rift, что противоречит требованиям турнира", "обладает Проклятым Оружием, что противоречит требованиям турнира", "не находится в мирной зоне", "находится в режиме обозревания"};
		for(Player eachmember : party)
		{
			int abuseId = checkPlayer(eachmember, false);
			if(abuseId == 0)
				continue;
			player.sendMessage("Player " + eachmember.getName() + " " + abuseReason[abuseId - 1]);
			return;
		}
		leaderList.add(player.getRef());
		player.getParty().broadcastMessageToPartyMembers("Ваша группа внесена в список ожидания. Пожалуйста, не регистрируйтесь в других ивентах и не участвуйте в дуэлях до начала турнира.");
	}
	
	public static class Launch extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			activateEvent();
		}
	}
	
	public static class Countdown extends RunnableImpl
	{
		int _timer;
		
		public Countdown(int timer)
		{
			_timer = timer;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.timeexpend", new String[] {Integer.toString(_timer)});
		}
	}
	
	public static class RegTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			prepare();
		}
	}
}