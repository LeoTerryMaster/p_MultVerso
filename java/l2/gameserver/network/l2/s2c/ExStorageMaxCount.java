package l2.gameserver.network.l2.s2c;

import l2.gameserver.Config;
import l2.gameserver.model.Player;

public class ExStorageMaxCount extends L2GameServerPacket
{
	private final int _inventory;
	private final int _warehouse;
	private final int _clan;
	private final int _privateSell;
	private final int _privateBuy;
	private final int _recipeDwarven;
	private final int _recipeCommon;
	private final int _questItemsLimit;
	private int _inventoryExtraSlots;
	
	public ExStorageMaxCount(Player player)
	{
		_inventory = player.getInventoryLimit();
		_warehouse = player.getWarehouseLimit();
		_clan = Config.WAREHOUSE_SLOTS_CLAN;
		_privateBuy = _privateSell = player.getTradeLimit();
		_recipeDwarven = player.getDwarvenRecipeLimit();
		_recipeCommon = player.getCommonRecipeLimit();
		_questItemsLimit = Config.QUEST_INVENTORY_MAXIMUM;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(46);
		writeD(_inventory);
		writeD(_warehouse);
		writeD(_clan);
		writeD(_privateSell);
		writeD(_privateBuy);
		writeD(_recipeDwarven);
		writeD(_recipeCommon);
	}
}