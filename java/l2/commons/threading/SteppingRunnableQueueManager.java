package l2.commons.threading;

import l2.commons.collections.LazyArrayList;
import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SteppingRunnableQueueManager implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(SteppingRunnableQueueManager.class);
	protected final long tickPerStepInMillis;
	private final List<SteppingScheduledFuture<?>> queue = new CopyOnWriteArrayList();
	private final AtomicBoolean isRunning = new AtomicBoolean();
	
	public SteppingRunnableQueueManager(long tickPerStepInMillis)
	{
		this.tickPerStepInMillis = tickPerStepInMillis;
	}
	
	public SteppingScheduledFuture<?> schedule(Runnable r, long delay)
	{
		return schedule(r, delay, delay, false);
	}
	
	public SteppingScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay)
	{
		return schedule(r, initial, delay, true);
	}
	
	private SteppingScheduledFuture<?> schedule(Runnable r, long initial, long delay, boolean isPeriodic)
	{
		long initialStepping = getStepping(initial);
		long stepping = getStepping(delay);
		SteppingScheduledFuture sr = new SteppingScheduledFuture(r, initialStepping, stepping, isPeriodic);
		queue.add(sr);
		return sr;
	}
	
	private long getStepping(long delay)
	{
		return (delay = Math.max(0, delay)) % tickPerStepInMillis > tickPerStepInMillis / 2 ? delay / tickPerStepInMillis + 1 : delay < tickPerStepInMillis ? 1 : delay / tickPerStepInMillis;
	}
	
	@Override
	public void run()
	{
		try
		{
			if(!isRunning.compareAndSet(false, true))
			{
				_log.warn("Slow running queue, managed by " + this + ", queue size : " + queue.size() + "!");
				return;
			}
			try
			{
				if(queue.isEmpty())
				{
					return;
				}
				for(SteppingScheduledFuture sr : queue)
				{
					if(sr.isDone())
						continue;
					sr.run();
				}
			}
			finally
			{
				isRunning.set(false);
			}
		}
		catch(Throwable e)
		{
			_log.error("Exception in stepped queue manager", e);
		}
	}
	
	public void purge()
	{
		LazyArrayList purge = LazyArrayList.newInstance();
		for(SteppingScheduledFuture sr : queue)
		{
			if(!sr.isDone())
				continue;
			purge.add(sr);
		}
		queue.removeAll(purge);
		LazyArrayList.recycle(purge);
	}
	
	public CharSequence getStats()
	{
		StringBuilder list = new StringBuilder();
		TreeMap<String, MutableLong> stats = new TreeMap<>();
		int total = 0;
		int done = 0;
		for(SteppingScheduledFuture sr : queue)
		{
			if(sr.isDone())
			{
				++done;
				continue;
			}
			++total;
			MutableLong count = stats.get(sr.r.getClass().getName());
			if(count == null)
			{
				count = new MutableLong(1);
				stats.put(sr.r.getClass().getName(), count);
				continue;
			}
			count.increment();
		}
		for(Map.Entry e : stats.entrySet())
		{
			list.append("\t").append((String) e.getKey()).append(" : ").append(((MutableLong) e.getValue()).longValue()).append("\n");
		}
		list.append("Scheduled: ....... ").append(total).append("\n");
		list.append("Done/Cancelled: .. ").append(done).append("\n");
		return list;
	}
	
	public class SteppingScheduledFuture<V> implements RunnableScheduledFuture<V>
	{
		private final Runnable r;
		private final long stepping;
		private final boolean isPeriodic;
		private long step;
		private boolean isCancelled;
		
		public SteppingScheduledFuture(Runnable r, long initial, long stepping, boolean isPeriodic)
		{
			this.r = r;
			step = initial;
			this.stepping = stepping;
			this.isPeriodic = isPeriodic;
		}
		
		@Override
		public void run()
		{
			if(--step == 0)
			{
				try
				{
					r.run();
				}
				catch(Throwable e)
				{
					_log.error("Exception in a Runnable execution:", e);
				}
				finally
				{
					if(isPeriodic)
					{
						step = stepping;
					}
				}
			}
		}
		
		@Override
		public boolean isDone()
		{
			return isCancelled || !isPeriodic && step == 0;
		}
		
		@Override
		public boolean isCancelled()
		{
			return isCancelled;
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning)
		{
			isCancelled = true;
			return true;
		}
		
		@Override
		public V get() throws InterruptedException, ExecutionException
		{
			return null;
		}
		
		@Override
		public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
		{
			return null;
		}
		
		@Override
		public long getDelay(TimeUnit unit)
		{
			return unit.convert(step * tickPerStepInMillis, TimeUnit.MILLISECONDS);
		}
		
		@Override
		public int compareTo(Delayed o)
		{
			return 0;
		}
		
		@Override
		public boolean isPeriodic()
		{
			return isPeriodic;
		}
	}
}