package l2.gameserver.templates.npc;

import gnu.trove.TIntObjectHashMap;
import l2.commons.util.TroveUtils;
import l2.gameserver.ai.CharacterAI;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.model.Skill;
import l2.gameserver.model.TeleportLocation;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.RaidBossInstance;
import l2.gameserver.model.instances.ReflectionBossInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestEventType;
import l2.gameserver.model.reward.RewardList;
import l2.gameserver.model.reward.RewardType;
import l2.gameserver.scripts.Scripts;
import l2.gameserver.skills.effects.EffectTemplate;
import l2.gameserver.templates.CharTemplate;
import l2.gameserver.templates.StatsSet;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NpcTemplate extends CharTemplate
{
	public static final Constructor<NpcInstance> DEFAULT_TYPE_CONSTRUCTOR = (Constructor<NpcInstance>) NpcInstance.class.getConstructors()[0];
	public static final Constructor<CharacterAI> DEFAULT_AI_CONSTRUCTOR = (Constructor<CharacterAI>) CharacterAI.class.getConstructors()[0];
	private static final Logger _log = LoggerFactory.getLogger(NpcTemplate.class);
	public final int npcId;
	public final String name;
	public final String title;
	public final int level;
	public final long rewardExp;
	public final int rewardSp;
	public final int rewardRp;
	public final int aggroRange;
	public final int rhand;
	public final int lhand;
	public final double rateHp;
	public final String jClass;
	public final int displayId;
	public final ShotsType shots;
	private final StatsSet _AIParams;
	private final int _castleId;
	private final String _htmRoot;
	public boolean isRaid;
	private Faction faction = Faction.NONE;
	private int race;
	private Map<RewardType, RewardList> _rewards = Collections.emptyMap();
	private TIntObjectHashMap<TeleportLocation[]> _teleportList = TroveUtils.emptyIntObjectMap();
	private List<MinionData> _minions = Collections.emptyList();
	private List<AbsorbInfo> _absorbInfo = Collections.emptyList();
	private List<ClassId> _teachInfo = Collections.emptyList();
	private Map<QuestEventType, Quest[]> _questEvents = Collections.emptyMap();
	private TIntObjectHashMap<Skill> _skills = TroveUtils.emptyIntObjectMap();
	private Skill[] _damageSkills = Skill.EMPTY_ARRAY;
	private Skill[] _dotSkills = Skill.EMPTY_ARRAY;
	private Skill[] _debuffSkills = Skill.EMPTY_ARRAY;
	private Skill[] _buffSkills = Skill.EMPTY_ARRAY;
	private Skill[] _stunSkills = Skill.EMPTY_ARRAY;
	private Skill[] _healSkills = Skill.EMPTY_ARRAY;
	private Class<NpcInstance> _classType = NpcInstance.class;
	private Constructor<NpcInstance> _constructorType = DEFAULT_TYPE_CONSTRUCTOR;
	private Class<CharacterAI> _classAI = CharacterAI.class;
	private Constructor<CharacterAI> _constructorAI = DEFAULT_AI_CONSTRUCTOR;
	
	public NpcTemplate(StatsSet set)
	{
		super(set);
		npcId = set.getInteger("npcId");
		displayId = set.getInteger("displayId");
		name = set.getString("name");
		title = set.getString("title");
		level = set.getInteger("level");
		rewardExp = set.getLong("rewardExp");
		rewardSp = set.getInteger("rewardSp");
		rewardRp = set.getInteger("rewardRp");
		aggroRange = set.getInteger("aggroRange");
		rhand = set.getInteger("rhand", 0);
		lhand = set.getInteger("lhand", 0);
		rateHp = set.getDouble("baseHpRate");
		jClass = set.getString("texture", null);
		_htmRoot = set.getString("htm_root", null);
		shots = set.getEnum("shots", ShotsType.class, ShotsType.NONE);
		_castleId = set.getInteger("castle_id", 0);
		_AIParams = (StatsSet) set.getObject("aiParams", StatsSet.EMPTY);
		setType(set.getString("type", null));
		setAI(set.getString("ai_type", null));
	}
	
	public Class<? extends NpcInstance> getInstanceClass()
	{
		return _classType;
	}
	
	public Constructor<? extends NpcInstance> getInstanceConstructor()
	{
		return _constructorType;
	}
	
	public boolean isInstanceOf(Class<?> _class)
	{
		return _class.isAssignableFrom(_classType);
	}
	
	public NpcInstance getNewInstance()
	{
		try
		{
			return _constructorType.newInstance(IdFactory.getInstance().getNextId(), this);
		}
		catch(Exception e)
		{
			_log.error("Unable to create instance of NPC " + npcId, e);
			return null;
		}
	}
	
	public CharacterAI getNewAI(NpcInstance npc)
	{
		try
		{
			return _constructorAI.newInstance(npc);
		}
		catch(Exception e)
		{
			_log.error("Unable to create ai of NPC " + npcId, e);
			return new CharacterAI(npc);
		}
	}
	
	private void setType(String type)
	{
		Class classType;
		try
		{
			classType = Class.forName("l2.gameserver.model.instances." + type + "Instance");
		}
		catch(ClassNotFoundException e)
		{
			classType = Scripts.getInstance().getClasses().get("npc.model." + type + "Instance");
		}
		if(classType == null)
		{
			_log.error("Not found type class for type: " + type + ". NpcId: " + npcId);
		}
		else
		{
			_classType = classType;
			_constructorType = (Constructor<NpcInstance>) _classType.getConstructors()[0];
		}
		if(_classType.isAnnotationPresent(Deprecated.class))
		{
			_log.error("Npc type: " + type + ", is deprecated. NpcId: " + npcId);
		}
		isRaid = isInstanceOf(RaidBossInstance.class) && !isInstanceOf(ReflectionBossInstance.class);
	}
	
	private void setAI(String ai)
	{
		Class classAI;
		try
		{
			classAI = Class.forName("l2.gameserver.ai." + ai);
		}
		catch(ClassNotFoundException e)
		{
			classAI = Scripts.getInstance().getClasses().get("ai." + ai);
		}
		if(classAI == null)
		{
			_log.error("Not found ai class for ai: " + ai + ". NpcId: " + npcId);
		}
		else
		{
			_classAI = classAI;
			_constructorAI = (Constructor<CharacterAI>) _classAI.getConstructors()[0];
		}
		if(_classAI.isAnnotationPresent(Deprecated.class))
		{
			_log.error("Ai type: " + ai + ", is deprecated. NpcId: " + npcId);
		}
	}
	
	public void addTeachInfo(ClassId classId)
	{
		if(_teachInfo.isEmpty())
		{
			_teachInfo = new ArrayList<>(1);
		}
		_teachInfo.add(classId);
	}
	
	public List<ClassId> getTeachInfo()
	{
		return _teachInfo;
	}
	
	public boolean canTeach(ClassId classId)
	{
		return _teachInfo.contains(classId);
	}
	
	public void addTeleportList(int id, TeleportLocation[] list)
	{
		if(_teleportList.isEmpty())
		{
			_teleportList = new TIntObjectHashMap(1);
		}
		_teleportList.put(id, list);
	}
	
	public TeleportLocation[] getTeleportList(int id)
	{
		return _teleportList.get(id);
	}
	
	public TIntObjectHashMap<TeleportLocation[]> getTeleportList()
	{
		return _teleportList;
	}
	
	public void putRewardList(RewardType rewardType, RewardList list)
	{
		if(_rewards.isEmpty())
		{
			_rewards = new HashMap<>(RewardType.values().length);
		}
		_rewards.put(rewardType, list);
	}
	
	public RewardList getRewardList(RewardType t)
	{
		return _rewards.get(t);
	}
	
	public Map<RewardType, RewardList> getRewards()
	{
		return _rewards;
	}
	
	public void addAbsorbInfo(AbsorbInfo absorbInfo)
	{
		if(_absorbInfo.isEmpty())
		{
			_absorbInfo = new ArrayList<>(1);
		}
		_absorbInfo.add(absorbInfo);
	}
	
	public void addMinion(MinionData minion)
	{
		if(_minions.isEmpty())
		{
			_minions = new ArrayList<>(1);
		}
		_minions.add(minion);
	}
	
	public Faction getFaction()
	{
		return faction;
	}
	
	public void setFaction(Faction faction)
	{
		this.faction = faction;
	}
	
	public void addSkill(Skill skill)
	{
		if(_skills.isEmpty())
		{
			_skills = new TIntObjectHashMap();
		}
		_skills.put(skill.getId(), skill);
		if(skill.isNotUsedByAI() || skill.getTargetType() == Skill.SkillTargetType.TARGET_NONE || skill.getSkillType() == Skill.SkillType.NOTDONE || !skill.isActive())
		{
			return;
		}
		switch(skill.getSkillType())
		{
			case PDAM:
			case MANADAM:
			case MDAM:
			case DRAIN:
			case DRAIN_SOUL:
			{
				boolean added = false;
				if(skill.hasEffects())
				{
					block12:
					for(EffectTemplate eff : skill.getEffectTemplates())
					{
						switch(eff.getEffectType())
						{
							case Stun:
							{
								_stunSkills = ArrayUtils.add(_stunSkills, skill);
								added = true;
								continue block12;
							}
							case DamOverTime:
							case DamOverTimeLethal:
							case ManaDamOverTime:
							case LDManaDamOverTime:
							{
								_dotSkills = ArrayUtils.add(_dotSkills, skill);
								added = true;
							}
						}
					}
				}
				if(added)
					break;
				_damageSkills = ArrayUtils.add(_damageSkills, skill);
				break;
			}
			case DOT:
			case MDOT:
			case POISON:
			case BLEED:
			{
				_dotSkills = ArrayUtils.add(_dotSkills, skill);
				break;
			}
			case DEBUFF:
			case SLEEP:
			case ROOT:
			case PARALYZE:
			case MUTE:
			case TELEPORT_NPC:
			case AGGRESSION:
			{
				_debuffSkills = ArrayUtils.add(_debuffSkills, skill);
				break;
			}
			case BUFF:
			{
				_buffSkills = ArrayUtils.add(_buffSkills, skill);
				break;
			}
			case STUN:
			{
				_stunSkills = ArrayUtils.add(_stunSkills, skill);
				break;
			}
			case HEAL:
			case HEAL_PERCENT:
			case HOT:
			{
				_healSkills = ArrayUtils.add(_healSkills, skill);
				break;
			}
		}
	}
	
	public Skill[] getDamageSkills()
	{
		return _damageSkills;
	}
	
	public Skill[] getDotSkills()
	{
		return _dotSkills;
	}
	
	public Skill[] getDebuffSkills()
	{
		return _debuffSkills;
	}
	
	public Skill[] getBuffSkills()
	{
		return _buffSkills;
	}
	
	public Skill[] getStunSkills()
	{
		return _stunSkills;
	}
	
	public Skill[] getHealSkills()
	{
		return _healSkills;
	}
	
	public List<MinionData> getMinionData()
	{
		return _minions;
	}
	
	public TIntObjectHashMap<Skill> getSkills()
	{
		return _skills;
	}
	
	public void addQuestEvent(QuestEventType EventType2, Quest q)
	{
		if(_questEvents.isEmpty())
		{
			_questEvents = new HashMap<>();
		}
		if(_questEvents.get(EventType2) == null)
		{
			_questEvents.put(EventType2, new Quest[] {q});
		}
		else
		{
			Quest[] _quests = _questEvents.get(EventType2);
			int len = _quests.length;
			Quest[] tmp = new Quest[len + 1];
			for(int i = 0;i < len;++i)
			{
				if(_quests[i].getName().equals(q.getName()))
				{
					_quests[i] = q;
					return;
				}
				tmp[i] = _quests[i];
			}
			tmp[len] = q;
			_questEvents.put(EventType2, tmp);
		}
	}
	
	public Quest[] getEventQuests(QuestEventType EventType2)
	{
		return _questEvents.get(EventType2);
	}
	
	public int getRace()
	{
		return race;
	}
	
	public void setRace(int newrace)
	{
		race = newrace;
	}
	
	public boolean isUndead()
	{
		return race == 1;
	}
	
	@Override
	public String toString()
	{
		return "Npc template " + name + "[" + npcId + "]";
	}
	
	@Override
	public int getNpcId()
	{
		return npcId;
	}
	
	public String getName()
	{
		return name;
	}
	
	public final String getJClass()
	{
		return jClass;
	}
	
	public final StatsSet getAIParams()
	{
		return _AIParams;
	}
	
	public List<AbsorbInfo> getAbsorbInfo()
	{
		return _absorbInfo;
	}
	
	public int getCastleId()
	{
		return _castleId;
	}
	
	public Map<QuestEventType, Quest[]> getQuestEvents()
	{
		return _questEvents;
	}
	
	public String getHtmRoot()
	{
		return _htmRoot;
	}
	
	public enum ShotsType
	{
		NONE,
		SOUL,
		SPIRIT,
		BSPIRIT,
		SOUL_SPIRIT,
		SOUL_BSPIRIT;
	}
}