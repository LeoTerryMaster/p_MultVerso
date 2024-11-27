package l2.gameserver.ai;

import l2.commons.collections.CollectionUtils;
import l2.commons.collections.LazyArrayList;
import l2.commons.lang.reference.HardReference;
import l2.commons.math.random.RndSelector;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.model.AggroList;
import l2.gameserver.model.Creature;
import l2.gameserver.model.MinionList;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.World;
import l2.gameserver.model.WorldRegion;
import l2.gameserver.model.Zone;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.model.instances.MinionInstance;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.QuestEventType;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.stats.Stats;
import l2.gameserver.taskmanager.AiTaskManager;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.NpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledFuture;

public class DefaultAI extends CharacterAI
{
	public static final int TaskDefaultWeight = 10000;
	protected static final Logger _log = LoggerFactory.getLogger(DefaultAI.class);
	protected final NavigableSet<Task> _tasks;
	protected final Skill[] _damSkills;
	protected final Skill[] _dotSkills;
	protected final Skill[] _debuffSkills;
	protected final Skill[] _healSkills;
	protected final Skill[] _buffSkills;
	protected final Skill[] _stunSkills;
	protected final Comparator<Creature> _nearestTargetComparator;
	protected long AI_TASK_ATTACK_DELAY = Config.AI_TASK_ATTACK_DELAY;
	protected long AI_TASK_ACTIVE_DELAY;
	protected long AI_TASK_DELAY_CURRENT;
	protected int MAX_PURSUE_RANGE;
	protected ScheduledFuture<?> _aiTask;
	protected ScheduledFuture<?> _runningTask;
	protected ScheduledFuture<?> _madnessTask;
	protected boolean _def_think;
	protected long _globalAggro;
	protected long _randomAnimationEnd;
	protected int _pathfindFails;
	protected long _lastActiveCheck;
	protected long _checkAggroTimestamp;
	protected long _attackTimeout;
	protected long _lastFactionNotifyTime;
	protected long _minFactionNotifyInterval;
	private boolean _thinking;
	
	public DefaultAI(NpcInstance actor)
	{
		super(actor);
		AI_TASK_DELAY_CURRENT = AI_TASK_ACTIVE_DELAY = Config.AI_TASK_ACTIVE_DELAY;
		_thinking = false;
		_def_think = false;
		_tasks = new ConcurrentSkipListSet<>(TaskComparator.getInstance());
		_checkAggroTimestamp = 0;
		_lastFactionNotifyTime = 0;
		_minFactionNotifyInterval = 3000;
		setAttackTimeout(Long.MAX_VALUE);
		NpcInstance npc = getActor();
		_damSkills = npc.getTemplate().getDamageSkills();
		_dotSkills = npc.getTemplate().getDotSkills();
		_debuffSkills = npc.getTemplate().getDebuffSkills();
		_buffSkills = npc.getTemplate().getBuffSkills();
		_stunSkills = npc.getTemplate().getStunSkills();
		_healSkills = npc.getTemplate().getHealSkills();
		_nearestTargetComparator = new NearestTargetComparator(actor);
		MAX_PURSUE_RANGE = actor.getParameter("MaxPursueRange", actor.isRaid() ? Config.MAX_PURSUE_RANGE_RAID : npc.isUnderground() ? Config.MAX_PURSUE_UNDERGROUND_RANGE : Config.MAX_PURSUE_RANGE);
		_minFactionNotifyInterval = actor.getParameter("FactionNotifyInterval", 3000);
	}
	
	protected static Skill selectTopSkillByDamage(Creature actor, Creature target, double distance, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
		{
			return null;
		}
		if(skills.length == 1)
		{
			return skills[0];
		}
		RndSelector<Skill> rnd = new RndSelector<>(skills.length);
		for(Skill skill : skills)
		{
			double weight = skill.getSimpleDamage(actor, target) * (double) skill.getAOECastRange() / distance;
			if(weight < 1.0)
			{
				weight = 1.0;
			}
			rnd.add(skill, (int) weight);
		}
		return rnd.select();
	}
	
	protected static Skill selectTopSkillByDebuff(Creature actor, Creature target, double distance, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
		{
			return null;
		}
		if(skills.length == 1)
		{
			return skills[0];
		}
		RndSelector<Skill> rnd = new RndSelector<>(skills.length);
		for(Skill skill : skills)
		{
			if(skill.getSameByStackType(target) != null)
				continue;
			double weight = 100.0 * (double) skill.getAOECastRange() / distance;
			if(weight <= 0.0)
			{
				weight = 1.0;
			}
			rnd.add(skill, (int) weight);
		}
		return rnd.select();
	}
	
	protected static Skill selectTopSkillByBuff(Creature target, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
		{
			return null;
		}
		if(skills.length == 1)
		{
			return skills[0];
		}
		RndSelector<Skill> rnd = new RndSelector<>(skills.length);
		for(Skill skill : skills)
		{
			if(skill.getSameByStackType(target) != null)
				continue;
			double weight = skill.getPower();
			if(weight <= 0.0)
			{
				weight = 1.0;
			}
			rnd.add(skill, (int) weight);
		}
		return rnd.select();
	}
	
	protected static Skill selectTopSkillByHeal(Creature target, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
		{
			return null;
		}
		double hpReduced = (double) target.getMaxHp() - target.getCurrentHp();
		if(hpReduced < 1.0)
		{
			return null;
		}
		if(skills.length == 1)
		{
			return skills[0];
		}
		RndSelector<Skill> rnd = new RndSelector<>(skills.length);
		for(Skill skill : skills)
		{
			double weight = Math.abs(skill.getPower() - hpReduced);
			if(weight <= 0.0)
			{
				weight = 1.0;
			}
			rnd.add(skill, (int) weight);
		}
		return rnd.select();
	}
	
