package handler.items;

import l2.gameserver.data.xml.holder.EnchantItemHolder;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.ChooseInventoryItem;

public class EnchantScrolls extends ScriptItemHandler
{
	private static int[] ITEM_IDS;
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
		{
			return false;
		}
		Player player = (Player) playable;
		if(player.getEnchantScroll() != null)
		{
			return false;
		}
		player.setEnchantScroll(item);
		player.sendPacket(new ChooseInventoryItem(item.getItemId()));
		return true;
	}
	
	@Override
	public final int[] getItemIds()
	{
		if(ITEM_IDS == null)
		{
			ITEM_IDS = EnchantItemHolder.getInstance().getScrollIds();
		}
		return ITEM_IDS;
	}
}