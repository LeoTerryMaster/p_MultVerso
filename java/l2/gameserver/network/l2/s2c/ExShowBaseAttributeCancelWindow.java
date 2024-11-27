package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.base.Element;
import l2.gameserver.model.items.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class ExShowBaseAttributeCancelWindow extends L2GameServerPacket
{
	private final List<ItemInstance> _items = new ArrayList<>();
	
	public ExShowBaseAttributeCancelWindow(Player activeChar)
	{
		for(ItemInstance item : activeChar.getInventory().getItems())
		{
			if(item.getAttributeElement() == Element.NONE || !item.canBeEnchanted(true) || getAttributeRemovePrice(item) == 0)
				continue;
			_items.add(item);
		}
	}
	
	public static long getAttributeRemovePrice(ItemInstance item)
	{
		switch(item.getCrystalType())
		{
			case S:
			{
				return item.getTemplate().getType2() == 0 ? 50000 : 40000;
			}
		}
		return 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(116);
		writeD(_items.size());
		for(ItemInstance item : _items)
		{
			writeD(item.getObjectId());
			writeQ(getAttributeRemovePrice(item));
		}
	}
}