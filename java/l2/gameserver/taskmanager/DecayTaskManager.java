package l2.gameserver.taskmanager;

import l2.commons.threading.RunnableImpl;
import l2.commons.threading.SteppingRunnableQueueManager;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.model.Creature;

import java.util.concurrent.Future;

public class DecayTaskManager extends SteppingRunnableQueueManager
{
	private static final DecayTaskManager _instance = new DecayTaskManager();
	
	private DecayTaskManager()
	{
		super(500);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 500, 500);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				purge();
			}
		}, 60000, 60000);
	}
	
	public static final DecayTaskManager getInstance()
	{
		return _instance;
	}
	
	public Future<?> addDecayTask(Creature actor, long delay)
	{
		return schedule(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				actor.doDecay();
			}
		}, delay);
	}
}