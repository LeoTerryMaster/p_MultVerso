package l2.gameserver.model;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import l2.commons.collections.LazyArrayList;
import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AggroList
{
	private final NpcInstance npc;
	private final TIntObjectHashMap<AggroInfo> hateList = new TIntObjectHashMap();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	
	public AggroList(NpcInstance npc)
	{
		this.npc = npc;
	}
	
	public void addDamageHate(Creature attacker, int damage, int aggro)
	{
		if((damage = Math.max(damage, 0)) == 0 && aggro == 0)
		{
			return;
		}
		writeLock.lock();
		try
		{
			AggroInfo ai = hateList.get(attacker.getObjectId());
			if(ai == null)
			{
				ai = new AggroInfo(attacker);
				hateList.put(attacker.getObjectId(), ai);
			}
			ai.damage += damage;
			ai.hate += aggro;
			ai.damage = Math.max(ai.damage, 0);
			ai.hate = Math.max(ai.hate, 0);
		}
		finally
		{
			writeLock.unlock();
		}
	}
	
	public AggroInfo get(Creature attacker)
	{
		readLock.lock();
		try
		{
			AggroInfo aggroInfo = hateList.get(attacker.getObjectId());
			return aggroInfo;
		}
		finally
		{
			readLock.unlock();
		}
	}
	
	public void remove(Creature attacker, boolean onlyHate)
	{
		writeLock.lock();
		try
		{
			if(!onlyHate)
			{
				hateList.remove(attacker.getObjectId());
				return;
			}
			AggroInfo ai = hateList.get(attacker.getObjectId());
			if(ai != null)
			{
				ai.hate = 0;
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}
	
	public void clear()
	{
		clear(false);
	}
	
	public void clear(boolean onlyHate)
	{
		writeLock.lock();
		try
		{
			if(hateList.isEmpty())
			{
				return;
			}
			if(!onlyHate)
			{
				hateList.clear();
				return;
			}
			TIntObjectIterator itr = hateList.iterator();
			while(itr.hasNext())
			{
				itr.advance();
				AggroInfo ai = (AggroInfo) itr.value();
				ai.hate = 0;
				if(ai.damage != 0)
					continue;
				itr.remove();
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}
	
	public boolean isEmpty()
	{
		readLock.lock();
		try
		{
			boolean bl = hateList.isEmpty();
			return bl;
		}
		finally
		{
			readLock.unlock();
		}
	}
	
	public List<Creature> getHateList(int radius)
	{
		readLock.lock();
		AggroInfo[] hated;
		try
		{
			if(hateList.isEmpty())
			{
				List<Creature> list = Collections.emptyList();
				return list;
			}
			hated = (AggroInfo[]) hateList.getValues((Object[]) new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}
		Arrays.sort(hated, HateComparator.getInstance());
		if(hated[0].hate == 0)
		{
			return Collections.emptyList();
		}
		LazyArrayList<Creature> hateList = new LazyArrayList<>();
		List<Creature> chars = World.getAroundCharacters(npc, radius, radius);
		block4:
		for(int i = 0;i < hated.length;++i)
		{
			AggroInfo ai = hated[i];
			if(ai.hate == 0)
				continue;
			for(Creature cha : chars)
			{
				if(cha.getObjectId() != ai.attackerId)
					continue;
				hateList.add(cha);
				continue block4;
			}
		}
		return hateList;
	}
	
	public Creature getMostHated()
	{
		readLock.lock();
		AggroInfo[] hated;
		try
		{
			if(hateList.isEmpty())
			{
				Creature creature = null;
				return creature;
			}
			hated = (AggroInfo[]) hateList.getValues((Object[]) new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}
		Arrays.sort(hated, HateComparator.getInstance());
		if(hated[0].hate == 0)
		{
			return null;
		}
		List<Creature> chars = World.getAroundCharacters(npc);
		block4:
		for(int i = 0;i < hated.length;++i)
		{
			AggroInfo ai = hated[i];
			if(ai.hate == 0)
				continue;
			for(Creature cha : chars)
			{
				if(cha.getObjectId() != ai.attackerId)
					continue;
				if(cha.isDead())
					continue block4;
				return cha;
			}
		}
		return null;
	}
	
	public Creature getRandomHated()
	{
		readLock.lock();
		AggroInfo[] hated;
		try
		{
			if(hateList.isEmpty())
			{
				Creature creature = null;
				return creature;
			}
			hated = (AggroInfo[]) hateList.getValues((Object[]) new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}
		Arrays.sort(hated, HateComparator.getInstance());
		if(hated[0].hate == 0)
		{
			return null;
		}
		List<Creature> chars = World.getAroundCharacters(npc);
		LazyArrayList<Creature> randomHated = LazyArrayList.newInstance();
		block4:
		for(int i = 0;i < hated.length;++i)
		{
			AggroInfo ai = hated[i];
			if(ai.hate == 0)
				continue;
			for(Creature cha : chars)
			{
				if(cha.getObjectId() != ai.attackerId)
					continue;
				if(cha.isDead())
					continue block4;
				randomHated.add(cha);
				continue block4;
			}
		}
		Creature mostHated = randomHated.isEmpty() ? null : randomHated.get(Rnd.get(randomHated.size()));
		LazyArrayList.recycle(randomHated);
		return mostHated;
	}
	
	public Creature getTopDamager()
	{
		readLock.lock();
		AggroInfo[] hated;
		try
		{
			if(hateList.isEmpty())
			{
				Creature creature = null;
				return creature;
			}
			hated = (AggroInfo[]) hateList.getValues((Object[]) new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}
		Arrays.sort(hated, DamageComparator.getInstance());
		if(hated[0].damage == 0)
		{
			return null;
		}
		List<Creature> chars = World.getAroundCharacters(npc);
		for(int i = 0;i < hated.length;++i)
		{
			AggroInfo ai = hated[i];
			if(ai.damage == 0)
				continue;
			for(Creature cha : chars)
			{
				if(cha.getObjectId() != ai.attackerId)
					continue;
				Creature topDamager = cha;
				return topDamager;
			}
		}
		return null;
	}
	
	public Map<Creature, HateInfo> getCharMap()
	{
		if(isEmpty())
		{
			return Collections.emptyMap();
		}
		HashMap<Creature, HateInfo> aggroMap = new HashMap<>();
		List<Creature> chars = World.getAroundCharacters(npc);
		readLock.lock();
		try
		{
			TIntObjectIterator itr = hateList.iterator();
			block3:
			while(itr.hasNext())
			{
				itr.advance();
				AggroInfo ai = (AggroInfo) itr.value();
				if(ai.damage == 0 && ai.hate == 0)
					continue;
				for(Creature attacker : chars)
				{
					if(attacker.getObjectId() != ai.attackerId)
						continue;
					aggroMap.put(attacker, new HateInfo(attacker, ai));
					continue block3;
				}
			}
		}
		finally
		{
			readLock.unlock();
		}
		return aggroMap;
	}
	
	public Map<Playable, HateInfo> getPlayableMap()
	{
		if(isEmpty())
		{
			return Collections.emptyMap();
		}
		HashMap<Playable, HateInfo> aggroMap = new HashMap<>();
		List<Playable> chars = World.getAroundPlayables(npc);
		readLock.lock();
		try
		{
			TIntObjectIterator itr = hateList.iterator();
			block3:
			while(itr.hasNext())
			{
				itr.advance();
				AggroInfo ai = (AggroInfo) itr.value();
				if(ai.damage == 0 && ai.hate == 0)
					continue;
				for(Playable attacker : chars)
				{
					if(attacker.getObjectId() != ai.attackerId)
						continue;
					aggroMap.put(attacker, new HateInfo(attacker, ai));
					continue block3;
				}
			}
		}
		finally
		{
			readLock.unlock();
		}
		return aggroMap;
	}
	
	public static class HateComparator implements Comparator<DamageHate>
	{
		private static final Comparator<DamageHate> instance = new HateComparator();
		
		HateComparator()
		{
		}
		
		public static Comparator<DamageHate> getInstance()
		{
			return instance;
		}
		
		@Override
		public int compare(DamageHate o1, DamageHate o2)
		{
			int diff = o2.hate - o1.hate;
			return diff == 0 ? o2.damage - o1.damage : diff;
		}
	}
	
	public static class DamageComparator implements Comparator<DamageHate>
	{
		private static final Comparator<DamageHate> instance = new DamageComparator();
		
		DamageComparator()
		{
		}
		
		public static Comparator<DamageHate> getInstance()
		{
			return instance;
		}
		
		@Override
		public int compare(DamageHate o1, DamageHate o2)
		{
			return o2.damage - o1.damage;
		}
	}
	
	public class AggroInfo extends DamageHate
	{
		public final int attackerId;
		
		AggroInfo(Creature attacker)
		{
			attackerId = attacker.getObjectId();
		}
	}
	
	public class HateInfo extends DamageHate
	{
		public final Creature attacker;
		
		HateInfo(Creature attacker, AggroInfo ai)
		{
			this.attacker = attacker;
			hate = ai.hate;
			damage = ai.damage;
		}
	}
	
	private abstract class DamageHate
	{
		public int hate;
		public int damage;
	}
}