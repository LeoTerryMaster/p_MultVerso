package l2.commons.collections;

import java.util.Iterator;
import java.util.List;

public class JoinedIterator<E> implements Iterator<E>
{
	private final Iterator<E>[] _iterators;
	private int _currentIteratorIndex;
	private Iterator<E> _currentIterator;
	private Iterator<E> _lastUsedIterator;
	
	public JoinedIterator(List<Iterator<E>> iterators)
	{
		this(iterators.toArray(new Iterator[iterators.size()]));
	}
	
	public JoinedIterator(Iterator... iterators)
	{
		if(iterators == null)
		{
			throw new NullPointerException("Unexpected NULL iterators argument");
		}
		_iterators = iterators;
	}
	
	@Override
	public boolean hasNext()
	{
		updateCurrentIterator();
		return _currentIterator.hasNext();
	}
	
	@Override
	public E next()
	{
		updateCurrentIterator();
		return _currentIterator.next();
	}
	
	@Override
	public void remove()
	{
		updateCurrentIterator();
		_lastUsedIterator.remove();
	}
	
	protected void updateCurrentIterator()
	{
		if(_currentIterator == null)
		{
			_currentIterator = _iterators.length == 0 ? EmptyIterator.getInstance() : _iterators[0];
			_lastUsedIterator = _currentIterator;
		}
		while(!_currentIterator.hasNext() && _currentIteratorIndex < _iterators.length - 1)
		{
			++_currentIteratorIndex;
			_currentIterator = _iterators[_currentIteratorIndex];
		}
	}
}