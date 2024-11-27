package handler.items;

import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.SSQStatus;
import l2.gameserver.network.l2.s2c.ShowXMasSeal;

public class Books extends SimpleItemHandler
{
	private static final int[] ITEM_IDS = {5555, 5707};
	
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
			case 5555:
			{
				player.sendPacket(new ShowXMasSeal(5555));
				break;
			}
			case 5707:
			{
				player.sendPacket(new SSQStatus(player, 1));
				break;
			}
			default:
			{
				return false;
			}
		}
		return true;
	}
}