package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.items.ItemInfo;
import l2.gameserver.model.items.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class PetInventoryUpdate extends L2GameServerPacket
{
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int MODIFIED = 2;
	public static final int REMOVED = 3;
	private final List<ItemInfo> _items = new ArrayList<>(1);
	
	public PetInventoryUpdate addNewItem(ItemInstance item)
	{
		addItem(item).setLastChange(1);
		return this;
	}
	
	public PetInventoryUpdate addModifiedItem(ItemInstance item)
	{
		addItem(item).setLastChange(2);
		return this;
	}
	
	public PetInventoryUpdate addRemovedItem(ItemInstance item)
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
		writeC(179);
		writeH(_items.size());
		for(ItemInfo item : _items)
		{
			writeH(item.getLastChange());
			writeH(item.getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD((int) item.getCount());
			writeH(item.getType2());
			writeH(item.getCustomType1());
			writeH(item.isEquipped() ? 1 : 0);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getCustomType2());
		}
	}
}