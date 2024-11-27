package l2.gameserver.model;

import l2.commons.collections.LazyArrayList;
import l2.commons.collections.MultiValueSet;
import l2.commons.dbutils.DbUtils;
import l2.commons.lang.reference.HardReference;
import l2.commons.lang.reference.HardReferences;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.GameTimeController;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.ai.NextAction;
import l2.gameserver.ai.PlayerAI;
import l2.gameserver.cache.Msg;
import l2.gameserver.dao.AccountBonusDAO;
import l2.gameserver.dao.CharacterDAO;
import l2.gameserver.dao.CharacterGroupReuseDAO;
import l2.gameserver.dao.CharacterPostFriendDAO;
import l2.gameserver.dao.CharacterVariablesDAO;
import l2.gameserver.dao.EffectsDAO;
import l2.gameserver.data.xml.holder.EventHolder;
import l2.gameserver.data.xml.holder.HennaHolder;
import l2.gameserver.data.xml.holder.InstantZoneHolder;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.data.xml.holder.MultiSellHolder;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.data.xml.holder.RecipeHolder;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.data.xml.holder.SkillAcquireHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.database.mysql;
import l2.gameserver.handler.items.IItemHandler;
import l2.gameserver.handler.items.IRefineryHandler;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.instancemanager.CursedWeaponsManager;
import l2.gameserver.instancemanager.DimensionalRiftManager;
import l2.gameserver.instancemanager.MatchingRoomManager;
import l2.gameserver.instancemanager.QuestManager;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.listener.actor.player.OnAnswerListener;
import l2.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2.gameserver.listener.actor.player.impl.ScriptAnswerListener;
import l2.gameserver.listener.actor.player.impl.SummonAnswerListener;
import l2.gameserver.model.actor.instances.player.Bonus;
import l2.gameserver.model.actor.instances.player.FriendList;
import l2.gameserver.model.actor.instances.player.Macro;
import l2.gameserver.model.actor.instances.player.MacroList;
import l2.gameserver.model.actor.instances.player.ShortCut;
import l2.gameserver.model.actor.instances.player.ShortCutList;
import l2.gameserver.model.actor.listener.PlayerListenerList;
import l2.gameserver.model.actor.recorder.PlayerStatsChangeRecorder;
import l2.gameserver.model.base.AcquireType;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.base.Element;
import l2.gameserver.model.base.Experience;
import l2.gameserver.model.base.InvisibleType;
import l2.gameserver.model.base.PlayerAccess;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.base.RestartType;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.model.chat.chatfilter.ChatMsg;
import l2.gameserver.model.entity.DimensionalRift;
import l2.gameserver.model.entity.Mentoring;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.entity.SevenSignsFestival.DarknessFestival;
import l2.gameserver.model.entity.boat.Boat;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.model.entity.events.impl.DuelEvent;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.entity.oly.CompetitionState;
import l2.gameserver.model.entity.oly.HeroController;
import l2.gameserver.model.entity.oly.NoblesController;
import l2.gameserver.model.entity.oly.OlyController;
import l2.gameserver.model.entity.oly.Participant;
import l2.gameserver.model.entity.oly.ParticipantPool;
import l2.gameserver.model.entity.oly.Stadium;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.model.instances.*;
import l2.gameserver.model.items.*;
import l2.gameserver.model.items.attachment.FlagItemAttachment;
import l2.gameserver.model.items.attachment.PickableAttachment;
import l2.gameserver.model.matching.MatchingRoom;
import l2.gameserver.model.pledge.Alliance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.pledge.Privilege;
import l2.gameserver.model.pledge.RankPrivs;
import l2.gameserver.model.pledge.SubUnit;
import l2.gameserver.model.pledge.UnitMember;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestEventType;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.IStaticPacket;
import l2.gameserver.network.l2.components.SceneMovie;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.*;
import l2.gameserver.scripts.Events;
import l2.gameserver.skills.AbnormalEffect;
import l2.gameserver.skills.EffectType;
import l2.gameserver.skills.TimeStamp;
import l2.gameserver.skills.effects.EffectCubic;
import l2.gameserver.skills.skillclasses.Transformation;
import l2.gameserver.stats.Formulas;
import l2.gameserver.stats.Stats;
import l2.gameserver.tables.CharTemplateTable;
import l2.gameserver.tables.ClanTable;
import l2.gameserver.tables.PetDataTable;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.tables.SkillTreeTable;
import l2.gameserver.taskmanager.AutoSaveManager;
import l2.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2.gameserver.templates.FishTemplate;
import l2.gameserver.templates.Henna;
import l2.gameserver.templates.InstantZone;
import l2.gameserver.templates.PlayerTemplate;
import l2.gameserver.templates.item.ArmorTemplate;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.templates.item.WeaponTemplate;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Player extends Playable implements PlayerGroup{
	public static final int DEFAULT_TITLE_COLOR = 16777079;
	public static final int MAX_POST_FRIEND_SIZE = 100;
	public static final int MAX_FRIEND_SIZE = 128;
	public static final String NO_TRADERS_VAR = "notraders";
	public static final String CUSTOM_HERO_END_TIME_VAR = "CustomHeroEndTime";
	public static final String ANIMATION_OF_CAST_RANGE_VAR = "buffAnimRange";
	public static final String LAST_PVP_PK_KILL_VAR_NAME = "LastPVPPKKill";
	public static final int OBSERVER_NONE = 0;
	public static final int OBSERVER_STARTING = 1;
	public static final int OBSERVER_STARTED = 3;
	public static final int OBSERVER_LEAVING = 2;
	public static final int STORE_PRIVATE_NONE = 0;
	public static final int STORE_PRIVATE_SELL = 1;
	public static final int STORE_PRIVATE_BUY = 3;
	public static final int STORE_PRIVATE_MANUFACTURE = 5;
	public static final int STORE_OBSERVING_GAMES = 7;
	public static final int STORE_PRIVATE_SELL_PACKAGE = 8;
	public static final int RANK_VAGABOND = 0;
	public static final int RANK_VASSAL = 1;
	public static final int RANK_HEIR = 2;
	public static final int RANK_KNIGHT = 3;
	public static final int RANK_WISEMAN = 4;
	public static final int RANK_BARON = 5;
	public static final int RANK_VISCOUNT = 6;
	public static final int RANK_COUNT = 7;
	public static final int RANK_MARQUIS = 8;
	public static final int LANG_ENG = 0;
	public static final int LANG_RUS = 1;
	public static final int LANG_UNK = -1;
	public static final int PLAYER_SEX_MALE = 0;
	public static final int PLAYER_SEX_FEMALE = 1;
	public static final int[] EXPERTISE_LEVELS = {
		0,
		20,
		40,
		52,
		61,
		76,
		Integer.MAX_VALUE };
	private static final Logger _log = LoggerFactory.getLogger(Player.class);
	private static final String NOT_CONNECTED = "<not connected>";
	public final AntiFlood antiFlood;
	private final PcInventory _inventory;
	private final Warehouse _warehouse;
	private final ItemContainer _refund;
	private final PcFreight _freight;
	private final Deque<ChatMsg> _msgBucket;
	private final Map<Integer, Recipe> _recipebook;
	private final Map<Integer, Recipe> _commonrecipebook;
	private final Map<String, QuestState> _quests;
	private final ShortCutList _shortCuts;
	private final MacroList _macroses;
	private final Henna[] _henna;
	private final Map<Integer, String> _blockList;
	private final FriendList _friendList;
	private final Fishing _fishing;
	private final Lock _storeLock;
	private final Map<Integer, Long> _instancesReuses;
	private final AtomicReference<MoveToLocationOffloadData> _mtlOffloadData;
	private final MultiValueSet<String> _vars;
	private final Map<Integer, PremiumItem> _premiumItems;
	private final AtomicBoolean _isLogout;
	private final Set<Integer> _activeSoulShots;
	private final AtomicInteger _observerMode;
	private final Bonus _bonus;
	private final List<String> _blockedActions;
	private final IntObjectMap<TimeStamp> _sharedGroupReuses;
	private final IntSet _recommendedCharIds;
	private final AtomicBoolean isActive;
	public Map<Integer, SubClass> _classlist = new HashMap<>(4);
	public int expertiseIndex;
	public int _telemode;
	public boolean entering;
	public Location _stablePoint;
	public int[] _loto;
	public int[] _race;
	protected int _baseClass;
	protected SubClass _activeClass;
	protected int _pvpFlag;
	volatile boolean sittingTaskLaunched;
	Map<Integer, Skill> _transformationSkills;
	private GameClient _connection;
	private String _login;
	private int _karma;
	private int _pkKills;
	private int _pvpKills;
	private int _face;
	private int _hairStyle;
	private int _hairColor;
	private boolean _isUndying;
	private int _deleteTimer;
	private Stadium _olyObserveStadium;
	private Participant _olyParticipant;
	private long _createTime;
	private long _onlineTime;
	private long _onlineBeginTime;
	private long _leaveClanTime;
	private long _deleteClanTime;
	private long _NoChannel;
	private long _NoChannelBegin;
	private long _uptime;
	private long _lastAccess;
	private int _nameColor;
	private int _titlecolor;
	private String _disconnectedTitle = Config.DISCONNECTED_PLAYER_TITLE;
	private int _disconnectedTitleColor = Config.DISCONNECTED_PLAYER_TITLE_COLOR;
	private boolean _overloaded;
	private int _fakeDeath;
	private int _waitTimeWhenSit;
	private boolean _autoLoot = Config.AUTO_LOOT;
	private boolean AutoLootHerbs = Config.AUTO_LOOT_HERBS;
	private boolean AutoLootAdena = Config.AUTO_LOOT_ADENA;
	private long _lastNpcInteractionTime;
	private int _privatestore;
	private String _manufactureName;
	private List<ManufactureItem> _createList;
	private String _sellStoreName;
	private List<TradeItem> _sellList;
	private List<TradeItem> _packageSellList;
	private String _buyStoreName;
	private List<TradeItem> _buyList;
	private List<TradeItem> _tradeList;
	private int _hennaSTR;
	private int _hennaINT;
	private int _hennaDEX;
	private int _hennaMEN;
	private int _hennaWIT;
	private int _hennaCON;
	private Party _party;
	private Location _lastPartyPosition;
	private Clan _clan;
	private int _pledgeClass;
	private int _pledgeType;
	private int _powerGrade;
	private int _lvlJoinedAcademy;
	private int _apprentice;
	private int _accessLevel;
	private PlayerAccess _playerAccess;
	private boolean _messageRefusal;
	private boolean _tradeRefusal;
	private boolean _blockAll;
	private Summon _summon;
	private boolean _riding;
	private Map<Integer, EffectCubic> _cubics;
	private int _agathionId;
	private Request _request;
	private ItemInstance _arrowItem;
	private WeaponTemplate _fistsWeaponItem;
	private Map<Integer, String> _chars;
	private ItemInstance _enchantScroll;
	private IRefineryHandler _refineryHandler;
	private Warehouse.WarehouseType _usingWHType;
	private boolean _isOnline;
	private HardReference<NpcInstance> _lastNpc;
	private MultiSellHolder.MultiSellListContainer _multisell;
	private WorldRegion _observerRegion;
	private boolean _hero;
	private Boat _boat;
	private Location _inBoatPosition;
	private Future<?> _bonusExpiration;
	private boolean _isSitting;
	private StaticObjectInstance _sittingObject;
	private boolean _noble;
	private int _varka;
	private int _ketra;
	private int _ram;
	private byte[] _keyBindings;
	private int _cursedWeaponEquippedId;
	private boolean _isFishing;
	private Future<?> _taskWater;
	private Future<?> _autoSaveTask;
	private Future<?> _kickTask;
	private Future<?> _pcCafePointsTask;
	private Future<?> _unjailTask;
	private Future<?> _customHeroRemoveTask;
	private int _zoneMask;
	private boolean _offline;
	private int _transformationId;
	private int _transformationTemplate;
	private String _transformationName;
	private String _transformationTitle;
	private int _pcBangPoints;
	private int _expandInventory;
	private int _expandWarehouse;
	private int _buffAnimRange;
	private int _battlefieldChatId;
	private InvisibleType _invisibleType;
	private IntObjectMap<String> _postFriends;
	private boolean _notShowTraders;
	private boolean _debug;
	private long _dropDisabled;
	private long _lastItemAuctionInfoRequest;
	private Pair<Integer, OnAnswerListener> _askDialog;
	private MatchingRoom _matchingRoom;
	private Mentoring _mentroring;
	private int _receivedRec;
	private int _givableRec;
	private Future<?> _updateEffectIconsTask;
	private ScheduledFuture<?> _broadcastCharInfoTask;
	private int _polyNpcId;
	private Future<?> _userInfoTask;
	private int _mountNpcId;
	private int _mountObjId;
	private int _mountLevel;
	private boolean _resurect_prohibited;
	private boolean _maried;
	private int _partnerId;
	private int _coupleId;
	private boolean _maryrequest;
	private boolean _maryaccepted;
	private boolean _charmOfCourage;
	private int _increasedForce;
	private long _increasedForceLastUpdateTimeStamp;
	private Future<?> _increasedForceCleanupTask;
	private int _consumedSouls;
	private long _lastFalling;
	private Location _lastClientPosition;
	private Location _lastServerPosition;
	private int _useSeed;
	private Future<?> _PvPRegTask;
	private long _lastPvpAttack;
	private TamedBeastInstance _tamedBeast;
	private long _lastAttackPacket;
	private Location _groundSkillLoc;
	private int _buyListId;
	private int _incorrectValidateCount;
	private int _movieId;
	private boolean _isInMovie;
	private ItemInstance _petControlItem;
	private Map<Integer, Long> _traps;
	private Future<?> _hourlyTask;
	private int _hoursInGame;
	private Map<String, String> _userSession;
	private long _afterTeleportPortectionTime;

	public Player(int objectId, PlayerTemplate template, String accountName) {
		super(objectId, template);
		_inventory = new PcInventory(this);
		_warehouse = new PcWarehouse(this);
		_refund = new PcRefund(this);
		_freight = new PcFreight(this);
		antiFlood = new AntiFlood();
		_lastNpcInteractionTime = 0;
		_msgBucket = new LinkedList<>();
		_recipebook = new TreeMap<>();
		_commonrecipebook = new TreeMap<>();
		_premiumItems = new TreeMap<>();
		_quests = new HashMap<>();
		_shortCuts = new ShortCutList(this);
		_macroses = new MacroList(this);
		_createList = Collections.emptyList();
		_sellList = Collections.emptyList();
		_packageSellList = Collections.emptyList();
		_buyList = Collections.emptyList();
		_tradeList = Collections.emptyList();
		_henna = new Henna[3];
		_pledgeClass = 0;
		_pledgeType = -128;
		_powerGrade = 0;
		_lvlJoinedAcademy = 0;
		_apprentice = 0;
		_playerAccess = new PlayerAccess();
		_messageRefusal = false;
		_tradeRefusal = false;
		_blockAll = false;
		_summon = null;
		_cubics = null;
		_agathionId = 0;
		_chars = new HashMap<>(8);
		expertiseIndex = 0;
		_enchantScroll = null;
		_refineryHandler = null;
		_isOnline = false;
		_isLogout = new AtomicBoolean();
		_lastNpc = HardReferences.emptyRef();
		_multisell = null;
		_activeSoulShots = new CopyOnWriteArraySet<>();
		_observerMode = new AtomicInteger(0);
		_telemode = 0;
		entering = true;
		_stablePoint = null;
		_loto = new int[5];
		_race = new int[2];
		_blockList = new ConcurrentSkipListMap<>();
		_friendList = new FriendList(this);
		_hero = false;
		_baseClass = -1;
		_activeClass = null;
		_bonus = new Bonus();
		_noble = false;
		_varka = 0;
		_ketra = 0;
		_ram = 0;
		_keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;
		_cursedWeaponEquippedId = 0;
		_fishing = new Fishing(this);
		_storeLock = new ReentrantLock();
		_offline = false;
		_transformationSkills = new HashMap<>();
		_expandInventory = 0;
		_expandWarehouse = 0;
		_buffAnimRange = 1500;
		_invisibleType = InvisibleType.NONE;
		_postFriends = Containers.emptyIntObjectMap();
		_blockedActions = new ArrayList<>();
		_notShowTraders = false;
		_debug = false;
		_sharedGroupReuses = new CHashIntObjectMap<>();
		_askDialog = null;
		_instancesReuses = new ConcurrentHashMap<>();
		_receivedRec = 0;
		_givableRec = 0;
		_recommendedCharIds = new HashIntSet();
		_mtlOffloadData = new AtomicReference<>(null);
		_vars = new MultiValueSet<>();
		_resurect_prohibited = false;
		_maried = false;
		_partnerId = 0;
		_coupleId = 0;
		_maryrequest = false;
		_maryaccepted = false;
		_charmOfCourage = false;
		_increasedForce = 0;
		_increasedForceLastUpdateTimeStamp = 0;
		_increasedForceCleanupTask = null;
		_consumedSouls = 0;
		_useSeed = 0;
		_lastAttackPacket = 0;
		_incorrectValidateCount = 0;
		_movieId = 0;
		_petControlItem = null;
		isActive = new AtomicBoolean();
		_hoursInGame = 0;
		_afterTeleportPortectionTime = 0;
		_login = accountName;
		_nameColor = 16777215;
		_titlecolor = 16777079;
		_baseClass = getClassId().getId();
	}

	private Player(int objectId, PlayerTemplate template) {
		this(objectId, template, null);
		_ai = new PlayerAI(this);
		if (!Config.EVERYBODY_HAS_ADMIN_RIGHTS) {
			setPlayerAccess(Config.gmlist.get(objectId));
		} else {
			setPlayerAccess(Config.gmlist.get(0));
		}
	}

	public static Player create(int classId, int sex, String accountName, String name, int hairStyle, int hairColor, int face) {
		PlayerTemplate template = CharTemplateTable.getInstance().getTemplate(classId, sex != 0);
		Player player = new Player(IdFactory.getInstance().getNextId(), template, accountName);
		player.setName(name);
		player.setTitle("");
		player.setHairStyle(hairStyle);
		player.setHairColor(hairColor);
		player.setFace(face);
		player.setCreateTime(System.currentTimeMillis());
		if (!CharacterDAO.getInstance().insert(player)) {
			return null;
		}
		return player;
	}

	public static Player restore(int objectId) {
		Player player;
		block51:
		{
			player = null;
			Connection con = null;
			Statement statement = null;
			Statement statement2 = null;
			PreparedStatement statement3 = null;
			ResultSet rset = null;
			ResultSet rset2 = null;
			ResultSet rset3 = null;
			try {
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.createStatement();
				statement2 = con.createStatement();
				rset = statement.executeQuery("SELECT * FROM `characters` WHERE `obj_Id`=" + objectId + " LIMIT 1");
				rset2 = statement2.executeQuery("SELECT `class_id` FROM `character_subclasses` WHERE `char_obj_id`=" + objectId + " AND `isBase`=1 LIMIT 1");
				if (!rset.next() || !rset2.next())
					break block51;
				int classId = rset2.getInt("class_id");
				boolean female = rset.getInt("sex") == 1;
				PlayerTemplate template = CharTemplateTable.getInstance().getTemplate(classId, female);
				player = new Player(objectId, template);
				CharacterVariablesDAO.getInstance().loadVariables(objectId, player.getVars());
				player.loadInstanceReuses();
				player.loadPremiumItemList();
				player._friendList.restore();
				player._postFriends = CharacterPostFriendDAO.getInstance().select(player);
				CharacterGroupReuseDAO.getInstance().select(player);
				player.setBaseClass(classId);
				player._login = rset.getString("account_name");
				String name = rset.getString("char_name");
				player.setName(name);
				player.setFace(rset.getInt("face"));
				player.setHairStyle(rset.getInt("hairStyle"));
				player.setHairColor(rset.getInt("hairColor"));
				player.setHeading(0);
				player.setKarma(rset.getInt("karma"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));
				player.setLeaveClanTime(rset.getLong("leaveclan") * 1000);
				if (player.getLeaveClanTime() > 0 && player.canJoinClan()) {
					player.setLeaveClanTime(0);
				}
				player.setDeleteClanTime(rset.getLong("deleteclan") * 1000);
				if (player.getDeleteClanTime() > 0 && player.canCreateClan()) {
					player.setDeleteClanTime(0);
				}
				player.setNoChannel(rset.getLong("nochannel") * 1000);
				if (player.getNoChannel() > 0 && player.getNoChannelRemained() < 0) {
					player.setNoChannel(0);
				}
				player.setOnlineTime(rset.getLong("onlinetime") * 1000);
				int clanId = rset.getInt("clanid");
				if (clanId > 0) {
					player.setClan(ClanTable.getInstance().getClan(clanId));
					player.setPledgeType(rset.getInt("pledge_type"));
					player.setPowerGrade(rset.getInt("pledge_rank"));
					player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
					player.setApprentice(rset.getInt("apprentice"));
				}
				player.setCreateTime(rset.getLong("createtime") * 1000);
				player.setDeleteTimer(rset.getInt("deletetime"));
				player.setTitle(rset.getString("title"));
				if (player.getVar("titlecolor") != null) {
					player.setTitleColor(Integer.decode("0x" + player.getVar("titlecolor")));
				}
				if (player.getVar("namecolor") == null) {
					if (player.isGM()) {
						player.setNameColor(Config.GM_NAME_COLOUR);
					} else if (player.getClan() != null && player.getClan().getLeaderId() == player.getObjectId()) {
						player.setNameColor(Config.CLANLEADER_NAME_COLOUR);
					} else {
						player.setNameColor(Config.NORMAL_NAME_COLOUR);
					}
				} else {
					player.setNameColor(Integer.decode("0x" + player.getVar("namecolor")));
				}
				if (Config.AUTO_LOOT_INDIVIDUAL) {
					player._autoLoot = player.getVarB("AutoLoot", Config.AUTO_LOOT);
					player.AutoLootHerbs = player.getVarB("AutoLootHerbs", Config.AUTO_LOOT_HERBS);
				}
				player.setFistsWeaponItem(player.findFistsWeaponItem(classId));
				player.setUptime(System.currentTimeMillis());
				player.setLastAccess(rset.getLong("lastAccess"));
				int givableRecs = rset.getInt("rec_left");
				int receibedRecs = rset.getInt("rec_have");
				player.setKeyBindings(rset.getBytes("key_bindings"));
				player.setPcBangPoints(rset.getInt("pcBangPoints"));
				player.restoreRecipeBook();
				player.setNoble(NoblesController.getInstance().isNobles(player));
				boolean removeHeroSkills = false;
				if (Config.OLY_ENABLED) {
					player.setHero(HeroController.getInstance().isCurrentHero(player));
					if (player.isHero()) {
						HeroController.getInstance().loadDiary(player.getObjectId());
					}
					if (Config.ALT_ALLOW_CUSTOM_HERO && !player.isHero() && player.getVar("CustomHeroEndTime") != null) {
						long customHeroEndTime = player.getVarLong("CustomHeroEndTime", 0);
						long customHeroLeftTimeSec = customHeroEndTime - System.currentTimeMillis() / 1000;
						if (customHeroLeftTimeSec > 0) {
							player.setCustomHero(true, customHeroLeftTimeSec, false);
						} else {
							player.setCustomHero(false, 0, false);
							removeHeroSkills = true;
						}
					}
				}
				player.updatePledgeClass();
				int reflection = 0;
				if (player.getVar("jailed") != null && System.currentTimeMillis() / 1000 < (long) (Integer.parseInt(player.getVar("jailed")) + 60)) {
					player.setXYZ(-114648, -249384, -2984);
					player.sitDown(null);
					player.block();
					player._unjailTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.UnJailTask(player), (long) Integer.parseInt(player.getVar("jailed")) * 1000);
				} else {
					player.setXYZ(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
					String ref = player.getVar("reflection");
					if (ref != null && (reflection = Integer.parseInt(ref)) > 0) {
						String back = player.getVar("backCoords");
						if (back != null) {
							player.setLoc(Location.parseLoc(back));
							player.unsetVar("backCoords");
						}
						reflection = 0;
					}
				}
				player.setReflection(reflection);
				EventHolder.getInstance().findEvent(player);
				Quest.restoreQuestStates(player);
				player.getInventory().restore();
				restoreCharSubClasses(player);
				player.restoreRecommendedCharacters();
				player.restoreGivableAndReceivedRec(givableRecs, receibedRecs);
				String var;
				try {
					var = player.getVar("ExpandInventory");
					if (var != null) {
						player.setExpandInventory(Integer.parseInt(var));
					}
				} catch (Exception e) {
					_log.error("", e);
				}
				try {
					var = player.getVar("ExpandWarehouse");
					if (var != null) {
						player.setExpandWarehouse(Integer.parseInt(var));
					}
				} catch (Exception e) {
					_log.error("", e);
				}
				try {
					var = player.getVar("buffAnimRange");
					if (var != null) {
						player.setBuffAnimRange(Integer.parseInt(var));
					}
				} catch (Exception e) {
					_log.error("", e);
				}
				try {
					var = player.getVar("notraders");
					if (var != null) {
						player.setNotShowTraders(Boolean.parseBoolean(var));
					}
				} catch (Exception e) {
					_log.error("", e);
				}
				try {
					var = player.getVar("pet");
					if (var != null) {
						player.setPetControlItem(Integer.parseInt(var));
					}
				} catch (Exception e) {
					_log.error("", e);
				}
				statement3 = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id!=?");
				statement3.setString(1, player._login);
				statement3.setInt(2, objectId);
				rset3 = statement3.executeQuery();
				while (rset3.next()) {
					Integer charId = rset3.getInt("obj_Id");
					String charName = rset3.getString("char_name");
					player._chars.put(charId, charName);
				}
				DbUtils.close(statement3, rset3);
				if (removeHeroSkills) {
					HeroController.removeSkills(player);
				}
				LazyArrayList<Zone> zones = LazyArrayList.newInstance();
				World.getZones(zones, player.getLoc(), player.getReflection());
				if (!zones.isEmpty()) {
					for (Zone zone : zones) {
						if (zone.getType() == Zone.ZoneType.no_restart) {
							if (System.currentTimeMillis() / 1000 - player.getLastAccess() <= zone.getRestartTime())
								continue;
							player.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.EnterWorld.TeleportedReasonNoRestart", player));
							player.setLoc(TeleportUtils.getRestartLocation(player, RestartType.TO_VILLAGE));
							continue;
						}
						if (zone.getType() != Zone.ZoneType.SIEGE)
							continue;
						SiegeEvent siegeEvent = player.getEvent(SiegeEvent.class);
						if (siegeEvent != null) {
							player.setLoc(siegeEvent.getEnterLoc(player));
							continue;
						}
						Residence r = ResidenceHolder.getInstance().getResidence(zone.getParams().getInteger("residence"));
						player.setLoc(r.getNotOwnerRestartPoint(player));
					}
				}
				LazyArrayList.recycle(zones);
				if (DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getLoc(), false)) {
					player.setLoc(DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords());
				}
				player.restoreBlockList();
				player._macroses.restore();
				player.refreshExpertisePenalty();
				player.refreshOverloaded();
				player.getWarehouse().restore();
				player.getFreight().restore();
				player.restoreTradeList();
				if (player.getVar("storemode") != null) {
					player.setPrivateStoreType(Integer.parseInt(player.getVar("storemode")));
					player.setSitting(true);
				}
				player.updateKetraVarka();
				player.updateRam();
				if (player.getVar("lang@") == null) {
					player.setVar("lang@", Config.DEFAULT_LANG, -1);
				}
				if (Config.SERVICES_ENABLE_NO_CARRIER && player.getVar("noCarrier") == null) {
					player.setVar("noCarrier", Config.SERVICES_NO_CARRIER_DEFAULT_TIME, -1);
				}
			} catch (Exception e) {
				_log.error("Could not restore char data!", e);
			} finally {
				DbUtils.closeQuietly(statement2, rset2);
				DbUtils.closeQuietly(statement3, rset3);
				DbUtils.closeQuietly(con, statement, rset);
			}
		}
		return player;
	}

	public static void restoreCharSubClasses(Player player) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT class_id,exp,sp,curHp,curCp,curMp,active,isBase,death_penalty FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			SubClass activeSubclass = null;
			while (rset.next()) {
				SubClass subClass = new SubClass();
				subClass.setBase(rset.getInt("isBase") != 0);
				subClass.setClassId(rset.getInt("class_id"));
				subClass.setExp(rset.getLong("exp"));
				subClass.setSp(rset.getInt("sp"));
				subClass.setHp(rset.getDouble("curHp"));
				subClass.setMp(rset.getDouble("curMp"));
				subClass.setCp(rset.getDouble("curCp"));
				subClass.setDeathPenalty(new DeathPenalty(player, rset.getInt("death_penalty")));
				boolean  active = rset.getInt("active") != 0;
				if (active) {
					activeSubclass = subClass;
				}
				player.getSubClasses().put(subClass.getClassId(), subClass);
			}
			if (player.getSubClasses().size() == 0) {
				throw new Exception("There are no one subclass for player: " + player);
			}
			int BaseClassId = player.getBaseClassId();
			if (BaseClassId == -1) {
				throw new Exception("There are no base subclass for player: " + player);
			}
			if (activeSubclass != null) {
				player.setActiveSubClass(activeSubclass.getClassId(), false);
			}
			if (player.getActiveClass() == null) {
				SubClass subClass = player.getSubClasses().get(BaseClassId);
				subClass.setActive(true);
				player.setActiveSubClass(subClass.getClassId(), false);
			}
		} catch (Exception e) {
			_log.warn("Could not restore char sub-classes: " + e);
			_log.error("", e);
		} finally {
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public int buffAnimRange() {
		return _buffAnimRange;
	}

	public void setBuffAnimRange(int value) {
		_buffAnimRange = value;
	}

	@Override
	public HardReference<Player> getRef() {
		return (HardReference<Player>) super.getRef();
	}

	public String getAccountName() {
		if (_connection == null) {
			return _login;
		}
		return _connection.getLogin();
	}

	public String getIP() {
		if (_connection == null) {
			return "<not connected>";
		}
		return _connection.getIpAddr();
	}

	public Map<Integer, String> getAccountChars() {
		return _chars;
	}

	@Override
	public final PlayerTemplate getTemplate() {
		return (PlayerTemplate) _template;
	}

	@Override
	public PlayerTemplate getBaseTemplate() {
		return (PlayerTemplate) _baseTemplate;
	}

	public void changeSex() {
		boolean male = true;
		if (getSex() == 1) {
			male = false;
		}
		_template = CharTemplateTable.getInstance().getTemplate(getClassId(), !male);
	}

	@Override
	public PlayerAI getAI() {
		return (PlayerAI) _ai;
	}

	@Override
	public void doCast(Skill skill, Creature target, boolean forceUse) {
		if (skill == null) {
			return;
		}
		super.doCast(skill, target, forceUse);
		if (Config.ALT_TELEPORT_PROTECTION && getAfterTeleportPortectionTime() > System.currentTimeMillis()) {
			setAfterTeleportPortectionTime(0);
			sendMessage(new CustomMessage("alt.teleport_protect_gonna", this));
		}
	}

	@Override
	public void altUseSkill(Skill skill, Creature target) {
		super.altUseSkill(skill, target);
		if (Config.ALT_TELEPORT_PROTECTION && isPlayer()) {
			Player player = getPlayer();
			if (getPlayer().getAfterTeleportPortectionTime() > System.currentTimeMillis()) {
				getPlayer().setAfterTeleportPortectionTime(0);
				getPlayer().sendMessage(new CustomMessage("alt.teleport_protect_gonna", getPlayer()));
			}
		}
	}

	@Override
	public void sendReuseMessage(Skill skill) {
		if (isCastingNow()) {
			return;
		}
		TimeStamp sts = getSkillReuse(skill);
		if (sts == null || !sts.hasNotPassed()) {
			return;
		}
		long timeleft = sts.getReuseCurrent();
		if (!Config.ALT_SHOW_REUSE_MSG && timeleft < 10000 || timeleft < 500) {
			return;
		}
		sendPacket(new SystemMessage(48).addSkillName(skill.getDisplayId(), skill.getDisplayLevel()));
	}

	@Override
	public final int getLevel() {
		return _activeClass == null ? 1 : _activeClass.getLevel();
	}

	public int getSex() {
		return getTemplate().isMale ? 0 : 1;
	}

	public int getFace() {
		return _face;
	}

	public void setFace(int face) {
		_face = face;
	}

	public int getHairColor() {
		return _hairColor;
	}

	public void setHairColor(int hairColor) {
		_hairColor = hairColor;
	}

	public int getHairStyle() {
		return _hairStyle;
	}

	public void setHairStyle(int hairStyle) {
		_hairStyle = hairStyle;
	}

	public void offline() {
		if (_connection != null) {
			_connection.setActiveChar(null);
			_connection.close(ServerClose.STATIC);
			setNetConnection(null);
		}
		if (Config.SERVICES_OFFLINE_TRADE_NAME_COLOR_CHANGE) {
			setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR);
		}
		if (Config.SERVICES_OFFLINE_TRADE_ABNORMAL != AbnormalEffect.NULL) {
			startAbnormalEffect(Config.SERVICES_OFFLINE_TRADE_ABNORMAL);
		}
		setOfflineMode(true);
		setVar("offline", String.valueOf(System.currentTimeMillis() / 1000), -1);
		if (Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0) {
			startKickTask(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK * 1000);
		}
		Party party;
		if ((party = getParty()) != null) {
			if (isFestivalParticipant()) {
				party.broadcastMessageToPartyMembers(getName() + " has been removed from the upcoming festival.");
			}
			leaveParty();
		}
		if (getPet() != null) {
			getPet().unSummon();
		}
		CursedWeaponsManager.getInstance().doLogout(this);
		if (isOlyParticipant()) {
			getOlyParticipant().OnDisconnect(this);
		}
		broadcastCharInfo();
		stopWaterTask();
		stopBonusTask();
		stopHourlyTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		stopQuestTimers();
		try {
			getInventory().store();
		} catch (Throwable e) {
			_log.error("", e);
		}
		try {
			store(false);
		} catch (Throwable e) {
			_log.error("", e);
		}
	}

	public void kick() {
		if (_connection != null) {
			_connection.close(LeaveWorld.STATIC);
			setNetConnection(null);
		}
		prepareToLogout();
		deleteMe();
	}

	public void restart() {
		if (_connection != null) {
			_connection.setActiveChar(null);
			setNetConnection(null);
		}
		prepareToLogout();
		deleteMe();
	}

	public void logout() {
		if (_connection != null) {
			_connection.close(ServerClose.STATIC);
			setNetConnection(null);
		}
		prepareToLogout();
		deleteMe();
	}

	private void prepareToLogout() {
		if (_isLogout.getAndSet(true)) {
			return;
		}
		setNetConnection(null);
		setIsOnline(false);
		getListeners().onExit();
		if (isFlying() && !checkLandingState()) {
			_stablePoint = TeleportUtils.getRestartLocation(this, RestartType.TO_VILLAGE);
		}
		if (isCastingNow()) {
			abortCast(true, true);
		}
		Party party;
		if ((party = getParty()) != null) {
			if (isFestivalParticipant()) {
				party.broadcastMessageToPartyMembers(getName() + " has been removed from the upcoming festival.");
			}
			leaveParty();
		}
		if (Config.OLY_ENABLED && OlyController.getInstance().isCompetitionsActive()) {
			if (isOlyParticipant()) {
				getOlyParticipant().OnDisconnect(this);
			}
			if (ParticipantPool.getInstance().isRegistred(this)) {
				ParticipantPool.getInstance().onLogout(this);
			}
		}
		CursedWeaponsManager.getInstance().doLogout(this);
		if (isOlyObserver()) {
			leaveOlympiadObserverMode();
		}
		if (isInObserverMode()) {
			leaveObserverMode();
		}
		if (Config.MENTORING_ENABLE && getMentoring() != null) {
			getMentoring().onExit(this);
		}
		stopFishing();
		if (_stablePoint != null) {
			teleToLocation(_stablePoint);
		}
		Summon pet;
		if ((pet = getPet()) != null) {
			pet.saveEffects();
			pet.unSummon();
		}
		_friendList.notifyFriends(false);
		if (isProcessingRequest()) {
			getRequest().cancel();
		}
		stopAllTimers();
		if (isInBoat()) {
			getBoat().removePlayer(this);
		}
		SubUnit unit;
		UnitMember member;
		UnitMember unitMember = member = (unit = getSubUnit()) == null ? null : unit.getUnitMember(getObjectId());
		if (member != null) {
			int sponsor = member.getSponsor();
			int apprentice = getApprentice();
			PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(this);
			for (Player clanMember : _clan.getOnlineMembers(getObjectId())) {
				clanMember.sendPacket(memberUpdate);
				if (clanMember.getObjectId() == sponsor) {
					clanMember.sendPacket(new SystemMessage(1757).addString(_name));
					continue;
				}
				if (clanMember.getObjectId() != apprentice)
					continue;
				clanMember.sendPacket(new SystemMessage(1759).addString(_name));
			}
			member.setPlayerInstance(this, true);
		}
		FlagItemAttachment attachment;
		if ((attachment = getActiveWeaponFlagAttachment()) != null) {
			attachment.onLogout(this);
		}
		if (CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()) != null) {
			CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()).setPlayer(null);
		}
		MatchingRoom room;
		if ((room = getMatchingRoom()) != null) {
			if (room.getLeader() == this) {
				room.disband();
			} else {
				room.removeMember(this, false);
			}
		}
		setMatchingRoom(null);
		MatchingRoomManager.getInstance().removeFromWaitingList(this);
		destroyAllTraps();
		stopPvPFlag();
		Reflection ref = getReflection();
		if (ref != ReflectionManager.DEFAULT) {
			if (ref.getReturnLoc() != null) {
				_stablePoint = ref.getReturnLoc();
			}
			ref.removeObject(this);
		}
		try {
			getInventory().store();
			getRefund().clear();
		} catch (Throwable e) {
			_log.error("", e);
		}
		try {
			store(false);
		} catch (Throwable e) {
			_log.error("", e);
		}
	}

	public Collection<Recipe> getDwarvenRecipeBook() {
		return _recipebook.values();
	}

	public Collection<Recipe> getCommonRecipeBook() {
		return _commonrecipebook.values();
	}

	public int recipesCount() {
		return _commonrecipebook.size() + _recipebook.size();
	}

	public boolean hasRecipe(Recipe id) {
		return _recipebook.containsValue(id) || _commonrecipebook.containsValue(id);
	}

	public boolean findRecipe(int id) {
		return _recipebook.containsKey(id) || _commonrecipebook.containsKey(id);
	}

	public void registerRecipe(Recipe recipe, boolean saveDB) {
		if (recipe == null) {
			return;
		}
		switch (recipe.getType()) {
		case ERT_COMMON: {
			_commonrecipebook.put(recipe.getId(), recipe);
			break;
		}
		case ERT_DWARF: {
			_recipebook.put(recipe.getId(), recipe);
			break;
		}
		default: {
			return;
		}
		}
		if (saveDB) {
			mysql.set("REPLACE INTO character_recipebook (char_id, id) VALUES(?,?)", getObjectId(), recipe.getId());
		}
	}

	public void unregisterRecipe(int RecipeID) {
		if (_recipebook.containsKey(RecipeID)) {
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
			_recipebook.remove(RecipeID);
		} else if (_commonrecipebook.containsKey(RecipeID)) {
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
			_commonrecipebook.remove(RecipeID);
		} else {
			_log.warn("Attempted to remove unknown RecipeList" + RecipeID);
		}
	}

	public QuestState getQuestState(Quest quest) {
		return getQuestState(quest.getName());
	}

	public QuestState getQuestState(String quest) {
		questRead.lock();
		try {
			QuestState questState = _quests.get(quest);
			return questState;
		} finally {
			questRead.unlock();
		}
	}

	public QuestState getQuestState(Class<?> quest) {
		return getQuestState(quest.getSimpleName());
	}

	public boolean isQuestCompleted(String quest) {
		QuestState q = getQuestState(quest);
		return q != null && q.isCompleted();
	}

	public boolean isQuestCompleted(Class<?> quest) {
		QuestState q = getQuestState(quest);
		return q != null && q.isCompleted();
	}

	public void setQuestState(QuestState qs) {
		questWrite.lock();
		try {
			_quests.put(qs.getQuest().getName(), qs);
		} finally {
			questWrite.unlock();
		}
	}

	public void removeQuestState(String quest) {
		questWrite.lock();
		try {
			_quests.remove(quest);
		} finally {
			questWrite.unlock();
		}
	}

	public Quest[] getAllActiveQuests() {
		ArrayList<Quest> quests = new ArrayList<>(_quests.size());
		questRead.lock();
		try {
			for (QuestState qs : _quests.values()) {
				if (!qs.isStarted())
					continue;
				quests.add(qs.getQuest());
			}
		} finally {
			questRead.unlock();
		}
		return quests.toArray(new Quest[quests.size()]);
	}

	public QuestState[] getAllQuestsStates() {
		questRead.lock();
		try {
			QuestState[] arrquestState = _quests.values().toArray(new QuestState[_quests.size()]);
			return arrquestState;
		} finally {
			questRead.unlock();
		}
	}

	public List<QuestState> getQuestsForEvent(NpcInstance npc, QuestEventType event) {
		ArrayList<QuestState> states = new ArrayList<>();
		Quest[] quests = npc.getTemplate().getEventQuests(event);
		if (quests != null) {
			for (Quest quest : quests) {
				QuestState qs = getQuestState(quest.getName());
				if (qs == null || qs.isCompleted())
					continue;
				states.add(getQuestState(quest.getName()));
			}
		}
		return states;
	}

	public void processQuestEvent(String quest, String event, NpcInstance npc) {
		if (event == null) {
			event = "";
		}
		QuestState qs;
		if ((qs = getQuestState(quest)) == null) {
			Quest q = QuestManager.getQuest(quest);
			if (q == null) {
				_log.warn("Quest " + quest + " not found!");
				return;
			}
			qs = q.newQuestState(this, 1);
		}
		if (qs == null || qs.isCompleted()) {
			return;
		}
		qs.getQuest().notifyEvent(event, qs, npc);
		sendPacket(new QuestList(this));
	}

	public boolean isQuestContinuationPossible(boolean msg) {
		if (getWeightPenalty() >= 3 || (double) getInventoryLimit() * 0.9 < (double) getInventory().getSize() || (double) Config.QUEST_INVENTORY_MAXIMUM * 0.9 < (double) getInventory().getQuestSize()) {
			if (msg) {
				sendPacket(Msg.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORYS_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
			}
			return false;
		}
		return true;
	}

	public void stopQuestTimers() {
		for (QuestState qs : getAllQuestsStates()) {
			if (qs.isStarted()) {
				qs.pauseQuestTimers();
				continue;
			}
			qs.stopQuestTimers();
		}
	}

	public void resumeQuestTimers() {
		for (QuestState qs : getAllQuestsStates()) {
			qs.resumeQuestTimers();
		}
	}

	public Collection<ShortCut> getAllShortCuts() {
		return _shortCuts.getAllShortCuts();
	}

	public ShortCut getShortCut(int slot, int page) {
		return _shortCuts.getShortCut(slot, page);
	}

	public void registerShortCut(ShortCut shortcut) {
		_shortCuts.registerShortCut(shortcut);
	}

	public void deleteShortCut(int slot, int page) {
		_shortCuts.deleteShortCut(slot, page);
	}

	public void registerMacro(Macro macro) {
		_macroses.registerMacro(macro);
	}

	public void deleteMacro(int id) {
		_macroses.deleteMacro(id);
	}

	public MacroList getMacroses() {
		return _macroses;
	}

	public boolean isCastleLord(int castleId) {
		return _clan != null && isClanLeader() && _clan.getCastle() == castleId;
	}

	public int getPkKills() {
		return _pkKills;
	}

	public void setPkKills(int pkKills) {
		_pkKills = pkKills;
	}

	public long getCreateTime() {
		return _createTime;
	}

	public void setCreateTime(long createTime) {
		_createTime = createTime;
	}

	public int getDeleteTimer() {
		return _deleteTimer;
	}

	public void setDeleteTimer(int deleteTimer) {
		_deleteTimer = deleteTimer;
	}

	public int getCurrentLoad() {
		return getInventory().getTotalWeight();
	}

	public long getLastAccess() {
		return _lastAccess;
	}

	public void setLastAccess(long value) {
		_lastAccess = value;
	}

	public boolean isRecommended(Player target) {
		return _recommendedCharIds.contains(target.getObjectId());
	}

	public int getReceivedRec() {
		return _receivedRec;
	}

	public void setReceivedRec(int value) {
		_receivedRec = value;
	}

	public int getGivableRec() {
		return _givableRec;
	}

	public void setGivableRec(int value) {
		_givableRec = value;
	}

	public void updateRecommends() {
		_recommendedCharIds.clear();
		if (getLevel() >= 40) {
			_givableRec = 9;
			_receivedRec = Math.max(0, _receivedRec - 3);
		} else if (getLevel() >= 20) {
			_givableRec = 6;
			_receivedRec = Math.max(0, _receivedRec - 2);
		} else if (getLevel() >= 10) {
			_givableRec = 3;
			_receivedRec = Math.max(0, _receivedRec - 1);
		} else {
			_givableRec = 0;
			_receivedRec = 0;
		}
	}

	public void restoreGivableAndReceivedRec(int givableRecs, int receivedRecs) {
		_givableRec = givableRecs;
		_receivedRec = receivedRecs;
		Calendar temp = Calendar.getInstance();
		temp.set(11, Config.REC_FLUSH_HOUR);
		temp.set(12, Config.REC_FLUSH_MINUTE);
		temp.set(13, 0);
		temp.set(14, 0);
		long daysElapsed = Math.round((System.currentTimeMillis() / 1000 - getLastAccess()) / 86400);
		if (daysElapsed == 0 && getLastAccess() < temp.getTimeInMillis() / 1000 && System.currentTimeMillis() > temp.getTimeInMillis()) {
			++daysElapsed;
		}
		int i = 1;
		while ((long) i < daysElapsed) {
			updateRecommends();
			++i;
		}
	}

	public void giveRecommendation(Player target) {
		if (target == null) {
			return;
		}
		if (getGivableRec() <= 0 || target.getReceivedRec() >= 255) {
			return;
		}
		if (_recommendedCharIds.contains(target.getObjectId())) {
			return;
		}
		_recommendedCharIds.add(target.getObjectId());
		setGivableRec(getGivableRec() - 1);
		sendUserInfo();
		target.setReceivedRec(target.getReceivedRec() + 1);
		target.broadcastUserInfo(true);
	}

	private void restoreRecommendedCharacters() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		try {
			conn = DatabaseFactory.getInstance().getConnection();
			pstmt = conn.prepareStatement("SELECT `targetId` AS `recommendedObjId` FROM `character_recommends` WHERE `objId` = ?");
			pstmt.setInt(1, getObjectId());
			rset = pstmt.executeQuery();
			_recommendedCharIds.clear();
			while (rset.next()) {
				int recommendedCharId = rset.getInt("recommendedObjId");
				_recommendedCharIds.add(recommendedCharId);
			}
		} catch (SQLException e) {
			_log.error("Can't load recommended characters", e);
		} finally {
			DbUtils.closeQuietly(conn, pstmt, rset);
		}
	}

	private void storeRecommendedCharacters() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DatabaseFactory.getInstance().getConnection();
			pstmt = conn.prepareStatement("DELETE FROM `character_recommends` WHERE `objId` = ?");
			pstmt.setInt(1, getObjectId());
			pstmt.executeUpdate();
			DbUtils.close(pstmt);
			if (!_recommendedCharIds.isEmpty()) {
				pstmt = conn.prepareStatement("INSERT INTO `character_recommends` (`objId`, `targetId`) VALUES (?, ?)");
				for (int charId : _recommendedCharIds.toArray()) {
					pstmt.setInt(1, getObjectId());
					pstmt.setInt(2, charId);
					pstmt.executeUpdate();
				}
			}
		} catch (SQLException e) {
			_log.error("Can't store recommended characters", e);
		} finally {
			DbUtils.closeQuietly(conn, pstmt);
		}
	}

	@Override
	public int getKarma() {
		return _karma;
	}

	public void setKarma(int karma) {
		if (karma < 0) {
			karma = 0;
		}
		if (_karma == karma) {
			return;
		}
		_karma = karma;
		sendChanges();
		if (getPet() != null) {
			getPet().broadcastCharInfo();
		}
	}

	@Override
	public int getMaxLoad() {
		int con = getCON();
		if (con < 1) {
			return (int) (31000.0 * Config.MAXLOAD_MODIFIER);
		}
		if (con > 59) {
			return (int) (176000.0 * Config.MAXLOAD_MODIFIER);
		}
		return (int) calcStat(Stats.MAX_LOAD, Math.pow(1.029993928, con) * 30495.627366 * Config.MAXLOAD_MODIFIER, this, null);
	}

	@Override
	public void updateEffectIcons() {
		if (entering || isLogoutStarted()) {
			return;
		}
		if (Config.USER_INFO_INTERVAL == 0) {
			if (_updateEffectIconsTask != null) {
				_updateEffectIconsTask.cancel(false);
				_updateEffectIconsTask = null;
			}
			updateEffectIconsImpl();
			return;
		}
		if (_updateEffectIconsTask != null) {
			return;
		}
		_updateEffectIconsTask = ThreadPoolManager.getInstance().schedule(new UpdateEffectIcons(), Config.USER_INFO_INTERVAL);
	}

	private void updateEffectIconsImpl() {
		Effect[] effects = getEffectList().getAllFirstEffects();
		PartySpelled ps = new PartySpelled(this, false);
		AbnormalStatusUpdate mi = new AbnormalStatusUpdate();
		for (Effect.EEffectSlot ees : Effect.EEffectSlot.VALUES) {
			for (Effect eff : effects) {
				if (!eff.isInUse() || eff.getEffectSlot() != ees)
					continue;
				if (eff.isStackTypeMatch("HpRecoverCast")) {
					sendPacket(new ShortBuffStatusUpdate(eff));
				} else {
					eff.addIcon(mi);
				}
				if (_party == null)
					continue;
				eff.addPartySpelledIcon(ps);
			}
		}
		sendPacket(mi);
		if (_party != null) {
			_party.broadCast(ps);
		}
		if (isOlyParticipant()) {
			getOlyParticipant().getCompetition().broadcastEffectIcons(this, effects);
		}
	}

	public int getWeightPenalty() {
		return getSkillLevel(4270, 0);
	}

	public void refreshOverloaded() {
		if (isLogoutStarted() || getMaxLoad() <= 0) {
			return;
		}
		setOverloaded(getCurrentLoad() > getMaxLoad());
		double weightproc = 100.0 * ((double) getCurrentLoad() - calcStat(Stats.MAX_NO_PENALTY_LOAD, 0.0, this, null)) / (double) getMaxLoad();
		int newWeightPenalty = weightproc < 50.0 ? 0 : weightproc < 66.6 ? 1 : weightproc < 80.0 ? 2 : weightproc < 100.0 ? 3 : 4;
		int current = getWeightPenalty();
		if (current == newWeightPenalty) {
			return;
		}
		if (newWeightPenalty > 0) {
			addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
		} else {
			removeSkill(getKnownSkill(4270));
		}
		sendPacket(new SkillList(this));
		sendEtcStatusUpdate();
		updateStats();
	}

	public int getGradePenalty() {
		return getSkillLevel(4267, 0);
	}

	public int getExpertisePenalty(ItemInstance item) {
		if (item.getTemplate().getType2() == 0 || item.getTemplate().getType2() == 1 || item.getTemplate().getType2() == 2) {
			return getGradePenalty();
		}
		return 0;
	}

	public void refreshExpertisePenalty() {
		if (isLogoutStarted()) {
			return;
		}
		int level = (int) calcStat(Stats.GRADE_EXPERTISE_LEVEL, getLevel(), null, null);
		int i = 0;
		for (i = 0; i < EXPERTISE_LEVELS.length && level >= EXPERTISE_LEVELS[i + 1]; ++i) {
		}
		boolean skillUpdate = false;
		if (expertiseIndex != i) {
			expertiseIndex = i;
			if (expertiseIndex > 0) {
				addSkill(SkillTable.getInstance().getInfo(239, expertiseIndex), false);
				skillUpdate = true;
			}
		}
		ItemInstance[] items = getInventory().getPaperdollItems();
		int newGradePenalty = 0;
		for (ItemInstance item : items) {
			if (item == null)
				continue;
			int crystaltype = item.getTemplate().getCrystalType().ordinal();
			if (item.getTemplate().getType2() != 0 && item.getTemplate().getType2() != 1 && item.getTemplate().getType2() != 2 || crystaltype <= newGradePenalty)
				continue;
			newGradePenalty = crystaltype;
		}
		if ((newGradePenalty -= expertiseIndex) <= 0) {
			newGradePenalty = 0;
		} else if (newGradePenalty >= 4) {
			newGradePenalty = 4;
		}
		int PenaltyExpertise = getGradePenalty();
		if (PenaltyExpertise != newGradePenalty) {
			PenaltyExpertise = newGradePenalty;
			if (newGradePenalty > 0) {
				addSkill(SkillTable.getInstance().getInfo(4267, PenaltyExpertise));
			} else {
				removeSkill(getKnownSkill(4267));
			}
			skillUpdate = true;
		}
		if (skillUpdate) {
			getInventory().validateItemsSkills();
			sendPacket(new SkillList(this));
			sendEtcStatusUpdate();
			updateStats();
		}
	}

	public int getPvpKills() {
		return _pvpKills;
	}

	public void setPvpKills(int pvpKills) {
		_pvpKills = pvpKills;
	}

	public ClassId getClassId() {
		return getTemplate().classId;
	}

	public void addClanPointsOnProfession(int id) {
		if (getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5 && ClassId.VALUES[id].getLevel() == 2) {
			_clan.incReputation(100, true, "Academy");
		} else if (getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5 && ClassId.VALUES[id].getLevel() == 3) {
			int earnedPoints = getLvlJoinedAcademy() > 39 ? 160 : getLvlJoinedAcademy() > 16 ? 400 - (getLvlJoinedAcademy() - 16) * 10 : 400;
			_clan.removeClanMember(getObjectId());
			SystemMessage sm = new SystemMessage(1748);
			sm.addString(getName());
			sm.addNumber(_clan.incReputation(earnedPoints, true, "Academy"));
			_clan.broadcastToOnlineMembers(sm);
			_clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListDelete(getName()), this);
			setClan(null);
			setTitle("");
			sendPacket(new SystemMessage(SystemMessage.CONGRATULATIONS_YOU_WILL_NOW_GRADUATE_FROM_THE_CLAN_ACADEMY_AND_LEAVE_YOUR_CURRENT_CLAN_AS_A_GRADUATE_OF_THE_ACADEMY_YOU_CAN_IMMEDIATELY_JOIN_A_CLAN_AS_A_REGULAR_MEMBER_WITHOUT_BEING_SUBJECT_TO_ANY_PENALTIES));
			setLeaveClanTime(0);
			broadcastCharInfo();
			sendPacket(PledgeShowMemberListDeleteAll.STATIC);
			ItemFunctions.addItem(this, 8181, 1, true);
		}
	}

	public synchronized void setClassId(int id, boolean noban, boolean fromQuest) {
		if (!(noban || ClassId.VALUES[id].equalsOrChildOf(ClassId.VALUES[getActiveClassId()]) || getPlayerAccess().CanChangeClass || Config.EVERYBODY_HAS_ADMIN_RIGHTS)) {
			Thread.dumpStack();
			return;
		}
		boolean isNewSub = !getSubClasses().containsKey(id);
		if (isNewSub) {
			SubClass cclass = getActiveClass();
			getSubClasses().remove(getActiveClassId());
			changeClassInDb(cclass.getClassId(), id);
			if (cclass.isBase()) {
				setBaseClass(id);
				addClanPointsOnProfession(id);
				ItemInstance coupons = null;
				if (ClassId.VALUES[id].getLevel() == 2) {
					if (fromQuest && Config.ALT_ALLOW_SHADOW_WEAPONS) {
						coupons = ItemFunctions.createItem(8869);
					}
					unsetVar("newbieweapon");
					unsetVar("p1q2");
					unsetVar("p1q3");
					unsetVar("p1q4");
					unsetVar("prof1");
					unsetVar("ng1");
					unsetVar("ng2");
					unsetVar("ng3");
					unsetVar("ng4");
				} else if (ClassId.VALUES[id].getLevel() == 3) {
					if (fromQuest && Config.ALT_ALLOW_SHADOW_WEAPONS) {
						coupons = ItemFunctions.createItem(8870);
					}
					unsetVar("newbiearmor");
					unsetVar("dd1");
					unsetVar("dd2");
					unsetVar("dd3");
					unsetVar("prof2.1");
					unsetVar("prof2.2");
					unsetVar("prof2.3");
				}
				if (coupons != null) {
					coupons.setCount(15);
					sendPacket(SystemMessage2.obtainItems(coupons));
					getInventory().addItem(coupons);
				}
			}
			cclass.setClassId(id);
			getSubClasses().put(id, cclass);
			rewardSkills(true);
			storeCharSubClasses();
			if (fromQuest) {
				broadcastPacket(new SocialAction(getObjectId(), 16));
				sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
			}
			broadcastCharInfo();
		}
		PlayerTemplate t;
		if ((t = CharTemplateTable.getInstance().getTemplate(id, getSex() == 1)) == null) {
			_log.error("Missing template for classId: " + id);
			return;
		}
		_template = t;
		if (isInParty()) {
			getParty().broadCast(new PartySmallWindowUpdate(this));
		}
		if (getClan() != null) {
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
		}
		if (_matchingRoom != null) {
			_matchingRoom.broadcastPlayerUpdate(this);
		}
		if (Config.MENTORING_ENABLE && getMentoring() != null && getMentoring().isMentee(this)) {
			getMentoring().makeReward(this, isNewSub);
		}
		sendPacket(new SkillList(this));
	}

	public long getExp() {
		return _activeClass == null ? 0 : _activeClass.getExp();
	}

	public long getMaxExp() {
		return _activeClass == null ? Experience.LEVEL[Experience.getMaxLevel() + 1] : _activeClass.getMaxExp();
	}

	public ItemInstance getEnchantScroll() {
		return _enchantScroll;
	}

	public void setEnchantScroll(ItemInstance scroll) {
		_enchantScroll = scroll;
	}

	public IRefineryHandler getRefineryHandler() {
		return _refineryHandler;
	}

	public void setRefineryHandler(IRefineryHandler refineryHandler) {
		_refineryHandler = refineryHandler;
	}

	public WeaponTemplate getFistsWeaponItem() {
		return _fistsWeaponItem;
	}

	public void setFistsWeaponItem(WeaponTemplate weaponItem) {
		_fistsWeaponItem = weaponItem;
	}

	public WeaponTemplate findFistsWeaponItem(int classId) {
		if (classId >= 0 && classId <= 9) {
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(246);
		}
		if (classId >= 10 && classId <= 17) {
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(251);
		}
		if (classId >= 18 && classId <= 24) {
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(244);
		}
		if (classId >= 25 && classId <= 30) {
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(249);
		}
		if (classId >= 31 && classId <= 37) {
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(245);
		}
		if (classId >= 38 && classId <= 43) {
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(250);
		}
		if (classId >= 44 && classId <= 48) {
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(248);
		}
		if (classId >= 49 && classId <= 52) {
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(252);
		}
		if (classId >= 53 && classId <= 57) {
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(247);
		}
		return null;
	}

	public void addExpAndCheckBonus(MonsterInstance mob, double noRateExp, double noRateSp) {
		if (_activeClass == null) {
			return;
		}
		double neededExp = calcStat(Stats.SOULS_CONSUME_EXP, 0.0, mob, null);
		if (neededExp > 0.0 && noRateExp > neededExp) {
			mob.broadcastPacket(new SpawnEmitter(mob, this));
			ThreadPoolManager.getInstance().schedule(new GameObjectTasks.SoulConsumeTask(this), 1000);
		}
		double expRate = Config.RATE_XP;
		double spRate = Config.RATE_SP;
		if (mob.isRaid()) {
			expRate = Config.RATE_RAIDBOSS_XP;
			spRate = Config.RATE_RAIDBOSS_SP;
		}
		long normalExp = (long) (noRateExp * (expRate * getRateExp()));
		long normalSp = (long) (noRateSp * (spRate * getRateSp()));
		addExpAndSp(normalExp, normalSp, false, true);
	}

	@Override
	public void addExpAndSp(long exp, long sp) {
		addExpAndSp(exp, sp, false, false);
	}

	public void addExpAndSp(long addToExp, long addToSp, boolean applyRate, boolean applyToPet) {
		if (_activeClass == null) {
			return;
		}
		if (applyRate) {
			addToExp = (long) ((double) addToExp * (Config.RATE_XP * getRateExp()));
			addToSp = (long) ((double) addToSp * (Config.RATE_SP * getRateSp()));
		}
		Summon pet = getPet();
		boolean updatePetInfo = false;
		if (addToExp > 0) {
			if (!isCursedWeaponEquipped() && addToSp > 0 && _karma > 0) {
				_karma = (int) ((double) _karma - (double) addToSp / ((double) Config.KARMA_SP_DIVIDER * Config.RATE_SP));
				updatePetInfo = true;
			}
			if (_karma < 0) {
				_karma = 0;
			}
			if (applyToPet && pet != null && !pet.isDead()) {
				if (pet.getNpcId() == 12564) {
					pet.addExpAndSp(addToExp, 0);
					addToExp = 0;
				} else if (pet.isPet() && pet.getExpPenalty() > 0.0) {
					if (pet.getLevel() > getLevel() - 20 && pet.getLevel() < getLevel() + 5) {
						pet.addExpAndSp((long) ((double) addToExp * pet.getExpPenalty()), 0);
						addToExp = (long) ((double) addToExp * (1.0 - pet.getExpPenalty()));
					} else {
						pet.addExpAndSp((long) ((double) addToExp * pet.getExpPenalty() / 5.0), 0);
						addToExp = (long) ((double) addToExp * (1.0 - pet.getExpPenalty() / 5.0));
					}
				} else if (pet.isSummon()) {
					addToExp = (long) ((double) addToExp * (1.0 - pet.getExpPenalty()));
				}
			}
			long max_xp = getVarB("NoExp") ? Experience.LEVEL[getLevel() + 1] - 1 : getMaxExp();
			addToExp = Math.min(addToExp, max_xp - getExp());
		}
		int oldLvl = _activeClass.getLevel();
		_activeClass.addExp(addToExp);
		_activeClass.addSp(addToSp);
		if (addToSp > 0 && addToExp == 0) {
			sendPacket(new SystemMessage(331).addNumber(addToSp));
		} else if (addToSp > 0 && addToExp > 0) {
			sendPacket(new SystemMessage(95).addNumber(addToExp).addNumber(addToSp));
		} else if (addToSp == 0 && addToExp > 0) {
			sendPacket(new SystemMessage(45).addNumber(addToExp));
		}
		int level = _activeClass.getLevel();
		if (level != oldLvl) {
			int levels = level - oldLvl;
			levelSet(levels);
		}
		if (Config.MENTORING_ENABLE && level > oldLvl && getMentoring() != null && getMentoring().isMentee(this)) {
			getMentoring().makeReward(this, false);
		}
		updateStats();
		if (pet != null && updatePetInfo) {
			pet.broadcastCharInfo();
		}
		getListeners().onGainExpSp(addToExp, addToSp);
	}

	private void rewardSkills(boolean send) {
		boolean update = false;
		if (Config.AUTO_LEARN_SKILLS) {
			int unLearnable = 0;
			Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, ClassId.VALUES[getActiveClassId()], AcquireType.NORMAL, null);
			while (skills.size() > unLearnable) {
				unLearnable = 0;
				for (SkillLearn s : skills) {
					Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
					if (sk == null || !sk.getCanLearn(getClassId()) || !Config.AUTO_LEARN_FORGOTTEN_SKILLS && s.isClicked()) {
						++unLearnable;
						continue;
					}
					addSkill(sk, true);
				}
				skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
			}
			update = true;
		} else {
			for (SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL)) {
				if (skill.getCost() != 0 || skill.getItemId() != 0)
					continue;
				Skill sk = SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel());
				addSkill(sk, true);
				if (getAllShortCuts().size() > 0 && sk.getLevel() > 1) {
					for (ShortCut sc : getAllShortCuts()) {
						if (sc.getId() != sk.getId() || sc.getType() != 2)
							continue;
						ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), sk.getLevel(), 1);
						sendPacket(new ShortCutRegister(this, newsc));
						registerShortCut(newsc);
					}
				}
				update = true;
			}
		}
		if (send && update) {
			sendPacket(new SkillList(this));
		}
		updateStats();
	}

	public Race getRace() {
		return getBaseTemplate().race;
	}

	public int getIntSp() {
		return (int) getSp();
	}

	public long getSp() {
		return _activeClass == null ? 0 : _activeClass.getSp();
	}

	public void setSp(long sp) {
		if (_activeClass != null) {
			_activeClass.setSp(sp);
		}
	}

	public int getClanId() {
		return _clan == null ? 0 : _clan.getClanId();
	}

	public long getLeaveClanTime() {
		return _leaveClanTime;
	}

	public void setLeaveClanTime(long time) {
		_leaveClanTime = time;
	}

	public long getDeleteClanTime() {
		return _deleteClanTime;
	}

	public void setDeleteClanTime(long time) {
		_deleteClanTime = time;
	}

	public long getOnlineBeginTime() {
		return _onlineBeginTime;
	}

	public long getOnlineTime() {
		return _onlineTime;
	}

	public void setOnlineTime(long time) {
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}

	public long getNoChannel() {
		return _NoChannel;
	}

	public void setNoChannel(long time) {
		_NoChannel = time;
		if (_NoChannel > 2145909600000L || _NoChannel < 0) {
			_NoChannel = -1;
		}
		_NoChannelBegin = _NoChannel > 0 ? System.currentTimeMillis() : 0;
	}

	public long getNoChannelRemained() {
		if (_NoChannel == 0) {
			return 0;
		}
		if (_NoChannel < 0) {
			return -1;
		}
		long remained = _NoChannel - System.currentTimeMillis() + _NoChannelBegin;
		if (remained < 0) {
			return 0;
		}
		return remained;
	}

	public void setLeaveClanCurTime() {
		_leaveClanTime = System.currentTimeMillis();
	}

	public void setDeleteClanCurTime() {
		_deleteClanTime = System.currentTimeMillis();
	}

	public boolean canJoinClan() {
		if (_leaveClanTime == 0) {
			return true;
		}
		if (System.currentTimeMillis() - _leaveClanTime >= Config.CLAN_LEAVE_TIME_PERNALTY) {
			_leaveClanTime = 0;
			return true;
		}
		return false;
	}

	public boolean canCreateClan() {
		if (_deleteClanTime == 0) {
			return true;
		}
		if (System.currentTimeMillis() - _deleteClanTime >= 864000000) {
			_deleteClanTime = 0;
			return true;
		}
		return false;
	}

	public IStaticPacket canJoinParty(Player inviter) {
		Request request = getRequest();
		if (request != null && request.isInProgress() && request.getOtherPlayer(this) != inviter) {
			return SystemMsg.WAITING_FOR_ANOTHER_REPLY.packet(inviter);
		}
		if (isBlockAll() || getMessageRefusal()) {
			return SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE.packet(inviter);
		}
		if (isInParty()) {
			return new SystemMessage2(SystemMsg.C1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED).addName(this);
		}
		if (inviter.getReflection() != getReflection() && inviter.getReflection() != ReflectionManager.DEFAULT && getReflection() != ReflectionManager.DEFAULT) {
			return SystemMsg.INVALID_TARGET.packet(inviter);
		}
		if (isCursedWeaponEquipped() || inviter.isCursedWeaponEquipped()) {
			return SystemMsg.INVALID_TARGET.packet(inviter);
		}
		if (inviter.isOlyParticipant() || isOlyParticipant()) {
			return SystemMsg.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS.packet(inviter);
		}
		if (!inviter.getPlayerAccess().CanJoinParty || !getPlayerAccess().CanJoinParty) {
			return SystemMsg.INVALID_TARGET.packet(inviter);
		}
		if (getTeam() != TeamType.NONE) {
			return SystemMsg.INVALID_TARGET.packet(inviter);
		}
		return null;
	}

	@Override
	public PcInventory getInventory() {
		return _inventory;
	}

	@Override
	public long getWearedMask() {
		return _inventory.getWearedMask();
	}

	public PcFreight getFreight() {
		return _freight;
	}

	public void removeItemFromShortCut(int objectId) {
		_shortCuts.deleteShortCutByObjectId(objectId);
	}

	public void removeSkillFromShortCut(int skillId) {
		_shortCuts.deleteShortCutBySkillId(skillId);
	}

	public boolean isSitting() {
		return _isSitting;
	}

	public void setSitting(boolean val) {
		_isSitting = val;
	}

	public boolean getSittingTask() {
		return sittingTaskLaunched;
	}

	@Override
	public void sitDown(StaticObjectInstance throne) {
		if (isSitting() || sittingTaskLaunched || isAlikeDead()) {
			return;
		}
		if (isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isMoving()) {
			getAI().setNextAction(NextAction.REST, null, null, false, false);
			return;
		}
		resetWaitSitTime();
		getAI().setIntention(CtrlIntention.AI_INTENTION_REST, null, null);
		if (throne == null) {
			broadcastPacket(new ChangeWaitType(this, 0));
		} else {
			broadcastPacket(new ChairSit(this, throne));
		}
		_sittingObject = throne;
		setSitting(true);
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.EndSitDownTask(this), 2500);
	}

	@Override
	public void standUp() {
		if (!isSitting() || sittingTaskLaunched || isInStoreMode() || isAlikeDead()) {
			return;
		}
		getAI().clearNextAction();
		broadcastPacket(new ChangeWaitType(this, 1));
		_sittingObject = null;
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.EndStandUpTask(this), 2500);
	}

	@Override
	protected Creature.MoveActionBase createMoveToLocation(Location dest, int indent, boolean pathFind) {
		boolean ignoreGeo = !Config.ALLOW_GEODATA;
		Location from = getLoc();
		Location to = dest.clone();
		if (isInBoat()) {
			indent = (int) ((double) indent + (from.distance(to) - (double) (3 * getBoat().getActingRange())));
			ignoreGeo = true;
		}
		if (Config.MOVE_OFFLOAD_MTL_PC) {
			return new MoveToLocationActionForOffload(this, from, to, ignoreGeo, indent, pathFind);
		}
		return new Creature.MoveToLocationAction(this, from, to, ignoreGeo, indent, pathFind);
	}

	public void moveBackwardToLocationForPacket(Location loc, boolean pathfinding) {
		if (isMoving() && Config.MOVE_OFFLOAD_MTL_PC) {
			_mtlOffloadData.set(new MoveToLocationOffloadData(loc, 0, pathfinding));
			return;
		}
		moveToLocation(loc, 0, pathfinding);
	}

	public void updateWaitSitTime() {
		if (_waitTimeWhenSit < 200) {
			_waitTimeWhenSit += 2;
		}
	}

	public int getWaitSitTime() {
		return _waitTimeWhenSit;
	}

	public void resetWaitSitTime() {
		_waitTimeWhenSit = 0;
	}

	public Warehouse getWarehouse() {
		return _warehouse;
	}

	public ItemContainer getRefund() {
		return _refund;
	}

	public long getAdena() {
		return getInventory().getAdena();
	}

	public boolean reduceAdena(long adena) {
		return reduceAdena(adena, false);
	}

	public boolean reduceAdena(long adena, boolean notify) {
		if (adena < 0) {
			return false;
		}
		if (adena == 0) {
			return true;
		}
		boolean result = getInventory().reduceAdena(adena);
		if (notify && result) {
			sendPacket(SystemMessage2.removeItems(57, adena));
		}
		return result;
	}

	public ItemInstance addAdena(long adena) {
		return addAdena(adena, false);
	}

	public ItemInstance addAdena(long adena, boolean notify) {
		if (adena < 1) {
			return null;
		}
		ItemInstance item = getInventory().addAdena(adena);
		if (item != null && notify) {
			sendPacket(SystemMessage2.obtainItems(57, adena, 0));
		}
		return item;
	}

	public GameClient getNetConnection() {
		return _connection;
	}

	public void setNetConnection(GameClient connection) {
		_connection = connection;
	}

	public int getRevision() {
		return _connection == null ? 0 : _connection.getRevision();
	}

	public boolean isConnected() {
		return _connection != null && _connection.isConnected();
	}

	@Override
	public void onAction(Player player, boolean shift) {
		if (isFrozen()) {
			player.sendActionFailed();
			return;
		}
		if (Events.onAction(player, this, shift)) {
			player.sendActionFailed();
			return;
		}
		if (player.getTarget() != this) {
			player.setTarget(this);
			if (player.getTarget() == this) {
				player.sendPacket(new MyTargetSelected(getObjectId(), 0));
				player.sendPacket(makeStatusUpdate(9, 10, 11, 12));
			} else {
				player.sendActionFailed();
			}
		} else if (getPrivateStoreType() != 0) {
			if (getRealDistance(player) > (double) getActingRange() && player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT) {
				if (!shift) {
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				} else {
					player.sendActionFailed();
				}
			} else {
				player.doInteract(this);
			}
		} else if (isAutoAttackable(player)) {
			player.getAI().Attack(this, false, shift);
		} else if (player != this) {
			if (player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW) {
				if (!shift) {
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				} else {
					player.sendActionFailed();
				}
			} else {
				player.sendActionFailed();
			}
		} else {
			player.sendActionFailed();
		}
	}

	@Override
	public void broadcastStatusUpdate() {
		if (!needStatusUpdate()) {
			return;
		}
		StatusUpdate su = makeStatusUpdate(10, 12, 34, 9, 11, 33);
		sendPacket(su);
		if (isInParty()) {
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
		}
		DuelEvent duelEvent;
		if ((duelEvent = getEvent(DuelEvent.class)) != null) {
			duelEvent.sendPacket(new ExDuelUpdateUserInfo(this), getTeam().revert().name());
		}
		if (isOlyCompetitionStarted()) {
			broadcastPacket(new ExOlympiadUserInfo(this));
		}
	}

	@Override
	public void broadcastCharInfo() {
		broadcastUserInfo(false);
	}

	public void broadcastUserInfo(boolean force) {
		sendUserInfo(force);
		if (!isVisible() || isInvisible()) {
			return;
		}
		if (Config.BROADCAST_CHAR_INFO_INTERVAL == 0) {
			force = true;
		}
		if (force) {
			if (_broadcastCharInfoTask != null) {
				_broadcastCharInfoTask.cancel(false);
				_broadcastCharInfoTask = null;
			}
			broadcastCharInfoImpl();
			return;
		}
		if (_broadcastCharInfoTask != null) {
			return;
		}
		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	public boolean isPolymorphed() {
		return _polyNpcId != 0;
	}

	public int getPolyId() {
		return _polyNpcId;
	}

	public void setPolyId(int polyid) {
		_polyNpcId = polyid;
		teleToLocation(getLoc());
		broadcastUserInfo(true);
	}

	private void broadcastCharInfoImpl() {
		if (!isVisible() || isInvisible()) {
			return;
		}
		L2GameServerPacket ci = isPolymorphed() ? new NpcInfoPoly(this) : new CharInfo(this);
		for (Player player : World.getAroundPlayers(this)) {
			player.sendPacket(ci);
			player.sendPacket(RelationChanged.create(player, this, player));
		}
	}

	public void setLastNpcInteractionTime() {
		_lastNpcInteractionTime = System.currentTimeMillis();
	}

	public boolean canMoveAfterInteraction() {
		return _lastNpcInteractionTime + 1000 < System.currentTimeMillis();
	}

	public void broadcastRelationChanged() {
		if (!isVisible() || isInvisible()) {
			return;
		}
		for (Player player : World.getAroundPlayers(this)) {
			player.sendPacket(RelationChanged.create(player, this, player));
		}
	}

	public void sendEtcStatusUpdate() {
		if (!isVisible()) {
			return;
		}
		sendPacket(new EtcStatusUpdate(this));
	}

	private void sendUserInfoImpl() {
		sendPacket(new UserInfo(this));
	}

	public void sendUserInfo() {
		sendUserInfo(false);
	}

	public void sendUserInfo(boolean force) {
		if (!isVisible() || entering || isLogoutStarted()) {
			return;
		}
		if (Config.USER_INFO_INTERVAL == 0 || force) {
			if (_userInfoTask != null) {
				_userInfoTask.cancel(false);
				_userInfoTask = null;
			}
			sendUserInfoImpl();
			return;
		}
		if (_userInfoTask != null) {
			return;
		}
		_userInfoTask = ThreadPoolManager.getInstance().schedule(new UserInfoTask(), Config.USER_INFO_INTERVAL);
	}

	@Override
	public StatusUpdate makeStatusUpdate(int... fields) {
		StatusUpdate su = new StatusUpdate(this);
		block12:
		for (int field : fields) {
			switch (field) {
			case 9: {
				su.addAttribute(field, (int) getCurrentHp());
				continue block12;
			}
			case 10: {
				su.addAttribute(field, getMaxHp());
				continue block12;
			}
			case 11: {
				su.addAttribute(field, (int) getCurrentMp());
				continue block12;
			}
			case 12: {
				su.addAttribute(field, getMaxMp());
				continue block12;
			}
			case 14: {
				su.addAttribute(field, getCurrentLoad());
				continue block12;
			}
			case 15: {
				su.addAttribute(field, getMaxLoad());
				continue block12;
			}
			case 26: {
				su.addAttribute(field, _pvpFlag);
				continue block12;
			}
			case 27: {
				su.addAttribute(field, getKarma());
				continue block12;
			}
			case 33: {
				su.addAttribute(field, (int) getCurrentCp());
				continue block12;
			}
			case 34: {
				su.addAttribute(field, getMaxCp());
			}
			}
		}
		return su;
	}

	public void sendStatusUpdate(boolean broadCast, boolean withPet, int... fields) {
		if (fields.length == 0 || entering && !broadCast) {
			return;
		}
		StatusUpdate su = makeStatusUpdate(fields);
		if (!su.hasAttributes()) {
			return;
		}
		ArrayList<L2GameServerPacket> packets = new ArrayList<>(withPet ? 2 : 1);
		if (withPet && getPet() != null) {
			packets.add(getPet().makeStatusUpdate(fields));
		}
		packets.add(su);
		if (!broadCast) {
			sendPacket(packets);
		} else if (entering) {
			broadcastPacketToOthers(packets);
		} else {
			broadcastPacket(packets);
		}
	}

	public int getAllyId() {
		return _clan == null ? 0 : _clan.getAllyId();
	}

	@Override
	public void sendPacket(IStaticPacket p) {
		if (!isConnected()) {
			return;
		}
		if (isPacketIgnored(p.packet(this))) {
			return;
		}
		_connection.sendPacket(p.packet(this));
	}

	@Override
	public void sendPacket(IStaticPacket... packets) {
		if (!isConnected()) {
			return;
		}
		for (IStaticPacket p : packets) {
			if (isPacketIgnored(p))
				continue;
			_connection.sendPacket(p.packet(this));
		}
	}

	private boolean isPacketIgnored(IStaticPacket p) {
		return p == null;
	}

	@Override
	public void sendPacket(List<? extends IStaticPacket> packets) {
		if (!isConnected()) {
			return;
		}
		for (IStaticPacket p : packets) {
			_connection.sendPacket(p.packet(this));
		}
	}

	public void doInteract(GameObject target) {
		if (target == null || isActionsDisabled()) {
			sendActionFailed();
			return;
		}
		if (target.isPlayer()) {
			Player temp = (Player) target;
			if (getRealDistance(target) <= (double) target.getActingRange()) {
				if (temp.getPrivateStoreType() == 1 || temp.getPrivateStoreType() == 8) {
					sendPacket(new PrivateStoreListSell(this, temp));
					sendActionFailed();
				} else if (temp.getPrivateStoreType() == 3) {
					sendPacket(new PrivateStoreListBuy(this, temp));
					sendActionFailed();
				} else if (temp.getPrivateStoreType() == 5) {
					sendPacket(new RecipeShopSellList(this, temp));
					sendActionFailed();
				}
				sendActionFailed();
			} else if (!getAI().isIntendingInteract(temp)) {
				getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, temp);
			}
		} else {
			target.onAction(this, false);
		}
	}

	public void doAutoLootOrDrop(ItemInstance item, NpcInstance fromNpc) {

		boolean forceAutoloot = fromNpc.isFlying() || getReflection().isAutolootForced();
		if (!(!fromNpc.isRaid() && !(fromNpc instanceof ReflectionBossInstance) || Config.AUTO_LOOT_FROM_RAIDS || item.isHerb() || forceAutoloot)) {
			item.dropToTheGround(this, fromNpc);
			return;
		}
		if (item.isHerb()) {
			if (!AutoLootHerbs && !forceAutoloot) {
				item.dropToTheGround(this, fromNpc);
				return;
			}
			Skill[] skills = item.getTemplate().getAttachedSkills();
			if (skills.length > 0) {
				for (Skill skill : skills) {
					altUseSkill(skill, this);
					if (getPet() == null || !getPet().isSummon() || getPet().isDead())
						continue;
					getPet().altUseSkill(skill, getPet());
				}
			}
			item.deleteMe();
			return;
		}
		if (forceAutoloot || _autoLoot || item.getItemId() == 57 && AutoLootAdena) {
			if (!isInParty()) {
				if (!pickupItem(item, Log.ItemLog.Pickup)) {
					item.dropToTheGround(this, fromNpc);
					return;
				}
			} else {
				getParty().distributeItem(this, item, fromNpc);
			}
			broadcastPickUpMsg(item);
			return;
		}
		item.dropToTheGround(this, fromNpc);
	}

	@Override
	public void doPickupItem(GameObject object) {
		if (!object.isItem()) {
			_log.warn("trying to pickup wrong target." + getTarget());
			return;
		}
		sendActionFailed();
		stopMove();
		ItemInstance item;
		ItemInstance itemInstance = item = (ItemInstance) object;
		synchronized (itemInstance) {
			if (!item.isVisible()) {
				return;
			}
			if (!ItemFunctions.checkIfCanPickup(this, item)) {
				SystemMessage sm;
				if (item.getItemId() == 57) {
					sm = new SystemMessage(55);
					sm.addNumber(item.getCount());
				} else {
					sm = new SystemMessage(56);
					sm.addItemName(item.getItemId());
				}
				sendPacket(sm);
				return;
			}
			if (item.isHerb()) {
				Skill[] skills = item.getTemplate().getAttachedSkills();
				if (skills.length > 0) {
					for (Skill skill : skills) {
						altUseSkill(skill, this);
					}
				}
				broadcastPacket(new GetItem(item, getObjectId()));
				item.deleteMe();
				return;
			}

			FlagItemAttachment attachment = item.getAttachment() instanceof FlagItemAttachment ? (FlagItemAttachment) item.getAttachment() : null;
			if (!isInParty() || attachment != null) {
				if (pickupItem(item, Log.ItemLog.Pickup)) {
					broadcastPacket(new GetItem(item, getObjectId()));
					broadcastPickUpMsg(item);
					item.pickupMe();
				}
			} else {
				getParty().distributeItem(this, item, null);
			}
		}
	}

	public boolean pickupItem(ItemInstance item, Log.ItemLog log) {
		PickableAttachment attachment = item.getAttachment() instanceof PickableAttachment ? (PickableAttachment) item.getAttachment() : null;
		if (!ItemFunctions.canAddItem(this, item)) {
			return false;
		}
		Quest q;
		if ((item.getItemId() == 57 || item.getItemId() == 6353) && (q = QuestManager.getQuest(255)) != null) {
			processQuestEvent(q.getName(), "CE" + item.getItemId(), null);
		}
		Log.LogItem(this, log, item);
		sendPacket(SystemMessage2.obtainItems(item));
		getInventory().addItem(item);
		if (attachment != null) {
			attachment.pickUp(this);
		}
		sendChanges();
		return true;
	}

	public void setObjectTarget(GameObject target) {
		setTarget(target);
		if (target == null) {
			return;
		}
		if (target == getTarget()) {
			if (target.isNpc()) {
				NpcInstance npc = (NpcInstance) target;
				sendPacket(new MyTargetSelected(npc.getObjectId(), getLevel() - npc.getLevel()));
				sendPacket(npc.makeStatusUpdate(9, 10));
				sendPacket(new ValidateLocation(npc), ActionFail.STATIC);
			} else {
				sendPacket(new MyTargetSelected(target.getObjectId(), 0));
			}
		}
	}

	@Override
	public void setTarget(GameObject newTarget) {
		if (newTarget != null && !newTarget.isVisible()) {
			newTarget = null;
		}
		if (newTarget instanceof FestivalMonsterInstance && !isFestivalParticipant()) {
			newTarget = null;
		}
		Party party;
		if ((party = getParty()) != null && party.isInDimensionalRift()) {
			int riftType = party.getDimensionalRift().getType();
			int riftRoom = party.getDimensionalRift().getCurrentRoom();
			if (newTarget != null && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getX(), newTarget.getY(), newTarget.getZ())) {
				newTarget = null;
			}
		}
		GameObject oldTarget;
		if ((oldTarget = getTarget()) != null) {
			if (oldTarget.equals(newTarget)) {
				return;
			}
			if (oldTarget.isCreature()) {
				((Creature) oldTarget).removeStatusListener(this);
			}
			broadcastPacket(new TargetUnselected(this));
		}
		if (newTarget != null) {
			if (newTarget.isCreature()) {
				((Creature) newTarget).addStatusListener(this);
			}
			broadcastPacket(new TargetSelected(getObjectId(), newTarget.getObjectId(), getLoc()));
		}
		super.setTarget(newTarget);
	}

	@Override
	public ItemInstance getActiveWeaponInstance() {
		return getInventory().getPaperdollItem(7);
	}

	@Override
	public WeaponTemplate getActiveWeaponItem() {
		ItemInstance weapon = getActiveWeaponInstance();
		if (weapon == null) {
			return getFistsWeaponItem();
		}
		return (WeaponTemplate) weapon.getTemplate();
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance() {
		return getInventory().getPaperdollItem(8);
	}

	@Override
	public WeaponTemplate getSecondaryWeaponItem() {
		ItemInstance weapon = getSecondaryWeaponInstance();
		if (weapon == null) {
			return getFistsWeaponItem();
		}
		ItemTemplate item = weapon.getTemplate();
		if (item instanceof WeaponTemplate) {
			return (WeaponTemplate) item;
		}
		return null;
	}

	public boolean isWearingArmor(ArmorTemplate.ArmorType armorType) {
		ItemInstance chest = getInventory().getPaperdollItem(10);
		if (chest == null) {
			return armorType == ArmorTemplate.ArmorType.NONE;
		}
		if (chest.getItemType() != armorType) {
			return false;
		}
		if (chest.getBodyPart() == 32768) {
			return true;
		}
		if (chest.getBodyPart() == 131072) {
			return true;
		}
		ItemInstance legs = getInventory().getPaperdollItem(11);
		return legs == null ? armorType == ArmorTemplate.ArmorType.NONE : legs.getItemType() == armorType;
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage) {
		if (attacker == null || isDead() || attacker.isDead() && !isDot) {
			return;
		}
		if (attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10) {
			if (attacker.getKarma() > 0 && getEffectList().getEffectsBySkillId(5182) != null && !isInZone(Zone.ZoneType.SIEGE)) {
				return;
			}
			if (getKarma() > 0 && attacker.getEffectList().getEffectsBySkillId(5182) != null && !attacker.isInZone(Zone.ZoneType.SIEGE)) {
				return;
			}
		}
		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp) {
		if (standUp) {
			standUp();
			if (isFakeDeath()) {
				breakFakeDeath();
			}
		}
		if (isOlyParticipant()) {
			if (isOlyCompetitionStarted()) {
				getOlyParticipant().OnDamaged(this, attacker, damage, directHp ? getCurrentHp() : getCurrentHp() + getCurrentCp());
			}
			if (!getOlyParticipant().isAlive()) {
				return;
			}
		}
		if (attacker.isPlayable() && !directHp && getCurrentCp() > 0.0) {
			double cp = getCurrentCp();
			if (cp >= damage) {
				cp -= damage;
				damage = 0.0;
			} else {
				damage -= cp;
				cp = 0.0;
			}
			setCurrentCp(cp);
		}
		double hp = getCurrentHp();
		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if (duelEvent != null && hp - damage <= 1.0) {
			setCurrentHp(1.0, false);
			duelEvent.onDie(this);
			return;
		}
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	@Override
	public boolean isAlikeDead() {
		return _fakeDeath == 1 || super.isAlikeDead();
	}

	@Override
	public boolean isMovementDisabled() {
		return isFakeDeath() || super.isMovementDisabled();
	}

	@Override
	public boolean isActionsDisabled() {
		return isFakeDeath() || super.isActionsDisabled();
	}

	@Override
	public void doAttack(Creature target) {
		if (isFakeDeath() || isInMountTransform()) {
			return;
		}
		super.doAttack(target);
	}

	@Override
	public void onHitTimer(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS) {
		if (isFakeDeath()) {
			sendActionFailed();
			return;
		}
		super.onHitTimer(target, damage, crit, miss, soulshot, shld, unchargeSS);
	}

	public boolean isFakeDeath() {
		return _fakeDeath != 0;
	}

	public void setFakeDeath(int value) {
		_fakeDeath = value;
	}

	public void breakFakeDeath() {
		getEffectList().stopAllSkillEffects(EffectType.FakeDeath);
	}

	private void altDeathPenalty(Creature killer) {
		if (!Config.ALT_GAME_DELEVEL) {
			return;
		}
		if (isInZoneBattle() || isInZone(Zone.ZoneType.fun)) {
			return;
		}
		deathPenalty(killer);
	}

	public final boolean atWarWith(Player player) {
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId());
	}

	public boolean atMutualWarWith(Player player) {
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId()) && player.getClan().isAtWarWith(_clan.getClanId());
	}

	@Override
	public void doPurePk(Player killer) {
		super.doPurePk(killer);
		killer.setPkKills(killer.getPkKills() + 1);
		if (Config.SERVICES_PK_ANNOUNCE) {
			Announcements.getInstance().announceByCustomMessage("player.pkannounce", new String[] {
				killer.getName(),
				getName() });
		}
	}

	private final void processRewardPvpPkKill(Player killer, boolean isThisPlayerKiller) {
		if (isThisPlayerKiller) {
			doPurePk(killer);
			killer.getListeners().onPvpPkKill(this, true);
		} else {
			killer.setPvpKills(killer.getPvpKills() + 1);
			killer.getListeners().onPvpPkKill(this, false);
		}
		if (Config.SERVICES_PK_KILL_BONUS_ENABLE || Config.SERVICES_PVP_KILL_BONUS_ENABLE) {
			boolean ipCheckSuccess = true;
			if (Config.SERVICES_PK_PVP_BONUS_TIE_IF_SAME_IP) {
				ipCheckSuccess = getIP() == null && killer.getIP() != null || getIP() != null && !getIP().equals(killer.getIP());
			}
			long now = System.currentTimeMillis();
			long lastKillTime = killer.getVarLong("LastPVPPKKill", 0);
			if (ipCheckSuccess && now - lastKillTime > Config.SERVICES_PK_KILL_BONUS_INTERVAL) {
				if (isThisPlayerKiller) {
					ItemFunctions.addItem(killer, Config.SERVICES_PK_KILL_BONUS_REWARD_ITEM, Config.SERVICES_PK_KILL_BONUS_REWARD_COUNT, true);
				} else {
					ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_BONUS_REWARD_ITEM, Config.SERVICES_PVP_KILL_BONUS_REWARD_COUNT, true);
				}
				killer.setVar("LastPVPPKKill", now, -1);
			}
		}
	}

	public void checkAddItemToDrop(List<ItemInstance> array, List<ItemInstance> items, int maxCount) {
		for (int i = 0; i < maxCount && !items.isEmpty(); ++i) {
			array.add(items.remove(Rnd.get(items.size())));
		}
	}

	public FlagItemAttachment getActiveWeaponFlagAttachment() {
		ItemInstance item = getActiveWeaponInstance();
		if (item == null || !(item.getAttachment() instanceof FlagItemAttachment)) {
			return null;
		}
		return (FlagItemAttachment) item.getAttachment();
	}

	protected void doPKPVPManage(Creature killer) {
		FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
		if (attachment != null) {
			attachment.onDeath(this, killer);
		}
		if (killer == null || killer == _summon || killer == this) {
			return;
		}
		if (isInZoneBattle() || killer.isInZoneBattle()) {
			return;
		}
		boolean inFunZone = isInZone(Zone.ZoneType.fun);
		if (!Config.FUN_ZONE_PVP_COUNT && inFunZone) {
			return;
		}
		if (killer instanceof Summon && (killer = killer.getPlayer()) == null) {
			return;
		}
		if (killer.isPlayer()) {
			Player pk = (Player) killer;
			int repValue = getLevel() - pk.getLevel() >= 20 ? 2 : 1;
			boolean war = atMutualWarWith(pk);
			if (war && pk.getClan().getReputationScore() > 0 && _clan.getLevel() >= 5 && _clan.getReputationScore() > 0 && pk.getClan().getLevel() >= 5) {
				_clan.broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.YOUR_CLAN_MEMBER_S1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENT_CLAN_REPUTATION_SCORE).addString(getName()).addNumber(-_clan.incReputation(-repValue, true, "ClanWar")), this);
				pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE).addNumber(pk.getClan().incReputation(repValue, true, "ClanWar")), pk);
			}
			if (isOnSiegeField()) {
				return;
			}
			if (Config.FUN_ZONE_PVP_COUNT && inFunZone) {
				processRewardPvpPkKill(pk, false);
				pk.sendChanges();
				return;
			}
			if (_pvpFlag > 0 || war) {
				processRewardPvpPkKill(pk, false);
			} else {
				processRewardPvpPkKill(pk, _karma <= 0);
			}
			pk.sendChanges();
		}
		int karma = _karma;
		decreaseKarma(Config.KARMA_LOST_BASE);
		boolean isPvP = killer.isPlayable() || killer instanceof GuardInstance;
		if (killer.isMonster() && !Config.DROP_ITEMS_ON_DIE || isPvP && (_pkKills < Config.MIN_PK_TO_ITEMS_DROP || karma == 0 && Config.KARMA_NEEDED_TO_DROP) || isFestivalParticipant() || !killer.isMonster() && !isPvP) {
			return;
		}
		if (!Config.KARMA_DROP_GM && isGM()) {
			return;
		}
		if (Config.ITEM_ANTIDROP_FROM_PK > 0 && getInventory().getItemByItemId(Config.ITEM_ANTIDROP_FROM_PK) != null) {
			return;
		}
		int max_drop_count = isPvP ? Config.KARMA_DROP_ITEM_LIMIT : 1;
		double dropRate = isPvP ? (double) _pkKills * Config.KARMA_DROPCHANCE_MOD + Config.KARMA_DROPCHANCE_BASE : Config.NORMAL_DROPCHANCE_BASE;
		int dropEquipCount = 0;
		int dropWeaponCount = 0;
		int dropItemCount = 0;
		for (int i = 0; (double) i < Math.ceil(dropRate / 100.0) && i < max_drop_count; ++i) {
			if (!Rnd.chance(dropRate))
				continue;
			int rand = Rnd.get(Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT + Config.DROPCHANCE_ITEM) + 1;
			if (rand > Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT) {
				++dropItemCount;
				continue;
			}
			if (rand > Config.DROPCHANCE_EQUIPPED_WEAPON) {
				++dropEquipCount;
				continue;
			}
			++dropWeaponCount;
		}
		getInventory().writeLock();
		try {
			LazyArrayList<ItemInstance> dropWeapon = new LazyArrayList<>();
			LazyArrayList<ItemInstance> dropEquip = new LazyArrayList<>();
			LazyArrayList<ItemInstance> dropItem = new LazyArrayList<>();
			for (ItemInstance item : getInventory().getItems()) {
				if (!item.canBeDropped(this, true) || Config.KARMA_LIST_NONDROPPABLE_ITEMS.contains(item.getItemId()))
					continue;
				if (item.getTemplate().getType2() == 0) {
					dropWeapon.add(item);
					continue;
				}
				if (item.getTemplate().getType2() == 1 || item.getTemplate().getType2() == 2) {
					dropEquip.add(item);
					continue;
				}
				if (item.getTemplate().getType2() != 5)
					continue;
				dropItem.add(item);
			}
			LazyArrayList<ItemInstance> drop = new LazyArrayList<>();
			checkAddItemToDrop(drop, dropWeapon, dropWeaponCount);
			checkAddItemToDrop(drop, dropEquip, dropEquipCount);
			checkAddItemToDrop(drop, dropItem, dropItemCount);
			if (drop.isEmpty()) {
				return;
			}
			Iterator<ItemInstance> iterator = drop.iterator();
			while (iterator.hasNext()) {
				ItemInstance item = iterator.next();
				if (item.isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED) {
					item.setVariationStat1(0);
					item.setVariationStat2(0);
				}
				item = getInventory().removeItem(item);
				Log.LogItem(this, Log.ItemLog.PvPDrop, item);
				if (item.getEnchantLevel() > 0) {
					sendPacket(new SystemMessage(375).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
				} else {
					sendPacket(new SystemMessage(298).addItemName(item.getItemId()));
				}
				if (killer.isPlayable() && (Config.AUTO_LOOT && Config.AUTO_LOOT_PK || isInFlyingTransform())) {
					killer.getPlayer().getInventory().addItem(item);
					Log.LogItem(this, Log.ItemLog.Pickup, item);
					killer.getPlayer().sendPacket(SystemMessage2.obtainItems(item));
					continue;
				}
				item.dropToTheGround(this, Location.findAroundPosition(this, Config.KARMA_RANDOM_DROP_LOCATION_LIMIT));
			}
		} finally {
			getInventory().writeUnlock();
		}
	}

	@Override
	protected void onDeath(Creature killer) {
		getDeathPenalty().checkCharmOfLuck();
		if (isInStoreMode()) {
			setPrivateStoreType(0);
		}
		if (isProcessingRequest()) {
			Request request = getRequest();
			if (isInTrade()) {
				Player parthner = request.getOtherPlayer(this);
				sendPacket(SendTradeDone.FAIL);
				parthner.sendPacket(SendTradeDone.FAIL);
			}
			request.cancel();
		}
		setAgathion(0);
		boolean checkPvp = true;
		if (Config.ALLOW_CURSED_WEAPONS) {
			if (isCursedWeaponEquipped()) {
				CursedWeaponsManager.getInstance().dropPlayer(this);
				checkPvp = false;
			} else if (killer != null && killer.isPlayer() && killer.isCursedWeaponEquipped()) {
				CursedWeaponsManager.getInstance().increaseKills(((Player) killer).getCursedWeaponEquippedId());
				checkPvp = false;
			}
		}
		if (checkPvp) {
			doPKPVPManage(killer);
			altDeathPenalty(killer);
		}
		getDeathPenalty().notifyDead(killer);
		setIncreasedForce(0);
		if (isInParty() && getParty().isInReflection() && getParty().getReflection() instanceof DimensionalRift) {
			((DimensionalRift) getParty().getReflection()).memberDead(this);
		}
		stopWaterTask();
		if (!isSalvation() && isOnSiegeField() && isCharmOfCourage()) {
			setCharmOfCourage(false);
		}
		Quest q;
		if (getLevel() < 6 && (q = QuestManager.getQuest(255)) != null) {
			processQuestEvent(q.getName(), "CE30", null);
		}
		super.onDeath(killer);
	}

	public void restoreExp() {
		restoreExp(100.0);
	}

	public void restoreExp(double percent) {
		if (percent == 0.0) {
			return;
		}
		int lostexp = 0;
		String lostexps = getVar("lostexp");
		if (lostexps != null) {
			lostexp = Integer.parseInt(lostexps);
			unsetVar("lostexp");
		}
		if (lostexp != 0) {
			addExpAndSp((long) ((double) lostexp * percent / 100.0), 0);
		}
	}

	public void deathPenalty(Creature killer) {
		if (killer == null) {
			return;
		}
		boolean atwar = killer.getPlayer() != null && atWarWith(killer.getPlayer());
		double deathPenaltyBonus = getDeathPenalty().getLevel() * Config.ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY;
		if (deathPenaltyBonus < 2.0)
			deathPenaltyBonus = 1.0;
		else
			deathPenaltyBonus /= 2.0;

		double percentLost = 8.0;
		int level = getLevel();
		if (level >= 79) {
			percentLost = 1.0;
		} else if (level >= 78) {
			percentLost = 1.5;
		} else if (level >= 76) {
			percentLost = 2.0;
		} else if (level >= 40) {
			percentLost = 4.0;
		}
		if (Config.ALT_DEATH_PENALTY) {
			percentLost = percentLost * Config.RATE_XP + (double) _pkKills * Config.ALT_PK_DEATH_RATE;
		}
		if (isFestivalParticipant() || atwar) {
			percentLost /= 4.0;
		}
		int lostexp = (int) Math.round((double) (Experience.LEVEL[level + 1] - Experience.LEVEL[level]) * percentLost / 100.0);
		lostexp = (int) ((double) lostexp * deathPenaltyBonus);
		lostexp = (int) calcStat(Stats.EXP_LOST, lostexp, killer, null);
		if (isOnSiegeField() && getEvent(SiegeEvent.class) != null) {
			lostexp = 0;
		}
		long before = getExp();
		addExpAndSp(-lostexp, 0);
		long lost = before - getExp();
		if (lost > 0) {
			setVar("lostexp", String.valueOf(lost), -1);
		}
	}

	public Request getRequest() {
		return _request;
	}

	public void setRequest(Request transaction) {
		_request = transaction;
	}

	public boolean isBusy() {
		return isProcessingRequest() || isOutOfControl() || isOlyParticipant() || getTeam() != TeamType.NONE || isInStoreMode() || isInDuel() || getMessageRefusal() || isBlockAll() || isInvisible();
	}

	public boolean isProcessingRequest() {
		if (_request == null) {
			return false;
		}
		return _request.isInProgress();
	}

	public boolean isInTrade() {
		return isProcessingRequest() && getRequest().isTypeOf(Request.L2RequestType.TRADE);
	}

	public List<L2GameServerPacket> addVisibleObject(GameObject object, Creature dropper) {
		if (isLogoutStarted() || object == null || object.getObjectId() == getObjectId() || !object.isVisible()) {
			return Collections.emptyList();
		}
		return object.addPacketList(this, dropper);
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper) {
		if (isInvisible() && forPlayer.getObjectId() != getObjectId()) {
			return Collections.emptyList();
		}
		if (getPrivateStoreType() != 0 && forPlayer.getVarB("notraders")) {
			return Collections.emptyList();
		}
		if (isInObserverMode() && getCurrentRegion() != getObserverRegion() && getObserverRegion() == forPlayer.getCurrentRegion()) {
			return Collections.emptyList();
		}
		ArrayList<L2GameServerPacket> list = new ArrayList<>();
		if (forPlayer.getObjectId() != getObjectId()) {
			list.add(isPolymorphed() ? new NpcInfoPoly(this) : new CharInfo(this));
		}
		if (isSitting() && _sittingObject != null) {
			list.add(new ChairSit(this, _sittingObject));
		}
		if (getPrivateStoreType() != 0) {
			if (getPrivateStoreType() == 3) {
				list.add(new PrivateStoreMsgBuy(this));
			} else if (getPrivateStoreType() == 1 || getPrivateStoreType() == 8) {
				list.add(new PrivateStoreMsgSell(this));
			} else if (getPrivateStoreType() == 5) {
				list.add(new RecipeShopMsg(this));
			}
			if (forPlayer.isInZonePeace()) {
				return list;
			}
		}
		if (isCastingNow()) {
			Creature castingTarget = getCastingTarget();
			Skill castingSkill = getCastingSkill();
			long animationEndTime = getAnimationEndTime();
			if (castingSkill != null && castingTarget != null && castingTarget.isCreature() && getAnimationEndTime() > 0) {
				list.add(new MagicSkillUse(this, castingTarget, castingSkill, (int) (animationEndTime - System.currentTimeMillis()), 0));
			}
		}
		if (isInCombat()) {
			list.add(new AutoAttackStart(getObjectId()));
		}
		list.add(RelationChanged.create(forPlayer, this, forPlayer));
		if (isInBoat()) {
			list.add(getBoat().getOnPacket(this, getInBoatPosition()));
		} else if (isMoving() || isFollowing()) {
			list.add(movePacket());
		}
		return list;
	}

	public List<L2GameServerPacket> removeVisibleObject(GameObject object, List<L2GameServerPacket> list) {
		if (isLogoutStarted() || object == null || object.getObjectId() == getObjectId()) {
			return null;
		}
		List<L2GameServerPacket> result = list == null ? object.deletePacketList() : list;
		if (isFollowing() && getFollowTarget() == object) {
			stopMove();
		}
		getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
		return result;
	}

	private void levelSet(int levels) {
		if (levels > 0) {
			sendPacket(Msg.YOU_HAVE_INCREASED_YOUR_LEVEL);
			broadcastPacket(new SocialAction(getObjectId(), 15));
			setCurrentHpMp(getMaxHp(), getMaxMp());
			setCurrentCp(getMaxCp());
			Quest q = QuestManager.getQuest(255);
			if (q != null) {
				processQuestEvent(q.getName(), "CE40", null);
			}
		} else if (levels < 0) {
			checkSkills();
		}
		if (isInParty()) {
			getParty().recalculatePartyData();
		}
		if (_clan != null) {
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
		}
		if (_matchingRoom != null) {
			_matchingRoom.broadcastPlayerUpdate(this);
		}
		rewardSkills(true);
	}

	public void checkSkills() {
		if (Config.ALT_WEAK_SKILL_LEARN) {
			return;
		}
		for (Skill sk : getAllSkillsArray()) {
			SkillTreeTable.checkSkill(this, sk);
		}
		sendPacket(new SkillList(this));
	}

	public void startTimers() {
		startAutoSaveTask();
		startPcBangPointsTask();
		startBonusTask();
		getInventory().startTimers();
		resumeQuestTimers();
	}

	public void stopAllTimers() {
		setAgathion(0);
		stopWaterTask();
		stopBonusTask();
		stopHourlyTask();
		stopKickTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		getInventory().stopAllTimers();
		stopQuestTimers();
		stopCustomHeroEndTask();
	}

	@Override
	public Summon getPet() {
		return _summon;
	}

	public void setPet(Summon summon) {
		boolean isPet = false;
		if (_summon != null && _summon.isPet()) {
			isPet = true;
		}
		unsetVar("pet");
		_summon = summon;
		autoShot();
		if (summon == null) {
			if (isPet) {
				if (isLogoutStarted() && getPetControlItem() != null) {
					setVar("pet", String.valueOf(getPetControlItem().getObjectId()), -1);
				}
				setPetControlItem(null);
			}
			getEffectList().stopEffect(4140);
		}
	}

	public void scheduleDelete() {
		long time = 0;
		if (Config.SERVICES_ENABLE_NO_CARRIER) {
			time = NumberUtils.toInt(getVar("noCarrier"), Config.SERVICES_NO_CARRIER_DEFAULT_TIME);
		}
		scheduleDelete(time * 1000);
	}

	public void scheduleDelete(long time) {
		if (isLogoutStarted() || isInOfflineMode()) {
			return;
		}
		broadcastCharInfo();
		ThreadPoolManager.getInstance().schedule(new RunnableImpl() {

			@Override
			public void runImpl() throws Exception {
				if (!isConnected()) {
					prepareToLogout();
					deleteMe();
				}
			}
		}, time);
	}

	@Override
	protected void onDelete() {
		super.onDelete();
		WorldRegion observerRegion = getObserverRegion();
		if (observerRegion != null) {
			observerRegion.removeObject(this);
		}
		_friendList.notifyFriends(false);
		_inventory.clear();
		_warehouse.clear();
		_summon = null;
		_arrowItem = null;
		_fistsWeaponItem = null;
		_chars = null;
		_enchantScroll = null;
		_lastNpc = HardReferences.emptyRef();
		_observerRegion = null;
	}

	public List<TradeItem> getTradeList() {
		return _tradeList;
	}

	public void setTradeList(List<TradeItem> list) {
		_tradeList = list;
	}

	public String getSellStoreName() {
		return _sellStoreName;
	}

	public void setSellStoreName(String name) {
		_sellStoreName = Strings.stripToSingleLine(name);
	}

	public void setSellList(boolean packageSell, List<TradeItem> list) {
		if (packageSell) {
			_packageSellList = list;
		} else {
			_sellList = list;
		}
	}

	public List<TradeItem> getSellList() {
		return getSellList(_privatestore == 8);
	}

	public List<TradeItem> getSellList(boolean packageSell) {
		return packageSell ? _packageSellList : _sellList;
	}

	public String getBuyStoreName() {
		return _buyStoreName;
	}

	public void setBuyStoreName(String name) {
		_buyStoreName = Strings.stripToSingleLine(name);
	}

	public List<TradeItem> getBuyList() {
		return _buyList;
	}

	public void setBuyList(List<TradeItem> list) {
		_buyList = list;
	}

	public String getManufactureName() {
		return _manufactureName;
	}

	public void setManufactureName(String name) {
		_manufactureName = Strings.stripToSingleLine(name);
	}

	public List<ManufactureItem> getCreateList() {
		return _createList;
	}

	public void setCreateList(List<ManufactureItem> list) {
		_createList = list;
	}

	public boolean isInStoreMode() {
		return _privatestore != 0;
	}

	public int getPrivateStoreType() {
		return _privatestore;
	}

	public void setPrivateStoreType(int type) {
		_privatestore = type;
		if (type != 0) {
			setVar("storemode", String.valueOf(type), -1);
		} else {
			unsetVar("storemode");
		}
	}

	@Override
	public Clan getClan() {
		return _clan;
	}

	public void setClan(Clan clan) {
		if (_clan != clan && _clan != null) {
			unsetVar("canWhWithdraw");
		}
		Clan oldClan;
		if ((oldClan = _clan) != null && clan == null) {
			for (Skill skill : oldClan.getAllSkills()) {
				removeSkill(skill, false);
			}
		}
		_clan = clan;
		if (clan == null) {
			_pledgeType = -128;
			_pledgeClass = 0;
			_powerGrade = 0;
			_apprentice = 0;
			getInventory().validateItems();
			return;
		}
		if (!clan.isAnyMember(getObjectId())) {
			setClan(null);
			if (!isNoble()) {
				setTitle("");
			}
		}
	}

	public SubUnit getSubUnit() {
		return _clan == null ? null : _clan.getSubUnit(_pledgeType);
	}

	public ClanHall getClanHall() {
		int id = _clan != null ? _clan.getHasHideout() : 0;
		return ResidenceHolder.getInstance().getResidence(ClanHall.class, id);
	}

	public Castle getCastle() {
		int id = _clan != null ? _clan.getCastle() : 0;
		return ResidenceHolder.getInstance().getResidence(Castle.class, id);
	}

	public Alliance getAlliance() {
		return _clan == null ? null : _clan.getAlliance();
	}

	public boolean isClanLeader() {
		return _clan != null && getObjectId() == _clan.getLeaderId();
	}

	public boolean isAllyLeader() {
		return getAlliance() != null && getAlliance().getLeader().getLeaderId() == getObjectId();
	}

	@Override
	public void reduceArrowCount() {
		sendPacket(SystemMsg.YOU_CAREFULLY_NOCK_AN_ARROW);
		if (!getInventory().destroyItemByObjectId(getInventory().getPaperdollObjectId(8), 1)) {
			getInventory().setPaperdollItem(8, null);
			_arrowItem = null;
		}
	}

	protected boolean checkAndEquipArrows() {
		if (getInventory().getPaperdollItem(8) == null) {
			ItemInstance activeWeapon = getActiveWeaponInstance();
			if (activeWeapon != null && activeWeapon.getItemType() == WeaponTemplate.WeaponType.BOW) {
				_arrowItem = getInventory().findArrowForBow(activeWeapon.getTemplate());
			}
			if (_arrowItem != null) {
				getInventory().setPaperdollItem(8, _arrowItem);
			}
		} else {
			_arrowItem = getInventory().getPaperdollItem(8);
		}
		return _arrowItem != null;
	}

	public long getUptime() {
		return System.currentTimeMillis() - _uptime;
	}

	public void setUptime(long time) {
		_uptime = time;
	}

	public boolean isInParty() {
		return _party != null;
	}

	public void joinParty(Party party) {
		if (party != null) {
			party.addPartyMember(this);
		}
	}

	public void leaveParty() {
		if (isInParty()) {
			_party.removePartyMember(this, false);
		}
	}

	public Party getParty() {
		return _party;
	}

	public void setParty(Party party) {
		_party = party;
	}

	public Location getLastPartyPosition() {
		return _lastPartyPosition;
	}

	public void setLastPartyPosition(Location loc) {
		_lastPartyPosition = loc;
	}

	public boolean isGM() {
		return _playerAccess != null && _playerAccess.IsGM;
	}

	@Override
	public int getAccessLevel() {
		return _accessLevel;
	}

	public void setAccessLevel(int level) {
		_accessLevel = level;
	}

	public PlayerAccess getPlayerAccess() {
		return _playerAccess;
	}

	public void setPlayerAccess(PlayerAccess pa) {
		_playerAccess = pa != null ? pa : new PlayerAccess();
		setAccessLevel(isGM() || _playerAccess.Menu ? 100 : 0);
	}

	@Override
	public double getLevelMod() {
		return (89.0 + (double) getLevel()) / 100.0;
	}

	@Override
	public void updateStats() {
		if (entering || isLogoutStarted()) {
			return;
		}
		refreshOverloaded();
		refreshExpertisePenalty();
		super.updateStats();
	}

	@Override
	public void sendChanges() {
		if (entering || isLogoutStarted()) {
			return;
		}
		super.sendChanges();
	}

	public void updateKarma(boolean flagChanged) {
		sendStatusUpdate(true, true, 27);
		if (flagChanged) {
			broadcastRelationChanged();
		}
	}

	public boolean isOnline() {
		return _isOnline;
	}

	public void setIsOnline(boolean isOnline) {
		_isOnline = isOnline;
	}

	public void setOnlineStatus(boolean isOnline) {
		_isOnline = isOnline;
		updateOnlineStatus();
	}

	private void updateOnlineStatus() {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
			statement.setInt(1, isOnline() && !isInOfflineMode() ? 1 : 0);
			statement.setLong(2, System.currentTimeMillis() / 1000);
			statement.setInt(3, getObjectId());
			statement.execute();
		} catch (Exception e) {
			_log.error("", e);
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void increaseKarma(long add_karma) {
		boolean flagChanged = _karma == 0;
		long new_karma = (long) _karma + add_karma;
		if (new_karma > Integer.MAX_VALUE) {
			new_karma = Integer.MAX_VALUE;
		}
		if (_karma == 0 && new_karma > 0) {
			if (_pvpFlag > 0) {
				_pvpFlag = 0;
				if (_PvPRegTask != null) {
					_PvPRegTask.cancel(true);
					_PvPRegTask = null;
				}
				sendStatusUpdate(true, true, 26);
			}
			_karma = (int) new_karma;
		} else {
			_karma = (int) new_karma;
		}
		updateKarma(flagChanged);
	}

	public void decreaseKarma(int i) {
		boolean flagChanged = _karma > 0;
		_karma -= i;
		if (_karma <= 0) {
			_karma = 0;
			updateKarma(flagChanged);
		} else {
			updateKarma(false);
		}
	}

	private void loadPremiumItemList() {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT itemNum, itemId, itemCount, itemSender FROM character_premium_items WHERE charId=?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while (rs.next()) {
				int itemNum = rs.getInt("itemNum");
				int itemId = rs.getInt("itemId");
				long itemCount = rs.getLong("itemCount");
				String itemSender = rs.getString("itemSender");
				PremiumItem item = new PremiumItem(itemId, itemCount, itemSender);
				_premiumItems.put(itemNum, item);
			}
		} catch (Exception e) {
			_log.error("", e);
		} finally {
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	public void updatePremiumItem(int itemNum, long newcount) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE character_premium_items SET itemCount=? WHERE charId=? AND itemNum=?");
			statement.setLong(1, newcount);
			statement.setInt(2, getObjectId());
			statement.setInt(3, itemNum);
			statement.execute();
		} catch (Exception e) {
			_log.error("", e);
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void deletePremiumItem(int itemNum) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_premium_items WHERE charId=? AND itemNum=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, itemNum);
			statement.execute();
		} catch (Exception e) {
			_log.error("", e);
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
	}

	public Map<Integer, PremiumItem> getPremiumItemList() {
		return _premiumItems;
	}

	public void store(boolean fast) {
		if (!_storeLock.tryLock()) {
			return;
		}
		try {
			Connection con = null;
			PreparedStatement statement = null;
			try {
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE characters SET face=?,hairStyle=?,hairColor=?,x=?,y=?,z=?,karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,rec_bonus_time=?,hunting_bonus_time=?,rec_tick_cnt=?,hunting_bonus=?,clanid=?,deletetime=?,title=?,accesslevel=?,online=?,leaveclan=?,deleteclan=?,nochannel=?,onlinetime=?,pledge_type=?,pledge_rank=?,lvl_joined_academy=?,apprentice=?,key_bindings=?,pcBangPoints=?,char_name=?,bookmarks=? WHERE obj_Id=? LIMIT 1");
				statement.setInt(1, getFace());
				statement.setInt(2, getHairStyle());
				statement.setInt(3, getHairColor());
				if (_stablePoint == null) {
					statement.setInt(4, getX());
					statement.setInt(5, getY());
					statement.setInt(6, getZ());
				} else {
					statement.setInt(4, _stablePoint.x);
					statement.setInt(5, _stablePoint.y);
					statement.setInt(6, _stablePoint.z);
				}
				statement.setInt(7, getKarma());
				statement.setInt(8, getPvpKills());
				statement.setInt(9, getPkKills());
				statement.setInt(10, getReceivedRec());
				statement.setInt(11, getGivableRec());
				statement.setInt(12, 0);
				statement.setInt(13, 0);
				statement.setInt(14, 0);
				statement.setInt(15, 0);
				statement.setInt(16, getClanId());
				statement.setInt(17, getDeleteTimer());
				statement.setString(18, _title);
				statement.setInt(19, _accessLevel);
				statement.setInt(20, isOnline() && !isInOfflineMode() ? 1 : 0);
				statement.setLong(21, getLeaveClanTime() / 1000);
				statement.setLong(22, getDeleteClanTime() / 1000);
				statement.setLong(23, _NoChannel > 0 ? getNoChannelRemained() / 1000 : _NoChannel);
				statement.setInt(24, (int) (_onlineBeginTime > 0 ? (_onlineTime + System.currentTimeMillis() - _onlineBeginTime) / 1000 : _onlineTime / 1000));
				statement.setInt(25, getPledgeType());
				statement.setInt(26, getPowerGrade());
				statement.setInt(27, getLvlJoinedAcademy());
				statement.setInt(28, getApprentice());
				statement.setBytes(29, getKeyBindings());
				statement.setInt(30, getPcBangPoints());
				statement.setString(31, getName());
				statement.setInt(32, 0);
				statement.setInt(33, getObjectId());
				statement.executeUpdate();
				GameStats.increaseUpdatePlayerBase();
				if (!fast) {
					EffectsDAO.getInstance().insert(this);
					CharacterGroupReuseDAO.getInstance().insert(this);
					storeDisableSkills();
					storeBlockList();
				}
				storeCharSubClasses();
				storeRecommendedCharacters();
			} catch (Exception e) {
				_log.error("Could not store char data: " + this + "!", e);
			} finally {
				DbUtils.closeQuietly(con, statement);
			}
		} finally {
			_storeLock.unlock();
		}
	}

	public Skill addSkill(Skill newSkill, boolean store) {
		if (newSkill == null) {
			return null;
		}
		Skill oldSkill = addSkill(newSkill);
		if (newSkill.equals(oldSkill)) {
			return oldSkill;
		}
		if (store) {
			storeSkill(newSkill, oldSkill);
		}
		return oldSkill;
	}

	public Skill removeSkill(Skill skill, boolean fromDB) {
		if (skill == null) {
			return null;
		}
		return removeSkill(skill.getId(), fromDB);
	}

	public Skill removeSkill(int id, boolean fromDB) {
		Skill oldSkill = removeSkillById(id);
		if (!fromDB) {
			return oldSkill;
		}
		if (oldSkill != null) {
			Connection con = null;
			PreparedStatement statement = null;
			try {
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?");
				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, getActiveClassId());
				statement.execute();
			} catch (Exception e) {
				_log.error("Could not delete skill!", e);
			} finally {
				DbUtils.closeQuietly(con, statement);
			}
		}
		return oldSkill;
	}

	private void storeSkill(Skill newSkill, Skill oldSkill) {
		if (newSkill == null) {
			_log.warn("could not store new skill. its NULL");
			return;
		}
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO character_skills (char_obj_id,skill_id,skill_level,class_index) values(?,?,?,?)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newSkill.getId());
			statement.setInt(3, newSkill.getLevel());
			statement.setInt(4, getActiveClassId());
			statement.execute();
		} catch (Exception e) {
			_log.error("Error could not store skills!", e);
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void restoreSkills() {
		restoreSkills(getActiveClassId());
	}

	private void restoreSkills(int classId) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, classId);
			rset = statement.executeQuery();
			while (rset.next()) {
				int id = rset.getInt("skill_id");
				int level = rset.getInt("skill_level");
				Skill skill = SkillTable.getInstance().getInfo(id, level);
				if (skill == null)
					continue;
				if (!(isGM() || Config.ALT_WEAK_SKILL_LEARN || SkillAcquireHolder.getInstance().isSkillPossible(this, skill))) {
					_log.warn("Skill " + skill + " not possible for player " + this + " with classId " + getActiveClassId());
					removeSkill(skill, true);
					removeSkillFromShortCut(skill.getId());
					continue;
				}
				addSkill(skill);
			}
			if (getActiveClassId() != classId) {
				return;
			}
			if (isNoble()) {
				updateNobleSkills();
			}
			if (_hero && (getBaseClassId() == getActiveClassId() || Config.ALT_ALLOW_HERO_SKILLS_ON_SUB_CLASS)) {
				HeroController.addSkills(this);
			}
			if (_clan != null) {
				_clan.addSkillsQuietly(this);
				if (_clan.getLeaderId() == getObjectId() && _clan.getLevel() >= 5) {
					SiegeUtils.addSiegeSkills(this);
				}
			}
			ClassId activeClassId = null;
			for (ClassId clsId : ClassId.VALUES) {
				if (clsId.getId() != getActiveClassId())
					continue;
				activeClassId = clsId;
			}
			switch (activeClassId) {
			case dwarvenFighter:
			case scavenger:
			case bountyHunter:
			case artisan:
			case warsmith:
			case fortuneSeeker:
			case maestro: {
				addSkill(SkillTable.getInstance().getInfo(1321, 1));
			}
			}
			addSkill(SkillTable.getInstance().getInfo(1322, 1));
			if (Config.UNSTUCK_SKILL && getSkillLevel(1050) < 0) {
				addSkill(SkillTable.getInstance().getInfo(2099, 1));
			}
			if (Config.BLOCK_BUFF_SKILL) {
				addSkill(SkillTable.getInstance().getInfo(5088, 1));
			}
			if (Config.NOBLES_BUFF_SKILL) {
				addSkill(SkillTable.getInstance().getInfo(1323, 1));
			}
		} catch (Exception e) {
			_log.warn("Could not restore skills for player objId: " + getObjectId());
			_log.error("", e);
		} finally {
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void storeDisableSkills() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DatabaseFactory.getInstance().getConnection();
			pstmt = conn.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id = ? AND (class_index=? OR class_index=-1) AND `end_time` < ?");
			pstmt.setInt(1, getObjectId());
			pstmt.setInt(2, getActiveClassId());
			pstmt.setLong(3, System.currentTimeMillis());
			pstmt.executeUpdate();
			DbUtils.close(pstmt);
			if (_skillReuses.isEmpty()) {
				return;
			}
			pstmt = conn.prepareStatement("REPLACE INTO `character_skills_save`(`char_obj_id`, `skill_id`, `skill_level`, `class_index`, `end_time`, `reuse_delay_org`) VALUES\t(?,?,?,?,?,?)");
			CHashIntObjectMap<TimeStamp> skillReuses = new CHashIntObjectMap<>();
			IntObjectMap<TimeStamp> intObjectMap = _skillReuses;
			synchronized (intObjectMap) {
				skillReuses.putAll(_skillReuses);
			}
			for (TimeStamp timeStamp : skillReuses.values()) {
				Skill skill = SkillTable.getInstance().getInfo(timeStamp.getId(), timeStamp.getLevel());
				if (skill == null)
					continue;
				pstmt.setInt(1, getObjectId());
				pstmt.setInt(2, skill.getId());
				pstmt.setInt(3, skill.getLevel());
				pstmt.setInt(4, !skill.isSharedClassReuse() ? getActiveClassId() : -1);
				pstmt.setLong(5, timeStamp.getEndTime());
				pstmt.setLong(6, timeStamp.getReuseBasic());
				pstmt.executeUpdate();
			}
		} catch (Exception e) {
			_log.warn("Could not store disable skills data: " + e);
		} finally {
			DbUtils.closeQuietly(conn, pstmt);
		}
	}

	public void restoreDisableSkills() {
		_skillReuses.clear();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		try {
			conn = DatabaseFactory.getInstance().getConnection();
			pstmt = conn.prepareStatement("SELECT skill_id, skill_level, end_time, reuse_delay_org FROM character_skills_save WHERE char_obj_id=? AND (class_index=? OR class_index=-1)");
			pstmt.setInt(1, getObjectId());
			pstmt.setInt(2, getActiveClassId());
			rset = pstmt.executeQuery();
			while (rset.next()) {
				int skillId = rset.getInt("skill_id");
				int skillLevel = rset.getInt("skill_level");
				long endTime = rset.getLong("end_time");
				long rDelayOrg = rset.getLong("reuse_delay_org");
				long curTime = System.currentTimeMillis();
				Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
				if (skill == null || endTime - curTime <= 500)
					continue;
				_skillReuses.put(skill.hashCode(), new TimeStamp(skill, endTime, rDelayOrg));
			}
			DbUtils.close(pstmt);
			pstmt = conn.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id = ? AND (class_index=? OR class_index=-1) AND `end_time` < ?");
			pstmt.setInt(1, getObjectId());
			pstmt.setInt(2, getActiveClassId());
			pstmt.setLong(3, System.currentTimeMillis());
			pstmt.executeUpdate();
		} catch (Exception e) {
			_log.error("Could not restore active skills data!", e);
		} finally {
			DbUtils.closeQuietly(conn, pstmt, rset);
		}
	}

	private void restoreHenna() {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("select slot, symbol_id from character_hennas where char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, getActiveClassId());
			rset = statement.executeQuery();
			for (int i = 0; i < 3; ++i) {
				_henna[i] = null;
			}
			while (rset.next()) {
				int symbol_id;
				Henna tpl;
				int slot = rset.getInt("slot");
				if (slot < 1 || slot > 3 || (symbol_id = rset.getInt("symbol_id")) == 0 || (tpl = HennaHolder.getInstance().getHenna(symbol_id)) == null)
					continue;
				_henna[slot - 1] = tpl;
			}
		} catch (Exception e) {
			_log.warn("could not restore henna: " + e);
		} finally {
			DbUtils.closeQuietly(con, statement, rset);
		}
		recalcHennaStats();
	}

	public int getHennaEmptySlots() {
		int totalSlots = 1 + getClassId().level();
		for (int i = 0; i < 3; ++i) {
			if (_henna[i] == null)
				continue;
			--totalSlots;
		}
		if (totalSlots <= 0) {
			return 0;
		}
		return totalSlots;
	}

	public boolean removeHenna(int slot) {
		if (slot < 1 || slot > 3) {
			return false;
		}
		if (_henna[--slot] == null) {
			return false;
		}
		Henna henna = _henna[slot];
		int dyeID = henna.getDyeId();
		_henna[slot] = null;
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_hennas where char_obj_id=? and slot=? and class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot + 1);
			statement.setInt(3, getActiveClassId());
			statement.execute();
		} catch (Exception e) {
			_log.warn("could not remove char henna: " + e, e);
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
		recalcHennaStats();
		sendPacket(new HennaInfo(this));
		sendUserInfo(true);
		ItemFunctions.addItem(this, dyeID, henna.getDrawCount() / 2, true);
		return true;
	}

	public boolean addHenna(Henna henna) {
		if (getHennaEmptySlots() == 0) {
			sendPacket(SystemMsg.NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL);
			return false;
		}
		for (int i = 0; i < 3; ++i) {
			if (_henna[i] != null)
				continue;
			_henna[i] = henna;
			recalcHennaStats();
			Connection con = null;
			PreparedStatement statement = null;
			try {
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("INSERT INTO `character_hennas` (char_obj_id, symbol_id, slot, class_index) VALUES (?,?,?,?)");
				statement.setInt(1, getObjectId());
				statement.setInt(2, henna.getSymbolId());
				statement.setInt(3, i + 1);
				statement.setInt(4, getActiveClassId());
				statement.execute();
			} catch (Exception e) {
				_log.warn("could not save char henna: " + e);
			} finally {
				DbUtils.closeQuietly(con, statement);
			}
			sendPacket(new HennaInfo(this));
			sendUserInfo(true);
			return true;
		}
		return false;
	}

	private void recalcHennaStats() {
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;
		for (int i = 0; i < 3; ++i) {
			Henna henna = _henna[i];
			if (henna == null || !henna.isForThisClass(this))
				continue;
			_hennaINT += henna.getStatINT();
			_hennaSTR += henna.getStatSTR();
			_hennaMEN += henna.getStatMEN();
			_hennaCON += henna.getStatCON();
			_hennaWIT += henna.getStatWIT();
			_hennaDEX += henna.getStatDEX();
		}
		if (_hennaINT > 5) {
			_hennaINT = 5;
		}
		if (_hennaSTR > 5) {
			_hennaSTR = 5;
		}
		if (_hennaMEN > 5) {
			_hennaMEN = 5;
		}
		if (_hennaCON > 5) {
			_hennaCON = 5;
		}
		if (_hennaWIT > 5) {
			_hennaWIT = 5;
		}
		if (_hennaDEX > 5) {
			_hennaDEX = 5;
		}
	}

	public Henna getHenna(int slot) {
		if (slot < 1 || slot > 3) {
			return null;
		}
		return _henna[slot - 1];
	}

	public int getHennaStatINT() {
		return _hennaINT;
	}

	public int getHennaStatSTR() {
		return _hennaSTR;
	}

	public int getHennaStatCON() {
		return _hennaCON;
	}

	public int getHennaStatMEN() {
		return _hennaMEN;
	}

	public int getHennaStatWIT() {
		return _hennaWIT;
	}

	public int getHennaStatDEX() {
		return _hennaDEX;
	}

	@Override
	public boolean consumeItem(int itemConsumeId, long itemCount) {
		if (getInventory().destroyItemByItemId(itemConsumeId, itemCount)) {
			sendPacket(SystemMessage2.removeItems(itemConsumeId, itemCount));
			return true;
		}
		return false;
	}

	@Override
	public boolean consumeItemMp(int itemId, int mp) {
		for (ItemInstance item : getInventory().getPaperdollItems()) {
			if (item == null || item.getItemId() != itemId)
				continue;
			int newMp = item.getDuration() - mp;
			if (newMp < 0)
				break;
			item.setDuration(newMp);
			sendPacket(new InventoryUpdate().addModifiedItem(item));
			return true;
		}
		return false;
	}

	@Override
	public boolean isMageClass() {
		ClassId classId = getClassId();
		return classId.isMage();
	}

	public boolean isMounted() {
		return _mountNpcId > 0;
	}

	public final boolean isRiding() {
		return _riding;
	}

	public final void setRiding(boolean mode) {
		_riding = mode;
	}

	public boolean checkLandingState() {
		if (isInZone(Zone.ZoneType.no_landing)) {
			return false;
		}
		SiegeEvent siege = getEvent(SiegeEvent.class);
		if (siege != null) {
			Residence unit = siege.getResidence();
			return unit != null && getClan() != null && isClanLeader() && getClan().getCastle() == unit.getId();
		}
		return true;
	}

	public void setMount(int npcId, int obj_id, int level) {
		if (isCursedWeaponEquipped()) {
			return;
		}
		switch (npcId) {
		case 0: {
			setFlying(false);
			setRiding(false);
			if (getTransformation() > 0) {
				setTransformation(0);
			}
			removeSkillById(4289);
			getEffectList().stopEffect(4258);
			break;
		}
		case 12526:
		case 12527:
		case 12528:
		case 16038:
		case 16039:
		case 16040:
		case 16068: {
			setRiding(true);
			break;
		}
		case 12621: {
			setFlying(true);
			setLoc(getLoc().changeZ(32));
			addSkill(SkillTable.getInstance().getInfo(4289, 1), false);
			break;
		}
		case 16037:
		case 16041:
		case 16042: {
			setRiding(true);
		}
		}
		if (npcId > 0) {
			unEquipWeapon();
		}
		_mountNpcId = npcId;
		_mountObjId = obj_id;
		_mountLevel = level;
		broadcastUserInfo(true);
		broadcastPacket(new Ride(this));
		broadcastUserInfo(true);
		sendPacket(new SkillList(this));
	}

	public void unEquipWeapon() {
		ItemInstance wpn = getSecondaryWeaponInstance();
		if (wpn != null) {
			sendDisarmMessage(wpn);
			getInventory().unEquipItem(wpn);
		}
		if ((wpn = getActiveWeaponInstance()) != null) {
			sendDisarmMessage(wpn);
			getInventory().unEquipItem(wpn);
		}
		abortAttack(true, true);
		abortCast(true, true);
	}

	@Override
	public int getSpeed(int baseSpeed) {
		if (isMounted()) {
			PetData petData = PetDataTable.getInstance().getInfo(_mountNpcId, _mountLevel);
			int speed = 187;
			if (petData != null) {
				speed = petData.getSpeed();
			}
			double mod = 1.0;
			int level = getLevel();
			if (_mountLevel > level && level - _mountLevel > 10) {
				mod = 0.5;
			}
			baseSpeed = (int) (mod * (double) speed);
		}
		return super.getSpeed(baseSpeed);
	}

	public int getMountNpcId() {
		return _mountNpcId;
	}

	public int getMountObjId() {
		return _mountObjId;
	}

	public int getMountLevel() {
		return _mountLevel;
	}

	public void sendDisarmMessage(ItemInstance wpn) {
		if (wpn.getEnchantLevel() > 0) {
			SystemMessage sm = new SystemMessage(1064);
			sm.addNumber(wpn.getEnchantLevel());
			sm.addItemName(wpn.getItemId());
			sendPacket(sm);
		} else {
			SystemMessage sm = new SystemMessage(417);
			sm.addItemName(wpn.getItemId());
			sendPacket(sm);
		}
	}

	public Warehouse.WarehouseType getUsingWarehouseType() {
		return _usingWHType;
	}

	public void setUsingWarehouseType(Warehouse.WarehouseType type) {
		_usingWHType = type;
	}

	public Collection<EffectCubic> getCubics() {
		return _cubics == null ? Collections.emptyList() : _cubics.values();
	}

	public void addCubic(EffectCubic cubic) {
		if (_cubics == null) {
			_cubics = new ConcurrentHashMap<>(3);
		}
		_cubics.put(cubic.getId(), cubic);
	}

	public void removeCubic(int id) {
		if (_cubics != null) {
			_cubics.remove(id);
		}
	}

	public EffectCubic getCubic(int id) {
		return _cubics == null ? null : _cubics.get(id);
	}

	@Override
	public String toString() {
		return getName() + "[" + getObjectId() + "]";
	}

	public int getEnchantEffect() {
		ItemInstance wpn = getActiveWeaponInstance();
		if (wpn == null) {
			return 0;
		}
		return Math.min(127, wpn.getEnchantLevel());
	}

	public NpcInstance getLastNpc() {
		return _lastNpc.get();
	}

	public void setLastNpc(NpcInstance npc) {
		_lastNpc = npc == null ? HardReferences.emptyRef() : npc.getRef();
	}

	public MultiSellHolder.MultiSellListContainer getMultisell() {
		return _multisell;
	}

	public void setMultisell(MultiSellHolder.MultiSellListContainer multisell) {
		_multisell = multisell;
	}

	public boolean isFestivalParticipant() {
		return getReflection() instanceof DarknessFestival;
	}

	@Override
	public boolean unChargeShots(boolean spirit) {
		ItemInstance weapon = getActiveWeaponInstance();
		if (weapon == null) {
			return false;
		}
		if (spirit) {
			weapon.setChargedSpiritshot(0);
		} else {
			weapon.setChargedSoulshot(0);
		}
		autoShot();
		return true;
	}

	public boolean unChargeFishShot() {
		ItemInstance weapon = getActiveWeaponInstance();
		if (weapon == null) {
			return false;
		}
		weapon.setChargedFishshot(false);
		autoShot();
		return true;
	}

	public void autoShot() {
		for (Integer shotId : _activeSoulShots) {
			ItemInstance item = getInventory().getItemByItemId(shotId);
			if (item == null) {
				removeAutoSoulShot(shotId);
				continue;
			}
			IItemHandler handler;
			if (!item.getTemplate().testCondition(this, item, false) || (handler = item.getTemplate().getHandler()) == null)
				continue;
			handler.useItem(this, item, false);
		}
	}

	public boolean getChargedFishShot() {
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getChargedFishshot();
	}

	@Override
	public boolean getChargedSoulShot() {
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getChargedSoulshot() == 1;
	}

	@Override
	public int getChargedSpiritShot() {
		ItemInstance weapon = getActiveWeaponInstance();
		if (weapon == null) {
			return 0;
		}
		return weapon.getChargedSpiritshot();
	}

	public void addAutoSoulShot(Integer itemId) {
		_activeSoulShots.add(itemId);
	}

	public void removeAutoSoulShot(Integer itemId) {
		_activeSoulShots.remove(itemId);
	}

	public Set<Integer> getAutoSoulShot() {
		return _activeSoulShots;
	}

	@Override
	public InvisibleType getInvisibleType() {
		return _invisibleType;
	}

	public void setInvisibleType(InvisibleType vis) {
		_invisibleType = vis;
	}

	public int getClanPrivileges() {
		if (_clan == null) {
			return 0;
		}
		if (isClanLeader()) {
			return 8388606;
		}
		if (_powerGrade < 1 || _powerGrade > 9) {
			return 0;
		}
		RankPrivs privs = _clan.getRankPrivs(_powerGrade);
		if (privs != null) {
			return privs.getPrivs();
		}
		return 0;
	}

	public void teleToClosestTown() {
		teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_VILLAGE), ReflectionManager.DEFAULT);
	}

	public void teleToCastle() {
		teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_CASTLE), ReflectionManager.DEFAULT);
	}

	public void teleToClanhall() {
		teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_CLANHALL), ReflectionManager.DEFAULT);
	}

	@Override
	public void sendMessage(CustomMessage message) {
		sendMessage(message.toString());
	}

	@Override
	public void teleToLocation(int x, int y, int z, int refId) {
		if (isDeleted()) {
			return;
		}
		super.teleToLocation(x, y, z, refId);
	}

	@Override
	public boolean onTeleported() {
		if (!super.onTeleported()) {
			return false;
		}
		if (isFakeDeath()) {
			breakFakeDeath();
		}
		if (isInBoat()) {
			setLoc(getBoat().getLoc());
		}
		setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
		spawnMe();
		setLastClientPosition(getLoc());
		setLastServerPosition(getLoc());
		if (isPendingRevive()) {
			doRevive();
		}
		sendActionFailed();
		getAI().notifyEvent(CtrlEvent.EVT_TELEPORTED);
		if (isLockedTarget() && getTarget() != null) {
			sendPacket(new MyTargetSelected(getTarget().getObjectId(), 0));
		}
		sendUserInfo(true);
		if (getPet() != null) {
			getPet().teleportToOwner();
		}
		if (!(!Config.ALT_TELEPORT_PROTECTION || isInZone(Zone.ZoneType.peace_zone) || isInZone(Zone.ZoneType.SIEGE) || isInZone(Zone.ZoneType.offshore) || isOlyParticipant())) {
			setAfterTeleportPortectionTime(System.currentTimeMillis() + 1000 * Config.ALT_TELEPORT_PROTECTION_TIME);
			sendMessage(new CustomMessage("alt.teleport_protect", this, Config.ALT_TELEPORT_PROTECTION_TIME));
		}
		return true;
	}

	public boolean enterObserverMode(Location loc) {
		WorldRegion observerRegion = World.getRegion(loc);
		if (observerRegion == null) {
			return false;
		}
		if (!_observerMode.compareAndSet(0, 1)) {
			return false;
		}
		setTarget(null);
		stopMove();
		sitDown(null);
		setFlying(true);
		World.removeObjectsFromPlayer(this);
		setObserverRegion(observerRegion);
		broadcastCharInfo();
		sendPacket(new ObserverStart(loc));
		return true;
	}

	public void appearObserverMode() {
		if (!_observerMode.compareAndSet(1, 3)) {
			return;
		}
		WorldRegion currentRegion = getCurrentRegion();
		WorldRegion observerRegion = getObserverRegion();
		if (!observerRegion.equals(currentRegion)) {
			observerRegion.addObject(this);
		}
		World.showObjectsToPlayer(this);
		if (isOlyObserver()) {
			for (Player p : getOlyObservingStadium().getPlayers()) {
				if (!p.isOlyCompetitionStarted())
					continue;
				sendPacket(new ExOlympiadUserInfo(p));
			}
		}
	}

	public void leaveObserverMode() {
		if (!_observerMode.compareAndSet(3, 2)) {
			return;
		}
		WorldRegion currentRegion = getCurrentRegion();
		WorldRegion observerRegion = getObserverRegion();
		if (!observerRegion.equals(currentRegion)) {
			observerRegion.removeObject(this);
		}
		World.removeObjectsFromPlayer(this);
		setObserverRegion(null);
		setTarget(null);
		stopMove();
		sendPacket(new ObserverEnd(getLoc()));
	}

	public void returnFromObserverMode() {
		if (!_observerMode.compareAndSet(2, 0)) {
			return;
		}
		setLastClientPosition(null);
		setLastServerPosition(null);
		unblock();
		standUp();
		setFlying(false);
		broadcastCharInfo();
		World.showObjectsToPlayer(this);
	}

	public void enterOlympiadObserverMode(Stadium stadium) {
		WorldRegion observerRegion = World.getRegion(stadium.getObservingLoc());
		if (observerRegion == null || _olyObserveStadium != null) {
			return;
		}
		if (!_observerMode.compareAndSet(0, 1)) {
			return;
		}
		setTarget(null);
		setLastNpc(null);
		stopMove();
		_olyObserveStadium = stadium;
		World.removeObjectsFromPlayer(this);
		setObserverRegion(observerRegion);
		block();
		broadcastCharInfo();
		setReflection(stadium);
		setLastClientPosition(null);
		setLastServerPosition(null);
		sendPacket(new ExOlympiadMode(3), new TeleportToLocation(this, stadium.getObservingLoc()));
	}

	public void switchOlympiadObserverArena(Stadium stadium) {
		if (_olyObserveStadium == null || stadium == _olyObserveStadium) {
			return;
		}
		WorldRegion oldObserverRegion = World.getRegion(_olyObserveStadium.getObservingLoc());
		if (!_observerMode.compareAndSet(3, 0)) {
			return;
		}
		if (oldObserverRegion != null) {
			oldObserverRegion.removeObject(this);
			oldObserverRegion.removeFromPlayers(this);
		}
		_olyObserveStadium = null;
		World.removeObjectsFromPlayer(this);
		sendPacket(new ExOlympiadMode(0));
		enterOlympiadObserverMode(stadium);
	}

	public void leaveOlympiadObserverMode() {
		if (_olyObserveStadium == null) {
			return;
		}
		if (!_observerMode.compareAndSet(3, 2)) {
			return;
		}
		WorldRegion currentRegion = getCurrentRegion();
		WorldRegion observerRegion = getObserverRegion();
		if (observerRegion != null && currentRegion != null && !observerRegion.equals(currentRegion)) {
			observerRegion.removeObject(this);
		}
		World.removeObjectsFromPlayer(this);
		setObserverRegion(null);
		_olyObserveStadium = null;
		setTarget(null);
		stopMove();
		sendPacket(new ExOlympiadMode(0));
		setReflection(ReflectionManager.DEFAULT);
		sendPacket(new TeleportToLocation(this, getLoc()));
	}

	public boolean isOlyObserver() {
		return _olyObserveStadium != null;
	}

	public Stadium getOlyObservingStadium() {
		return _olyObserveStadium;
	}

	@Override
	public boolean isInObserverMode() {
		return _observerMode.get() > 0;
	}

	public int getObserverMode() {
		return _observerMode.get();
	}

	public Participant getOlyParticipant() {
		return _olyParticipant;
	}

	@Override
	public boolean isOlyParticipant() {
		return _olyParticipant != null;
	}

	public void setOlyParticipant(Participant participant) {
		_olyParticipant = participant;
	}

	public boolean isOlyCompetitionStarted() {
		return isOlyParticipant() && _olyParticipant.getCompetition().getState() == CompetitionState.PLAYING;
	}

	public boolean isOlyCompetitionStandby() {
		return isOlyParticipant() && _olyParticipant.getCompetition().getState() == CompetitionState.STAND_BY;
	}

	public boolean isOlyCompetitionPreparing() {
		return isOlyParticipant() && (_olyParticipant.getCompetition().getState() == CompetitionState.INIT || _olyParticipant.getCompetition().getState() == CompetitionState.STAND_BY);
	}

	public boolean isOlyCompetitionFinished() {
		return isOlyParticipant() && _olyParticipant.getCompetition().getState() == CompetitionState.FINISH;
	}

	public boolean isLooseOlyCompetition() {
		if (isOlyParticipant()) {
			if (isOlyCompetitionFinished()) {
				return !_olyParticipant.isAlive();
			}
			return _olyParticipant.isPlayerLoose(this);
		}
		return false;
	}

	public WorldRegion getObserverRegion() {
		return _observerRegion;
	}

	public void setObserverRegion(WorldRegion region) {
		_observerRegion = region;
	}

	public int getTeleMode() {
		return _telemode;
	}

	public void setTeleMode(int mode) {
		_telemode = mode;
	}

	public void setLoto(int i, int val) {
		_loto[i] = val;
	}

	public int getLoto(int i) {
		return _loto[i];
	}

	public void setRace(int i, int val) {
		_race[i] = val;
	}

	public int getRace(int i) {
		return _race[i];
	}

	public boolean getMessageRefusal() {
		return _messageRefusal;
	}

	public void setMessageRefusal(boolean mode) {
		_messageRefusal = mode;
	}

	public boolean getTradeRefusal() {
		return _tradeRefusal;
	}

	public void setTradeRefusal(boolean mode) {
		_tradeRefusal = mode;
	}

	public void addToBlockList(String charName) {
		if (charName == null || charName.equalsIgnoreCase(getName()) || isInBlockList(charName)) {
			sendPacket(Msg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
			return;
		}
		Player block_target = World.getPlayer(charName);
		if (block_target != null) {
			if (block_target.isGM()) {
				sendPacket(Msg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
				return;
			}
			_blockList.put(block_target.getObjectId(), block_target.getName());
			sendPacket(new SystemMessage(617).addString(block_target.getName()));
			block_target.sendPacket(new SystemMessage(619).addString(getName()));
			return;
		}
		int charId = CharacterDAO.getInstance().getObjectIdByName(charName);
		if (charId == 0) {
			sendPacket(Msg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
			return;
		}
		if (Config.gmlist.containsKey(charId) && Config.gmlist.get(Integer.valueOf(charId)).IsGM) {
			sendPacket(Msg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
			return;
		}
		_blockList.put(charId, charName);
		sendPacket(new SystemMessage(617).addString(charName));
	}

	public void removeFromBlockList(String charName) {
		int charId = 0;
		Iterator<Integer> iterator = _blockList.keySet().iterator();
		while (iterator.hasNext()) {
			int blockId = iterator.next();
			if (!charName.equalsIgnoreCase(_blockList.get(blockId)))
				continue;
			charId = blockId;
			break;
		}
		if (charId == 0) {
			sendPacket(Msg.YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER_FROM_IGNORE_LIST);
			return;
		}
		sendPacket(new SystemMessage(618).addString(_blockList.remove(charId)));
		Player block_target = GameObjectsStorage.getPlayer(charId);
		if (block_target != null) {
			block_target.sendMessage(getName() + " has removed you from his/her Ignore List.");
		}
	}

	public boolean isInBlockList(Player player) {
		return isInBlockList(player.getObjectId());
	}

	public boolean isInBlockList(int charId) {
		return _blockList != null && _blockList.containsKey(charId);
	}

	public boolean isInBlockList(String charName) {
		Iterator<Integer> iterator = _blockList.keySet().iterator();
		while (iterator.hasNext()) {
			int blockId = iterator.next();
			if (!charName.equalsIgnoreCase(_blockList.get(blockId)))
				continue;
			return true;
		}
		return false;
	}

	private void restoreBlockList() {
		_blockList.clear();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT target_Id, char_name FROM character_blocklist LEFT JOIN characters ON ( character_blocklist.target_Id = characters.obj_Id ) WHERE character_blocklist.obj_Id = ?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while (rs.next()) {
				int targetId = rs.getInt("target_Id");
				String name = rs.getString("char_name");
				if (name == null)
					continue;
				_blockList.put(targetId, name);
			}
		} catch (SQLException e) {
			_log.warn("Can't restore player blocklist " + e, e);
		} finally {
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	private void storeBlockList() {
		Connection con = null;
		Statement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_blocklist WHERE obj_Id=" + getObjectId());
			if (_blockList.isEmpty()) {
				return;
			}
			SqlBatch b = new SqlBatch("INSERT IGNORE INTO `character_blocklist` (`obj_Id`,`target_Id`) VALUES");
			Map<Integer, String> map = _blockList;
			synchronized (map) {
				for (Map.Entry<Integer, String> e : _blockList.entrySet()) {
					StringBuilder sb = new StringBuilder("(");
					sb.append(getObjectId()).append(",");
					sb.append(e.getKey()).append(")");
					b.write(sb.toString());
				}
			}
			if (!b.isEmpty()) {
				statement.executeUpdate(b.close());
			}
		} catch (Exception e) {
			_log.warn("Can't store player blocklist " + e);
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean isBlockAll() {
		return _blockAll;
	}

	public void setBlockAll(boolean state) {
		_blockAll = state;
	}

	public Collection<String> getBlockList() {
		return _blockList.values();
	}

	public Map<Integer, String> getBlockListMap() {
		return _blockList;
	}

	private void stopCustomHeroEndTask() {
		if (_customHeroRemoveTask != null) {
			_customHeroRemoveTask.cancel(true);
			_customHeroRemoveTask = null;
		}
	}

	public void setCustomHero(boolean customHero, long customHeroStatusDuration, boolean processSkills) {
		if (!isHero() && customHero && customHeroStatusDuration > 0) {
			setVar("CustomHeroEndTime", System.currentTimeMillis() / 1000 + customHeroStatusDuration, -1);
			setHero(true);
			if (processSkills) {
				HeroController.addSkills(this);
			}
			_customHeroRemoveTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.EndCustomHeroTask(this), customHeroStatusDuration * 1000);
		} else if (!customHero) {
			unsetVar("CustomHeroEndTime");
			stopCustomHeroEndTask();
			if (HeroController.getInstance().isCurrentHero(this)) {
				return;
			}
			setHero(false);
			if (processSkills) {
				HeroController.removeSkills(this);
			}
		}
	}

	@Override
	public boolean isHero() {
		return _hero;
	}

	public void setHero(boolean hero) {
		_hero = hero;
	}

	public void updateNobleSkills() {
		if (isNoble()) {
			if (isClanLeader() && getClan().getCastle() > 0) {
				addSkill(SkillTable.getInstance().getInfo(327, 1));
			}
			addSkill(SkillTable.getInstance().getInfo(325, 1));
			addSkill(SkillTable.getInstance().getInfo(1323, 1));
			addSkill(SkillTable.getInstance().getInfo(1324, 1));
			addSkill(SkillTable.getInstance().getInfo(1325, 1));
			addSkill(SkillTable.getInstance().getInfo(1326, 1));
			addSkill(SkillTable.getInstance().getInfo(1327, 1));
		} else {
			removeSkillById(327);
			removeSkillById(325);
			removeSkillById(1323);
			removeSkillById(1324);
			removeSkillById(1325);
			removeSkillById(1326);
			removeSkillById(1327);
		}
	}

	public boolean isNoble() {
		return _noble;
	}

	public void setNoble(boolean noble) {
		_noble = noble;
		if (noble) {
			broadcastPacket(new SocialAction(getObjectId(), 16));
		}
	}

	public int getSubLevel() {
		return isSubClassActive() ? getLevel() : 0;
	}

	public void updateKetraVarka() {
		if (ItemFunctions.getItemCount(this, 7215) > 0) {
			_ketra = 5;
		} else if (ItemFunctions.getItemCount(this, 7214) > 0) {
			_ketra = 4;
		} else if (ItemFunctions.getItemCount(this, 7213) > 0) {
			_ketra = 3;
		} else if (ItemFunctions.getItemCount(this, 7212) > 0) {
			_ketra = 2;
		} else if (ItemFunctions.getItemCount(this, 7211) > 0) {
			_ketra = 1;
		} else if (ItemFunctions.getItemCount(this, 7225) > 0) {
			_varka = 5;
		} else if (ItemFunctions.getItemCount(this, 7224) > 0) {
			_varka = 4;
		} else if (ItemFunctions.getItemCount(this, 7223) > 0) {
			_varka = 3;
		} else if (ItemFunctions.getItemCount(this, 7222) > 0) {
			_varka = 2;
		} else if (ItemFunctions.getItemCount(this, 7221) > 0) {
			_varka = 1;
		} else {
			_varka = 0;
			_ketra = 0;
		}
	}

	public int getVarka() {
		return _varka;
	}

	public int getKetra() {
		return _ketra;
	}

	public void updateRam() {
		_ram = ItemFunctions.getItemCount(this, 7247) > 0 ? 2 : ItemFunctions.getItemCount(this, 7246) > 0 ? 1 : 0;
	}

	public int getRam() {
		return _ram;
	}

	public int getPledgeType() {
		return _pledgeType;
	}

	public void setPledgeType(int typeId) {
		_pledgeType = typeId;
	}

	public int getLvlJoinedAcademy() {
		return _lvlJoinedAcademy;
	}

	public void setLvlJoinedAcademy(int lvl) {
		_lvlJoinedAcademy = lvl;
	}

	public int getPledgeClass() {
		return _pledgeClass;
	}

	public EPledgeRank getPledgeRank() {
		return EPledgeRank.getPledgeRank(getPledgeClass());
	}

	public void updatePledgeClass() {
		int CLAN_LEVEL = _clan == null ? -1 : _clan.getLevel();
		boolean IN_ACADEMY = _clan != null && Clan.isAcademy(_pledgeType);
		boolean IS_GUARD = _clan != null && Clan.isRoyalGuard(_pledgeType);
		boolean IS_KNIGHT = _clan != null && Clan.isOrderOfKnights(_pledgeType);
		boolean IS_GUARD_CAPTAIN = false;
		boolean IS_KNIGHT_COMMANDER = false;
		boolean IS_LEADER = false;
		SubUnit unit = getSubUnit();
		if (unit != null) {
			UnitMember unitMember = unit.getUnitMember(getObjectId());
			if (unitMember == null) {
				_log.warn("Player: unitMember null, clan: " + _clan.getClanId() + "; pledgeType: " + unit.getType());
				return;
			}
			IS_GUARD_CAPTAIN = Clan.isRoyalGuard(unitMember.getLeaderOf());
			IS_KNIGHT_COMMANDER = Clan.isOrderOfKnights(unitMember.getLeaderOf());
			IS_LEADER = unitMember.getLeaderOf() == 0;
		}
		switch (CLAN_LEVEL) {
		case -1: {
			_pledgeClass = 0;
			break;
		}
		case 0:
		case 1:
		case 2:
		case 3: {
			if (IS_LEADER) {
				_pledgeClass = 2;
				break;
			}
			_pledgeClass = 1;
			break;
		}
		case 4: {
			if (IS_LEADER) {
				_pledgeClass = 3;
				break;
			}
			_pledgeClass = 2;
			break;
		}
		case 5: {
			if (IS_LEADER) {
				_pledgeClass = 4;
				break;
			}
			if (IN_ACADEMY) {
				_pledgeClass = 1;
				break;
			}
			_pledgeClass = 2;
			break;
		}
		case 6: {
			if (IS_LEADER) {
				_pledgeClass = 5;
				break;
			}
			if (IN_ACADEMY) {
				_pledgeClass = 1;
				break;
			}
			if (IS_GUARD_CAPTAIN) {
				_pledgeClass = 4;
				break;
			}
			if (IS_GUARD) {
				_pledgeClass = 2;
				break;
			}
			_pledgeClass = 3;
			break;
		}
		case 7: {
			if (IS_LEADER) {
				_pledgeClass = 7;
				break;
			}
			if (IN_ACADEMY) {
				_pledgeClass = 1;
				break;
			}
			if (IS_GUARD_CAPTAIN) {
				_pledgeClass = 6;
				break;
			}
			if (IS_GUARD) {
				_pledgeClass = 3;
				break;
			}
			if (IS_KNIGHT_COMMANDER) {
				_pledgeClass = 5;
				break;
			}
			if (IS_KNIGHT) {
				_pledgeClass = 2;
				break;
			}
			_pledgeClass = 4;
			break;
		}
		case 8: {
			if (IS_LEADER) {
				_pledgeClass = 8;
				break;
			}
			if (IN_ACADEMY) {
				_pledgeClass = 1;
				break;
			}
			if (IS_GUARD_CAPTAIN) {
				_pledgeClass = 7;
				break;
			}
			if (IS_GUARD) {
				_pledgeClass = 4;
				break;
			}
			if (IS_KNIGHT_COMMANDER) {
				_pledgeClass = 6;
				break;
			}
			if (IS_KNIGHT) {
				_pledgeClass = 3;
				break;
			}
			_pledgeClass = 5;
		}
		}
		if (_hero && _pledgeClass < 8) {
			_pledgeClass = 8;
		} else if (_noble && _pledgeClass < 5) {
			_pledgeClass = 5;
		}
	}

	public int getPowerGrade() {
		return _powerGrade;
	}

	public void setPowerGrade(int grade) {
		_powerGrade = grade;
	}

	public int getApprentice() {
		return _apprentice;
	}

	public void setApprentice(int apprentice) {
		_apprentice = apprentice;
	}

	public int getSponsor() {
		return _clan == null ? 0 : _clan.getAnyMember(getObjectId()).getSponsor();
	}

	public int getNameColor() {
		if (isInObserverMode()) {
			return Color.black.getRGB();
		}
		return _nameColor;
	}

	public void setNameColor(int nameColor) {
		if (nameColor != Config.NORMAL_NAME_COLOUR && nameColor != Config.CLANLEADER_NAME_COLOUR && nameColor != Config.GM_NAME_COLOUR && nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR) {
			setVar("namecolor", Integer.toHexString(nameColor), -1);
		} else if (nameColor == Config.NORMAL_NAME_COLOUR) {
			unsetVar("namecolor");
		}
		_nameColor = nameColor;
	}

	public void setVar(String name, String value, long expirationTime) {
		_vars.put(name, value);
		CharacterVariablesDAO.getInstance().setVar(getObjectId(), name, value, expirationTime);
	}

	public void setVar(String name, int value, long expirationTime) {
		setVar(name, String.valueOf(value), expirationTime);
	}

	public void setVar(String name, long value, long expirationTime) {
		setVar(name, String.valueOf(value), expirationTime);
	}

	public void unsetVar(String name) {
		if (name == null) {
			return;
		}
		if (_vars.remove(name) != null) {
			CharacterVariablesDAO.getInstance().deleteVar(getObjectId(), name);
		}
	}

	public String getVar(String name) {
		return _vars.getString(name, null);
	}

	public boolean getVarB(String name, boolean defaultVal) {
		String var = _vars.getString(name, null);
		if (var == null) {
			return defaultVal;
		}
		return !var.equals("0") && !var.equalsIgnoreCase("false");
	}

	public boolean getVarB(String name) {
		String var = _vars.getString(name, null);
		return var != null && !var.equals("0") && !var.equalsIgnoreCase("false");
	}

	public long getVarLong(String name) {
		return getVarLong(name, 0);
	}

	public long getVarLong(String name, long defaultVal) {
		long result = defaultVal;
		String var = getVar(name);
		if (var != null) {
			result = Long.parseLong(var);
		}
		return result;
	}

	public int getVarInt(String name) {
		return getVarInt(name, 0);
	}

	public int getVarInt(String name, int defaultVal) {
		int result = defaultVal;
		String var = getVar(name);
		if (var != null) {
			result = Integer.parseInt(var);
		}
		return result;
	}

	public MultiValueSet<String> getVars() {
		return _vars;
	}

	public String getLang() {
		return getVar("lang@");
	}

	public String getHWIDLock() {
		return getVar("hwidlock@");
	}

	public void setHWIDLock(String HWIDLock) {
		if (HWIDLock == null) {
			unsetVar("hwidlock@");
		} else {
			setVar("hwidlock@", HWIDLock, -1);
		}
	}

	public String getIPLock() {
		return getVar("iplock@");
	}

	public void setIPLock(String IPLock) {
		if (IPLock == null) {
			unsetVar("iplock@");
		} else {
			setVar("iplock@", IPLock, -1);
		}
	}

	public int getLangId() {
		String lang = getLang();
		if (lang.equalsIgnoreCase("en") || lang.equalsIgnoreCase("e") || lang.equalsIgnoreCase("eng")) {
			return 0;
		}
		if (lang.equalsIgnoreCase("ru") || lang.equalsIgnoreCase("r") || lang.equalsIgnoreCase("rus")) {
			return 1;
		}
		return -1;
	}

	public Language getLanguage() {
		String lang = getLang();
		if (lang == null || lang.equalsIgnoreCase("en") || lang.equalsIgnoreCase("e") || lang.equalsIgnoreCase("eng")) {
			return Language.ENGLISH;
		}
		if (lang.equalsIgnoreCase("ru") || lang.equalsIgnoreCase("r") || lang.equalsIgnoreCase("rus")) {
			return Language.RUSSIAN;
		}
		return Language.ENGLISH;
	}

	public boolean isLangRus() {
		return getLangId() == 1;
	}

	public int isAtWarWith(Integer id) {
		return _clan == null || !_clan.isAtWarWith(id) ? 0 : 1;
	}

	public int isAtWar() {
		return _clan == null || _clan.isAtWarOrUnderAttack() <= 0 ? 0 : 1;
	}

	public void stopWaterTask() {
		if (_taskWater != null) {
			_taskWater.cancel(false);
			_taskWater = null;
			sendPacket(new SetupGauge(this, 2, 0));
			sendChanges();
		}
	}

	public void startWaterTask() {
		if (isDead()) {
			stopWaterTask();
		} else if (Config.ALLOW_WATER && _taskWater == null) {
			int timeinwater = (int) (calcStat(Stats.BREATH, 86.0, null, null) * 1000.0);
			sendPacket(new SetupGauge(this, 2, timeinwater));
			if (getTransformation() > 0 && getTransformationTemplate() > 0 && !isCursedWeaponEquipped()) {
				setTransformation(0);
			}
			_taskWater = ThreadPoolManager.getInstance().scheduleAtFixedRate(new GameObjectTasks.WaterTask(this), timeinwater, 1000);
			sendChanges();
		}
	}

	public void doRevive(double percent) {
		restoreExp(percent);
		doRevive();
	}

	@Override
	public void doRevive() {
		super.doRevive();
		unsetVar("lostexp");
		updateEffectIcons();
		autoShot();
	}

	public void reviveRequest(Player reviver, double percent, boolean pet) {
		ReviveAnswerListener reviveAsk;
		ReviveAnswerListener reviveAnswerListener = reviveAsk = _askDialog != null && _askDialog.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener) _askDialog.getValue() : null;
		if (reviveAsk != null) {
			if (reviveAsk.isForPet() == pet && reviveAsk.getPower() >= percent) {
				reviver.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED);
				return;
			}
			if (pet && !reviveAsk.isForPet()) {
				reviver.sendPacket(Msg.SINCE_THE_MASTER_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_THE_PET_HAS_BEEN_CANCELLED);
				return;
			}
			if (pet && isDead()) {
				reviver.sendPacket(Msg.WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER);
				return;
			}
		}
		if (pet && getPet() != null && getPet().isDead() || !pet && isDead()) {
			ConfirmDlg pkt = new ConfirmDlg(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0);
			pkt.addName(reviver).addString("" + Math.round(percent) + " percent");
			ask(pkt, new ReviveAnswerListener(this, percent, pet));
		}
	}

	public void summonCharacterRequest(Creature summoner, Location loc, int summonConsumeCrystal) {
		ConfirmDlg cd = new ConfirmDlg(SystemMsg.C1_WISHES_TO_SUMMON_YOU_FROM_S2, 60000);
		cd.addName(summoner).addZoneName(loc);
		ask(cd, new SummonAnswerListener(this, loc, summonConsumeCrystal));
	}

	public void scriptRequest(String text, String scriptName, Object[] args) {
		ask(new ConfirmDlg(SystemMsg.S1, 30000).addString(text), new ScriptAnswerListener(this, scriptName, args, 30000));
	}

	public void updateNoChannel(long time) {
		setNoChannel(time);
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			String stmt = "UPDATE characters SET nochannel = ? WHERE obj_Id=?";
			statement = con.prepareStatement("UPDATE characters SET nochannel = ? WHERE obj_Id=?");
			statement.setLong(1, _NoChannel > 0 ? _NoChannel / 1000 : _NoChannel);
			statement.setInt(2, getObjectId());
			statement.executeUpdate();
		} catch (Exception e) {
			_log.warn("Could not activate nochannel:" + e);
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
		sendPacket(new EtcStatusUpdate(this));
	}

	public boolean canTalkWith(Player player) {
		return _NoChannel >= 0 || player == this;
	}

	public Deque<ChatMsg> getMessageBucket() {
		return _msgBucket;
	}

	@Override
	public boolean isInBoat() {
		return _boat != null;
	}

	public Boat getBoat() {
		return _boat;
	}

	public void setBoat(Boat boat) {
		_boat = boat;
	}

	@Override
	protected L2GameServerPacket stopMovePacket() {
		if (isInBoat()) {
			getBoat().inStopMovePacket(this);
		}
		return super.stopMovePacket();
	}

	public Location getInBoatPosition() {
		return _inBoatPosition;
	}

	public void setInBoatPosition(Location loc) {
		_inBoatPosition = loc;
	}

	public Map<Integer, SubClass> getSubClasses() {
		return _classlist;
	}

	public void setBaseClass(int baseClass) {
		_baseClass = baseClass;
	}

	public int getBaseClassId() {
		return _baseClass;
	}

	public SubClass getActiveClass() {
		return _activeClass;
	}

	public void setActiveClass(SubClass activeClass) {
		_activeClass = activeClass;
	}

	public int getActiveClassId() {
		return getActiveClass().getClassId();
	}

	public synchronized void changeClassInDb(int oldclass, int newclass) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE character_subclasses SET class_id=? WHERE char_obj_id=? AND class_id=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("UPDATE character_hennas SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("UPDATE character_shortcuts SET class_index=? WHERE object_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("UPDATE character_skills SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=? AND id=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("UPDATE character_effects_save SET id=? WHERE object_id=? AND id=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("UPDATE character_skills_save SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
		} catch (SQLException e) {
			_log.error("", e);
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void storeCharSubClasses() {
		SubClass main = getActiveClass();
		if (main != null) {
			main.setCp(getCurrentCp());
			main.setHp(getCurrentHp());
			main.setMp(getCurrentMp());
			main.setActive(true);
			getSubClasses().put(getActiveClassId(), main);
		} else {
			_log.warn("Could not store char sub data, main class " + getActiveClassId() + " not found for " + this);
		}
		Connection con = null;
		Statement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			StringBuilder sb;
			for (SubClass subClass : getSubClasses().values()) {
				sb = new StringBuilder("UPDATE character_subclasses SET ");
				sb.append("exp=").append(subClass.getExp()).append(",");
				sb.append("sp=").append(subClass.getSp()).append(",");
				sb.append("curHp=").append(subClass.getHp()).append(",");
				sb.append("curMp=").append(subClass.getMp()).append(",");
				sb.append("curCp=").append(subClass.getCp()).append(",");
				sb.append("level=").append(subClass.getLevel()).append(",");
				sb.append("active=").append(subClass.isActive() ? 1 : 0).append(",");
				sb.append("isBase=").append(subClass.isBase() ? 1 : 0).append(",");
				sb.append("death_penalty=").append(subClass.getDeathPenalty(this).getLevelOnSaveDB());
				sb.append(" WHERE char_obj_id=").append(getObjectId()).append(" AND class_id=").append(subClass.getClassId()).append(" LIMIT 1");
				statement.executeUpdate(sb.toString());
			}
			sb = new StringBuilder("UPDATE character_subclasses SET ");
			sb.append("maxHp=").append(getMaxHp()).append(",");
			sb.append("maxMp=").append(getMaxMp()).append(",");
			sb.append("maxCp=").append(getMaxCp());
			sb.append(" WHERE char_obj_id=").append(getObjectId()).append(" AND active=1 LIMIT 1");
			statement.executeUpdate(sb.toString());
		} catch (Exception e) {
			_log.warn("Could not store char sub data: " + e);
			_log.error("", e);
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean addSubClass(int classId, boolean storeOld) {
		if (_classlist.size() >= Config.ALT_GAME_BASE_SUB) {
			return false;
		}
		ClassId newId = ClassId.VALUES[classId];
		SubClass newClass = new SubClass();
		newClass.setBase(false);
		if (newId.getRace() == null) {
			return false;
		}
		newClass.setClassId(classId);
		_classlist.put(classId, newClass);
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO  `character_subclasses`  (\t`char_obj_id`,   `class_id`,   `exp`,   `sp`,   `curHp`,   `curMp`,   `curCp`,   `maxHp`,   `maxMp`,   `maxCp`,   `level`,   `active`,   `isBase`,   `death_penalty`)VALUES  (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newClass.getClassId());
			statement.setLong(3, newClass.getExp());
			statement.setInt(4, 0);
			statement.setDouble(5, getCurrentHp());
			statement.setDouble(6, getCurrentMp());
			statement.setDouble(7, getCurrentCp());
			statement.setDouble(8, getCurrentHp());
			statement.setDouble(9, getCurrentMp());
			statement.setDouble(10, getCurrentCp());
			statement.setInt(11, newClass.getLevel());
			statement.setInt(12, 0);
			statement.setInt(13, 0);
			statement.setInt(14, 0);
			statement.execute();
		} catch (Exception e) {
			_log.warn("Could not add character sub-class: " + e, e);
			boolean bl = false;
			return bl;
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
		setActiveSubClass(classId, storeOld);
		boolean countUnlearnable = true;
		int unLearnable = 0;
		Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
		while (skills.size() > unLearnable) {
			for (SkillLearn s : skills) {
				Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if (sk == null || !sk.getCanLearn(newId)) {
					if (!countUnlearnable)
						continue;
					++unLearnable;
					continue;
				}
				addSkill(sk, true);
			}
			countUnlearnable = false;
			skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
		}
		sendPacket(new SkillList(this));
		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		setCurrentCp(getMaxCp());
		return true;
	}

	public boolean modifySubClass(int oldClassId, int newClassId) {
		SubClass originalClass = _classlist.get(oldClassId);
		if (originalClass == null || originalClass.isBase()) {
			return false;
		}
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=? AND class_id=? AND isBase = 0");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=? AND id=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
		} catch (Exception e) {
			_log.warn("Could not delete char sub-class: " + e);
			_log.error("", e);
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
		_classlist.remove(oldClassId);
		return newClassId <= 0 || addSubClass(newClassId, false);
	}

	public void setActiveSubClass(int subId, boolean store) {
		SubClass sub = getSubClasses().get(subId);
		if (sub == null) {
			return;
		}
		try {
			if (getActiveClass() != null) {
				String qn = QuestManager.getQuest(422).getName();
				QuestState qs = getQuestState(qn);
				EffectsDAO.getInstance().insert(this);
				storeDisableSkills();
				if (QuestManager.getQuest(422) != null && qn != null && qs != null) {
					qs.exitCurrentQuest(true);
				}
			}
		} catch (Exception e) {
			_log.warn("", e);
		}
		SubClass oldsub = getActiveClass();
		if (oldsub != null) {
			oldsub.setActive(false);
			if (store) {
				oldsub.setCp(getCurrentCp());
				oldsub.setHp(getCurrentHp());
				oldsub.setMp(getCurrentMp());
				getSubClasses().put(getActiveClassId(), oldsub);
			}
		}
		sub.setActive(true);
		setActiveClass(sub);
		getSubClasses().put(getActiveClassId(), sub);
		setClassId(subId, false, false);
		removeAllSkills();
		getEffectList().stopAllEffects();
		if (getPet() != null && (getPet().isSummon() || Config.ALT_IMPROVED_PETS_LIMITED_USE && (getPet().getNpcId() == 16035 && !isMageClass() || getPet().getNpcId() == 16034 && isMageClass()))) {
			getPet().unSummon();
		}
		setAgathion(0);
		restoreSkills();
		if (Config.ALT_SUBLASS_SKILL_TRANSFER && getBaseClassId() == subId) {
			for (SubClass ssc : getSubClasses().values()) {
				if (ssc.getClassId() == subId)
					continue;
				restoreSkills(ssc.getClassId());
			}
		}
		rewardSkills(false);
		checkSkills();
		refreshExpertisePenalty();
		sendPacket(new SkillList(this));
		getInventory().refreshEquip();
		getInventory().validateItems();
		for (int i = 0; i < 3; ++i) {
			_henna[i] = null;
		}
		restoreHenna();
		sendPacket(new HennaInfo(this));
		EffectsDAO.getInstance().restoreEffects(this);
		restoreDisableSkills();
		setCurrentHpMp(sub.getHp(), sub.getMp());
		setCurrentCp(sub.getCp());
		_shortCuts.restore();
		sendPacket(new ShortCutInit(this));
		Iterator<Integer> i = getAutoSoulShot().iterator();
		while (i.hasNext()) {
			int shotId = i.next();
			sendPacket(new ExAutoSoulShot(shotId, true));
		}
		sendPacket(new SkillCoolTime(this));
		broadcastPacket(new SocialAction(getObjectId(), 15));
		getDeathPenalty().restore(this);
		setIncreasedForce(0);
		startHourlyTask();
		broadcastCharInfo();
		updateEffectIcons();
		updateStats();
	}

	public void startKickTask(long delayMillis) {
		stopKickTask();
		_kickTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.KickTask(this), delayMillis);
	}

	public void stopKickTask() {
		if (_kickTask != null) {
			_kickTask.cancel(false);
			_kickTask = null;
		}
	}

	public void startBonusTask() {
		if (Config.SERVICES_RATE_ENABLED) {
			AccountBonusDAO.getInstance().load(getAccountName(), getBonus());
			long bonusExpireTime = getBonus().getBonusExpire();
			if (bonusExpireTime > System.currentTimeMillis() / 1000) {
				if (_bonusExpiration == null) {
					_bonusExpiration = LazyPrecisionTaskManager.getInstance().startBonusExpirationTask(this);
				}
			} else if (bonusExpireTime > 0) {
				AccountBonusDAO.getInstance().delete(getAccountName());
			}
		}
	}

	public void stopBonusTask() {
		if (_bonusExpiration != null) {
			_bonusExpiration.cancel(false);
			_bonusExpiration = null;
		}
	}

	@Override
	public int getInventoryLimit() {
		return (int) calcStat(Stats.INVENTORY_LIMIT, 0.0, null, null);
	}

	public int getWarehouseLimit() {
		return (int) calcStat(Stats.STORAGE_LIMIT, 0.0, null, null);
	}

	public int getTradeLimit() {
		return (int) calcStat(Stats.TRADE_LIMIT, 0.0, null, null);
	}

	public int getDwarvenRecipeLimit() {
		return (int) calcStat(Stats.DWARVEN_RECIPE_LIMIT, 50.0, null, null) + Config.ALT_ADD_RECIPES;
	}

	public int getCommonRecipeLimit() {
		return (int) calcStat(Stats.COMMON_RECIPE_LIMIT, 50.0, null, null) + Config.ALT_ADD_RECIPES;
	}

	public Element getAttackElement() {
		return Formulas.getAttackElement(this, null);
	}

	public int getAttack(Element element) {
		if (element == Element.NONE) {
			return 0;
		}
		return (int) calcStat(element.getAttack(), 0.0, null, null);
	}

	public int getDefence(Element element) {
		if (element == Element.NONE) {
			return 0;
		}
		return (int) calcStat(element.getDefence(), 0.0, null, null);
	}

	public boolean getAndSetLastItemAuctionRequest() {
		if (_lastItemAuctionInfoRequest + 2000 < System.currentTimeMillis()) {
			_lastItemAuctionInfoRequest = System.currentTimeMillis();
			return true;
		}
		_lastItemAuctionInfoRequest = System.currentTimeMillis();
		return false;
	}

	@Override
	public int getNpcId() {
		return -2;
	}

	public GameObject getVisibleObject(int id) {
		if (getObjectId() == id) {
			return this;
		}
		GameObject target = null;
		if (getTargetId() == id) {
			target = getTarget();
		}
		if (target == null && _party != null) {
			for (Player p : _party.getPartyMembers()) {
				if (p == null || p.getObjectId() != id)
					continue;
				target = p;
				break;
			}
		}
		if (target == null) {
			target = World.getAroundObjectById(this, id);
		}
		return target == null || target.isInvisible() ? null : target;
	}

	@Override
	public int getPAtk(Creature target) {
		double init = getActiveWeaponInstance() == null ? (double) (isMageClass() ? 3 : 4) : 0.0;
		return (int) calcStat(Stats.POWER_ATTACK, init, target, null);
	}

	@Override
	public int getPDef(Creature target) {
		double init = 4.0;
		ItemInstance chest = getInventory().getPaperdollItem(10);
		if (chest == null) {
			init += isMageClass() ? 15.0 : 31.0;
		}
		if (getInventory().getPaperdollItem(11) == null && (chest == null || chest.getBodyPart() != 32768)) {
			init += isMageClass() ? 8.0 : 18.0;
		}
		if (getInventory().getPaperdollItem(6) == null) {
			init += 12.0;
		}
		if (getInventory().getPaperdollItem(9) == null) {
			init += 8.0;
		}
		if (getInventory().getPaperdollItem(12) == null) {
			init += 7.0;
		}
		return (int) calcStat(Stats.POWER_DEFENCE, init, target, null);
	}

	@Override
	public int getMDef(Creature target, Skill skill) {
		double init = 0.0;
		if (getInventory().getPaperdollItem(2) == null) {
			init += 9.0;
		}
		if (getInventory().getPaperdollItem(1) == null) {
			init += 9.0;
		}
		if (getInventory().getPaperdollItem(3) == null) {
			init += 13.0;
		}
		if (getInventory().getPaperdollItem(5) == null) {
			init += 5.0;
		}
		if (getInventory().getPaperdollItem(4) == null) {
			init += 5.0;
		}
		return (int) calcStat(Stats.MAGIC_DEFENCE, init, target, skill);
	}

	public boolean isSubClassActive() {
		return getBaseClassId() != getActiveClassId();
	}

	@Override
	public String getTitle() {
		return super.getTitle();
	}

	public int getTitleColor() {
		return _titlecolor;
	}

	public void setTitleColor(int titlecolor) {
		if (titlecolor != 16777079) {
			setVar("titlecolor", Integer.toHexString(titlecolor), -1);
		} else {
			unsetVar("titlecolor");
		}
		_titlecolor = titlecolor;
	}

	public String getDisconnectedTitle() {
		return _disconnectedTitle;
	}

	public void setDisconnectedTitle(String disconnectedTitle) {
		_disconnectedTitle = disconnectedTitle;
	}

	public int getDisconnectedTitleColor() {
		return _disconnectedTitleColor;
	}

	public void setDisconnectedTitleColor(int disconnectedTitleColor) {
		_disconnectedTitleColor = disconnectedTitleColor;
	}

	@Override
	public boolean isCursedWeaponEquipped() {
		return _cursedWeaponEquippedId != 0;
	}

	public int getCursedWeaponEquippedId() {
		return _cursedWeaponEquippedId;
	}

	public void setCursedWeaponEquippedId(int value) {
		_cursedWeaponEquippedId = value;
	}

	@Override
	public boolean isImmobilized() {
		return super.isImmobilized() || isOverloaded() || isSitting() || isFishing();
	}

	@Override
	public boolean isBlocked() {
		return super.isBlocked() || isInMovie() || isInObserverMode() || isTeleporting() || isLogoutStarted();
	}

	@Override
	public boolean isInvul() {
		return super.isInvul() || isInMovie() || getAfterTeleportPortectionTime() > System.currentTimeMillis();
	}

	public boolean isResurectProhibited() {
		return _resurect_prohibited;
	}

	public void setResurectProhibited(boolean prohibited) {
		_resurect_prohibited = prohibited;
	}

	public boolean isOverloaded() {
		return _overloaded;
	}

	public void setOverloaded(boolean overloaded) {
		_overloaded = overloaded;
	}

	public boolean isFishing() {
		return _isFishing;
	}

	public Fishing getFishing() {
		return _fishing;
	}

	public void setFishing(boolean value) {
		_isFishing = value;
	}

	public void startFishing(FishTemplate fish, int lureId) {
		_fishing.setFish(fish);
		_fishing.setLureId(lureId);
		_fishing.startFishing();
	}

	public void stopFishing() {
		_fishing.stopFishing();
	}

	public Location getFishLoc() {
		return _fishing.getFishLoc();
	}

	public Bonus getBonus() {
		return _bonus;
	}

	public boolean hasBonus() {
		return _bonus.getBonusExpire() > System.currentTimeMillis() / 1000;
	}

	@Override
	public double getRateAdena() {
		return calcStat(Stats.ADENA_REWARD_MULTIPLIER, _party == null ? (double) _bonus.getDropAdena() : _party._rateAdena);
	}

	@Override
	public double getRateItems() {
		return calcStat(Stats.ITEM_REWARD_MULTIPLIER, _party == null ? (double) _bonus.getDropItems() : _party._rateDrop);
	}

	@Override
	public double getRateExp() {
		return calcStat(Stats.EXP, _party == null ? (double) _bonus.getRateXp() : _party._rateExp, null, null);
	}

	@Override
	public double getRateSp() {
		return calcStat(Stats.SP, _party == null ? (double) _bonus.getRateSp() : _party._rateSp, null, null);
	}

	@Override
	public double getRateSpoil() {
		return calcStat(Stats.SPOIL_REWARD_MULTIPLIER, _party == null ? (double) _bonus.getDropSpoil() : _party._rateSpoil);
	}

	public boolean isMaried() {
		return _maried;
	}

	public void setMaried(boolean state) {
		_maried = state;
	}

	public boolean isMaryRequest() {
		return _maryrequest;
	}

	public void setMaryRequest(boolean state) {
		_maryrequest = state;
	}

	public boolean isMaryAccepted() {
		return _maryaccepted;
	}

	public void setMaryAccepted(boolean state) {
		_maryaccepted = state;
	}

	public int getPartnerId() {
		return _partnerId;
	}

	public void setPartnerId(int partnerid) {
		_partnerId = partnerid;
	}

	public int getCoupleId() {
		return _coupleId;
	}

	public void setCoupleId(int coupleId) {
		_coupleId = coupleId;
	}

	public boolean isUndying() {
		return _isUndying;
	}

	public void setUndying(boolean val) {
		if (!isGM()) {
			return;
		}
		_isUndying = val;
	}

	public void resetReuse() {
		_skillReuses.clear();
		_sharedGroupReuses.clear();
	}

	public DeathPenalty getDeathPenalty() {
		return _activeClass == null ? null : _activeClass.getDeathPenalty(this);
	}

	public boolean isCharmOfCourage() {
		return _charmOfCourage;
	}

	public void setCharmOfCourage(boolean val) {
		_charmOfCourage = val;
		if (!val) {
			getEffectList().stopEffect(5041);
		}
		sendEtcStatusUpdate();
	}

	@Override
	public int getIncreasedForce() {
		return _increasedForce;
	}

	@Override
	public void setIncreasedForce(int i) {
		if (_increasedForce == i) {
			return;
		}
		i = Math.min(i, 7);
		if ((i = Math.max(i, 0)) != 0 && i > _increasedForce) {
			_increasedForceLastUpdateTimeStamp = System.currentTimeMillis();
			if (_increasedForceCleanupTask == null) {
				_increasedForceCleanupTask = ThreadPoolManager.getInstance().schedule(new ForceCleanupTask(), 600000);
			}
			sendPacket(new SystemMessage(323).addNumber(i));
		}
		_increasedForce = i;
		sendEtcStatusUpdate();
	}

	@Override
	public int getConsumedSouls() {
		return _consumedSouls;
	}

	@Override
	public void setConsumedSouls(int i, NpcInstance monster) {
		if (i == _consumedSouls) {
			return;
		}
		int max = (int) calcStat(Stats.SOULS_LIMIT, 0.0, monster, null);
		if (i > max) {
			i = max;
		}
		if (i <= 0) {
			_consumedSouls = 0;
			sendEtcStatusUpdate();
			return;
		}
		if (_consumedSouls != i) {
			int diff = i - _consumedSouls;
			if (diff > 0) {
				SystemMessage sm = new SystemMessage(2162);
				sm.addNumber(diff);
				sm.addNumber(i);
				sendPacket(sm);
			}
		} else if (max == i) {
			sendPacket(Msg.SOUL_CANNOT_BE_ABSORBED_ANY_MORE);
			return;
		}
		_consumedSouls = i;
		sendPacket(new EtcStatusUpdate(this));
	}

	public boolean isFalling() {
		return System.currentTimeMillis() - _lastFalling < 5000;
	}

	public void falling(int height) {
		if (!Config.DAMAGE_FROM_FALLING || isDead() || isFlying() || isInWater() || isInBoat()) {
			return;
		}
		_lastFalling = System.currentTimeMillis();
		int damage = (int) calcStat(Stats.FALL, (double) getMaxHp() / 2000.0 * (double) height, null, null);
		if (damage > 0) {
			int curHp = (int) getCurrentHp();
			if (curHp - damage < 1) {
				setCurrentHp(1.0, false);
			} else {
				setCurrentHp(curHp - damage, false);
			}
			sendPacket(new SystemMessage(296).addNumber(damage));
		}
	}

	@Override
	public void checkHpMessages(double curHp, double newHp) {
		int[] _hp = {
			30,
			30 };
		int[] skills = {
			290,
			291 };
		double percent = getMaxHp() / 100;
		double _curHpPercent = curHp / percent;
		double _newHpPercent = newHp / percent;
		boolean needsUpdate = false;
		for (int i = 0; i < skills.length; ++i) {
			int level = getSkillLevel(skills[i]);
			if (level <= 0)
				continue;
			if (_curHpPercent > (double) _hp[i] && _newHpPercent <= (double) _hp[i]) {
				sendPacket(new SystemMessage(1133).addSkillName(skills[i], level));
				needsUpdate = true;
				continue;
			}
			if (_curHpPercent > (double) _hp[i] || _newHpPercent <= (double) _hp[i])
				continue;
			sendPacket(new SystemMessage(1134).addSkillName(skills[i], level));
			needsUpdate = true;
		}
		if (needsUpdate) {
			sendChanges();
		}
	}

	public void checkDayNightMessages() {
		int level = getSkillLevel(294);
		if (level > 0) {
			if (GameTimeController.getInstance().isNowNight()) {
				sendPacket(new SystemMessage(1131).addSkillName(294, level));
			} else {
				sendPacket(new SystemMessage(1132).addSkillName(294, level));
			}
		}
		sendChanges();
	}

	public int getZoneMask() {
		return _zoneMask;
	}

	@Override
	public boolean updateZones() {
		if (!super.updateZones()) {
			return false;
		}
		boolean lastInCombatZone = (_zoneMask & 16384) == 16384;
		boolean lastInDangerArea = (_zoneMask & 256) == 256;
		boolean lastOnSiegeField = (_zoneMask & 2048) == 2048;
		boolean lastInPeaceZone = (_zoneMask & 4096) == 4096;
		boolean isInCombatZone = isInCombatZone();
		boolean isInDangerArea = isInDangerArea();
		boolean isOnSiegeField = isOnSiegeField() || isInZone(Zone.ZoneType.fun);
		boolean isInPeaceZone = isInPeaceZone();
		boolean isInSSQZone = isInSSQZone();
		int lastZoneMask = _zoneMask;
		_zoneMask = 0;
		if (isInCombatZone) {
			_zoneMask |= 16384;
		}
		if (isInDangerArea) {
			_zoneMask |= 256;
		}
		if (isOnSiegeField) {
			_zoneMask |= 2048;
		}
		if (isInPeaceZone) {
			_zoneMask |= 4096;
		}
		if (isInSSQZone) {
			_zoneMask |= 8192;
		}
		if (lastZoneMask != _zoneMask) {
			sendPacket(new ExSetCompassZoneCode(this));
		}
		if (lastInCombatZone != isInCombatZone) {
			broadcastRelationChanged();
		}
		if (lastInDangerArea != isInDangerArea) {
			sendPacket(new EtcStatusUpdate(this));
		}
		if (lastOnSiegeField != isOnSiegeField) {
			broadcastRelationChanged();
			if (isOnSiegeField) {
				sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ENTERED_A_COMBAT_ZONE));
			} else {
				sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_LEFT_A_COMBAT_ZONE));
				if (!isTeleporting() && getPvpFlag() == 0) {
					startPvPFlag(null);
				}
			}
		}
		FlagItemAttachment attachment;
		if (isInPeaceZone && !lastInPeaceZone && (attachment = getActiveWeaponFlagAttachment()) != null) {
			attachment.onEnterPeace(this);
		}
		if (isInWater()) {
			startWaterTask();
		} else {
			stopWaterTask();
		}
		return true;
	}

	public void startAutoSaveTask() {
		if (!Config.AUTOSAVE) {
			return;
		}
		if (_autoSaveTask == null) {
			_autoSaveTask = AutoSaveManager.getInstance().addAutoSaveTask(this);
		}
	}

	public void stopAutoSaveTask() {
		if (_autoSaveTask != null) {
			_autoSaveTask.cancel(false);
		}
		_autoSaveTask = null;
	}

	public void startPcBangPointsTask() {
		if (!Config.ALT_PCBANG_POINTS_ENABLED || Config.ALT_PCBANG_POINTS_DELAY <= 0) {
			return;
		}
		if (_pcCafePointsTask == null) {
			_pcCafePointsTask = LazyPrecisionTaskManager.getInstance().addPCCafePointsTask(this);
		}
	}

	public void stopPcBangPointsTask() {
		if (_pcCafePointsTask != null) {
			_pcCafePointsTask.cancel(false);
		}
		_pcCafePointsTask = null;
	}

	public void startUnjailTask(Player player, int time) {
		if (_unjailTask != null) {
			_unjailTask.cancel(false);
		}
		_unjailTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.UnJailTask(player), time * 60000);
	}

	public void stopUnjailTask() {
		if (_unjailTask != null) {
			_unjailTask.cancel(false);
		}
		_unjailTask = null;
	}

	@Override
	public void sendMessage(String message) {
		sendPacket(new SystemMessage(message));
	}

	public Location getLastClientPosition() {
		return _lastClientPosition;
	}

	public void setLastClientPosition(Location position) {
		_lastClientPosition = position;
	}

	public Location getLastServerPosition() {
		return _lastServerPosition;
	}

	public void setLastServerPosition(Location position) {
		_lastServerPosition = position;
	}

	public int getUseSeed() {
		return _useSeed;
	}

	public void setUseSeed(int id) {
		_useSeed = id;
	}

	public int getRelation(Player target) {
		int result = 0;
		if (getClan() != null) {
			result |= 64;
		}
		if (isClanLeader()) {
			result |= 128;
		}
		Party party;
		if ((party = getParty()) != null && party == target.getParty()) {
			result |= 32;
			switch (party.getPartyMembers().indexOf(this)) {
			case 0: {
				result |= 16;
				break;
			}
			case 1: {
				result |= 8;
				break;
			}
			case 2: {
				result |= 7;
				break;
			}
			case 3: {
				result |= 6;
				break;
			}
			case 4: {
				result |= 5;
				break;
			}
			case 5: {
				result |= 4;
				break;
			}
			case 6: {
				result |= 3;
				break;
			}
			case 7: {
				result |= 2;
				break;
			}
			case 8: {
				result |= 1;
			}
			}
		}
		Clan clan1 = getClan();
		Clan clan2 = target.getClan();
		if (clan1 != null && clan2 != null && target.getPledgeType() != -1 && getPledgeType() != -1 && clan2.isAtWarWith(clan1.getClanId())) {
			result |= 65536;
			if (clan1.isAtWarWith(clan2.getClanId())) {
				result |= 32768;
			}
		}
		for (GlobalEvent e : getEvents()) {
			result = e.getRelation(this, target, result);
		}
		return result;
	}

	public long getlastPvpAttack() {
		return _lastPvpAttack;
	}

	@Override
	public void startPvPFlag(Creature target) {
		if (_karma > 0) {
			return;
		}
		long startTime = System.currentTimeMillis();
		if (target != null && target.getPvpFlag() != 0) {
			startTime -= (long) Math.max(0, Config.PVP_TIME - Config.PVP_FLAG_ON_UN_FLAG_TIME);
		}
		if (_pvpFlag != 0 && _lastPvpAttack > startTime) {
			return;
		}
		_lastPvpAttack = startTime;
		updatePvPFlag(1);
		if (_PvPRegTask == null) {
			_PvPRegTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new GameObjectTasks.PvPFlagTask(this), 1000, 1000);
		}
	}

	public void stopPvPFlag() {
		if (_PvPRegTask != null) {
			_PvPRegTask.cancel(false);
			_PvPRegTask = null;
		}
		updatePvPFlag(0);
	}

	public void updatePvPFlag(int value) {
		if (_pvpFlag == value) {
			return;
		}
		setPvpFlag(value);
		sendStatusUpdate(true, true, 26);
		broadcastRelationChanged();
	}

	@Override
	public int getPvpFlag() {
		return _pvpFlag;
	}

	public void setPvpFlag(int pvpFlag) {
		_pvpFlag = pvpFlag;
	}

	public boolean isInDuel() {
		return getEvent(DuelEvent.class) != null;
	}

	public TamedBeastInstance getTrainedBeast() {
		return _tamedBeast;
	}

	public void setTrainedBeast(TamedBeastInstance tamedBeast) {
		_tamedBeast = tamedBeast;
	}

	public long getLastAttackPacket() {
		return _lastAttackPacket;
	}

	public void setLastAttackPacket() {
		_lastAttackPacket = System.currentTimeMillis();
	}

	public byte[] getKeyBindings() {
		return _keyBindings;
	}

	public void setKeyBindings(byte[] keyBindings) {
		if (keyBindings == null) {
			keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		_keyBindings = keyBindings;
	}

	private void preparateToTransform(Skill transSkill) {
		if (transSkill == null || !transSkill.isBaseTransformation()) {
			for (Effect effect : getEffectList().getAllEffects()) {
				if (effect == null || !effect.getSkill().isToggle())
					continue;
				effect.exit();
			}
		}
	}

	public boolean isInFlyingTransform() {
		return _transformationId == 8 || _transformationId == 9 || _transformationId == 260;
	}

	public boolean isInMountTransform() {
		return _transformationId == 106 || _transformationId == 109 || _transformationId == 110 || _transformationId == 20001;
	}

	public int getTransformation() {
		return _transformationId;
	}

	public void setTransformation(int transformationId) {
		if (transformationId == _transformationId || _transformationId != 0 && transformationId != 0) {
			return;
		}
		if (transformationId == 0) {
			for (Effect effect : getEffectList().getAllEffects()) {
				if (effect == null || effect.getEffectType() != EffectType.Transformation || effect.calc() == 0.0)
					continue;
				effect.exit();
				preparateToTransform(effect.getSkill());
				break;
			}
			if (!_transformationSkills.isEmpty()) {
				for (Skill s : _transformationSkills.values()) {
					if (s.isCommon() || SkillAcquireHolder.getInstance().isSkillPossible(this, s) || s.isHeroic())
						continue;
					removeSkill(s);
				}
				_transformationSkills.clear();
			}
		} else {
			if (!isCursedWeaponEquipped()) {
				for (Effect effect : getEffectList().getAllEffects()) {
					if (effect == null || effect.getEffectType() != EffectType.Transformation)
						continue;
					if (effect.getSkill() instanceof Transformation && ((Transformation) effect.getSkill()).isDisguise) {
						for (Skill s : getAllSkills()) {
							if (s == null || !s.isActive() && !s.isToggle())
								continue;
							_transformationSkills.put(s.getId(), s);
						}
					} else {
						for (Skill.AddedSkill s : effect.getSkill().getAddedSkills()) {
							if (s.level == 0) {
								int s2 = getSkillLevel(s.id);
								if (s2 <= 0)
									continue;
								_transformationSkills.put(s.id, SkillTable.getInstance().getInfo(s.id, s2));
								continue;
							}
							if (s.level == -2) {
								int learnLevel = Math.max(effect.getSkill().getMagicLevel(), 40);
								int maxLevel = SkillTable.getInstance().getBaseLevel(s.id);
								int curSkillLevel = 1;
								if (maxLevel > 3)
									curSkillLevel += getLevel() - learnLevel;
								else
									curSkillLevel += (getLevel() - learnLevel) / ((76 - learnLevel) / maxLevel);

								curSkillLevel = Math.min(Math.max(curSkillLevel, 1), maxLevel);
								_transformationSkills.put(s.id, SkillTable.getInstance().getInfo(s.id, curSkillLevel));
								continue;
							}
							_transformationSkills.put(s.id, s.getSkill());
						}
					}
					preparateToTransform(effect.getSkill());
					break;
				}
			} else {
				preparateToTransform(null);
			}
			if (!isOlyParticipant() && isCursedWeaponEquipped() && _hero && getBaseClassId() == getActiveClassId()) {
				_transformationSkills.put(395, SkillTable.getInstance().getInfo(395, 1));
				_transformationSkills.put(396, SkillTable.getInstance().getInfo(396, 1));
				_transformationSkills.put(1374, SkillTable.getInstance().getInfo(1374, 1));
				_transformationSkills.put(1375, SkillTable.getInstance().getInfo(1375, 1));
				_transformationSkills.put(1376, SkillTable.getInstance().getInfo(1376, 1));
			}
			for (Skill s : _transformationSkills.values()) {
				addSkill(s, false);
			}
		}
		_transformationId = transformationId;
		sendPacket(new ExBasicActionList(this));
		sendPacket(new SkillList(this));
		sendPacket(new ShortCutInit(this));
		Iterator iterator = getAutoSoulShot().iterator();
		while (iterator.hasNext()) {
			int shotId = (Integer) iterator.next();
			sendPacket(new ExAutoSoulShot(shotId, true));
		}
		broadcastUserInfo(true);
	}

	public String getTransformationName() {
		return _transformationName;
	}

	public void setTransformationName(String name) {
		_transformationName = name;
	}

	public String getTransformationTitle() {
		return _transformationTitle;
	}

	public void setTransformationTitle(String transformationTitle) {
		_transformationTitle = transformationTitle;
	}

	public int getTransformationTemplate() {
		return _transformationTemplate;
	}

	public void setTransformationTemplate(int template) {
		_transformationTemplate = template;
	}

	@Override
	public final Collection<Skill> getAllSkills() {
		if (_transformationId == 0) {
			return super.getAllSkills();
		}
		HashMap<Integer, Skill> tempSkills = new HashMap<>();
		for (Skill s : super.getAllSkills()) {
			if (s == null || s.isActive() || s.isToggle())
				continue;
			tempSkills.put(s.getId(), s);
		}
		tempSkills.putAll(_transformationSkills);
		return tempSkills.values();
	}

	public void setAgathion(int id) {
		if (_agathionId == id) {
			return;
		}
		_agathionId = id;
		broadcastCharInfo();
	}

	public int getAgathionId() {
		return _agathionId;
	}

	public int getPcBangPoints() {
		return _pcBangPoints;
	}

	public void setPcBangPoints(int val) {
		_pcBangPoints = val;
	}

	public void addPcBangPoints(int count, boolean doublePoints) {
		if (doublePoints) {
			count *= 2;
		}
		_pcBangPoints += count;
		sendPacket(new SystemMessage(doublePoints ? 1708 : 1707).addNumber(count));
		sendPacket(new ExPCCafePointInfo(this, count, 1, 2, 12));
	}

	public boolean reducePcBangPoints(int count) {
		if (_pcBangPoints < count) {
			return false;
		}
		_pcBangPoints -= count;
		sendPacket(new SystemMessage(1709).addNumber(count));
		sendPacket(new ExPCCafePointInfo(this, 0, 1, 2, 12));
		return true;
	}

	public Location getGroundSkillLoc() {
		return _groundSkillLoc;
	}

	public void setGroundSkillLoc(Location location) {
		_groundSkillLoc = location;
	}

	public boolean isLogoutStarted() {
		return _isLogout.get();
	}

	public void setOfflineMode(boolean val) {
		if (!val) {
			unsetVar("offline");
		}
		_offline = val;
	}

	public boolean isInOfflineMode() {
		return _offline;
	}

	public void saveTradeList() {
		String val = "";
		if (_sellList == null || _sellList.isEmpty()) {
			unsetVar("selllist");
		} else {
			for (TradeItem i : _sellList) {
				val = val + i.getObjectId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			}
			setVar("selllist", val, -1);
			val = "";
			if (_tradeList != null && getSellStoreName() != null) {
				setVar("sellstorename", getSellStoreName(), -1);
			}
		}
		if (_packageSellList == null || _packageSellList.isEmpty()) {
			unsetVar("packageselllist");
		} else {
			for (TradeItem i : _packageSellList) {
				val = val + i.getObjectId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			}
			setVar("packageselllist", val, -1);
			val = "";
			if (_tradeList != null && getSellStoreName() != null) {
				setVar("sellstorename", getSellStoreName(), -1);
			}
		}
		if (_buyList == null || _buyList.isEmpty()) {
			unsetVar("buylist");
		} else {
			for (TradeItem i : _buyList) {
				val = val + i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			}
			setVar("buylist", val, -1);
			val = "";
			if (_tradeList != null && getBuyStoreName() != null) {
				setVar("buystorename", getBuyStoreName(), -1);
			}
		}
		if (_createList == null || _createList.isEmpty()) {
			unsetVar("createlist");
		} else {
			for (ManufactureItem i : _createList) {
				val = val + i.getRecipeId() + ";" + i.getCost() + ":";
			}
			setVar("createlist", val, -1);
			if (getManufactureName() != null) {
				setVar("manufacturename", getManufactureName(), -1);
			}
		}
	}

	public void restoreTradeList() {
		int oId;
		ItemInstance itemToSell;
		long count;
		long price;
		String[] values;
		TradeItem i;
		String var = getVar("selllist");
		if (var != null) {
			_sellList = new CopyOnWriteArrayList<>();
			String[] items = var.split(":");
			for (String item : items) {
				if (item.equals("") || (values = item.split(";")).length < 3)
					continue;
				oId = Integer.parseInt(values[0]);
				count = Long.parseLong(values[1]);
				price = Long.parseLong(values[2]);
				itemToSell = getInventory().getItemByObjectId(oId);
				if (count < 1 || itemToSell == null)
					continue;
				if (count > itemToSell.getCount()) {
					count = itemToSell.getCount();
				}
				i = new TradeItem(itemToSell);
				i.setCount(count);
				i.setOwnersPrice(price);
				_sellList.add(i);
			}
			var = getVar("sellstorename");
			if (var != null) {
				setSellStoreName(var);
			}
		}
		if ((var = getVar("packageselllist")) != null) {
			_packageSellList = new CopyOnWriteArrayList<>();
			String[] items = var.split(":");
			for (String item : items) {
				if (item.equals("") || (values = item.split(";")).length < 3)
					continue;
				oId = Integer.parseInt(values[0]);
				count = Long.parseLong(values[1]);
				price = Long.parseLong(values[2]);
				itemToSell = getInventory().getItemByObjectId(oId);
				if (count < 1 || itemToSell == null)
					continue;
				if (count > itemToSell.getCount()) {
					count = itemToSell.getCount();
				}
				i = new TradeItem(itemToSell);
				i.setCount(count);
				i.setOwnersPrice(price);
				_packageSellList.add(i);
			}
			var = getVar("sellstorename");
			if (var != null) {
				setSellStoreName(var);
			}
		}
		if ((var = getVar("buylist")) != null) {
			_buyList = new CopyOnWriteArrayList<>();
			String[] items = var.split(":");
			for (String item : items) {
				if (item.equals("") || (values = item.split(";")).length < 3)
					continue;
				TradeItem i2 = new TradeItem();
				i2.setItemId(Integer.parseInt(values[0]));
				i2.setCount(Long.parseLong(values[1]));
				i2.setOwnersPrice(Long.parseLong(values[2]));
				_buyList.add(i2);
			}
			var = getVar("buystorename");
			if (var != null) {
				setBuyStoreName(var);
			}
		}
		if ((var = getVar("createlist")) != null) {
			_createList = new CopyOnWriteArrayList<>();
			String[] items = var.split(":");
			for (String item : items) {
				if (item.equals("") || (values = item.split(";")).length < 2)
					continue;
				int recId = Integer.parseInt(values[0]);
				long price2 = Long.parseLong(values[1]);
				if (!findRecipe(recId))
					continue;
				_createList.add(new ManufactureItem(recId, price2));
			}
			var = getVar("manufacturename");
			if (var != null) {
				setManufactureName(var);
			}
		}
	}

	public void restoreRecipeBook() {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();
			while (rset.next()) {
				int recipeId = rset.getInt("id");
				Recipe recipe = RecipeHolder.getInstance().getRecipeById(recipeId);
				registerRecipe(recipe, false);
			}
		} catch (Exception e) {
			_log.warn("count not recipe skills:" + e);
			_log.error("", e);
		} finally {
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public int getMountType() {
		switch (getMountNpcId()) {
		case 12526:
		case 12527:
		case 12528:
		case 16038:
		case 16039:
		case 16040:
		case 16068: {
			return 1;
		}
		case 12621: {
			return 2;
		}
		case 16037:
		case 16041:
		case 16042: {
			return 3;
		}
		}
		return 0;
	}

	@Override
	public double getColRadius() {
		int mountTemplate;
		NpcTemplate mountNpcTemplate;
		if (getTransformation() != 0) {
			NpcTemplate npcTemplate;
			int template = getTransformationTemplate();
			if (template != 0 && (npcTemplate = NpcHolder.getInstance().getTemplate(template)) != null) {
				return npcTemplate.collisionRadius;
			}
		} else if (isMounted() && (mountTemplate = getMountNpcId()) != 0 && (mountNpcTemplate = NpcHolder.getInstance().getTemplate(mountTemplate)) != null) {
			return mountNpcTemplate.collisionRadius;
		}
		return getBaseTemplate().collisionRadius;
	}

	@Override
	public double getColHeight() {
		int mountTemplate;
		NpcTemplate mountNpcTemplate;
		if (getTransformation() != 0) {
			NpcTemplate npcTemplate;
			int template = getTransformationTemplate();
			if (template != 0 && (npcTemplate = NpcHolder.getInstance().getTemplate(template)) != null) {
				return npcTemplate.collisionHeight;
			}
		} else if (isMounted() && (mountTemplate = getMountNpcId()) != 0 && (mountNpcTemplate = NpcHolder.getInstance().getTemplate(mountTemplate)) != null) {
			return mountNpcTemplate.collisionHeight;
		}
		return getBaseTemplate().collisionHeight;
	}

	@Override
	public void setReflection(Reflection reflection) {
		if (getReflection() == reflection) {
			return;
		}
		super.setReflection(reflection);
		if (_summon != null && !_summon.isDead()) {
			_summon.setReflection(reflection);
		}
		if (reflection != ReflectionManager.DEFAULT) {
			String var = getVar("reflection");
			if (var == null || !var.equals(String.valueOf(reflection.getId()))) {
				setVar("reflection", String.valueOf(reflection.getId()), -1);
			}
		} else {
			unsetVar("reflection");
		}
		if (getActiveClass() != null) {
			getInventory().validateItems();
			if (getPet() != null && (getPet().getNpcId() == 14916 || getPet().getNpcId() == 14917)) {
				getPet().unSummon();
			}
		}
	}

	public int getBuyListId() {
		return _buyListId;
	}

	public void setBuyListId(int listId) {
		_buyListId = listId;
	}

	public int getIncorrectValidateCount() {
		return _incorrectValidateCount;
	}

	public int setIncorrectValidateCount(int count) {
		_incorrectValidateCount = count;
		return _incorrectValidateCount;
	}

	public int getExpandInventory() {
		return _expandInventory;
	}

	public void setExpandInventory(int inventory) {
		_expandInventory = inventory;
	}

	public int getExpandWarehouse() {
		return _expandWarehouse;
	}

	public void setExpandWarehouse(int warehouse) {
		_expandWarehouse = warehouse;
	}

	public void enterMovieMode() {
		if (isInMovie()) {
			return;
		}
		setTarget(null);
		stopMove();
		setIsInMovie(true);
		sendPacket(new CameraMode(1));
	}

	public void leaveMovieMode() {
		setIsInMovie(false);
		sendPacket(new CameraMode(0));
		broadcastCharInfo();
	}

	public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration) {
		sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration));
	}

	public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int unk) {
		sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration, turn, rise, widescreen, unk));
	}

	public int getMovieId() {
		return _movieId;
	}

	public void setMovieId(int id) {
		_movieId = id;
	}

	public boolean isInMovie() {
		return _isInMovie;
	}

	public void setIsInMovie(boolean state) {
		_isInMovie = state;
	}

	public void showQuestMovie(SceneMovie movie) {
		if (isInMovie()) {
			return;
		}
		sendActionFailed();
		setTarget(null);
		stopMove();
		setMovieId(movie.getId());
		setIsInMovie(true);
		sendPacket(movie.packet(this));
	}

	public void showQuestMovie(int movieId) {
		if (isInMovie()) {
			return;
		}
		sendActionFailed();
		setTarget(null);
		stopMove();
		setMovieId(movieId);
		setIsInMovie(true);
		sendPacket(new ExStartScenePlayer(movieId));
	}

	public void setAutoLoot(boolean enable) {
		if (Config.AUTO_LOOT_INDIVIDUAL) {
			_autoLoot = enable;
			setVar("AutoLoot", String.valueOf(enable), -1);
		}
	}

	public void setAutoLootHerbs(boolean enable) {
		if (Config.AUTO_LOOT_INDIVIDUAL) {
			AutoLootHerbs = enable;
			setVar("AutoLootHerbs", String.valueOf(enable), -1);
		}
	}

	public void setAutoLootAdena(boolean enable) {
		if (Config.AUTO_LOOT_INDIVIDUAL) {
			AutoLootAdena = enable;
			setVar("AutoLootAdend", String.valueOf(enable), -1);
		}
	}

	public boolean isAutoLootEnabled() {
		return _autoLoot;
	}

	public boolean isAutoLootHerbsEnabled() {
		return AutoLootHerbs;
	}

	public boolean isAutoLootAdenaEnabled() {
		return AutoLootAdena;
	}

	public final void reName(String name, boolean saveToDB) {
		setName(name);
		if (saveToDB) {
			saveNameToDB();
		}
		if (isNoble()) {
			NoblesController.getInstance().renameNoble(getObjectId(), name);
		}
	}

	public final void reName(String name) {
		reName(name, false);
	}

	public final void saveNameToDB() {
		Connection con = null;
		PreparedStatement st = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("UPDATE characters SET char_name = ? WHERE obj_Id = ?");
			st.setString(1, getName());
			st.setInt(2, getObjectId());
			st.executeUpdate();
		} catch (Exception e) {
			_log.error("", e);
		} finally {
			DbUtils.closeQuietly(con, st);
		}
	}

	@Override
	public Player getPlayer() {
		return this;
	}

	public int getTalismanCount() {
		return (int) calcStat(Stats.TALISMANS_LIMIT, 0.0, null, null);
	}

	public final void disableDrop(int time) {
		_dropDisabled = System.currentTimeMillis() + (long) time;
	}

	public final boolean isDropDisabled() {
		return _dropDisabled > System.currentTimeMillis();
	}

	public void setPetControlItem(int itemObjId) {
		setPetControlItem(getInventory().getItemByObjectId(itemObjId));
	}

	public ItemInstance getPetControlItem() {
		return _petControlItem;
	}

	public void setPetControlItem(ItemInstance item) {
		_petControlItem = item;
	}

	public boolean isActive() {
		return isActive.get();
	}

	public void setActive() {
		setNonAggroTime(0);
		if (isActive.getAndSet(true)) {
			return;
		}
		onActive();
	}

	private void onActive() {
		setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONLOGIN);
		if (getPetControlItem() != null) {
			ThreadPoolManager.getInstance().execute(new RunnableImpl() {

				@Override
				public void runImpl() {
					if (getPetControlItem() != null) {
						summonPet();
					}
				}
			});
		}
	}

	public void summonPet() {
		if (getPet() != null) {
			return;
		}
		ItemInstance controlItem = getPetControlItem();
		if (controlItem == null) {
			return;
		}
		int npcId = PetDataTable.getSummonId(controlItem);
		if (npcId == 0) {
			return;
		}
		NpcTemplate petTemplate = NpcHolder.getInstance().getTemplate(npcId);
		if (petTemplate == null) {
			return;
		}
		PetInstance pet = PetInstance.restore(controlItem, petTemplate, this);
		if (pet == null) {
			return;
		}
		setPet(pet);
		pet.setTitle(getName());
		if (!pet.isRespawned()) {
			pet.setCurrentHp(pet.getMaxHp(), false);
			pet.setCurrentMp(pet.getMaxMp());
			pet.setCurrentFed(pet.getMaxFed());
			pet.updateControlItem();
			pet.store();
		}
		pet.getInventory().restore();
		pet.setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
		pet.setReflection(getReflection());
		pet.spawnMe(Location.findPointToStay(this, 50, 70));
		pet.setRunning();
		pet.setFollowMode(true);
		pet.getInventory().validateItems();
		if (pet instanceof PetBabyInstance) {
			((PetBabyInstance) pet).startBuffTask();
		}
	}

	public Collection<TrapInstance> getTraps() {
		if (_traps == null) {
			return null;
		}
		ArrayList<TrapInstance> result = new ArrayList<>(getTrapsCount());
		for (Integer trapId : _traps.keySet()) {
			TrapInstance trap = (TrapInstance) GameObjectsStorage.get(_traps.get(trapId));
			if (trap != null) {
				result.add(trap);
				continue;
			}
			_traps.remove(trapId);
		}
		return result;
	}

	public int getTrapsCount() {
		return _traps == null ? 0 : _traps.size();
	}

	public void addTrap(TrapInstance trap) {
		if (_traps == null) {
			_traps = new HashMap<>();
		}
		_traps.put(trap.getObjectId(), trap.getStoredId());
	}

	public void removeTrap(TrapInstance trap) {
		Map<Integer, Long> traps = _traps;
		if (traps == null || traps.isEmpty()) {
			return;
		}
		traps.remove(trap.getObjectId());
	}

	public void destroyFirstTrap() {
		Map<Integer, Long> traps = _traps;
		if (traps == null || traps.isEmpty()) {
			return;
		}
		Iterator<Integer> iterator = traps.keySet().iterator();
		if (iterator.hasNext()) {
			Integer trapId = iterator.next();
			TrapInstance trap = (TrapInstance) GameObjectsStorage.get(traps.get(trapId));
			if (trap != null) {
				trap.deleteMe();
				return;
			}
			return;
		}
	}

	public void destroyAllTraps() {
		Map<Integer, Long> traps = _traps;
		if (traps == null || traps.isEmpty()) {
			return;
		}
		ArrayList<TrapInstance> toRemove = new ArrayList<>();
		for (Integer trapId : traps.keySet()) {
			toRemove.add((TrapInstance) GameObjectsStorage.get(traps.get(trapId)));
		}
		for (TrapInstance t : toRemove) {
			if (t == null)
				continue;
			t.deleteMe();
		}
	}

	@Override
	public PlayerListenerList getListeners() {
		if (listeners == null) {
			Player player = this;
			synchronized (player) {
				if (listeners == null) {
					listeners = new PlayerListenerList(this);
				}
			}
		}
		return (PlayerListenerList) listeners;
	}

	@Override
	public PlayerStatsChangeRecorder getStatsRecorder() {
		if (_statsRecorder == null) {
			Player player = this;
			synchronized (player) {
				if (_statsRecorder == null) {
					_statsRecorder = new PlayerStatsChangeRecorder(this);
				}
			}
		}
		return (PlayerStatsChangeRecorder) _statsRecorder;
	}

	public int getHoursInGame() {
		++_hoursInGame;
		return _hoursInGame;
	}

	public void startHourlyTask() {
		_hourlyTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new GameObjectTasks.HourlyTask(this), 3600000, 3600000);
	}

	public void stopHourlyTask() {
		if (_hourlyTask != null) {
			_hourlyTask.cancel(false);
			_hourlyTask = null;
		}
	}

	public long getPremiumPoints() {
		if (Config.GAME_POINT_ITEM_ID > 0) {
			return ItemFunctions.getItemCount(this, Config.GAME_POINT_ITEM_ID);
		}
		return 0;
	}

	public void reducePremiumPoints(int val) {
		if (Config.GAME_POINT_ITEM_ID > 0) {
			ItemFunctions.removeItem(this, Config.GAME_POINT_ITEM_ID, val, true);
		}
	}

	public String getSessionVar(String key) {
		if (_userSession == null) {
			return null;
		}
		return _userSession.get(key);
	}

	public void setSessionVar(String key, String val) {
		if (_userSession == null) {
			_userSession = new ConcurrentHashMap<>();
		}
		if (val == null || val.isEmpty()) {
			_userSession.remove(key);
		} else {
			_userSession.put(key, val);
		}
	}

	public FriendList getFriendList() {
		return _friendList;
	}

	public boolean isNotShowTraders() {
		return _notShowTraders;
	}

	public void setNotShowTraders(boolean notShowTraders) {
		_notShowTraders = notShowTraders;
	}

	public boolean isDebug() {
		return _debug;
	}

	public void setDebug(boolean b) {
		_debug = b;
	}

	public void sendItemList(boolean show) {
		ItemInstance[] items = getInventory().getItems();
		LockType lockType = getInventory().getLockType();
		int[] lockItems = getInventory().getLockItems();
		sendPacket(new ItemList(items.length, items, show, lockType, lockItems));
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	@Override
	public void startAttackStanceTask() {
		startAttackStanceTask0();
		Summon summon = getPet();
		if (summon != null) {
			summon.startAttackStanceTask0();
		}
	}

	@Override
	public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic) {
		super.displayGiveDamageMessage(target, damage, crit, miss, shld, magic);
		if (crit) {
			if (magic) {
				sendPacket(new SystemMessage(SystemMessage.MAGIC_CRITICAL_HIT));
			} else {
				sendPacket(new SystemMessage(44));
			}
		}
		if (miss) {
			sendPacket(new SystemMessage(43));
		} else if (!target.isDamageBlocked()) {
			sendPacket(new SystemMessage(35).addNumber(damage));
		}
		if (target.isPlayer()) {
			if (shld && damage > 1) {
				target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
			} else if (shld && damage == 1) {
				target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
			}
		}
	}

	@Override
	public void displayReceiveDamageMessage(Creature attacker, int damage) {
		if (attacker != this) {
			sendPacket(new SystemMessage(36).addName(attacker).addNumber((long) damage));
		}
	}

	public IntObjectMap<String> getPostFriends() {
		return _postFriends;
	}

	public boolean isSharedGroupDisabled(int groupId) {
		TimeStamp sts = _sharedGroupReuses.get(groupId);
		if (sts == null) {
			return false;
		}
		if (sts.hasNotPassed()) {
			return true;
		}
		_sharedGroupReuses.remove(groupId);
		return false;
	}

	public TimeStamp getSharedGroupReuse(int groupId) {
		return _sharedGroupReuses.get(groupId);
	}

	public void addSharedGroupReuse(int group, TimeStamp stamp) {
		_sharedGroupReuses.put(group, stamp);
	}

	public Collection<IntObjectMap.Entry<TimeStamp>> getSharedGroupReuses() {
		return _sharedGroupReuses.entrySet();
	}

	public void sendReuseMessage(ItemInstance item) {
		TimeStamp sts = getSharedGroupReuse(item.getTemplate().getReuseGroup());
		if (sts == null || !sts.hasNotPassed()) {
			sendPacket(new SystemMessage(48).addItemName(item.getTemplate().getItemId()));
		}
	}

	public void ask(ConfirmDlg dlg, OnAnswerListener listener) {
		if (_askDialog != null) {
			return;
		}
		int rnd = Rnd.nextInt();
		_askDialog = new ImmutablePair<>(rnd, listener);
		dlg.setRequestId(rnd);
		sendPacket(dlg);
	}

	public Pair<Integer, OnAnswerListener> getAskListener(boolean clear) {
		if (!clear) {
			return _askDialog;
		}
		Pair<Integer, OnAnswerListener> ask = _askDialog;
		_askDialog = null;
		return ask;
	}

	@Override
	public boolean isDead() {
		return isOlyParticipant() || isInDuel() ? getCurrentHp() <= 1.0 : super.isDead();
	}

	public boolean hasPrivilege(Privilege privilege) {
		return _clan != null && (getClanPrivileges() & privilege.mask()) == privilege.mask();
	}

	public MatchingRoom getMatchingRoom() {
		return _matchingRoom;
	}

	public void setMatchingRoom(MatchingRoom matchingRoom) {
		_matchingRoom = matchingRoom;
	}

	public Mentoring getMentoring() {
		return _mentroring;
	}

	public void setMentroring(Mentoring mentoring) {
		_mentroring = mentoring;
	}

	public void dispelBuffs() {
		for (Effect e : getEffectList().getAllEffects()) {
			if (e.getSkill().isOffensive() || e.getSkill().isNewbie() || !e.isCancelable() || e.getSkill().isPreservedOnDeath())
				continue;
			sendPacket(new SystemMessage(749).addSkillName(e.getSkill().getId(), e.getSkill().getLevel()));
			e.exit();
		}
		if (getPet() != null) {
			for (Effect e : getPet().getEffectList().getAllEffects()) {
				if (e.getSkill().isOffensive() || e.getSkill().isNewbie() || !e.isCancelable() || e.getSkill().isPreservedOnDeath())
					continue;
				e.exit();
			}
		}
	}

	public void setInstanceReuse(int id, long time) {
		CustomMessage msg = new CustomMessage("INSTANT_ZONE_FROM_HERE__S1_S_ENTRY_HAS_BEEN_RESTRICTED_YOU_CAN_CHECK_THE_NEXT_ENTRY_POSSIBLE", this).addString(getName());
		sendMessage(msg);
		_instancesReuses.put(id, time);
		mysql.set("REPLACE INTO character_instances (obj_id, id, reuse) VALUES (?,?,?)", getObjectId(), id, time);
	}

	public void removeInstanceReuse(int id) {
		if (_instancesReuses.remove(id) != null) {
			mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=? AND `id`=? LIMIT 1", getObjectId(), id);
		}
	}

	public void removeAllInstanceReuses() {
		_instancesReuses.clear();
		mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=?", getObjectId());
	}

	public void removeInstanceReusesByGroupId(int groupId) {
		Iterator<Integer> iterator = InstantZoneHolder.getInstance().getSharedReuseInstanceIdsByGroup(groupId).iterator();
		while (iterator.hasNext()) {
			int i = iterator.next();
			if (getInstanceReuse(i) == null)
				continue;
			removeInstanceReuse(i);
		}
	}

	public Long getInstanceReuse(int id) {
		return _instancesReuses.get(id);
	}

	public Map<Integer, Long> getInstanceReuses() {
		return _instancesReuses;
	}

	private void loadInstanceReuses() {
		Connection con = null;
		PreparedStatement offline = null;
		ResultSet rs = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT * FROM character_instances WHERE obj_id = ?");
			offline.setInt(1, getObjectId());
			rs = offline.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				long reuse = rs.getLong("reuse");
				_instancesReuses.put(id, reuse);
			}
		} catch (Exception e) {
			_log.error("", e);
		} finally {
			DbUtils.closeQuietly(con, offline, rs);
		}
	}

	public Reflection getActiveReflection() {
		for (Reflection r : ReflectionManager.getInstance().getAll()) {
			if (r == null || !ArrayUtils.contains(r.getVisitors(), getObjectId()))
				continue;
			return r;
		}
		return null;
	}

	public boolean canEnterInstance(int instancedZoneId) {
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
		if (isDead()) {
			return false;
		}
		if (ReflectionManager.getInstance().size() > Config.MAX_REFLECTIONS_COUNT) {
			sendMessage(new CustomMessage("THE_MAXIMUM_NUMBER_OF_INSTANCE_ZONES_HAS_BEEN_EXCEEDED", this));
			return false;
		}
		if (iz == null) {
			sendPacket(SystemMsg.SYSTEM_ERROR);
			return false;
		}
		if (ReflectionManager.getInstance().getCountByIzId(instancedZoneId) >= iz.getMaxChannels()) {
			sendMessage(new CustomMessage("THE_MAXIMUM_NUMBER_OF_INSTANCE_ZONES_HAS_BEEN_EXCEEDED", this));
			return false;
		}
		return iz.getEntryType().canEnter(this, iz);
	}

	public boolean canReenterInstance(int instancedZoneId) {
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
		if (getActiveReflection() != null && getActiveReflection().getInstancedZoneId() != instancedZoneId) {
			sendMessage(new CustomMessage("YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON", this));
			return false;
		}
		if (iz.isDispelBuffs()) {
			dispelBuffs();
		}
		return iz.getEntryType().canReEnter(this, iz);
	}

	public int getBattlefieldChatId() {
		return _battlefieldChatId;
	}

	public void setBattlefieldChatId(int battlefieldChatId) {
		_battlefieldChatId = battlefieldChatId;
	}

	@Override
	public void broadCast(IStaticPacket... packet) {
		sendPacket(packet);
	}

	@Override
	public Iterator<Player> iterator() {
		return Collections.singleton(this).iterator();
	}

	public PlayerGroup getPlayerGroup() {
		if (getParty() != null) {
			if (getParty().getCommandChannel() != null) {
				return getParty().getCommandChannel();
			}
			return getParty();
		}
		return this;
	}

	public boolean isActionBlocked(String action) {
		return _blockedActions.contains(action);
	}

	public void blockActions(String... actions) {
		Collections.addAll(_blockedActions, actions);
	}

	public void unblockActions(String... actions) {
		for (String action : actions) {
			_blockedActions.remove(action);
		}
	}

	public void addRadar(int x, int y, int z) {
		sendPacket(new RadarControl(0, 1, x, y, z));
	}

	public void addRadarWithMap(int x, int y, int z) {
		sendPacket(new RadarControl(0, 2, x, y, z));
	}

	public long getAfterTeleportPortectionTime() {
		return _afterTeleportPortectionTime;
	}

	public void setAfterTeleportPortectionTime(long afterTeleportPortectionTime) {
		_afterTeleportPortectionTime = afterTeleportPortectionTime;
	}

	public enum EPledgeRank {
		VAGABOND(0),
		VASSAL(1),
		HEIR(2),
		KNIGHT(3),
		WISEMAN(4),
		BARON(5),
		VISCOUNT(6),
		COUNT(7),
		MARQUIS(8);

		public static EPledgeRank[] VALUES;

		static {
			VALUES = EPledgeRank.values();
		}

		private final int _rankId;

		EPledgeRank(int rankId) {
			_rankId = rankId;
		}

		public static EPledgeRank getPledgeRank(int pledgeRankId) {
			for (EPledgeRank pledgeRank : VALUES) {
				if (pledgeRank.getRankId() != pledgeRankId)
					continue;
				return pledgeRank;
			}
			return null;
		}

		public int getRankId() {
			return _rankId;
		}
	}

	private static class MoveToLocationActionForOffload extends Creature.MoveToLocationAction{
		public MoveToLocationActionForOffload(Creature actor, Location moveFrom, Location moveTo, boolean ignoreGeo, int indent, boolean pathFind) {
			super(actor, moveFrom, moveTo, ignoreGeo, indent, pathFind);
		}

		private void tryOffloadedMove() {
			Player player = (Player) getActor();
			MoveToLocationOffloadData mtlOffloadData;
			if (player != null && (mtlOffloadData = player._mtlOffloadData.get()) != null && player._mtlOffloadData.compareAndSet(mtlOffloadData, null)) {
				player.moveToLocation(mtlOffloadData.getDest(), mtlOffloadData.getIndent(), mtlOffloadData.isPathfind());
			}
		}

		@Override
		protected boolean onTick(double done) {
			boolean result;
			try {
				result = super.onTick(done);
			} finally {
				tryOffloadedMove();
			}
			return result;
		}

		@Override
		protected void onFinish(boolean finishedWell, boolean isInterrupted) {
			try {
				super.onFinish(finishedWell, isInterrupted);
			} finally {
				tryOffloadedMove();
			}
		}
	}

	private static class MoveToLocationOffloadData{
		private final Location _dest;
		private final int _indent;
		private final boolean _pathfind;

		public MoveToLocationOffloadData(Location dest, int indent, boolean pathfind) {
			_dest = dest;
			_indent = indent;
			_pathfind = pathfind;
		}

		public Location getDest() {
			return _dest;
		}

		public int getIndent() {
			return _indent;
		}

		public boolean isPathfind() {
			return _pathfind;
		}
	}

	private class ForceCleanupTask implements Runnable{
		@Override
		public void run() {
			long nextDelay = 600000 - (System.currentTimeMillis() - _increasedForceLastUpdateTimeStamp);
			if (nextDelay > 1000) {
				_increasedForceCleanupTask = ThreadPoolManager.getInstance().schedule(new ForceCleanupTask(), nextDelay);
				return;
			}
			_increasedForce = 0;
			sendEtcStatusUpdate();
			_increasedForceCleanupTask = null;
		}
	}

	private class UserInfoTask extends RunnableImpl{
		@Override
		public void runImpl() throws Exception {
			sendUserInfoImpl();
			_userInfoTask = null;
		}
	}

	public class BroadcastCharInfoTask extends RunnableImpl{
		@Override
		public void runImpl() throws Exception {
			broadcastCharInfoImpl();
			_broadcastCharInfoTask = null;
		}
	}

	private class UpdateEffectIcons extends RunnableImpl{
		@Override
		public void runImpl() throws Exception {
			updateEffectIconsImpl();
			_updateEffectIconsTask = null;
		}
	}

}