	public void addTaskCast(Creature target, Skill skill)
	{
		Task task = new Task();
		task.type = TaskType.CAST;
		task.target = target.getRef();
		task.skill = skill;
		_tasks.add(task);
		_def_think = true;
	}
	
	public void addTaskBuff(Creature target, Skill skill)
	{
		Task task = new Task();
		task.type = TaskType.BUFF;
		task.target = target.getRef();
		task.skill = skill;
		_tasks.add(task);
		_def_think = true;
	}
	
	public void addTaskAttack(Creature target)
	{
		Task task = new Task();
		task.type = TaskType.ATTACK;
		task.target = target.getRef();
		_tasks.add(task);
		_def_think = true;
	}
	
	public void addTaskAttack(Creature target, Skill skill, int weight)
	{
		Task task = new Task();
		task.type = skill.isOffensive() ? TaskType.CAST : TaskType.BUFF;
		task.target = target.getRef();
		task.skill = skill;
		task.weight = weight;
		_tasks.add(task);
		_def_think = true;
	}
	
	public void addTaskMove(Location loc, boolean pathfind)
	{
		Task task = new Task();
		task.type = TaskType.MOVE;
		task.loc = loc;
		task.pathfind = pathfind;
		_tasks.add(task);
		_def_think = true;
	}
	
	protected void addTaskMove(int locX, int locY, int locZ, boolean pathfind)
	{
		addTaskMove(new Location(locX, locY, locZ), pathfind);
	}
	
	@Override
	public void runImpl() throws Exception
	{
		if(_aiTask == null)
		{
			return;
		}
		if(!isGlobalAI() && System.currentTimeMillis() - _lastActiveCheck > 60000)
		{
			_lastActiveCheck = System.currentTimeMillis();
			NpcInstance actor = getActor();
			WorldRegion region;
			WorldRegion worldRegion = region = actor == null ? null : actor.getCurrentRegion();
			if(region == null || !region.isActive())
			{
				stopAITask();
				return;
			}
		}
		onEvtThink();
	}
	
	@Override
	public synchronized void startAITask()
	{
		if(_aiTask == null)
		{
			AI_TASK_DELAY_CURRENT = AI_TASK_ACTIVE_DELAY;
			_aiTask = AiTaskManager.getInstance().scheduleAtFixedRate(this, 0, AI_TASK_DELAY_CURRENT);
		}
	}
	
	protected synchronized void switchAITask(long NEW_DELAY)
	{
		if(_aiTask == null)
		{
			return;
		}
		if(AI_TASK_DELAY_CURRENT != NEW_DELAY)
		{
			_aiTask.cancel(false);
			AI_TASK_DELAY_CURRENT = NEW_DELAY;
			_aiTask = AiTaskManager.getInstance().scheduleAtFixedRate(this, 0, AI_TASK_DELAY_CURRENT);
		}
	}
	
	@Override
	public final synchronized void stopAITask()
	{
		if(_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
		}
	}
	
	protected boolean canSeeInSilentMove(Playable target)
	{
		if(getActor().getParameter("canSeeInSilentMove", false))
		{
			return true;
		}
		return !target.isSilentMoving();
	}
	
	protected boolean canSeeInHide(Playable target)
	{
		if(getActor().getParameter("canSeeInHide", false))
		{
			return true;
		}
		return !target.isInvisible();
	}
	
