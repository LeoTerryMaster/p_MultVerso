package services;

import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.Experience;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;

public class Levelup extends Functions
{
	public void levelup_page()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_LEVEL_UP_SELL_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/levelup_change.htm");
		msg.replace("%item_id%", String.valueOf(Config.SERVICES_LEVEL_UP_SELL_ITEM));
		msg.replace("%item_count%", String.valueOf(Config.SERVICES_LEVEL_UP_SELL_PRICE));
		player.sendPacket(msg);
	}
	
	public void levelup()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_LEVEL_UP_SELL_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(player.getLevel() >= (player.isSubClassActive() ? Experience.getMaxSubLevel() : Experience.getMaxLevel()) || player.getExp() > player.getMaxExp())
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/level_upchange_max_level.htm"));
			return;
		}
		if(getItemCount(player, Config.SERVICES_LEVEL_UP_SELL_ITEM) < (long) Config.SERVICES_LEVEL_UP_SELL_PRICE)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		removeItem(player, Config.SERVICES_LEVEL_UP_SELL_ITEM, (long) Config.SERVICES_LEVEL_UP_SELL_PRICE);
		player.addExpAndSp(Experience.LEVEL[Math.min(player.getLevel() + Config.SERVICES_LEVEL_UP_COUNT, Experience.LEVEL.length - 1)] - player.getExp(), 0, false, false);
		levelup_page();
	}
}