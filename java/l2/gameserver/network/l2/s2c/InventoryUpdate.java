package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.items.ItemInfo;
import l2.gameserver.model.items.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class InventoryUpdate extends L2GameServerPacket
{
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int MODIFIED = 2;
	public static final int REMOVED = 3;
	private final List<ItemInfo> _items = new ArrayList<>(1);
	
	public InventoryUpdate addNewItem(ItemInstance item)
	{
		addItem(item).setLastChange(1);
		return this;
	}
	
	public InventoryUpdate addModifiedItem(ItemInstance item)
	{
		addItem(item).setLastChange(2);
		return this;
	}
	
	public InventoryUpdate addRemovedItem(ItemInstance item)
	{
		addItem(item).setLastChange(3);
		return this;
	}
	
	private ItemInfo addItem(ItemInstance item)
	{
		ItemInfo info = new ItemInfo(item);
		_items.add(info);
		return info;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(39);
		writeH(_items.size());
		for(ItemInfo temp : _items)
		{
			writeH(temp.getLastChange());
			writeItemInfo(temp);
		}
	}
}