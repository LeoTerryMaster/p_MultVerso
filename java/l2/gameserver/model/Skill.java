package l2.gameserver.model;

import l2.commons.collections.LazyArrayList;
import l2.commons.geometry.Polygon;
import l2.commons.lang.ArrayUtils;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.model.base.BaseStats;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.base.Element;
import l2.gameserver.model.base.SkillTrait;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.model.entity.oly.CompetitionType;
import l2.gameserver.model.instances.ChestInstance;
import l2.gameserver.model.instances.FeedableBeastInstance;
import l2.gameserver.model.items.ItemContainer;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.FlyToLocation;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.skills.effects.EffectTemplate;
import l2.gameserver.skills.skillclasses.*;
import l2.gameserver.skills.skillclasses.DeathPenalty;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.Formulas;
import l2.gameserver.stats.StatTemplate;
import l2.gameserver.stats.Stats;
import l2.gameserver.stats.conditions.Condition;
import l2.gameserver.stats.funcs.Func;
import l2.gameserver.stats.funcs.FuncTemplate;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.utils.PositionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public abstract class Skill extends StatTemplate implements Cloneable
{
	public static final Skill[] EMPTY_ARRAY = new Skill[0];
	public static final int SKILL_CRAFTING = 172;
	public static final int SKILL_POLEARM_MASTERY = 216;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_WEAPON_MAGIC_MASTERY1 = 249;
	public static final int SKILL_WEAPON_MAGIC_MASTERY2 = 250;
	public static final int SKILL_BLINDING_BLOW = 321;
	public static final int SKILL_STRIDER_ASSAULT = 325;
	public static final int SKILL_WYVERN_AEGIS = 327;
	public static final int SKILL_BLUFF = 358;
	public static final int SKILL_HEROIC_MIRACLE = 395;
	public static final int SKILL_HEROIC_BERSERKER = 396;
	public static final int SKILL_SOUL_MASTERY = 467;
	public static final int SKILL_TRANSFORM_DISPEL = 619;
	public static final int SKILL_FINAL_FLYING_FORM = 840;
	public static final int SKILL_AURA_BIRD_FALCON = 841;
	public static final int SKILL_AURA_BIRD_OWL = 842;
	public static final int SKILL_RECHARGE = 1013;
	public static final int SKILL_TRANSFER_PAIN = 1262;
	public static final int SKILL_FISHING_MASTERY = 1315;
	public static final int SKILL_NOBLESSE_BLESSING = 1323;
	public static final int SKILL_SUMMON_CP_POTION = 1324;
	public static final int SKILL_FORTUNE_OF_NOBLESSE = 1325;
	public static final int SKILL_HARMONY_OF_NOBLESSE = 1326;
	public static final int SKILL_SYMPHONY_OF_NOBLESSE = 1327;
	public static final int SKILL_HEROIC_VALOR = 1374;
	public static final int SKILL_HEROIC_GRANDEUR = 1375;
	public static final int SKILL_HEROIC_DREAD = 1376;
	public static final int SKILL_MYSTIC_IMMUNITY = 1411;
	public static final int SKILL_RAID_BLESSING = 2168;
	public static final int SKILL_HINDER_STRIDER = 4258;
	public static final int SKILL_WYVERN_BREATH = 4289;
	public static final int SKILL_RAID_CURSE = 4515;
	public static final int SKILL_CHARM_OF_COURAGE = 5041;
	private static final Logger _log = LoggerFactory.getLogger(Skill.class);
	protected final int[] _itemConsume;
	protected final int[] _itemConsumeId;
	protected final int _referenceItemId;
	protected final int _referenceItemMpConsume;
	private final int hashCode;
	public boolean _isStandart;
	protected EffectTemplate[] _effectTemplates = EffectTemplate.EMPTY_ARRAY;
	protected List<Integer> _teachers;
	protected List<ClassId> _canLearn;
	protected AddedSkill[] _addedSkills = AddedSkill.EMPTY_ARRAY;
	protected boolean _isAltUse;
	protected boolean _isBehind;
	protected boolean _isCancelable;
	protected boolean _isCorpse;
	protected boolean _isCommon;
	protected boolean _isItemHandler;
	protected boolean _isOffensive;
	protected boolean _isPvpSkill;
	protected boolean _isNotUsedByAI;
	protected boolean _isFishingSkill;
	protected boolean _isPvm;
	protected boolean _isForceUse;
	protected boolean _isNewbie;
	protected boolean _isPreservedOnDeath;
	protected boolean _isHeroic;
	protected boolean _isSaveable;
	protected boolean _isSkillTimePermanent;
	protected boolean _isReuseDelayPermanent;
	protected boolean _isReflectable;
	protected boolean _isSuicideAttack;
	protected boolean _isShieldignore;
	protected boolean _isUndeadOnly;
	protected Ternary _isUseSS;
	protected boolean _isOverhit;
	protected boolean _isSoulBoost;
	protected boolean _isChargeBoost;
	protected boolean _isUsingWhileCasting;
	protected boolean _isIgnoreResists;
	protected boolean _isIgnoreInvul;
	protected boolean _isTrigger;
	protected boolean _isNotAffectedByMute;
	protected boolean _basedOnTargetDebuff;
	protected boolean _deathlink;
	protected boolean _hideStartMessage;
	protected boolean _hideUseMessage;
	protected boolean _skillInterrupt;
	protected boolean _flyingTransformUsage;
	protected boolean _canUseTeleport;
	protected boolean _isProvoke;
	protected boolean _isCubicSkill;
	protected boolean _isSelfDispellable;
	protected boolean _isSlotNone;
	protected boolean _isSharedClassReuse;
	protected SkillType _skillType;
	protected SkillOpType _operateType;
	protected SkillTargetType _targetType;
	protected SkillMagicType _magicType;
	protected SkillTrait _traitType;
	protected BaseStats _saveVs;
	protected SkillNextAction _skillNextAction;
	protected Element _element;
	protected FlyToLocation.FlyType _flyType;
	protected boolean _flyToBack;
	protected Condition[] _preCondition = Condition.EMPTY_ARRAY;
	protected int _id;
	protected int _level;
	protected int _baseLevel;
	protected int _displayId;
	protected int _displayLevel;
	protected int _activateRate;
	protected int _castRange;
	protected int _cancelTarget;
	protected int _coolTime;
	protected int _delayedEffect;
	protected int _effectPoint;
	protected int _energyConsume;
	protected int _elementPower;
	protected int _flyRadius;
	protected int _hitTime;
	protected int _hpConsume;
	protected int _levelModifier;
	protected int _magicLevel;
	protected int _matak;
	protected int _minPledgeClass;
	protected int _minRank;
	protected int _negatePower;
	protected int _negateSkill;
	protected int _npcId;
	protected int _numCharges;
	protected int _skillInterruptTime;
	protected int _skillRadius;
	protected int _effectiveRange;
	protected int _soulsConsume;
	protected int _symbolId;
	protected int _weaponsAllowed;
	protected int _enchantLevelCount;
	protected int _criticalRate;
	protected int _secondSkill;
	protected long _reuseDelay;
	protected double _power;
	protected double _powerPvP;
	protected double _powerPvE;
	protected double _mpConsume1;
	protected double _mpConsume2;
	protected double _lethal1;
	protected double _lethal2;
	protected double _absorbPart;
	protected String _name;
	protected String _baseValues;
	protected String _icon;
	
	protected Skill(StatsSet set)
	{
		_id = set.getInteger("skill_id");
		_level = set.getInteger("level");
		_displayId = set.getInteger("displayId", _id);
		_displayLevel = set.getInteger("displayLevel", _level);
		_baseLevel = set.getInteger("base_level");
		_name = set.getString("name");
		_operateType = set.getEnum("operateType", SkillOpType.class);
		_isNewbie = set.getBool("isNewbie", false);
		_isSelfDispellable = set.getBool("isSelfDispellable", true);
		_isPreservedOnDeath = set.getBool("isPreservedOnDeath", false);
		_isHeroic = set.getBool("isHeroic", false);
		_isAltUse = set.getBool("altUse", false);
		_mpConsume1 = set.getInteger("mpConsume1", 0);
		_mpConsume2 = set.getInteger("mpConsume2", 0);
		_energyConsume = set.getInteger("energyConsume", 0);
		_hpConsume = set.getInteger("hpConsume", 0);
		_soulsConsume = set.getInteger("soulsConsume", 0);
		_isSoulBoost = set.getBool("soulBoost", false);
		_isChargeBoost = set.getBool("chargeBoost", false);
		_isProvoke = set.getBool("provoke", false);
		_isUsingWhileCasting = set.getBool("isUsingWhileCasting", false);
		_matak = set.getInteger("mAtk", 0);
		_isUseSS = Ternary.valueOf(set.getString("useSS", Ternary.DEFAULT.toString()).toUpperCase());
		_magicLevel = set.getInteger("magicLevel", 0);
		_castRange = set.getInteger("castRange", 40);
		_effectiveRange = set.getInteger("effectiveRange", _castRange + (_castRange < 200 ? 400 : 500));
		_baseValues = set.getString("baseValues", null);
		String s1 = set.getString("itemConsumeCount", "");
		String s2 = set.getString("itemConsumeId", "");
		int i;
		String[] s;
		if(s1.length() == 0)
		{
			_itemConsume = new int[] {0};
		}
		else
		{
			s = s1.split(" ");
			_itemConsume = new int[s.length];
			for(i = 0;i < s.length;++i)
			{
				_itemConsume[i] = Integer.parseInt(s[i]);
			}
		}
		if(s2.length() == 0)
		{
			_itemConsumeId = new int[] {0};
		}
		else
		{
			s = s2.split(" ");
			_itemConsumeId = new int[s.length];
			for(i = 0;i < s.length;++i)
			{
				_itemConsumeId[i] = Integer.parseInt(s[i]);
			}
		}
		_referenceItemId = set.getInteger("referenceItemId", 0);
		_referenceItemMpConsume = set.getInteger("referenceItemMpConsume", 0);
		_isItemHandler = set.getBool("isHandler", false);
		_isCommon = set.getBool("isCommon", false);
		_isSaveable = set.getBool("isSaveable", true);
		_coolTime = set.getInteger("coolTime", 0);
		_skillInterruptTime = set.getInteger("hitCancelTime", 0);
		_reuseDelay = set.getLong("reuseDelay", 0);
		_hitTime = set.getInteger("hitTime", 0);
		_skillRadius = set.getInteger("skillRadius", 80);
		_targetType = set.getEnum("target", SkillTargetType.class);
		_magicType = set.getEnum("magicType", SkillMagicType.class, SkillMagicType.PHYSIC);
		_traitType = set.getEnum("trait", SkillTrait.class, null);
		_saveVs = set.getEnum("saveVs", BaseStats.class, null);
		_hideStartMessage = set.getBool("isHideStartMessage", false);
		_hideUseMessage = set.getBool("isHideUseMessage", false);
		_isUndeadOnly = set.getBool("undeadOnly", false);
		_isCorpse = set.getBool("corpse", false);
		_power = set.getDouble("power", 0.0);
		_powerPvP = set.getDouble("powerPvP", 0.0);
		_powerPvE = set.getDouble("powerPvE", 0.0);
		_effectPoint = set.getInteger("effectPoint", 0);
		_skillNextAction = SkillNextAction.valueOf(set.getString("nextAction", "DEFAULT").toUpperCase());
		_skillType = set.getEnum("skillType", SkillType.class);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		_isSkillTimePermanent = set.getBool("isSkillTimePermanent", false);
		_isReuseDelayPermanent = set.getBool("isReuseDelayPermanent", false);
		_deathlink = set.getBool("deathlink", false);
		_basedOnTargetDebuff = set.getBool("basedOnTargetDebuff", false);
		_isNotUsedByAI = set.getBool("isNotUsedByAI", false);
		_isIgnoreResists = set.getBool("isIgnoreResists", false);
		_isIgnoreInvul = set.getBool("isIgnoreInvul", false);
		_isSharedClassReuse = set.getBool("isSharedClassReuse", false);
		_isTrigger = set.getBool("isTrigger", false);
		_isNotAffectedByMute = set.getBool("isNotAffectedByMute", false);
		_flyingTransformUsage = set.getBool("flyingTransformUsage", false);
		_canUseTeleport = set.getBool("canUseTeleport", true);
		_element = NumberUtils.isNumber(set.getString("element", "NONE")) ? Element.getElementById(set.getInteger("element", -1)) : Element.getElementByName(set.getString("element", "none").toUpperCase());
		_elementPower = set.getInteger("elementPower", 0);
		if(_element != Element.NONE && _elementPower == 0)
		{
			_elementPower = 20;
		}
		_activateRate = set.getInteger("activateRate", -1);
		_levelModifier = set.getInteger("levelModifier", 1);
		_isCancelable = set.getBool("cancelable", true);
		_isReflectable = set.getBool("reflectable", true);
		_isShieldignore = set.getBool("shieldignore", false);
		_criticalRate = set.getInteger("criticalRate", 0);
		_isOverhit = set.getBool("overHit", false);
		_weaponsAllowed = set.getInteger("weaponsAllowed", 0);
		_minPledgeClass = set.getInteger("minPledgeClass", 0);
		_minRank = set.getInteger("minRank", 0);
		_isOffensive = set.getBool("isOffensive", _skillType.isOffensive());
		_isPvpSkill = set.getBool("isPvpSkill", _skillType.isPvpSkill());
		_isFishingSkill = set.getBool("isFishingSkill", false);
		_isPvm = set.getBool("isPvm", _skillType.isPvM());
		_isForceUse = set.getBool("isForceUse", false);
		_isBehind = set.getBool("behind", false);
		_symbolId = set.getInteger("symbolId", 0);
		_npcId = set.getInteger("npcId", 0);
		_flyType = FlyToLocation.FlyType.valueOf(set.getString("flyType", "NONE").toUpperCase());
		_flyToBack = set.getBool("flyToBack", false);
		_flyRadius = set.getInteger("flyRadius", 200);
		_negateSkill = set.getInteger("negateSkill", 0);
		_negatePower = set.getInteger("negatePower", Integer.MAX_VALUE);
		_numCharges = set.getInteger("num_charges", 0);
		_delayedEffect = set.getInteger("delayedEffect", 0);
		_cancelTarget = set.getInteger("cancelTarget", 0);
		_skillInterrupt = set.getBool("skillInterrupt", false);
		_lethal1 = set.getDouble("lethal1", 0.0);
		_lethal2 = set.getDouble("lethal2", 0.0);
		_absorbPart = set.getDouble("absorbPart", 0.0);
		_icon = set.getString("icon", "");
		_secondSkill = set.getInteger("secondSkill", 0);
		_isSlotNone = set.getBool("isIgnorBuffLimit", false);
		StringTokenizer st = new StringTokenizer(set.getString("addSkills", ""), ";");
		while(st.hasMoreTokens())
		{
			int id = Integer.parseInt(st.nextToken());
			int level = Integer.parseInt(st.nextToken());
			if(level == -1)
			{
				level = _level;
			}
			_addedSkills = ArrayUtils.add(_addedSkills, new AddedSkill(id, level));
		}
		if(_skillNextAction == SkillNextAction.DEFAULT)
		{
			switch(_skillType)
			{
				case SOWING:
				case LETHAL_SHOT:
				case PDAM:
				case CPDAM:
				case SPOIL:
				case STUN:
				{
					_skillNextAction = SkillNextAction.ATTACK;
					break;
				}
				default:
				{
					_skillNextAction = SkillNextAction.NONE;
				}
			}
		}
		String canLearn;
		if((canLearn = set.getString("canLearn", null)) == null)
		{
			_canLearn = null;
		}
		else
		{
			_canLearn = new ArrayList<>();
			st = new StringTokenizer(canLearn, " \r\n\t,;");
			while(st.hasMoreTokens())
			{
				String cls = st.nextToken();
				_canLearn.add(ClassId.valueOf(cls));
			}
		}
		String teachers = set.getString("teachers", null);
		if(teachers == null)
		{
			_teachers = null;
		}
		else
		{
			_teachers = new ArrayList<>();
			st = new StringTokenizer(teachers, " \r\n\t,;");
			while(st.hasMoreTokens())
			{
				String npcid = st.nextToken();
				_teachers.add(Integer.parseInt(npcid));
			}
		}
		hashCode = _id * 1023 + _level;
	}
	
	public final boolean getWeaponDependancy(Creature activeChar)
	{
		if(_weaponsAllowed == 0)
		{
			return true;
		}
		if(activeChar.getActiveWeaponInstance() != null && activeChar.getActiveWeaponItem() != null && (activeChar.getActiveWeaponItem().getItemType().mask() & (long) _weaponsAllowed) != 0)
		{
			return true;
		}
		if(activeChar.getSecondaryWeaponInstance() != null && activeChar.getSecondaryWeaponItem() != null && (activeChar.getSecondaryWeaponItem().getItemType().mask() & (long) _weaponsAllowed) != 0)
		{
			return true;
		}
		activeChar.sendPacket(new SystemMessage(113).addSkillName(_displayId, _displayLevel));
		return false;
	}
	
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		Player player = activeChar.getPlayer();
		if(activeChar.isDead())
		{
			return false;
		}
		if(target != null && activeChar.getReflection() != target.getReflection())
		{
			activeChar.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			return false;
		}
		if(!getWeaponDependancy(activeChar))
		{
			return false;
		}
		if(activeChar.isUnActiveSkill(_id))
		{
			return false;
		}
		if(first && activeChar.isSkillDisabled(this))
		{
			activeChar.sendReuseMessage(this);
			return false;
		}
		if(first)
		{
			double mpConsume2 = _mpConsume2;
			if(isMusic())
			{
				mpConsume2 += (double) activeChar.getEffectList().getActiveMusicCount(getId()) * mpConsume2 / 2.0;
				mpConsume2 = activeChar.calcStat(Stats.MP_DANCE_SKILL_CONSUME, mpConsume2, target, this);
			}
			else
			{
				mpConsume2 = isMagic() ? activeChar.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, target, this) : activeChar.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, target, this);
			}
			if(activeChar.getCurrentMp() < _mpConsume1 + mpConsume2)
			{
				activeChar.sendPacket(Msg.NOT_ENOUGH_MP);
				return false;
			}
		}
		if(activeChar.getCurrentHp() < (double) (_hpConsume + 1))
		{
			activeChar.sendPacket(Msg.NOT_ENOUGH_HP);
			return false;
		}
		if(!_isItemHandler && !_isAltUse && activeChar.isMuted(this))
		{
			return false;
		}
		if(_soulsConsume > activeChar.getConsumedSouls())
		{
			activeChar.sendPacket(Msg.THERE_IS_NOT_ENOUGHT_SOUL);
			return false;
		}
		if(player != null)
		{
			if(player.isInFlyingTransform() && _isItemHandler && !flyingTransformUsage())
			{
				player.sendPacket(new SystemMessage(113).addItemName(getItemConsumeId()[0]));
				return false;
			}
			if(player.isInBoat() && player.getBoat().isVehicle() && !(this instanceof FishingSkill) && !(this instanceof ReelingPumping))
			{
				return false;
			}
			if(player.isInObserverMode())
			{
				activeChar.sendPacket(Msg.OBSERVERS_CANNOT_PARTICIPATE);
				return false;
			}
			if(first && _itemConsume[0] > 0)
			{
				for(int i = 0;i < _itemConsume.length;++i)
				{
					ItemContainer inv = activeChar instanceof Summon ? player.getInventory() : ((Playable) activeChar).getInventory();
					ItemInstance requiredItems = inv.getItemByItemId(_itemConsumeId[i]);
					if(requiredItems != null && requiredItems.getCount() >= (long) _itemConsume[i])
						continue;
					if(activeChar == player)
					{
						player.sendPacket(isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : new SystemMessage(113).addSkillName(getDisplayId(), getDisplayLevel()));
					}
					return false;
				}
			}
			if(!(!player.isFishing() || isFishingSkill() || altUse() || activeChar.isSummon() || activeChar.isPet()))
			{
				if(activeChar == player)
				{
					player.sendPacket(Msg.ONLY_FISHING_SKILLS_ARE_AVAILABLE);
				}
				return false;
			}
			if(player.isOlyParticipant() && isOffensive() && !player.isOlyCompetitionStarted() && getId() != 347)
			{
				return false;
			}
		}
		if(getFlyType() != FlyToLocation.FlyType.NONE && getId() != 628 && getId() != 821 && (activeChar.isImmobilized() || activeChar.isRooted()))
		{
			activeChar.getPlayer().sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}
		if(first && target != null && getFlyType() == FlyToLocation.FlyType.CHARGE && activeChar.isInRange(target.getLoc(), (long) Math.min(150, getFlyRadius())))
		{
			activeChar.getPlayer().sendPacket(Msg.THERE_IS_NOT_ENOUGH_SPACE_TO_MOVE_THE_SKILL_CANNOT_BE_USED);
			return false;
		}
		SystemMsg msg = checkTarget(activeChar, target, target, forceUse, first);
		if(msg != null && activeChar.getPlayer() != null)
		{
			activeChar.getPlayer().sendPacket(msg);
			return false;
		}
		if(_preCondition.length == 0)
		{
			return true;
		}
		Env env = new Env();
		env.character = activeChar;
		env.skill = this;
		env.target = target;
		if(first)
		{
			for(Condition c : _preCondition)
			{
				if(c.test(env))
					continue;
				SystemMsg cond_msg = c.getSystemMsg();
				if(cond_msg != null)
				{
					if(cond_msg.size() > 0)
					{
						activeChar.sendPacket(new SystemMessage2(cond_msg).addSkillName(this));
					}
					else
					{
						activeChar.sendPacket(cond_msg);
					}
				}
				return false;
			}
		}
		return true;
	}
	
	public int getSecondSkill()
	{
		return _secondSkill;
	}
	
	public SystemMsg checkTarget(Creature activeChar, Creature target, Creature aimingTarget, boolean forceUse, boolean first)
	{
		if(target == activeChar && isNotTargetAoE() || target == activeChar.getPet() && _targetType == SkillTargetType.TARGET_PET_AURA)
		{
			return null;
		}
		if(target == null || isOffensive() && target == activeChar)
		{
			return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
		}
		if(activeChar.getReflection() != target.getReflection())
		{
			return SystemMsg.CANNOT_SEE_TARGET;
		}
		if(target != activeChar && target == aimingTarget && getCastRange() > 0 && getCastRange() < 32767)
		{
			if(!GeoEngine.canSeeTarget(activeChar, target, activeChar.isFlying()))
			{
				return SystemMsg.CANNOT_SEE_TARGET;
			}
			if(!first)
			{
				int minRange = (int) ((double) Math.max(0, getEffectiveRange()) + activeChar.getMinDistance(target) + 16.0);
				if(!activeChar.isInRange(target.getLoc(), (long) minRange))
				{
					return SystemMsg.THE_DISTANCE_IS_TOO_FAR_AND_SO_THE_CASTING_HAS_BEEN_STOPPED;
				}
			}
		}
		if(_skillType == SkillType.TAKECASTLE)
		{
			return null;
		}
		if(!first && target != activeChar && (_targetType == SkillTargetType.TARGET_MULTIFACE || _targetType == SkillTargetType.TARGET_MULTIFACE_AURA || _targetType == SkillTargetType.TARGET_TUNNEL) && (_isBehind ? PositionUtils.isFacing(activeChar, target, 120) : !PositionUtils.isFacing(activeChar, target, 60)))
		{
			return SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE;
		}
		if(target.isDead() != _isCorpse && _targetType != SkillTargetType.TARGET_AREA_AIM_CORPSE || _isUndeadOnly && !target.isUndead())
		{
			return SystemMsg.INVALID_TARGET;
		}
		if(_isAltUse || _targetType == SkillTargetType.TARGET_FEEDABLE_BEAST || _targetType == SkillTargetType.TARGET_UNLOCKABLE || _targetType == SkillTargetType.TARGET_CHEST)
		{
			return null;
		}
		Player player = activeChar.getPlayer();
		Player pcTarget;
		if(player != null && (pcTarget = target.getPlayer()) != null)
		{
			if(isPvM())
			{
				return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
			}
			if(player.isInZone(Zone.ZoneType.epic) != pcTarget.isInZone(Zone.ZoneType.epic))
			{
				return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
			}
			if(!player.isOlyParticipant() && pcTarget.isOlyParticipant() || player.isOlyParticipant() && !pcTarget.isOlyParticipant() || player.isOlyParticipant() && pcTarget.isOlyParticipant() && player.getOlyParticipant().getCompetition() != pcTarget.getOlyParticipant().getCompetition())
			{
				return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
			}
			if(isOffensive())
			{
				if(player.isOlyParticipant() && pcTarget.isOlyParticipant() && player.getOlyParticipant().getCompetition() != pcTarget.getOlyParticipant().getCompetition())
				{
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
				}
				if(player.isOlyParticipant() && !player.isOlyCompetitionStarted())
				{
					return SystemMsg.INVALID_TARGET;
				}
				if(player.isOlyParticipant() && player.getOlyParticipant() == pcTarget.getOlyParticipant())
				{
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
				}
				if(pcTarget.isOlyParticipant() && pcTarget.isLooseOlyCompetition())
				{
					return SystemMsg.INVALID_TARGET;
				}
				if(player.getTeam() != TeamType.NONE && player.getTeam() == pcTarget.getTeam())
				{
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
				}
				if(isAoE() && getCastRange() < 32767 && !GeoEngine.canSeeTarget(activeChar, target, activeChar.isFlying()))
				{
					return SystemMsg.CANNOT_SEE_TARGET;
				}
				if(activeChar.isInZoneBattle() != target.isInZoneBattle() && !player.getPlayerAccess().PeaceAttack)
				{
					return SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;
				}
				if((activeChar.isInZonePeace() || target.isInZonePeace()) && !player.getPlayerAccess().PeaceAttack)
				{
					return SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;
				}
				if(isAoE() && player.getParty() != null && player.getParty() == pcTarget.getParty())
				{
					return SystemMsg.INVALID_TARGET;
				}
				if(activeChar.isInZoneBattle())
				{
					if(!forceUse && !isForceUse() && player.getParty() != null && player.getParty() == pcTarget.getParty())
					{
						return SystemMsg.INVALID_TARGET;
					}
					return null;
				}
				for(GlobalEvent e : player.getEvents())
				{
					SystemMsg msg = e.checkForAttack(target, activeChar, this, forceUse);
					if(msg == null)
						continue;
					return msg;
				}
				for(GlobalEvent e : player.getEvents())
				{
					if(!e.canAttack(target, activeChar, this, forceUse))
						continue;
					return null;
				}
				if(isProvoke())
				{
					if(!forceUse && player.getParty() != null && player.getParty() == pcTarget.getParty())
					{
						return SystemMsg.INVALID_TARGET;
					}
					return null;
				}
				if(isPvpSkill() || !forceUse || isAoE())
				{
					if(player == pcTarget)
					{
						return SystemMsg.INVALID_TARGET;
					}
					if(player.getParty() != null && player.getParty() == pcTarget.getParty())
					{
						return SystemMsg.INVALID_TARGET;
					}
					if(player.getClan() != null && player.getClan() == pcTarget.getClan())
					{
						return SystemMsg.INVALID_TARGET;
					}
					if(Config.ALLY_ALLOW_BUFF_DEBUFFS && player.getAlliance() != null && player.getAlliance() == pcTarget.getAlliance())
					{
						return SystemMsg.INVALID_TARGET;
					}
				}
				if(activeChar.isInZone(Zone.ZoneType.SIEGE) && target.isInZone(Zone.ZoneType.SIEGE))
				{
					return null;
				}
				if(activeChar.isInZone(Zone.ZoneType.fun) && target.isInZone(Zone.ZoneType.fun))
				{
					return null;
				}
				if(player.atMutualWarWith(pcTarget))
				{
					return null;
				}
				if(isForceUse())
				{
					return null;
				}
				if(pcTarget.getPvpFlag() != 0)
				{
					return null;
				}
				if(pcTarget.getKarma() > 0)
				{
					return null;
				}
				if(!(!forceUse || isPvpSkill() || isAoE() && aimingTarget != target))
				{
					return null;
				}
				return SystemMsg.INVALID_TARGET;
			}
			if(pcTarget == player)
			{
				return null;
			}
			if(player.isOlyParticipant() && player.getOlyParticipant().getCompetition() == pcTarget.getOlyParticipant().getCompetition() && player.getOlyParticipant() != pcTarget.getOlyParticipant())
			{
				if(player.getOlyParticipant().getCompetition().getType() == CompetitionType.TEAM_CLASS_FREE)
				{
					return SystemMsg.INVALID_TARGET;
				}
				if(!forceUse)
				{
					return SystemMsg.INVALID_TARGET;
				}
			}
			if(!activeChar.isInZoneBattle() && target.isInZoneBattle())
			{
				return SystemMsg.INVALID_TARGET;
			}
			if(forceUse || isForceUse())
			{
				return null;
			}
			if(player.getParty() != null && player.getParty() == pcTarget.getParty())
			{
				return null;
			}
			if(player.getClan() != null && player.getClan() == pcTarget.getClan())
			{
				return null;
			}
			if(Config.ALLY_ALLOW_BUFF_DEBUFFS && player.getAlliance() != null && player.getAlliance() == pcTarget.getAlliance())
			{
				return null;
			}
			if(player.atMutualWarWith(pcTarget))
			{
				return SystemMsg.INVALID_TARGET;
			}
			if(pcTarget.getPvpFlag() != 0)
			{
				return SystemMsg.INVALID_TARGET;
			}
			if(pcTarget.getKarma() > 0)
			{
				return SystemMsg.INVALID_TARGET;
			}
			return null;
		}
		if(isAoE() && isOffensive() && getCastRange() < 32767 && !GeoEngine.canSeeTarget(activeChar, target, activeChar.isFlying()))
		{
			return SystemMsg.CANNOT_SEE_TARGET;
		}
		if(!forceUse && !isForceUse() && !isOffensive() && target.isAutoAttackable(activeChar))
		{
			return SystemMsg.INVALID_TARGET;
		}
		if(!forceUse && !isForceUse() && isOffensive() && !target.isAutoAttackable(activeChar))
		{
			return SystemMsg.INVALID_TARGET;
		}
		if(!target.isAttackable(activeChar))
		{
			return SystemMsg.INVALID_TARGET;
		}
		return null;
	}
	
	public final Creature getAimingTarget(Creature activeChar, GameObject obj)
	{
		Creature target = obj == null || !obj.isCreature() ? null : (Creature) obj;
		switch(_targetType)
		{
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_PARTY:
			case TARGET_CLAN_ONLY:
			case TARGET_SELF:
			{
				return activeChar;
			}
			case TARGET_AURA:
			case TARGET_COMMCHANNEL:
			case TARGET_MULTIFACE_AURA:
			{
				return activeChar;
			}
			case TARGET_HOLY:
			{
				return target != null && activeChar.isPlayer() && target.isArtefact() ? target : null;
			}
			case TARGET_FLAGPOLE:
			{
				return activeChar;
			}
			case TARGET_UNLOCKABLE:
			{
				return target != null && target.isDoor() || target instanceof ChestInstance ? target : null;
			}
			case TARGET_CHEST:
			{
				return target instanceof ChestInstance ? target : null;
			}
			case TARGET_FEEDABLE_BEAST:
			{
				return target instanceof FeedableBeastInstance ? target : null;
			}
			case TARGET_PET:
			case TARGET_PET_AURA:
			{
				target = activeChar.getPet();
				return target != null && target.isDead() == _isCorpse ? target : null;
			}
			case TARGET_OWNER:
			{
				if(!activeChar.isSummon() && !activeChar.isPet())
				{
					return null;
				}
				target = activeChar.getPlayer();
				return target != null && target.isDead() == _isCorpse ? target : null;
			}
			case TARGET_ENEMY_PET:
			{
				if(target == null || target == activeChar.getPet() || !target.isPet())
				{
					return null;
				}
				return target;
			}
			case TARGET_ENEMY_SUMMON:
			{
				if(target == null || target == activeChar.getPet() || !target.isSummon())
				{
					return null;
				}
				return target;
			}
			case TARGET_ENEMY_SERVITOR:
			{
				if(target == null || target == activeChar.getPet() || !(target instanceof Summon))
				{
					return null;
				}
				return target;
			}
			case TARGET_ONE:
			{
				return !(target == null || target.isDead() != _isCorpse || target == activeChar && isOffensive() || _isUndeadOnly && !target.isUndead()) ? target : null;
			}
			case TARGET_OTHER:
			{
				return target != null && target != activeChar && target.isDead() == _isCorpse && (!_isUndeadOnly || target.isUndead()) ? target : null;
			}
			case TARGET_AREA:
			case TARGET_MULTIFACE:
			case TARGET_TUNNEL:
			{
				return !(target == null || target.isDead() != _isCorpse || target == activeChar && isOffensive() || _isUndeadOnly && !target.isUndead()) ? target : null;
			}
			case TARGET_AREA_AIM_CORPSE:
			{
				return target != null && target.isDead() ? target : null;
			}
			case TARGET_CORPSE:
			{
				if(target == null || !target.isDead())
				{
					return null;
				}
				if(target.isSummon() && target != activeChar.getPet())
				{
					return target;
				}
				return target.isNpc() ? target : null;
			}
			case TARGET_CORPSE_PLAYER:
			{
				return target != null && target.isPlayable() && target.isDead() ? target : null;
			}
			case TARGET_SIEGE:
			{
				return target != null && !target.isDead() && target.isDoor() ? target : null;
			}
		}
		activeChar.sendMessage("Target type of skill is not currently handled");
		return null;
	}
	
	public List<Creature> getTargets(Creature activeChar, Creature aimingTarget, boolean forceUse)
	{
		if(oneTarget())
		{
			LazyArrayList<Creature> targets = new LazyArrayList<>(1);
			targets.add(aimingTarget);
			return targets;
		}
		LazyArrayList<Creature> targets = new LazyArrayList<>();
		switch(_targetType)
		{
			case TARGET_AREA:
			case TARGET_MULTIFACE:
			case TARGET_TUNNEL:
			case TARGET_AREA_AIM_CORPSE:
			{
				if(aimingTarget.isDead() == _isCorpse && (!_isUndeadOnly || aimingTarget.isUndead()))
				{
					targets.add(aimingTarget);
				}
				addTargetsToList(targets, aimingTarget, activeChar, forceUse);
				break;
			}
			case TARGET_AURA:
			case TARGET_MULTIFACE_AURA:
			{
				addTargetsToList(targets, activeChar, activeChar, forceUse);
				break;
			}
			case TARGET_COMMCHANNEL:
			{
				if(activeChar.getPlayer() == null)
					break;
				if(activeChar.getPlayer().isInParty())
				{
					if(activeChar.getPlayer().getParty().isInCommandChannel())
					{
						for(Player p : activeChar.getPlayer().getParty().getCommandChannel())
						{
							if(p.isDead() || !p.isInRange(activeChar, _skillRadius == 0 ? 600 : (long) _skillRadius))
								continue;
							targets.add(p);
						}
						addTargetAndPetToList(targets, activeChar.getPlayer(), activeChar.getPlayer());
						break;
					}
					for(Player p : activeChar.getPlayer().getParty().getPartyMembers())
					{
						if(p.isDead() || !p.isInRange(activeChar, _skillRadius == 0 ? 600 : (long) _skillRadius))
							continue;
						targets.add(p);
					}
					addTargetAndPetToList(targets, activeChar.getPlayer(), activeChar.getPlayer());
					break;
				}
				targets.add(activeChar);
				addTargetAndPetToList(targets, activeChar.getPlayer(), activeChar.getPlayer());
				break;
			}
			case TARGET_PET_AURA:
			{
				if(activeChar.getPet() == null)
					break;
				addTargetsToList(targets, activeChar.getPet(), activeChar, forceUse);
				break;
			}
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_PARTY:
			case TARGET_CLAN_ONLY:
			{
				if(activeChar.isMonster() || activeChar.isSiegeGuard())
				{
					targets.add(activeChar);
					for(Creature c : World.getAroundCharacters(activeChar, _skillRadius, 600))
					{
						if(c.isDead() || !c.isMonster() && !c.isSiegeGuard())
							continue;
						targets.add(c);
					}
					break;
				}
				Player player = activeChar.getPlayer();
				if(player == null)
					break;
				for(Player target : World.getAroundPlayers(player, _skillRadius, 600))
				{
					boolean check = false;
					switch(_targetType)
					{
						case TARGET_PARTY:
						{
							check = player.getParty() != null && player.getParty() == target.getParty();
							break;
						}
						case TARGET_CLAN:
						{
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId() || player.getParty() != null && target.getParty() == player.getParty();
							break;
						}
						case TARGET_CLAN_ONLY:
						{
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId();
							break;
						}
						case TARGET_ALLY:
						{
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId() || player.getAllyId() != 0 && target.getAllyId() == player.getAllyId();
						}
					}
					if(!check || player.isOlyParticipant() && target.isOlyParticipant() && player.getOlyParticipant() != target.getOlyParticipant() || checkTarget(player, target, aimingTarget, forceUse, false) != null)
						continue;
					addTargetAndPetToList(targets, player, target);
				}
				addTargetAndPetToList(targets, player, player);
				break;
			}
		}
		return targets;
	}
	
	private void addTargetAndPetToList(List<Creature> targets, Player actor, Player target)
	{
		if((actor == target || actor.isInRange(target, (long) _skillRadius)) && target.isDead() == _isCorpse)
		{
			targets.add(target);
		}
		Summon pet;
		if((pet = target.getPet()) != null && actor.isInRange(pet, (long) _skillRadius) && pet.isDead() == _isCorpse)
		{
			targets.add(pet);
		}
	}
	
	private void addTargetsToList(List<Creature> targets, Creature aimingTarget, Creature activeChar, boolean forceUse)
	{
		Polygon terr = null;
		if(_targetType == SkillTargetType.TARGET_TUNNEL)
		{
			int zmin1 = activeChar.getZ() - 200;
			int zmax1 = activeChar.getZ() + 200;
			int zmin2 = aimingTarget.getZ() - 200;
			int zmax2 = aimingTarget.getZ() + 200;
			double angle = PositionUtils.convertHeadingToDegree(activeChar.getHeading());
			terr = new Polygon();
			double radian1 = Math.toRadians(angle - 90.0);
			int radius = 100;
			terr.add(activeChar.getX() + (int) (Math.cos(radian1) * (double) radius), activeChar.getY() + (int) (Math.sin(radian1) * (double) radius));
			double radian2 = Math.toRadians(angle + 90.0);
			terr.add(activeChar.getX() + (int) (Math.cos(radian2) * (double) radius), activeChar.getY() + (int) (Math.sin(radian2) * (double) radius));
			terr.add(aimingTarget.getX() + (int) (Math.cos(radian2) * (double) radius), aimingTarget.getY() + (int) (Math.sin(radian2) * (double) radius));
			terr.add(aimingTarget.getX() + (int) (Math.cos(radian1) * (double) radius), aimingTarget.getY() + (int) (Math.sin(radian1) * (double) radius));
			terr.setZmin(Math.min(zmin1, zmin2)).setZmax(Math.max(zmax1, zmax2));
		}
		int count = 0;
		for(Creature target : aimingTarget.getAroundCharacters(_skillRadius, 300))
		{
			if(terr != null && !terr.isInside(target.getX(), target.getY(), target.getZ()) || target == null || activeChar == target || activeChar.getPlayer() != null && activeChar.getPlayer() == target.getPlayer() || checkTarget(activeChar, target, aimingTarget, forceUse, false) != null || activeChar.isNpc() && target.isNpc())
				continue;
			targets.add(target);
			if(!isOffensive() || ++count < 20 || activeChar.isRaid())
				continue;
			break;
		}
	}
	
	public final void getEffects(Creature effector, Creature effected, boolean calcChance, boolean applyOnCaster)
	{
		getEffects(effector, effected, calcChance, applyOnCaster, false);
	}
	
	public final void getEffects(Creature effector, Creature effected, boolean calcChance, boolean applyOnCaster, boolean skillReflected)
	{
		double timeMult = 1.0;
		if(isMusic())
		{
			timeMult = Config.SONGDANCETIME_MODIFIER;
		}
		else if(getId() >= 4342 && getId() <= 4360)
		{
			timeMult = Config.CLANHALL_BUFFTIME_MODIFIER;
		}
		getEffects(effector, effected, calcChance, applyOnCaster, 0, timeMult, skillReflected);
	}
	
	public final void getEffects(Creature effector, Creature effected, boolean calcChance, boolean applyOnCaster, long timeConst, double timeMult, boolean skillReflected)
	{
		if(isPassive() || !hasEffects() || effector == null || effected == null)
		{
			return;
		}
		boolean isChg = false;
		if(getId() == 345 || getId() == 346 || getId() == 321 || getId() == 369 || getId() == 1231)
		{
			boolean bl = isChg = effected == effector;
		}
		if(!isChg && (effected.isEffectImmune() || effected.isInvul() && isOffensive()))
		{
			if(effector.isPlayer())
			{
				effector.sendPacket(new SystemMessage(139).addName(effected).addSkillName(_displayId, _displayLevel));
			}
			return;
		}
		if(effected.isDoor() || effected.isAlikeDead() && !isPreservedOnDeath())
		{
			return;
		}
		ThreadPoolManager.getInstance().execute(new RunnableImpl()
		{
			
			@Override
			public void runImpl()
			{
				boolean skillMastery = false;
				int sps = effector.getChargedSpiritShot();
				if(effector.getSkillMastery(getId()) == 2)
				{
					skillMastery = true;
					effector.removeSkillMastery(getId());
				}
				boolean success = false;
				for(EffectTemplate et : getEffectTemplates())
				{
					if(applyOnCaster != et._applyOnCaster || et._count == 0)
						continue;
					Creature character = et._applyOnCaster || et._isReflectable && skillReflected ? effector : effected;
					LazyArrayList<Creature> targets = new LazyArrayList<>(1);
					targets.add(character);
					Summon summon;
					if(et._applyOnSummon && character.isPlayer() && (summon = character.getPlayer().getPet()) != null && summon.isSummon() && !isOffensive() && !isToggle() && !isCubicSkill())
					{
						targets.add(summon);
					}
					block1:
					for(Creature target : targets)
					{
						if(target.isAlikeDead() && !isPreservedOnDeath() || target.isRaid() && et.getEffectType().isRaidImmune())
							continue;
						if(et.getPeriod() > 0 && (effected.isBuffImmune() && !isOffensive() && effector != effected && getId() != 1323 || effected.isDebuffImmune() && isOffensive()))
						{
							if(!effector.isPlayer())
								continue;
							effector.sendPacket(new SystemMessage(139).addName(effected).addSkillName(_displayId, _displayLevel));
							continue;
						}
						if(isBlockedByChar(target, et))
							continue;
						if(et._stackOrder == -1)
						{
							if(!et._stackType.equals("none"))
							{
								for(Effect e2 : target.getEffectList().getAllEffects())
								{
									if(!e2.getStackType().equalsIgnoreCase(et._stackType))
										continue;
									continue block1;
								}
							}
							else if(target.getEffectList().getEffectsBySkillId(getId()) != null)
								continue;
						}
						Env env = new Env(effector, target, Skill.this);
						int chance = et.chance(getActivateRate());
						if((calcChance || chance >= 0) && !et._applyOnCaster)
						{
							env.value = chance;
							if(!Formulas.calcSkillSuccess(env, et, sps))
								continue;
						}
						if(_isReflectable && et._isReflectable && isOffensive() && target != effector && !effector.isTrap() && Rnd.chance(target.calcStat(isMagic() ? Stats.REFLECT_MAGIC_DEBUFF : Stats.REFLECT_PHYSIC_DEBUFF, 0.0, effector, Skill.this)))
						{
							target.sendPacket(new SystemMessage(1998).addName(effector));
							effector.sendPacket(new SystemMessage(1999).addName(target));
							target = effector;
							env.target = target;
						}
						if(success)
						{
							env.value = 2.147483647E9;
						}
						Effect e;
						if((e = et.getEffect(env)) == null)
							continue;
						if(chance > 0)
						{
							success = true;
						}
						if(e.isOneTime())
						{
							if(!e.checkCondition())
								continue;
							e.onStart();
							e.onActionTime();
							e.onExit();
							continue;
						}
						int count = et.getCount();
						long period = et.getPeriod();
						if(skillMastery)
						{
							if(count > 1)
							{
								count *= 2;
							}
							else
							{
								period *= 2;
							}
						}
						if(Config.CALC_EFFECT_TIME_YIELD_AND_RESIST && !et._applyOnCaster && isOffensive() && !isIgnoreResists() && !effector.isRaid())
						{
							double res = 0.0;
							Pair<Stats, Stats> resistAndPowerType = et.getEffectType().getResistAndPowerType();
							if(resistAndPowerType != null)
							{
								Stats resistType = resistAndPowerType.getLeft();
								Stats powerType = resistAndPowerType.getRight();
								if(resistType != null)
								{
									res += effected.calcStat(resistType, effector, Skill.this);
								}
								if(powerType != null)
								{
									res -= effector.calcStat(powerType, effected, Skill.this);
								}
							}
							if((res += effected.calcStat(Stats.DEBUFF_RESIST, effector, Skill.this)) != 0.0)
							{
								double mod = 1.0 + Math.abs(0.01 * res);
								if(res > 0.0)
								{
									mod = 1.0 / mod;
								}
								if(count > 1)
								{
									count = (int) Math.floor(Math.max((double) count * mod, 1.0));
								}
								else
								{
									period = (long) Math.floor(Math.max((double) period * mod, 1.0));
								}
							}
						}
						if(timeConst > 0)
						{
							period = count > 1 ? timeConst / (long) count : timeConst;
						}
						else if(timeMult > 1.0)
						{
							if(count > 1)
							{
								count = (int) ((double) count * timeMult);
							}
							else
							{
								period = (long) ((double) period * timeMult);
							}
						}
						Skill s = e.getSkill();
						if(s != null && Config.SKILL_DURATION_MOD.containsKey(s.getId()))
						{
							int mtime = Config.SKILL_DURATION_MOD.get(s.getId());
							if(s.getLevel() >= 100 && s.getLevel() < 140)
							{
								if(count > 1)
								{
									count = mtime;
								}
								else
								{
									period = mtime;
								}
							}
							else if(count > 1)
							{
								count = mtime;
							}
							else
							{
								period = mtime;
							}
						}
						e.setCount(count);
						e.setPeriod(period);
						e.schedule();
					}
				}
				if(calcChance)
				{
					if(success)
					{
						effector.sendPacket(new SystemMessage(1595).addSkillName(_displayId, _displayLevel));
					}
					else
					{
						effector.sendPacket(new SystemMessage(1597).addSkillName(_displayId, _displayLevel));
					}
				}
			}
		});
	}
	
	public final void attach(EffectTemplate effect)
	{
		_effectTemplates = ArrayUtils.add(_effectTemplates, effect);
	}
	
	public EffectTemplate[] getEffectTemplates()
	{
		return _effectTemplates;
	}
	
	public boolean hasEffects()
	{
		return _effectTemplates.length > 0;
	}
	
	public final Func[] getStatFuncs()
	{
		return getStatFuncs(this);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(obj == null)
		{
			return false;
		}
		if(getClass() != obj.getClass())
		{
			return false;
		}
		return hashCode() == obj.hashCode();
	}
	
	@Override
	public int hashCode()
	{
		return hashCode;
	}
	
	public final void attach(Condition c)
	{
		_preCondition = ArrayUtils.add(_preCondition, c);
	}
	
	public final boolean altUse()
	{
		return _isAltUse;
	}
	
	public final boolean canTeachBy(int npcId)
	{
		return _teachers == null || _teachers.contains(npcId);
	}
	
	public final int getActivateRate()
	{
		return _activateRate;
	}
	
	public AddedSkill[] getAddedSkills()
	{
		return _addedSkills;
	}
	
	public final boolean getCanLearn(ClassId cls)
	{
		return _canLearn == null || _canLearn.contains(cls);
	}
	
	public final int getCastRange()
	{
		return _castRange;
	}
	
	public void setCastRange(int castRange)
	{
		_castRange = castRange;
	}
	
	public int getEffectiveRange()
	{
		return _effectiveRange;
	}
	
	public final int getAOECastRange()
	{
		return Math.max(_castRange, _skillRadius);
	}
	
	public final int getCoolTime()
	{
		return _coolTime;
	}
	
	public boolean getCorpse()
	{
		return _isCorpse;
	}
	
	public int getDelayedEffect()
	{
		return _delayedEffect;
	}
	
	public final int getDisplayId()
	{
		return _displayId;
	}
	
	public int getDisplayLevel()
	{
		return _displayLevel;
	}
	
	public void setDisplayLevel(int lvl)
	{
		_displayLevel = lvl;
	}
	
	public int getEffectPoint()
	{
		return _effectPoint;
	}
	
	public Effect getSameByStackType(List<Effect> list)
	{
		for(EffectTemplate et : getEffectTemplates())
		{
			Effect ret;
			if(et == null || (ret = et.getSameByStackType(list)) == null)
				continue;
			return ret;
		}
		return null;
	}
	
	public Effect getSameByStackType(EffectList list)
	{
		return getSameByStackType(list.getAllEffects());
	}
	
	public Effect getSameByStackType(Creature actor)
	{
		return getSameByStackType(actor.getEffectList().getAllEffects());
	}
	
	public final Element getElement()
	{
		return _element;
	}
	
	public final int getElementPower()
	{
		return _elementPower;
	}
	
	public Skill getFirstAddedSkill()
	{
		if(_addedSkills.length == 0)
		{
			return null;
		}
		return _addedSkills[0].getSkill();
	}
	
	public int getFlyRadius()
	{
		return _flyRadius;
	}
	
	public FlyToLocation.FlyType getFlyType()
	{
		return _flyType;
	}
	
	public boolean isFlyToBack()
	{
		return _flyToBack;
	}
	
	public final int getHitTime()
	{
		return _hitTime;
	}
	
	public void setHitTime(int hitTime)
	{
		_hitTime = hitTime;
	}
	
	public final int getHpConsume()
	{
		return _hpConsume;
	}
	
	public void setHpConsume(int hpConsume)
	{
		_hpConsume = hpConsume;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public final int[] getItemConsume()
	{
		return _itemConsume;
	}
	
	public final int[] getItemConsumeId()
	{
		return _itemConsumeId;
	}
	
	public final int getReferenceItemId()
	{
		return _referenceItemId;
	}
	
	public final int getReferenceItemMpConsume()
	{
		return _referenceItemMpConsume;
	}
	
	public final int getLevel()
	{
		return _level;
	}
	
	public final int getBaseLevel()
	{
		return _baseLevel;
	}
	
	public final void setBaseLevel(int baseLevel)
	{
		_baseLevel = baseLevel;
	}
	
	public final int getLevelModifier()
	{
		return _levelModifier;
	}
	
	public final int getMagicLevel()
	{
		return _magicLevel;
	}
	
	public final void setMagicLevel(int newlevel)
	{
		_magicLevel = newlevel;
	}
	
	public int getMatak()
	{
		return _matak;
	}
	
	public int getMinPledgeClass()
	{
		return _minPledgeClass;
	}
	
	public int getMinRank()
	{
		return _minRank;
	}
	
	public final double getMpConsume()
	{
		return _mpConsume1 + _mpConsume2;
	}
	
	public final double getMpConsume1()
	{
		return _mpConsume1;
	}
	
	public void setMpConsume1(double mpConsume1)
	{
		_mpConsume1 = mpConsume1;
	}
	
	public final double getMpConsume2()
	{
		return _mpConsume2;
	}
	
	public void setMpConsume2(double mpConsume2)
	{
		_mpConsume2 = mpConsume2;
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public int getNegatePower()
	{
		return _negatePower;
	}
	
	public int getNegateSkill()
	{
		return _negateSkill;
	}
	
	public SkillNextAction getSkillNextAction()
	{
		return _skillNextAction;
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public int getNumCharges()
	{
		return _numCharges;
	}
	
	public final double getPower(Creature target)
	{
		if(target != null)
		{
			if(target.isPlayable())
			{
				return getPowerPvP();
			}
			if(target.isMonster())
			{
				return getPowerPvE();
			}
		}
		return getPower();
	}
	
	public final double getPower()
	{
		return _power;
	}
	
	public final void setPower(double power)
	{
		_power = power;
	}
	
	public final double getPowerPvP()
	{
		return _powerPvP != 0.0 ? _powerPvP : _power;
	}
	
	public final double getPowerPvE()
	{
		return _powerPvE != 0.0 ? _powerPvE : _power;
	}
	
	public final long getReuseDelay()
	{
		return _reuseDelay;
	}
	
	public final void setReuseDelay(long newReuseDelay)
	{
		_reuseDelay = newReuseDelay;
	}
	
	public final boolean getShieldIgnore()
	{
		return _isShieldignore;
	}
	
	public final boolean isReflectable()
	{
		return _isReflectable;
	}
	
	public final int getSkillInterruptTime()
	{
		return _skillInterruptTime;
	}
	
	public void setSkillInterruptTime(int skillInterruptTime)
	{
		_skillInterruptTime = skillInterruptTime;
	}
	
	public final int getSkillRadius()
	{
		return _skillRadius;
	}
	
	public final SkillType getSkillType()
	{
		return _skillType;
	}
	
	public int getSoulsConsume()
	{
		return _soulsConsume;
	}
	
	public int getSymbolId()
	{
		return _symbolId;
	}
	
	public final SkillTargetType getTargetType()
	{
		return _targetType;
	}
	
	public final SkillTrait getTraitType()
	{
		return _traitType;
	}
	
	public final BaseStats getSaveVs()
	{
		return _saveVs;
	}
	
	public final int getWeaponsAllowed()
	{
		return _weaponsAllowed;
	}
	
	public double getLethal1()
	{
		return _lethal1;
	}
	
	public double getLethal2()
	{
		return _lethal2;
	}
	
	public String getBaseValues()
	{
		return _baseValues;
	}
	
	public boolean isBlockedByChar(Creature effected, EffectTemplate et)
	{
		if(et.getAttachedFuncs() == null)
		{
			return false;
		}
		for(FuncTemplate func : et.getAttachedFuncs())
		{
			if(func == null || !effected.checkBlockedStat(func._stat))
				continue;
			return true;
		}
		return false;
	}
	
	public final boolean isCancelable()
	{
		return _isCancelable && getSkillType() != SkillType.TRANSFORMATION && !isToggle();
	}
	
	public final boolean isCommon()
	{
		return _isCommon;
	}
	
	public final int getCriticalRate()
	{
		return _criticalRate;
	}
	
	public final boolean isHandler()
	{
		return _isItemHandler;
	}
	
	public final boolean isMagic()
	{
		return _magicType == SkillMagicType.MAGIC;
	}
	
	public final SkillMagicType getMagicType()
	{
		return _magicType;
	}
	
	public void setMagicType(SkillMagicType type)
	{
		_magicType = type;
	}
	
	public final boolean isNewbie()
	{
		return _isNewbie;
	}
	
	public final boolean isPreservedOnDeath()
	{
		return _isPreservedOnDeath;
	}
	
	public final boolean isHeroic()
	{
		return _isHeroic;
	}
	
	public final boolean isSelfDispellable()
	{
		return _isSelfDispellable;
	}
	
	public void setOperateType(SkillOpType type)
	{
		_operateType = type;
	}
	
	public final boolean isOverhit()
	{
		return _isOverhit;
	}
	
	public void setOverhit(boolean isOverhit)
	{
		_isOverhit = isOverhit;
	}
	
	public final boolean isActive()
	{
		return _operateType == SkillOpType.OP_ACTIVE;
	}
	
	public final boolean isPassive()
	{
		return _operateType == SkillOpType.OP_PASSIVE;
	}
	
	public boolean isSaveable()
	{
		if(!Config.ALT_SAVE_UNSAVEABLE && (isMusic() || _name.startsWith("Herb of")))
		{
			return false;
		}
		return _isSaveable;
	}
	
	public final boolean isSkillTimePermanent()
	{
		return _isSkillTimePermanent || _isItemHandler || _name.contains("Talisman");
	}
	
	public final boolean isReuseDelayPermanent()
	{
		return _isReuseDelayPermanent || _isItemHandler;
	}
	
	public boolean isDeathlink()
	{
		return _deathlink;
	}
	
	public boolean isBasedOnTargetDebuff()
	{
		return _basedOnTargetDebuff;
	}
	
	public boolean isSoulBoost()
	{
		return _isSoulBoost;
	}
	
	public boolean isChargeBoost()
	{
		return _isChargeBoost;
	}
	
	public boolean isUsingWhileCasting()
	{
		return _isUsingWhileCasting;
	}
	
	public boolean isBehind()
	{
		return _isBehind;
	}
	
	public boolean isHideStartMessage()
	{
		return _hideStartMessage;
	}
	
	public boolean isHideUseMessage()
	{
		return _hideUseMessage;
	}
	
	public boolean isSSPossible()
	{
		return _isUseSS == Ternary.TRUE || _isUseSS == Ternary.DEFAULT && !_isItemHandler && !isMusic() && isActive() && (getTargetType() != SkillTargetType.TARGET_SELF || isMagic());
	}
	
	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}
	
	public final boolean isToggle()
	{
		return _operateType == SkillOpType.OP_TOGGLE;
	}
	
	public boolean isItemSkill()
	{
		return _name.contains("Item Skill") || _name.contains("Talisman");
	}
	
	@Override
	public String toString()
	{
		return _name + "[id=" + _id + ",lvl=" + _level + "]";
	}
	
	public abstract void useSkill(Creature activeChar, List<Creature> targets);
	
	public boolean isAoE()
	{
		switch(_targetType)
		{
			case TARGET_AURA:
			case TARGET_MULTIFACE_AURA:
			case TARGET_PET_AURA:
			case TARGET_AREA:
			case TARGET_MULTIFACE:
			case TARGET_TUNNEL:
			case TARGET_AREA_AIM_CORPSE:
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isNotTargetAoE()
	{
		switch(_targetType)
		{
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_PARTY:
			case TARGET_CLAN_ONLY:
			case TARGET_AURA:
			case TARGET_MULTIFACE_AURA:
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isOffensive()
	{
		return _isOffensive;
	}
	
	public final boolean isForceUse()
	{
		return _isForceUse;
	}
	
	public boolean isAI()
	{
		return _skillType.isAI();
	}
	
	public boolean isPvM()
	{
		return _isPvm;
	}
	
	public final boolean isPvpSkill()
	{
		return _isPvpSkill;
	}
	
	public final boolean isFishingSkill()
	{
		return _isFishingSkill;
	}
	
	public boolean isMusic()
	{
		return _magicType == SkillMagicType.MUSIC;
	}
	
	public boolean isTrigger()
	{
		return _isTrigger;
	}
	
	public boolean isSlotNone()
	{
		return _isSlotNone;
	}
	
	public boolean oneTarget()
	{
		switch(_targetType)
		{
			case TARGET_SELF:
			case TARGET_HOLY:
			case TARGET_FLAGPOLE:
			case TARGET_UNLOCKABLE:
			case TARGET_CHEST:
			case TARGET_FEEDABLE_BEAST:
			case TARGET_PET:
			case TARGET_OWNER:
			case TARGET_ENEMY_PET:
			case TARGET_ENEMY_SUMMON:
			case TARGET_ENEMY_SERVITOR:
			case TARGET_ONE:
			case TARGET_OTHER:
			case TARGET_CORPSE:
			case TARGET_CORPSE_PLAYER:
			case TARGET_SIEGE:
			case TARGET_ITEM:
			case TARGET_NONE:
			{
				return true;
			}
		}
		return false;
	}
	
	public int getCancelTarget()
	{
		return _cancelTarget;
	}
	
	public boolean isSkillInterrupt()
	{
		return _skillInterrupt;
	}
	
	public boolean isNotUsedByAI()
	{
		return _isNotUsedByAI;
	}
	
	public boolean isIgnoreResists()
	{
		return _isIgnoreResists;
	}
	
	public boolean isIgnoreInvul()
	{
		return _isIgnoreInvul;
	}
	
	public boolean isSharedClassReuse()
	{
		return _isSharedClassReuse;
	}
	
	public boolean isNotAffectedByMute()
	{
		return _isNotAffectedByMute;
	}
	
	public boolean flyingTransformUsage()
	{
		return _flyingTransformUsage;
	}
	
	public boolean canUseTeleport()
	{
		return _canUseTeleport;
	}
	
	public int getEnchantLevelCount()
	{
		return _enchantLevelCount;
	}
	
	public void setEnchantLevelCount(int count)
	{
		_enchantLevelCount = count;
	}
	
	public boolean isClanSkill()
	{
		return _id >= 370 && _id <= 391 || _id >= 611 && _id <= 616;
	}
	
	public boolean isBaseTransformation()
	{
		return _id >= 810 && _id <= 813 || _id >= 1520 && _id <= 1522 || _id == 538;
	}
	
	public boolean isSummonerTransformation()
	{
		return _id >= 929 && _id <= 931;
	}
	
	public double getSimpleDamage(Creature attacker, Creature target)
	{
		if(isMagic())
		{
			double mAtk = attacker.getMAtk(target, this);
			double mdef = target.getMDef(null, this);
			double power = getPower();
			int sps = attacker.getChargedSpiritShot() > 0 && isSSPossible() ? attacker.getChargedSpiritShot() * 2 : 1;
			return 91.0 * power * Math.sqrt((double) sps * mAtk) / mdef;
		}
		double pAtk = attacker.getPAtk(target);
		double pdef = target.getPDef(attacker);
		double power = getPower();
		int ss = attacker.getChargedSoulShot() && isSSPossible() ? 2 : 1;
		return (double) ss * (pAtk + power) * 70.0 / pdef;
	}
	
	public long getReuseForMonsters()
	{
		long min = 1000;
		switch(_skillType)
		{
			case DEBUFF:
			case PARALYZE:
			case NEGATE_STATS:
			case NEGATE_EFFECTS:
			case STEAL_BUFF:
			{
				min = 10000;
				break;
			}
			case MUTE:
			case ROOT:
			case SLEEP:
			case STUN:
			{
				min = 5000;
			}
		}
		return Math.max(Math.max((long) (_hitTime + _coolTime), _reuseDelay), min);
	}
	
	public double getAbsorbPart()
	{
		return _absorbPart;
	}
	
	public boolean isProvoke()
	{
		return _isProvoke;
	}
	
	public String getIcon()
	{
		return _icon;
	}
	
	public int getEnergyConsume()
	{
		return _energyConsume;
	}
	
	public boolean isCubicSkill()
	{
		return _isCubicSkill;
	}
	
	public void setCubicSkill(boolean value)
	{
		_isCubicSkill = value;
	}
	
	public boolean isBlowSkill()
	{
		return false;
	}
	
	public enum SkillType
	{
		AGGRESSION(Aggression.class),
		AIEFFECTS(AIeffects.class),
		BALANCE(Balance.class),
		BEAST_FEED(BeastFeed.class),
		BLEED(Continuous.class),
		BUFF(Continuous.class),
		BUFF_CHARGER(BuffCharger.class),
		CALL(Call.class),
		CLAN_GATE(ClanGate.class),
		COMBATPOINTHEAL(CombatPointHeal.class),
		CONT(Toggle.class),
		CPDAM(CPDam.class),
		CPHOT(Continuous.class),
		CRAFT(Craft.class),
		DEATH_PENALTY(DeathPenalty.class),
		DEBUFF(Continuous.class),
		DELETE_HATE(DeleteHate.class),
		DELETE_HATE_OF_ME(DeleteHateOfMe.class),
		DESTROY_SUMMON(DestroySummon.class),
		DEFUSE_TRAP(DefuseTrap.class),
		DETECT_TRAP(DetectTrap.class),
		DISCORD(Continuous.class),
		DOT(Continuous.class),
		DRAIN(Drain.class),
		DRAIN_SOUL(DrainSoul.class),
		EFFECT(l2.gameserver.skills.skillclasses.Effect.class),
		EFFECTS_FROM_SKILLS(EffectsFromSkills.class),
		ENCHANT_ARMOR,
		ENCHANT_WEAPON,
		FEED_PET,
		FISHING(FishingSkill.class),
		HARDCODED(l2.gameserver.skills.skillclasses.Effect.class),
		HARVESTING(Harvesting.class),
		HEAL(Heal.class),
		HEAL_PERCENT(HealPercent.class),
		HOT(Continuous.class),
		LETHAL_SHOT(LethalShot.class),
		LUCK,
		MANADAM(ManaDam.class),
		MANAHEAL(ManaHeal.class),
		MANAHEAL_PERCENT(ManaHealPercent.class),
		MDAM(MDam.class),
		MDOT(Continuous.class),
		MPHOT(Continuous.class),
		MUTE(Disablers.class),
		NEGATE_EFFECTS(NegateEffects.class),
		NEGATE_STATS(NegateStats.class),
		ADD_PC_BANG(PcBangPointsAdd.class),
		NOTDONE,
		NOTUSED,
		PARALYZE(Disablers.class),
		PASSIVE,
		PDAM(PDam.class),
		PET_SUMMON(PetSummon.class),
		POISON(Continuous.class),
		PUMPING(ReelingPumping.class),
		RECALL(Recall.class),
		REELING(ReelingPumping.class),
		RESURRECT(Resurrect.class),
		RIDE(Ride.class),
		ROOT(Disablers.class),
		SHIFT_AGGRESSION(ShiftAggression.class),
		SSEED(SkillSeed.class),
		SLEEP(Disablers.class),
		SOULSHOT,
		SOWING(Sowing.class),
		SPHEAL(SPHeal.class),
		SPIRITSHOT,
		SPOIL(Spoil.class),
		STEAL_BUFF(StealBuff.class),
		STUN(Disablers.class),
		SUMMON(l2.gameserver.skills.skillclasses.Summon.class),
		SUMMON_FLAG(SummonSiegeFlag.class),
		SUMMON_ITEM(SummonItem.class),
		SWEEP(Sweep.class),
		TAKECASTLE(TakeCastle.class),
		TAMECONTROL(TameControl.class),
		TELEPORT_NPC(TeleportNpc.class),
		TRANSFORMATION(Transformation.class),
		UNLOCK(Unlock.class),
		WATCHER_GAZE(Continuous.class);
		
		private final Class<? extends Skill> clazz;
		
		SkillType()
		{
			clazz = Default.class;
		}
		
		SkillType(Class<? extends Skill> clazz)
		{
			this.clazz = clazz;
		}
		
		public Skill makeSkill(StatsSet set)
		{
			try
			{
				Constructor<? extends Skill> c = clazz.getConstructor(StatsSet.class);
				return c.newInstance(set);
			}
			catch(Exception e)
			{
				_log.error("", e);
				throw new RuntimeException(e);
			}
		}
		
		public final boolean isPvM()
		{
			switch(this)
			{
				case DISCORD:
				{
					return true;
				}
			}
			return false;
		}
		
		public boolean isAI()
		{
			switch(this)
			{
				case AGGRESSION:
				case AIEFFECTS:
				case SOWING:
				case DELETE_HATE:
				case DELETE_HATE_OF_ME:
				{
					return true;
				}
			}
			return false;
		}
		
		public final boolean isPvpSkill()
		{
			switch(this)
			{
				case AGGRESSION:
				case DELETE_HATE:
				case DELETE_HATE_OF_ME:
				case BLEED:
				case DEBUFF:
				case DOT:
				case MDOT:
				case MUTE:
				case PARALYZE:
				case POISON:
				case ROOT:
				case SLEEP:
				case MANADAM:
				case DESTROY_SUMMON:
				case NEGATE_STATS:
				case NEGATE_EFFECTS:
				case STEAL_BUFF:
				{
					return true;
				}
			}
			return false;
		}
		
		public boolean isOffensive()
		{
			switch(this)
			{
				case DISCORD:
				case AGGRESSION:
				case AIEFFECTS:
				case SOWING:
				case DELETE_HATE:
				case DELETE_HATE_OF_ME:
				case BLEED:
				case DEBUFF:
				case DOT:
				case MDOT:
				case MUTE:
				case PARALYZE:
				case POISON:
				case ROOT:
				case SLEEP:
				case MANADAM:
				case DESTROY_SUMMON:
				case STEAL_BUFF:
				case DRAIN:
				case DRAIN_SOUL:
				case LETHAL_SHOT:
				case MDAM:
				case PDAM:
				case CPDAM:
				case SOULSHOT:
				case SPIRITSHOT:
				case SPOIL:
				case STUN:
				case SWEEP:
				case HARVESTING:
				case TELEPORT_NPC:
				{
					return true;
				}
			}
			return false;
		}
	}
	
	public enum SkillTargetType
	{
		TARGET_ALLY,
		TARGET_AREA,
		TARGET_AREA_AIM_CORPSE,
		TARGET_AURA,
		TARGET_PET_AURA,
		TARGET_CHEST,
		TARGET_FEEDABLE_BEAST,
		TARGET_CLAN,
		TARGET_CLAN_ONLY,
		TARGET_CORPSE,
		TARGET_CORPSE_PLAYER,
		TARGET_ENEMY_PET,
		TARGET_ENEMY_SUMMON,
		TARGET_ENEMY_SERVITOR,
		TARGET_FLAGPOLE,
		TARGET_COMMCHANNEL,
		TARGET_HOLY,
		TARGET_ITEM,
		TARGET_MULTIFACE,
		TARGET_MULTIFACE_AURA,
		TARGET_TUNNEL,
		TARGET_NONE,
		TARGET_ONE,
		TARGET_OTHER,
		TARGET_OWNER,
		TARGET_PARTY,
		TARGET_PET,
		TARGET_SELF,
		TARGET_SIEGE,
		TARGET_UNLOCKABLE;
	}
	
	public enum SkillMagicType
	{
		PHYSIC,
		MAGIC,
		SPECIAL,
		MUSIC;
	}
	
	public enum Ternary
	{
		TRUE,
		FALSE,
		DEFAULT;
	}
	
	public enum SkillOpType
	{
		OP_ACTIVE,
		OP_PASSIVE,
		OP_TOGGLE;
	}
	
	public enum SkillNextAction
	{
		ATTACK,
		CAST,
		DEFAULT,
		MOVE,
		NONE;
	}
	
	public static class AddedSkill
	{
		public static final AddedSkill[] EMPTY_ARRAY = new AddedSkill[0];
		public int id;
		public int level;
		private Skill _skill;
		
		public AddedSkill(int id, int level)
		{
			this.id = id;
			this.level = level;
		}
		
		public Skill getSkill()
		{
			if(_skill == null)
			{
				_skill = SkillTable.getInstance().getInfo(id, level);
			}
			return _skill;
		}
	}
}