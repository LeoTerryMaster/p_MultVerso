package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.LockType;

public class ItemList extends L2GameServerPacket
{
	private final int _size;
	private final ItemInstance[] _items;
	private final boolean _showWindow;
	private final LockType _lockType;
	private final int[] _lockItems;
	
	public ItemList(int size, ItemInstance[] items, boolean showWindow, LockType lockType, int[] lockItems)
	{
		_size = size;
		_items = items;
		_showWindow = showWindow;
		_lockType = lockType;
		_lockItems = lockItems;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(27);
		writeH(_showWindow ? 1 : 0);
		writeH(_size);
		for(ItemInstance temp : _items)
		{
			writeItemInfo(temp);
		}
		writeH(_lockItems.length);
		if(_lockItems.length > 0)
		{
			writeC(_lockType.ordinal());
			for(int i : _lockItems)
			{
				writeD(i);
			}
		}
	}
}