	protected boolean checkAggression(Creature target)
	{
		NpcInstance actor = getActor();
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE || !isGlobalAggro())
		{
			return false;
		}
		if(target.isAlikeDead())
		{
			return false;
		}
		if(target.isNpc() && target.isInvul())
		{
			return false;
		}
		if(target.isPlayable())
		{
			if(!canSeeInSilentMove((Playable) target))
			{
				return false;
			}
			if(!canSeeInHide((Playable) target))
			{
				return false;
			}
			if(actor.getFaction().getName().equalsIgnoreCase("varka_silenos_clan") && target.getPlayer().getVarka() > 0)
			{
				return false;
			}
			if(actor.getFaction().getName().equalsIgnoreCase("ketra_orc_clan") && target.getPlayer().getKetra() > 0)
			{
				return false;
			}
			if(target.isPlayer() && ((Player) target).isGM() && target.isInvisible())
			{
				return false;
			}
			if(((Playable) target).getNonAggroTime() > System.currentTimeMillis())
			{
				return false;
			}
			if(target.isPlayer() && !target.getPlayer().isActive())
			{
				return false;
			}
			if(actor.isMonster() && target.isInZonePeace())
			{
				return false;
			}
		}
		if(!isInAggroRange(target))
		{
			return false;
		}
		if(!canAttackCharacter(target))
		{
			return false;
		}
		return GeoEngine.canSeeTarget(actor, target, false);
	}
	
	protected Location getPursueBaseLoc()
	{
		NpcInstance actor = getActor();
		Location spawnLoc = actor.getSpawnedLoc();
		return spawnLoc != null ? spawnLoc : actor.getLoc();
	}
	
	protected boolean isInAggroRange(Creature target)
	{
		NpcInstance actor = getActor();
		AggroList.AggroInfo ai = actor.getAggroList().get(target);
		return ai != null && ai.hate > 0 ? target.isInRangeZ(getPursueBaseLoc(), (long) MAX_PURSUE_RANGE) : isAggressive() && target.isInRangeZ(getPursueBaseLoc(), (long) actor.getAggroRange());
	}
	
	protected void setIsInRandomAnimation(long time)
	{
		_randomAnimationEnd = System.currentTimeMillis() + time;
	}
	
	protected boolean randomAnimation()
	{
		NpcInstance actor = getActor();
		if(actor.getParameter("noRandomAnimation", false))
		{
			return false;
		}
		if(actor.hasRandomAnimation() && !actor.isActionsDisabled() && !actor.isMoving() && !actor.isInCombat() && Rnd.chance(Config.RND_ANIMATION_RATE))
		{
			setIsInRandomAnimation(3000);
			actor.onRandomAnimation();
			return true;
		}
		return false;
	}
	
	protected Creature getNearestTarget(List<Creature> targets)
	{
		NpcInstance actor = getActor();
		Creature nextTarget = null;
		long minDist = Long.MAX_VALUE;
		for(int i = 0;i < targets.size();++i)
		{
			Creature target = targets.get(i);
			long dist = actor.getXYZDeltaSq(target.getX(), target.getY(), target.getZ());
			if(dist >= minDist)
				continue;
			nextTarget = target;
		}
		return nextTarget;
	}
	
	protected boolean randomWalk()
	{
		NpcInstance actor = getActor();
		if(actor.getParameter("noRandomWalk", false))
		{
			return false;
		}
		return !actor.isMoving() && maybeMoveToHome();
	}
	
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isActionsDisabled())
		{
			return true;
		}
		if(_randomAnimationEnd > System.currentTimeMillis())
		{
			return true;
		}
		if(_def_think)
		{
			if(doTask())
			{
				clearTasks();
			}
			return true;
		}
		long now = System.currentTimeMillis();
		if(now - _checkAggroTimestamp > (long) Config.AGGRO_CHECK_INTERVAL)
		{
			_checkAggroTimestamp = now;
			boolean aggressive = Rnd.chance(actor.getParameter("SelfAggressive", actor.isAggressive() ? 100 : 0));
			if(!actor.getAggroList().isEmpty() || aggressive)
			{
				List<Creature> chars = World.getAroundCharacters(actor);
				CollectionUtils.eqSort(chars, _nearestTargetComparator);
				for(Creature target : chars)
				{
					if(!aggressive && actor.getAggroList().get(target) == null || !checkAggression(target))
						continue;
					actor.getAggroList().addDamageHate(target, 0, 2);
					if(target.isSummon())
					{
						actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);
					}
					startRunningTask(AI_TASK_ATTACK_DELAY);
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					return true;
				}
			}
		}
		MonsterInstance leader;
		if(actor.isMinion() && (leader = ((MinionInstance) actor).getLeader()) != null)
		{
			double distance = actor.getDistance(leader.getX(), leader.getY());
			if(distance > 1000.0)
			{
				actor.teleToLocation(leader.getMinionPosition());
			}
			else if(distance > 200.0)
			{
				addTaskMove(leader.getMinionPosition(), false);
			}
			return true;
		}
		if(randomAnimation())
		{
			return true;
		}
		return randomWalk();
	}
	
	@Override
	protected void onIntentionIdle()
	{
		NpcInstance actor = getActor();
		clearTasks();
		actor.stopMove();
		actor.getAggroList().clear(true);
		setAttackTimeout(Long.MAX_VALUE);
		setAttackTarget(null);
		changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
	}
	
	@Override
	protected void onIntentionActive()
	{
		NpcInstance actor = getActor();
		actor.stopMove();
		setAttackTimeout(Long.MAX_VALUE);
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
		{
			switchAITask(AI_TASK_ACTIVE_DELAY);
			changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		}
		onEvtThink();
	}
	
	@Override
	protected void onIntentionAttack(Creature target)
	{
		NpcInstance actor = getActor();
		clearTasks();
		actor.stopMove();
		setAttackTarget(target);
		setAttackTimeout((long) getMaxAttackTimeout() + System.currentTimeMillis());
		setGlobalAggro(0);
		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
			switchAITask(AI_TASK_ATTACK_DELAY);
		}
		onEvtThink();
	}
	
	protected boolean canAttackCharacter(Creature target)
	{
		return target.isPlayable();
	}
	
	protected boolean isAggressive()
	{
		return getActor().isAggressive();
	}
	
	protected boolean checkTarget(Creature target, int range)
	{
		NpcInstance actor = getActor();
		if(target == null || target.isAlikeDead() || !actor.isInRangeZ(target, (long) range))
		{
			return false;
		}
		boolean hided;
		boolean bl = hided = target.isPlayable() && !canSeeInHide((Playable) target);
		if(!hided && actor.isConfused())
		{
			return true;
		}
		if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
		{
			AggroList.AggroInfo ai = actor.getAggroList().get(target);
			if(ai != null)
			{
				if(hided)
				{
					ai.hate = 0;
					return false;
				}
				return ai.hate > 0;
			}
			return false;
		}
		return canAttackCharacter(target);
	}
	
	protected long getAttackTimeout()
	{
		return _attackTimeout;
	}
	
	public void setAttackTimeout(long time)
	{
		_attackTimeout = time;
	}
	
	protected void thinkAttack()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
		{
			return;
		}
		Location loc = getPursueBaseLoc();
		if(!actor.isInRange(loc, (long) MAX_PURSUE_RANGE))
		{
			teleportHome();
			return;
		}
		if(doTask() && !actor.isAttackingNow() && !actor.isCastingNow() && !createNewTask() && System.currentTimeMillis() > getAttackTimeout())
		{
			returnHome();
		}
	}
	
	@Override
	protected void onEvtSpawn()
	{
		setGlobalAggro(System.currentTimeMillis() + getActor().getParameter("globalAggro", 10000));
		setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}
	
	@Override
	protected void onEvtReadyToAct()
	{
		onEvtThink();
	}
	
	@Override
	protected void onEvtArrivedTarget()
	{
		onEvtThink();
	}
	
	@Override
	protected void onEvtArrived()
	{
		onEvtThink();
	}
	
	protected boolean tryMoveToTarget(Creature target)
	{
		return tryMoveToTarget(target, getIndentRange(target.getActingRange()), target.getActingRange());
	}
	
	protected boolean tryMoveToTarget(Creature target, int indent, int range)
	{
		NpcInstance actor = getActor();
		if(range > 16)
		{
			if(!actor.moveToRelative(target, indent, range))
			{
				++_pathfindFails;
			}
		}
		else if(!actor.moveToLocation(target.getLoc(), indent, true))
		{
			++_pathfindFails;
		}
		if(_pathfindFails >= getMaxPathfindFails() && System.currentTimeMillis() > getAttackTimeout() - (long) getMaxAttackTimeout() + (long) getTeleportTimeout() && actor.isInRange(target, (long) MAX_PURSUE_RANGE))
		{
			_pathfindFails = 0;
			AggroList.AggroInfo hate;
			if(target.isPlayable() && ((hate = actor.getAggroList().get(target)) == null || hate.hate < 100 || actor.isRaid() && Math.abs(target.getZ() - actor.getZ()) > 900))
			{
				return false;
			}
			Location loc = GeoEngine.moveCheckForAI(target.getLoc(), actor.getLoc(), actor.getGeoIndex());
			if(!GeoEngine.canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), loc.x, loc.y, loc.z, actor.getGeoIndex()))
			{
				loc = target.getLoc();
			}
			actor.teleToLocation(loc);
		}
		return true;
	}
	
	protected boolean maybeNextTask(Task currentTask)
	{
		_tasks.remove(currentTask);
		return _tasks.size() == 0;
	}
	
	protected boolean doTask()
	{
		NpcInstance actor = getActor();
		if(!_def_think)
		{
			return true;
		}
		Task currentTask = _tasks.pollFirst();
		if(currentTask == null)
		{
			clearTasks();
			return true;
		}
		if(actor.isDead() || actor.isAttackingNow() || actor.isCastingNow())
		{
			return false;
		}
		switch(currentTask.type)
		{
			case MOVE:
			{
				if(actor.isMovementDisabled() || !getIsMobile())
				{
					return true;
				}
				if(actor.isInRange(currentTask.loc, 100))
				{
					return maybeNextTask(currentTask);
				}
				if(actor.isMoving())
				{
					return false;
				}
				if(actor.moveToLocation(currentTask.loc, 0, currentTask.pathfind))
					break;
				clientStopMoving();
				_pathfindFails = 0;
				actor.teleToLocation(currentTask.loc);
				return maybeNextTask(currentTask);
			}
			case ATTACK:
			{
				Creature target = currentTask.target.get();
				if(!checkTarget(target, MAX_PURSUE_RANGE))
				{
					return true;
				}
				setAttackTarget(target);
				if(actor.isMoving())
				{
					return Rnd.chance(25);
				}
				int pAtkRng = actor.getPhysicalAttackRange();
				int collisions = (int) (actor.getCollisionRadius() + target.getColRadius());
				boolean incZ = actor.isFlying() || actor.isInWater() || target.isFlying() || target.isInWater();
				int dist = (int) (!incZ ? actor.getDistance(target) : actor.getDistance3D(target)) - collisions;
				if(dist <= pAtkRng + 16 && GeoEngine.canSeeTarget(actor, target, incZ))
				{
					clientStopMoving();
					_pathfindFails = 0;
					setAttackTimeout((long) getMaxAttackTimeout() + System.currentTimeMillis());
					actor.doAttack(target);
					return maybeNextTask(currentTask);
				}
				if(actor.isMovementDisabled() || !getIsMobile())
				{
					return true;
				}
				return !tryMoveToTarget(target, collisions + getIndentRange(pAtkRng), collisions + pAtkRng);
			}
			case CAST:
			{
				Creature target = currentTask.target.get();
				if(actor.isMuted(currentTask.skill) || actor.isSkillDisabled(currentTask.skill) || actor.isUnActiveSkill(currentTask.skill.getId()))
				{
					return true;
				}
				boolean isAoE = currentTask.skill.getTargetType() == Skill.SkillTargetType.TARGET_AURA;
				int castRange = currentTask.skill.getAOECastRange();
				if(!checkTarget(target, MAX_PURSUE_RANGE + castRange))
				{
					return true;
				}
				setAttackTarget(target);
				int collisions = (int) (actor.getCollisionRadius() + target.getColRadius());
				boolean incZ = actor.isFlying() || actor.isInWater() || target.isFlying() || target.isInWater();
				int dist = (int) (!incZ ? actor.getDistance(target) : actor.getDistance3D(target)) - collisions;
				if(dist <= castRange && GeoEngine.canSeeTarget(actor, target, incZ))
				{
					clientStopMoving();
					_pathfindFails = 0;
					setAttackTimeout((long) getMaxAttackTimeout() + System.currentTimeMillis());
					actor.doCast(currentTask.skill, isAoE ? actor : target, !target.isPlayable());
					return maybeNextTask(currentTask);
				}
				if(actor.isMoving())
				{
					return Rnd.chance(10);
				}
				if(actor.isMovementDisabled() || !getIsMobile())
				{
					return true;
				}
				return !tryMoveToTarget(target, collisions + getIndentRange(castRange), collisions + castRange);
			}
			case BUFF:
			{
				Creature target = currentTask.target.get();
				if(actor.isMuted(currentTask.skill) || actor.isSkillDisabled(currentTask.skill) || actor.isUnActiveSkill(currentTask.skill.getId()))
				{
					return true;
				}
				if(target == null || target.isAlikeDead() || !actor.isInRange(target, 2000))
				{
					return true;
				}
				boolean isAoE = currentTask.skill.getTargetType() == Skill.SkillTargetType.TARGET_AURA;
				int castRange = currentTask.skill.getAOECastRange();
				if(actor.isMoving())
				{
					return Rnd.chance(10);
				}
				int collisions = (int) (actor.getCollisionRadius() + target.getColRadius());
				boolean incZ = actor.isFlying() || actor.isInWater() || target.isFlying() || target.isInWater();
				int dist = (int) (!incZ ? actor.getDistance(target) : actor.getDistance3D(target)) - collisions;
				if(dist <= castRange && GeoEngine.canSeeTarget(actor, target, incZ))
				{
					clientStopMoving();
					_pathfindFails = 0;
					actor.doCast(currentTask.skill, isAoE ? actor : target, !target.isPlayable());
					return maybeNextTask(currentTask);
				}
				if(actor.isMovementDisabled() || !getIsMobile())
				{
					return true;
				}
				return !tryMoveToTarget(target, collisions + getIndentRange(castRange), collisions + castRange);
			}
		}
		return false;
	}
	
	protected boolean createNewTask()
	{
		return false;
	}
	
	protected boolean defaultNewTask()
	{
		clearTasks();
		NpcInstance actor = getActor();
		Creature target;
		if(actor == null || (target = prepareTarget()) == null)
		{
			return false;
		}
		double distance = actor.getDistance(target);
		return chooseTaskAndTargets(null, target, distance);
	}
	
	@Override
	protected void onEvtThink()
	{
		NpcInstance actor = getActor();
		if(_thinking || actor == null || actor.isActionsDisabled() || actor.isAfraid())
		{
			return;
		}
		if(_randomAnimationEnd > System.currentTimeMillis())
		{
			return;
		}
		if(Config.RAID_TELE_TO_HOME_FROM_PVP_ZONES && actor.isRaid() && (actor.isInZoneBattle() || actor.isInZone(Zone.ZoneType.SIEGE) || actor.isInZone(Zone.ZoneType.fun)))
		{
			teleportHome();
			return;
		}
		if(Config.RAID_TELE_TO_HOME_FROM_TOWN_ZONES && actor.isRaid() && actor.isInZonePeace())
		{
			teleportHome();
			return;
		}
		_thinking = true;
		try
		{
			if(!Config.BLOCK_ACTIVE_TASKS && getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
			{
				thinkActive();
			}
			else if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
			{
				thinkAttack();
			}
		}
		finally
		{
			_thinking = false;
		}
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		int transformer = actor.getParameter("transformOnDead", 0);
		int amount = actor.getParameter("transformSpawnAmount", 1);
		int rndRadius = actor.getParameter("transformSpawnRndRadius", 0);
		int chance = actor.getParameter("transformChance", 100);
		if(transformer > 0 && Rnd.chance(chance))
		{
			for(int cnt = 0;cnt < amount;++cnt)
			{
				Location loc = actor.getLoc();
				if(rndRadius > 0)
				{
					loc = Location.findPointToStay(loc, rndRadius, killer.getGeoIndex());
				}
				NpcInstance npc = NpcUtils.spawnSingle(transformer, loc, actor.getReflection());
				if(killer == null || !killer.isPlayable())
					continue;
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 100);
				killer.sendPacket(npc.makeStatusUpdate(9, 10));
			}
		}
		super.onEvtDead(killer);
	}
	
	@Override
	protected void onEvtClanAttacked(Creature attacked, Creature attacker, int damage)
	{
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE || !isGlobalAggro())
		{
			return;
		}
		notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 2);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker == null || actor.isDead())
		{
			return;
		}
		int transformer = actor.getParameter("transformOnUnderAttack", 0);
		int chance;
		if(transformer > 0 && ((chance = actor.getParameter("transformChance", 5)) == 100 || ((MonsterInstance) actor).getChampion() == 0 && actor.getCurrentHpPercents() > 50.0 && Rnd.chance(chance)))
		{
			MonsterInstance npc = (MonsterInstance) NpcHolder.getInstance().getTemplate(transformer).getNewInstance();
			npc.setSpawnedLoc(actor.getLoc());
			npc.setReflection(actor.getReflection());
			npc.setChampion(((MonsterInstance) actor).getChampion());
			npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
			npc.spawnMe(npc.getSpawnedLoc());
			npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 100);
			actor.doDie(actor);
			actor.decayMe();
			attacker.setTarget(npc);
			attacker.sendPacket(npc.makeStatusUpdate(9, 10));
			return;
		}
		Player player = attacker.getPlayer();
		if(player != null)
		{
			if(Config.ALT_TELEPORT_FROM_SEVEN_SING_MONSTER && (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod()) && actor.isSevenSignsMonster())
			{
				int pcabal = SevenSigns.getInstance().getPlayerCabal(player);
				int wcabal = SevenSigns.getInstance().getCabalHighestScore();
				if(pcabal != wcabal && wcabal != 0)
				{
					player.sendMessage("You have been teleported to the nearest town because you not signed for winning cabal.");
					player.teleToClosestTown();
					return;
				}
			}
			List<QuestState> quests = player.getQuestsForEvent(actor, QuestEventType.ATTACKED_WITH_QUEST);
			if(quests != null)
			{
				for(QuestState qs : quests)
				{
					qs.getQuest().notifyAttack(actor, qs);
				}
			}
		}
		actor.getAggroList().addDamageHate(attacker, 0, damage);
		if(damage > 0 && (attacker.isSummon() || attacker.isPet()))
		{
			actor.getAggroList().addDamageHate(attacker.getPlayer(), 0, actor.getParameter("searchingMaster", false) ? damage : 1);
		}
		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			if(!actor.isRunning())
			{
				startRunningTask(AI_TASK_ATTACK_DELAY);
			}
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
		notifyFriends(attacker, damage);
	}
	
	@Override
	protected void onEvtAggression(Creature attacker, int aggro)
	{
		NpcInstance actor = getActor();
		if(attacker == null || actor.isDead())
		{
			return;
		}
		actor.getAggroList().addDamageHate(attacker, 0, aggro);
		if(aggro > 0 && (attacker.isSummon() || attacker.isPet()))
		{
			actor.getAggroList().addDamageHate(attacker.getPlayer(), 0, actor.getParameter("searchingMaster", false) ? aggro : 1);
		}
		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			if(!actor.isRunning())
			{
				startRunningTask(AI_TASK_ATTACK_DELAY);
			}
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
	}
	
	protected boolean maybeMoveToHome()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
		{
			return false;
		}
		boolean randomWalk = actor.hasRandomWalk();
		Location sloc = getPursueBaseLoc();
		if(!(!randomWalk || Config.RND_WALK && Rnd.chance(Config.RND_WALK_RATE)))
		{
			return false;
		}
		boolean isInRange = actor.isInRangeZ(sloc, (long) Config.MAX_DRIFT_RANGE);
		if(!randomWalk && isInRange)
		{
			return false;
		}
		Location pos = Location.findPointToStay(actor, sloc, 0, Config.MAX_DRIFT_RANGE);
		actor.setWalking();
		if(!actor.moveToLocation(pos.x, pos.y, pos.z, 0, true) && !isInRange)
		{
			teleportHome();
		}
		return true;
	}
	
	protected void returnHome()
	{
		returnHome(true, Config.ALWAYS_TELEPORT_HOME);
	}
	
	protected void teleportHome()
	{
		returnHome(true, true);
	}
	
	protected void returnHome(boolean clearAggro, boolean teleport)
	{
		NpcInstance actor = getActor();
		Location sloc = getPursueBaseLoc();
		clearTasks();
		actor.stopMove();
		if(clearAggro)
		{
			actor.getAggroList().clear(true);
		}
		setAttackTimeout(Long.MAX_VALUE);
		setAttackTarget(null);
		changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		if(teleport)
		{
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 0));
			actor.teleToLocation(sloc.x, sloc.y, GeoEngine.getHeight(sloc, actor.getGeoIndex()));
		}
		else
		{
			if(!clearAggro)
			{
				actor.setRunning();
			}
			else
			{
				actor.setWalking();
			}
			addTaskMove(sloc, false);
		}
	}
	
	protected Creature prepareTarget()
	{
		NpcInstance actor = getActor();
		if(actor.isConfused())
		{
			return getAttackTarget();
		}
		Creature randomHated;
		if(Rnd.chance(actor.getParameter("isMadness", 0)) && (randomHated = actor.getAggroList().getRandomHated()) != null)
		{
			setAttackTarget(randomHated);
			if(_madnessTask == null && !actor.isConfused())
			{
				actor.startConfused();
				_madnessTask = ThreadPoolManager.getInstance().schedule(new MadnessTask(), 10000);
			}
			return randomHated;
		}
		List<Creature> hateList = actor.getAggroList().getHateList(MAX_PURSUE_RANGE);
		Creature hated = null;
		for(Creature cha : hateList)
		{
			if(!checkTarget(cha, MAX_PURSUE_RANGE))
			{
				actor.getAggroList().remove(cha, true);
				continue;
			}
			hated = cha;
			break;
		}
		if(hated != null)
		{
			setAttackTarget(hated);
			return hated;
		}
		return null;
	}
	
	protected boolean canUseSkill(Skill skill, Creature target, double distance)
	{
		NpcInstance actor = getActor();
		if(skill == null || skill.isNotUsedByAI())
		{
			return false;
		}
		if(skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF && target != actor)
		{
			return false;
		}
		int castRange = skill.getAOECastRange();
		if(castRange <= 200 && distance > 200.0)
		{
			return false;
		}
		if(actor.isSkillDisabled(skill) || actor.isMuted(skill) || actor.isUnActiveSkill(skill.getId()))
		{
			return false;
		}
		double mpConsume2 = skill.getMpConsume2();
		mpConsume2 = skill.isMagic() ? actor.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, target, skill) : actor.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, target, skill);
		if(actor.getCurrentMp() < mpConsume2)
		{
			return false;
		}
		return target.getEffectList().getEffectsCountForSkill(skill.getId()) == 0;
	}
	
	protected boolean canUseSkill(Skill sk, Creature target)
	{
		return canUseSkill(sk, target, 0.0);
	}
	
	protected Skill[] selectUsableSkills(Creature target, double distance, Skill[] skills)
	{
		if(skills == null || skills.length == 0 || target == null)
		{
			return null;
		}
		Skill[] ret = null;
		int usable = 0;
		for(Skill skill : skills)
		{
			if(!canUseSkill(skill, target, distance))
				continue;
			if(ret == null)
			{
				ret = new Skill[skills.length];
			}
			ret[usable++] = skill;
		}
		if(ret == null || usable == skills.length)
		{
			return ret;
		}
		if(usable == 0)
		{
			return null;
		}
		ret = Arrays.copyOf(ret, usable);
		return ret;
	}
	
	protected void addDesiredSkill(Map<Skill, Integer> skillMap, Creature target, double distance, Skill[] skills)
	{
		if(skills == null || skills.length == 0 || target == null)
		{
			return;
		}
		for(Skill sk : skills)
		{
			addDesiredSkill(skillMap, target, distance, sk);
		}
	}
	
	protected void addDesiredSkill(Map<Skill, Integer> skillMap, Creature target, double distance, Skill skill)
	{
		if(skill == null || target == null || !canUseSkill(skill, target))
		{
			return;
		}
		int weight = (int) -Math.abs((double) skill.getAOECastRange() - distance);
		if((double) skill.getAOECastRange() >= distance)
		{
			weight += 1000000;
		}
		else if(skill.isNotTargetAoE() && skill.getTargets(getActor(), target, false).size() == 0)
		{
			return;
		}
		skillMap.put(skill, weight);
	}
	
	protected void addDesiredHeal(Map<Skill, Integer> skillMap, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
		{
			return;
		}
		NpcInstance actor = getActor();
		double hpReduced = (double) actor.getMaxHp() - actor.getCurrentHp();
		double hpPercent = actor.getCurrentHpPercents();
		if(hpReduced < 1.0)
		{
			return;
		}
		for(Skill sk : skills)
		{
			if(!canUseSkill(sk, actor) || sk.getPower() > hpReduced)
				continue;
			int weight = (int) sk.getPower();
			if(hpPercent < 50.0)
			{
				weight += 1000000;
			}
			skillMap.put(sk, weight);
		}
	}
	
	protected void addDesiredBuff(Map<Skill, Integer> skillMap, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
		{
			return;
		}
		NpcInstance actor = getActor();
		for(Skill sk : skills)
		{
			if(!canUseSkill(sk, actor))
				continue;
			skillMap.put(sk, 1000000);
		}
	}
	
	protected Skill selectTopSkill(Map<Skill, Integer> skillMap)
	{
		if(skillMap == null || skillMap.isEmpty())
		{
			return null;
		}
		int topWeight = Integer.MIN_VALUE;
		int nWeight;
		for(Skill next : skillMap.keySet())
		{
			nWeight = skillMap.get(next);
			if(nWeight <= topWeight)
				continue;
			topWeight = nWeight;
		}
		if(topWeight == Integer.MIN_VALUE)
		{
			return null;
		}
		Skill[] skills = new Skill[skillMap.size()];
		nWeight = 0;
		for(Map.Entry<Skill, Integer> e : skillMap.entrySet())
		{
			if(e.getValue() < topWeight)
				continue;
			skills[nWeight++] = e.getKey();
		}
		return skills[Rnd.get(nWeight)];
	}
	
	protected boolean chooseTaskAndTargets(Skill skill, Creature target, double distance)
	{
		NpcInstance actor = getActor();
		if(skill != null)
		{
			if(actor.isMovementDisabled() && distance > (double) (skill.getAOECastRange() + 60))
			{
				target = null;
				if(skill.isOffensive())
				{
					LazyArrayList<Creature> targets = LazyArrayList.newInstance();
					for(Creature cha : actor.getAggroList().getHateList(MAX_PURSUE_RANGE))
					{
						if(!checkTarget(cha, skill.getAOECastRange() + 60) || !canUseSkill(skill, cha))
							continue;
						targets.add(cha);
					}
					if(!targets.isEmpty())
					{
						target = targets.get(Rnd.get(targets.size()));
					}
					LazyArrayList.recycle(targets);
				}
			}
			if(target == null)
			{
				return false;
			}
			if(skill.isOffensive())
			{
				addTaskCast(target, skill);
			}
			else
			{
				addTaskBuff(target, skill);
			}
			return true;
		}
		if(actor.isMovementDisabled() && distance > (double) (actor.getPhysicalAttackRange() + 40))
		{
			target = null;
			LazyArrayList<Creature> targets = LazyArrayList.newInstance();
			for(Creature cha : actor.getAggroList().getHateList(MAX_PURSUE_RANGE))
			{
				if(!checkTarget(cha, actor.getPhysicalAttackRange() + 40))
					continue;
				targets.add(cha);
			}
			if(!targets.isEmpty())
			{
				target = targets.get(Rnd.get(targets.size()));
			}
			LazyArrayList.recycle(targets);
		}
		if(target == null)
		{
			return false;
		}
		addTaskAttack(target);
		return true;
	}
	
	@Override
	public boolean isActive()
	{
		return _aiTask != null;
	}
	
	protected void clearTasks()
	{
		_def_think = false;
		_tasks.clear();
	}
	
	protected void startRunningTask(long interval)
	{
		NpcInstance actor = getActor();
		if(actor != null && _runningTask == null && !actor.isRunning())
		{
			_runningTask = ThreadPoolManager.getInstance().schedule(new RunningTask(), interval);
		}
	}
	
	protected boolean isGlobalAggro()
	{
		if(_globalAggro == 0)
		{
			return true;
		}
		if(_globalAggro <= System.currentTimeMillis())
		{
			_globalAggro = 0;
			return true;
		}
		return false;
	}
	
	public void setGlobalAggro(long value)
	{
		_globalAggro = value;
	}
	
	@Override
	public NpcInstance getActor()
	{
		return (NpcInstance) super.getActor();
	}
	
	protected boolean defaultThinkBuff(int rateSelf)
	{
		return defaultThinkBuff(rateSelf, 0);
	}
	
	protected void notifyFriends(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(System.currentTimeMillis() - _lastFactionNotifyTime > _minFactionNotifyInterval)
		{
			_lastFactionNotifyTime = System.currentTimeMillis();
			if(actor.isMinion())
			{
				MonsterInstance master = ((MinionInstance) actor).getLeader();
				if(master != null)
				{
					if(!master.isDead() && master.isVisible())
					{
						master.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, damage);
					}
					
					MinionList minionList = master.getMinionList();
					if(minionList != null)
					{
						for(MinionInstance minion : minionList.getAliveMinions())
						{
							if(minion != actor)
							{
								minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, damage);
							}
						}
					}
				}
			}
			
			MinionList minionList = actor.getMinionList();
			if(minionList != null && minionList.hasAliveMinions())
			{
				for(MinionInstance minion : minionList.getAliveMinions())
				{
					minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, damage);
				}
			}
			
			for(NpcInstance npc : activeFactionTargets())
			{
				npc.getAI().notifyEvent(CtrlEvent.EVT_CLAN_ATTACKED, new Object[] {actor, attacker, damage});
			}
		}
	}
	
	protected List<NpcInstance> activeFactionTargets()
	{
		NpcInstance actor = getActor();
		if(actor.getFaction().isNone())
		{
			return Collections.emptyList();
		}
		LazyArrayList<NpcInstance> npcFriends = new LazyArrayList<>();
		for(NpcInstance npc : World.getAroundNpc(actor))
		{
			if(npc.isDead() || !npc.isInFaction(actor) || !npc.isInRangeZ(actor, (long) npc.getFaction().getRange()) || !GeoEngine.canSeeTarget(npc, actor, false))
				continue;
			npcFriends.add(npc);
		}
		return npcFriends;
	}
	
	protected boolean defaultThinkBuff(int rateSelf, int rateFriends)
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
		{
			return true;
		}
		if(Rnd.chance(rateSelf))
		{
			Skill[] skills;
			double actorHp = actor.getCurrentHpPercents();
			Skill[] arrskill = skills = actorHp < 50.0 ? selectUsableSkills(actor, 0.0, _healSkills) : selectUsableSkills(actor, 0.0, _buffSkills);
			if(skills == null || skills.length == 0)
			{
				return false;
			}
			Skill skill = skills[Rnd.get(skills.length)];
			addTaskBuff(actor, skill);
			return true;
		}
		if(Rnd.chance(rateFriends))
		{
			for(NpcInstance npc : activeFactionTargets())
			{
				double targetHp = npc.getCurrentHpPercents();
				Skill[] skills = targetHp < 50.0 ? selectUsableSkills(actor, 0.0, _healSkills) : selectUsableSkills(actor, 0.0, _buffSkills);
				if(skills == null || skills.length == 0)
					continue;
				Skill skill = skills[Rnd.get(skills.length)];
				addTaskBuff(actor, skill);
				return true;
			}
		}
		return false;
	}
	
	protected boolean defaultFightTask()
	{
		clearTasks();
		NpcInstance actor = getActor();
		if(actor.isDead() || actor.isAMuted())
		{
			return false;
		}
		Creature target = prepareTarget();
		if(target == null)
		{
			return false;
		}
		double distance = actor.getDistance(target);
		double targetHp = target.getCurrentHpPercents();
		double actorHp = actor.getCurrentHpPercents();
		Skill[] dam = Rnd.chance(getRateDAM()) ? selectUsableSkills(target, distance, _damSkills) : null;
		Skill[] dot;
		Skill[] arrskill = dot = Rnd.chance(getRateDOT()) ? selectUsableSkills(target, distance, _dotSkills) : null;
		Skill[] debuff = targetHp > 10.0 ? Rnd.chance(getRateDEBUFF()) ? selectUsableSkills(target, distance, _debuffSkills) : null : null;
		Skill[] stun;
		Skill[] arrskill2 = stun = Rnd.chance(getRateSTUN()) ? selectUsableSkills(target, distance, _stunSkills) : null;
		Skill[] heal = actorHp < 50.0 ? Rnd.chance(getRateHEAL()) ? selectUsableSkills(actor, 0.0, _healSkills) : null : null;
		Skill[] buff = Rnd.chance(getRateBUFF()) ? selectUsableSkills(actor, 0.0, _buffSkills) : null;
		RndSelector<Skill[]> rnd = new RndSelector<>();
		if(!actor.isAMuted())
		{
			rnd.add(null, getRatePHYS());
		}
		rnd.add(dam, getRateDAM());
		rnd.add(dot, getRateDOT());
		rnd.add(debuff, getRateDEBUFF());
		rnd.add(heal, getRateHEAL());
		rnd.add(buff, getRateBUFF());
		rnd.add(stun, getRateSTUN());
		Skill[] selected = rnd.select();
		if(selected != null)
		{
			if(selected == dam || selected == dot)
			{
				return chooseTaskAndTargets(selectTopSkillByDamage(actor, target, distance, selected), target, distance);
			}
			if(selected == debuff || selected == stun)
			{
				return chooseTaskAndTargets(selectTopSkillByDebuff(actor, target, distance, selected), target, distance);
			}
			if(selected == buff)
			{
				return chooseTaskAndTargets(selectTopSkillByBuff(actor, selected), actor, distance);
			}
			if(selected == heal)
			{
				return chooseTaskAndTargets(selectTopSkillByHeal(actor, selected), actor, distance);
			}
		}
		return chooseTaskAndTargets(null, target, distance);
	}
	
	public int getRatePHYS()
	{
		return 100;
	}
	
	public int getRateDOT()
	{
		return 0;
	}
	
	public int getRateDEBUFF()
	{
		return 0;
	}
	
	public int getRateDAM()
	{
		return 0;
	}
	
	public int getRateSTUN()
	{
		return 0;
	}
	
	public int getRateBUFF()
	{
		return 0;
	}
	
	public int getRateHEAL()
	{
		return 0;
	}
	
	public boolean getIsMobile()
	{
		return !getActor().getParameter("isImmobilized", false);
	}
	
	public int getMaxPathfindFails()
	{
		return 3;
	}
	
	public int getMaxAttackTimeout()
	{
		return 15000;
	}
	
	public int getTeleportTimeout()
	{
		return 10000;
	}
	
	public enum TaskType
	{
		MOVE,
		ATTACK,
		CAST,
		BUFF;
	}
	
	private static class TaskComparator implements Comparator<Task>
	{
		private static final Comparator<Task> instance = new TaskComparator();
		
		public static final Comparator<Task> getInstance()
		{
			return instance;
		}
		
		@Override
		public int compare(Task o1, Task o2)
		{
			if(o1 == null || o2 == null)
			{
				return 0;
			}
			return o2.weight - o1.weight;
		}
	}
	
	public static class Task
	{
		public TaskType type;
		public Skill skill;
		public HardReference<? extends Creature> target;
		public Location loc;
		public boolean pathfind;
		public int weight = 10000;
	}
	
	protected class NearestTargetComparator implements Comparator<Creature>
	{
		private final Creature actor;
		
		public NearestTargetComparator(Creature actor)
		{
			this.actor = actor;
		}
		
		@Override
		public int compare(Creature o1, Creature o2)
		{
			double diff = actor.getDistance3D(o1) - actor.getDistance3D(o2);
			if(diff < 0.0)
			{
				return -1;
			}
			return diff > 0.0 ? 1 : 0;
		}
	}
	
	public class MadnessTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			NpcInstance actor = getActor();
			if(actor != null)
			{
				actor.stopConfused();
			}
			_madnessTask = null;
		}
	}
	
	protected class RunningTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			NpcInstance actor = getActor();
			if(actor != null)
			{
				actor.setRunning();
			}
			_runningTask = null;
		}
	}
	
	public class Teleport extends RunnableImpl
	{
		Location _destination;
		
		public Teleport(Location destination)
		{
			_destination = destination;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			NpcInstance actor = getActor();
			if(actor != null)
			{
				actor.teleToLocation(_destination);
			}
		}
	}
}