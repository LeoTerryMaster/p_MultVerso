package handler.items;

import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.ShowMiniMap;

public class WorldMap extends ScriptItemHandler
{
	private static final int[] _itemIds = {1665, 1863};
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
		{
			return false;
		}
		Player player = (Player) playable;
		player.sendPacket(new ShowMiniMap(player, item.getItemId()));
		return true;
	}
	
	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}
}