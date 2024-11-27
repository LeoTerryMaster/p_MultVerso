package services;

import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.ItemFunctions;

public class ExpandInventory extends Functions
{
	public void get()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_EXPAND_INVENTORY_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(player.getInventoryLimit() >= Config.SERVICES_EXPAND_INVENTORY_MAX)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/expand_inventory_max.htm"));
			return;
		}
		if(ItemFunctions.removeItem(player, Config.SERVICES_EXPAND_INVENTORY_ITEM, (long) Config.SERVICES_EXPAND_INVENTORY_PRICE, true) >= (long) Config.SERVICES_EXPAND_INVENTORY_PRICE)
		{
			player.setExpandInventory(player.getExpandInventory() + Config.SERVICES_EXPAND_INVENTORY_SLOT_AMOUNT);
			player.setVar("ExpandInventory", String.valueOf(player.getExpandInventory()), -1);
		}
		else if(Config.SERVICES_EXPAND_INVENTORY_ITEM == 57)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		}
		else
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
		}
		show();
	}
	
	public void show()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_EXPAND_INVENTORY_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/expand_inventory.htm");
		msg.replace("%inven_cap_now%", String.valueOf(player.getInventoryLimit()));
		msg.replace("%inven_limit%", String.valueOf(Config.SERVICES_EXPAND_INVENTORY_MAX));
		msg.replace("%inven_exp_price%", String.valueOf(Config.SERVICES_EXPAND_INVENTORY_PRICE));
		msg.replace("%inven_exp_item%", String.valueOf(Config.SERVICES_EXPAND_INVENTORY_ITEM));
		msg.replace("%inven_exp_slot_amount%", String.valueOf(Config.SERVICES_EXPAND_INVENTORY_SLOT_AMOUNT));
		player.sendPacket(msg);
	}
}