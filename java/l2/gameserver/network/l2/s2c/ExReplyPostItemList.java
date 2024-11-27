package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInfo;
import l2.gameserver.model.items.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class ExReplyPostItemList extends L2GameServerPacket
{
	private final List<ItemInfo> _itemsList = new ArrayList<>();
	
	public ExReplyPostItemList(Player activeChar)
	{
		ItemInstance[] items = activeChar.getInventory().getItems();
		for(ItemInstance item : items)
		{
			if(!item.canBeTraded(activeChar))
				continue;
			_itemsList.add(new ItemInfo(item));
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(178);
		writeD(_itemsList.size());
		for(ItemInfo item : _itemsList)
		{
			writeItemInfo(item);
		}
	}
}