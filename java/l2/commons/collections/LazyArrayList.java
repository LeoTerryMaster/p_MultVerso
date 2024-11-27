package l2.commons.collections;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

public class LazyArrayList<E> implements List<E>, RandomAccess, Cloneable, Serializable
{
	private static final long serialVersionUID = 8683452581122892189L;
	private static final int POOL_SIZE = Integer.parseInt(System.getProperty("lazyarraylist.poolsize", "-1"));
	private static final ObjectPool POOL;
	private static final int L = 8;
	private static final int H = 1024;
	
	static
	{
		POOL = new GenericObjectPool(new LazyArrayList.PoolableLazyArrayListFactory(), POOL_SIZE, (byte) 2, 0L, -1);
	}
	
	protected transient Object[] elementData;
	protected transient int size;
	protected transient int capacity;
	
	public LazyArrayList(int initialCapacity)
	{
		size = 0;
		capacity = 8;
		if(initialCapacity < H)
		{
			while(capacity < initialCapacity)
			{
				capacity <<= 1;
			}
		}
		else
		{
			capacity = initialCapacity;
		}
	}
	
	public LazyArrayList()
	{
		this(L);
	}
	
	public static <E> LazyArrayList<E> newInstance()
	{
		try
		{
			return (LazyArrayList) POOL.borrowObject();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new LazyArrayList();
		}
	}
	
