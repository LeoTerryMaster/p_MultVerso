package l2.gameserver.handler.items;

import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;

public interface IRefineryHandler
{
	void onInitRefinery(Player player);
	
	void onPutTargetItem(Player player, ItemInstance targetItem);
	
	void onPutMineralItem(Player player, ItemInstance targetItem, ItemInstance mineralItem);
	
	void onPutGemstoneItem(Player player, ItemInstance targetItem, ItemInstance mineralItem, ItemInstance gemstoneItem, long gemstoneItemCnt);
	
	void onRequestRefine(Player player, ItemInstance targetItem, ItemInstance mineralItem, ItemInstance gemstoneItem, long gemstoneItemCnt);
	
	void onInitRefineryCancel(Player player);
	
	void onPutTargetCancelItem(Player player, ItemInstance targetCancelItem);
	
	void onRequestCancelRefine(Player player, ItemInstance targetCancelItem);
}