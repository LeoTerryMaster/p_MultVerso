package l2.gameserver.taskmanager;

import java.util.concurrent.ScheduledFuture;

public abstract class Task
{
	public abstract void init();
	
	public ScheduledFuture<?> launchSpecial(TaskManager.ExecutedTask instance)
	{
		return null;
	}
	
	public abstract String getName();
	
	public abstract void onTimeElapsed(TaskManager.ExecutedTask task);
	
	public void onDestroy()
	{
	}
}