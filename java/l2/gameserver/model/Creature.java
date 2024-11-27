package l2.gameserver.model;

import gnu.trove.TIntHashSet;
import l2.commons.collections.LazyArrayList;
import l2.commons.lang.reference.HardReference;
import l2.commons.lang.reference.HardReferences;
import l2.commons.listener.Listener;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.commons.util.concurrent.atomic.AtomicState;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CharacterAI;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.ai.NextAction;
import l2.gameserver.cache.Msg;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.geodata.GeoMove;
import l2.gameserver.instancemanager.DimensionalRiftManager;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.actor.recorder.CharStatsChangeRecorder;
import l2.gameserver.model.base.InvisibleType;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.instances.MinionInstance;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.StaticObjectInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.quest.QuestEventType;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.model.reference.L2Reference;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.IStaticPacket;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.*;
import l2.gameserver.skills.AbnormalEffect;
import l2.gameserver.skills.EffectType;
import l2.gameserver.skills.TimeStamp;
import l2.gameserver.stats.Calculator;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.Formulas;
import l2.gameserver.stats.StatFunctions;
import l2.gameserver.stats.StatTemplate;
import l2.gameserver.stats.Stats;
import l2.gameserver.stats.funcs.Func;
import l2.gameserver.stats.triggers.TriggerInfo;
import l2.gameserver.stats.triggers.TriggerType;
import l2.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2.gameserver.taskmanager.RegenTaskManager;
import l2.gameserver.templates.CharTemplate;
import l2.gameserver.templates.item.WeaponTemplate;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.PositionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Creature extends GameObject
{
	public static final double HEADINGS_IN_PI = 10430.378350470453;
	public static final int CLIENT_BAR_SIZE = 352;
	private static final Logger _log = LoggerFactory.getLogger(Creature.class);
	private static final double[] POLE_VAMPIRIC_MOD = {1.0, 0.9, 0.0, 7.0, 0.2, 0.01};
	protected final Map<Integer, Skill> _skills = new ConcurrentSkipListMap<>();
	private final Lock moveLock = new ReentrantLock();
	private final Calculator[] _calculators;
	private final Lock regenLock = new ReentrantLock();
	private final Lock statusListenersLock = new ReentrantLock();
	private final AtomicState _afraid = new AtomicState();
	private final AtomicState _muted = new AtomicState();
	private final AtomicState _pmuted = new AtomicState();
	private final AtomicState _amuted = new AtomicState();
	private final AtomicState _paralyzed = new AtomicState();
	private final AtomicState _rooted = new AtomicState();
	private final AtomicState _sleeping = new AtomicState();
	private final AtomicState _stunned = new AtomicState();
	private final AtomicState _immobilized = new AtomicState();
	private final AtomicState _confused = new AtomicState();
	private final AtomicState _frozen = new AtomicState();
	private final AtomicState _healBlocked = new AtomicState();
	private final AtomicState _damageBlocked = new AtomicState();
	private final AtomicState _buffImmunity = new AtomicState();
	private final AtomicState _debuffImmunity = new AtomicState();
	private final AtomicState _effectImmunity = new AtomicState();
	private final AtomicState _weaponEquipBlocked = new AtomicState();
	private final AtomicReference<Zone[]> _zonesRef = new AtomicReference<>(Zone.EMPTY_L2ZONE_ARRAY);
	private final TIntHashSet _unActiveSkills = new TIntHashSet();
	public int _scheduledCastInterval;
	public Future<?> _skillTask;
	public Future<?> _skillLaunchedTask;
	protected double _currentCp;
	protected double _currentHp = 1.0;
	protected double _currentMp = 1.0;
	protected boolean _isAttackAborted;
	protected long _attackEndTime;
	protected long _attackReuseEndTime;
	protected Map<TriggerType, Set<TriggerInfo>> _triggers;
	protected IntObjectMap<TimeStamp> _skillReuses = new CHashIntObjectMap();
	protected volatile EffectList _effectList;
	protected volatile CharStatsChangeRecorder<? extends Creature> _statsRecorder;
	protected AtomicBoolean isDead = new AtomicBoolean();
	protected AtomicBoolean isTeleporting = new AtomicBoolean();
	protected boolean _isInvul;
	protected MoveActionBase moveAction;
	protected CharTemplate _template;
	protected CharTemplate _baseTemplate;
	protected volatile CharacterAI _ai;
	protected String _name;
	protected String _title;
	protected TeamType _team = TeamType.NONE;
	protected volatile CharListenerList listeners;
	protected Long _storedId;
	protected HardReference<? extends Creature> reference;
	private Skill _castingSkill;
	private long _castInterruptTime;
	private long _animationEndTime;
	private Future<?> _stanceTask;
	private Runnable _stanceTaskRunnable;
	private long _stanceEndTime;
	private int _lastCpBarUpdate = -1;
	private int _lastHpBarUpdate = -1;
	private int _lastMpBarUpdate = -1;
	private int _poleAttackCount;
	private List<Stats> _blockedStats;
	private int _abnormalEffects;
	private int _abnormalEffects2;
	private int _abnormalEffects3;
	private Map<Integer, Integer> _skillMastery;
	private boolean _isBlessedByNoblesse;
	private boolean _isSalvation;
	private boolean _meditated;
	private boolean _lockedTarget;
	private boolean _blocked;
	private boolean _flying;
	private boolean _running;
	private Future<?> _moveTask;
	private Runnable _moveTaskRunnable;
	private volatile HardReference<? extends GameObject> target = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> castingTarget = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> _aggressionTarget = HardReferences.emptyRef();
	private int _heading;
	private boolean _isRegenerating;
	private Future<?> _regenTask;
	private Runnable _regenTaskRunnable;
	private List<Player> _statusListeners;
	private Location _flyLoc;
	
	public Creature(int objectId, CharTemplate template)
	{
		super(objectId);
		_template = template;
		_baseTemplate = template;
		_calculators = new Calculator[Stats.NUM_STATS];
		StatFunctions.addPredefinedFuncs(this);
		reference = new L2Reference<>(this);
		_storedId = GameObjectsStorage.put(this);
	}
	
	@Override
	public int getActingRange()
	{
		return 150;
	}
	
	public final Long getStoredId()
	{
		return _storedId;
	}
	
	@Override
	public HardReference<? extends Creature> getRef()
	{
		return reference;
	}
	
	public boolean isAttackAborted()
	{
		return _isAttackAborted;
	}
	
	public final void abortAttack(boolean force, boolean message)
	{
		if(isAttackingNow())
		{
			_attackEndTime = 0;
			if(force)
			{
				_isAttackAborted = true;
			}
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			if(isPlayer() && message)
			{
				sendActionFailed();
				sendPacket(new SystemMessage(158));
			}
		}
	}
	
	public final void abortCast(boolean force, boolean message)
	{
		if(isCastingNow() && (force || canAbortCast()))
		{
			Skill castingSkill = _castingSkill;
			Future skillTask = _skillTask;
			Future skillLaunchedTask = _skillLaunchedTask;
			finishFly();
			clearCastVars();
			if(skillTask != null)
			{
				skillTask.cancel(false);
			}
			if(skillLaunchedTask != null)
			{
				skillLaunchedTask.cancel(false);
			}
			if(castingSkill != null)
			{
				Creature target;
				if(castingSkill.isUsingWhileCasting() && (target = getAI().getAttackTarget()) != null)
				{
					target.getEffectList().stopEffect(castingSkill.getId());
				}
				removeSkillMastery(castingSkill.getId());
			}
			broadcastPacket(new MagicSkillCanceled(this));
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			if(isPlayer() && message)
			{
				sendPacket(Msg.CASTING_HAS_BEEN_INTERRUPTED);
			}
		}
	}
	
	public final boolean canAbortCast()
	{
		return _castInterruptTime >= System.currentTimeMillis();
	}
	
	public boolean absorbAndReflect(Creature target, Skill skill, double damage, boolean sendMessage)
	{
		if(target.isDead())
		{
			return false;
		}
		boolean bow = getActiveWeaponItem() != null && getActiveWeaponItem().getItemType() == WeaponTemplate.WeaponType.BOW;
		double value = 0.0;
		if(skill != null && skill.isMagic())
		{
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, 0.0, this, skill);
		}
		else if(skill != null && skill.getCastRange() <= 200)
		{
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, 0.0, this, skill);
		}
		else if(skill == null && !bow)
		{
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, 0.0, this, null);
		}
		if(value > 0.0 && Rnd.chance(value))
		{
			reduceCurrentHp(damage, target, null, true, true, false, false, false, false, true);
			return true;
		}
		if(skill != null && skill.isMagic())
		{
			value = target.calcStat(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, 0.0, this, skill);
		}
		else if(skill != null && skill.getCastRange() <= 200)
		{
			value = target.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, 0.0, this, skill);
		}
		else if(skill == null && !bow)
		{
			value = target.calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0.0, this, null);
		}
		if(value > 0.0 && target.getCurrentHp() + target.getCurrentCp() > damage)
		{
			double dam = value / 100.0 * damage;
			reduceCurrentHp(dam, target, null, true, true, false, false, false, false, sendMessage);
			if(sendMessage && target.isPlayable())
			{
				target.sendPacket(new SystemMessage(35).addNumber((int) dam));
			}
		}
		if(skill != null || bow)
		{
			return false;
		}
		if((damage = (double) (int) (damage - target.getCurrentCp())) <= 0.0)
		{
			return false;
		}
		double poleMod = _poleAttackCount < POLE_VAMPIRIC_MOD.length ? POLE_VAMPIRIC_MOD[_poleAttackCount] : 0.0;
		double absorb = poleMod * calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0.0, target, null);
		if(absorb > 0.0 && !target.isDamageBlocked())
		{
			double limit = calcStat(Stats.HP_LIMIT, null, null) * (double) getMaxHp() / 100.0;
			if(getCurrentHp() < limit)
			{
				setCurrentHp(Math.min(_currentHp + damage * absorb * Config.ALT_ABSORB_DAMAGE_MODIFIER / 100.0, limit), false);
			}
		}
		return false;
	}
	
	public double absorbToEffector(Creature attacker, double damage)
	{
		double transferToEffectorDam = calcStat(Stats.TRANSFER_TO_EFFECTOR_DAMAGE_PERCENT, 0.0);
		if(transferToEffectorDam > 0.0)
		{
			Effect effect = getEffectList().getEffectByType(EffectType.AbsorbDamageToEffector);
			if(effect == null)
			{
				return damage;
			}
			Creature effector = effect.getEffector();
			if(effector == this || effector.isDead() || !isInRange(effector, 1200))
			{
				return damage;
			}
			Player thisPlayer = getPlayer();
			Player effectorPlayer = effector.getPlayer();
			if(thisPlayer != null && effectorPlayer != null)
			{
				if(!(thisPlayer == effectorPlayer || thisPlayer.isOnline() && thisPlayer.isInParty() && thisPlayer.getParty() == effectorPlayer.getParty()))
				{
					return damage;
				}
			}
			else
			{
				return damage;
			}
			double transferDamage = damage * transferToEffectorDam * 0.01;
			damage -= transferDamage;
			effector.reduceCurrentHp(transferDamage, effector, null, false, false, !attacker.isPlayable(), false, true, false, true);
		}
		return damage;
	}
	
	public double absorbToSummon(Creature attacker, double damage)
	{
		double transferToSummonDam = calcStat(Stats.TRANSFER_TO_SUMMON_DAMAGE_PERCENT, 0.0);
		if(transferToSummonDam > 0.0)
		{
			Summon summon = getPet();
			double transferDamage = damage * transferToSummonDam * 0.01;
			if(summon == null || summon.isDead() || summon.getCurrentHp() < transferDamage)
			{
				getEffectList().stopEffects(EffectType.AbsorbDamageToSummon);
			}
			else if(summon.isSummon() && summon.isInRangeZ(this, 1200))
			{
				damage -= transferDamage;
				summon.reduceCurrentHp(transferDamage, summon, null, false, false, false, false, true, false, true);
			}
		}
		return damage;
	}
	
	public void addBlockStats(List<Stats> stats)
	{
		if(_blockedStats == null)
		{
			_blockedStats = new ArrayList<>();
		}
		_blockedStats.addAll(stats);
	}
	
	public Skill addSkill(Skill newSkill)
	{
		if(newSkill == null)
		{
			return null;
		}
		Skill oldSkill = _skills.get(newSkill.getId());
		if(oldSkill != null && oldSkill.getLevel() == newSkill.getLevel())
		{
			return newSkill;
		}
		_skills.put(newSkill.getId(), newSkill);
		if(oldSkill != null)
		{
			removeStatsOwner(oldSkill);
			removeTriggers(oldSkill);
		}
		addTriggers(newSkill);
		addStatFuncs(newSkill.getStatFuncs());
		return oldSkill;
	}
	
	public Calculator[] getCalculators()
	{
		return _calculators;
	}
	
	public final void addStatFunc(Func f)
	{
		if(f == null)
		{
			return;
		}
		int stat = f.stat.ordinal();
		Calculator[] arrcalculator = _calculators;
		synchronized(arrcalculator)
		{
			if(_calculators[stat] == null)
			{
				_calculators[stat] = new Calculator(f.stat, this);
			}
			_calculators[stat].addFunc(f);
		}
	}
	
	public final void addStatFuncs(Func[] funcs)
	{
		for(Func f : funcs)
		{
			addStatFunc(f);
		}
	}
	
	public final void removeStatFunc(Func f)
	{
		if(f == null)
		{
			return;
		}
		int stat = f.stat.ordinal();
		Calculator[] arrcalculator = _calculators;
		synchronized(arrcalculator)
		{
			if(_calculators[stat] != null)
			{
				_calculators[stat].removeFunc(f);
			}
		}
	}
	
	public final void removeStatFuncs(Func[] funcs)
	{
		for(Func f : funcs)
		{
			removeStatFunc(f);
		}
	}
	
	public final void removeStatsOwner(Object owner)
	{
		Calculator[] arrcalculator = _calculators;
		synchronized(arrcalculator)
		{
			for(int i = 0;i < _calculators.length;++i)
			{
				if(_calculators[i] == null)
					continue;
				_calculators[i].removeOwner(owner);
			}
		}
	}
	
	public void altOnMagicUseTimer(Creature aimingTarget, Skill skill)
	{
		if(isAlikeDead())
		{
			return;
		}
		List<Creature> targets = skill.getTargets(this, aimingTarget, true);
		double mpConsume2 = skill.getMpConsume2();
		if(mpConsume2 > 0.0)
		{
			if(_currentMp < mpConsume2)
			{
				sendPacket(Msg.NOT_ENOUGH_MP);
				return;
			}
			if(skill.isMagic())
			{
				reduceCurrentMp(calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
			}
			else
			{
				reduceCurrentMp(calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
			}
		}
		callSkill(skill, targets, false);
		broadcastPacket(new MagicSkillLaunched(this, skill, targets));
	}
	
	public void altUseSkill(Skill skill, Creature target)
	{
		if(skill == null)
		{
			return;
		}
		int magicId = skill.getId();
		if(isUnActiveSkill(magicId))
		{
			return;
		}
		if(isSkillDisabled(skill))
		{
			sendReuseMessage(skill);
			return;
		}
		if(target == null && (target = skill.getAimingTarget(this, getTarget())) == null)
		{
			return;
		}
		getListeners().onMagicUse(skill, target, true);
		int[] itemConsume = skill.getItemConsume();
		if(itemConsume[0] > 0)
		{
			for(int i = 0;i < itemConsume.length;++i)
			{
				if(consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
					continue;
				sendPacket(skill.isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : new SystemMessage(113).addSkillName(skill.getDisplayId(), skill.getDisplayLevel()));
				return;
			}
		}
		if(skill.getReferenceItemId() > 0 && !consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
		{
			return;
		}
		if(skill.getSoulsConsume() > getConsumedSouls())
		{
			sendPacket(Msg.THERE_IS_NOT_ENOUGHT_SOUL);
			return;
		}
		if(skill.getEnergyConsume() > getAgathionEnergy())
		{
			sendPacket(SystemMsg.THE_SKILL_HAS_BEEN_CANCELED_BECAUSE_YOU_HAVE_INSUFFICIENT_ENERGY);
			return;
		}
		if(skill.getSoulsConsume() > 0)
		{
			setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);
		}
		if(skill.getEnergyConsume() > 0)
		{
			setAgathionEnergy(getAgathionEnergy() - skill.getEnergyConsume());
		}
		int level = Math.max(1, getSkillDisplayLevel(magicId));
		Formulas.calcSkillMastery(skill, this);
		long reuseDelay = Formulas.calcSkillReuseDelay(this, skill);
		if(!skill.isToggle())
		{
			broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), level, skill.getHitTime(), reuseDelay));
		}
		if(!skill.isHideUseMessage())
		{
			if(skill.getSkillType() == Skill.SkillType.PET_SUMMON)
			{
				sendPacket(new SystemMessage(547));
			}
			else if(!skill.isHandler())
			{
				sendPacket(new SystemMessage(46).addSkillName(magicId, level));
			}
			else
			{
				sendPacket(new SystemMessage(46).addItemName(skill.getItemConsumeId()[0]));
			}
		}
		if(!skill.isHandler())
		{
			disableSkill(skill, reuseDelay);
		}
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.AltMagicUseTask(this, target, skill), skill.getHitTime());
	}
	
	public void sendReuseMessage(Skill skill)
	{
	}
	
	public void broadcastPacket(L2GameServerPacket... packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}
	
	public void broadcastPacket(List<L2GameServerPacket> packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}
	
	public void broadcastPacketToOthers(L2GameServerPacket... packets)
	{
		if(!isVisible() || packets.length == 0)
		{
			return;
		}
		List<Player> players = World.getAroundPlayers(this);
		for(int i = 0;i < players.size();++i)
		{
			Player target = players.get(i);
			target.sendPacket(packets);
		}
	}
	
	public void broadcastPacketToOthers(List<L2GameServerPacket> packets)
	{
		if(!isVisible() || packets.isEmpty())
		{
			return;
		}
		List<Player> players = World.getAroundPlayers(this);
		for(int i = 0;i < players.size();++i)
		{
			Player target = players.get(i);
			target.sendPacket(packets);
		}
	}
	
	public void broadcastToStatusListeners(L2GameServerPacket... packets)
	{
		if(!isVisible() || packets.length == 0)
		{
			return;
		}
		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null || _statusListeners.isEmpty())
			{
				return;
			}
			for(int i = 0;i < _statusListeners.size();++i)
			{
				Player player = _statusListeners.get(i);
				player.sendPacket(packets);
			}
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}
	
	public void addStatusListener(Player cha)
	{
		if(cha == this)
		{
			return;
		}
		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null)
			{
				_statusListeners = new LazyArrayList<>();
			}
			if(!_statusListeners.contains(cha))
			{
				_statusListeners.add(cha);
			}
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}
	
	public void removeStatusListener(Creature cha)
	{
		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null)
			{
				return;
			}
			_statusListeners.remove(cha);
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}
	
	public void clearStatusListeners()
	{
		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null)
			{
				return;
			}
			_statusListeners.clear();
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}
	
	public StatusUpdate makeStatusUpdate(int... fields)
	{
		StatusUpdate su = new StatusUpdate(getObjectId());
		block10:
		for(int field : fields)
		{
			switch(field)
			{
				case 9:
				{
					su.addAttribute(field, (int) getCurrentHp());
					continue block10;
				}
				case 10:
				{
					su.addAttribute(field, getMaxHp());
					continue block10;
				}
				case 11:
				{
					su.addAttribute(field, (int) getCurrentMp());
					continue block10;
				}
				case 12:
				{
					su.addAttribute(field, getMaxMp());
					continue block10;
				}
				case 27:
				{
					su.addAttribute(field, getKarma());
					continue block10;
				}
				case 33:
				{
					su.addAttribute(field, (int) getCurrentCp());
					continue block10;
				}
				case 34:
				{
					su.addAttribute(field, getMaxCp());
					continue block10;
				}
				case 26:
				{
					su.addAttribute(field, getPvpFlag());
				}
			}
		}
		return su;
	}
	
	public void broadcastStatusUpdate()
	{
		if(!needStatusUpdate())
		{
			return;
		}
		StatusUpdate su = makeStatusUpdate(10, 12, 9, 11);
		broadcastToStatusListeners(su);
	}
	
	public int calcHeading(int x_dest, int y_dest)
	{
		return (int) (Math.atan2(getY() - y_dest, getX() - x_dest) * 10430.378350470453) + 32768;
	}
	
	public final double calcStat(Stats stat, double init)
	{
		return calcStat(stat, init, null, null);
	}
	
	public final double calcStat(Stats stat, double init, Creature target, Skill skill)
	{
		int id = stat.ordinal();
		Calculator c = _calculators[id];
		if(c == null)
		{
			return init;
		}
		Env env = new Env();
		env.character = this;
		env.target = target;
		env.skill = skill;
		env.value = init;
		c.calc(env);
		return env.value;
	}
	
	public final double calcStat(Stats stat, Creature target, Skill skill)
	{
		Env env = new Env(this, target, skill);
		env.value = stat.getInit();
		int id = stat.ordinal();
		Calculator c = _calculators[id];
		if(c != null)
		{
			c.calc(env);
		}
		return env.value;
	}
	
	public int calculateAttackDelay()
	{
		return Formulas.calcPAtkSpd(getPAtkSpd());
	}
	
	public void callSkill(Skill skill, List<Creature> targets, boolean useActionSkills)
	{
		try
		{
			if(useActionSkills && !skill.isUsingWhileCasting() && _triggers != null)
			{
				if(skill.isOffensive())
				{
					if(skill.isMagic())
					{
						useTriggers(getTarget(), TriggerType.OFFENSIVE_MAGICAL_SKILL_USE, null, skill, 0.0);
					}
					else
					{
						useTriggers(getTarget(), TriggerType.OFFENSIVE_PHYSICAL_SKILL_USE, null, skill, 0.0);
					}
				}
				else if(Config.BUFF_STICK_FOR_ALL || skill.isMagic())
				{
					boolean targetSelf = skill.isAoE() || skill.isNotTargetAoE() || skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF;
					useTriggers(targetSelf ? this : getTarget(), TriggerType.SUPPORT_MAGICAL_SKILL_USE, null, skill, 0.0);
				}
			}
			Player pl = getPlayer();
			Iterator<Creature> itr = targets.iterator();
			while(itr.hasNext())
			{
				Creature target = itr.next();
				if(skill.isOffensive() && target.isInvul())
				{
					Player pcTarget = target.getPlayer();
					if((!skill.isIgnoreInvul() || pcTarget != null && pcTarget.isGM()) && !target.isArtefact())
					{
						itr.remove();
						continue;
					}
				}
				Effect ie = target.getEffectList().getEffectByType(EffectType.IgnoreSkill);
				if(ie != null && ArrayUtils.contains(ie.getTemplate().getParam().getIntegerArray("skillId"), skill.getId()))
				{
					itr.remove();
					continue;
				}
				target.getListeners().onMagicHit(skill, this);
				if(pl != null && target != null && target.isNpc())
				{
					NpcInstance npc = (NpcInstance) target;
					List<QuestState> ql = pl.getQuestsForEvent(npc, QuestEventType.MOB_TARGETED_BY_SKILL);
					if(ql != null)
					{
						for(QuestState qs : ql)
						{
							qs.getQuest().notifySkillUse(npc, skill, qs);
						}
					}
				}
				if(skill.getNegateSkill() > 0)
				{
					for(Effect e : target.getEffectList().getAllEffects())
					{
						Skill efs = e.getSkill();
						if(efs.getId() != skill.getNegateSkill() || !e.isCancelable() || skill.getNegatePower() > 0 && efs.getPower() > (double) skill.getNegatePower())
							continue;
						e.exit();
					}
				}
				if(skill.getCancelTarget() <= 0 || !Rnd.chance(skill.getCancelTarget()) || target.getCastingSkill() != null && target.getCastingSkill().getSkillType() == Skill.SkillType.TAKECASTLE || target.isRaid())
					continue;
				target.abortAttack(true, true);
				target.abortCast(true, true);
				target.setTarget(null);
			}
			if(skill.isOffensive())
			{
				startAttackStanceTask();
			}
			if(!skill.isNotTargetAoE() || !skill.isOffensive() || targets.size() != 0)
			{
				skill.getEffects(this, this, false, true);
			}
			skill.useSkill(this, targets);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}
	
	public void useTriggers(GameObject target, TriggerType type, Skill ex, Skill owner, double damage)
	{
		if(_triggers == null)
		{
			return;
		}
		Set<TriggerInfo> SkillsOnSkillAttack = _triggers.get(type);
		if(SkillsOnSkillAttack != null)
		{
			for(TriggerInfo t : SkillsOnSkillAttack)
			{
				if(t.getSkill() == ex)
					continue;
				useTriggerSkill(target == null ? getTarget() : target, null, t, owner, damage);
			}
		}
	}
	
	public void useTriggerSkill(GameObject target, List<Creature> targets, TriggerInfo trigger, Skill owner, double damage)
	{
		Skill skill = trigger.getSkill();
		if(skill.getReuseDelay() > 0 && isSkillDisabled(skill))
		{
			return;
		}
		Creature aimTarget = skill.getAimingTarget(this, target);
		Creature realTarget;
		Creature creature = realTarget = target != null && target.isCreature() ? (Creature) target : null;
		if(Rnd.chance(trigger.getChance()) && trigger.checkCondition(this, realTarget, aimTarget, owner, damage) && skill.checkCondition(this, aimTarget, false, true, true))
		{
			if(targets == null)
			{
				targets = skill.getTargets(this, aimTarget, false);
			}
			int displayId = 0;
			int displayLevel = 0;
			if(skill.hasEffects())
			{
				displayId = skill.getEffectTemplates()[0]._displayId;
				displayLevel = skill.getEffectTemplates()[0]._displayLevel;
			}
			if(displayId == 0)
			{
				displayId = skill.getDisplayId();
			}
			if(displayLevel == 0)
			{
				displayLevel = skill.getDisplayLevel();
			}
			if(trigger.getType() != TriggerType.SUPPORT_MAGICAL_SKILL_USE)
			{
				for(Creature cha : targets)
				{
					broadcastPacket(new MagicSkillUse(this, cha, displayId, displayLevel, 0, 0));
				}
			}
			callSkill(skill, targets, false);
			disableSkill(skill, skill.getReuseDelay());
		}
	}
	
	public boolean checkBlockedStat(Stats stat)
	{
		return _blockedStats != null && _blockedStats.contains(stat);
	}
	
	public boolean checkReflectSkill(Creature attacker, Skill skill)
	{
		if(!skill.isReflectable())
		{
			return false;
		}
		if(isInvul() || attacker.isInvul() || !skill.isOffensive())
		{
			return false;
		}
		if(skill.isMagic() && skill.getSkillType() != Skill.SkillType.MDAM)
		{
			return false;
		}
		if(Rnd.chance(calcStat(skill.isMagic() ? Stats.REFLECT_MAGIC_SKILL : Stats.REFLECT_PHYSIC_SKILL, 0.0, attacker, skill)))
		{
			sendPacket(new SystemMessage(1998).addName(attacker));
			attacker.sendPacket(new SystemMessage(1999).addName(this));
			return true;
		}
		return false;
	}
	
	public void doCounterAttack(Skill skill, Creature attacker, boolean blow)
	{
		if(isDead())
		{
			return;
		}
		if(isDamageBlocked() || attacker.isDamageBlocked())
		{
			return;
		}
		if(skill == null || skill.hasEffects() || skill.isMagic() || !skill.isOffensive() || skill.getCastRange() > 200)
		{
			return;
		}
		if(Rnd.chance(calcStat(Stats.COUNTER_ATTACK, 0.0, attacker, skill)))
		{
			double damage = 1189 * getPAtk(attacker) / Math.max(attacker.getPDef(this), 1);
			attacker.sendPacket(new SystemMessage(1997).addName(this));
			if(blow)
			{
				sendPacket(new SystemMessage(1997).addName(this));
				sendPacket(new SystemMessage(35).addNumber((long) damage));
				attacker.reduceCurrentHp(damage, this, skill, true, true, false, false, false, false, true);
			}
			else
			{
				sendPacket(new SystemMessage(1997).addName(this));
			}
			sendPacket(new SystemMessage(35).addNumber((long) damage));
			attacker.reduceCurrentHp(damage, this, skill, true, true, false, false, false, false, true);
		}
	}
	
	public void disableSkill(Skill skill, long delay)
	{
		_skillReuses.put(skill.hashCode(), new TimeStamp(skill, delay));
	}
	
	public abstract boolean isAutoAttackable(Creature attacker);
	
	public void doAttack(Creature target)
	{
		if(target == null || isAMuted() || isAttackingNow() || isAlikeDead() || target.isAlikeDead() || !isInRange(target, 2048) || isPlayer() && getPlayer().isInMountTransform())
		{
			return;
		}
		getListeners().onAttack(target);
		Player player;
		if(Config.ALT_TELEPORT_PROTECTION && isPlayer() && (player = getPlayer()).getAfterTeleportPortectionTime() > System.currentTimeMillis())
		{
			player.setAfterTeleportPortectionTime(0);
			player.sendMessage(new CustomMessage("alt.teleport_protect_gonna", player));
		}
		int sAtk = Math.max(calculateAttackDelay(), Config.MIN_ATK_DELAY);
		_attackEndTime = (long) sAtk + System.currentTimeMillis() - (long) Config.ATTACK_END_DELAY;
		_isAttackAborted = false;
		WeaponTemplate weaponItem = getActiveWeaponItem();
		int reuse = sAtk;
		int ssGrade = 0;
		if(weaponItem != null)
		{
			reuse = sAtk + (int) ((float) weaponItem.getAttackReuseDelay() / ((float) getPAtkSpd() / 333.0f));
			if(isPlayer() && weaponItem.getAttackReuseDelay() > 0 && reuse > 0)
			{
				sendPacket(new SetupGauge(this, 1, reuse));
				_attackReuseEndTime = (long) reuse + System.currentTimeMillis() - (long) Config.ATTACK_END_DELAY;
			}
			ssGrade = weaponItem.getCrystalType().gradeOrd();
		}
		Attack attack = new Attack(this, target, getChargedSoulShot(), ssGrade);
		setHeading(PositionUtils.calculateHeadingFrom(this, target));
		int hitDelay = reuse / 2;
		if(weaponItem == null)
		{
			doAttackHitSimple(attack, target, 1.0, !isPlayer(), hitDelay, true);
		}
		else
		{
			switch(weaponItem.getItemType())
			{
				case BOW:
				{
					doAttackHitByBow(attack, target, hitDelay);
					break;
				}
				case POLE:
				{
					doAttackHitByPole(attack, target, hitDelay);
					break;
				}
				case DUAL:
				case DUALFIST:
				{
					doAttackHitByDual(attack, target, hitDelay);
					break;
				}
				default:
				{
					doAttackHitSimple(attack, target, 1.0, true, hitDelay, true);
				}
			}
		}
		if(attack.hasHits())
		{
			broadcastPacket(attack);
		}
	}
	
	private void doAttackHitSimple(Attack attack, Creature target, double multiplier, boolean unchargeSS, long hitDelay, boolean notify)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		boolean miss1 = Formulas.calcHitMiss(this, target);
		if(!miss1)
		{
			Formulas.AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
			damage1 = (int) (info.damage * multiplier);
			shld1 = info.shld;
			crit1 = info.crit;
		}
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, unchargeSS, notify, hitDelay), hitDelay);
		attack.addHit(target, damage1, miss1, crit1, shld1);
	}
	
	private void doAttackHitByBow(Attack attack, Creature target, long hitDelay)
	{
		WeaponTemplate activeWeapon = getActiveWeaponItem();
		if(activeWeapon == null)
		{
			return;
		}
		boolean miss1 = Formulas.calcHitMiss(this, target);
		if(Config.ALT_CONSUME_ARROWS)
		{
			reduceArrowCount();
		}
		boolean crit1 = false;
		boolean shld1 = false;
		int damage1 = 0;
		if(!miss1)
		{
			Formulas.AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
			damage1 = (int) info.damage;
			shld1 = info.shld;
			crit1 = info.crit;
		}
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, true, hitDelay), hitDelay);
		attack.addHit(target, damage1, miss1, crit1, shld1);
	}
	
	private void doAttackHitByDual(Attack attack, Creature target, long hitDelay)
	{
		Formulas.AttackInfo info;
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);
		if(!miss1)
		{
			info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
			damage1 = (int) info.damage;
			shld1 = info.shld;
			crit1 = info.crit;
		}
		boolean crit2 = false;
		boolean shld2 = false;
		int damage2 = 0;
		if(!miss2)
		{
			info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
			damage2 = (int) info.damage;
			shld2 = info.shld;
			crit2 = info.crit;
		}
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, false), hitDelay / 2);
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage2, crit2, miss2, attack._soulshot, shld2, false, true, hitDelay), hitDelay);
		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
	}
	
	private void doAttackHitByPole(Attack attack, Creature target, long hitDelay)
	{
		int angle = (int) calcStat(Stats.POLE_ATTACK_ANGLE, 90.0, target, null);
		int range = (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().baseAtkRange, target, null);
		int attackcountmax = (int) Math.round(calcStat(Stats.POLE_TARGET_COUNT, 0.0, target, null));
		if(isBoss())
		{
			attackcountmax += 27;
		}
		else if(isRaid())
		{
			attackcountmax += 12;
		}
		else if(isMonster() && getLevel() > 0)
		{
			attackcountmax = (int) ((double) attackcountmax + (double) getLevel() / 7.5);
		}
		_poleAttackCount = 1;
		if(!isInZonePeace())
		{
			double mult = 1.0;
			for(Creature t : getAroundCharacters(range, 200))
			{
				if(_poleAttackCount > attackcountmax)
					break;
				if(t == target || t.isDead() || !PositionUtils.isFacing(this, t, angle) || !t.isAutoAttackable(this))
					continue;
				doAttackHitSimple(attack, t, mult, false, hitDelay, false);
				mult *= Config.ALT_POLE_DAMAGE_MODIFIER;
				++_poleAttackCount;
			}
		}
		_poleAttackCount = 0;
		doAttackHitSimple(attack, target, 1.0, true, hitDelay, true);
	}
	
	public long getAnimationEndTime()
	{
		return _animationEndTime;
	}
	
	public void doCast(Skill skill, Creature target, boolean forceUse)
	{
		if(skill == null)
		{
			return;
		}
		int[] itemConsume = skill.getItemConsume();
		if(itemConsume[0] > 0)
		{
			for(int i = 0;i < itemConsume.length;++i)
			{
				if(consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
					continue;
				sendPacket(skill.isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : new SystemMessage(113).addSkillName(skill.getId(), skill.getLevel()));
				return;
			}
		}
		if(skill.getReferenceItemId() > 0 && !consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
		{
			return;
		}
		int magicId = skill.getId();
		if(target == null)
		{
			target = skill.getAimingTarget(this, getTarget());
		}
		if(target == null)
		{
			return;
		}
		getListeners().onMagicUse(skill, target, false);
		if(this != target)
		{
			setHeading(PositionUtils.calculateHeadingFrom(this, target));
		}
		int level = Math.max(1, getSkillDisplayLevel(magicId));
		int skillTime = skill.isSkillTimePermanent() ? skill.getHitTime() : Formulas.calcMAtkSpd(this, skill, skill.getHitTime());
		int skillInterruptTime = skill.getSkillInterruptTime();
		int minCastTime = Math.min(Config.SKILLS_CAST_TIME_MIN, skill.getHitTime());
		if(skillTime < minCastTime)
		{
			skillTime = minCastTime;
			skillInterruptTime = 0;
		}
		_animationEndTime = System.currentTimeMillis() + (long) skillTime;
		if(skill.isMagic() && !skill.isSkillTimePermanent() && getChargedSpiritShot() > 0)
		{
			skillTime = (int) (0.7 * (double) skillTime);
			skillInterruptTime = (int) (0.7 * (double) skillInterruptTime);
		}
		Formulas.calcSkillMastery(skill, this);
		long reuseDelay = Math.max(0, Formulas.calcSkillReuseDelay(this, skill));
		broadcastPacket(new MagicSkillUse(this, target, skill, skillTime, reuseDelay));
		if(!skill.isHandler())
		{
			disableSkill(skill, reuseDelay);
		}
		if(isPlayer())
		{
			if(skill.getSkillType() == Skill.SkillType.PET_SUMMON)
			{
				sendPacket(Msg.SUMMON_A_PET);
			}
			else if(!skill.isHandler())
			{
				sendPacket(new SystemMessage(46).addSkillName(magicId, level));
			}
			else
			{
				sendPacket(new SystemMessage(46).addItemName(skill.getItemConsumeId()[0]));
			}
		}
		if(skill.getTargetType() == Skill.SkillTargetType.TARGET_HOLY)
		{
			target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, 1);
		}
		double mpConsume1;
		double d = mpConsume1 = skill.isUsingWhileCasting() ? skill.getMpConsume() : skill.getMpConsume1();
		if(mpConsume1 > 0.0)
		{
			if(_currentMp < mpConsume1)
			{
				sendPacket(Msg.NOT_ENOUGH_MP);
				onCastEndTime();
				return;
			}
			reduceCurrentMp(mpConsume1, null);
		}
		_flyLoc = null;
		switch(skill.getFlyType())
		{
			case DUMMY:
			case CHARGE:
			{
				Location flyLoc = getFlyLocation(target, skill);
				if(flyLoc != null)
				{
					_flyLoc = flyLoc;
					broadcastPacket(new FlyToLocation(this, flyLoc, skill.getFlyType()));
					break;
				}
				_animationEndTime = 0;
				sendPacket(SystemMsg.CANNOT_SEE_TARGET);
				return;
			}
		}
		_castingSkill = skill;
		int skillLaunchTime = skillInterruptTime > 0 ? Math.max(0, skillTime - skillInterruptTime) : 0;
		_castInterruptTime = System.currentTimeMillis() + (long) skillLaunchTime;
		setCastingTarget(target);
		if(skill.isUsingWhileCasting())
		{
			callSkill(skill, skill.getTargets(this, target, forceUse), true);
		}
		_scheduledCastInterval = skillTime;
		if(skillTime > 333 && isPlayer())
		{
			sendPacket(new SetupGauge(this, 0, skillTime));
		}
		scheduleSkillLaunchedTask(forceUse, skillLaunchTime);
		scheduleSkillUseTask(forceUse, skillTime);
	}
	
	protected void scheduleSkillLaunchedTask(boolean forceUse, int skillLaunchTime)
	{
		_skillLaunchedTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.MagicLaunchedTask(this, forceUse), skillLaunchTime);
	}
	
	protected void scheduleSkillUseTask(boolean forceUse, int skillTime)
	{
		_skillTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.MagicUseTask(this, forceUse), skillTime);
	}
	
	public void clearCastVars()
	{
		_animationEndTime = 0;
		_castInterruptTime = 0;
		_castingSkill = null;
		_skillTask = null;
		_skillLaunchedTask = null;
		_flyLoc = null;
	}
	
	private Location getFlyLocation(GameObject target, Skill skill)
	{
		if(target != null && target != this)
		{
			Location loc;
			if(skill.isFlyToBack())
			{
				double radian = PositionUtils.convertHeadingToRadian(target.getHeading());
				loc = new Location(target.getX() + (int) (Math.sin(radian) * 40.0), target.getY() - (int) (Math.cos(radian) * 40.0), target.getZ());
			}
			else
			{
				double alpha = Math.atan2(getY() - target.getY(), getX() - target.getX());
				loc = new Location(target.getX() + (int) Math.round(Math.cos(alpha) * 40.0), target.getY() + (int) Math.round(Math.sin(alpha) * 40.0), target.getZ());
			}
			if(isFlying())
			{
				if(isPlayer() && ((Player) this).isInFlyingTransform() && (loc.z <= 0 || loc.z >= 6000))
				{
					return null;
				}
				if(GeoEngine.moveCheckInAir(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getColRadius(), getGeoIndex()) == null)
				{
					return null;
				}
			}
			else
			{
				loc.correctGeoZ();
				if(!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
				{
					loc = target.getLoc();
					if(!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
					{
						return null;
					}
				}
			}
			return loc;
		}
		double radian = PositionUtils.convertHeadingToRadian(getHeading());
		int x1 = -(int) (Math.sin(radian) * (double) skill.getFlyRadius());
		int y1 = (int) (Math.cos(radian) * (double) skill.getFlyRadius());
		if(isFlying())
		{
			return GeoEngine.moveCheckInAir(getX(), getY(), getZ(), getX() + x1, getY() + y1, getZ(), getColRadius(), getGeoIndex());
		}
		return GeoEngine.moveCheck(getX(), getY(), getZ(), getX() + x1, getY() + y1, getGeoIndex());
	}
	
	public final void doDie(Creature killer)
	{
		if(!isDead.compareAndSet(false, true))
		{
			return;
		}
		onDeath(killer);
	}
	
	protected void onDeath(Creature killer)
	{
		if(killer != null)
		{
			Player killerPlayer = killer.getPlayer();
			if(killerPlayer != null)
			{
				killerPlayer.getListeners().onKillIgnorePetOrSummon(this);
			}
			killer.getListeners().onKill(this);
			if(isPlayer() && killer.isPlayable())
			{
				_currentCp = 0.0;
			}
		}
		setTarget(null);
		stopMove();
		stopAttackStanceTask();
		stopRegeneration();
		_currentHp = 0.0;
		if(isBlessedByNoblesse() || isSalvation())
		{
			if(isSalvation() && isPlayer() && !getPlayer().isOlyParticipant() && !getPlayer().isResurectProhibited())
			{
				getPlayer().reviveRequest(getPlayer(), 100.0, false);
			}
			for(Effect e : getEffectList().getAllEffects())
			{
				if(e.getEffectType() != EffectType.BlessNoblesse && e.getSkill().getId() != 1325 && e.getSkill().getId() != 2168)
					continue;
				e.exit();
			}
		}
		else if(Config.ALT_PASSIVE_NOBLESS_ID == 0 || getKnownSkill(Config.ALT_PASSIVE_NOBLESS_ID) == null)
		{
			for(Effect e : getEffectList().getAllEffects())
			{
				if(e.getEffectType() == EffectType.Transformation || e.getSkill().isPreservedOnDeath())
					continue;
				e.exit();
			}
		}
		ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(this, CtrlEvent.EVT_DEAD, killer, null));
		getListeners().onDeath(killer);
		updateEffectIcons();
		updateStats();
		broadcastStatusUpdate();
	}
	
	protected void onRevive()
	{
	}
	
	public void enableSkill(Skill skill)
	{
		_skillReuses.remove(skill.hashCode());
	}
	
	public int getAbnormalEffect()
	{
		return _abnormalEffects;
	}
	
	public int getAbnormalEffect2()
	{
		return _abnormalEffects2;
	}
	
	public int getAbnormalEffect3()
	{
		return _abnormalEffects3;
	}
	
	public int getAccuracy()
	{
		return (int) calcStat(Stats.ACCURACY_COMBAT, 0.0, null, null);
	}
	
	public Collection<Skill> getAllSkills()
	{
		return _skills.values();
	}
	
	public final Skill[] getAllSkillsArray()
	{
		Collection<Skill> vals = _skills.values();
		return vals.toArray(new Skill[vals.size()]);
	}
	
	public final double getAttackSpeedMultiplier()
	{
		return 1.1 * (double) getPAtkSpd() / (double) getTemplate().basePAtkSpd;
	}
	
	public int getBuffLimit()
	{
		return (int) calcStat(Stats.BUFF_LIMIT, Config.ALT_BUFF_LIMIT, null, null);
	}
	
	public Skill getCastingSkill()
	{
		return _castingSkill;
	}
	
	public int getCON()
	{
		return (int) calcStat(Stats.STAT_CON, _template.baseCON, null, null);
	}
	
	public int getCriticalHit(Creature target, Skill skill)
	{
		return (int) calcStat(Stats.CRITICAL_BASE, _template.baseCritRate, target, skill);
	}
	
	public double getMagicCriticalRate(Creature target, Skill skill)
	{
		return calcStat(Stats.MCRITICAL_RATE, target, skill);
	}
	
	public final double getCurrentCp()
	{
		return _currentCp;
	}
	
	public final void setCurrentCp(double newCp)
	{
		setCurrentCp(newCp, true);
	}
	
	public final double getCurrentCpRatio()
	{
		return getCurrentCp() / (double) getMaxCp();
	}
	
	public final double getCurrentCpPercents()
	{
		return getCurrentCpRatio() * 100.0;
	}
	
	public final boolean isCurrentCpFull()
	{
		return getCurrentCp() >= (double) getMaxCp();
	}
	
	public final boolean isCurrentCpZero()
	{
		return getCurrentCp() < 1.0;
	}
	
	public final double getCurrentHp()
	{
		return _currentHp;
	}
	
	public final double getCurrentHpRatio()
	{
		return getCurrentHp() / (double) getMaxHp();
	}
	
	public final double getCurrentHpPercents()
	{
		return getCurrentHpRatio() * 100.0;
	}
	
	public final boolean isCurrentHpFull()
	{
		return getCurrentHp() >= (double) getMaxHp();
	}
	
	public final boolean isCurrentHpZero()
	{
		return getCurrentHp() < 1.0;
	}
	
	public final double getCurrentMp()
	{
		return _currentMp;
	}
	
	public final void setCurrentMp(double newMp)
	{
		setCurrentMp(newMp, true);
	}
	
	public final double getCurrentMpRatio()
	{
		return getCurrentMp() / (double) getMaxMp();
	}
	
	public final double getCurrentMpPercents()
	{
		return getCurrentMpRatio() * 100.0;
	}
	
	public final boolean isCurrentMpFull()
	{
		return getCurrentMp() >= (double) getMaxMp();
	}
	
	public final boolean isCurrentMpZero()
	{
		return getCurrentMp() < 1.0;
	}
	
	public int getDEX()
	{
		return (int) calcStat(Stats.STAT_DEX, _template.baseDEX, null, null);
	}
	
	public int getEvasionRate(Creature target)
	{
		return (int) calcStat(Stats.EVASION_RATE, 0.0, target, null);
	}
	
	public int getINT()
	{
		return (int) calcStat(Stats.STAT_INT, _template.baseINT, null, null);
	}
	
	public List<Creature> getAroundCharacters(int radius, int height)
	{
		if(!isVisible())
		{
			return Collections.emptyList();
		}
		return World.getAroundCharacters(this, radius, height);
	}
	
	public List<NpcInstance> getAroundNpc(int range, int height)
	{
		if(!isVisible())
		{
			return Collections.emptyList();
		}
		return World.getAroundNpc(this, range, height);
	}
	
	public boolean knowsObject(GameObject obj)
	{
		return World.getAroundObjectById(this, obj.getObjectId()) != null;
	}
	
	public final Skill getKnownSkill(int skillId)
	{
		return _skills.get(skillId);
	}
	
	public final int getMagicalAttackRange(Skill skill)
	{
		if(skill != null)
		{
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		}
		return getTemplate().baseAtkRange;
	}
	
	public final int getMagicalAttackRange(double base, Skill skill)
	{
		if(skill != null)
		{
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, base, null, skill);
		}
		return getTemplate().baseAtkRange;
	}
	
	public int getMAtk(Creature target, Skill skill)
	{
		if(skill != null && skill.getMatak() > 0)
		{
			return skill.getMatak();
		}
		return (int) calcStat(Stats.MAGIC_ATTACK, _template.baseMAtk, target, skill);
	}
	
	public int getMAtkSpd()
	{
		return (int) calcStat(Stats.MAGIC_ATTACK_SPEED, _template.baseMAtkSpd, null, null);
	}
	
	public final int getMaxCp()
	{
		return (int) calcStat(Stats.MAX_CP, _template.baseCpMax, null, null);
	}
	
	public int getMaxHp()
	{
		return (int) calcStat(Stats.MAX_HP, _template.baseHpMax, null, null);
	}
	
	public int getMaxMp()
	{
		return (int) calcStat(Stats.MAX_MP, _template.baseMpMax, null, null);
	}
	
	public int getMDef(Creature target, Skill skill)
	{
		return Math.max((int) calcStat(Stats.MAGIC_DEFENCE, _template.baseMDef, target, skill), 1);
	}
	
	public int getMEN()
	{
		return (int) calcStat(Stats.STAT_MEN, _template.baseMEN, null, null);
	}
	
	public double getMinDistance(GameObject obj)
	{
		double distance = getTemplate().collisionRadius;
		if(obj != null && obj.isCreature())
		{
			distance += ((Creature) obj).getTemplate().collisionRadius;
		}
		return distance;
	}
	
	public double getMovementSpeedMultiplier()
	{
		return (double) getRunSpeed() * 1.0 / (double) _template.baseRunSpd;
	}
	
	@Override
	public int getMoveSpeed()
	{
		if(isRunning())
		{
			return getRunSpeed();
		}
		return getWalkSpeed();
	}
	
	@Override
	public String getName()
	{
		return StringUtils.defaultString(_name);
	}
	
	public final void setName(String name)
	{
		_name = name;
	}
	
	public int getPAtk(Creature target)
	{
		return (int) calcStat(Stats.POWER_ATTACK, _template.basePAtk, target, null);
	}
	
	public int getPAtkSpd()
	{
		return (int) calcStat(Stats.POWER_ATTACK_SPEED, _template.basePAtkSpd, null, null);
	}
	
	public int getPDef(Creature target)
	{
		return (int) calcStat(Stats.POWER_DEFENCE, _template.basePDef, target, null);
	}
	
	public final int getPhysicalAttackRange()
	{
		WeaponTemplate weaponItem = getActiveWeaponItem();
		if(weaponItem == null)
		{
			return (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().baseAtkRange, null, null);
		}
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, weaponItem.getAttackRange(), null, null);
	}
	
	@Deprecated
	public final int getRandomDamage()
	{
		WeaponTemplate weaponItem = getActiveWeaponItem();
		if(weaponItem == null)
		{
			return 5 + (int) Math.sqrt(getLevel());
		}
		return weaponItem.getRandomDamage();
	}
	
	public double getReuseModifier(Creature target)
	{
		return calcStat(Stats.ATK_REUSE, 1.0, target, null);
	}
	
	public int getRunSpeed()
	{
		return getSpeed(_template.baseRunSpd);
	}
	
	public final int getShldDef()
	{
		if(isPlayer())
		{
			return (int) calcStat(Stats.SHIELD_DEFENCE, 0.0, null, null);
		}
		return (int) calcStat(Stats.SHIELD_DEFENCE, _template.baseShldDef, null, null);
	}
	
	public final int getSkillDisplayLevel(Integer skillId)
	{
		Skill skill = _skills.get(skillId);
		if(skill == null)
		{
			return -1;
		}
		return skill.getDisplayLevel();
	}
	
	public final int getSkillLevel(Integer skillId)
	{
		return getSkillLevel(skillId, -1);
	}
	
	public final int getSkillLevel(Integer skillId, int def)
	{
		Skill skill = _skills.get(skillId);
		if(skill == null)
		{
			return def;
		}
		return skill.getLevel();
	}
	
	public int getSkillMastery(Integer skillId)
	{
		if(_skillMastery == null)
		{
			return 0;
		}
		Integer val = _skillMastery.get(skillId);
		return val == null ? 0 : val;
	}
	
	public void removeSkillMastery(Integer skillId)
	{
		if(_skillMastery != null)
		{
			_skillMastery.remove(skillId);
		}
	}
	
	public int getSpeed(int baseSpeed)
	{
		if(isInWater())
		{
			return getSwimSpeed();
		}
		return (int) calcStat(Stats.RUN_SPEED, baseSpeed, null, null);
	}
	
	public int getSTR()
	{
		return (int) calcStat(Stats.STAT_STR, _template.getBaseSTR(), null, null);
	}
	
	public int getSwimSpeed()
	{
		return (int) calcStat(Stats.RUN_SPEED, Config.SWIMING_SPEED, null, null);
	}
	
	public GameObject getTarget()
	{
		return target.get();
	}
	
	public void setTarget(GameObject object)
	{
		if(object != null && !object.isVisible())
		{
			object = null;
		}
		target = object == null ? HardReferences.emptyRef() : object.getRef();
	}
	
	public final int getTargetId()
	{
		GameObject target = getTarget();
		return target == null ? -1 : target.getObjectId();
	}
	
	public CharTemplate getTemplate()
	{
		return _template;
	}
	
	public CharTemplate getBaseTemplate()
	{
		return _baseTemplate;
	}
	
	public String getTitle()
	{
		return StringUtils.defaultString(_title);
	}
	
	public void setTitle(String title)
	{
		_title = title;
	}
	
	public final int getWalkSpeed()
	{
		if(isInWater())
		{
			return getSwimSpeed();
		}
		return getSpeed(_template.baseWalkSpd);
	}
	
	public int getWIT()
	{
		return (int) calcStat(Stats.STAT_WIT, _template.baseWIT, null, null);
	}
	
	public double headingToRadians(int heading)
	{
		return (double) (heading - 32768) / 10430.378350470453;
	}
	
	public boolean isAlikeDead()
	{
		return isDead();
	}
	
	public final boolean isAttackingNow()
	{
		return _attackEndTime > System.currentTimeMillis();
	}
	
	public final boolean isBlessedByNoblesse()
	{
		return _isBlessedByNoblesse;
	}
	
	public final boolean isSalvation()
	{
		return _isSalvation;
	}
	
	public boolean isEffectImmune()
	{
		return _effectImmunity.get();
	}
	
	public boolean isBuffImmune()
	{
		return _buffImmunity.get();
	}
	
	public boolean isDebuffImmune()
	{
		return _debuffImmunity.get();
	}
	
	public boolean isDead()
	{
		return _currentHp < 0.5 || isDead.get();
	}
	
	@Override
	public final boolean isFlying()
	{
		return _flying;
	}
	
	public final void setFlying(boolean mode)
	{
		_flying = mode;
	}
	
	public final boolean isInCombat()
	{
		return System.currentTimeMillis() < _stanceEndTime;
	}
	
	public boolean isInvul()
	{
		return _isInvul;
	}
	
	public boolean isMageClass()
	{
		return getTemplate().baseMAtk > 3;
	}
	
	public final boolean isRunning()
	{
		return _running;
	}
	
	public boolean isSkillDisabled(Skill skill)
	{
		TimeStamp sts = _skillReuses.get(skill.hashCode());
		if(sts == null)
		{
			return false;
		}
		if(sts.hasNotPassed())
		{
			return true;
		}
		_skillReuses.remove(skill.hashCode());
		return false;
	}
	
	public final boolean isTeleporting()
	{
		return isTeleporting.get();
	}
	
	public Location getDestination()
	{
		if(moveAction != null && moveAction instanceof MoveToLocationAction)
		{
			return moveAction.moveTo().clone();
		}
		return null;
	}
	
	public boolean isMoving()
	{
		MoveActionBase theMoveActionBase = moveAction;
		return theMoveActionBase != null && !theMoveActionBase.isFinished();
	}
	
	public boolean isFollowing()
	{
		MoveActionBase theMoveActionBase = moveAction;
		return theMoveActionBase != null && theMoveActionBase instanceof MoveToRelativeAction && !theMoveActionBase.isFinished();
	}
	
	public int maxZDiff()
	{
		MoveActionBase theMoveActionBase = moveAction;
		if(theMoveActionBase != null)
		{
			Location moveFrom = theMoveActionBase.moveFrom();
			Location moveTo = theMoveActionBase.moveTo();
			if(moveFrom.getZ() > moveTo.getZ())
			{
				int maxZDiff = moveFrom.getZ() - moveTo.getZ();
				return maxZDiff;
			}
		}
		return Config.MAX_Z_DIFF;
	}
	
	public Creature getFollowTarget()
	{
		if(moveAction != null && moveAction instanceof Creature.MoveToRelativeAction && !moveAction.isFinished())
		{
			Creature.MoveToRelativeAction mtra = (Creature.MoveToRelativeAction) moveAction;
			GameObject target = mtra.getTarget();
			if(target != null && target instanceof Creature)
			{
				return (Creature) target;
			}
		}
		return null;
	}
	
	protected MoveActionBase createMoveToRelative(GameObject pawn, int indent, int range, boolean pathfinding)
	{
		return new MoveToRelativeAction(this, pawn, !Config.ALLOW_GEODATA, indent, range, pathfinding);
	}
	
	protected MoveActionBase createMoveToLocation(Location dest, int indent, boolean pathFind)
	{
		return new MoveToLocationAction(this, getLoc(), dest, isInBoat() || isBoat() || !Config.ALLOW_GEODATA, indent, pathFind);
	}
	
	public boolean moveToLocation(Location loc, int offset, boolean pathfinding)
	{
		return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding);
	}
	
	public boolean moveToLocation(int toX, int toY, int toZ, int indent, boolean pathfinding)
	{
		moveLock.lock();
		try
		{
			indent = Math.max(indent, 0);
			Location worldTo = new Location(toX, toY, toZ);
			MoveActionBase prevMoveAction = moveAction;
			if(prevMoveAction != null && prevMoveAction instanceof MoveToLocationAction && ((MoveToLocationAction) prevMoveAction).isSameDest(worldTo))
			{
				sendActionFailed();
				boolean bl = false;
				return bl;
			}
			if(isMovementDisabled())
			{
				getAI().setNextAction(NextAction.MOVE, new Location(toX, toY, toZ), indent, pathfinding, false);
				sendActionFailed();
				boolean bl = false;
				return bl;
			}
			getAI().clearNextAction();
			if(isPlayer())
			{
				Player player = getPlayer();
				getAI().changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				if(Config.ALT_TELEPORT_PROTECTION && isPlayer() && player.getAfterTeleportPortectionTime() > System.currentTimeMillis())
				{
					player.setAfterTeleportPortectionTime(0);
					player.sendMessage(new CustomMessage("alt.teleport_protect_gonna", player));
				}
			}
			stopMove(false, false);
			MoveActionBase mtla;
			moveAction = mtla = createMoveToLocation(worldTo, indent, pathfinding);
			if(!mtla.start())
			{
				moveAction = null;
				sendActionFailed();
				boolean bl = false;
				return bl;
			}
			mtla.scheduleNextTick();
			boolean bl = true;
			return bl;
		}
		finally
		{
			moveLock.unlock();
		}
	}
	
	public boolean moveToRelative(GameObject pawn, int indent, int range)
	{
		return moveToRelative(pawn, indent, range, Config.ALLOW_PAWN_PATHFIND);
	}
	
	public boolean moveToRelative(GameObject pawn, int indent, int range, boolean pathfinding)
	{
		moveLock.lock();
		try
		{
			if(isMovementDisabled() || pawn == null || isInBoat())
			{
				boolean bl = false;
				return bl;
			}
			MoveActionBase prevMoveAction = moveAction;
			if(prevMoveAction != null && prevMoveAction instanceof MoveToRelativeAction && !prevMoveAction.isFinished() && ((MoveToRelativeAction) prevMoveAction).isSameTarget(pawn))
			{
				sendActionFailed();
				boolean bl = false;
				return bl;
			}
			range = Math.max(range, 10);
			indent = Math.min(indent, range);
			getAI().clearNextAction();
			if(isPlayer())
			{
				Player player = getPlayer();
				if(Config.ALT_TELEPORT_PROTECTION && isPlayer() && player.getAfterTeleportPortectionTime() > System.currentTimeMillis())
				{
					player.setAfterTeleportPortectionTime(0);
					player.sendMessage(new CustomMessage("alt.teleport_protect_gonna", player));
				}
			}
			stopMove(false, false);
			MoveActionBase mtra;
			moveAction = mtra = createMoveToRelative(pawn, indent, range, pathfinding);
			if(!mtra.start())
			{
				moveAction = null;
				sendActionFailed();
				boolean bl = false;
				return bl;
			}
			mtra.scheduleNextTick();
			boolean bl = true;
			return bl;
		}
		finally
		{
			moveLock.unlock();
		}
	}
	
	private void broadcastMove()
	{
		validateLocation(isPlayer() ? 2 : 1);
		broadcastPacket(movePacket());
	}
	
	public void stopMove()
	{
		stopMove(true, true);
	}
	
	public void stopMove(boolean validate)
	{
		stopMove(true, validate);
	}
	
	public void stopMove(boolean stop, boolean validate)
	{
		stopMove(stop, validate, true);
	}
	
	public void stopMove(boolean stop, boolean validate, boolean action)
	{
		if(!isMoving())
		{
			return;
		}
		moveLock.lock();
		try
		{
			if(!isMoving())
			{
				return;
			}
			if(action && moveAction != null && !moveAction.isFinished())
			{
				moveAction.interrupt();
				moveAction = null;
			}
			if(_moveTask != null)
			{
				_moveTask.cancel(false);
				_moveTask = null;
			}
			if(validate)
			{
				validateLocation(isPlayer() ? 2 : 1);
			}
			if(stop)
			{
				broadcastPacket(stopMovePacket());
			}
		}
		finally
		{
			moveLock.unlock();
		}
	}
	
	public int getWaterZ()
	{
		if(!isInWater())
		{
			return Integer.MIN_VALUE;
		}
		Zone[] zones = _zonesRef.get();
		int waterZ = Integer.MIN_VALUE;
		for(Zone zone : zones)
		{
			if(zone.getType() != Zone.ZoneType.water || waterZ != Integer.MIN_VALUE && waterZ >= zone.getTerritory().getZmax())
				continue;
			waterZ = zone.getTerritory().getZmax();
		}
		return waterZ;
	}
	
	protected L2GameServerPacket stopMovePacket()
	{
		return new StopMove(this);
	}
	
	public L2GameServerPacket movePacket()
	{
		MoveActionBase moveAction = this.moveAction;
		if(moveAction != null)
		{
			return moveAction.movePacket();
		}
		return new CharMoveToLocation(this);
	}
	
	public boolean updateZones()
	{
		if(isInObserverMode())
		{
			return false;
		}
		Zone[] regionZones = isVisible() ? getCurrentRegion().getZones() : Zone.EMPTY_L2ZONE_ARRAY;
		Zone[] currZones;
		Zone[] newZones = currZones = _zonesRef.get();
		Zone zone;
		int currZoneIdx;
		if(currZones.length > 0)
		{
			for(currZoneIdx = 0;currZoneIdx < currZones.length;++currZoneIdx)
			{
				zone = currZones[currZoneIdx];
				if(ArrayUtils.contains(regionZones, zone) && zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
					continue;
				newZones = (Zone[]) ArrayUtils.removeElement((Object[]) newZones, zone);
			}
		}
		if(regionZones.length > 0)
		{
			for(int regionZoneIdx = 0;regionZoneIdx < regionZones.length;++regionZoneIdx)
			{
				zone = regionZones[regionZoneIdx];
				if(ArrayUtils.contains(currZones, zone) || !zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
					continue;
				newZones = ArrayUtils.add(newZones, zone);
			}
		}
		if(currZones != newZones && _zonesRef.compareAndSet(currZones, newZones))
		{
			for(currZoneIdx = 0;currZoneIdx < currZones.length;++currZoneIdx)
			{
				zone = currZones[currZoneIdx];
				if(ArrayUtils.contains(newZones, zone))
					continue;
				zone.doLeave(this);
			}
			for(int newZoneIdx = 0;newZoneIdx < newZones.length;++newZoneIdx)
			{
				zone = newZones[newZoneIdx];
				if(ArrayUtils.contains(currZones, zone))
					continue;
				zone.doEnter(this);
			}
			return true;
		}
		return false;
	}
	
	public boolean isInZonePeace()
	{
		return isInZone(Zone.ZoneType.peace_zone) && !isInZoneBattle();
	}
	
	public boolean isInZoneBattle()
	{
		return isInZone(Zone.ZoneType.battle_zone);
	}
	
	public boolean isInWater()
	{
		return isInZone(Zone.ZoneType.water) && !isInBoat() && !isBoat() && !isFlying();
	}
	
	public boolean isInZone(Zone.ZoneType type)
	{
		Zone[] zones = _zonesRef.get();
		for(Zone zone : zones)
		{
			if(zone.getType() != type)
				continue;
			return true;
		}
		return false;
	}
	
	public boolean isInZone(String name)
	{
		Zone[] zones = _zonesRef.get();
		for(Zone zone : zones)
		{
			if(!zone.getName().equals(name))
				continue;
			return true;
		}
		return false;
	}
	
	public boolean isInZone(Zone zone)
	{
		Object[] zones = _zonesRef.get();
		return ArrayUtils.contains(zones, zone);
	}
	
	public Zone getZone(Zone.ZoneType type)
	{
		Zone[] zones = _zonesRef.get();
		for(Zone zone : zones)
		{
			if(zone.getType() != type)
				continue;
			return zone;
		}
		return null;
	}
	
	public Location getRestartPoint()
	{
		Zone[] zones = _zonesRef.get();
		for(Zone zone : zones)
		{
			Zone.ZoneType type;
			if(zone.getRestartPoints() == null || (type = zone.getType()) != Zone.ZoneType.battle_zone && type != Zone.ZoneType.peace_zone && type != Zone.ZoneType.offshore && type != Zone.ZoneType.dummy)
				continue;
			return zone.getSpawn();
		}
		return null;
	}
	
	public Location getPKRestartPoint()
	{
		Zone[] zones = _zonesRef.get();
		for(Zone zone : zones)
		{
			Zone.ZoneType type;
			if(zone.getRestartPoints() == null || (type = zone.getType()) != Zone.ZoneType.battle_zone && type != Zone.ZoneType.peace_zone && type != Zone.ZoneType.offshore && type != Zone.ZoneType.dummy)
				continue;
			return zone.getPKSpawn();
		}
		return null;
	}
	
	@Override
	public int getGeoZ(Location loc)
	{
		if(isFlying() || isInWater() || isInBoat() || isBoat() || isDoor())
		{
			return loc.z;
		}
		return super.getGeoZ(loc);
	}
	
	protected boolean needStatusUpdate()
	{
		if(!isVisible())
		{
			return false;
		}
		boolean result = false;
		int bar = (int) (getCurrentHp() * 352.0 / (double) getMaxHp());
		if(bar == 0 || bar != _lastHpBarUpdate)
		{
			_lastHpBarUpdate = bar;
			result = true;
		}
		if((bar = (int) (getCurrentMp() * 352.0 / (double) getMaxMp())) == 0 || bar != _lastMpBarUpdate)
		{
			_lastMpBarUpdate = bar;
			result = true;
		}
		if(isPlayer() && ((bar = (int) (getCurrentCp() * 352.0 / (double) getMaxCp())) == 0 || bar != _lastCpBarUpdate))
		{
			_lastCpBarUpdate = bar;
			result = true;
		}
		return result;
	}
	
	@Override
	public void onForcedAttack(Player player, boolean shift)
	{
		player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
		if(!isAttackable(player) || player.isConfused() || player.isBlocked())
		{
			player.sendActionFailed();
			return;
		}
		player.getAI().Attack(this, true, shift);
	}
	
	public void onHitTimer(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS)
	{
		if(isAlikeDead())
		{
			sendActionFailed();
			return;
		}
		if(target.isDead() || !isInRange(target, 2000))
		{
			sendActionFailed();
			return;
		}
		if(isPlayable() && target.isPlayable() && isInZoneBattle() != target.isInZoneBattle())
		{
			Player player = getPlayer();
			if(player != null)
			{
				player.sendPacket(Msg.INVALID_TARGET);
				player.sendActionFailed();
			}
			return;
		}
		target.getListeners().onAttackHit(this);
		if(!miss && target.isPlayer() && (isCursedWeaponEquipped() || getActiveWeaponInstance() != null && getActiveWeaponInstance().isHeroWeapon() && target.isCursedWeaponEquipped()))
		{
			target.setCurrentCp(0.0);
		}
		if(target.isStunned() && Formulas.calcStunBreak(crit))
		{
			target.getEffectList().stopEffects(EffectType.Stun);
		}
		displayGiveDamageMessage(target, damage, crit, miss, shld, false);
		ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(target, CtrlEvent.EVT_ATTACKED, this, damage));
		boolean checkPvP = checkPvP(target, null);
		if(!miss && damage > 0)
		{
			target.reduceCurrentHp(damage, this, null, true, true, false, true, false, false, true);
			if(!target.isDead())
			{
				if(crit)
				{
					useTriggers(target, TriggerType.CRIT, null, null, damage);
				}
				useTriggers(target, TriggerType.ATTACK, null, null, damage);
				if(Formulas.calcCastBreak(target, crit))
				{
					target.abortCast(false, true);
				}
			}
			if(soulshot && unchargeSS)
			{
				unChargeShots(false);
			}
		}
		if(miss)
		{
			target.useTriggers(this, TriggerType.UNDER_MISSED_ATTACK, null, null, damage);
		}
		startAttackStanceTask();
		if(checkPvP)
		{
			startPvPFlag(target);
		}
	}
	
	public void onMagicUseTimer(Creature aimingTarget, Skill skill, boolean forceUse)
	{
		_castInterruptTime = 0;
		if(skill.isUsingWhileCasting())
		{
			aimingTarget.getEffectList().stopEffect(skill.getId());
			onCastEndTime();
			return;
		}
		if(!skill.isOffensive() && getAggressionTarget() != null)
		{
			forceUse = true;
		}
		if(!skill.checkCondition(this, aimingTarget, forceUse, false, false))
		{
			if(skill.getSkillType() == Skill.SkillType.PET_SUMMON && isPlayer())
			{
				getPlayer().setPetControlItem(null);
			}
			onCastEndTime();
			return;
		}
		List<Creature> targets = skill.getTargets(this, aimingTarget, forceUse);
		int hpConsume = skill.getHpConsume();
		if(hpConsume > 0)
		{
			setCurrentHp(Math.max(0.0, _currentHp - (double) hpConsume), false);
		}
		double mpConsume2;
		if((mpConsume2 = skill.getMpConsume2()) > 0.0)
		{
			if(skill.isMusic())
			{
				mpConsume2 += (double) getEffectList().getActiveMusicCount(skill.getId()) * mpConsume2 / 2.0;
				mpConsume2 = calcStat(Stats.MP_DANCE_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			}
			else
			{
				mpConsume2 = skill.isMagic() ? calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill) : calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			}
			if(_currentMp < mpConsume2 && isPlayable())
			{
				sendPacket(Msg.NOT_ENOUGH_MP);
				onCastEndTime();
				return;
			}
			reduceCurrentMp(mpConsume2, null);
		}
		callSkill(skill, targets, true);
		if(skill.getNumCharges() > 0)
		{
			setIncreasedForce(getIncreasedForce() - skill.getNumCharges());
		}
		if(skill.isSoulBoost())
		{
			setConsumedSouls(getConsumedSouls() - Math.min(getConsumedSouls(), 5), null);
		}
		else if(skill.getSoulsConsume() > 0)
		{
			setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);
		}
		switch(skill.getFlyType())
		{
			case THROW_UP:
			case THROW_HORIZONTAL:
			{
				for(Creature target : targets)
				{
					Location flyLoc = getFlyLocation(null, skill);
					target.setLoc(flyLoc);
					broadcastPacket(new FlyToLocation(target, flyLoc, skill.getFlyType()));
				}
				break;
			}
		}
		int skillCoolTime = Formulas.calcMAtkSpd(this, skill, skill.getCoolTime());
		if(skillCoolTime > 0)
		{
			ThreadPoolManager.getInstance().schedule(new GameObjectTasks.CastEndTimeTask(this), skillCoolTime);
		}
		else
		{
			onCastEndTime();
		}
	}
	
	public void onCastEndTime()
	{
		finishFly();
		clearCastVars();
		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING, null, null);
	}
	
	private void finishFly()
	{
		Location flyLoc = _flyLoc;
		_flyLoc = null;
		if(flyLoc != null)
		{
			setLoc(flyLoc);
			validateLocation(1);
		}
	}
	
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(attacker == null || isDead() || attacker.isDead() && !isDot)
		{
			return;
		}
		if(isDamageBlocked() && transferDamage)
		{
			return;
		}
		if(isDamageBlocked() && attacker != this)
		{
			if(sendMessage)
			{
				attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
			}
			return;
		}
		if(canReflect)
		{
			if(attacker.absorbAndReflect(this, skill, damage, sendMessage))
			{
				return;
			}
			damage = absorbToEffector(attacker, damage);
			damage = absorbToSummon(attacker, damage);
		}
		getListeners().onCurrentHpDamage(damage, attacker, skill);
		if(attacker != this)
		{
			if(sendMessage)
			{
				displayReceiveDamageMessage(attacker, (int) damage);
			}
			if(!isDot)
			{
				useTriggers(attacker, TriggerType.RECEIVE_DAMAGE, null, null, damage);
			}
		}
		onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}
	
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(awake && isSleeping())
		{
			getEffectList().stopEffects(EffectType.Sleep);
		}
		if(attacker != this || skill != null && skill.isOffensive())
		{
			Effect effect;
			if(isMeditated() && (effect = getEffectList().getEffectByType(EffectType.Meditation)) != null)
			{
				getEffectList().stopEffect(effect.getSkill());
			}
			startAttackStanceTask();
			checkAndRemoveInvisible();
			if(getCurrentHp() - damage < 0.5)
			{
				useTriggers(attacker, TriggerType.DIE, null, null, damage);
			}
		}
		if(isPlayer() && getPlayer().isGM() && getPlayer().isUndying() && damage + 0.5 >= getCurrentHp())
		{
			return;
		}
		setCurrentHp(Math.max(getCurrentHp() - damage, 0.0), false);
		if(getCurrentHp() < 0.5)
		{
			doDie(attacker);
		}
	}
	
	public void reduceCurrentMp(double i, Creature attacker)
	{
		reduceCurrentMp(i, attacker, false);
	}
	
	public void reduceCurrentMp(double i, Creature attacker, boolean sendMessage)
	{
		if(attacker != null && attacker != this)
		{
			if(isSleeping())
			{
				getEffectList().stopEffects(EffectType.Sleep);
			}
			Effect effect;
			if(isMeditated() && (effect = getEffectList().getEffectByType(EffectType.Meditation)) != null)
			{
				getEffectList().stopEffect(effect.getSkill());
			}
		}
		if(isDamageBlocked() && attacker != null && attacker != this)
		{
			attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}
		if(attacker != null && attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			if(attacker.getKarma() > 0 && getEffectList().getEffectsBySkillId(5182) != null && !isInZone(Zone.ZoneType.SIEGE))
			{
				return;
			}
			if(getKarma() > 0 && attacker.getEffectList().getEffectsBySkillId(5182) != null && !attacker.isInZone(Zone.ZoneType.SIEGE))
			{
				return;
			}
		}
		getListeners().onCurrentMpReduce(i, attacker);
		if(sendMessage)
		{
			int msgMp = (int) Math.min(_currentMp, i);
			sendPacket(new SystemMessage(1866).addNumber(msgMp));
			if(attacker != null && attacker.isPlayer())
			{
				attacker.sendPacket(new SystemMessage(1867).addNumber(msgMp));
			}
		}
		i = Math.max(0.0, _currentMp - i);
		setCurrentMp(i);
		if(attacker != null && attacker != this)
		{
			startAttackStanceTask();
		}
	}
	
	public double relativeSpeed(GameObject target)
	{
		return (double) getMoveSpeed() - (double) target.getMoveSpeed() * Math.cos(headingToRadians(getHeading()) - headingToRadians(target.getHeading()));
	}
	
	public void removeAllSkills()
	{
		for(Skill s : getAllSkillsArray())
		{
			removeSkill(s);
		}
	}
	
	public void removeBlockStats(List<Stats> stats)
	{
		if(_blockedStats != null)
		{
			_blockedStats.removeAll(stats);
			if(_blockedStats.isEmpty())
			{
				_blockedStats = null;
			}
		}
	}
	
	public Skill removeSkill(Skill skill)
	{
		if(skill == null)
		{
			return null;
		}
		return removeSkillById(skill.getId());
	}
	
	public Skill removeSkillById(Integer id)
	{
		Skill oldSkill = _skills.remove(id);
		if(oldSkill != null)
		{
			removeTriggers(oldSkill);
			removeStatsOwner(oldSkill);
			if(Config.ALT_DELETE_SA_BUFFS && (oldSkill.isItemSkill() || oldSkill.isHandler()))
			{
				List<Effect> effects = getEffectList().getEffectsBySkill(oldSkill);
				if(effects != null)
				{
					for(Effect effect : effects)
					{
						effect.exit();
					}
				}
				Summon pet;
				if((pet = getPet()) != null && (effects = pet.getEffectList().getEffectsBySkill(oldSkill)) != null)
				{
					for(Effect effect : effects)
					{
						effect.exit();
					}
				}
			}
		}
		return oldSkill;
	}
	
	public void addTriggers(StatTemplate f)
	{
		if(f.getTriggerList().isEmpty())
		{
			return;
		}
		for(TriggerInfo t : f.getTriggerList())
		{
			addTrigger(t);
		}
	}
	
	public void addTrigger(TriggerInfo t)
	{
		if(_triggers == null)
		{
			_triggers = new ConcurrentHashMap<>();
		}
		Set<TriggerInfo> hs;
		if((hs = _triggers.get(t.getType())) == null)
		{
			hs = new CopyOnWriteArraySet<>();
			_triggers.put(t.getType(), hs);
		}
		hs.add(t);
		if(t.getType() == TriggerType.ADD)
		{
			useTriggerSkill(this, null, t, null, 0.0);
		}
	}
	
	public void removeTriggers(StatTemplate f)
	{
		if(_triggers == null || f.getTriggerList().isEmpty())
		{
			return;
		}
		for(TriggerInfo t : f.getTriggerList())
		{
			removeTrigger(t);
		}
	}
	
	public void removeTrigger(TriggerInfo t)
	{
		if(_triggers == null)
		{
			return;
		}
		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if(hs == null)
		{
			return;
		}
		hs.remove(t);
	}
	
	public void sendActionFailed()
	{
		sendPacket(ActionFail.STATIC);
	}
	
	public boolean hasAI()
	{
		return _ai != null;
	}
	
	public CharacterAI getAI()
	{
		if(_ai == null)
		{
			Creature creature = this;
			synchronized(creature)
			{
				if(_ai == null)
				{
					_ai = new CharacterAI(this);
				}
			}
		}
		return _ai;
	}
	
	public void setAI(CharacterAI newAI)
	{
		if(newAI == null)
		{
			return;
		}
		CharacterAI oldAI = _ai;
		Creature creature = this;
		synchronized(creature)
		{
			_ai = newAI;
		}
		if(oldAI != null && oldAI.isActive())
		{
			oldAI.stopAITask();
			newAI.startAITask();
			newAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
	
	public final void setCurrentHp(double newHp, boolean canRessurect, boolean sendInfo)
	{
		int maxHp = getMaxHp();
		newHp = Math.min((double) maxHp, Math.max(0.0, newHp));
		if(_currentHp == newHp)
		{
			return;
		}
		if(newHp >= 0.5 && isDead() && !canRessurect)
		{
			return;
		}
		double hpStart = _currentHp;
		_currentHp = newHp;
		if(isDead.compareAndSet(true, false))
		{
			onRevive();
		}
		checkHpMessages(hpStart, _currentHp);
		if(sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}
		if(_currentHp < (double) maxHp)
		{
			startRegeneration();
		}
	}
	
	public final void setCurrentHp(double newHp, boolean canRessurect)
	{
		setCurrentHp(newHp, canRessurect, true);
	}
	
	public final void setCurrentMp(double newMp, boolean sendInfo)
	{
		int maxMp = getMaxMp();
		newMp = Math.min((double) maxMp, Math.max(0.0, newMp));
		if(_currentMp == newMp)
		{
			return;
		}
		if(newMp >= 0.5 && isDead())
		{
			return;
		}
		_currentMp = newMp;
		if(sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}
		if(_currentMp < (double) maxMp)
		{
			startRegeneration();
		}
	}
	
	public final void setCurrentCp(double newCp, boolean sendInfo)
	{
		if(!isPlayer())
		{
			return;
		}
		int maxCp = getMaxCp();
		newCp = Math.min((double) maxCp, Math.max(0.0, newCp));
		if(_currentCp == newCp)
		{
			return;
		}
		if(newCp >= 0.5 && isDead())
		{
			return;
		}
		_currentCp = newCp;
		if(sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}
		if(_currentCp < (double) maxCp)
		{
			startRegeneration();
		}
	}
	
	public void setCurrentHpMp(double newHp, double newMp, boolean canRessurect)
	{
		int maxHp = getMaxHp();
		int maxMp = getMaxMp();
		newHp = Math.min((double) maxHp, Math.max(0.0, newHp));
		newMp = Math.min((double) maxMp, Math.max(0.0, newMp));
		if(_currentHp == newHp && _currentMp == newMp)
		{
			return;
		}
		if(newHp >= 0.5 && isDead() && !canRessurect)
		{
			return;
		}
		double hpStart = _currentHp;
		_currentHp = newHp;
		_currentMp = newMp;
		if(isDead.compareAndSet(true, false))
		{
			onRevive();
		}
		checkHpMessages(hpStart, _currentHp);
		broadcastStatusUpdate();
		sendChanges();
		if(_currentHp < (double) maxHp || _currentMp < (double) maxMp)
		{
			startRegeneration();
		}
	}
	
	public void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHpMp(newHp, newMp, false);
	}
	
	@Override
	public final int getHeading()
	{
		return _heading;
	}
	
	public void setHeading(int heading)
	{
		_heading = heading;
	}
	
	public final void setIsTeleporting(boolean value)
	{
		isTeleporting.compareAndSet(!value, value);
	}
	
	public Creature getCastingTarget()
	{
		return castingTarget.get();
	}
	
	public void setCastingTarget(Creature target)
	{
		castingTarget = target == null ? HardReferences.emptyRef() : target.getRef();
	}
	
	public final void setRunning()
	{
		if(!_running)
		{
			_running = true;
			broadcastPacket(new ChangeMoveType(this));
		}
	}
	
	public void setSkillMastery(Integer skill, int mastery)
	{
		if(_skillMastery == null)
		{
			_skillMastery = new HashMap<>();
		}
		_skillMastery.put(skill, mastery);
	}
	
	public Creature getAggressionTarget()
	{
		return _aggressionTarget.get();
	}
	
	public void setAggressionTarget(Creature target)
	{
		_aggressionTarget = target == null ? HardReferences.emptyRef() : target.getRef();
	}
	
	public void setWalking()
	{
		if(_running)
		{
			_running = false;
			broadcastPacket(new ChangeMoveType(this));
		}
	}
	
	public void startAbnormalEffect(AbnormalEffect ae)
	{
		if(ae == AbnormalEffect.NULL)
		{
			_abnormalEffects = AbnormalEffect.NULL.getMask();
			_abnormalEffects2 = AbnormalEffect.NULL.getMask();
			_abnormalEffects3 = AbnormalEffect.NULL.getMask();
		}
		else if(ae.isSpecial())
		{
			_abnormalEffects2 |= ae.getMask();
		}
		else if(ae.isEvent())
		{
			_abnormalEffects3 |= ae.getMask();
		}
		else
		{
			_abnormalEffects |= ae.getMask();
		}
		sendChanges();
	}
	
	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
	}
	
	protected void startAttackStanceTask0()
	{
		if(isInCombat())
		{
			_stanceEndTime = System.currentTimeMillis() + 15000;
			return;
		}
		_stanceEndTime = System.currentTimeMillis() + 15000;
		broadcastPacket(new AutoAttackStart(getObjectId()));
		Future<?> task = _stanceTask;
		if(task != null)
		{
			task.cancel(false);
		}
		Runnable runnable = _stanceTaskRunnable == null ? (_stanceTaskRunnable = new AttackStanceTask()) : _stanceTaskRunnable;
		_stanceTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(runnable, 1000, 1000);
	}
	
	public void stopAttackStanceTask()
	{
		_stanceEndTime = 0;
		Future<?> task = _stanceTask;
		if(task != null)
		{
			task.cancel(false);
			_stanceTask = null;
			broadcastPacket(new AutoAttackStop(getObjectId()));
		}
	}
	
	protected void stopRegeneration()
	{
		regenLock.lock();
		try
		{
			if(_isRegenerating)
			{
				_isRegenerating = false;
				if(_regenTask != null)
				{
					_regenTask.cancel(false);
					_regenTask = null;
				}
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}
	
	protected void startRegeneration()
	{
		if(!isVisible() || isDead() || getRegenTick() == 0)
		{
			return;
		}
		if(_isRegenerating)
		{
			return;
		}
		regenLock.lock();
		try
		{
			if(!_isRegenerating)
			{
				_isRegenerating = true;
				Runnable runnable = _regenTaskRunnable == null ? (_regenTaskRunnable = new RegenTask()) : _regenTaskRunnable;
				_regenTask = RegenTaskManager.getInstance().scheduleAtFixedRate(runnable, 0, getRegenTick());
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}
	
	public long getRegenTick()
	{
		return 3000;
	}
	
	public void stopAbnormalEffect(AbnormalEffect ae)
	{
		if(ae.isSpecial())
		{
			_abnormalEffects2 &= ~ae.getMask();
		}
		if(ae.isEvent())
		{
			_abnormalEffects3 &= ~ae.getMask();
		}
		else
		{
			_abnormalEffects &= ~ae.getMask();
		}
		sendChanges();
	}
	
	public void block()
	{
		_blocked = true;
	}
	
	public void unblock()
	{
		_blocked = false;
	}
	
	public boolean startConfused()
	{
		return _confused.getAndSet(true);
	}
	
	public boolean stopConfused()
	{
		return _confused.setAndGet(false);
	}
	
	public boolean startFear()
	{
		return _afraid.getAndSet(true);
	}
	
	public boolean stopFear()
	{
		return _afraid.setAndGet(false);
	}
	
	public boolean startMuted()
	{
		return _muted.getAndSet(true);
	}
	
	public boolean stopMuted()
	{
		return _muted.setAndGet(false);
	}
	
	public boolean startPMuted()
	{
		return _pmuted.getAndSet(true);
	}
	
	public boolean stopPMuted()
	{
		return _pmuted.setAndGet(false);
	}
	
	public boolean startAMuted()
	{
		return _amuted.getAndSet(true);
	}
	
	public boolean stopAMuted()
	{
		return _amuted.setAndGet(false);
	}
	
	public boolean startRooted()
	{
		return _rooted.getAndSet(true);
	}
	
	public boolean stopRooted()
	{
		return _rooted.setAndGet(false);
	}
	
	public boolean startSleeping()
	{
		return _sleeping.getAndSet(true);
	}
	
	public boolean stopSleeping()
	{
		return _sleeping.setAndGet(false);
	}
	
	public boolean startStunning()
	{
		return _stunned.getAndSet(true);
	}
	
	public boolean stopStunning()
	{
		return _stunned.setAndGet(false);
	}
	
	public boolean startParalyzed()
	{
		return _paralyzed.getAndSet(true);
	}
	
	public boolean stopParalyzed()
	{
		return _paralyzed.setAndGet(false);
	}
	
	public boolean startImmobilized()
	{
		return _immobilized.getAndSet(true);
	}
	
	public boolean stopImmobilized()
	{
		return _immobilized.setAndGet(false);
	}
	
	public boolean startHealBlocked()
	{
		return _healBlocked.getAndSet(true);
	}
	
	public boolean stopHealBlocked()
	{
		return _healBlocked.setAndGet(false);
	}
	
	public boolean startDamageBlocked()
	{
		return _damageBlocked.getAndSet(true);
	}
	
	public boolean stopDamageBlocked()
	{
		return _damageBlocked.setAndGet(false);
	}
	
	public boolean startBuffImmunity()
	{
		return _buffImmunity.getAndSet(true);
	}
	
	public boolean stopBuffImmunity()
	{
		return _buffImmunity.setAndGet(false);
	}
	
	public boolean startDebuffImmunity()
	{
		return _debuffImmunity.getAndSet(true);
	}
	
	public boolean stopDebuffImmunity()
	{
		return _debuffImmunity.setAndGet(false);
	}
	
	public boolean startEffectImmunity()
	{
		return _effectImmunity.getAndSet(true);
	}
	
	public boolean stopEffectImmunity()
	{
		return _effectImmunity.setAndGet(false);
	}
	
	public boolean startWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.getAndSet(true);
	}
	
	public boolean stopWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.getAndSet(false);
	}
	
	public boolean startFrozen()
	{
		return _frozen.getAndSet(true);
	}
	
	public boolean stopFrozen()
	{
		return _frozen.setAndGet(false);
	}
	
	public final void setIsBlessedByNoblesse(boolean value)
	{
		_isBlessedByNoblesse = value;
	}
	
	public final void setIsSalvation(boolean value)
	{
		_isSalvation = value;
	}
	
	public void setIsInvul(boolean value)
	{
		_isInvul = value;
	}
	
	public boolean isConfused()
	{
		return _confused.get();
	}
	
	public boolean isAfraid()
	{
		return _afraid.get();
	}
	
	public boolean isBlocked()
	{
		return _blocked;
	}
	
	public boolean isMuted(Skill skill)
	{
		if(skill == null || skill.isNotAffectedByMute())
		{
			return false;
		}
		return isMMuted() && skill.isMagic() || isPMuted() && !skill.isMagic();
	}
	
	public boolean isPMuted()
	{
		return _pmuted.get();
	}
	
	public boolean isMMuted()
	{
		return _muted.get();
	}
	
	public boolean isAMuted()
	{
		return _amuted.get();
	}
	
	public boolean isRooted()
	{
		return _rooted.get();
	}
	
	public boolean isSleeping()
	{
		return _sleeping.get();
	}
	
	public boolean isStunned()
	{
		return _stunned.get();
	}
	
	public boolean isMeditated()
	{
		return _meditated;
	}
	
	public void setMeditated(boolean value)
	{
		_meditated = value;
	}
	
	public boolean isWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.get();
	}
	
	public boolean isParalyzed()
	{
		return _paralyzed.get();
	}
	
	public boolean isFrozen()
	{
		return _frozen.get();
	}
	
	public boolean isImmobilized()
	{
		return _immobilized.get() || getRunSpeed() < 1;
	}
	
	public boolean isHealBlocked()
	{
		return isAlikeDead() || _healBlocked.get();
	}
	
	public boolean isDamageBlocked()
	{
		return isInvul() || _damageBlocked.get();
	}
	
	public boolean isCastingNow()
	{
		return _skillTask != null;
	}
	
	public boolean isLockedTarget()
	{
		return _lockedTarget;
	}
	
	public void setLockedTarget(boolean value)
	{
		_lockedTarget = value;
	}
	
	public boolean isMovementDisabled()
	{
		return isBlocked() || isRooted() || isImmobilized() || isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isFrozen();
	}
	
	public boolean isActionsDisabled()
	{
		return isBlocked() || isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isFrozen();
	}
	
	public boolean isPotionsDisabled()
	{
		return isActionsDisabled() || isStunned() || isSleeping() || isParalyzed() || isAlikeDead() || isAfraid();
	}
	
	public final boolean isAttackingDisabled()
	{
		return _attackReuseEndTime > System.currentTimeMillis();
	}
	
	public boolean isOutOfControl()
	{
		return isBlocked() || isConfused() || isAfraid() || isFrozen();
	}
	
	public void teleToLocation(Location loc)
	{
		teleToLocation(loc.x, loc.y, loc.z, getReflection());
	}
	
	public void teleToLocation(Location loc, int refId)
	{
		teleToLocation(loc.x, loc.y, loc.z, refId);
	}
	
	public void teleToLocation(Location loc, Reflection r)
	{
		teleToLocation(loc.x, loc.y, loc.z, r);
	}
	
	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, getReflection());
	}
	
	public void checkAndRemoveInvisible()
	{
		InvisibleType invisibleType = getInvisibleType();
		if(invisibleType == InvisibleType.EFFECT)
		{
			getEffectList().stopEffects(EffectType.Invisible);
		}
	}
	
	public void teleToLocation(int x, int y, int z, int refId)
	{
		Reflection r = ReflectionManager.getInstance().get(refId);
		if(r == null)
		{
			return;
		}
		teleToLocation(x, y, z, r);
	}
	
	public void teleToLocation(int x, int y, int z, Reflection r)
	{
		if(!isTeleporting.compareAndSet(false, true))
		{
			return;
		}
		abortCast(true, false);
		if(!isLockedTarget())
		{
			setTarget(null);
		}
		stopMove(true, true, false);
		if(!(isBoat() || isFlying() || World.isWater(new Location(x, y, z), r)))
		{
			z = GeoEngine.getHeight(x, y, z, r.getGeoIndex());
		}
		Player player;
		if(isPlayer() && DimensionalRiftManager.getInstance().checkIfInRiftZone(getLoc(), true) && (player = (Player) this).isInParty() && player.getParty().isInDimensionalRift())
		{
			Location newCoords = DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords();
			x = newCoords.x;
			y = newCoords.y;
			z = newCoords.z;
			player.getParty().getDimensionalRift().usedTeleport(player);
		}
		if(isPlayer())
		{
			player = (Player) this;
			player.getListeners().onTeleport(x, y, z, r);
			decayMe();
			setXYZ(x, y, z);
			setReflection(r);
			player.setLastClientPosition(null);
			player.setLastServerPosition(null);
			player.sendPacket(new TeleportToLocation(player, x, y, z));
		}
		else
		{
			setXYZ(x, y, z);
			setReflection(r);
			broadcastPacket(new TeleportToLocation(this, x, y, z));
			onTeleported();
		}
	}
	
	public boolean onTeleported()
	{
		return isTeleporting.compareAndSet(true, false);
	}
	
	public void sendMessage(CustomMessage message)
	{
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getObjectId() + "]";
	}
	
	@Override
	public double getColRadius()
	{
		return getTemplate().collisionRadius;
	}
	
	@Override
	public double getColHeight()
	{
		return getTemplate().collisionHeight;
	}
	
	public EffectList getEffectList()
	{
		if(_effectList == null)
		{
			Creature creature = this;
			synchronized(creature)
			{
				if(_effectList == null)
				{
					_effectList = new EffectList(this);
				}
			}
		}
		return _effectList;
	}
	
	public boolean paralizeOnAttack(Creature attacker)
	{
		int max_level_diff;
		MonsterInstance leader;
		int max_attacker_level = 65535;
		if(isRaid() || isMinion() && (leader = ((MinionInstance) this).getLeader()) != null && leader.isRaid())
		{
			max_attacker_level = getLevel() + Config.RAID_MAX_LEVEL_DIFF;
		}
		else if(isNpc() && (max_level_diff = ((NpcInstance) this).getParameter("ParalizeOnAttack", -1000)) != -1000)
		{
			max_attacker_level = getLevel() + max_level_diff;
		}
		return attacker.getLevel() > max_attacker_level;
	}
	
	@Override
	protected void onDelete()
	{
		GameObjectsStorage.remove(_storedId);
		getEffectList().stopAllEffects();
		super.onDelete();
	}
	
	public void addExpAndSp(long exp, long sp)
	{
	}
	
	public void broadcastCharInfo()
	{
	}
	
	public void checkHpMessages(double currentHp, double newHp)
	{
	}
	
	public boolean checkPvP(Creature target, Skill skill)
	{
		return false;
	}
	
	public boolean consumeItem(int itemConsumeId, long itemCount)
	{
		return true;
	}
	
	public boolean consumeItemMp(int itemId, int mp)
	{
		return true;
	}
	
	public boolean isFearImmune()
	{
		return false;
	}
	
	public boolean isLethalImmune()
	{
		return getMaxHp() >= 50000;
	}
	
	public boolean getChargedSoulShot()
	{
		return false;
	}
	
	public int getChargedSpiritShot()
	{
		return 0;
	}
	
	public int getIncreasedForce()
	{
		return 0;
	}
	
	public void setIncreasedForce(int i)
	{
	}
	
	public int getConsumedSouls()
	{
		return 0;
	}
	
	public int getAgathionEnergy()
	{
		return 0;
	}
	
	public void setAgathionEnergy(int val)
	{
	}
	
	public int getKarma()
	{
		return 0;
	}
	
	public double getLevelMod()
	{
		return 1.0;
	}
	
	public int getNpcId()
	{
		return 0;
	}
	
	public Summon getPet()
	{
		return null;
	}
	
	public int getPvpFlag()
	{
		return 0;
	}
	
	public TeamType getTeam()
	{
		return _team;
	}
	
	public void setTeam(TeamType t)
	{
		_team = t;
		sendChanges();
	}
	
	public boolean isUndead()
	{
		return false;
	}
	
	public boolean isParalyzeImmune()
	{
		return false;
	}
	
	public void reduceArrowCount()
	{
	}
	
	public void sendChanges()
	{
		getStatsRecorder().sendChanges();
	}
	
	public void sendMessage(String message)
	{
	}
	
	public void sendPacket(IStaticPacket mov)
	{
	}
	
	public void sendPacket(IStaticPacket... mov)
	{
	}
	
	public void sendPacket(List<? extends IStaticPacket> mov)
	{
	}
	
	public void setConsumedSouls(int i, NpcInstance monster)
	{
	}
	
	public void startPvPFlag(Creature target)
	{
	}
	
	public boolean unChargeShots(boolean spirit)
	{
		return false;
	}
	
	public void updateEffectIcons()
	{
	}
	
	protected void refreshHpMpCp()
	{
		int maxCp;
		int maxHp = getMaxHp();
		int maxMp = getMaxMp();
		int n = maxCp = isPlayer() ? getMaxCp() : 0;
		if(_currentHp > (double) maxHp)
		{
			setCurrentHp(maxHp, false);
		}
		if(_currentMp > (double) maxMp)
		{
			setCurrentMp(maxMp, false);
		}
		if(_currentCp > (double) maxCp)
		{
			setCurrentCp(maxCp, false);
		}
		if(_currentHp < (double) maxHp || _currentMp < (double) maxMp || _currentCp < (double) maxCp)
		{
			startRegeneration();
		}
	}
	
	public void updateStats()
	{
		refreshHpMpCp();
		sendChanges();
	}
	
	public void setOverhitAttacker(Creature attacker)
	{
	}
	
	public void setOverhitDamage(double damage)
	{
	}
	
	public boolean isCursedWeaponEquipped()
	{
		return false;
	}
	
	public boolean isHero()
	{
		return false;
	}
	
	public int getAccessLevel()
	{
		return 0;
	}
	
	public Clan getClan()
	{
		return null;
	}
	
	public double getRateAdena()
	{
		return 1.0;
	}
	
	public double getRateItems()
	{
		return 1.0;
	}
	
	public double getRateExp()
	{
		return 1.0;
	}
	
	public double getRateSp()
	{
		return 1.0;
	}
	
	public double getRateSpoil()
	{
		return 1.0;
	}
	
	public int getFormId()
	{
		return 0;
	}
	
	public boolean isNameAbove()
	{
		return true;
	}
	
	@Override
	public void setLoc(Location loc)
	{
		setXYZ(loc.x, loc.y, loc.z);
	}
	
	public void setLoc(Location loc, boolean MoveTask)
	{
		setXYZ(loc.x, loc.y, loc.z, MoveTask);
	}
	
	@Override
	public void setXYZ(int x, int y, int z)
	{
		setXYZ(x, y, z, false);
	}
	
	public void setXYZ(int x, int y, int z, boolean MoveTask)
	{
		if(!MoveTask)
		{
			stopMove();
		}
		moveLock.lock();
		try
		{
			super.setXYZ(x, y, z);
		}
		finally
		{
			moveLock.unlock();
		}
		updateZones();
	}
	
	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		updateStats();
		updateZones();
	}
	
	@Override
	public void spawnMe(Location loc)
	{
		if(loc.h > 0)
		{
			setHeading(loc.h);
		}
		try
		{
			super.spawnMe(loc);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDespawn()
	{
		if(!isLockedTarget())
		{
			setTarget(null);
		}
		stopMove();
		stopAttackStanceTask();
		stopRegeneration();
		updateZones();
		clearStatusListeners();
		super.onDespawn();
	}
	
	public final void doDecay()
	{
		if(!isDead())
		{
			return;
		}
		onDecay();
	}
	
	protected void onDecay()
	{
		decayMe();
	}
	
	public void validateLocation(int broadcast)
	{
		ValidateLocation sp = new ValidateLocation(this);
		if(broadcast == 0)
		{
			sendPacket(sp);
		}
		else if(broadcast == 1)
		{
			broadcastPacket(sp);
		}
		else
		{
			broadcastPacketToOthers(sp);
		}
	}
	
	public void addUnActiveSkill(Skill skill)
	{
		if(skill == null || isUnActiveSkill(skill.getId()))
		{
			return;
		}
		removeStatsOwner(skill);
		removeTriggers(skill);
		_unActiveSkills.add(skill.getId());
	}
	
	public void removeUnActiveSkill(Skill skill)
	{
		if(skill == null || !isUnActiveSkill(skill.getId()))
		{
			return;
		}
		addStatFuncs(skill.getStatFuncs());
		addTriggers(skill);
		_unActiveSkills.remove(skill.getId());
	}
	
	public boolean isUnActiveSkill(int id)
	{
		return _unActiveSkills.contains(id);
	}
	
	public abstract int getLevel();
	
	public abstract ItemInstance getActiveWeaponInstance();
	
	public abstract WeaponTemplate getActiveWeaponItem();
	
	public abstract ItemInstance getSecondaryWeaponInstance();
	
	public abstract WeaponTemplate getSecondaryWeaponItem();
	
	public CharListenerList getListeners()
	{
		if(listeners == null)
		{
			Creature creature = this;
			synchronized(creature)
			{
				if(listeners == null)
				{
					listeners = new CharListenerList(this);
				}
			}
		}
		return listeners;
	}
	
	public <T extends Listener<Creature>> boolean addListener(T listener)
	{
		return getListeners().add(listener);
	}
	
	public <T extends Listener<Creature>> boolean removeListener(T listener)
	{
		return getListeners().remove(listener);
	}
	
	public CharStatsChangeRecorder<? extends Creature> getStatsRecorder()
	{
		if(_statsRecorder == null)
		{
			Creature creature = this;
			synchronized(creature)
			{
				if(_statsRecorder == null)
				{
					_statsRecorder = new CharStatsChangeRecorder<>(this);
				}
			}
		}
		return _statsRecorder;
	}
	
	@Override
	public boolean isCreature()
	{
		return true;
	}
	
	public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic)
	{
		if(miss && target.isPlayer() && !target.isDamageBlocked())
		{
			target.sendPacket(new SystemMessage(42).addName(this));
		}
	}
	
	public void displayReceiveDamageMessage(Creature attacker, int damage)
	{
	}
	
	public Collection<TimeStamp> getSkillReuses()
	{
		return _skillReuses.values();
	}
	
	public TimeStamp getSkillReuse(Skill skill)
	{
		return _skillReuses.get(skill.hashCode());
	}
	
	public static class MoveToRelativeAction extends MoveToAction
	{
		private final HardReference<? extends GameObject> targetRef;
		private final int range;
		private Location prevTargetLoc;
		private boolean isRelativeMoveEnabled;
		
		protected MoveToRelativeAction(Creature actor, GameObject target, boolean ignoreGeo, int indent, int range, boolean pathFind)
		{
			super(actor, ignoreGeo, indent, pathFind);
			targetRef = target.getRef();
			prevTargetLoc = target.getLoc().clone();
			this.range = Math.max(range, indent + 16);
			isRelativeMoveEnabled = false;
		}
		
		private GameObject getTarget()
		{
			return targetRef.get();
		}
		
		public boolean isSameTarget(GameObject target)
		{
			return getTarget() == target;
		}
		
		@Override
		public boolean start()
		{
			if(!super.start())
			{
				return false;
			}
			Creature actor = getActor();
			GameObject target = getTarget();
			if(actor == null || target == null)
			{
				return false;
			}
			Location actorLoc = actor.getLoc();
			Location pawnLoc;
			if(!buildPathLines(actorLoc, pawnLoc = target.getLoc().clone()))
			{
				return false;
			}
			prevTargetLoc = pawnLoc.clone();
			return !onEnd();
		}
		
		protected boolean isPathRebuildRequired()
		{
			Creature actor = getActor();
			GameObject target = getTarget();
			if(actor == null || target == null)
			{
				return true;
			}
			Location targetLoc = target.getLoc();
			if(!isRelativeMoveEnabled)
			{
				return false;
			}
			return !prevTargetLoc.equalsGeo(targetLoc);
		}
		
		@Override
		protected boolean onEnd()
		{
			Creature actor = getActor();
			GameObject target = getTarget();
			if(actor == null || target == null)
			{
				return true;
			}
			int remainingLinesCount = remainingLinesCount();
			if(remainingLinesCount > 1)
			{
				if(!pollPathLine())
				{
					onFinish(false, false);
					return true;
				}
			}
			else if(remainingLinesCount == 1)
			{
				if(!(actor instanceof Summon))
				{
					isRelativeMoveEnabled = true;
				}
				if(isPathRebuildRequired())
				{
					if(isArrived())
					{
						onFinish(true, false);
						return true;
					}
					Location actorLoc = actor.getLoc();
					Location targetLoc;
					if(!buildPathLines(actorLoc, targetLoc = getImpliedTargetLoc()))
					{
						onFinish(false, false);
						return true;
					}
					if(!pollPathLine())
					{
						onFinish(false, false);
						return true;
					}
					prevTargetLoc = targetLoc.clone();
				}
				else if(!pollPathLine())
				{
					onFinish(false, false);
					return true;
				}
			}
			else
			{
				onFinish(true, false);
				return true;
			}
			actor.broadcastMove();
			return false;
		}
		
		protected boolean isArrived()
		{
			Creature actor = getActor();
			GameObject target = getTarget();
			if(actor == null || target == null)
			{
				return false;
			}
			if(target.isCreature() && ((Creature) target).isMoving())
			{
				int threshold = indent + 16;
				if(includeMoveZ())
				{
					return target.isInRangeZ(actor, (long) threshold);
				}
				return target.isInRange(actor, (long) threshold);
			}
			if(includeMoveZ())
			{
				return target.isInRangeZ(actor, (long) (indent + 16));
			}
			return target.isInRange(actor, (long) (indent + 16));
		}
		
		private Location getImpliedTargetLoc()
		{
			Creature actor = getActor();
			GameObject targetObj = getTarget();
			if(actor == null || targetObj == null)
			{
				return null;
			}
			if(!targetObj.isCreature())
			{
				return targetObj.getLoc();
			}
			Creature target = (Creature) targetObj;
			Location loc = targetObj.getLoc();
			if(!target.isMoving())
			{
				return loc;
			}
			return GeoMove.getIntersectPoint(actor.getLoc(), loc, target.getMoveSpeed(), Math.max(128, Config.MOVE_TASK_QUANTUM_PC / 2));
		}
		
		@Override
		protected boolean onTick(double done)
		{
			if(!super.onTick(done))
			{
				return false;
			}
			Creature actor = getActor();
			GameObject target = getTarget();
			if(actor == null || target == null)
			{
				return false;
			}
			if(done < 1.0)
			{
				if(isPathRebuildRequired())
				{
					Location actorLoc = actor.getLoc();
					Location pawnLoc = getImpliedTargetLoc();
					if(actor.isPlayer() && actor.getPlayer().getNetConnection() != null)
					{
						int pawnClippingRange = actor.getPlayer().getNetConnection().getPawnClippingRange();
						if(actorLoc.distance3D(pawnLoc) > (double) pawnClippingRange)
						{
							onFinish(false, false);
							return false;
						}
					}
					if(!buildPathLines(actorLoc, pawnLoc))
					{
						onFinish(false, false);
						return false;
					}
					if(!pollPathLine())
					{
						onFinish(false, false);
						return false;
					}
					prevTargetLoc = pawnLoc.clone();
				}
				else if(isRelativeMoveEnabled && isArrived())
				{
					onFinish(true, false);
					return false;
				}
			}
			return true;
		}
		
		@Override
		protected void onFinish(boolean finishedWell, boolean isInterrupted)
		{
			Creature actor = getActor();
			GameObject target = getTarget();
			if(isFinished() || actor == null || target == null)
			{
				return;
			}
			if(isInterrupted)
			{
				setIsFinished(true);
				return;
			}
			actor.stopMove(!(target instanceof StaticObjectInstance) && !target.isDoor(), false, false);
			boolean succeed = false;
			if(finishedWell)
			{
				succeed = (includeMoveZ() ? actor.getRealDistance3D(target) : actor.getRealDistance(target)) <= (double) (range + 16);
			}
			setIsFinished(true);
			if(succeed)
			{
				ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(actor, CtrlEvent.EVT_ARRIVED_TARGET));
			}
			else
			{
				ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(actor, CtrlEvent.EVT_ARRIVED_BLOCKED, actor.getLoc()));
			}
		}
		
		@Override
		protected boolean isRelativeMove()
		{
			return isRelativeMoveEnabled;
		}
		
		@Override
		public L2GameServerPacket movePacket()
		{
			Creature actor = getActor();
			if(actor == null)
			{
				return null;
			}
			GameObject target = getTarget();
			if(isRelativeMove())
			{
				if(target == null)
				{
					return null;
				}
				return new MoveToPawn(actor, target, indent);
			}
			return new CharMoveToLocation(actor, actor.getLoc(), moveTo.clone());
		}
	}
	
	public static class MoveToLocationAction extends MoveToAction
	{
		private final Location dst;
		private final Location src;
		
		public MoveToLocationAction(Creature actor, Location moveFrom, Location moveTo, boolean ignoreGeo, int indent, boolean pathFind)
		{
			super(actor, ignoreGeo, indent, pathFind);
			src = moveFrom.clone();
			dst = moveTo.clone();
		}
		
		public MoveToLocationAction(Creature actor, Location dest, int indent, boolean pathFind)
		{
			this(actor, actor.getLoc(), dest, actor.isBoat() || actor.isInBoat(), indent, pathFind);
		}
		
		public boolean isSameDest(Location to)
		{
			return dst.equalsGeo(to);
		}
		
		@Override
		public boolean start()
		{
			if(!super.start())
			{
				return false;
			}
			if(!buildPathLines(src, dst))
			{
				return false;
			}
			return !onEnd();
		}
		
		@Override
		protected boolean onEnd()
		{
			Creature actor = getActor();
			if(actor == null)
			{
				return true;
			}
			if(!pollPathLine())
			{
				onFinish(true, false);
				return true;
			}
			actor.broadcastMove();
			return false;
		}
		
		@Override
		protected void onFinish(boolean finishedWell, boolean isInterrupted)
		{
			Creature actor = getActor();
			if(isFinished() || actor == null)
			{
				return;
			}
			if(isInterrupted)
			{
				setIsFinished(true);
				return;
			}
			if(finishedWell)
			{
				ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(actor, CtrlEvent.EVT_ARRIVED));
			}
			else
			{
				actor.stopMove(true, true, false);
				ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(actor, CtrlEvent.EVT_ARRIVED_BLOCKED, actor.getLoc()));
			}
			super.onFinish(finishedWell, isInterrupted);
		}
		
		@Override
		public L2GameServerPacket movePacket()
		{
			Creature actor = getActor();
			return actor != null ? new CharMoveToLocation(actor, actor.getLoc(), moveTo.clone()) : null;
		}
		
		@Override
		protected boolean isRelativeMove()
		{
			return false;
		}
	}
	
	public abstract static class MoveToAction extends MoveActionBase
	{
		protected final int indent;
		protected final boolean pathFind;
		protected final boolean ignoreGeo;
		protected Queue<List<Location>> geoPathLines;
		protected List<Location> currentGeoPathLine;
		protected Location moveFrom;
		protected Location moveTo;
		protected double prevMoveLen;
		protected boolean prevIncZ;
		
		protected MoveToAction(Creature actor, boolean ignoreGeo, int indent, boolean pathFind)
		{
			super(actor);
			this.indent = indent;
			this.pathFind = pathFind;
			this.ignoreGeo = ignoreGeo;
			geoPathLines = new LinkedList<>();
			currentGeoPathLine = Collections.emptyList();
			moveFrom = actor.getLoc();
			moveTo = actor.getLoc();
			prevMoveLen = 0.0;
			prevIncZ = false;
		}
		
		protected boolean buildPathLines(Location pathFrom, Location pathTo)
		{
			Creature actor = getActor();
			if(actor == null)
			{
				return false;
			}
			LinkedList<List<Location>> geoPathLines = new LinkedList<>();
			if(!GeoMove.buildGeoPath(geoPathLines, pathFrom.clone().world2geo(), pathTo.clone().world2geo(), actor.getGeoIndex(), (int) actor.getColRadius(), (int) actor.getColHeight(), indent, pathFind && !ignoreGeo && !isRelativeMove(), isForPlayable(), actor.isFlying(), actor.isInWater(), actor.getWaterZ(), ignoreGeo))
			{
				return false;
			}
			this.geoPathLines.clear();
			this.geoPathLines.addAll(geoPathLines);
			return true;
		}
		
		protected boolean pollPathLine()
		{
			currentGeoPathLine = geoPathLines.poll();
			if(currentGeoPathLine != null)
			{
				Creature actor = getActor();
				moveFrom = currentGeoPathLine.get(0).clone().geo2world();
				moveTo = currentGeoPathLine.get(currentGeoPathLine.size() - 1).clone().geo2world();
				prevIncZ = includeMoveZ();
				prevMoveLen = PositionUtils.calculateDistance(moveFrom, moveTo, prevIncZ);
				setPassDist(0.0);
				setPrevTick(System.currentTimeMillis());
				if(prevMoveLen > 16.0)
				{
					actor.setHeading(PositionUtils.calculateHeadingFrom(moveFrom.getX(), moveFrom.getY(), moveTo.getX(), moveTo.getY()));
				}
				return true;
			}
			return false;
		}
		
		protected int remainingLinesCount()
		{
			return geoPathLines.size();
		}
		
		protected abstract boolean isRelativeMove();
		
		@Override
		protected boolean calcMidDest(Creature creature, Location result, boolean includeZ, double done, double pass, double len)
		{
			if(currentGeoPathLine == null)
			{
				return false;
			}
			Location currLoc = creature.getLoc();
			if(len < 16.0 || done == 0.0 || pass == 0.0 || currentGeoPathLine.isEmpty())
			{
				result.set(currLoc);
				return true;
			}
			int lastIdx = currentGeoPathLine.size() - 1;
			result.set(moveFrom).indent(moveTo, (int) (pass + 0.5), includeZ).setZ(currentGeoPathLine.get(Math.min(lastIdx, (int) ((double) lastIdx * done + 0.5))).getZ());
			if(result.equalsGeo(currLoc) || ignoreGeo || !Config.ALLOW_GEODATA)
			{
				return true;
			}
			if(includeZ)
			{
				return true;
			}
			return GeoEngine.canMoveToCoord(currLoc.getX(), currLoc.getY(), currLoc.getZ(), result.getX(), result.getY(), result.getZ(), creature.getGeoIndex());
		}
		
		@Override
		public Location moveFrom()
		{
			return moveFrom;
		}
		
		@Override
		public Location moveTo()
		{
			return moveTo;
		}
		
		@Override
		protected double getMoveLen()
		{
			boolean incZ = includeMoveZ();
			if(incZ != prevIncZ)
			{
				prevMoveLen = PositionUtils.calculateDistance(moveFrom, moveTo, incZ);
				prevIncZ = incZ;
			}
			return prevMoveLen;
		}
	}
	
	protected abstract static class MoveActionBase
	{
		private final HardReference<? extends Creature> actorRef;
		private final boolean isForPlayable;
		protected volatile boolean isFinished;
		private long prevTick;
		private int prevSpeed;
		private double passDist;
		
		public MoveActionBase(Creature actor)
		{
			actorRef = actor.getRef();
			isForPlayable = actor.isPlayable();
			prevTick = 0;
			prevSpeed = 0;
			passDist = 0.0;
			isFinished = false;
		}
		
		protected boolean isForPlayable()
		{
			return isForPlayable;
		}
		
		protected Creature getActor()
		{
			return actorRef.get();
		}
		
		protected void setIsFinished(boolean isFinished)
		{
			this.isFinished = isFinished;
		}
		
		public boolean isFinished()
		{
			return isFinished;
		}
		
		protected long getPrevTick()
		{
			return prevTick;
		}
		
		protected void setPrevTick(long prevTick)
		{
			this.prevTick = prevTick;
		}
		
		protected int getPrevSpeed()
		{
			return prevSpeed;
		}
		
		protected void setPrevSpeed(int prevSpeed)
		{
			this.prevSpeed = prevSpeed;
		}
		
		protected double getPassDist()
		{
			return passDist;
		}
		
		protected void setPassDist(double passDist)
		{
			this.passDist = passDist;
		}
		
		public boolean start()
		{
			Creature actor = getActor();
			if(actor == null)
			{
				return false;
			}
			setPrevTick(System.currentTimeMillis());
			setPrevSpeed(actor.getMoveSpeed());
			setPassDist(0.0);
			setIsFinished(false);
			return weightCheck(actor);
		}
		
		public abstract Location moveFrom();
		
		public abstract Location moveTo();
		
		protected double getMoveLen()
		{
			return PositionUtils.calculateDistance(moveFrom(), moveTo(), includeMoveZ());
		}
		
		protected boolean includeMoveZ()
		{
			Creature actor = getActor();
			return actor == null || actor.isInWater() || actor.isFlying() || actor.isBoat() || actor.isInBoat();
		}
		
		public int getNextTickInterval()
		{
			if(!isForPlayable())
			{
				return Math.min(Config.MOVE_TASK_QUANTUM_NPC, (int) (1000.0 * (getMoveLen() - getPassDist()) / (double) Math.max(getPrevSpeed(), 1)));
			}
			return Math.min(Config.MOVE_TASK_QUANTUM_PC, (int) (1000.0 * (getMoveLen() - getPassDist()) / (double) Math.max(getPrevSpeed(), 1)));
		}
		
		protected boolean onEnd()
		{
			return true;
		}
		
		protected void onFinish(boolean finishedWell, boolean isInterrupted)
		{
			setIsFinished(true);
		}
		
		public void interrupt()
		{
			tick();
			onFinish(false, true);
		}
		
		protected boolean onTick(double done)
		{
			Creature actor = getActor();
			if(actor == null)
			{
				onFinish(false, true);
				return false;
			}
			return true;
		}
		
		public boolean scheduleNextTick()
		{
			Creature actor = getActor();
			if(actor == null)
			{
				return false;
			}
			Runnable r = new CreatureMoveActionTask(actor);
			actor._moveTaskRunnable = r;
			actor._moveTask = ThreadPoolManager.getInstance().schedule(r, getNextTickInterval());
			return true;
		}
		
		public boolean tick()
		{
			Creature actor = getActor();
			if(actor == null)
			{
				return false;
			}
			actor.moveLock.lock();
			try
			{
				boolean bl = tickImpl(actor);
				return bl;
			}
			finally
			{
				actor.moveLock.unlock();
			}
		}
		
		private boolean tickImpl(Creature actor)
		{
			if(isFinished())
			{
				return false;
			}
			if(actor.moveAction != this)
			{
				setIsFinished(true);
				return false;
			}
			if(actor.isMovementDisabled())
			{
				onFinish(false, false);
				return false;
			}
			int currSpeed = actor.getMoveSpeed();
			if(currSpeed <= 0)
			{
				onFinish(false, false);
				return false;
			}
			long now = System.currentTimeMillis();
			float delta = (float) (now - getPrevTick()) / 1000.0f;
			boolean includeMoveZ = includeMoveZ();
			double passLen = getPassDist();
			setPrevTick(now);
			setPrevSpeed(currSpeed);
			setPassDist(passLen += (double) delta * ((double) Math.max(getPrevSpeed() + currSpeed, 2) / 2.0));
			double len = getMoveLen();
			double done = Math.max(0.0, Math.min(passLen / Math.max(len, 1.0), 1.0));
			Location currLoc = actor.getLoc();
			Location newLoc = currLoc.clone();
			if(!calcMidDest(actor, newLoc, includeMoveZ, done, passLen, len))
			{
				onFinish(false, false);
				return false;
			}
			if(!includeMoveZ)
			{
				
			}
			actor.setLoc(newLoc, true);
			if(done == 1.0)
			{
				return !onEnd();
			}
			if(!onTick(done))
			{
				setIsFinished(true);
				return false;
			}
			return true;
		}
		
		protected boolean weightCheck(Creature creature)
		{
			if(!creature.isPlayer())
			{
				return true;
			}
			if(creature.getPlayer().getCurrentLoad() >= 2 * creature.getPlayer().getMaxLoad())
			{
				creature.sendPacket(new SystemMessage(555));
				return false;
			}
			return true;
		}
		
		protected boolean calcMidDest(Creature creature, Location result, boolean includeZ, double done, double pass, double len)
		{
			result.set(moveTo().clone().indent(moveFrom(), (int) Math.round(len - pass), creature.isFlying() || creature.isInWater())).correctGeoZ();
			return true;
		}
		
		public abstract L2GameServerPacket movePacket();
	}
	
	protected static class CreatureMoveActionTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _creatureRef;
		
		public CreatureMoveActionTask(Creature creature)
		{
			_creatureRef = creature.getRef();
		}
		
		@Override
		public void runImpl() throws Exception
		{
			Creature actor = _creatureRef.get();
			if(actor == null)
			{
				return;
			}
			actor.moveLock.lock();
			try
			{
				MoveActionBase moveActionBase = actor.moveAction;
				if(actor._moveTaskRunnable == this && moveActionBase != null && !moveActionBase.isFinished() && moveActionBase.tickImpl(actor) && actor._moveTaskRunnable == this)
				{
					moveActionBase.scheduleNextTick();
				}
			}
			finally
			{
				actor.moveLock.unlock();
			}
		}
	}
	
	private class RegenTask implements Runnable
	{
		@Override
		public void run()
		{
			if(isAlikeDead() || getRegenTick() == 0)
			{
				return;
			}
			double hpStart = _currentHp;
			int maxHp = getMaxHp();
			int maxMp = getMaxMp();
			int maxCp = isPlayer() ? getMaxCp() : 0;
			regenLock.lock();
			try
			{
				double addHp = 0.0;
				if(_currentHp < (double) maxHp)
				{
					addHp += Formulas.calcHpRegen(Creature.this);
				}
				double addMp = 0.0;
				if(_currentMp < (double) maxMp)
				{
					addMp += Formulas.calcMpRegen(Creature.this);
				}
				if(isPlayer() && Config.REGEN_SIT_WAIT)
				{
					Player pl = (Player) Creature.this;
					if(pl.isSitting())
					{
						pl.updateWaitSitTime();
						if(pl.getWaitSitTime() > 5)
						{
							addHp += (double) pl.getWaitSitTime();
							addMp += (double) pl.getWaitSitTime();
						}
					}
				}
				else if(isRaid())
				{
					addHp *= Config.RATE_RAID_REGEN;
					addMp *= Config.RATE_RAID_REGEN;
				}
				_currentHp += Math.max(0.0, Math.min(addHp, calcStat(Stats.HP_LIMIT, null, null) * (double) maxHp / 100.0 - _currentHp));
				_currentMp += Math.max(0.0, Math.min(addMp, calcStat(Stats.MP_LIMIT, null, null) * (double) maxMp / 100.0 - _currentMp));
				_currentHp = Math.min((double) maxHp, _currentHp);
				_currentMp = Math.min((double) maxMp, _currentMp);
				if(isPlayer())
				{
					_currentCp += Math.max(0.0, Math.min(Formulas.calcCpRegen(Creature.this), calcStat(Stats.CP_LIMIT, null, null) * (double) maxCp / 100.0 - _currentCp));
					_currentCp = Math.min((double) maxCp, _currentCp);
				}
				if(_currentHp == (double) maxHp && _currentMp == (double) maxMp && _currentCp == (double) maxCp)
				{
					stopRegeneration();
				}
			}
			finally
			{
				regenLock.unlock();
			}
			broadcastStatusUpdate();
			sendChanges();
			checkHpMessages(hpStart, _currentHp);
		}
	}
	
	private class AttackStanceTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(!isInCombat())
			{
				stopAttackStanceTask();
			}
		}
	}
}