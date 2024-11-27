package l2.gameserver.handler.items;

import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;

public interface IItemHandler
{
	IItemHandler NULL = new IItemHandler()
	{
		@Override
		public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
		{
			return false;
		}
		
		@Override
		public void dropItem(Player player, ItemInstance item, long count, Location loc)
		{
			if(item.isEquipped())
			{
				player.getInventory().unEquipItem(item);
				player.sendUserInfo(true);
			}
			if((item = player.getInventory().removeItemByObjectId(item.getObjectId(), count)) == null)
			{
				player.sendActionFailed();
				return;
			}
			Log.LogItem(player, Log.ItemLog.Drop, item);
			item.dropToTheGround(player, loc);
			player.disableDrop(1000);
			player.sendChanges();
		}
		
		@Override
		public boolean pickupItem(Playable playable, ItemInstance item)
		{
			return true;
		}
		
		@Override
		public int[] getItemIds()
		{
			return ArrayUtils.EMPTY_INT_ARRAY;
		}
	};
	
	boolean useItem(Playable playable, ItemInstance item, boolean ctrl);
	
	void dropItem(Player player, ItemInstance item, long count, Location loc);
	
	boolean pickupItem(Playable playable, ItemInstance item);
	
	int[] getItemIds();
}