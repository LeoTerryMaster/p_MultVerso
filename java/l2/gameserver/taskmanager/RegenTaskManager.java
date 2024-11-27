package l2.gameserver.taskmanager;

import l2.commons.threading.RunnableImpl;
import l2.commons.threading.SteppingRunnableQueueManager;
import l2.gameserver.ThreadPoolManager;

public class RegenTaskManager extends SteppingRunnableQueueManager
{
	private static final RegenTaskManager _instance = new RegenTaskManager();
	
	private RegenTaskManager()
	{
		super(1000);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000, 1000);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				purge();
			}
		}, 10000, 10000);
	}
	
	public static final RegenTaskManager getInstance()
	{
		return _instance;
	}
}