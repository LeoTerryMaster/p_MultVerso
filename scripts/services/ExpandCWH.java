package services;

import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.ItemFunctions;

public class ExpandCWH extends Functions
{
	public void get()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_EXPAND_CWH_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(player.getClan() == null)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/expand_cwh_clanrestriction.htm"));
			return;
		}
		if(ItemFunctions.removeItem(player, Config.SERVICES_EXPAND_CWH_ITEM, (long) Config.SERVICES_EXPAND_CWH_PRICE, true) >= (long) Config.SERVICES_EXPAND_CWH_PRICE)
		{
			player.getClan().setWhBonus(player.getClan().getWhBonus() + Config.SERVICES_EXPAND_CWH_SLOT_AMOUNT);
			player.sendMessage("Warehouse capacity is now " + (Config.WAREHOUSE_SLOTS_CLAN + player.getClan().getWhBonus()));
		}
		else if(Config.SERVICES_EXPAND_CWH_ITEM == 57)
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
		if(!Config.SERVICES_EXPAND_WAREHOUSE_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(player.getClan() == null)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/expand_cwh_clanrestriction.htm"));
			return;
		}
		NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/expand_cwh.htm");
		msg.replace("%cwh_cap_now%", String.valueOf(Config.WAREHOUSE_SLOTS_CLAN + player.getClan().getWhBonus()));
		msg.replace("%cwh_exp_price%", String.valueOf(Config.SERVICES_EXPAND_CWH_PRICE));
		msg.replace("%cwh_exp_item%", String.valueOf(Config.SERVICES_EXPAND_CWH_ITEM));
		msg.replace("%cwh_slot_amount%", String.valueOf(Config.SERVICES_EXPAND_CWH_SLOT_AMOUNT));
		player.sendPacket(msg);
	}
}