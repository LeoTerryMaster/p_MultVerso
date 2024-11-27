package events.PcCafePointsExchange;

import l2.gameserver.model.Player;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PcCafePointsExchange extends Functions implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(PcCafePointsExchange.class);
	private static final String EVENT_NAME = "PcCafePointsExchange";
	private static final int EVENT_MANAGER_ID = 32130;
	private static final List<SimpleSpawner> _spawns = new ArrayList<>();
	
	private static boolean isActive()
	{
		return IsActive("PcCafePointsExchange");
	}
	
	private void spawnEventManagers()
	{
		int[][] EVENT_MANAGERS = {{15880, 143704, -2888, 0}, {83656, 148440, -3430, 32768}, {147272, 27416, -2228, 16384}, {42808, -47896, -822, 49152}};
		SpawnNPCs(32130, EVENT_MANAGERS, _spawns);
	}
	
	private void unSpawnEventManagers()
	{
		deSpawnNPCs(_spawns);
	}
	
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive("PcCafePointsExchange", true))
		{
			spawnEventManagers();
			System.out.println("Event: 'PcCafePointsExchange' started.");
		}
		else
		{
			player.sendMessage("Event 'PcCafePointsExchange' already started.");
		}
		show("admin/events/events.htm", player);
	}
	
	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive("PcCafePointsExchange", false))
		{
			unSpawnEventManagers();
			System.out.println("Event: 'PcCafePointsExchange' stopped.");
		}
		else
		{
			player.sendMessage("Event: 'PcCafePointsExchange' not started.");
		}
		show("admin/events/events.htm", player);
	}
	
	@Override
	public void onLoad()
	{
		if(isActive())
		{
			spawnEventManagers();
			_log.info("Loaded Event: PcCafePointsExchange [state: activated]");
		}
		else
		{
			_log.info("Loaded Event: PcCafePointsExchange [state: deactivated]");
		}
	}
	
	@Override
	public void onReload()
	{
		unSpawnEventManagers();
	}
	
	@Override
	public void onShutdown()
	{
		unSpawnEventManagers();
	}
}