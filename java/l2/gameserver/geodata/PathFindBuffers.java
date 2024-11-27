package l2.gameserver.geodata;

import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntIterator;
import gnu.trove.TIntObjectHashMap;
import l2.commons.text.StrTable;
import l2.gameserver.Config;
import l2.gameserver.utils.Location;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PathFindBuffers
{
	public static final int MIN_MAP_SIZE = 64;
	public static final int STEP_MAP_SIZE = 32;
	public static final int MAX_MAP_SIZE = 512;
	private static final TIntObjectHashMap<PathFindBuffer[]> buffers = new TIntObjectHashMap();
	private static final Lock lock = new ReentrantLock();
	private static int[] sizes = new int[0];
	
	static
	{
		TIntIntHashMap config = new TIntIntHashMap();
		for(String e : Config.PATHFIND_BUFFERS.split(";"))
		{
			String[] k;
			if(e.isEmpty() || (k = e.split("x")).length != 2)
				continue;
			config.put(Integer.valueOf(k[1]).intValue(), Integer.valueOf(k[0]).intValue());
		}
		TIntIntIterator itr = config.iterator();
		while(itr.hasNext())
		{
			itr.advance();
			int size = itr.key();
			int count = itr.value();
			PathFindBuffer[] buff = new PathFindBuffer[count];
			for(int i = 0;i < count;++i)
			{
				buff[i] = new PathFindBuffer(size);
			}
			buffers.put(size, buff);
		}
		sizes = config.keys();
		Arrays.sort(sizes);
	}
	
	private static PathFindBuffer create(int mapSize)
	{
		lock.lock();
		try
		{
			PathFindBuffer buffer;
			PathFindBuffer[] buff = buffers.get(mapSize);
			if(buff != null)
			{
				buffer = new PathFindBuffer(mapSize);
				buff = l2.commons.lang.ArrayUtils.add(buff, buffer);
			}
			else
			{
				buffer = new PathFindBuffer(mapSize);
				PathFindBuffer[] arrpathFindBuffer = {buffer};
				buff = arrpathFindBuffer;
				sizes = ArrayUtils.add(sizes, mapSize);
				Arrays.sort(sizes);
			}
			buffers.put(mapSize, buff);
			buffer.inUse = true;
			PathFindBuffer pathFindBuffer = buffer;
			return pathFindBuffer;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	private static PathFindBuffer get(int mapSize)
	{
		lock.lock();
		try
		{
			PathFindBuffer[] buff = buffers.get(mapSize);
			for(PathFindBuffer buffer : buff)
			{
				if(buffer.inUse)
					continue;
				buffer.inUse = true;
				PathFindBuffer pathFindBuffer = buffer;
				return pathFindBuffer;
			}
			return null;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public static PathFindBuffer alloc(int mapSize)
	{
		if(mapSize > 512)
		{
			return null;
		}
		if((mapSize += 32) < 64)
		{
			mapSize = 64;
		}
		PathFindBuffer buffer = null;
		for(int i = 0;i < sizes.length;++i)
		{
			if(sizes[i] < mapSize)
				continue;
			mapSize = sizes[i];
			buffer = get(mapSize);
			break;
		}
		if(buffer == null)
		{
			for(int size = 64;size < 512;size += 32)
			{
				if(size < mapSize)
					continue;
				mapSize = size;
				buffer = create(mapSize);
				break;
			}
		}
		return buffer;
	}
	
	public static void recycle(PathFindBuffer buffer)
	{
		lock.lock();
		try
		{
			buffer.inUse = false;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public static StrTable getStats()
	{
		StrTable table = new StrTable("PathFind Buffers Stats");
		lock.lock();
		try
		{
			long totalUses = 0;
			long totalPlayable = 0;
			long totalTime = 0;
			int index = 0;
			for(int size : sizes)
			{
				++index;
				int count = 0;
				long uses = 0;
				long playable = 0;
				long itrs = 0;
				long success = 0;
				long overtime = 0;
				long time = 0;
				for(PathFindBuffer buff : buffers.get(size))
				{
					++count;
					uses += buff.totalUses;
					playable += buff.playableUses;
					success += buff.successUses;
					overtime += buff.overtimeUses;
					time += buff.totalTime / 1000000;
					itrs += buff.totalItr;
				}
				totalUses += uses;
				totalPlayable += playable;
				totalTime += time;
				table.set(index, "Size", size);
				table.set(index, "Count", count);
				Object[] arrobject = new Object[1];
				arrobject[0] = uses > 0 ? (double) success * 100.0 / (double) uses : 0.0;
				table.set(index, "Uses (success%)", "" + uses + "(" + String.format("%2.2f", arrobject) + "%)");
				Object[] arrobject2 = new Object[1];
				arrobject2[0] = uses > 0 ? (double) playable * 100.0 / (double) uses : 0.0;
				table.set(index, "Uses, playble", "" + playable + "(" + String.format("%2.2f", arrobject2) + "%)");
				Object[] arrobject3 = new Object[1];
				arrobject3[0] = uses > 0 ? (double) overtime * 100.0 / (double) uses : 0.0;
				table.set(index, "Uses, overtime", "" + overtime + "(" + String.format("%2.2f", arrobject3) + "%)");
				table.set(index, "Iter., avg", uses > 0 ? itrs / uses : 0);
				Object[] arrobject4 = new Object[1];
				arrobject4[0] = uses > 0 ? (double) time / (double) uses : 0.0;
				table.set(index, "Time, avg (ms)", String.format("%1.3f", arrobject4));
			}
			table.addTitle("Uses, total / playable  : " + totalUses + " / " + totalPlayable);
			Object[] arrobject = new Object[1];
			arrobject[0] = totalUses > 0 ? (double) totalTime / (double) totalUses : 0.0;
			table.addTitle("Uses, total time / avg (ms) : " + totalTime + " / " + String.format("%1.3f", arrobject));
		}
		finally
		{
			lock.unlock();
		}
		return table;
	}
	
	public static class GeoNode implements Comparable<GeoNode>
	{
		public static final int NONE = 0;
		public static final int OPENED = 1;
		public static final int CLOSED = -1;
		public int x;
		public int y;
		public short z;
		public short nswe = -1;
		public float totalCost;
		public float costFromStart;
		public float costToEnd;
		public int state;
		public GeoNode parent;
		
		public GeoNode set(int x, int y, short z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}
		
		public boolean isSet()
		{
			return nswe != -1;
		}
		
		public void free()
		{
			nswe = -1;
			costFromStart = 0.0f;
			totalCost = 0.0f;
			costToEnd = 0.0f;
			parent = null;
			state = 0;
		}
		
		public Location getLoc()
		{
			return new Location(x, y, z);
		}
		
		@Override
		public String toString()
		{
			return "[" + x + "," + y + "," + z + "] f: " + totalCost;
		}
		
		@Override
		public int compareTo(GeoNode o)
		{
			if(totalCost > o.totalCost)
			{
				return 1;
			}
			if(totalCost < o.totalCost)
			{
				return -1;
			}
			return 0;
		}
	}
	
	public static class PathFindBuffer
	{
		final int mapSize;
		final GeoNode[][] nodes;
		final Queue<GeoNode> open;
		int offsetX;
		int offsetY;
		boolean inUse;
		long totalUses;
		long successUses;
		long overtimeUses;
		long playableUses;
		long totalTime;
		long totalItr;
		
		public PathFindBuffer(int mapSize)
		{
			open = new PriorityQueue<>(mapSize);
			this.mapSize = mapSize;
			nodes = new GeoNode[mapSize][mapSize];
			for(int i = 0;i < nodes.length;++i)
			{
				for(int j = 0;j < nodes[i].length;++j)
				{
					nodes[i][j] = new GeoNode();
				}
			}
		}
		
		public void free()
		{
			open.clear();
			for(int i = 0;i < nodes.length;++i)
			{
				for(int j = 0;j < nodes[i].length;++j)
				{
					nodes[i][j].free();
				}
			}
		}
	}
}