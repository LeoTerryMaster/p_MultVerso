package l2.gameserver.model;

import l2.commons.collections.MultiValueSet;
import l2.commons.time.cron.NextTime;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.entity.events.EventOwner;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.model.instances.MinionInstance;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.PetInstance;
import l2.gameserver.taskmanager.SpawnTaskManager;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.templates.spawn.SpawnRange;
import l2.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Spawner extends EventOwner implements Cloneable
{
	protected static final Logger _log = LoggerFactory.getLogger(Spawner.class);
	protected static final int MIN_RESPAWN_DELAY = 300;
	private static final long serialVersionUID = 1;
	protected int _maximumCount;
	protected int _referenceCount;
	protected AtomicInteger _currentCount = new AtomicInteger(0);
	protected AtomicInteger _scheduledCount = new AtomicInteger(0);
	protected long _respawnDelay;
	protected long _respawnDelayRandom;
	protected NextTime _respawnCron;
	protected int _respawnTime;
	protected boolean _doRespawn;
	protected NpcInstance _lastSpawn;
	protected List<NpcInstance> _spawned;
	protected Reflection _reflection = ReflectionManager.DEFAULT;
	
	public void decreaseScheduledCount()
	{
		int scheduledCount;
		while((scheduledCount = _scheduledCount.get()) > 0)
		{
			if(!_scheduledCount.compareAndSet(scheduledCount, scheduledCount - 1))
				continue;
			return;
		}
	}
	
	public boolean isDoRespawn()
	{
		return _doRespawn;
	}
	
	public Reflection getReflection()
	{
		return _reflection;
	}
	
	public void setReflection(Reflection reflection)
	{
		_reflection = reflection;
	}
	
	public long getRespawnDelay()
	{
		return _respawnDelay;
	}
	
	public void setRespawnDelay(int respawnDelay)
	{
		setRespawnDelay(respawnDelay, 0);
	}
	
	public long getRespawnDelayRandom()
	{
		return _respawnDelayRandom;
	}
	
	public long getRespawnDelayWithRnd()
	{
		if(_respawnDelayRandom == 0)
		{
			return _respawnDelay;
		}
		return Rnd.get(_respawnDelay - _respawnDelayRandom, _respawnDelay);
	}
	
	public int getRespawnTime()
	{
		return _respawnTime;
	}
	
	public void setRespawnTime(int respawnTime)
	{
		_respawnTime = respawnTime;
	}
	
	public NpcInstance getLastSpawn()
	{
		return _lastSpawn;
	}
	
	public void setAmount(int amount)
	{
		if(_referenceCount == 0)
		{
			_referenceCount = amount;
		}
		_maximumCount = amount;
	}
	
	public void deleteAll()
	{
		stopRespawn();
		for(NpcInstance npc : _spawned)
		{
			npc.deleteMe();
		}
		_spawned.clear();
		_respawnTime = 0;
		_scheduledCount.set(0);
		_currentCount.set(0);
	}
	
	public abstract void decreaseCount(NpcInstance oldNpc);
	
	public abstract NpcInstance doSpawn(boolean spawn);
	
	public abstract void respawnNpc(NpcInstance oldNpc);
	
	protected abstract NpcInstance initNpc(NpcInstance mob, boolean spawn, MultiValueSet<String> set);
	
	public abstract int getCurrentNpcId();
	
	public abstract SpawnRange getCurrentSpawnRange();
	
	public int init()
	{
		while(_currentCount.get() + _scheduledCount.get() < _maximumCount)
		{
			doSpawn(false);
		}
		_doRespawn = true;
		return _currentCount.get();
	}
	
	public NpcInstance spawnOne()
	{
		return doSpawn(false);
	}
	
	public void stopRespawn()
	{
		_doRespawn = false;
	}
	
	public void startRespawn()
	{
		_doRespawn = true;
	}
	
	public List<NpcInstance> getAllSpawned()
	{
		return _spawned;
	}
	
	public NpcInstance getFirstSpawned()
	{
		List<NpcInstance> npcs = getAllSpawned();
		return npcs.size() > 0 ? npcs.get(0) : null;
	}
	
	public void setRespawnDelay(long respawnDelay, long respawnDelayRandom)
	{
		if(respawnDelay < 0)
		{
			_log.warn("respawn delay is negative");
		}
		_respawnDelay = respawnDelay;
		_respawnDelayRandom = respawnDelayRandom;
	}
	
	public NextTime getRespawnCron()
	{
		return _respawnCron;
	}
	
	public void setRespawnCron(NextTime respawnCron)
	{
		_respawnCron = respawnCron;
	}
	
	protected NpcInstance doSpawn0(NpcTemplate template, boolean spawn, MultiValueSet<String> set)
	{
		if(template.isInstanceOf(PetInstance.class) || template.isInstanceOf(MinionInstance.class))
		{
			_currentCount.incrementAndGet();
			return null;
		}
		NpcInstance tmp = template.getNewInstance();
		if(tmp == null)
		{
			return null;
		}
		if(!spawn)
		{
			spawn = (long) _respawnTime <= System.currentTimeMillis() / 1000 + 300;
		}
		return initNpc(tmp, spawn, set);
	}
	
	protected NpcInstance initNpc0(NpcInstance mob, Location newLoc, boolean spawn, MultiValueSet<String> set)
	{
		mob.setParameters(set);
		mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp(), true);
		mob.setSpawn(this);
		mob.setSpawnedLoc(newLoc);
		mob.setUnderground(GeoEngine.getHeight(newLoc, getReflection().getGeoIndex()) < GeoEngine.getHeight(newLoc.clone().changeZ(5000), getReflection().getGeoIndex()));
		for(GlobalEvent e : getEvents())
		{
			mob.addEvent(e);
		}
		if(spawn)
		{
			mob.setReflection(getReflection());
			if(mob.isMonster())
			{
				((MonsterInstance) mob).setChampion();
			}
			mob.spawnMe(newLoc);
			_currentCount.incrementAndGet();
		}
		else
		{
			mob.setLoc(newLoc);
			_scheduledCount.incrementAndGet();
			SpawnTaskManager.getInstance().addSpawnTask(mob, (long) _respawnTime * 1000 - System.currentTimeMillis());
		}
		_spawned.add(mob);
		_lastSpawn = mob;
		return mob;
	}
	
	public void decreaseCount0(NpcTemplate template, NpcInstance spawnedNpc, long deadTime)
	{
		int currentCount;
		while((currentCount = _currentCount.get()) > 0 && !_currentCount.compareAndSet(currentCount, currentCount - 1))
		{
		}
		if(getRespawnDelay() == 0 && getRespawnCron() == null)
		{
			return;
		}
		if(_doRespawn && _scheduledCount.get() + _currentCount.get() < _maximumCount)
		{
			long now = System.currentTimeMillis();
			long delay = getRespawnCron() == null ? template.isRaid ? (long) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (double) getRespawnDelayWithRnd()) * 1000 : getRespawnDelayWithRnd() * 1000 : getRespawnCron().next(now) - now;
			delay = Math.max(1000, delay - deadTime);
			_respawnTime = (int) ((now + delay) / 1000);
			_scheduledCount.incrementAndGet();
			SpawnTaskManager.getInstance().addSpawnTask(spawnedNpc, delay);
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(NpcInstance spawnedNpc : _spawned)
		{
			sb.append(spawnedNpc.getNpcId());
		}
		return "Spawner{_currentCount=" + _currentCount + ", _maximumCount=" + _maximumCount + ", _referenceCount=" + _referenceCount + ", _scheduledCount=" + _scheduledCount + ", _respawnDelay=" + _respawnDelay + ", _respawnCron=" + _respawnCron + ", _respawnDelayRandom=" + _respawnDelayRandom + ", _respawnTime=" + _respawnTime + ", _doRespawn=" + _doRespawn + ", _lastSpawn=" + _lastSpawn + ", _spawned=" + _spawned + ", _reflection=" + _reflection + '}';
	}
}