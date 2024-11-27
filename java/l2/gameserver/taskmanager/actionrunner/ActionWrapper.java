package l2.gameserver.taskmanager.actionrunner;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public abstract class ActionWrapper extends RunnableImpl
{
	private static final Logger _log = LoggerFactory.getLogger(ActionWrapper.class);
	private final String _name;
	private Future<?> _scheduledFuture;
	
	public ActionWrapper(String name)
	{
		_name = name;
	}
	
	public void schedule(long time)
	{
		_scheduledFuture = ThreadPoolManager.getInstance().schedule(this, time);
	}
	
	public void cancel()
	{
		if(_scheduledFuture != null)
		{
			_scheduledFuture.cancel(true);
			_scheduledFuture = null;
		}
	}
	
	public abstract void runImpl0() throws Exception;
	
	@Override
	public void runImpl()
	{
		try
		{
			runImpl0();
		}
		catch(Exception e)
		{
			_log.info("ActionWrapper: Exception: " + e + "; name: " + _name, e);
		}
		finally
		{
			ActionRunner.getInstance().remove(_name, this);
			_scheduledFuture = null;
		}
	}
	
	public String getName()
	{
		return _name;
	}
}