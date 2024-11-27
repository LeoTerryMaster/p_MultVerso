package handler.items;

import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.scripts.Functions;

public class Special extends SimpleItemHandler
{
	private static final int[] ITEM_IDS = {8060};
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
	
	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();
		switch(itemId)
		{
			case 8060:
			{
				return use8060(player, ctrl);
			}
		}
		return false;
	}
	
	private boolean use8060(Player player, boolean ctrl)
	{
		if(Functions.removeItem(player, 8058, (long) 1) == 1)
		{
			Functions.addItem(player, 8059, (long) 1);
			return true;
		}
		return false;
	}
}