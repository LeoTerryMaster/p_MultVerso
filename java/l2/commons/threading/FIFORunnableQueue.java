package l2.commons.threading;

import java.util.Queue;

public abstract class FIFORunnableQueue<T extends Runnable> implements Runnable
{
	private static final int NONE = 0;
	private static final int QUEUED = 1;
	private static final int RUNNING = 2;
	private final Queue<T> _queue;
	private int _state;
	
	public FIFORunnableQueue(Queue<T> queue)
	{
		_queue = queue;
	}
	
	public void execute(T t)
	{
		_queue.add(t);
		FIFORunnableQueue fIFORunnableQueue = this;
		synchronized(fIFORunnableQueue)
		{
			if(_state != 0)
			{
				return;
			}
			_state = 1;
		}
		execute();
	}
	
	protected abstract void execute();
	
	public void clear()
	{
		_queue.clear();
	}
	
	@Override
	public void run()
	{
		FIFORunnableQueue fIFORunnableQueue = this;
		synchronized(fIFORunnableQueue)
		{
			if(_state == 2)
			{
				return;
			}
			_state = 2;
		}
		Runnable t;
		try
		{
			while((t = _queue.poll()) != null)
			{
				t.run();
			}
		}
		finally
		{
			t = this;
			synchronized(t)
			{
				_state = 0;
			}
		}
	}
}