package l2.gameserver.model.instances;

import gnu.trove.TIntObjectIterator;
import l2.commons.collections.MultiValueSet;
import l2.commons.lang.reference.HardReference;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CharacterAI;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.data.xml.holder.MultiSellHolder;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.data.xml.holder.SkillAcquireHolder;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.handler.items.RefineryHandler;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.instancemanager.DimensionalRiftManager;
import l2.gameserver.instancemanager.QuestManager;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.listener.NpcListener;
import l2.gameserver.model.*;
import l2.gameserver.model.actor.listener.NpcListenerList;
import l2.gameserver.model.actor.recorder.NpcStatsChangeRecorder;
import l2.gameserver.model.base.AcquireType;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.entity.DimensionalRift;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestEventType;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.*;
import l2.gameserver.scripts.Events;
import l2.gameserver.stats.Stats;
import l2.gameserver.tables.ClanTable;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.taskmanager.DecayTaskManager;
import l2.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.templates.item.WeaponTemplate;
import l2.gameserver.templates.npc.Faction;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.templates.spawn.SpawnRange;
import l2.gameserver.utils.HtmlUtils;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.ReflectionUtils;
import l2.gameserver.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

public class NpcInstance extends Creature
{
	public static final String NO_CHAT_WINDOW = "noChatWindow";
	public static final String NO_RANDOM_WALK = "noRandomWalk";
	public static final String IGNORE_DROP_DIFF = "ignoreDropLevelDiff";
	public static final String NO_RANDOM_ANIMATION = "noRandomAnimation";
	public static final String TARGETABLE = "TargetEnabled";
	public static final String SHOW_NAME = "showName";
	private static final Logger _log = LoggerFactory.getLogger(NpcInstance.class);
	private final AggroList _aggroList;
	protected int _spawnAnimation = 2;
	protected boolean _hasRandomAnimation;
	protected boolean _hasRandomWalk;
	protected boolean _hasChatWindow;
	protected boolean _ignoreDropDiffPenalty;
	protected boolean _unAggred;
	protected long _lastSocialAction;
	private int _personalAggroRange = -1;
	private int _level;
	private long _dieTime;
	private int _currentLHandId;
	private int _currentRHandId;
	private double _currentCollisionRadius;
	private double _currentCollisionHeight;
	private int npcState;
	private Future<?> _decayTask;
	private Future<?> _animationTask;
	private boolean _isTargetable;
	private boolean _showName;
	private Castle _nearestCastle;
	private ClanHall _nearestClanHall;
	private Spawner _spawn;
	private Location _spawnedLoc = new Location();
	private SpawnRange _spawnRange;
	private MultiValueSet<String> _parameters = StatsSet.EMPTY;
	private int _displayId;
	private ScheduledFuture<?> _broadcastCharInfoTask;
	private boolean _isBusy;
	private String _busyMessage = "";
	private boolean _isUnderground;
	
