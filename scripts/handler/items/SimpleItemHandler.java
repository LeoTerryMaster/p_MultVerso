package handler.items;

import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage;

public abstract class SimpleItemHandler extends ScriptItemHandler
{
	public static boolean useItem(Player player, ItemInstance item, long count)
	{
		if(player.getInventory().destroyItem(item, count))
		{
			player.sendPacket(new SystemMessage(47).addItemName(item.getItemId()));
			return true;
		}
		player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
		return false;
	}
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		Player player;
		if(playable.isPlayer())
		{
			player = (Player) playable;
		}
		else if(playable.isPet())
		{
			player = playable.getPlayer();
		}
		else
		{
			return false;
		}
		if(player.isInFlyingTransform())
		{
			player.sendPacket(new SystemMessage(113).addItemName(item.getItemId()));
			return false;
		}
		return useItemImpl(player, item, ctrl);
	}
	
	protected abstract boolean useItemImpl(Player player, ItemInstance item, boolean ctrl);
}