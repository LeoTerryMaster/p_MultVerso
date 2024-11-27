package events.CofferofShadows;

import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2.gameserver.model.Player;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CofferofShadows extends Functions implements ScriptFile, OnPlayerEnterListener
{
	private static final int COFFER_PRICE = 50000;
	private static final int COFFER_ID = 8659;
	private static final int EVENT_MANAGER_ID = 32091;
	private static final List<SimpleSpawner> _spawns = new ArrayList<>();
	private static final Logger _log = LoggerFactory.getLogger(CofferofShadows.class);
	private static final int[] buycoffer_counts = {1, 5, 10, 50};
	private static boolean _active;
	
	private static boolean isActive()
	{
		return IsActive("CofferofShadows");
	}
	
	private void spawnEventManagers()
	{
		int[][] EVENT_MANAGERS = {{-14823, 123567, -3143, 8192}, {-83159, 150914, -3155, 49152}, {18600, 145971, -3095, 40960}, {82158, 148609, -3493, 60}, {110992, 218753, -3568, 0}, {116339, 75424, -2738, 0}, {81140, 55218, -1551, 32768}, {147148, 27401, -2231, 2300}, {43532, -46807, -823, 31471}, {87765, -141947, -1367, 6500}, {147154, -55527, -2807, 61300}};
		SpawnNPCs(EVENT_MANAGER_ID, EVENT_MANAGERS, _spawns);
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
		if(SetActive("CofferofShadows", true))
		{
			spawnEventManagers();
			System.out.println("Event: Coffer of Shadows started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.CofferofShadows.AnnounceEventStarted", null);
		}
		else
		{
			player.sendMessage("Event 'Coffer of Shadows' already started.");
		}
		_active = true;
		show("admin/events/events.htm", player);
	}
	
	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive("CofferofShadows", false))
		{
			unSpawnEventManagers();
			System.out.println("Event: Coffer of Shadows stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.CofferofShadows.AnnounceEventStoped", null);
		}
		else
		{
			player.sendMessage("Event 'Coffer of Shadows' not started.");
		}
		_active = false;
		show("admin/events/events.htm", player);
	}
	
	public void buycoffer(String[] var)
	{
		Player player = getSelf();
		if(!player.isQuestContinuationPossible(true))
		{
			return;
		}
		if(!NpcInstance.canBypassCheck(player, player.getLastNpc()))
		{
			return;
		}
		int coffer_count = 1;
		try
		{
			coffer_count = Integer.valueOf(var[0]);
		}
		catch(Exception e)
		{
			
		}
		long need_adena = (long) ((double) COFFER_PRICE * Config.EVENT_CofferOfShadowsPriceRate * (double) coffer_count);
		if(player.getAdena() < need_adena)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		player.reduceAdena(need_adena, true);
		Functions.addItem(player, COFFER_ID, (long) coffer_count);
	}
	
	public String DialogAppend_32091(Integer val)
	{
		if(val != 0)
		{
			return "";
		}
		String append = "";
		for(int cnt : buycoffer_counts)
		{
			String price = Util.formatAdena((long) ((double) COFFER_PRICE * Config.EVENT_CofferOfShadowsPriceRate * (double) cnt));
			append = append + "<a action=\"bypass -h scripts_events.CofferofShadows.CofferofShadows:buycoffer " + cnt + "\">";
			append = cnt == 1 ? append + new CustomMessage("scripts.events.CofferofShadows.buycoffer", getSelf()).addString(price) : append + new CustomMessage("scripts.events.CofferofShadows.buycoffers", getSelf()).addNumber((long) cnt).addString(price);
			append = append + "</a><br>";
		}
		return append;
	}
	
	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);
		if(isActive())
		{
			_active = true;
			spawnEventManagers();
			_log.info("Loaded Event: Coffer of Shadows [state: activated]");
		}
		else
		{
			_log.info("Loaded Event: Coffer of Shadows [state: deactivated]");
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
	
	@Override
	public void onPlayerEnter(Player player)
	{
		if(_active)
		{
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.CofferofShadows.AnnounceEventStarted", null);
		}
	}
}