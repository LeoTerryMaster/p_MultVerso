package l2.commons.threading;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class RunnableStatsManager
{
	private static final RunnableStatsManager _instance = new RunnableStatsManager();
	private final Map<Class<?>, ClassStat> classStats = new HashMap();
	private final Lock lock = new ReentrantLock();
	
	public static final RunnableStatsManager getInstance()
	{
		return _instance;
	}
	
	public void handleStats(Class<?> cl, long runTime)
	{
		try
		{
			lock.lock();
			ClassStat stat = classStats.get(cl);
			if(stat == null)
			{
				stat = new ClassStat(cl);
			}
			stat.runCount++;
			ClassStat classStat = stat;
			classStat.runTime = classStat.runTime + runTime;
			if(stat.minTime > runTime)
			{
				stat.minTime = runTime;
			}
			if(stat.maxTime < runTime)
			{
				stat.maxTime = runTime;
			}
		}
		finally
		{
			lock.unlock();
		}
	}
	
	private List<ClassStat> getSortedClassStats()
	{
		List<ClassStat> result;
		try
		{
			lock.lock();
			result = Arrays.asList(classStats.values().toArray(new ClassStat[classStats.size()]));
		}
		finally
		{
			lock.unlock();
		}
		Collections.sort(result, (c1, c2) -> {
			if(c1.maxTime < c2.maxTime)
			{
				return 1;
			}
			if(c1.maxTime == c2.maxTime)
			{
				return 0;
			}
			return -1;
		});
		return result;
	}
	
	public CharSequence getStats()
	{
		StringBuilder list = new StringBuilder();
		List<ClassStat> stats = getSortedClassStats();
		for(ClassStat stat : stats)
		{
			list.append(stat.clazz.getName()).append(":\n");
			list.append("\tRun: ............ ").append(stat.runCount).append("\n");
			list.append("\tTime: ........... ").append(stat.runTime).append("\n");
			list.append("\tMin: ............ ").append(stat.minTime).append("\n");
			list.append("\tMax: ............ ").append(stat.maxTime).append("\n");
			list.append("\tAverage: ........ ").append(stat.runTime / stat.runCount).append("\n");
		}
		return list;
	}
	
	private class ClassStat
	{
		private final Class<?> clazz;
		private long runCount;
		private long runTime;
		private long minTime;
		private long maxTime;
		
		private ClassStat(Class<?> cl)
		{
			runCount = 0;
			runTime = 0;
			minTime = Long.MAX_VALUE;
			maxTime = Long.MIN_VALUE;
			clazz = cl;
			classStats.put(cl, this);
		}
	}
}