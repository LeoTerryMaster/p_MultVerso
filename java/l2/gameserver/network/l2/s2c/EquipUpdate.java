package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.items.ItemInfo;
import l2.gameserver.model.items.ItemInstance;

@Deprecated
public class EquipUpdate extends L2GameServerPacket
{
	private final ItemInfo _item;
	
	public EquipUpdate(ItemInstance item, int change)
	{
		_item = new ItemInfo(item);
		_item.setLastChange(change);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(75);
		writeD(_item.getLastChange());
		writeD(_item.getObjectId());
		writeD(_item.getEquipSlot());
	}
}