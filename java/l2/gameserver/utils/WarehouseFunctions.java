package l2.gameserver.utils;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.Warehouse;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.WareHouseDepositList;
import l2.gameserver.network.l2.s2c.WareHouseWithdrawList;
import l2.gameserver.templates.item.ItemTemplate;

public final class WarehouseFunctions
{
	private WarehouseFunctions()
	{
	}
	
	public static void showFreightWindow(Player player)
	{
		if(!canShowWarehouseWithdrawList(player, Warehouse.WarehouseType.FREIGHT))
		{
			player.sendActionFailed();
			return;
		}
		player.setUsingWarehouseType(Warehouse.WarehouseType.FREIGHT);
		player.sendPacket(new WareHouseWithdrawList(player, Warehouse.WarehouseType.FREIGHT, ItemTemplate.ItemClass.ALL));
	}
	
	public static void showRetrieveWindow(Player player, int val)
	{
		if(!canShowWarehouseWithdrawList(player, Warehouse.WarehouseType.PRIVATE))
		{
			player.sendActionFailed();
			return;
		}
		player.setUsingWarehouseType(Warehouse.WarehouseType.PRIVATE);
		player.sendPacket(new WareHouseWithdrawList(player, Warehouse.WarehouseType.PRIVATE, ItemTemplate.ItemClass.values()[val]));
	}
	
	public static void showDepositWindow(Player player)
	{
		if(!canShowWarehouseDepositList(player, Warehouse.WarehouseType.PRIVATE))
		{
			player.sendActionFailed();
			return;
		}
		player.setUsingWarehouseType(Warehouse.WarehouseType.PRIVATE);
		player.sendPacket(new WareHouseDepositList(player, Warehouse.WarehouseType.PRIVATE));
	}
	
	public static void showDepositWindowClan(Player player)
	{
		if(!canShowWarehouseDepositList(player, Warehouse.WarehouseType.CLAN))
		{
			player.sendActionFailed();
			return;
		}
		if(!(player.isClanLeader() || (Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE || player.getVarB("canWhWithdraw")) && (player.getClanPrivileges() & 8) == 8))
		{
			player.sendPacket(Msg.ITEMS_LEFT_AT_THE_CLAN_HALL_WAREHOUSE_CAN_ONLY_BE_RETRIEVED_BY_THE_CLAN_LEADER_DO_YOU_WANT_TO_CONTINUE);
		}
		player.setUsingWarehouseType(Warehouse.WarehouseType.CLAN);
		player.sendPacket(new WareHouseDepositList(player, Warehouse.WarehouseType.CLAN));
	}
	
	public static void showWithdrawWindowClan(Player player, int val)
	{
		if(!canShowWarehouseWithdrawList(player, Warehouse.WarehouseType.CLAN))
		{
			player.sendActionFailed();
			return;
		}
		player.setUsingWarehouseType(Warehouse.WarehouseType.CLAN);
		player.sendPacket(new WareHouseWithdrawList(player, Warehouse.WarehouseType.CLAN, ItemTemplate.ItemClass.values()[val]));
	}
	
	public static boolean canShowWarehouseWithdrawList(Player player, Warehouse.WarehouseType type)
	{
		if(!player.getPlayerAccess().UseWarehouse)
		{
			return false;
		}
		Warehouse warehouse;
		switch(type)
		{
			case PRIVATE:
			{
				warehouse = player.getWarehouse();
				break;
			}
			case FREIGHT:
			{
				warehouse = player.getFreight();
				break;
			}
			case CLAN:
			case CASTLE:
			{
				if(player.getClan() == null || player.getClan().getLevel() == 0)
				{
					player.sendPacket(Msg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE);
					return false;
				}
				boolean canWithdrawCWH = false;
				if(player.getClan() != null && (player.getClanPrivileges() & 8) == 8)
				{
					canWithdrawCWH = true;
				}
				if(!canWithdrawCWH)
				{
					player.sendPacket(Msg.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE);
					return false;
				}
				warehouse = player.getClan().getWarehouse();
				break;
			}
			default:
			{
				return false;
			}
		}
		if(warehouse.getSize() == 0)
		{
			player.sendPacket(type == Warehouse.WarehouseType.FREIGHT ? SystemMsg.NO_PACKAGES_HAVE_ARRIVED : Msg.YOU_HAVE_NOT_DEPOSITED_ANY_ITEMS_IN_YOUR_WAREHOUSE);
			return false;
		}
		return true;
	}
	
	public static boolean canShowWarehouseDepositList(Player player, Warehouse.WarehouseType type)
	{
		if(!player.getPlayerAccess().UseWarehouse)
		{
			return false;
		}
		switch(type)
		{
			case PRIVATE:
			{
				return true;
			}
			case CLAN:
			case CASTLE:
			{
				if(player.getClan() == null || player.getClan().getLevel() == 0)
				{
					player.sendPacket(Msg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE);
					return false;
				}
				boolean canWithdrawCWH = false;
				if(player.getClan() != null && (player.getClanPrivileges() & 8) == 8)
				{
					canWithdrawCWH = true;
				}
				if(!canWithdrawCWH)
				{
					player.sendPacket(Msg.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE);
					return false;
				}
				return true;
			}
		}
		return false;
	}
}