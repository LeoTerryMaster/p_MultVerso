package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.LockType;

public class ExQuestItemList extends L2GameServerPacket
{
	private final int _size;
	private final ItemInstance[] _items;
	private final LockType _lockType;
	private final int[] _lockItems;
	
	public ExQuestItemList(int size, ItemInstance[] t, LockType lockType, int[] lockItems)
	{
		_size = size;
		_items = t;
		_lockType = lockType;
		_lockItems = lockItems;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(25);
		writeH(_size);
		for(ItemInstance temp : _items)
		{
			if(!temp.getTemplate().isQuest())
				continue;
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