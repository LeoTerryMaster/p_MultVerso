package l2.gameserver.model;

import l2.commons.collections.MultiValueSet;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.templates.spawn.SpawnNpcInfo;
import l2.gameserver.templates.spawn.SpawnRange;
import l2.gameserver.templates.spawn.SpawnTemplate;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

public class HardSpawner extends Spawner
{
	private final SpawnTemplate _template;
	private final IntObjectMap<Queue<NpcInstance>> _cache = new CHashIntObjectMap();
	private final List<NpcInstance> _reSpawned = new CopyOnWriteArrayList<>();
	private int _pointIndex;
	private int _npcIndex;
	
	public HardSpawner(SpawnTemplate template)
	{
		_template = template;
		_spawned = new CopyOnWriteArrayList();
	}
	
	@Override
	public void decreaseCount(NpcInstance oldNpc)
	{
		addToCache(oldNpc);
		_spawned.remove(oldNpc);
		SpawnNpcInfo npcInfo = getNextNpcInfo();
		NpcInstance npc = getCachedNpc(npcInfo.getTemplate().getNpcId());
		if(npc == null)
		{
			npc = npcInfo.getTemplate().getNewInstance();
		}
		else
		{
			npc.refreshID();
		}
		npc.setSpawn(this);
		_reSpawned.add(npc);
		decreaseCount0(npcInfo.getTemplate(), npc, oldNpc.getDeadTime());
	}
	
	@Override
	public NpcInstance doSpawn(boolean spawn)
	{
		SpawnNpcInfo npcInfo = getNextNpcInfo();
		return doSpawn0(npcInfo.getTemplate(), spawn, npcInfo.getParameters());
	}
	
	@Override
	protected NpcInstance initNpc(NpcInstance mob, boolean spawn, MultiValueSet<String> set)
	{
		_reSpawned.remove(mob);
		SpawnRange range = _template.getSpawnRange(getNextRangeId());
		mob.setSpawnRange(range);
		return initNpc0(mob, range.getRandomLoc(getReflection().getGeoIndex()), spawn, set);
	}
	
	@Override
	public int getCurrentNpcId()
	{
		SpawnNpcInfo npcInfo = _template.getNpcId(_npcIndex);
		return npcInfo.getTemplate().npcId;
	}
	
	@Override
	public SpawnRange getCurrentSpawnRange()
	{
		return _template.getSpawnRange(_pointIndex);
	}
	
	@Override
	public void respawnNpc(NpcInstance oldNpc)
	{
		initNpc(oldNpc, true, StatsSet.EMPTY);
	}
	
	@Override
	public void deleteAll()
	{
		super.deleteAll();
		for(NpcInstance npc : _reSpawned)
		{
			addToCache(npc);
		}
		_reSpawned.clear();
		for(Collection c : _cache.values())
		{
			c.clear();
		}
		_cache.clear();
	}
	
	private synchronized SpawnNpcInfo getNextNpcInfo()
	{
		int old = _npcIndex++;
		if(_npcIndex >= _template.getNpcSize())
		{
			_npcIndex = 0;
		}
		SpawnNpcInfo npcInfo;
		if((npcInfo = _template.getNpcId(old)).getMax() > 0)
		{
			int count = 0;
			for(NpcInstance npc : _spawned)
			{
				if(npc.getNpcId() != npcInfo.getTemplate().getNpcId())
					continue;
				++count;
			}
			if(count >= npcInfo.getMax())
			{
				int attempts = 0;
				attempts++;
				if(attempts > _template.getNpcSize() * 2)
				{
					throw new IllegalStateException("getNextNpcInfo failed (" + count + ", " + npcInfo.getMax() + ", " + npcInfo.getNpcId() + ")");
				}
			}
		}
		return npcInfo;
	}
	
	private synchronized int getNextRangeId()
	{
		int old = _pointIndex++;
		if(_pointIndex >= _template.getSpawnRangeSize())
		{
			_pointIndex = 0;
		}
		return old;
	}
	
	@Override
	public HardSpawner clone()
	{
		HardSpawner spawnDat = new HardSpawner(_template);
		spawnDat.setAmount(_maximumCount);
		spawnDat.setRespawnDelay(_respawnDelay, _respawnDelayRandom);
		spawnDat.setRespawnTime(0);
		spawnDat.setRespawnCron(getRespawnCron());
		return spawnDat;
	}
	
	private void addToCache(NpcInstance npc)
	{
		npc.setSpawn(null);
		npc.decayMe();
		ArrayDeque<NpcInstance> queue = (ArrayDeque<NpcInstance>) _cache.get(npc.getNpcId());
		if(queue == null)
		{
			queue = new ArrayDeque<>();
			_cache.put(npc.getNpcId(), queue);
		}
		queue.add(npc);
	}
	
	private NpcInstance getCachedNpc(int id)
	{
		Queue queue = _cache.get(id);
		if(queue == null)
		{
			return null;
		}
		NpcInstance npc = (NpcInstance) queue.poll();
		if(npc != null && npc.isDeleted())
		{
			_log.info("Npc: " + id + " is deleted, cant used by cache.");
			return getCachedNpc(id);
		}
		return npc;
	}
	
	public SpawnTemplate getTemplate()
	{
		return _template;
	}
}