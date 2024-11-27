package l2.commons.collections;

import java.util.Iterator;

public class EmptyIterator<E> implements Iterator<E>
{
	private static final Iterator INSTANCE = new EmptyIterator();
	
	private EmptyIterator()
	{
	}
	
	public static <E> Iterator<E> getInstance()
	{
		return INSTANCE;
	}
	
	@Override
	public boolean hasNext()
	{
		return false;
	}
	
	@Override
	public E next()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}