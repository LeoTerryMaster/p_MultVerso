package l2.gameserver.taskmanager.tasks;

import l2.gameserver.Config;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.taskmanager.Task;
import l2.gameserver.taskmanager.TaskManager;
import l2.gameserver.taskmanager.TaskTypes;

public class RecommendationUpdateTask extends Task
{
	private static final String NAME = "sp_recommendations";
	
	@Override
	public void init()
	{
		TaskManager.addUniqueTask("sp_recommendations", TaskTypes.TYPE_GLOBAL_TASK, "1", String.format("%02d:%02d:00", Config.REC_FLUSH_HOUR, Config.REC_FLUSH_MINUTE), "");
	}
	
	@Override
	public String getName()
	{
		return "sp_recommendations";
	}
	
	@Override
	public void onTimeElapsed(TaskManager.ExecutedTask task)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			player.updateRecommends();
			player.broadcastUserInfo(true);
		}
	}
}