	public static <E> void recycle(LazyArrayList<E> obj)
	{
		try
		{
			POOL.returnObject(obj);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean add(E element)
	{
		ensureCapacity(size + 1);
		elementData[size++] = element;
		return true;
	}
	
	@Override
	public E set(int index, E element)
	{
		E e = null;
		if(index >= 0 && index < size)
		{
			e = (E) elementData[index];
			elementData[index] = element;
		}
		
		return e;
	}
	
	@Override
	public void add(int index, E element)
	{
		if(index >= 0 && index < size)
		{
			ensureCapacity(size + 1);
			System.arraycopy(elementData, index, elementData, index + 1, size - index);
			elementData[index] = element;
			++size;
		}
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends E> c)
	{
		if(c != null && !c.isEmpty())
		{
			if(index >= 0 && index < size)
			{
				Object[] a = c.toArray();
				int numNew = a.length;
				ensureCapacity(size + numNew);
				int numMoved = size - index;
				if(numMoved > 0)
				{
					System.arraycopy(elementData, index, elementData, index + numNew, numMoved);
				}
				
				System.arraycopy(a, 0, elementData, index, numNew);
				size += numNew;
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	protected void ensureCapacity(int newSize)
	{
		if(newSize > capacity)
		{
			if(newSize < H)
			{
				while(capacity < newSize)
				{
					capacity <<= 1;
				}
			}
			else
			{
				while(capacity < newSize)
				{
					capacity = capacity * 3 / 2;
				}
			}
			
			Object[] elementDataResized = new Object[capacity];
			if(elementData != null)
			{
				System.arraycopy(elementData, 0, elementDataResized, 0, size);
			}
			
			elementData = elementDataResized;
		}
		else if(elementData == null)
		{
			elementData = new Object[capacity];
		}
	}
	
	@Override
	public E remove(int index)
	{
		E e = null;
		if(index >= 0 && index < size)
		{
			--size;
			e = (E) elementData[index];
			elementData[index] = elementData[size];
			elementData[size] = null;
			trim();
		}
		
		return e;
	}
	
	@Override
	public boolean remove(Object o)
	{
		if(size == 0)
		{
			return false;
		}
		else
		{
			int index = -1;
			
			for(int i = 0;i < size;++i)
			{
				if(elementData[i] == o)
				{
					index = i;
					break;
				}
			}
			
			if(index == -1)
			{
				return false;
			}
			else
			{
				--size;
				elementData[index] = elementData[size];
				elementData[size] = null;
				trim();
				return true;
			}
		}
	}
	
	@Override
	public boolean contains(Object o)
	{
		if(size == 0)
		{
			return false;
		}
		else
		{
			for(int i = 0;i < size;++i)
			{
				if(elementData[i] == o)
				{
					return true;
				}
			}
			
			return false;
		}
	}
	
	@Override
	public int indexOf(Object o)
	{
		if(size == 0)
		{
			return -1;
		}
		else
		{
			int index = -1;
			
			for(int i = 0;i < size;++i)
			{
				if(elementData[i] == o)
				{
					index = i;
					break;
				}
			}
			
			return index;
		}
	}
	
	@Override
	public int lastIndexOf(Object o)
	{
		if(size == 0)
		{
			return -1;
		}
		else
		{
			int index = -1;
			
			for(int i = 0;i < size;++i)
			{
				if(elementData[i] == o)
				{
					index = i;
				}
			}
			
			return index;
		}
	}
	
	protected void trim()
	{
	}
	
	@Override
	public E get(int index)
	{
		return size > 0 && index >= 0 && index < size ? (E) elementData[index] : null;
	}
	
	@Override
	public Object clone()
	{
		LazyArrayList<E> clone = new LazyArrayList<>();
		if(size > 0)
		{
			clone.capacity = capacity;
			clone.elementData = new Object[elementData.length];
			System.arraycopy(elementData, 0, clone.elementData, 0, size);
		}
		
		return clone;
	}
	
	@Override
	public void clear()
	{
		if(size != 0)
		{
			for(int i = 0;i < size;++i)
			{
				elementData[i] = null;
			}
			
			size = 0;
			trim();
		}
	}
	
	@Override
	public int size()
	{
		return size;
	}
	
	@Override
	public boolean isEmpty()
	{
		return size == 0;
	}
	
	public int capacity()
	{
		return capacity;
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		if(c != null && !c.isEmpty())
		{
			Object[] a = c.toArray();
			int numNew = a.length;
			ensureCapacity(size + numNew);
			System.arraycopy(a, 0, elementData, size, numNew);
			size += numNew;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public boolean containsAll(Collection<?> c)
	{
		if(c == null)
		{
			return false;
		}
		else if(c.isEmpty())
		{
			return true;
		}
		else
		{
			Iterator e = c.iterator();
			
			do
			{
				if(!e.hasNext())
				{
					return true;
				}
			}
			while(contains(e.next()));
			
			return false;
		}
	}
	
	@Override
	public boolean retainAll(Collection<?> c)
	{
		if(c == null)
		{
			return false;
		}
		else
		{
			boolean modified = false;
			Iterator e = iterator();
			
			while(e.hasNext())
			{
				if(!c.contains(e.next()))
				{
					e.remove();
					modified = true;
				}
			}
			
			return modified;
		}
	}
	
	@Override
	public boolean removeAll(Collection<?> c)
	{
		if(c != null && !c.isEmpty())
		{
			boolean modified = false;
			Iterator e = iterator();
			
			while(e.hasNext())
			{
				if(c.contains(e.next()))
				{
					e.remove();
					modified = true;
				}
			}
			
			return modified;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public Object[] toArray()
	{
		Object[] r = new Object[size];
		if(size > 0)
		{
			System.arraycopy(elementData, 0, r, 0, size);
		}
		
		return r;
	}
	
	@Override
	public <T> T[] toArray(T[] a)
	{
		T[] r = a.length >= size ? a : (T[]) Array.newInstance(a.getClass().getComponentType(), size);
		if(size > 0)
		{
			System.arraycopy(elementData, 0, r, 0, size);
		}
		
		if(r.length > size)
		{
			r[size] = null;
		}
		
		return r;
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return new LazyArrayList.LazyItr();
	}
	
	@Override
	public ListIterator<E> listIterator()
	{
		return new LazyArrayList.LazyListItr(0);
	}
	
	@Override
	public ListIterator<E> listIterator(int index)
	{
		return new LazyArrayList.LazyListItr(index);
	}
	
	@Override
	public String toString()
	{
		if(size == 0)
		{
			return "[]";
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			
			for(int i = 0;i < size;++i)
			{
				Object e = elementData[i];
				sb.append(e == this ? "this" : e);
				if(i == size - 1)
				{
					sb.append(']').toString();
				}
				else
				{
					sb.append(", ");
				}
			}
			
			return sb.toString();
		}
	}
	
	@Override
	public List<E> subList(int fromIndex, int toIndex)
	{
		throw new UnsupportedOperationException();
	}
	
	private static class PoolableLazyArrayListFactory implements PoolableObjectFactory
	{
		@Override
		public Object makeObject() throws Exception
		{
			return new LazyArrayList();
		}
		
		@Override
		public void destroyObject(Object obj) throws Exception
		{
			((LazyArrayList) obj).clear();
		}
		
		@Override
		public boolean validateObject(Object obj)
		{
			return true;
		}
		
		@Override
		public void activateObject(Object obj) throws Exception
		{
		}
		
		@Override
		public void passivateObject(Object obj) throws Exception
		{
			((LazyArrayList) obj).clear();
		}
	}
	
	private class LazyListItr extends LazyArrayList<E>.LazyItr implements ListIterator<E>
	{
		LazyListItr(int index)
		{
			cursor = index;
		}
		
		@Override
		public boolean hasPrevious()
		{
			return cursor > 0;
		}
		
		@Override
		public E previous()
		{
			int i = cursor - 1;
			E previous = get(i);
			lastRet = cursor = i;
			return previous;
		}
		
		@Override
		public int nextIndex()
		{
			return cursor;
		}
		
		@Override
		public int previousIndex()
		{
			return cursor - 1;
		}
		
		@Override
		public void set(E e)
		{
			if(lastRet == -1)
			{
				throw new IllegalStateException();
			}
			else
			{
				LazyArrayList.this.set(lastRet, e);
			}
		}
		
		@Override
		public void add(E e)
		{
			LazyArrayList.this.add(cursor++, e);
			lastRet = -1;
		}
	}
	
	private class LazyItr implements Iterator<E>
	{
		int cursor;
		int lastRet;
		
		private LazyItr()
		{
			cursor = 0;
			lastRet = -1;
		}
		
		@Override
		public boolean hasNext()
		{
			return cursor < size();
		}
		
		@Override
		public E next()
		{
			E next = get(cursor);
			lastRet = cursor++;
			return next;
		}
		
		@Override
		public void remove()
		{
			if(lastRet == -1)
			{
				throw new IllegalStateException();
			}
			else
			{
				LazyArrayList.this.remove(lastRet);
				if(lastRet < cursor)
				{
					--cursor;
				}
				
				lastRet = -1;
			}
		}
	}
}