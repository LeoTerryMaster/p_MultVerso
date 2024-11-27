package l2.commons.util;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

public class TroveUtils
{
	public static final TIntArrayList EMPTY_INT_ARRAY_LIST = new TIntArrayListEmpty();
	private static final TIntObjectHashMap EMPTY_INT_OBJECT_MAP = new TIntObjectHashMapEmpty();
	
	public static <V> TIntObjectHashMap<V> emptyIntObjectMap()
	{
		return EMPTY_INT_OBJECT_MAP;
	}
	
	private static class TIntArrayListEmpty extends TIntArrayList
	{
		TIntArrayListEmpty()
		{
			super(0);
		}
		
		@Override
		public void add(int val)
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private static class TIntObjectHashMapEmpty<V> extends TIntObjectHashMap<V>
	{
		TIntObjectHashMapEmpty()
		{
			super(0);
		}
		
		@Override
		public V put(int key, V value)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public V putIfAbsent(int key, V value)
		{
			throw new UnsupportedOperationException();
		}
	}
}