	public NpcInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		if(template == null)
		{
			throw new NullPointerException("No template for Npc. Please check your datapack is setup correctly.");
		}
		setParameters(template.getAIParams());
		_hasRandomAnimation = !getParameter("noRandomAnimation", false) && Config.MAX_NPC_ANIMATION > 0;
		_hasRandomWalk = !getParameter("noRandomWalk", false);
		_ignoreDropDiffPenalty = getParameter("ignoreDropLevelDiff", false);
		setHasChatWindow(!getParameter("noChatWindow", false));
		setTargetable(getParameter("TargetEnabled", true));
		setShowName(getParameter("showName", true));
		if(template.getSkills().size() > 0)
		{
			TIntObjectIterator iterator = template.getSkills().iterator();
			while(iterator.hasNext())
			{
				iterator.advance();
				addSkill((Skill) iterator.value());
			}
		}
		setName(template.name);
		setTitle(template.title);
		setLHandId(getTemplate().lhand);
		setRHandId(getTemplate().rhand);
		setCollisionHeight(getTemplate().collisionHeight);
		setCollisionRadius(getTemplate().collisionRadius);
		_aggroList = new AggroList(this);
		setFlying(getParameter("isFlying", false));
	}
	
	public static boolean canBypassCheck(Player player, NpcInstance npc)
	{
		if(npc == null || player.isActionsDisabled() || !Config.ALLOW_TALK_WHILE_SITTING && player.isSitting() || !npc.isInActingRange(player))
		{
			player.sendActionFailed();
			return false;
		}
		return true;
	}
	
	public static void showFishingSkillList(Player player)
	{
		showAcquireList(AcquireType.FISHING, player);
	}
	
	public static void showClanSkillList(Player player)
	{
		if(player.getClan() == null || !player.isClanLeader())
		{
			player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			player.sendActionFailed();
			return;
		}
		showAcquireList(AcquireType.CLAN, player);
	}
	
	public static void showAcquireList(AcquireType t, Player player)
	{
		Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, t);
		AcquireSkillList asl = new AcquireSkillList(t, skills.size());
		for(SkillLearn s : skills)
		{
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 0);
		}
		if(skills.size() == 0)
		{
			player.sendPacket(AcquireSkillDone.STATIC);
			player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
		else
		{
			player.unsetVar("AcquireSkillClassId");
			player.sendPacket(asl);
		}
		player.sendActionFailed();
	}
	
	@Override
	public HardReference<NpcInstance> getRef()
	{
		return (HardReference<NpcInstance>) super.getRef();
	}
	
	@Override
	public CharacterAI getAI()
	{
		if(_ai == null)
		{
			NpcInstance npcInstance = this;
			synchronized(npcInstance)
			{
				if(_ai == null)
				{
					_ai = getTemplate().getNewAI(this);
				}
			}
		}
		return _ai;
	}
	
	public Location getSpawnedLoc()
	{
		return _spawnedLoc;
	}
	
	public void setSpawnedLoc(Location loc)
	{
		_spawnedLoc = loc;
	}
	
	public int getRightHandItem()
	{
		return _currentRHandId;
	}
	
	public int getLeftHandItem()
	{
		return _currentLHandId;
	}
	
	public void setLHandId(int newWeaponId)
	{
		_currentLHandId = newWeaponId;
	}
	
	public void setRHandId(int newWeaponId)
	{
		_currentRHandId = newWeaponId;
	}
	
	public double getCollisionHeight()
	{
		return _currentCollisionHeight;
	}
	
	public void setCollisionHeight(double offset)
	{
		_currentCollisionHeight = offset;
	}
	
	public double getCollisionRadius()
	{
		return _currentCollisionRadius;
	}
	
	public void setCollisionRadius(double collisionRadius)
	{
		_currentCollisionRadius = collisionRadius;
	}
	
	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(attacker.isPlayable())
		{
			getAggroList().addDamageHate(attacker, (int) damage, 0);
		}
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		_dieTime = System.currentTimeMillis();
		if(isMonster() && (((MonsterInstance) this).isSeeded() || ((MonsterInstance) this).isSpoiled()))
		{
			startDecay(20000);
		}
		else if(isBoss())
		{
			startDecay(20000);
		}
		else if(isFlying())
		{
			startDecay(4500);
		}
		else
		{
			startDecay(8500);
		}
		setLHandId(getTemplate().lhand);
		setRHandId(getTemplate().rhand);
		setCollisionHeight(getTemplate().collisionHeight);
		setCollisionRadius(getTemplate().collisionRadius);
		getAI().stopAITask();
		stopRandomAnimation();
		super.onDeath(killer);
	}
	
	public long getDeadTime()
	{
		if(_dieTime <= 0)
		{
			return 0;
		}
		return System.currentTimeMillis() - _dieTime;
	}
	
	public AggroList getAggroList()
	{
		return _aggroList;
	}
	
	public MinionList getMinionList()
	{
		return null;
	}
	
	public Location getRndMinionPosition()
	{
		return Location.findPointToStay(this, (int) getColRadius() + 30, (int) getColRadius() + 50);
	}
	
	public boolean hasMinions()
	{
		return false;
	}
	
	public void dropItem(Player lastAttacker, int itemId, long itemCount)
	{
		if(itemCount == 0 || lastAttacker == null)
		{
			return;
		}
		for(long i = 0;i < itemCount;++i)
		{
			ItemInstance item = ItemFunctions.createItem(itemId);
			for(GlobalEvent e : getEvents())
			{
				item.addEvent(e);
			}
			if(item.isStackable())
			{
				i = itemCount;
				item.setCount(itemCount);
			}
			if(isRaid() || this instanceof ReflectionBossInstance)
			{
				SystemMessage2 sm;
				if(itemId == 57)
				{
					sm = new SystemMessage2(SystemMsg.C1_HAS_DIED_AND_DROPPED_S2_ADENA);
					sm.addName(this);
					sm.addLong(item.getCount());
				}
				else
				{
					sm = new SystemMessage2(SystemMsg.C1_DIED_AND_DROPPED_S3_S2);
					sm.addName(this);
					sm.addItemName(itemId);
					sm.addLong(item.getCount());
				}
				broadcastPacket(sm);
			}
			lastAttacker.doAutoLootOrDrop(item, this);
		}
	}
	
	public void dropItem(Player lastAttacker, ItemInstance item)
	{
		if(item.getCount() == 0)
		{
			return;
		}
		if(isRaid() || this instanceof ReflectionBossInstance)
		{
			SystemMessage2 sm;
			if(item.getItemId() == 57)
			{
				sm = new SystemMessage2(SystemMsg.C1_HAS_DIED_AND_DROPPED_S2_ADENA);
				sm.addName(this);
				sm.addLong(item.getCount());
			}
			else
			{
				sm = new SystemMessage2(SystemMsg.C1_DIED_AND_DROPPED_S3_S2);
				sm.addName(this);
				sm.addItemName(item.getItemId());
				sm.addLong(item.getCount());
			}
			broadcastPacket(sm);
		}
		lastAttacker.doAutoLootOrDrop(item, this);
	}
	
	@Override
	public boolean isAttackable(Creature attacker)
	{
		return true;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
	
	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		_dieTime = 0;
		_spawnAnimation = 0;
		if(getAI().isGlobalAI() || getCurrentRegion() != null && getCurrentRegion().isActive())
		{
			getAI().startAITask();
			startRandomAnimation();
		}
		ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(this, CtrlEvent.EVT_SPAWN));
		getListeners().onSpawn();
	}
	
	@Override
	protected void onDespawn()
	{
		getAggroList().clear();
		getAI().onEvtDeSpawn();
		getAI().stopAITask();
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		stopRandomAnimation();
		super.onDespawn();
	}
	
	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) _template;
	}
	
	@Override
	public int getNpcId()
	{
		return getTemplate().npcId;
	}
	
	public void setUnAggred(boolean state)
	{
		_unAggred = state;
	}
	
	public boolean isAggressive()
	{
		return getAggroRange() > 0;
	}
	
	public int getAggroRange()
	{
		if(_unAggred)
		{
			return 0;
		}
		if(_personalAggroRange >= 0)
		{
			return _personalAggroRange;
		}
		return getTemplate().aggroRange;
	}
	
	public void setAggroRange(int aggroRange)
	{
		_personalAggroRange = aggroRange;
	}
	
	public Faction getFaction()
	{
		return getTemplate().getFaction();
	}
	
	public boolean isInFaction(NpcInstance npc)
	{
		return getFaction().equals(npc.getFaction()) && !getFaction().isIgnoreNpcId(npc.getNpcId());
	}
	
	@Override
	public int getMAtk(Creature target, Skill skill)
	{
		return (int) ((double) super.getMAtk(target, skill) * Config.ALT_NPC_MATK_MODIFIER);
	}
	
	@Override
	public int getPAtk(Creature target)
	{
		return (int) ((double) super.getPAtk(target) * Config.ALT_NPC_PATK_MODIFIER);
	}
	
	@Override
	public int getMaxHp()
	{
		return (int) ((double) super.getMaxHp() * Config.ALT_NPC_MAXHP_MODIFIER);
	}
	
	@Override
	public int getMaxMp()
	{
		return (int) ((double) super.getMaxMp() * Config.ALT_NPC_MAXMP_MODIFIER);
	}
	
	public long getExpReward()
	{
		return (long) calcStat(Stats.EXP, getTemplate().rewardExp, null, null);
	}
	
	public long getSpReward()
	{
		return (long) calcStat(Stats.SP, getTemplate().rewardSp, null, null);
	}
	
	@Override
	protected void onDelete()
	{
		stopDecay();
		if(_spawn != null)
		{
			_spawn.stopRespawn();
		}
		setSpawn(null);
		super.onDelete();
	}
	
	public Spawner getSpawn()
	{
		return _spawn;
	}
	
	public void setSpawn(Spawner spawn)
	{
		_spawn = spawn;
	}
	
	@Override
	protected void onDecay()
	{
		super.onDecay();
		_spawnAnimation = 2;
		if(_spawn != null)
		{
			_spawn.decreaseCount(this);
		}
		else if(!isMinion())
		{
			deleteMe();
		}
	}
	
	public final void decayOrDelete()
	{
		onDecay();
	}
	
	protected void startDecay(long delay)
	{
		stopDecay();
		_decayTask = DecayTaskManager.getInstance().addDecayTask(this, delay);
	}
	
	public void stopDecay()
	{
		if(_decayTask != null)
		{
			_decayTask.cancel(false);
			_decayTask = null;
		}
	}
	
	public void endDecayTask()
	{
		if(_decayTask != null)
		{
			_decayTask.cancel(false);
			_decayTask = null;
		}
		doDecay();
	}
	
	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}
	
	@Override
	public int getLevel()
	{
		return _level == 0 ? getTemplate().level : _level;
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	public int getDisplayId()
	{
		return _displayId > 0 ? _displayId : getTemplate().displayId;
	}
	
	public void setDisplayId(int displayId)
	{
		_displayId = displayId;
	}
	
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public WeaponTemplate getActiveWeaponItem()
	{
		int weaponId = getTemplate().rhand;
		if(weaponId < 1)
		{
			return null;
		}
		ItemTemplate item = ItemHolder.getInstance().getTemplate(getTemplate().rhand);
		if(!(item instanceof WeaponTemplate))
		{
			return null;
		}
		return (WeaponTemplate) item;
	}
	
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public WeaponTemplate getSecondaryWeaponItem()
	{
		int weaponId = getTemplate().lhand;
		if(weaponId < 1)
		{
			return null;
		}
		ItemTemplate item = ItemHolder.getInstance().getTemplate(getTemplate().lhand);
		if(!(item instanceof WeaponTemplate))
		{
			return null;
		}
		return (WeaponTemplate) item;
	}
	
	@Override
	public void sendChanges()
	{
		if(isFlying())
		{
			return;
		}
		super.sendChanges();
	}
	
	@Override
	public void broadcastCharInfo()
	{
		if(!isVisible())
		{
			return;
		}
		if(_broadcastCharInfoTask != null)
		{
			return;
		}
		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}
	
	public void broadcastCharInfoImpl()
	{
		for(Player player : World.getAroundPlayers(this))
		{
			player.sendPacket(new NpcInfo(this, player).update());
		}
	}
	
	public void onRandomAnimation()
	{
		if(System.currentTimeMillis() - _lastSocialAction > 10000)
		{
			broadcastPacket(new SocialAction(getObjectId(), 2));
			_lastSocialAction = System.currentTimeMillis();
		}
	}
	
	public void startRandomAnimation()
	{
		if(!hasRandomAnimation())
		{
			return;
		}
		_animationTask = LazyPrecisionTaskManager.getInstance().addNpcAnimationTask(this);
	}
	
	public void stopRandomAnimation()
	{
		if(_animationTask != null)
		{
			_animationTask.cancel(false);
			_animationTask = null;
		}
	}
	
	public boolean hasRandomAnimation()
	{
		return _hasRandomAnimation;
	}
	
	public boolean hasRandomWalk()
	{
		return _hasRandomWalk;
	}
	
	public Castle getCastle()
	{
		if(getReflection() == ReflectionManager.GIRAN_HARBOR && Config.SERVICES_GIRAN_HARBOR_NOTAX)
		{
			return null;
		}
		if(Config.SERVICES_OFFSHORE_NO_CASTLE_TAX && getReflection() == ReflectionManager.GIRAN_HARBOR)
		{
			return null;
		}
		if(Config.SERVICES_OFFSHORE_NO_CASTLE_TAX && isInZone(Zone.ZoneType.offshore))
		{
			return null;
		}
		if(_nearestCastle == null)
		{
			_nearestCastle = ResidenceHolder.getInstance().getResidence(getTemplate().getCastleId());
		}
		return _nearestCastle;
	}
	
	public Castle getCastle(Player player)
	{
		return getCastle();
	}
	
	public ClanHall getClanHall()
	{
		if(_nearestClanHall == null)
		{
			_nearestClanHall = ResidenceHolder.getInstance().findNearestResidence(ClanHall.class, getX(), getY(), getZ(), getReflection(), 32768);
		}
		return _nearestClanHall;
	}
	
	@Override
	public void onAction(Player player, boolean shift)
	{
		if(!isTargetable())
		{
			player.sendActionFailed();
			return;
		}
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() == this)
			{
				player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()), makeStatusUpdate(9, 10));
			}
			player.sendPacket(new ValidateLocation(this), ActionFail.STATIC);
			return;
		}
		if(Events.onAction(player, this, shift))
		{
			player.sendActionFailed();
			return;
		}
		if(isAutoAttackable(player))
		{
			player.getAI().Attack(this, false, shift);
			return;
		}
		if(!isInActingRange(player))
		{
			if(!player.getAI().isIntendingInteract(this))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			return;
		}
		if(player.getKarma() > 0 && !canInteractWithKarmaPlayer() && !player.isGM())
		{
			player.sendActionFailed();
			return;
		}
		if(player.isFlying())
		{
			player.sendActionFailed();
			return;
		}
		if(!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting() || player.isAlikeDead())
		{
			return;
		}
		if(hasRandomAnimation())
		{
			onRandomAnimation();
		}
		player.sendActionFailed();
		if(player.isMoving())
		{
			player.stopMove();
		}
		player.setLastNpcInteractionTime();
		if(_isBusy)
		{
			showBusyWindow(player);
		}
		else if(isHasChatWindow())
		{
			boolean flag = false;
			Quest[] qlst = getTemplate().getEventQuests(QuestEventType.NPC_FIRST_TALK);
			if(qlst != null && qlst.length > 0)
			{
				for(Quest element : qlst)
				{
					QuestState qs = player.getQuestState(element.getName());
					if(qs != null && qs.isCompleted() || !element.notifyFirstTalk(this, player))
						continue;
					flag = true;
				}
			}
			if(!flag)
			{
				showChatWindow(player, 0);
			}
		}
	}
	
	protected boolean canInteractWithKarmaPlayer()
	{
		if(Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP)
		{
			return true;
		}
		return this instanceof WarehouseInstance;
	}
	
	public void showQuestWindow(Player player, String questId)
	{
		if(!player.isQuestContinuationPossible(true))
		{
			return;
		}
		int count = 0;
		for(QuestState quest : player.getAllQuestsStates())
		{
			if(quest == null || !quest.getQuest().isVisible() || !quest.isStarted() || quest.getCond() <= 0)
				continue;
			++count;
		}
		if(count > 40)
		{
			showChatWindow(player, "quest-limit.htm");
			return;
		}
		try
		{
			QuestState qs = player.getQuestState(questId);
			if(qs != null)
			{
				if(qs.isCompleted())
				{
					showChatWindow(player, "completed-quest.htm");
					return;
				}
				if(qs.getQuest().notifyTalk(this, qs))
				{
					return;
				}
			}
			else
			{
				Quest[] qlst;
				Quest q = QuestManager.getQuest(questId);
				if(q != null && (qlst = getTemplate().getEventQuests(QuestEventType.QUEST_START)) != null && qlst.length > 0)
				{
					for(Quest element : qlst)
					{
						if(element != q)
							continue;
						qs = q.newQuestState(player, 1);
						if(!qs.getQuest().notifyTalk(this, qs))
							break;
						return;
					}
				}
			}
			showChatWindow(player, "no-quest.htm");
		}
		catch(Exception e)
		{
			_log.warn("problem with npc text(questId: " + questId + ") " + e);
			_log.error("", e);
		}
		player.sendActionFailed();
	}
	
	public void onBypassFeedback(Player player, String command)
	{
		block67:
		{
			if(!canBypassCheck(player, this))
			{
				return;
			}
			try
			{
				if(command.equalsIgnoreCase("TerritoryStatus"))
				{
					NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setFile("merchant/territorystatus.htm");
					html.replace("%npcname%", getName());
					Castle castle = getCastle(player);
					if(castle != null && castle.getId() > 0)
					{
						html.replace("%castlename%", HtmlUtils.htmlResidenceName(castle.getId()));
						html.replace("%taxpercent%", String.valueOf(castle.getTaxPercent()));
						if(castle.getOwnerId() > 0)
						{
							Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
							if(clan != null)
							{
								html.replace("%clanname%", clan.getName());
								html.replace("%clanleadername%", clan.getLeaderName());
							}
							else
							{
								html.replace("%clanname%", "unexistant clan");
								html.replace("%clanleadername%", "None");
							}
						}
						else
						{
							html.replace("%clanname%", "NPC");
							html.replace("%clanleadername%", "None");
						}
					}
					else
					{
						html.replace("%castlename%", "Open");
						html.replace("%taxpercent%", "0");
						html.replace("%clanname%", "No");
						html.replace("%clanleadername%", getName());
					}
					player.sendPacket(html);
					break block67;
				}
				if(command.startsWith("Quest"))
				{
					String quest = command.substring(5).trim();
					if(quest.length() == 0)
					{
						showQuestWindow(player);
					}
					else
					{
						showQuestWindow(player, quest);
					}
					break block67;
				}
				if(command.startsWith("Chat"))
				{
					try
					{
						int val = Integer.parseInt(command.substring(5));
						showChatWindow(player, val);
					}
					catch(NumberFormatException e)
					{
						String filename = command.substring(5).trim();
						if(filename.length() == 0)
						{
							showChatWindow(player, "npcdefault.htm");
							break block67;
						}
						showChatWindow(player, filename);
					}
					break block67;
				}
				if(command.startsWith("AttributeCancel"))
				{
					player.sendPacket(new ExShowBaseAttributeCancelWindow(player));
				}
				else if(command.startsWith("NpcLocationInfo"))
				{
					int val = Integer.parseInt(command.substring(16));
					NpcInstance npc = GameObjectsStorage.getByNpcId(val);
					if(npc != null)
					{
						player.sendPacket(new RadarControl(2, 2, npc.getLoc()));
						player.sendPacket(new RadarControl(0, 1, npc.getLoc()));
					}
				}
				else if(command.startsWith("Multisell") || command.startsWith("multisell"))
				{
					Castle castle;
					String listId = command.substring(9).trim();
					MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(listId), player, (castle = getCastle(player)) != null ? castle.getTaxRate() : 0.0);
				}
				else if(command.startsWith("EnterRift"))
				{
					StringTokenizer st = new StringTokenizer(command);
					st.nextToken();
					Integer b1 = Integer.parseInt(st.nextToken());
					DimensionalRiftManager.getInstance().start(player, b1, this);
				}
				else if(command.startsWith("ChangeRiftRoom"))
				{
					if(player.isInParty() && player.getParty().isInReflection() && player.getParty().getReflection() instanceof DimensionalRift)
					{
						((DimensionalRift) player.getParty().getReflection()).manualTeleport(player, this);
					}
					else
					{
						DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
					}
				}
				else if(command.startsWith("ExitRift"))
				{
					if(player.isInParty() && player.getParty().isInReflection() && player.getParty().getReflection() instanceof DimensionalRift)
					{
						((DimensionalRift) player.getParty().getReflection()).manualExitRift(player, this);
					}
					else
					{
						DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
					}
				}
				else if(command.equalsIgnoreCase("SkillList"))
				{
					showSkillList(player);
				}
				else if(command.startsWith("AltSkillList"))
				{
					int altClsId = Integer.parseInt(command.substring(13).trim());
					showSkillList(player, ClassId.VALUES[altClsId]);
				}
				else if(command.equalsIgnoreCase("SkillEnchantList"))
				{
					showSkillEnchantList(player);
				}
				else if(command.equalsIgnoreCase("ClanSkillList"))
				{
					showClanSkillList(player);
				}
				else if(command.startsWith("Augment"))
				{
					int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
					if(cmdChoice == 1)
					{
						player.setRefineryHandler(RefineryHandler.getInstance());
						RefineryHandler.getInstance().onInitRefinery(player);
					}
					else if(cmdChoice == 2)
					{
						player.setRefineryHandler(RefineryHandler.getInstance());
						RefineryHandler.getInstance().onInitRefineryCancel(player);
					}
				}
				else if(command.startsWith("Link"))
				{
					showChatWindow(player, command.substring(5));
				}
				else if(command.startsWith("Teleport"))
				{
					int cmdChoice = Integer.parseInt(command.substring(9).trim());
					TeleportLocation[] list = getTemplate().getTeleportList(cmdChoice);
					if(list != null)
					{
						showTeleportList(player, list);
					}
					else
					{
						player.sendMessage("Ссылка неисправна, сообщите администратору.");
					}
				}
				else if(command.startsWith("Tele20Lvl"))
				{
					int cmdChoice = Integer.parseInt(command.substring(10, 11).trim());
					TeleportLocation[] list = getTemplate().getTeleportList(cmdChoice);
					if(player.getLevel() > 20)
					{
						showChatWindow(player, "teleporter/" + getNpcId() + "-no.htm");
					}
					else if(list != null)
					{
						showTeleportList(player, list);
					}
					else
					{
						player.sendMessage("Ссылка неисправна, сообщите администратору.");
					}
				}
				else if(command.startsWith("open_gate"))
				{
					int val = Integer.parseInt(command.substring(10));
					ReflectionUtils.getDoor(val).openMe();
					player.sendActionFailed();
				}
				else if(command.startsWith("ExitFromQuestInstance"))
				{
					Reflection r = player.getReflection();
					r.startCollapseTimer(60000);
					player.teleToLocation(r.getReturnLoc(), 0);
					if(command.length() > 22)
					{
						try
						{
							int val = Integer.parseInt(command.substring(22));
							showChatWindow(player, val);
						}
						catch(NumberFormatException e)
						{
							String filename = command.substring(22).trim();
							if(filename.length() > 0)
							{
								showChatWindow(player, filename);
							}
						}
					}
				}
			}
			catch(StringIndexOutOfBoundsException e)
			{
				_log.info("Incorrect htm bypass! npcId=" + getTemplate().npcId + " command=[" + command + "]");
			}
			catch(NumberFormatException e)
			{
				_log.info("Invalid bypass to Server command parameter! npcId=" + getTemplate().npcId + " command=[" + command + "]");
			}
		}
	}
	
	public void showTeleportList(Player player, TeleportLocation[] list)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("&$556;").append("<br><br>");
		if(list != null && player.getPlayerAccess().UseTeleport)
		{
			for(TeleportLocation tl : list)
			{
				if(tl.getItem().getItemId() == 57)
				{
					double pricemod;
					double d = pricemod = player.getLevel() <= Config.GATEKEEPER_FREE ? 0.0 : Config.GATEKEEPER_MODIFIER;
					if(tl.getPrice() > 0 && pricemod > 0.0)
					{
						Calendar calendar = Calendar.getInstance();
						int day = calendar.get(7);
						int hour = Calendar.getInstance().get(11);
						if((day == 1 || day == 7) && hour >= 20 && hour <= 12)
						{
							pricemod /= 2.0;
						}
					}
					sb.append("[scripts_Util:Gatekeeper ").append(tl.getX()).append(" ").append(tl.getY()).append(" ").append(tl.getZ());
					if(tl.getCastleId() != 0)
					{
						sb.append(" ").append(tl.getCastleId());
					}
					String name = new CustomMessage(tl.getName(), player, new Object[0]).toString();
					sb.append(" ").append((long) ((double) tl.getPrice() * pricemod)).append(" @811;F;").append(name).append("|").append(name);
					if((double) tl.getPrice() * pricemod > 0.0)
					{
						sb.append(" - ").append((long) ((double) tl.getPrice() * pricemod)).append(" ").append(HtmlUtils.htmlItemName(57));
					}
					if(tl.getMinLevel() > 0)
					{
						sb.append(" - ").append(new CustomMessage("l2.gameserver.model.instances.NpcInstance.TeleportListMinLevel", player, tl.getMinLevel()));
					}
					if(tl.getMaxLevel() > 0)
					{
						sb.append(" - ").append(new CustomMessage("l2.gameserver.model.instances.NpcInstance.TeleportListMaxLevel", player, tl.getMaxLevel()));
					}
					sb.append("]<br1>\n");
					continue;
				}
				String name = new CustomMessage(tl.getName(), player, new Object[0]).toString();
				sb.append("[scripts_Util:QuestGatekeeper ").append(tl.getX()).append(" ").append(tl.getY()).append(" ").append(tl.getZ()).append(" ").append(tl.getPrice()).append(" ").append(tl.getItem().getItemId()).append(" @811;F;").append(name).append("|").append(name).append(" - ").append(tl.getPrice()).append(" ").append(HtmlUtils.htmlItemName(tl.getItem().getItemId()));
				if(tl.getMinLevel() > 0)
				{
					sb.append(" - ").append(new CustomMessage("l2.gameserver.model.instances.NpcInstance.TeleportListMinLevel", player, tl.getMinLevel()));
				}
				if(tl.getMaxLevel() > 0)
				{
					sb.append(" - ").append(new CustomMessage("l2.gameserver.model.instances.NpcInstance.TeleportListMaxLevel", player, tl.getMaxLevel()));
				}
				sb.append("]<br1>\n");
			}
		}
		else
		{
			sb.append("No teleports available for you.");
		}
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setHtml(Strings.bbParse(sb.toString()));
		player.sendPacket(html);
	}
	
	public void showQuestWindow(Player player)
	{
		ArrayList<Quest> options = new ArrayList<>();
		List<QuestState> awaits = player.getQuestsForEvent(this, QuestEventType.QUEST_TALK);
		Quest[] starts = getTemplate().getEventQuests(QuestEventType.QUEST_START);
		if(awaits != null)
		{
			for(QuestState x : awaits)
			{
				if(options.contains(x.getQuest()) || x.getQuest().getQuestIntId() <= 0)
					continue;
				options.add(x.getQuest());
			}
		}
		if(starts != null)
		{
			for(Quest x : starts)
			{
				if(options.contains(x) || x.getQuestIntId() <= 0)
					continue;
				options.add(x);
			}
		}
		if(options.size() > 1)
		{
			showQuestChooseWindow(player, options.toArray(new Quest[options.size()]));
		}
		else if(options.size() == 1)
		{
			showQuestWindow(player, options.get(0).getName());
		}
		else
		{
			showQuestWindow(player, "");
		}
	}
	
	public void showQuestChooseWindow(Player player, Quest[] quests)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body><title>Talk about:</title><br>");
		for(Quest q : quests)
		{
			if(!q.isVisible())
				continue;
			sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\">[").append(q.getDescr(player)).append("]</a><br>");
		}
		sb.append("</body></html>");
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	public void showChatWindow(Player player, int val, Object... replace)
	{
		String filename = "seven_signs/";
		int npcId = getNpcId();
		block0:
		switch(npcId)
		{
			case 31111:
			{
				int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(1);
				int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
				int compWinner = SevenSigns.getInstance().getCabalHighestScore();
				if(playerCabal == sealAvariceOwner && playerCabal == compWinner)
				{
					switch(sealAvariceOwner)
					{
						case 2:
						{
							filename = filename + "spirit_dawn.htm";
							break block0;
						}
						case 1:
						{
							filename = filename + "spirit_dusk.htm";
							break block0;
						}
						case 0:
						{
							filename = filename + "spirit_null.htm";
						}
					}
					break;
				}
				filename = filename + "spirit_null.htm";
				break;
			}
			case 31112:
			{
				filename = filename + "spirit_exit.htm";
				break;
			}
			case 30298:
			{
				if(player.getPledgeType() == -1)
				{
					filename = getHtmlPath(npcId, 1, player);
					break;
				}
				filename = getHtmlPath(npcId, 0, player);
				break;
			}
			default:
			{
				if(npcId >= 31093 && npcId <= 31094 || npcId >= 31172 && npcId <= 31201 || npcId >= 31239 && npcId <= 31254)
				{
					return;
				}
				filename = getHtmlPath(npcId, val, player);
			}
		}
		NpcHtmlMessage packet = new NpcHtmlMessage(player, this, filename, val);
		if(replace.length % 2 == 0)
		{
			for(int i = 0;i < replace.length;i += 2)
			{
				packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
			}
		}
		player.sendPacket(packet);
	}
	
	public void showChatWindow(Player player, String filename, Object... replace)
	{
		NpcHtmlMessage packet = new NpcHtmlMessage(player, this, filename, 0);
		if(replace.length % 2 == 0)
		{
			for(int i = 0;i < replace.length;i += 2)
			{
				packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
			}
		}
		player.sendPacket(packet);
	}
	
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom = val == 0 ? "" + npcId : "" + npcId + "-" + val;
		if(getTemplate().getHtmRoot() != null)
		{
			return getTemplate().getHtmRoot() + pom + ".htm";
		}
		String temp = "default/" + pom + ".htm";
		if(HtmCache.getInstance().getNullable(temp, player) != null)
		{
			return temp;
		}
		temp = "trainer/" + pom + ".htm";
		if(HtmCache.getInstance().getNullable(temp, player) != null)
		{
			return temp;
		}
		return "npcdefault.htm";
	}
	
	public final boolean isBusy()
	{
		return _isBusy;
	}
	
	public void setBusy(boolean isBusy)
	{
		_isBusy = isBusy;
	}
	
	public final String getBusyMessage()
	{
		return _busyMessage;
	}
	
	public void setBusyMessage(String message)
	{
		_busyMessage = message;
	}
	
	public void showBusyWindow(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile("npcbusy.htm");
		html.replace("%npcname%", getName());
		html.replace("%playername%", player.getName());
		html.replace("%busymessage%", _busyMessage);
		player.sendPacket(html);
	}
	
	public void showSkillEnchantList(Player player)
	{
		ClassId classId = player.getClassId();
		if(player.getClassId().getLevel() < 4)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			StringBuilder sb = new StringBuilder();
			sb.append("<html><head><body>");
			if(player.isLangRus())
			{
				sb.append("Мастер:<br>");
				sb.append("Вы должны выполнить квест на получение третьей профессии.");
			}
			else
			{
				sb.append("Trainer:<br>");
				sb.append("You must have 3rd class change quest completed.");
			}
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}
		if(player.getLevel() < 76)
		{
			player.sendPacket(new SystemMessage(1438));
			return;
		}
		if(!getTemplate().canTeach(classId) && !getTemplate().canTeach(classId.getParent(player.getSex())))
		{
			if(this instanceof TrainerInstance)
			{
				showChatWindow(player, "trainer/" + getNpcId() + "-noteach.htm");
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				StringBuilder sb = new StringBuilder();
				sb.append("<html><head><body>");
				sb.append(new CustomMessage("l2p.gameserver.model.instances.L2NpcInstance.WrongTeacherClass", player));
				sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);
			}
			return;
		}
		player.sendPacket(ExEnchantSkillList.packetFor(player));
	}
	
	public void showSkillList(Player player)
	{
		ClassId classId = player.getClassId();
		showSkillList(player, player.getClassId());
	}
	
	public void showSkillList(Player player, ClassId classId)
	{
		if(classId == null)
		{
			return;
		}
		int npcId = getTemplate().npcId;
		if(getTemplate().getTeachInfo().isEmpty())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			StringBuilder sb = new StringBuilder();
			sb.append("<html><head><body>");
			if(player.getVar("lang@").equalsIgnoreCase("en"))
			{
				sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. <br>NpcId:" + npcId + ", Your classId:" + classId.name() + "<br>");
			}
			else
			{
				sb.append("Я не могу обучить тебя. Для твоего класса мой список пуст.<br> Свяжись с админом для фикса этого. <br>NpcId:" + npcId + ", твой classId:" + classId.name() + "<br>");
			}
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}
		if(!getTemplate().canTeach(classId) && !getTemplate().canTeach(classId.getParent(player.getSex())))
		{
			if(this instanceof WarehouseInstance)
			{
				showChatWindow(player, "warehouse/" + getNpcId() + "-noteach.htm");
			}
			else if(this instanceof TrainerInstance)
			{
				showChatWindow(player, "trainer/" + getNpcId() + "-noteach.htm");
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				StringBuilder sb = new StringBuilder();
				sb.append("<html><head><body>");
				sb.append(new CustomMessage("l2p.gameserver.model.instances.L2NpcInstance.WrongTeacherClass", player));
				sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);
			}
			return;
		}
		Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, classId, AcquireType.NORMAL, null);
		AcquireSkillList asl = new AcquireSkillList(AcquireType.NORMAL, skills.size());
		int counts = 0;
		for(SkillLearn s : skills)
		{
			Skill sk;
			if(s.isClicked() || (sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel())) == null || !Config.ALT_WEAK_SKILL_LEARN && (!sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcId)))
				continue;
			++counts;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 0);
		}
		if(counts == 0)
		{
			int minlevel = SkillAcquireHolder.getInstance().getMinLevelForNewSkill(classId, player.getLevel(), AcquireType.NORMAL);
			if(minlevel > 0)
			{
				SystemMessage2 sm = new SystemMessage2(SystemMsg.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN__COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
				sm.addInteger(minlevel);
				player.sendPacket(sm);
			}
			else
			{
				player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
			}
			player.sendPacket(AcquireSkillDone.STATIC);
		}
		else
		{
			player.setVar("AcquireSkillClassId", classId.getId(), -1);
			player.sendPacket(asl);
		}
		player.sendActionFailed();
	}
	
	public int getSpawnAnimation()
	{
		return _spawnAnimation;
	}
	
	@Override
	public double getColRadius()
	{
		return getCollisionRadius();
	}
	
	@Override
	public double getColHeight()
	{
		return getCollisionHeight();
	}
	
	public int calculateLevelDiffForDrop(int charLevel)
	{
		if(!Config.DEEPBLUE_DROP_RULES || _ignoreDropDiffPenalty)
		{
			return 0;
		}
		int mobLevel = getLevel();
		int deepblue_maxdiff = this instanceof RaidBossInstance ? Config.DEEPBLUE_DROP_RAID_MAXDIFF : Config.DEEPBLUE_DROP_MAXDIFF;
		return Math.max(charLevel - mobLevel - deepblue_maxdiff, 0);
	}
	
	public boolean isSevenSignsMonster()
	{
		return getFaction().getName().equalsIgnoreCase("c_dungeon_clan");
	}
	
	@Override
	public String toString()
	{
		return "" + getNpcId() + " " + getName();
	}
	
	public void refreshID()
	{
		objectId = IdFactory.getInstance().getNextId();
		_storedId = GameObjectsStorage.refreshId(this);
	}
	
	public boolean isUnderground()
	{
		return _isUnderground;
	}
	
	public void setUnderground(boolean b)
	{
		_isUnderground = b;
	}
	
	public boolean isTargetable()
	{
		return _isTargetable;
	}
	
	public void setTargetable(boolean value)
	{
		_isTargetable = value;
	}
	
	public boolean isShowName()
	{
		return _showName;
	}
	
	public void setShowName(boolean value)
	{
		_showName = value;
	}
	
	@Override
	public NpcListenerList getListeners()
	{
		if(listeners == null)
		{
			NpcInstance npcInstance = this;
			synchronized(npcInstance)
			{
				if(listeners == null)
				{
					listeners = new NpcListenerList(this);
				}
			}
		}
		return (NpcListenerList) listeners;
	}
	
	public <T extends NpcListener> boolean addListener(T listener)
	{
		return getListeners().add(listener);
	}
	
	public <T extends NpcListener> boolean removeListener(T listener)
	{
		return getListeners().remove(listener);
	}
	
	@Override
	public NpcStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
		{
			NpcInstance npcInstance = this;
			synchronized(npcInstance)
			{
				if(_statsRecorder == null)
				{
					_statsRecorder = new NpcStatsChangeRecorder(this);
				}
			}
		}
		return (NpcStatsChangeRecorder) _statsRecorder;
	}
	
	public int getNpcState()
	{
		return npcState;
	}
	
	public void setNpcState(int stateId)
	{
		broadcastPacket(new ExChangeNpcState(getObjectId(), stateId));
		npcState = stateId;
	}
	
	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		ArrayList<L2GameServerPacket> list = new ArrayList<>(3);
		list.add(new NpcInfo(this, forPlayer));
		if(isInCombat())
		{
			list.add(new AutoAttackStart(getObjectId()));
		}
		if(isMoving() || isFollowing())
		{
			list.add(movePacket());
		}
		return list;
	}
	
	@Override
	public Clan getClan()
	{
		Castle castle = getCastle();
		return castle != null ? castle.getOwner() : null;
	}
	
	@Override
	public boolean isNpc()
	{
		return true;
	}
	
	@Override
	public int getGeoZ(Location loc)
	{
		if(isFlying() || isInWater() || isInBoat() || isBoat() || isDoor())
		{
			return loc.z;
		}
		if(isNpc())
		{
			if(_spawnRange instanceof Territory)
			{
				return GeoEngine.getHeight(loc, getGeoIndex());
			}
			return loc.z;
		}
		return super.getGeoZ(loc);
	}
	
	public boolean isMerchantNpc()
	{
		return false;
	}
	
	public SpawnRange getSpawnRange()
	{
		return _spawnRange;
	}
	
	public void setSpawnRange(SpawnRange spawnRange)
	{
		_spawnRange = spawnRange;
	}
	
	public void setParameter(String str, Object val)
	{
		if(_parameters == StatsSet.EMPTY)
		{
			_parameters = new StatsSet();
		}
		_parameters.set(str, val);
	}
	
	public int getParameter(String str, int val)
	{
		return _parameters.getInteger(str, val);
	}
	
	public long getParameter(String str, long val)
	{
		return _parameters.getLong(str, val);
	}
	
	public boolean getParameter(String str, boolean val)
	{
		return _parameters.getBool(str, val);
	}
	
	public String getParameter(String str, String val)
	{
		return _parameters.getString(str, val);
	}
	
	public MultiValueSet<String> getParameters()
	{
		return _parameters;
	}
	
	public void setParameters(MultiValueSet<String> set)
	{
		if(set.isEmpty())
		{
			return;
		}
		if(_parameters == StatsSet.EMPTY)
		{
			_parameters = new MultiValueSet(set.size());
		}
		_parameters.putAll(set);
	}
	
	@Override
	public boolean isInvul()
	{
		return true;
	}
	
	public boolean isHasChatWindow()
	{
		return _hasChatWindow;
	}
	
	public void setHasChatWindow(boolean hasChatWindow)
	{
		_hasChatWindow = hasChatWindow;
	}
	
	public class BroadcastCharInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			broadcastCharInfoImpl();
			_broadcastCharInfoTask = null;
		}
	}
}