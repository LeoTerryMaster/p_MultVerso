package events.StraightHands;

import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.templates.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class StraightHands extends Functions implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(StraightHands.class);
	private static final String EVENT_NAME = "StraightHands";
	private static final List<Integer> _restrictedFuncHolder = new ArrayList<>();
	private static int[] _restrictedItemsId;
	private static boolean _active;
	
	static
	{
		_active = false;
	}
	
	private static boolean isActive()
	{
		return IsActive("StraightHands");
	}
	
	private static void removeRestrictedItemsStats()
	{
		for(ItemTemplate item : ItemHolder.getInstance().getAllTemplates())
		{
			if(item == null)
				continue;
			for(int rid : _restrictedItemsId)
			{
				if(item.getItemId() != rid)
					continue;
				item.setStatDisabled(true);
				_restrictedFuncHolder.add(item.getItemId());
				_log.info("Event: StraightHands funcs of " + item.getName() + " removed.");
			}
		}
	}
	
	private static void restoreRestrictedItemsStats()
	{
		for(int e : _restrictedFuncHolder)
		{
			ItemTemplate item = ItemHolder.getInstance().getTemplate(e);
			if(item != null)
			{
				item.setStatDisabled(false);
			}
			_log.info("Event: StraightHands funcs of " + item.getName() + " restored.");
		}
	}
	
	private static void unEquipRestrictedItems(Player player)
	{
		for(ItemInstance item : player.getInventory().getItems())
		{
			for(int rid : _restrictedItemsId)
			{
				if(item.getItemId() != rid)
					continue;
				if(item.isEquipped())
				{
					player.getInventory().unEquipItem(item);
				}
				player.sendMessage(new CustomMessage("scripts.events.StraightHands.ItemS1StatsRemoved", player).addItemName(rid));
			}
		}
	}
	
	private static void start()
	{
		_restrictedItemsId = Config.EVENT_StraightHands_Items.clone();
		removeRestrictedItemsStats();
		for(Player player : GameObjectsStorage.getAllPlayers())
		{
			unEquipRestrictedItems(player);
		}
	}
	
	private static void stop()
	{
		restoreRestrictedItemsStats();
	}
	
	public static void OnPlayerEnter(Player player)
	{
		if(_active)
		{
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.StraightHands.AnnounceEventStarted", null);
			unEquipRestrictedItems(player);
		}
	}
	
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive("StraightHands", true))
		{
			start();
			_log.info("Event: StraightHands started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.StraightHands.AnnounceEventStarted", null);
		}
		else
		{
			player.sendMessage("Event StraightHands already started.");
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
		if(SetActive("StraightHands", false))
		{
			stop();
			_log.info("Event: StraightHands stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.StraightHands.AnnounceEventStoped", null);
		}
		else
		{
			player.sendMessage("Event StraightHands not started.");
		}
		_active = false;
		show("admin/events/events.htm", player);
	}
	
	@Override
	public void onLoad()
	{
		if(isActive())
		{
			_active = true;
			start();
			_log.info("Loaded Event: StraightHands [state: activated]");
		}
		else
		{
			_log.info("Loaded Event: StraightHands [state: deactivated]");
		}
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
}