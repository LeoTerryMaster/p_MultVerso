package l2.gameserver.taskmanager;

import l2.commons.threading.RunnableImpl;
import l2.commons.threading.SteppingRunnableQueueManager;
import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.model.Player;

import java.util.concurrent.Future;

public class AutoSaveManager extends SteppingRunnableQueueManager
{
	private static final AutoSaveManager _instance = new AutoSaveManager();
	
	private AutoSaveManager()
	{
		super(10000);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 10000, 10000);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				purge();
			}
		}, 60000, 60000);
	}
	
	public static final AutoSaveManager getInstance()
	{
		return _instance;
	}
	
	public Future<?> addAutoSaveTask(Player player)
	{
		long delay = (long) Rnd.get(180, 360) * 1000;
		return scheduleAtFixedRate(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				if(!player.isOnline())
				{
					return;
				}
				player.store(true);
			}
		}, delay, delay);
	}
}