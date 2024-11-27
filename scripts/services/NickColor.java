package services;

import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.ItemFunctions;

public class NickColor extends Functions
{
	public void list()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_CHANGE_NICK_COLOR_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/change_nick_color.htm");
		msg.replace("%item_id%", String.valueOf(Config.SERVICES_CHANGE_NICK_COLOR_ITEM));
		msg.replace("%item_count%", String.valueOf(Config.SERVICES_CHANGE_NICK_COLOR_PRICE));
		StringBuilder sb = new StringBuilder();
		for(String color : Config.SERVICES_CHANGE_NICK_COLOR_LIST)
		{
			sb.append("<br><a action=\"bypass -h scripts_services.NickColor:change ").append(color).append("\"><font color=\"").append(color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2)).append("\">").append(player.getName()).append("</font></a>");
		}
		msg.replace("%list%", sb.toString());
		player.sendPacket(msg);
	}
	
	public void change(String[] param)
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(param == null || param.length < 1)
		{
			return;
		}
		if(!Config.SERVICES_CHANGE_NICK_COLOR_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(param[0].equalsIgnoreCase("FFFFFF"))
		{
			player.setNameColor(Integer.decode("0xFFFFFF").intValue());
			player.broadcastUserInfo(true);
			return;
		}
		if(ItemFunctions.removeItem(player, Config.SERVICES_CHANGE_NICK_COLOR_ITEM, (long) Config.SERVICES_CHANGE_NICK_COLOR_PRICE, true) >= (long) Config.SERVICES_CHANGE_NICK_COLOR_PRICE)
		{
			player.setNameColor(Integer.decode("0x" + param[0]).intValue());
			player.broadcastUserInfo(true);
		}
		else if(Config.SERVICES_CHANGE_NICK_COLOR_ITEM == 57)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		}
		else
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
		}
	}
}