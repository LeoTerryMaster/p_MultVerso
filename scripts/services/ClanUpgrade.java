package services;

import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;

public class ClanUpgrade extends Functions
{
	public void clan_upgrade_page()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_CLANLEVEL_SELL_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(!player.isInPeaceZone())
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_peace_zone.htm"));
			return;
		}
		Clan clan = player.getClan();
		if(clan == null)
		{
			player.sendMessage("Get clan first.");
			return;
		}
		if(clan.getLeaderId() != player.getObjectId())
		{
			player.sendMessage("Only clan leader can do that.");
			return;
		}
		if(clan.getLevel() < 1 || clan.getLevel() >= Config.SERVICES_CLAN_MAX_SELL_LEVEL)
		{
			player.sendMessage("Clan level to high or to low.");
			return;
		}
		NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/clan_upgrade.htm");
		msg.replace("%item_id%", String.valueOf(Config.SERVICES_CLANLEVEL_SELL_ITEM[clan.getLevel() - 1]));
		msg.replace("%item_count%", String.valueOf(Config.SERVICES_CLANLEVEL_SELL_PRICE[clan.getLevel() - 1]));
		msg.replace("%clan_level_next%", String.valueOf(clan.getLevel() + 1));
		player.sendPacket(msg);
	}
	
	public void clan_upgrade()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_CLANLEVEL_SELL_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		if(!player.isInPeaceZone())
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_peace_zone.htm"));
			return;
		}
		Clan clan = player.getClan();
		if(clan == null)
		{
			player.sendMessage("Get clan first.");
			return;
		}
		if(clan.getLeaderId() != player.getObjectId())
		{
			player.sendMessage("Only clan leader can do that.");
			return;
		}
		if(clan.getLevel() < 1 || clan.getLevel() >= Config.SERVICES_CLAN_MAX_SELL_LEVEL)
		{
			player.sendMessage("Clan level to high or to low.");
			return;
		}
		int toLvl = clan.getLevel() + 1;
		int requiredItemId = Config.SERVICES_CLANLEVEL_SELL_ITEM[Math.min(Math.max(0, toLvl - 2), Config.SERVICES_CLANLEVEL_SELL_ITEM.length - 1)];
		long requiredItemCount = Config.SERVICES_CLANLEVEL_SELL_PRICE[Math.min(Math.max(0, toLvl - 2), Config.SERVICES_CLANLEVEL_SELL_PRICE.length - 1)];
		if(Functions.getItemCount(player, requiredItemId) < requiredItemCount)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		Functions.removeItem(player, requiredItemId, requiredItemCount);
		clan.setLevel(clan.getLevel() + 1);
		clan.updateClanInDB();
		clan.broadcastClanStatus(true, true, true);
		player.sendMessage("Congratulation! Clan level up!.");
	}
}