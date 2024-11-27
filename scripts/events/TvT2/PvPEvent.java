package events.TvT2;

import l2.commons.util.Rnd;
import l2.gameserver.Announcements;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.InstantZoneHolder;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.instancemanager.ServerVariables;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.listener.actor.player.OnPlayerExitListener;
import l2.gameserver.listener.actor.player.OnTeleportListener;
import l2.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Zone;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.entity.oly.ParticipantPool;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.attachment.FlagItemAttachment;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.IStaticPacket;
import l2.gameserver.network.l2.s2c.ExEventMatchMessage;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.NickNameChanged;
import l2.gameserver.network.l2.s2c.Revive;
import l2.gameserver.network.l2.s2c.SocialAction;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.scripts.Scripts;
import l2.gameserver.templates.InstantZone;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.PositionUtils;
import l2.gameserver.utils.TimeUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.GlobalServices;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PvPEvent extends Functions implements ScriptFile
{
	public static final String EVENT_NAME = "PvP";
	public static final String VAR_EVENT_ACTIVE = "PvP_is_active";
	public static final String VAR_START_TIME = "PvP_start_time";
	public static final String VAR_ANNOUNCE_TIME = "PvP_announce_time";
	public static final String VAR_ANNOUNCE_REDUCT = "PvP_announce_reduct";
	public static final String VAR_INSTANCES_IDS = "PvP_instances_ids";
	private static final Logger _log = LoggerFactory.getLogger(PvPEvent.class);
	private static final long RANK_BROADCAST_TIME = 20000;
	private static PvPEvent _instance;
	private DieListner _dieListner;
	private ZoneEnterLeaveListner _zoneListner;
	private ExitListner _exitListner;
	private boolean _event_active;
	private String _event_start_time;
	private int _event_announce_time;
	private int _event_announce_reductor;
	private int[] _event_instances_ids;
	private PvPEventState _state;
	private PvPEventRule _rule;
	private Pair<String, int[]> _lastProhibitedClassIds;
	private ScheduledFuture<?> _stateTask;
	private ScheduledFuture<?> _processTask;
	private Collection<Integer> _participants;
	private RegisrationState _regState;
	private Collection<Integer> _desireContainer;
	
	public static final PvPEvent getInstance()
	{
		return _instance;
	}
	
	private static boolean isDesirePlayer(Player player)
	{
		if(player == null || player.getNetConnection() == null || !player.isConnected())
		{
			return false;
		}
		if(player.isDead() || player.isBlocked() || player.isInZone(Zone.ZoneType.epic) || player.isInZone(Zone.ZoneType.SIEGE) || player.isInZone(Zone.ZoneType.SIEGE) || player.getReflectionId() != 0)
		{
			return false;
		}
		if(player.isFishing() || player.getTransformation() != 0 || player.isCursedWeaponEquipped())
		{
			return false;
		}
		if(player.getLevel() < getInstance().config_MinLevel() || player.getLevel() > getInstance().config_MaxLevel())
		{
			return false;
		}
		if(player.isOlyParticipant() || ParticipantPool.getInstance().isRegistred(player))
		{
			return false;
		}
		if(getInstance().config_hwidRestrict() && getInstance().isHWIDRegistred(player.getNetConnection().getHwid(), player))
		{
			return false;
		}
		if(ArrayUtils.contains(getInstance().config_ProhibitedClassIds(), player.getActiveClassId()))
		{
			return false;
		}
		return player.getTeam() == TeamType.NONE && !player.isInDuel();
	}
	
	public void LoadVars()
	{
		_event_active = ServerVariables.getBool("PvP_is_active", false);
		_event_start_time = ServerVariables.getString("PvP_start_time", "");
		_event_announce_time = ServerVariables.getInt("PvP_announce_time", 5);
		_event_announce_reductor = ServerVariables.getInt("PvP_announce_reduct", 1);
		String[] inst_ids = ServerVariables.getString("PvP_instances_ids", "").split("\\s*;\\s*");
		LinkedList<Integer> event_instances_ids = new LinkedList<>();
		int i;
		for(i = 0;i < inst_ids.length;++i)
		{
			String instIdStr = inst_ids[i].trim();
			if(instIdStr.isEmpty())
				continue;
			event_instances_ids.add(Integer.parseInt(instIdStr));
		}
		_event_instances_ids = new int[event_instances_ids.size()];
		for(i = 0;i < event_instances_ids.size();++i)
		{
			_event_instances_ids[i] = event_instances_ids.get(i);
		}
	}
	
	private PvPEventState getState()
	{
		return _state;
	}
	
	private synchronized void setState(PvPEventState state)
	{
		_log.info("PvPEventState changet to " + state.name());
		_state = state;
	}
	
	public PvPEventRule getRule()
	{
		return _rule;
	}
	
	public void setRule(PvPEventRule rule)
	{
		_rule = rule;
	}
	
	public PvPEventRule getNextRule(PvPEventRule rule)
	{
		if(rule == null)
		{
			if(config_isPvPEventRuleEnabled(PvPEventRule.TVT))
			{
				return PvPEventRule.TVT;
			}
			if(config_isPvPEventRuleEnabled(PvPEventRule.CTF))
			{
				return PvPEventRule.CTF;
			}
			if(config_isPvPEventRuleEnabled(PvPEventRule.DM))
			{
				return PvPEventRule.DM;
			}
			return null;
		}
		switch(rule)
		{
			case TVT:
			{
				if(config_isPvPEventRuleEnabled(PvPEventRule.CTF))
				{
					return PvPEventRule.CTF;
				}
				if(config_isPvPEventRuleEnabled(PvPEventRule.DM))
				{
					return PvPEventRule.DM;
				}
				if(!config_isPvPEventRuleEnabled(PvPEventRule.TVT))
					break;
				return PvPEventRule.TVT;
			}
			case CTF:
			{
				if(config_isPvPEventRuleEnabled(PvPEventRule.DM))
				{
					return PvPEventRule.DM;
				}
				if(config_isPvPEventRuleEnabled(PvPEventRule.TVT))
				{
					return PvPEventRule.TVT;
				}
				if(!config_isPvPEventRuleEnabled(PvPEventRule.CTF))
					break;
				return PvPEventRule.CTF;
			}
			case DM:
			{
				if(config_isPvPEventRuleEnabled(PvPEventRule.TVT))
				{
					return PvPEventRule.TVT;
				}
				if(config_isPvPEventRuleEnabled(PvPEventRule.CTF))
				{
					return PvPEventRule.CTF;
				}
				if(!config_isPvPEventRuleEnabled(PvPEventRule.DM))
					break;
				return PvPEventRule.DM;
			}
		}
		return null;
	}
	
	private boolean config_isUseCapcha()
	{
		return ServerVariables.getBool("PvP_" + getRule().name() + "_use_capcha", true);
	}
	
	private boolean config_hwidRestrict()
	{
		return ServerVariables.getBool("PvP_" + getRule().name() + "_use_hwid_restrict", false);
	}
	
	private boolean config_hideIdentiti()
	{
		return ServerVariables.getBool("PvP_" + getRule().name() + "_hide_identiti", false);
	}
	
	private int config_MaxParticipants()
	{
		return ServerVariables.getInt("PvP_" + getRule().name() + "_max_parts", 100);
	}
	
	private int config_ItemPerKill()
	{
		return ServerVariables.getInt("PvP_" + getRule().name() + "_item_per_kill", 0);
	}
	
	private int config_ReviveDelay()
	{
		return ServerVariables.getInt("PvP_" + getRule().name() + "_revive_delay", 1);
	}
	
	private boolean config_isPvPEventRuleEnabled(PvPEventRule rule)
	{
		return ServerVariables.getBool("PvP_" + rule.name() + "_enabled", false);
	}
	
	private int config_ReqParticipants()
	{
		return ServerVariables.getInt("PvP_" + getRule().name() + "_req_parts", 50);
	}
	
	private int config_MinLevel()
	{
		return ServerVariables.getInt("PvP_" + getRule().name() + "_min_lvl", 1);
	}
	
	private int config_MaxLevel()
	{
		return ServerVariables.getInt("PvP_" + getRule().name() + "_max_lvl", 86);
	}
	
	private int config_RewardHeroHours()
	{
		return ServerVariables.getInt("PvP_" + getRule().name() + "_herorevhours", 0);
	}
	
	private int[] config_ProhibitedClassIds()
	{
		String pci = ServerVariables.getString("PvP_" + getRule().name() + "_prohibited_class_ids", "");
		if(pci.isEmpty())
		{
			return ArrayUtils.EMPTY_INT_ARRAY;
		}
		String key = getRule().name() + pci;
		if(_lastProhibitedClassIds != null && key.equals(_lastProhibitedClassIds.getLeft()))
		{
			return _lastProhibitedClassIds.getRight();
		}
		pci = pci.trim();
		StringBuilder sb = new StringBuilder();
		int[] ids = ArrayUtils.EMPTY_INT_ARRAY;
		block4:
		for(int pciIdx = 0;pciIdx < pci.length();++pciIdx)
		{
			char c = pci.charAt(pciIdx);
			switch(c)
			{
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				{
					sb.append(c);
					continue block4;
				}
				case ',':
				case ';':
				{
					ids = ArrayUtils.add(ids, Integer.parseInt(sb.toString()));
					sb.setLength(0);
					continue block4;
				}
				default:
				{
					_log.error("Can't parse prohibited class ids \"" + pci + "\"");
					return ids;
				}
			}
		}
		if(sb.length() > 0)
		{
			ids = ArrayUtils.add(ids, Integer.parseInt(sb.toString()));
		}
		Arrays.sort(ids);
		_lastProhibitedClassIds = Pair.of(key, ids);
		return ids;
	}
	
	private List<Pair<ItemTemplate, Long>> config_RewardTeamItemIdAndAmount()
	{
		String teamRevListText = ServerVariables.getString("PvP_" + getRule().name() + "_rev_team", "");
		return Functions.parseItemIdAmountList(teamRevListText);
	}
	
	private List<Pair<ItemTemplate, Long>> config_RewardTopItemIdAndAmount()
	{
		String teamRevListText = ServerVariables.getString("PvP_" + getRule().name() + "_rev_top", "");
		return Functions.parseItemIdAmountList(teamRevListText);
	}
	
	private boolean config_dispellEffects()
	{
		return ServerVariables.getBool("PvP_" + getRule().name() + "_dispell", true);
	}
	
	private boolean config_dispellEffectsAfter()
	{
		return ServerVariables.getBool("PvP_" + getRule().name() + "_dispell_after", true);
	}
	
	private int config_EventTime()
	{
		switch(getRule())
		{
			case DM:
			{
				return ServerVariables.getInt("PvP_" + getRule().name() + "_time", 5);
			}
			case TVT:
			case CTF:
			{
				return ServerVariables.getInt("PvP_" + getRule().name() + "_time", 10);
			}
		}
		return 0;
	}
	
	private int getNewReflectionId()
	{
		return _event_instances_ids[Rnd.get(_event_instances_ids.length)];
	}
	
	private synchronized void scheduleStateChange(PvPEventState to_state, long delay)
	{
		_stateTask = ThreadPoolManager.getInstance().schedule(new PvPStateTask(to_state), delay);
	}
	
	private synchronized void cancelStateChange()
	{
		if(_stateTask != null)
		{
			_stateTask.cancel(false);
			_stateTask = null;
		}
	}
	
	private void goStandby()
	{
		setState(PvPEventState.STANDBY);
		long mills = getMillsToNextActivation(_event_start_time);
		if(mills > 0)
		{
			PvPEventRule nextRule = getNextRule(getRule());
			if(nextRule != null)
			{
				setRule(nextRule);
				scheduleStateChange(PvPEventState.REGISTRATION, mills);
				_log.info("PvPEvent: Next scheduled at " + TimeUtils.toSimpleFormat(System.currentTimeMillis() + mills));
			}
			else
			{
				_log.info("PvPEvent: No active next event");
			}
		}
		else
		{
			_log.warn("PvPEvent: Wrong event time: " + _event_start_time);
		}
	}
	
	public void goRegistration()
	{
		setState(PvPEventState.REGISTRATION);
		getInstance().scheduleProcessTask(new RegisrationTask(RegisrationState.ANNOUNCE, _event_announce_time), 1000);
	}
	
	private void goPrepareTo()
	{
		setState(PvPEventState.PREPARE_TO);
		getRule().getParticipantController().initReflection();
		getRule().getParticipantController().prepareParticipantsTo();
		scheduleStateChange(PvPEventState.PORTING_TO, 2000);
	}
	
	private void goPortingTo()
	{
		setState(PvPEventState.PORTING_TO);
		getRule().getParticipantController().portParticipantsTo();
		getInstance().scheduleProcessTask(new CompetitionRunTask(30), 1000);
	}
	
	private void goCompetition()
	{
		setState(PvPEventState.COMPETITION);
		getRule().getParticipantController().initParticipant();
		scheduleStateChange(PvPEventState.WINNER, config_EventTime() * 60 * 1000);
	}
	
	private void goWinner()
	{
		setState(PvPEventState.WINNER);
		getRule().getParticipantController().MakeWinner();
		scheduleStateChange(PvPEventState.PREPARE_FROM, 1000);
	}
	
	private void goPrepareFrom()
	{
		setState(PvPEventState.PREPARE_FROM);
		getRule().getParticipantController().prepareParticipantsFrom();
		scheduleStateChange(PvPEventState.PORTING_FROM, 10000);
	}
	
	private void goPortingFrom()
	{
		setState(PvPEventState.PORTING_FROM);
		getRule().getParticipantController().portParticipantsBack();
		getRule().getParticipantController().doneParicipant();
		getRule().getParticipantController().doneReflection();
		_participants.clear();
		_participants = null;
		scheduleStateChange(PvPEventState.STANDBY, 5000);
	}
	
	private synchronized void scheduleProcessTask(Runnable r, long delay)
	{
		_processTask = ThreadPoolManager.getInstance().schedule(r, delay);
	}
	
	private synchronized void cancelProcessTask()
	{
		if(_processTask != null)
		{
			_processTask.cancel(false);
			_processTask = null;
		}
	}
	
	private Collection<Player> getPlayers()
	{
		ArrayList<Player> result = new ArrayList<>(_participants.size());
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player == null)
				continue;
			for(Integer oid : _participants)
			{
				if(oid.intValue() != player.getObjectId())
					continue;
				result.add(player);
			}
		}
		return result;
	}
	
	private void broadcastParticipationRequest()
	{
		ArrayList<Player> players = new ArrayList<>();
		for(Player player : GameObjectsStorage.getAllPlayers())
		{
			if(!isDesirePlayer(player))
				continue;
			players.add(player);
		}
		for(Player player : players)
		{
			player.scriptRequest(new CustomMessage("events.PvPEvent.AskToS1Participation", player, new Object[] {getRule().name()}).toString(), "events.TvT2.PvPEvent:addDesire", new Object[0]);
		}
	}
	
	private void broadcastCapchaRequest()
	{
		if(_regState != RegisrationState.CAPCHA || _desireContainer == null)
		{
			return;
		}
		ArrayList<Player> players = new ArrayList<>();
		for(Integer oid : _desireContainer)
		{
			Player player = GameObjectsStorage.getPlayer(oid);
			if(!isDesirePlayer(player))
				continue;
			players.add(player);
		}
		_desireContainer.clear();
		_desireContainer = null;
		getInstance()._desireContainer = new ConcurrentSkipListSet<>();
		for(Player player : players)
		{
			Scripts.getInstance().callScripts(player, "Util", "RequestCapcha", new Object[] {"events.TvT2.PvPEvent:addDesire", player.getStoredId(), new Integer(30)});
		}
	}
	
	public boolean isHWIDRegistred(String hwid, Player player)
	{
		if(hwid == null || hwid.isEmpty() || _desireContainer == null)
		{
			return false;
		}
		for(Integer oid : _desireContainer)
		{
			GameClient gameClient;
			Player p = GameObjectsStorage.getPlayer(oid);
			if(p == null || p == player || (gameClient = p.getNetConnection()) == null || !gameClient.isConnected() || gameClient.getHwid() == null || !hwid.equalsIgnoreCase(gameClient.getHwid()))
				continue;
			return true;
		}
		return false;
	}
	
	public void addDesire()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(getInstance()._regState != RegisrationState.REQUEST && getInstance()._regState != RegisrationState.CAPCHA)
		{
			player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireInappropriateState", player));
			return;
		}
		if(!isDesirePlayer(player) || getInstance()._desireContainer == null)
		{
			player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireInsufficientConditions", player));
			return;
		}
		if(getInstance()._desireContainer.contains(player.getObjectId()))
		{
			player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireAlreadyAccepted", player));
			return;
		}
		player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireAccepted", player));
		getInstance()._desireContainer.add(player.getObjectId());
	}
	
	public void addDesireDuringAnnounce()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(getInstance()._regState != RegisrationState.ANNOUNCE)
		{
			player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireInappropriateState", player));
			return;
		}
		if(!isDesirePlayer(player) || getInstance()._desireContainer == null)
		{
			player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireInsufficientConditions", player));
			return;
		}
		if(getInstance()._desireContainer.contains(player.getObjectId()))
		{
			player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireAlreadyAccepted", player));
			return;
		}
		player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireAccepted", player));
		getInstance()._desireContainer.add(player.getObjectId());
	}
	
	private void morphDesires()
	{
		if(_regState != RegisrationState.MORPH || _desireContainer == null)
		{
			return;
		}
		LinkedList<Player> players = new LinkedList<>();
		for(Integer oid : _desireContainer)
		{
			Player player = GameObjectsStorage.getPlayer(oid);
			if(!isDesirePlayer(player))
				continue;
			players.add(player);
		}
		_desireContainer.clear();
		_desireContainer = null;
		ArrayList<Player> participants = new ArrayList();
		int max_part = config_MaxParticipants();
		while(participants.size() < max_part && !players.isEmpty())
		{
			participants.add(players.remove(Rnd.get(players.size())));
		}
		if(participants.size() < config_ReqParticipants())
		{
			Announcements.getInstance().announceByCustomMessage("events.PvPEvent.EventS1LackParticipants", new String[] {getRule().name()});
			goStandby();
			return;
		}
		_participants = new ConcurrentSkipListSet<>();
		for(Player player : players)
		{
			player.sendMessage(new CustomMessage("events.PvPEvent.ParticipantAskLater", player));
		}
		for(Player participant : participants)
		{
			participant.sendMessage(new CustomMessage("events.PvPEvent.ParticipantAccepted", participant));
			_participants.add(participant.getObjectId());
		}
		players.clear();
		scheduleStateChange(PvPEventState.PREPARE_TO, 10000);
	}
	
	public void Activate()
	{
		getInstance().scheduleStateChange(PvPEventState.STANDBY, 1000);
		_log.info("PvPEvent: [state: active]");
	}
	
	public void Deativate()
	{
		ServerVariables.set("PvP_is_active", false);
		getInstance().LoadVars();
		getInstance().cancelStateChange();
		_log.info("PvPEvent: [state: inactive]");
	}
	
	@Override
	public void onLoad()
	{
		_instance = this;
		LoadVars();
		if(_event_active)
		{
			Activate();
		}
		else
		{
			_log.info("PvPEvent: [state: inactive]");
		}
		_dieListner = new DieListner();
		_zoneListner = new ZoneEnterLeaveListner();
		_exitListner = new ExitListner();
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	private long getMillsToNextActivation(String schedule)
	{
		Matcher m = Pattern.compile("(\\d{2})\\:(\\d{2});*").matcher(schedule);
		long now = System.currentTimeMillis();
		long ret_mills = Long.MAX_VALUE;
		while(m.find())
		{
			String hour_str = m.group(1);
			String minute_str = m.group(2);
			int hour = Integer.parseInt(hour_str);
			Calendar next_c = Calendar.getInstance();
			next_c.set(11, hour);
			int minute = Integer.parseInt(minute_str);
			next_c.set(12, minute);
			next_c.set(13, 0);
			next_c.set(14, 0);
			if(next_c.getTimeInMillis() < now)
			{
				next_c.add(5, 1);
			}
			long mills_left;
			if((mills_left = next_c.getTimeInMillis() - now) <= 0 || mills_left >= ret_mills)
				continue;
			ret_mills = mills_left;
		}
		return ret_mills < Long.MAX_VALUE ? ret_mills : -1;
	}
	
	private void broadcast(L2GameServerPacket... gsp)
	{
		Collection<Player> players = getPlayers();
		for(Player player : players)
		{
			player.sendPacket((IStaticPacket[]) gsp);
		}
	}
	
	public boolean isEventPartisipant()
	{
		Player p = getSelf();
		if(getInstance()._participants == null || p == null)
		{
			return false;
		}
		int poid = p.getObjectId();
		for(Integer oid : getInstance()._participants)
		{
			if(oid != poid)
				continue;
			return true;
		}
		return false;
	}
	
	private enum RegisrationState
	{
		ANNOUNCE,
		REQUEST,
		MORPH,
		CAPCHA;
	}
	
	public enum PvPEventRule
	{
		TVT(new TvTParticipantController()),
		CTF(new CTFParticipantController()),
		DM(new DMParticipantController());
		
		public static PvPEventRule[] VALUES;
		
		static
		{
			VALUES = PvPEventRule.values();
		}
		
		private final IParticipantController _part_conteiner;
		
		PvPEventRule(IParticipantController conteiner)
		{
			_part_conteiner = conteiner;
		}
		
		public IParticipantController getParticipantController()
		{
			return _part_conteiner;
		}
	}
	
	protected enum PvPEventState
	{
		STANDBY,
		REGISTRATION,
		PORTING_TO,
		PREPARE_TO,
		COMPETITION,
		WINNER,
		PREPARE_FROM,
		PORTING_FROM;
	}
	
	private interface IParticipantController
	{
		void prepareParticipantsTo();
		
		void prepareParticipantsFrom();
		
		void initParticipant();
		
		void doneParicipant();
		
		void portParticipantsTo();
		
		void portParticipantsBack();
		
		void initReflection();
		
		void doneReflection();
		
		Reflection getReflection();
		
		void OnPlayerDied(Player target, Player killer);
		
		void OnEnter(Player player, Zone zone);
		
		void OnLeave(Player player, Zone zone);
		
		void OnExit(Player player);
		
		void OnTeleport(Player player, int x, int y, int z, Reflection r);
		
		void MakeWinner();
	}
	
	private static class DMParticipantController implements IParticipantController
	{
		private static final String TITLE_VAR = "pvp_dm_title";
		private final String RET_LOC_VAR = "backCoords";
		private Map<Integer, AtomicInteger> _kills;
		private ScheduledFuture<?> _rankBroadcastTask;
		private Reflection _reflection;
		private int _instance_id;
		private String ZONE_DEFAULT = "[pvp_%d_dm_default]";
		private String ZONE_SPAWN = "[pvp_%d_dm_spawn]";
		private Zone _default_zone;
		private Zone _spawn_zone;
		
		@Override
		public void prepareParticipantsTo()
		{
			_kills = new ConcurrentHashMap<>();
			boolean dispell = getInstance().config_dispellEffects();
			for(Player player : getInstance().getPlayers())
			{
				if(isDesirePlayer(player))
					continue;
				getInstance()._participants.remove(player.getObjectId());
				OnExit(player);
			}
			for(Player player : getInstance().getPlayers())
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				if(player.isAttackingNow())
				{
					player.abortAttack(true, false);
				}
				if(player.isCastingNow())
				{
					player.abortCast(true, false);
				}
				player.sendActionFailed();
				player.stopMove();
				player.sitDown(null);
				player.block();
				if(dispell)
				{
					player.getEffectList().stopAllEffects();
				}
				player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
				player.setCurrentCp((double) player.getMaxCp());
				player.setVar("pvp_dm_title", player.getTitle() != null ? player.getTitle() : "", -1);
				_kills.put(player.getObjectId(), new AtomicInteger(0));
				updateTitle(player, 0);
			}
		}
		
		private void updateTitle(Player player, int kills)
		{
			player.setTransformationTitle(String.format("Kills: %d", kills));
			player.setTitle(player.getTransformationTitle());
			player.broadcastPacket(new NickNameChanged(player));
		}
		
		@Override
		public void prepareParticipantsFrom()
		{
			boolean dispell_after = getInstance().config_dispellEffectsAfter();
			for(Player player : getInstance().getPlayers())
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				if(player.isAttackingNow())
				{
					player.abortAttack(true, false);
				}
				if(player.isCastingNow())
				{
					player.abortCast(true, false);
				}
				player.sendActionFailed();
				player.stopMove();
				player.sitDown(null);
				player.block();
				if(dispell_after)
				{
					player.getEffectList().stopAllEffects();
				}
				player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp(), true);
				player.setCurrentCp((double) player.getMaxCp());
				if(getInstance().config_hideIdentiti() && player.getTransformation() == 0)
				{
					player.setTransformationName(null);
					player.setTransformationTitle(null);
				}
				String title;
				if((title = player.getVar("pvp_dm_title")) != null)
				{
					player.setTitle(title);
					player.unsetVar("pvp_dm_title");
				}
				player.sendUserInfo(true);
			}
			_kills.clear();
			_kills = null;
		}
		
		@Override
		public void initParticipant()
		{
			for(Player player : getInstance().getPlayers())
			{
				player.addListener(getInstance()._dieListner);
				player.addListener(getInstance()._exitListner);
				player.setResurectProhibited(true);
				player.unblock();
				player.standUp();
			}
			_rankBroadcastTask = ThreadPoolManager.getInstance().schedule(new RankBroadcastTask(this), 20000);
		}
		
		@Override
		public void doneParicipant()
		{
			if(_rankBroadcastTask != null)
			{
				_rankBroadcastTask.cancel(true);
				_rankBroadcastTask = null;
			}
			for(Player player : getInstance().getPlayers())
			{
				player.removeListener(getInstance()._dieListner);
				player.removeListener(getInstance()._exitListner);
				player.setResurectProhibited(false);
				player.unblock();
				if(player.isDead())
				{
					player.doRevive(100.0);
				}
				player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp(), true);
				player.setCurrentCp((double) player.getMaxCp());
				player.standUp();
			}
		}
		
		@Override
		public void portParticipantsTo()
		{
			int playerCnt = 0;
			for(Player player : getInstance().getPlayers())
			{
				player.setVar(RET_LOC_VAR, player.getLoc().toXYZString(), -1);
				if(player.getParty() != null)
				{
					player.getParty().removePartyMember(player, false);
				}
				if(getInstance().config_hideIdentiti())
				{
					Object[] arrobject = {++playerCnt};
					player.setTransformationName(String.format("Player %d", arrobject));
				}
				player.teleToLocation(getRandomSpawnLoc(), getReflection());
			}
		}
		
		@Override
		public void portParticipantsBack()
		{
			for(Player player : getInstance().getPlayers())
			{
				if(getInstance().config_hideIdentiti() && player.getTransformation() == 0)
				{
					player.setTransformationName(null);
				}
				String sloc;
				if((sloc = player.getVar(RET_LOC_VAR)) != null)
				{
					player.unsetVar(RET_LOC_VAR);
					player.teleToLocation(Location.parseLoc(sloc), ReflectionManager.DEFAULT);
					continue;
				}
				player.teleToClosestTown();
			}
		}
		
		@Override
		public void initReflection()
		{
			_instance_id = getInstance().getNewReflectionId();
			InstantZone instantZone = InstantZoneHolder.getInstance().getInstantZone(_instance_id);
			ZONE_DEFAULT = String.format("[pvp_%d_dm_default]", _instance_id);
			ZONE_SPAWN = String.format("[pvp_%d_dm_spawn]", _instance_id);
			_reflection = new Reflection();
			_reflection.init(instantZone);
			_default_zone = _reflection.getZone(ZONE_DEFAULT);
			_default_zone.addListener(getInstance()._zoneListner);
			_spawn_zone = _reflection.getZone(ZONE_SPAWN);
		}
		
		private Location getRandomSpawnLoc()
		{
			return _spawn_zone.getTerritory().getRandomLoc(_reflection.getGeoIndex());
		}
		
		@Override
		public void doneReflection()
		{
			_default_zone.removeListener(getInstance()._zoneListner);
			_default_zone = null;
			_spawn_zone = null;
			_reflection.collapse();
			_reflection = null;
		}
		
		@Override
		public Reflection getReflection()
		{
			return _reflection;
		}
		
		@Override
		public void OnPlayerDied(Player target, Player killer)
		{
			if(target != null && killer != null && _kills.containsKey(target.getObjectId()) && _kills.containsKey(killer.getObjectId()))
			{
				AtomicInteger tcnt = _kills.get(target.getObjectId());
				AtomicInteger kcnt = _kills.get(killer.getObjectId());
				int kcntp = kcnt.addAndGet(tcnt.getAndSet(0) + 1);
				updateTitle(target, 0);
				updateTitle(killer, kcntp);
			}
			PvPEvent pvPEvent = getInstance();
			pvPEvent.getClass();
			ThreadPoolManager.getInstance().schedule(pvPEvent.new TeleportAndReviveTask(target, getRandomSpawnLoc(), getReflection()), (long) (getInstance().config_ReviveDelay() * 1000));
		}
		
		@Override
		public void OnEnter(Player player, Zone zone)
		{
			if(player != null && !_kills.containsKey(player.getObjectId()))
			{
				if(getInstance().config_hideIdentiti() && player.getTransformation() == 0)
				{
					player.setTransformationName(null);
				}
				player.teleToClosestTown();
			}
		}
		
		@Override
		public void OnLeave(Player player, Zone zone)
		{
			if(player != null && !_default_zone.checkIfInZone(player.getX(), player.getY(), player.getZ(), getReflection()) && _kills.containsKey(player.getObjectId()))
			{
				double radian = 6.283185307179586 - PositionUtils.convertHeadingToRadian(player.getHeading());
				int x = (int) Math.floor((double) player.getX() - 50.0 * Math.cos(radian));
				int y = (int) Math.floor((double) player.getY() + 50.0 * Math.sin(radian));
				PvPEvent pvPEvent = getInstance();
				pvPEvent.getClass();
				ThreadPoolManager.getInstance().schedule(pvPEvent.new TeleportTask(player, new Location(x, y, player.getZ()).correctGeoZ(), getReflection()), 3000);
			}
		}
		
		@Override
		public void OnExit(Player player)
		{
			String title = player.getVar("pvp_dm_title");
			if(title != null)
			{
				player.setTitle(player.getVar("pvp_dm_title"));
				player.unsetVar("pvp_dm_title");
			}
			_kills.remove(player.getObjectId());
		}
		
		@Override
		public void OnTeleport(Player player, int x, int y, int z, Reflection r)
		{
			Location loc;
			if(player != null && !_default_zone.checkIfInZone(x, y, z, getReflection()) && (loc = getRandomSpawnLoc()) != null)
			{
				PvPEvent pvPEvent = getInstance();
				pvPEvent.getClass();
				ThreadPoolManager.getInstance().schedule(pvPEvent.new TeleportTask(player, loc, getReflection()), 3000);
			}
		}
		
		private void rewardPerKill()
		{
			int itemId = getInstance().config_ItemPerKill();
			if(itemId <= 0)
			{
				return;
			}
			for(Map.Entry<Integer, AtomicInteger> e : _kills.entrySet())
			{
				int kills = e.getValue().get();
				int killerOid = e.getKey();
				Player player = GameObjectsStorage.getPlayer(killerOid);
				if(kills <= 0 || player == null)
					continue;
				Functions.addItem(player, itemId, (long) kills);
			}
		}
		
		@Override
		public void MakeWinner()
		{
			int max_oid = 0;
			int max = Integer.MIN_VALUE;
			for(Map.Entry<Integer, AtomicInteger> e : _kills.entrySet())
			{
				int val = e.getValue().get();
				if(val <= max)
					continue;
				max_oid = e.getKey();
				max = val;
			}
			if(max_oid != 0 && max > 0)
			{
				Player winner = GameObjectsStorage.getPlayer(max_oid);
				for(Pair rewardItemInfo : getInstance().config_RewardTopItemIdAndAmount())
				{
					Functions.addItem(winner, ((ItemTemplate) rewardItemInfo.getLeft()).getItemId(), (Long) rewardItemInfo.getRight());
				}
				if(getInstance().config_RewardHeroHours() > 0)
				{
					GlobalServices.makeCustomHero(winner, (long) (getInstance().config_RewardHeroHours() * 60) * 60);
				}
				getInstance().broadcast(new ExEventMatchMessage("'" + winner.getName() + "' winns!"), new SystemMessage(1497).addName(winner));
				Announcements.getInstance().announceByCustomMessage("events.PvPEvent.PlayerS1WonTheDMGame", new String[] {winner.getName()});
			}
			else
			{
				getInstance().broadcast(new ExEventMatchMessage("Tie"), Msg.THE_GAME_ENDED_IN_A_TIE);
				Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TheDMGameEndedInATie", ArrayUtils.EMPTY_STRING_ARRAY);
			}
			rewardPerKill();
		}
		
		private class RankBroadcastTask implements Runnable
		{
			private final DMParticipantController _controller;
			
			public RankBroadcastTask(DMParticipantController controller)
			{
				_controller = controller;
			}
			
			@Override
			public void run()
			{
				if(getInstance().getState() != PvPEventState.COMPETITION)
				{
					return;
				}
				_controller._rankBroadcastTask = ThreadPoolManager.getInstance().schedule(this, 20000);
			}
		}
	}
	
	private static class CTFParticipantController implements IParticipantController
	{
		private static final String TITLE_VAR = "pvp_ctf_title";
		private static final int BLUE_FLAG_NPC = 32027;
		private static final int RED_FLAG_NPC = 32027;
		private static final int BLUE_FLAG_ITEM = 6718;
		private static final int RED_FLAG_ITEM = 6718;
		private final String RET_LOC_VAR = "backCoords";
		private Map<Integer, AtomicInteger> _red_team;
		private Map<Integer, AtomicInteger> _blue_team;
		private AtomicInteger _red_points;
		private AtomicInteger _blue_points;
		private String ZONE_DEFAULT = "[pvp_%d_ctf_default]";
		private String ZONE_BLUE = "[pvp_%d_ctf_spawn_blue]";
		private String ZONE_RED = "[pvp_%d_ctf_spawn_red]";
		private Reflection _reflection;
		private Zone _default_zone;
		private Zone _blue_zone;
		private Zone _red_zone;
		private int _instance_id;
		private WeakReference<CTFFlagInstance> _redFlag;
		private WeakReference<CTFFlagInstance> _blueFlag;
		private ScheduledFuture<?> _rankBroadcastTask;
		
		@Override
		public void prepareParticipantsTo()
		{
			_red_team = new ConcurrentHashMap<>();
			_blue_team = new ConcurrentHashMap<>();
			_red_points = new AtomicInteger(0);
			_blue_points = new AtomicInteger(0);
			for(Player player : getInstance().getPlayers())
			{
				if(isDesirePlayer(player))
					continue;
				getInstance()._participants.remove(player.getObjectId());
				OnExit(player);
			}
			boolean dispell = getInstance().config_dispellEffects();
			TeamType team_type = TeamType.BLUE;
			for(Player player : getInstance().getPlayers())
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				if(player.isAttackingNow())
				{
					player.abortAttack(true, false);
				}
				if(player.isCastingNow())
				{
					player.abortCast(true, false);
				}
				player.sendActionFailed();
				player.stopMove();
				player.sitDown(null);
				player.block();
				if(dispell)
				{
					player.getEffectList().stopAllEffects();
				}
				player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
				player.setCurrentCp((double) player.getMaxCp());
				player.sendChanges();
				player.setVar("pvp_ctf_title", player.getTitle() != null ? player.getTitle() : "", -1);
				if(team_type == TeamType.BLUE)
				{
					player.setTeam(TeamType.BLUE);
					_blue_team.put(player.getObjectId(), new AtomicInteger(0));
					team_type = TeamType.RED;
					continue;
				}
				player.setTeam(TeamType.RED);
				_red_team.put(player.getObjectId(), new AtomicInteger(0));
				team_type = TeamType.BLUE;
			}
		}
		
		@Override
		public void prepareParticipantsFrom()
		{
			_redFlag.get().removeFlag(null);
			_blueFlag.get().removeFlag(null);
			boolean dispell_after = getInstance().config_dispellEffectsAfter();
			for(Player player : getInstance().getPlayers())
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				if(player.isAttackingNow())
				{
					player.abortAttack(true, false);
				}
				if(player.isCastingNow())
				{
					player.abortCast(true, false);
				}
				player.sendActionFailed();
				player.stopMove();
				player.sitDown(null);
				player.block();
				if(dispell_after)
				{
					player.getEffectList().stopAllEffects();
				}
				player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
				player.setCurrentCp((double) player.getMaxCp());
				player.sendChanges();
				if(getInstance().config_hideIdentiti() && player.getTransformation() == 0)
				{
					player.setTransformationName(null);
					player.setTransformationTitle(null);
				}
				player.setTeam(TeamType.NONE);
				String title = player.getVar("pvp_ctf_title");
				if(title != null)
				{
					player.setTitle(title);
					player.unsetVar("pvp_ctf_title");
				}
				player.sendUserInfo(true);
			}
			_red_team.clear();
			_blue_team.clear();
			_red_team = null;
			_blue_team = null;
			_red_points = null;
			_blue_points = null;
		}
		
		@Override
		public void initParticipant()
		{
			for(Player player : getInstance().getPlayers())
			{
				player.addListener(getInstance()._dieListner);
				player.addListener(getInstance()._exitListner);
				player.setResurectProhibited(true);
				player.unblock();
				player.standUp();
			}
		}
		
		@Override
		public void doneParicipant()
		{
			if(_rankBroadcastTask != null)
			{
				_rankBroadcastTask.cancel(true);
				_rankBroadcastTask = null;
			}
			for(Player player : getInstance().getPlayers())
			{
				player.removeListener(getInstance()._dieListner);
				player.removeListener(getInstance()._exitListner);
				player.setResurectProhibited(false);
				player.unblock();
				if(player.isDead())
				{
					player.doRevive(100.0);
					player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
					player.setCurrentCp((double) player.getMaxCp());
				}
				player.standUp();
			}
		}
		
		@Override
		public void portParticipantsTo()
		{
			int redCnt = 0;
			int blueCnt = 0;
			for(Player player : getInstance().getPlayers())
			{
				TeamType playerTeam = player.getTeam();
				if(playerTeam != TeamType.BLUE && playerTeam != TeamType.RED)
				{
					getInstance()._participants.remove(player.getObjectId());
					OnExit(player);
					continue;
				}
				player.setVar(RET_LOC_VAR, player.getLoc().toXYZString(), -1);
				if(player.getParty() != null)
				{
					player.getParty().removePartyMember(player, false);
				}
				if(getInstance().config_hideIdentiti())
				{
					switch(playerTeam)
					{
						case RED:
						{
							Object[] arrobject = {++redCnt};
							player.setTransformationName(String.format("Red %d", arrobject));
							break;
						}
						case BLUE:
						{
							Object[] arrobject = {++blueCnt};
							player.setTransformationName(String.format("Blue %d", arrobject));
						}
					}
				}
				player.teleToLocation(getRandomTeamLoc(playerTeam), getReflection());
			}
		}
		
		@Override
		public void portParticipantsBack()
		{
			for(Player player : getInstance().getPlayers())
			{
				String sloc = player.getVar(RET_LOC_VAR);
				if(sloc != null)
				{
					player.unsetVar(RET_LOC_VAR);
					player.teleToLocation(Location.parseLoc(sloc), ReflectionManager.DEFAULT);
					continue;
				}
				player.teleToClosestTown();
			}
		}
		
		@Override
		public void initReflection()
		{
			_instance_id = getInstance().getNewReflectionId();
			InstantZone instantZone = InstantZoneHolder.getInstance().getInstantZone(_instance_id);
			ZONE_DEFAULT = String.format("[pvp_%d_ctf_default]", _instance_id);
			ZONE_BLUE = String.format("[pvp_%d_ctf_spawn_blue]", _instance_id);
			ZONE_RED = String.format("[pvp_%d_ctf_spawn_red]", _instance_id);
			_reflection = new Reflection();
			_reflection.init(instantZone);
			_default_zone = _reflection.getZone(ZONE_DEFAULT);
			_blue_zone = _reflection.getZone(ZONE_BLUE);
			_red_zone = _reflection.getZone(ZONE_RED);
			_default_zone.addListener(getInstance()._zoneListner);
			_blue_zone.addListener(getInstance()._zoneListner);
			_red_zone.addListener(getInstance()._zoneListner);
			CTFFlagInstance red_flag = new CTFFlagInstance(TeamType.RED, this);
			red_flag.setSpawnedLoc(getRandomTeamLoc(TeamType.RED));
			red_flag.setReflection(getReflection());
			red_flag.setCurrentHpMp((double) red_flag.getMaxHp(), (double) red_flag.getMaxMp(), true);
			red_flag.spawnMe(red_flag.getSpawnedLoc());
			_redFlag = new WeakReference<>(red_flag);
			CTFFlagInstance blue_flag = new CTFFlagInstance(TeamType.BLUE, this);
			blue_flag.setSpawnedLoc(getRandomTeamLoc(TeamType.BLUE));
			blue_flag.setReflection(getReflection());
			blue_flag.setCurrentHpMp((double) blue_flag.getMaxHp(), (double) blue_flag.getMaxMp(), true);
			blue_flag.spawnMe(blue_flag.getSpawnedLoc());
			_blueFlag = new WeakReference<>(blue_flag);
		}
		
		@Override
		public void doneReflection()
		{
			if(_blueFlag.get() != null)
			{
				_blueFlag.get().destroy();
				_blueFlag.clear();
			}
			if(_redFlag.get() != null)
			{
				_redFlag.get().destroy();
				_redFlag.clear();
			}
			_redFlag = null;
			_blueFlag = null;
			_default_zone.removeListener(getInstance()._zoneListner);
			_blue_zone.removeListener(getInstance()._zoneListner);
			_red_zone.removeListener(getInstance()._zoneListner);
			_default_zone = null;
			_blue_zone = null;
			_red_zone = null;
			_reflection.collapse();
			_reflection = null;
		}
		
		@Override
		public Reflection getReflection()
		{
			return _reflection;
		}
		
		@Override
		public void OnPlayerDied(Player target, Player killer)
		{
			PvPEvent pvPEvent = getInstance();
			pvPEvent.getClass();
			ThreadPoolManager.getInstance().schedule(pvPEvent.new TeleportAndReviveTask(target, getRandomTeamLoc(target.getTeam()), getReflection()), (long) (getInstance().config_ReviveDelay() * 1000));
		}
		
		@Override
		public void OnEnter(Player player, Zone zone)
		{
			if(player != null && !player.isDead())
			{
				if(zone == _default_zone && player.getTeam() != TeamType.BLUE && player.getTeam() != TeamType.RED)
				{
					player.teleToClosestTown();
					_log.warn("PvPEvent.CTF: '" + player.getName() + "' in zone.");
				}
				else if(zone == _blue_zone && player.getTeam() == TeamType.BLUE && player.getObjectId() == _redFlag.get().getOwnerOid())
				{
					_redFlag.get().removeFlag(null);
					_blue_points.incrementAndGet();
				}
				else if(zone == _red_zone && player.getTeam() == TeamType.RED && player.getObjectId() == _blueFlag.get().getOwnerOid())
				{
					_blueFlag.get().removeFlag(null);
					_red_points.incrementAndGet();
				}
			}
		}
		
		@Override
		public void OnLeave(Player player, Zone zone)
		{
			if(player != null && !_default_zone.checkIfInZone(player.getX(), player.getY(), player.getZ(), getReflection()) && zone == _default_zone)
			{
				if(player.getTeam() != TeamType.BLUE && player.getTeam() != TeamType.RED)
				{
					if(getInstance().config_hideIdentiti() && player.getTransformation() == 0)
					{
						player.setTransformationName(null);
					}
					player.teleToClosestTown();
					return;
				}
				double radian = 6.283185307179586 - PositionUtils.convertHeadingToRadian(player.getHeading());
				int x = (int) Math.floor((double) player.getX() - 50.0 * Math.cos(radian));
				int y = (int) Math.floor((double) player.getY() + 50.0 * Math.sin(radian));
				PvPEvent pvPEvent = getInstance();
				pvPEvent.getClass();
				ThreadPoolManager.getInstance().schedule(pvPEvent.new TeleportTask(player, new Location(x, y, player.getZ()).correctGeoZ(), getReflection()), 3000);
				if(player.getObjectId() == _blueFlag.get().getOwnerOid())
				{
					_blueFlag.get().removeFlag(player);
				}
				else if(player.getObjectId() == _redFlag.get().getOwnerOid())
				{
					_redFlag.get().removeFlag(player);
				}
			}
		}
		
		@Override
		public void OnExit(Player player)
		{
			if(_blue_team.containsKey(player.getObjectId()))
			{
				_blue_team.remove(player.getObjectId());
			}
			else if(_red_team.containsKey(player.getObjectId()))
			{
				_red_team.remove(player.getObjectId());
			}
			if(player.getTransformation() == 0)
			{
				player.setTransformationName(null);
				player.setTransformationTitle(null);
			}
			String title = player.getVar("pvp_ctf_title");
			if(title != null)
			{
				player.setTitle(player.getVar("pvp_ctf_title"));
				player.unsetVar("pvp_ctf_title");
			}
		}
		
		@Override
		public void OnTeleport(Player player, int x, int y, int z, Reflection r)
		{
			Location loc;
			if(player != null && !_default_zone.checkIfInZone(x, y, z, r) && (loc = getRandomTeamLoc(player.getTeam())) != null)
			{
				PvPEvent pvPEvent = getInstance();
				pvPEvent.getClass();
				ThreadPoolManager.getInstance().schedule(pvPEvent.new TeleportTask(player, loc, getReflection()), 3000);
			}
		}
		
		private void reward(Map<Integer, AtomicInteger> team, List<Pair<ItemTemplate, Long>> rewardList)
		{
			for(Map.Entry<Integer, AtomicInteger> e : team.entrySet())
			{
				int oid = e.getKey();
				Player player = GameObjectsStorage.getPlayer(oid);
				if(player == null)
					continue;
				for(Pair<ItemTemplate, Long> rewardInfo : rewardList)
				{
					Functions.addItem(player, rewardInfo.getLeft().getItemId(), rewardInfo.getRight());
				}
			}
		}
		
		@Override
		public void MakeWinner()
		{
			int red_pnt;
			int blue_pnt = _blue_points.get();
			if(blue_pnt > (red_pnt = _red_points.get()))
			{
				reward(_blue_team, getInstance().config_RewardTeamItemIdAndAmount());
				Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TeamBlueWonTheCTFGameCountIsS1S2", new String[] {String.valueOf(blue_pnt), String.valueOf(red_pnt)});
			}
			else if(blue_pnt < red_pnt)
			{
				reward(_red_team, getInstance().config_RewardTeamItemIdAndAmount());
				Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TeamRedWonTheCTFGameCountIsS1S2", new String[] {String.valueOf(red_pnt), String.valueOf(blue_pnt)});
			}
			else if(blue_pnt == red_pnt)
			{
				Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TheCTFGameEndedInATie", ArrayUtils.EMPTY_STRING_ARRAY);
			}
			getInstance().broadcast(ExEventMatchMessage.FINISH);
		}
		
		private Location getRandomTeamLoc(TeamType tt)
		{
			if(tt == TeamType.BLUE)
			{
				return _blue_zone.getTerritory().getRandomLoc(_reflection.getGeoIndex());
			}
			if(tt == TeamType.RED)
			{
				return _red_zone.getTerritory().getRandomLoc(_reflection.getGeoIndex());
			}
			return null;
		}
		
		private class CTFFlagInstance extends MonsterInstance implements FlagItemAttachment
		{
			private final TeamType _team;
			private final CTFParticipantController _controller;
			private ItemInstance _flag;
			
			public CTFFlagInstance(TeamType team, CTFParticipantController controller)
			{
				super(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(team == TeamType.BLUE ? 32027 : team == TeamType.RED ? 32027 : -1));
				_team = team;
				_flag = ItemFunctions.createItem(team == TeamType.BLUE ? 6718 : team == TeamType.RED ? 6718 : -1);
				_flag.setAttachment(this);
				_controller = controller;
			}
			
			public void destroy()
			{
				Player owner = GameObjectsStorage.getPlayer(_flag.getOwnerId());
				if(owner != null)
				{
					owner.getInventory().destroyItem(_flag);
					owner.sendDisarmMessage(_flag);
				}
				_flag.setAttachment(null);
				_flag.deleteMe();
				_flag.delete();
				_flag = null;
				deleteMe();
			}
			
			@Override
			public boolean isAutoAttackable(Creature attacker)
			{
				return isAttackable(attacker);
			}
			
			@Override
			public boolean isAttackable(Creature attacker)
			{
				return attacker != null && attacker.getTeam() != null && attacker.getTeam() != TeamType.NONE && attacker.getTeam() != _team;
			}
			
			@Override
			protected void onDeath(Creature killer)
			{
				Player pkiller;
				if(isAttackable(killer) && (pkiller = killer.getPlayer()) != null && (_team == TeamType.RED && killer.isInZone(_controller._red_zone) || _team == TeamType.BLUE && killer.isInZone(_controller._blue_zone)))
				{
					pkiller.getInventory().addItem(_flag);
					pkiller.getInventory().equipItem(_flag);
					pkiller.broadcastPacket(new SocialAction(pkiller.getObjectId(), 16));
					decayMe();
					if(_team == TeamType.RED)
					{
						ExShowScreenMessage essm = new ExShowScreenMessage("'" + pkiller.getName() + "' captured the Red flag!", 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false);
						for(Integer poid : _controller._red_team.keySet())
						{
							Player player = GameObjectsStorage.getPlayer(poid);
							if(player == null)
								continue;
							player.sendPacket(essm);
						}
					}
					else if(_team == TeamType.BLUE)
					{
						ExShowScreenMessage essm = new ExShowScreenMessage("'" + pkiller.getName() + "' captured the Blue flag!", 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false);
						for(Integer poid : _controller._blue_team.keySet())
						{
							Player player = GameObjectsStorage.getPlayer(poid);
							if(player == null)
								continue;
							player.sendPacket(essm);
						}
					}
					return;
				}
				setCurrentHpMp((double) getMaxHp(), (double) getMaxMp(), true);
			}
			
			public int getOwnerOid()
			{
				return _flag.getOwnerId();
			}
			
			public void removeFlag(Player owner)
			{
				if(owner == null)
				{
					owner = GameObjectsStorage.getPlayer(_flag.getOwnerId());
				}
				if(owner != null)
				{
					owner.getInventory().removeItem(_flag);
					owner.sendDisarmMessage(_flag);
				}
				_flag.setOwnerId(0);
				setCurrentHpMp((double) getMaxHp(), (double) getMaxMp(), true);
				spawnMe(_controller.getRandomTeamLoc(_team));
			}
			
			@Override
			public boolean canPickUp(Player player)
			{
				return false;
			}
			
			@Override
			public void pickUp(Player player)
			{
			}
			
			@Override
			public void setItem(ItemInstance item)
			{
				if(item != null)
				{
					item.setCustomFlags(39);
				}
			}
			
			@Override
			public void onLogout(Player player)
			{
				player.getInventory().removeItem(_flag);
				_flag.setOwnerId(0);
				setCurrentHpMp((double) getMaxHp(), (double) getMaxMp(), true);
				spawnMe(_controller.getRandomTeamLoc(_team));
			}
			
			@Override
			public void onDeath(Player owner, Creature killer)
			{
				owner.getInventory().removeItem(_flag);
				owner.sendDisarmMessage(_flag);
				_flag.setOwnerId(0);
				setCurrentHpMp((double) getMaxHp(), (double) getMaxMp(), true);
				spawnMe(_controller.getRandomTeamLoc(_team));
			}
			
			@Override
			public void onEnterPeace(Player owner)
			{
			}
			
			@Override
			public boolean canAttack(Player player)
			{
				player.sendMessage(new CustomMessage("THAT_WEAPON_CANNOT_PERFORM_ANY_ATTACKS", player));
				return false;
			}
			
			@Override
			public boolean canCast(Player player, Skill skill)
			{
				player.sendMessage(new CustomMessage("THAT_WEAPON_CANNOT_USE_ANY_OTHER_SKILL_EXCEPT_THE_WEAPONS_SKILL", player));
				return false;
			}
			
			@Override
			public boolean isEffectImmune()
			{
				return true;
			}
			
			@Override
			public boolean isDebuffImmune()
			{
				return true;
			}
		}
	}
	
	private static class TvTParticipantController implements IParticipantController
	{
		private static final RankComparator _rankComparator = new RankComparator();
		private static final String TITLE_VAR = "pvp_tvt_title";
		private final String RET_LOC_VAR = "backCoords";
		private Map<Integer, ImmutablePair<AtomicInteger, AtomicInteger>> _red_team;
		private Map<Integer, ImmutablePair<AtomicInteger, AtomicInteger>> _blue_team;
		private int _instance_id;
		private String ZONE_DEFAULT = "[pvp_%d_tvt_default]";
		private String ZONE_BLUE = "[pvp_%d_tvt_spawn_blue]";
		private String ZONE_RED = "[pvp_%d_tvt_spawn_red]";
		private Reflection _reflection;
		private Zone _default_zone;
		private AtomicInteger _red_points;
		private AtomicInteger _blue_points;
		private ScheduledFuture<?> _rankBroadcastTask;
		
		public int getKills(TeamType team)
		{
			int result = 0;
			if(team == TeamType.RED)
			{
				for(ImmutablePair<AtomicInteger, AtomicInteger> entry : _red_team.values())
				{
					result += entry.getLeft().get();
				}
			}
			if(team == TeamType.BLUE)
			{
				for(ImmutablePair<AtomicInteger, AtomicInteger> entry : _blue_team.values())
				{
					result += entry.getLeft().get();
				}
			}
			return result;
		}
		
		@Override
		public void prepareParticipantsTo()
		{
			_red_team = new ConcurrentHashMap<>();
			_blue_team = new ConcurrentHashMap<>();
			_red_points = new AtomicInteger(0);
			_blue_points = new AtomicInteger(0);
			boolean dispell = getInstance().config_dispellEffects();
			for(Player player : getInstance().getPlayers())
			{
				if(isDesirePlayer(player))
					continue;
				getInstance()._participants.remove(player.getObjectId());
				OnExit(player);
			}
			TeamType team_type = TeamType.BLUE;
			for(Player player : getInstance().getPlayers())
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				if(player.isAttackingNow())
				{
					player.abortAttack(true, false);
				}
				if(player.isCastingNow())
				{
					player.abortCast(true, false);
				}
				player.sendActionFailed();
				player.stopMove();
				player.sitDown(null);
				player.block();
				if(dispell)
				{
					player.getEffectList().stopAllEffects();
				}
				player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
				player.setCurrentCp((double) player.getMaxCp());
				player.sendChanges();
				player.setVar("pvp_tvt_title", player.getTitle() != null ? player.getTitle() : "", -1);
				if(team_type == TeamType.BLUE)
				{
					player.setTeam(TeamType.BLUE);
					_blue_team.put(player.getObjectId(), new ImmutablePair(new AtomicInteger(0), new AtomicInteger(0)));
					team_type = TeamType.RED;
				}
				else
				{
					player.setTeam(TeamType.RED);
					_red_team.put(player.getObjectId(), new ImmutablePair(new AtomicInteger(0), new AtomicInteger(0)));
					team_type = TeamType.BLUE;
				}
				updateTitle(player, 0);
			}
		}
		
		@Override
		public void prepareParticipantsFrom()
		{
			boolean dispell_after = getInstance().config_dispellEffectsAfter();
			for(Player player : getInstance().getPlayers())
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				if(player.isAttackingNow())
				{
					player.abortAttack(true, false);
				}
				if(player.isCastingNow())
				{
					player.abortCast(true, false);
				}
				player.sendActionFailed();
				player.stopMove();
				player.sitDown(null);
				player.block();
				if(dispell_after)
				{
					player.getEffectList().stopAllEffects();
				}
				player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
				player.setCurrentCp((double) player.getMaxCp());
				player.sendChanges();
				player.setTeam(TeamType.NONE);
				String title = player.getVar("pvp_tvt_title");
				if(title != null)
				{
					player.setTitle(title);
					player.unsetVar("pvp_tvt_title");
				}
				if(getInstance().config_hideIdentiti() && player.getTransformation() == 0)
				{
					player.setTransformationName(null);
					player.setTransformationTitle(null);
				}
				player.sendUserInfo(true);
			}
			_red_team.clear();
			_blue_team.clear();
			_red_team = null;
			_blue_team = null;
			_red_points = null;
			_blue_points = null;
		}
		
		@Override
		public void initParticipant()
		{
			for(Player player : getInstance().getPlayers())
			{
				player.addListener(getInstance()._dieListner);
				player.addListener(getInstance()._exitListner);
				player.setResurectProhibited(true);
				player.unblock();
				player.standUp();
			}
		}
		
		@Override
		public void doneParicipant()
		{
			if(_rankBroadcastTask != null)
			{
				_rankBroadcastTask.cancel(true);
				_rankBroadcastTask = null;
			}
			for(Player player : getInstance().getPlayers())
			{
				player.removeListener(getInstance()._dieListner);
				player.removeListener(getInstance()._exitListner);
				player.setResurectProhibited(false);
				player.unblock();
				if(player.isDead())
				{
					player.doRevive(100.0);
					player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
					player.setCurrentCp((double) player.getMaxCp());
				}
				player.standUp();
			}
		}
		
		private void updateTitle(Player player, int kills)
		{
			player.setTransformationTitle(String.format("Kills: %d", kills));
			player.setTitle(player.getTransformationTitle());
			player.broadcastPacket(new NickNameChanged(player));
		}
		
		@Override
		public void OnPlayerDied(Player target, Player killer)
		{
			ImmutablePair<AtomicInteger, AtomicInteger> entry;
			if(killer != null && killer.getTeam() != target.getTeam())
			{
				if(killer.getTeam() == TeamType.RED && _red_team.containsKey(killer.getObjectId()))
				{
					entry = _red_team.get(killer.getObjectId());
					AtomicInteger cnt = entry.getLeft();
					updateTitle(killer, cnt.incrementAndGet());
					_red_points.incrementAndGet();
				}
				else if(killer.getTeam() == TeamType.BLUE && _blue_team.containsKey(killer.getObjectId()))
				{
					entry = _blue_team.get(killer.getObjectId());
					AtomicInteger cnt = entry.getLeft();
					updateTitle(killer, cnt.incrementAndGet());
					_blue_points.incrementAndGet();
				}
				else if(killer.getTeam() != TeamType.NONE)
				{
					_log.warn("PvPEvent.TVT: '" + killer.getName() + "' got color but not at list.");
				}
				killer.sendUserInfo(true);
			}
			if(target.getTeam() == TeamType.RED && _red_team.containsKey(target.getObjectId()))
			{
				entry = _red_team.get(target.getObjectId());
				entry.getRight().incrementAndGet();
			}
			else if(target.getTeam() == TeamType.BLUE && _blue_team.containsKey(target.getObjectId()))
			{
				entry = _blue_team.get(target.getObjectId());
				entry.getRight().incrementAndGet();
			}
			PvPEvent pvPEvent = getInstance();
			pvPEvent.getClass();
			ThreadPoolManager.getInstance().schedule(pvPEvent.new TeleportAndReviveTask(target, getRandomTeamLoc(target.getTeam()), getReflection()), (long) (getInstance().config_ReviveDelay() * 1000));
		}
		
		@Override
		public void portParticipantsTo()
		{
			int redCnt = 0;
			int blueCnt = 0;
			for(Player player : getInstance().getPlayers())
			{
				TeamType playerTeam = player.getTeam();
				if(playerTeam != TeamType.BLUE && playerTeam != TeamType.RED)
				{
					getInstance()._participants.remove(player.getObjectId());
					OnExit(player);
					continue;
				}
				player.setVar(RET_LOC_VAR, player.getLoc().toXYZString(), -1);
				if(player.getParty() != null)
				{
					player.getParty().removePartyMember(player, false);
				}
				if(getInstance().config_hideIdentiti())
				{
					switch(playerTeam)
					{
						case RED:
						{
							Object[] arrobject = {++redCnt};
							player.setTransformationName(String.format("Red %d", arrobject));
							break;
						}
						case BLUE:
						{
							Object[] arrobject = {++blueCnt};
							player.setTransformationName(String.format("Blue %d", arrobject));
						}
					}
				}
				player.teleToLocation(getRandomTeamLoc(playerTeam), getReflection());
			}
		}
		
		@Override
		public void portParticipantsBack()
		{
			for(Player player : getInstance().getPlayers())
			{
				if(player.getTransformation() == 0)
				{
					player.setTransformationName(null);
				}
				String sloc;
				if((sloc = player.getVar(RET_LOC_VAR)) != null)
				{
					player.unsetVar(RET_LOC_VAR);
					player.teleToLocation(Location.parseLoc(sloc), ReflectionManager.DEFAULT);
					continue;
				}
				player.teleToClosestTown();
			}
		}
		
		@Override
		public void initReflection()
		{
			_instance_id = getInstance().getNewReflectionId();
			InstantZone instantZone = InstantZoneHolder.getInstance().getInstantZone(_instance_id);
			ZONE_DEFAULT = String.format("[pvp_%d_tvt_default]", _instance_id);
			ZONE_BLUE = String.format("[pvp_%d_tvt_spawn_blue]", _instance_id);
			ZONE_RED = String.format("[pvp_%d_tvt_spawn_red]", _instance_id);
			_reflection = new Reflection();
			_reflection.init(instantZone);
			_default_zone = _reflection.getZone(ZONE_DEFAULT);
			_default_zone.addListener(getInstance()._zoneListner);
		}
		
		@Override
		public void doneReflection()
		{
			_default_zone.removeListener(getInstance()._zoneListner);
			_reflection.collapse();
			_reflection = null;
		}
		
		@Override
		public Reflection getReflection()
		{
			return _reflection;
		}
		
		private Location getRandomTeamLoc(TeamType tt)
		{
			if(tt == TeamType.BLUE)
			{
				return _reflection.getZone(ZONE_BLUE).getTerritory().getRandomLoc(_reflection.getGeoIndex());
			}
			if(tt == TeamType.RED)
			{
				return _reflection.getZone(ZONE_RED).getTerritory().getRandomLoc(_reflection.getGeoIndex());
			}
			return null;
		}
		
		@Override
		public void OnEnter(Player player, Zone zone)
		{
			if(player != null && player.getTeam() != TeamType.BLUE && player.getTeam() != TeamType.RED && zone == _default_zone)
			{
				player.teleToClosestTown();
			}
		}
		
		@Override
		public void OnLeave(Player player, Zone zone)
		{
			if(player != null && !_default_zone.checkIfInZone(player.getX(), player.getY(), player.getZ(), getReflection()))
			{
				if(player.getTeam() != TeamType.BLUE && player.getTeam() != TeamType.RED && zone == _default_zone)
				{
					player.teleToClosestTown();
					return;
				}
				double radian = 6.283185307179586 - PositionUtils.convertHeadingToRadian(player.getHeading());
				int x = (int) Math.floor((double) player.getX() - 50.0 * Math.cos(radian));
				int y = (int) Math.floor((double) player.getY() + 50.0 * Math.sin(radian));
				PvPEvent pvPEvent = getInstance();
				pvPEvent.getClass();
				ThreadPoolManager.getInstance().schedule(pvPEvent.new TeleportTask(player, new Location(x, y, player.getZ()).correctGeoZ(), getReflection()), 3000);
			}
		}
		
		@Override
		public void OnExit(Player player)
		{
			if(_blue_team.containsKey(player.getObjectId()))
			{
				_blue_team.remove(player.getObjectId());
			}
			else if(_red_team.containsKey(player.getObjectId()))
			{
				_red_team.remove(player.getObjectId());
			}
			String title = player.getVar("pvp_tvt_title");
			if(title != null)
			{
				player.setTitle(player.getVar("pvp_tvt_title"));
				player.unsetVar("pvp_tvt_title");
			}
		}
		
		@Override
		public void OnTeleport(Player player, int x, int y, int z, Reflection r)
		{
			Location loc;
			if(player != null && !_default_zone.checkIfInZone(x, y, z, getReflection()) && (loc = getRandomTeamLoc(player.getTeam())) != null)
			{
				PvPEvent pvPEvent = getInstance();
				pvPEvent.getClass();
				ThreadPoolManager.getInstance().schedule(pvPEvent.new TeleportTask(player, loc, getReflection()), 3000);
			}
		}
		
		private void rewardPerKill(Map<Integer, ImmutablePair<AtomicInteger, AtomicInteger>> team)
		{
			int itemId = getInstance().config_ItemPerKill();
			if(itemId <= 0)
			{
				return;
			}
			for(Map.Entry<Integer, ImmutablePair<AtomicInteger, AtomicInteger>> e : team.entrySet())
			{
				int oid = e.getKey();
				ImmutablePair<AtomicInteger, AtomicInteger> p = e.getValue();
				int kills = p.getLeft().get();
				Player player = GameObjectsStorage.getPlayer(oid);
				if(kills <= 0 || player == null)
					continue;
				Functions.addItem(player, itemId, (long) kills);
			}
		}
		
		private void reward(Map<Integer, ImmutablePair<AtomicInteger, AtomicInteger>> team, List<Pair<ItemTemplate, Long>> teamReward, List<Pair<ItemTemplate, Long>> topReward)
		{
			int top_oid = -1;
			int top_cnt = Integer.MIN_VALUE;
			for(Map.Entry<Integer, ImmutablePair<AtomicInteger, AtomicInteger>> e : team.entrySet())
			{
				int oid = e.getKey();
				ImmutablePair<AtomicInteger, AtomicInteger> p = e.getValue();
				int kills = p.getLeft().get();
				Player player2 = GameObjectsStorage.getPlayer(oid);
				if(player2 == null)
					continue;
				if(kills > 1)
				{
					for(Pair<ItemTemplate, Long> teamRewardItemInfo : teamReward)
					{
						Functions.addItem(player2, teamRewardItemInfo.getLeft().getItemId(), teamRewardItemInfo.getRight());
					}
					if(top_cnt >= kills)
						continue;
					top_cnt = kills;
					top_oid = oid;
					continue;
				}
				player2.sendMessage(new CustomMessage("PVPEVENTS_YOUR_TEAM_WIN_BUT_NO_PRIZE", player2));
			}
			Player player;
			if(top_oid > 0 && top_cnt > 0 && (player = GameObjectsStorage.getPlayer(top_oid)) != null)
			{
				for(Pair<ItemTemplate, Long> topRewardItemInfo : topReward)
				{
					Functions.addItem(player, topRewardItemInfo.getLeft().getItemId(), topRewardItemInfo.getRight());
				}
				Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TheTvTGameTopPlayerIsS1", new String[] {player.getName()});
				if(getInstance().config_RewardHeroHours() > 0)
				{
					GlobalServices.makeCustomHero(player, (long) (getInstance().config_RewardHeroHours() * 60) * 60);
				}
			}
		}
		
		@Override
		public void MakeWinner()
		{
			int red_pnt;
			int blue_pnt = _blue_points.get();
			if(blue_pnt > (red_pnt = _red_points.get()))
			{
				Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TeamBlueWonTheTvTGameCountIsS1S2", new String[] {String.valueOf(blue_pnt), String.valueOf(red_pnt)});
				reward(_blue_team, getInstance().config_RewardTeamItemIdAndAmount(), getInstance().config_RewardTopItemIdAndAmount());
			}
			else if(blue_pnt < red_pnt)
			{
				Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TeamRedWonTheTvTGameCountIsS1S2", new String[] {String.valueOf(red_pnt), String.valueOf(blue_pnt)});
				reward(_red_team, getInstance().config_RewardTeamItemIdAndAmount(), getInstance().config_RewardTopItemIdAndAmount());
			}
			else if(blue_pnt == red_pnt)
			{
				Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TheTvTGameEndedInATie", ArrayUtils.EMPTY_STRING_ARRAY);
			}
			rewardPerKill(_red_team);
			rewardPerKill(_blue_team);
			getInstance().broadcast(ExEventMatchMessage.FINISH);
		}
		
		private static class RankComparator implements Comparator<Map.Entry<Integer, ImmutablePair<AtomicInteger, AtomicInteger>>>
		{
			
			@Override
			public int compare(Map.Entry<Integer, ImmutablePair<AtomicInteger, AtomicInteger>> o1, Map.Entry<Integer, ImmutablePair<AtomicInteger, AtomicInteger>> o2)
			{
				try
				{
					if(o1 == null && o2 == null)
					{
						return 1;
					}
					if(o1 == null)
					{
						return 1;
					}
					if(o2 == null)
					{
						return -1;
					}
					int i1 = o1.getKey();
					int i2 = o2.getKey();
					int k1 = o1.getValue().getLeft().get();
					int k2;
					return k1 != (k2 = o2.getValue().getLeft().get()) ? k2 - k1 : i2 - i1;
				}
				catch(Exception e)
				{
					return 0;
				}
			}
		}
		
		private class RankBroadcastTask implements Runnable
		{
			private final TvTParticipantController _controller;
			
			public RankBroadcastTask(TvTParticipantController controller)
			{
				_controller = controller;
			}
			
			@Override
			public void run()
			{
				if(getInstance().getState() != PvPEventState.COMPETITION)
				{
					return;
				}
				_controller._rankBroadcastTask = ThreadPoolManager.getInstance().schedule(this, 20000);
			}
		}
	}
	
	private class TeleportAndReviveTask implements Runnable
	{
		private final Player _player;
		private final Location _loc;
		private final Reflection _ref;
		
		public TeleportAndReviveTask(Player player, Location loc, Reflection ref)
		{
			_player = player;
			_loc = loc;
			_ref = ref;
		}
		
		@Override
		public void run()
		{
			Player player = _player;
			synchronized(player)
			{
				_player.teleToLocation(_loc, _ref);
				if(!_player.isConnected())
				{
					_player.onTeleported();
				}
				if(_player.isDead())
				{
					_player.setCurrentHp((double) _player.getMaxHp(), true, true);
					_player.setCurrentCp((double) _player.getMaxCp());
					_player.setCurrentMp((double) _player.getMaxMp());
					_player.broadcastPacket(new Revive(_player));
				}
			}
		}
	}
	
	private class TeleportTask implements Runnable
	{
		private final Player _player;
		private final Location _loc;
		private final Reflection _ref;
		
		public TeleportTask(Player player, Location loc, Reflection ref)
		{
			_player = player;
			_loc = loc;
			_ref = ref;
		}
		
		@Override
		public void run()
		{
			_player.teleToLocation(_loc, _ref);
		}
	}
	
	private class ExitListner implements OnPlayerExitListener
	{
		@Override
		public void onPlayerExit(Player player)
		{
			try
			{
				if(getInstance().getState() == PvPEventState.STANDBY)
				{
					return;
				}
				getInstance().getRule().getParticipantController().OnExit(player);
				getInstance()._participants.remove(player.getObjectId());
			}
			catch(Exception e)
			{
				_log.warn("PVPEvent.onPlayerExit :", e);
			}
		}
	}
	
	private class TeleportListner implements OnTeleportListener
	{
		@Override
		public void onTeleport(Player player, int x, int y, int z, Reflection r)
		{
			try
			{
				if(getInstance().getState() != PvPEventState.COMPETITION)
				{
					return;
				}
				getInstance().getRule().getParticipantController().OnTeleport(player, x, y, z, r);
			}
			catch(Exception e)
			{
				_log.warn("PVPEvent.onTeleport :", e);
			}
		}
	}
	
	private class ZoneEnterLeaveListner implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature actor)
		{
			try
			{
				if(getInstance().getState() != PvPEventState.COMPETITION || !actor.isPlayer())
				{
					return;
				}
				getInstance().getRule().getParticipantController().OnEnter(actor.getPlayer(), zone);
			}
			catch(Exception e)
			{
				_log.warn("PVPEvent.onZoneEnter :", e);
			}
		}
		
		@Override
		public void onZoneLeave(Zone zone, Creature actor)
		{
			try
			{
				if(getInstance().getState() != PvPEventState.COMPETITION || !actor.isPlayer())
				{
					return;
				}
				getInstance().getRule().getParticipantController().OnLeave(actor.getPlayer(), zone);
			}
			catch(Exception e)
			{
				_log.warn("PVPEvent.onZoneLeave :", e);
			}
		}
	}
	
	private class DieListner implements OnDeathListener
	{
		@Override
		public void onDeath(Creature actor, Creature killer)
		{
			try
			{
				if(getInstance().getState() != PvPEventState.COMPETITION)
				{
					return;
				}
				Player ptarget = actor.getPlayer();
				Player pkiller = killer.getPlayer();
				if(ptarget != null)
				{
					getInstance().getRule().getParticipantController().OnPlayerDied(ptarget, pkiller);
				}
			}
			catch(Exception e)
			{
				_log.warn("PVPEvent.onDeath :", e);
			}
		}
	}
	
	private class CompetitionRunTask implements Runnable
	{
		private final int _left;
		
		public CompetitionRunTask(int left)
		{
			_left = left;
		}
		
		@Override
		public void run()
		{
			switch(_left)
			{
				case 30:
				{
					getInstance().scheduleProcessTask(new CompetitionRunTask(_left - 25), 25000);
					return;
				}
				case 0:
				{
					getInstance().scheduleStateChange(PvPEventState.COMPETITION, 100);
					getInstance().broadcast(ExEventMatchMessage.START);
					return;
				}
				case 5:
				{
					getInstance().broadcast(ExEventMatchMessage.COUNT5);
					for(Player player : getInstance().getPlayers())
					{
						player.broadcastUserInfo(true);
					}
					break;
				}
				case 4:
				{
					getInstance().broadcast(ExEventMatchMessage.COUNT4);
					break;
				}
				case 3:
				{
					getInstance().broadcast(ExEventMatchMessage.COUNT3);
					break;
				}
				case 2:
				{
					getInstance().broadcast(ExEventMatchMessage.COUNT2);
					break;
				}
				case 1:
				{
					getInstance().broadcast(ExEventMatchMessage.COUNT1);
				}
			}
			getInstance().scheduleProcessTask(new CompetitionRunTask(_left - 1), 1000);
		}
	}
	
	private class RegisrationTask implements Runnable
	{
		private final int _left;
		private final RegisrationState _to_reg_state;
		
		public RegisrationTask(RegisrationState to_state, int left)
		{
			_left = left;
			_to_reg_state = to_state;
		}
		
		@Override
		public void run()
		{
			if(getInstance()._regState != _to_reg_state && _to_reg_state == RegisrationState.ANNOUNCE)
			{
				if(getInstance()._desireContainer != null)
				{
					getInstance()._desireContainer.clear();
					getInstance()._desireContainer = null;
				}
				getInstance()._desireContainer = new ConcurrentSkipListSet();
			}
			getInstance()._regState = _to_reg_state;
			switch(_to_reg_state)
			{
				case ANNOUNCE:
				{
					if(_left > 0)
					{
						Announcements.getInstance().announceByCustomMessage("events.PvPEvent.EventS1StartAtS2Minutes", new String[] {getRule().name(), String.valueOf(_left)});
						getInstance().scheduleProcessTask(new RegisrationTask(RegisrationState.ANNOUNCE, Math.max(0, _left - getInstance()._event_announce_reductor)), getInstance()._event_announce_reductor * 60 * 1000);
						break;
					}
					getInstance().scheduleProcessTask(new RegisrationTask(RegisrationState.REQUEST, 0), 1000);
					break;
				}
				case REQUEST:
				{
					getInstance().broadcastParticipationRequest();
					getInstance().scheduleProcessTask(new RegisrationTask(getInstance().config_isUseCapcha() ? RegisrationState.CAPCHA : RegisrationState.MORPH, 0), 40000);
					break;
				}
				case CAPCHA:
				{
					getInstance().broadcastCapchaRequest();
					getInstance().scheduleProcessTask(new RegisrationTask(RegisrationState.MORPH, 0), 40000);
					break;
				}
				case MORPH:
				{
					getInstance().morphDesires();
				}
			}
		}
	}
	
	private class PvPStateTask implements Runnable
	{
		private final PvPEventState _to_state;
		
		public PvPStateTask(PvPEventState to_state)
		{
			_to_state = to_state;
		}
		
		@Override
		public void run()
		{
			try
			{
				switch(_to_state)
				{
					case STANDBY:
					{
						getInstance().goStandby();
						break;
					}
					case REGISTRATION:
					{
						getInstance().goRegistration();
						break;
					}
					case PORTING_TO:
					{
						getInstance().goPortingTo();
						break;
					}
					case PREPARE_TO:
					{
						getInstance().goPrepareTo();
						break;
					}
					case COMPETITION:
					{
						getInstance().goCompetition();
						break;
					}
					case WINNER:
					{
						getInstance().goWinner();
						break;
					}
					case PREPARE_FROM:
					{
						getInstance().goPrepareFrom();
						break;
					}
					case PORTING_FROM:
					{
						getInstance().goPortingFrom();
					}
				}
			}
			catch(Exception e)
			{
				_log.warn("PvPEvent: Exception on changing state to " + _to_state + " state.", e);
				e.printStackTrace();
			}
		}
	}
}