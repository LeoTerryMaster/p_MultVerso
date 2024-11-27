package l2.gameserver.taskmanager;

import l2.commons.threading.RunnableImpl;
import l2.commons.threading.SteppingRunnableQueueManager;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;

public class AiTaskManager extends SteppingRunnableQueueManager
{
	private static final long TICK = 250;
	private static final AiTaskManager[] _instances = new AiTaskManager[Config.AI_TASK_MANAGER_COUNT];
	private static int randomizer;
	
	static
	{
		for(int i = 0;i < _instances.length;++i)
		{
			_instances[i] = new AiTaskManager();
		}
		randomizer = 0;
	}
	
	private AiTaskManager()
	{
		super(250);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, Rnd.get(250), 250);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				purge();
			}
		}, 60000, 60000);
	}
	
	public static final AiTaskManager getInstance()
	{
		return _instances[randomizer++ & _instances.length - 1];
	}
	
	public CharSequence getStats(int num)
	{
		return _instances[num].getStats();
	}
}