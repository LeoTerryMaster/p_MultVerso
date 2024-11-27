package l2.commons.util.concurrent.locks;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class ReentrantReadWriteLock
{
	static final int SHARED_SHIFT = 16;
	static final int SHARED_UNIT = 1 << SHARED_SHIFT;
	static final int MAX_COUNT = (1 << SHARED_SHIFT) - 1;
	static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;
	private static final AtomicIntegerFieldUpdater<ReentrantReadWriteLock> stateUpdater = AtomicIntegerFieldUpdater.newUpdater(ReentrantReadWriteLock.class, "state");
	transient ThreadLocalHoldCounter readHolds;
	transient HoldCounter cachedHoldCounter;
	private Thread owner;
	private volatile int state;
	
	public ReentrantReadWriteLock()
	{
		readHolds = new ThreadLocalHoldCounter();
		setState(0);
	}
	
	static int sharedCount(int c)
	{
		return c >>> SHARED_SHIFT;
	}
	
	static int exclusiveCount(int c)
	{
		return c & EXCLUSIVE_MASK;
	}
	
	private final int getState()
	{
		return state;
	}
	
	private void setState(int newState)
	{
		state = newState;
	}
	
	private boolean compareAndSetState(int expect, int update)
	{
		return stateUpdater.compareAndSet(this, expect, update);
	}
	
	private Thread getExclusiveOwnerThread()
	{
		return owner;
	}
	
	private void setExclusiveOwnerThread(Thread thread)
	{
		owner = thread;
	}
	
	public void writeLock()
	{
		Thread current = Thread.currentThread();
		for(;;)
		{
			int c = getState();
			int w = exclusiveCount(c);
			if(c != 0)
			{
				// (Note: if c != 0 and w == 0 then shared count != 0)
				if(w == 0 || current != getExclusiveOwnerThread())
					continue;
				if(w + exclusiveCount(1) > MAX_COUNT)
					throw new Error("Maximum lock count exceeded");
			}
			if(compareAndSetState(c, c + 1))
			{
				setExclusiveOwnerThread(current);
				return;
			}
		}
	}
	
	public boolean tryWriteLock()
	{
		Thread current = Thread.currentThread();
		int c = getState();
		if(c != 0)
		{
			int w = exclusiveCount(c);
			if(w == 0 || current != getExclusiveOwnerThread())
				return false;
			if(w == MAX_COUNT)
				throw new Error("Maximum lock count exceeded");
		}
		if(!compareAndSetState(c, c + 1))
			return false;
		setExclusiveOwnerThread(current);
		return true;
	}
	
	final boolean tryReadLock()
	{
		Thread current = Thread.currentThread();
		int c = getState();
		int w = exclusiveCount(c);
		if(w != 0 && getExclusiveOwnerThread() != current)
			return false;
		if(sharedCount(c) == MAX_COUNT)
			throw new Error("Maximum lock count exceeded");
		if(compareAndSetState(c, c + SHARED_UNIT))
		{
			HoldCounter rh = cachedHoldCounter;
			if(rh == null || rh.tid != current.getId())
				cachedHoldCounter = rh = readHolds.get();
			rh.count++;
			return true;
		}
		return false;
	}
	
	public void readLock()
	{
		Thread current = Thread.currentThread();
		HoldCounter rh = cachedHoldCounter;
		if(rh == null || rh.tid != current.getId())
			rh = readHolds.get();
		for(;;)
		{
			int c = getState();
			int w = exclusiveCount(c);
			if(w != 0 && getExclusiveOwnerThread() != current)
				continue;
			if(sharedCount(c) == MAX_COUNT)
				throw new Error("Maximum lock count exceeded");
			if(compareAndSetState(c, c + SHARED_UNIT))
			{
				cachedHoldCounter = rh; // cache for release
				rh.count++;
				return;
			}
		}
	}
	
	public void writeUnlock()
	{
		int nextc = getState() - 1;
		if(Thread.currentThread() != getExclusiveOwnerThread())
			throw new IllegalMonitorStateException();
		if(exclusiveCount(nextc) == 0)
		{
			setExclusiveOwnerThread(null);
			setState(nextc);
			return;
		}
		else
		{
			setState(nextc);
			return;
		}
	}
	
	public void readUnlock()
	{
		HoldCounter rh = cachedHoldCounter;
		Thread current = Thread.currentThread();
		if(rh == null || rh.tid != current.getId())
			rh = readHolds.get();
		if(rh.tryDecrement() <= 0)
			throw new IllegalMonitorStateException();
		for(;;)
		{
			int c = getState();
			int nextc = c - SHARED_UNIT;
			if(compareAndSetState(c, nextc))
				return;
		}
	}
	
	static final class HoldCounter
	{
		// Use id, not reference, to avoid garbage retention
		final long tid = Thread.currentThread().getId();
		int count;
		
		int tryDecrement()
		{
			int c = count;
			if(c > 0)
				count = c - 1;
			return c;
		}
	}
	
	static final class ThreadLocalHoldCounter extends ThreadLocal<HoldCounter>
	{
		@Override
		public HoldCounter initialValue()
		{
			return new HoldCounter();
		}
	}
}