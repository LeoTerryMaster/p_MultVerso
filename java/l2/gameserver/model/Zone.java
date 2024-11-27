package l2.gameserver.model;

import l2.commons.collections.LazyArrayList;
import l2.commons.collections.MultiValueSet;
import l2.commons.listener.Listener;
import l2.commons.listener.ListenerList;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.network.l2.s2c.EventTrigger;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Stats;
import l2.gameserver.stats.funcs.FuncAdd;
import l2.gameserver.taskmanager.EffectTaskManager;
import l2.gameserver.templates.ZoneTemplate;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.PositionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Zone
{
	public static final Zone[] EMPTY_L2ZONE_ARRAY = new Zone[0];
	public static final String BLOCKED_ACTION_PRIVATE_STORE = "open_private_store";
	public static final String BLOCKED_ACTION_PRIVATE_WORKSHOP = "open_private_workshop";
	public static final String BLOCKED_ACTION_DROP_MERCHANT_GUARD = "drop_merchant_guard";
	public static final String BLOCKED_ACTION_SAVE_BOOKMARK = "save_bookmark";
	public static final String BLOCKED_ACTION_USE_BOOKMARK = "use_bookmark";
	public static final String BLOCKED_ACTION_MINIMAP = "open_minimap";
	public static final String BLOCKED_ACTION_DROP_ITEM = "drop_item";
	public static final int ZONE_STATS_ORDER = 64;
	private static final Logger _log = LoggerFactory.getLogger(Zone.class);
	private final MultiValueSet<String> _params;
	private final ZoneTemplate _template;
	private final ZoneListenerList listeners;
	private final ReadWriteLock lock;
	private final Lock readLock;
	private final Lock writeLock;
	private final List<Creature> _objects;
	private final Map<Creature, ZoneTimer> _zoneTimers;
	private ZoneType _type;
	private boolean _active;
	private Reflection _reflection;
	
	public Zone(ZoneTemplate template)
	{
		this(template.getType(), template);
	}
	
	public Zone(ZoneType type, ZoneTemplate template)
	{
		listeners = new ZoneListenerList();
		lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();
		_objects = new LazyArrayList<>(32);
		_zoneTimers = new ConcurrentHashMap<>();
		_type = type;
		_template = template;
		_params = template.getParams();
	}
	
	public ZoneTemplate getTemplate()
	{
		return _template;
	}
	
	public final String getName()
	{
		return getTemplate().getName();
	}
	
	public ZoneType getType()
	{
		return _type;
	}
	
	public void setType(ZoneType type)
	{
		_type = type;
	}
	
	public Territory getTerritory()
	{
		return getTemplate().getTerritory();
	}
	
	public final int getEnteringMessageId()
	{
		return getTemplate().getEnteringMessageId();
	}
	
	public final int getLeavingMessageId()
	{
		return getTemplate().getLeavingMessageId();
	}
	
	public Skill getZoneSkill()
	{
		return getTemplate().getZoneSkill();
	}
	
	public ZoneTarget getZoneTarget()
	{
		return getTemplate().getZoneTarget();
	}
	
	public Race getAffectRace()
	{
		return getTemplate().getAffectRace();
	}
	
	public int getDamageMessageId()
	{
		return getTemplate().getDamageMessageId();
	}
	
	public int getDamageOnHP()
	{
		return getTemplate().getDamageOnHP();
	}
	
	public int getDamageOnMP()
	{
		return getTemplate().getDamageOnMP();
	}
	
	public double getMoveBonus()
	{
		return getTemplate().getMoveBonus();
	}
	
	public double getRegenBonusHP()
	{
		return getTemplate().getRegenBonusHP();
	}
	
	public double getRegenBonusMP()
	{
		return getTemplate().getRegenBonusMP();
	}
	
	public long getRestartTime()
	{
		return getTemplate().getRestartTime();
	}
	
	public List<Location> getRestartPoints()
	{
		return getTemplate().getRestartPoints();
	}
	
	public List<Location> getPKRestartPoints()
	{
		return getTemplate().getPKRestartPoints();
	}
	
	public Location getSpawn()
	{
		if(getRestartPoints() == null)
		{
			return null;
		}
		Location loc = getRestartPoints().get(Rnd.get(getRestartPoints().size()));
		return loc.clone();
	}
	
	public Location getPKSpawn()
	{
		if(getPKRestartPoints() == null)
		{
			return getSpawn();
		}
		Location loc = getPKRestartPoints().get(Rnd.get(getPKRestartPoints().size()));
		return loc.clone();
	}
	
	public boolean checkIfInZone(int x, int y)
	{
		return getTerritory().isInside(x, y);
	}
	
	public boolean checkIfInZone(int x, int y, int z)
	{
		return checkIfInZone(x, y, z, getReflection());
	}
	
	public boolean checkIfInZone(int x, int y, int z, Reflection reflection)
	{
		return isActive() && _reflection == reflection && getTerritory().isInside(x, y, z);
	}
	
	public boolean checkIfInZone(Creature cha)
	{
		readLock.lock();
		try
		{
			boolean bl = _objects.contains(cha);
			return bl;
		}
		finally
		{
			readLock.unlock();
		}
	}
	
	public final double findDistanceToZone(GameObject obj, boolean includeZAxis)
	{
		return findDistanceToZone(obj.getX(), obj.getY(), obj.getZ(), includeZAxis);
	}
	
	public final double findDistanceToZone(int x, int y, int z, boolean includeZAxis)
	{
		return PositionUtils.calculateDistance(x, y, z, (getTerritory().getXmax() + getTerritory().getXmin()) / 2, (getTerritory().getYmax() + getTerritory().getYmin()) / 2, (getTerritory().getZmax() + getTerritory().getZmin()) / 2, includeZAxis);
	}
	
	public void doEnter(Creature cha)
	{
		boolean added = false;
		writeLock.lock();
		try
		{
			if(!_objects.contains(cha))
			{
				added = _objects.add(cha);
			}
		}
		finally
		{
			writeLock.unlock();
		}
		if(added)
		{
			onZoneEnter(cha);
		}
	}
	
	protected void onZoneEnter(Creature actor)
	{
		checkEffects(actor, true);
		addZoneStats(actor);
		if(actor.isPlayer())
		{
			if(getEnteringMessageId() != 0)
			{
				actor.sendPacket(new SystemMessage(getEnteringMessageId()));
			}
			if(getTemplate().getEventId() != 0)
			{
				actor.sendPacket(new EventTrigger(getTemplate().getEventId(), true));
			}
			if(getTemplate().getBlockedActions() != null)
			{
				((Player) actor).blockActions(getTemplate().getBlockedActions());
			}
		}
		listeners.onEnter(actor);
	}
	
	public void doLeave(Creature cha)
	{
		writeLock.lock();
		boolean removed;
		try
		{
			removed = _objects.remove(cha);
		}
		finally
		{
			writeLock.unlock();
		}
		if(removed)
		{
			onZoneLeave(cha);
		}
	}
	
	protected void onZoneLeave(Creature actor)
	{
		checkEffects(actor, false);
		removeZoneStats(actor);
		if(actor.isPlayer())
		{
			if(getLeavingMessageId() != 0 && actor.isPlayer())
			{
				actor.sendPacket(new SystemMessage(getLeavingMessageId()));
			}
			if(getTemplate().getEventId() != 0 && actor.isPlayer())
			{
				actor.sendPacket(new EventTrigger(getTemplate().getEventId(), false));
			}
			if(getTemplate().getBlockedActions() != null)
			{
				((Player) actor).unblockActions(getTemplate().getBlockedActions());
			}
		}
		listeners.onLeave(actor);
	}
	
	private void addZoneStats(Creature cha)
	{
		if(!checkTarget(cha))
		{
			return;
		}
		if(getMoveBonus() != 0.0 && cha.isPlayable())
		{
			cha.addStatFunc(new FuncAdd(Stats.RUN_SPEED, 64, this, getMoveBonus()));
			cha.sendChanges();
		}
		if(getRegenBonusHP() != 0.0)
		{
			cha.addStatFunc(new FuncAdd(Stats.REGENERATE_HP_RATE, 64, this, getRegenBonusHP()));
		}
		if(getRegenBonusMP() != 0.0)
		{
			cha.addStatFunc(new FuncAdd(Stats.REGENERATE_MP_RATE, 64, this, getRegenBonusMP()));
		}
	}
	
	private void removeZoneStats(Creature cha)
	{
		if(getRegenBonusHP() == 0.0 && getRegenBonusMP() == 0.0 && getMoveBonus() == 0.0)
		{
			return;
		}
		cha.removeStatsOwner(this);
		cha.sendChanges();
	}
	
	private void checkEffects(Creature cha, boolean enter)
	{
		if(checkTarget(cha))
		{
			if(enter)
			{
				if(getZoneSkill() != null)
				{
					SkillTimer timer = new SkillTimer(cha);
					_zoneTimers.put(cha, timer);
					timer.start();
				}
				else if(getDamageOnHP() > 0 || getDamageOnMP() > 0)
				{
					DamageTimer timer = new DamageTimer(cha);
					_zoneTimers.put(cha, timer);
					timer.start();
				}
			}
			else
			{
				ZoneTimer timer = _zoneTimers.remove(cha);
				if(timer != null)
				{
					timer.stop();
				}
				if(getZoneSkill() != null)
				{
					cha.getEffectList().stopEffect(getZoneSkill());
				}
			}
		}
	}
	
	private boolean checkTarget(Creature cha)
	{
		switch(getZoneTarget())
		{
			case pc:
			{
				if(cha.isPlayable())
					break;
				return false;
			}
			case only_pc:
			{
				if(cha.isPlayer())
					break;
				return false;
			}
			case npc:
			{
				if(cha.isNpc())
					break;
				return false;
			}
		}
		if(getAffectRace() != null)
		{
			Player player = cha.getPlayer();
			if(player == null)
			{
				return false;
			}
			if(player.getRace() != getAffectRace())
			{
				return false;
			}
		}
		return true;
	}
	
	public Creature[] getObjects()
	{
		readLock.lock();
		try
		{
			Creature[] arrcreature = _objects.toArray(new Creature[_objects.size()]);
			return arrcreature;
		}
		finally
		{
			readLock.unlock();
		}
	}
	
	public List<Player> getInsidePlayers()
	{
		LazyArrayList<Player> result = new LazyArrayList<>();
		readLock.lock();
		try
		{
			for(int i = 0;i < _objects.size();++i)
			{
				Creature cha = _objects.get(i);
				if(cha == null || !cha.isPlayer())
					continue;
				result.add((Player) cha);
			}
		}
		finally
		{
			readLock.unlock();
		}
		return result;
	}
	
	public List<Playable> getInsidePlayables()
	{
		LazyArrayList<Playable> result = new LazyArrayList<>();
		readLock.lock();
		try
		{
			for(int i = 0;i < _objects.size();++i)
			{
				Creature cha = _objects.get(i);
				if(cha == null || !cha.isPlayable())
					continue;
				result.add((Playable) cha);
			}
		}
		finally
		{
			readLock.unlock();
		}
		return result;
	}
	
	public boolean isActive()
	{
		return _active;
	}
	
	public void setActive(boolean value)
	{
		writeLock.lock();
		try
		{
			if(_active == value)
			{
				return;
			}
			_active = value;
		}
		finally
		{
			writeLock.unlock();
		}
		if(isActive())
		{
			World.addZone(this);
		}
		else
		{
			World.removeZone(this);
		}
	}
	
	public Reflection getReflection()
	{
		return _reflection;
	}
	
	public void setReflection(Reflection reflection)
	{
		_reflection = reflection;
	}
	
	public void setParam(String name, String value)
	{
		_params.put(name, value);
	}
	
	public void setParam(String name, Object value)
	{
		_params.put(name, value);
	}
	
	public MultiValueSet<String> getParams()
	{
		return _params;
	}
	
	public <T extends Listener<Zone>> boolean addListener(T listener)
	{
		return listeners.add(listener);
	}
	
	public <T extends Listener<Zone>> boolean removeListener(T listener)
	{
		return listeners.remove(listener);
	}
	
	@Override
	public final String toString()
	{
		return "[Zone " + getType() + " name: " + getName() + "]";
	}
	
	public void broadcastPacket(L2GameServerPacket packet, boolean toAliveOnly)
	{
		List<Player> insideZoners = getInsidePlayers();
		if(insideZoners != null && !insideZoners.isEmpty())
		{
			for(Player player : insideZoners)
			{
				if(toAliveOnly)
				{
					if(player.isDead())
						continue;
					player.broadcastPacket(packet);
					continue;
				}
				player.broadcastPacket(packet);
			}
		}
	}
	
	public enum ZoneTarget
	{
		pc,
		npc,
		only_pc;
	}
	
	public enum ZoneType
	{
		SIEGE,
		RESIDENCE,
		HEADQUARTER,
		FISHING,
		water,
		battle_zone,
		damage,
		instant_skill,
		mother_tree,
		peace_zone,
		poison,
		ssq_zone,
		swamp,
		no_escape,
		no_landing,
		no_restart,
		no_summon,
		dummy,
		offshore,
		epic,
		fun;
	}
	
	public class ZoneListenerList extends ListenerList<Zone>
	{
		public void onEnter(Creature actor)
		{
			if(!getListeners().isEmpty())
			{
				for(Listener listener : getListeners())
				{
					((OnZoneEnterLeaveListener) listener).onZoneEnter(Zone.this, actor);
				}
			}
		}
		
		public void onLeave(Creature actor)
		{
			if(!getListeners().isEmpty())
			{
				for(Listener listener : getListeners())
				{
					((OnZoneEnterLeaveListener) listener).onZoneLeave(Zone.this, actor);
				}
			}
		}
	}
	
	private class DamageTimer extends ZoneTimer
	{
		public DamageTimer(Creature cha)
		{
			super(cha);
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(!isActive())
			{
				return;
			}
			if(!checkTarget(cha))
			{
				return;
			}
			int hp = getDamageOnHP();
			int mp = getDamageOnMP();
			int message = getDamageMessageId();
			if(hp == 0 && mp == 0)
			{
				return;
			}
			if(hp > 0)
			{
				cha.reduceCurrentHp(hp, cha, null, false, false, true, false, false, false, true);
				if(message > 0)
				{
					cha.sendPacket(new SystemMessage(message).addNumber(hp));
				}
			}
			if(mp > 0)
			{
				cha.reduceCurrentMp(mp, null);
				if(message > 0)
				{
					cha.sendPacket(new SystemMessage(message).addNumber(mp));
				}
			}
			next();
		}
	}
	
	private class SkillTimer extends ZoneTimer
	{
		public SkillTimer(Creature cha)
		{
			super(cha);
		}
		
		@Override
		public void runImpl() throws Exception
		{
			if(!isActive())
			{
				return;
			}
			if(!checkTarget(cha))
			{
				return;
			}
			Skill skill = getZoneSkill();
			if(skill == null)
			{
				return;
			}
			if(Rnd.chance(getTemplate().getSkillProb()) && !cha.isDead())
			{
				skill.getEffects(cha, cha, false, false);
			}
			next();
		}
	}
	
	private abstract class ZoneTimer extends RunnableImpl
	{
		protected Creature cha;
		protected Future<?> future;
		protected boolean active;
		
		public ZoneTimer(Creature cha)
		{
			this.cha = cha;
		}
		
		public void start()
		{
			active = true;
			future = EffectTaskManager.getInstance().schedule(this, (long) getTemplate().getInitialDelay() * 1000);
		}
		
		public void stop()
		{
			active = false;
			if(future != null)
			{
				future.cancel(false);
				future = null;
			}
		}
		
		public void next()
		{
			if(!active)
			{
				return;
			}
			if(getTemplate().getUnitTick() == 0 && getTemplate().getRandomTick() == 0)
			{
				return;
			}
			future = EffectTaskManager.getInstance().schedule(this, (long) (getTemplate().getUnitTick() + Rnd.get(0, getTemplate().getRandomTick())) * 1000);
		}
		
		@Override
		public abstract void runImpl() throws Exception;
	}
}