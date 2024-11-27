package l2.gameserver.idfactory;

import l2.commons.math.PrimeFinder;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

public class BitSetIDFactory extends IdFactory
{
	private static final Logger _log = LoggerFactory.getLogger(BitSetIDFactory.class);
	private BitSet freeIds;
	private AtomicInteger freeIdCount;
	private AtomicInteger nextFreeId;
	
	protected BitSetIDFactory()
	{
		initialize();
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new BitSetCapacityCheck(), 30000, 30000);
	}
	
	private void initialize()
	{
		try
		{
			freeIds = new BitSet(PrimeFinder.nextPrime(100000));
			freeIds.clear();
			freeIdCount = new AtomicInteger(1879048191);
			for(int usedObjectId : extractUsedObjectIDTable())
			{
				int objectID = usedObjectId - 268435456;
				if(objectID < 0)
				{
					_log.warn("Object ID " + usedObjectId + " in DB is less than minimum ID of " + 268435456);
					continue;
				}
				freeIds.set(usedObjectId - 268435456);
				freeIdCount.decrementAndGet();
			}
			nextFreeId = new AtomicInteger(freeIds.nextClearBit(0));
			initialized = true;
			_log.info("IdFactory: " + freeIds.size() + " id's available.");
		}
		catch(Exception e)
		{
			initialized = false;
			_log.error("BitSet ID Factory could not be initialized correctly!", e);
		}
	}
	
	@Override
	public synchronized void releaseId(int objectID)
	{
		if(objectID - 268435456 > -1)
		{
			freeIds.clear(objectID - 268435456);
			freeIdCount.incrementAndGet();
			super.releaseId(objectID);
		}
		else
		{
			_log.warn("BitSet ID Factory: release objectID " + objectID + " failed (< " + 268435456 + ")");
		}
	}
	
	@Override
	public synchronized int getNextId()
	{
		int newID = nextFreeId.get();
		freeIds.set(newID);
		freeIdCount.decrementAndGet();
		int nextFree = freeIds.nextClearBit(newID);
		if(nextFree < 0)
		{
			nextFree = freeIds.nextClearBit(0);
		}
		if(nextFree < 0)
		{
			if(freeIds.size() < 1879048191)
			{
				increaseBitSetCapacity();
			}
			else
			{
				throw new NullPointerException("Ran out of valid Id's.");
			}
		}
		nextFreeId.set(nextFree);
		return newID + 268435456;
	}
	
	@Override
	public synchronized int size()
	{
		return freeIdCount.get();
	}
	
	protected synchronized int usedIdCount()
	{
		return size() - 268435456;
	}
	
	protected synchronized boolean reachingBitSetCapacity()
	{
		return PrimeFinder.nextPrime(usedIdCount() * 11 / 10) > freeIds.size();
	}
	
	protected synchronized void increaseBitSetCapacity()
	{
		BitSet newBitSet = new BitSet(PrimeFinder.nextPrime(usedIdCount() * 11 / 10));
		newBitSet.or(freeIds);
		freeIds = newBitSet;
	}
	
	public class BitSetCapacityCheck extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(reachingBitSetCapacity())
			{
				increaseBitSetCapacity();
			}
		}
	}
}