package l2.commons.lang.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class HardReferences
{
	private static final HardReference<?> EMPTY_REF = new EmptyReferencedHolder(null);
	
	private HardReferences()
	{
	}
	
	public static <T> HardReference<T> emptyRef()
	{
		return (HardReference<T>) EMPTY_REF;
	}
	
	public static <T> Collection<T> unwrap(Collection<HardReference<T>> refs)
	{
		ArrayList<T> result = new ArrayList<>(refs.size());
		for(HardReference<T> ref : refs)
		{
			T obj = ref.get();
			if(obj == null)
				continue;
			result.add(obj);
		}
		return result;
	}
	
	public static <T> Iterable<T> iterate(Iterable<HardReference<T>> refs)
	{
		return new WrappedIterable<>(refs);
	}
	
	private static class WrappedIterable<T> implements Iterable<T>
	{
		final Iterable<HardReference<T>> refs;
		
		WrappedIterable(Iterable<HardReference<T>> refs)
		{
			this.refs = refs;
		}
		
		@Override
		public Iterator<T> iterator()
		{
			return new WrappedIterator<>(refs.iterator());
		}
		
		private static class WrappedIterator<T> implements Iterator<T>
		{
			final Iterator<HardReference<T>> iterator;
			
			WrappedIterator(Iterator<HardReference<T>> iterator)
			{
				this.iterator = iterator;
			}
			
			@Override
			public boolean hasNext()
			{
				return iterator.hasNext();
			}
			
			@Override
			public T next()
			{
				return iterator.next().get();
			}
			
			@Override
			public void remove()
			{
				iterator.remove();
			}
		}
	}
	
	private static class EmptyReferencedHolder extends AbstractHardReference<Object>
	{
		public EmptyReferencedHolder(Object reference)
		{
			super(reference);
		}
	}
}