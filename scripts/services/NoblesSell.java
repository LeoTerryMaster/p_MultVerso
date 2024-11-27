package services;

import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.oly.NoblesController;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.SkillList;
import l2.gameserver.scripts.Functions;

public class NoblesSell extends Functions
{
	public void show()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_NOBLESS_SELL_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(player.isNoble())
		{
			player.sendMessage("You already have a noble status.");
			return;
		}
		if(!Config.SERVICES_NOBLESS_SELL_WITHOUT_SUBCLASS && player.getSubLevel() < Config.NOBLESS_LEVEL_FOR_SELL)
		{
			NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/service_noble_sell_need_sub_class.htm");
			msg.replace("%noble_level%", String.valueOf(Config.NOBLESS_LEVEL_FOR_SELL));
			player.sendPacket(msg);
			return;
		}
		if(player.getLevel() < Config.NOBLESS_LEVEL_FOR_SELL)
		{
			NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/service_noble_sell_need_level.htm");
			msg.replace("%noble_level%", String.valueOf(Config.NOBLESS_LEVEL_FOR_SELL));
			player.sendPacket(msg);
			return;
		}
		NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/noble_sell.htm");
		msg.replace("%noble_level%", String.valueOf(Config.NOBLESS_LEVEL_FOR_SELL));
		msg.replace("%noble_sell_price%", String.valueOf(Config.SERVICES_NOBLESS_SELL_PRICE));
		msg.replace("%noble_sell_item_id%", String.valueOf(Config.SERVICES_NOBLESS_SELL_ITEM));
		player.sendPacket(msg);
	}
	
	public void get()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_NOBLESS_SELL_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(getItemCount(player, Config.SERVICES_NOBLESS_SELL_ITEM) < (long) Config.SERVICES_NOBLESS_SELL_PRICE)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		if(player.isNoble())
		{
			player.sendMessage("You already have a noble status.");
			return;
		}
		if(player.getLevel() < Config.NOBLESS_LEVEL_FOR_SELL)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_noble_sell_need_level.htm"));
			return;
		}
		removeItem(player, Config.SERVICES_NOBLESS_SELL_ITEM, (long) Config.SERVICES_NOBLESS_SELL_PRICE);
		addItem(player, 7694, (long) 1);
		NoblesController.getInstance().addNoble(player);
		player.setNoble(true);
		player.updatePledgeClass();
		player.updateNobleSkills();
		player.sendPacket(new SkillList(player));
		player.broadcastUserInfo(true);
	}
}