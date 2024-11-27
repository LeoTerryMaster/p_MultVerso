package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.items.ItemInstance;

public class ExBR_AgathionEnergyInfo extends L2GameServerPacket
{
	private final int _size;
	private final ItemInstance[] _itemList;
	
	public ExBR_AgathionEnergyInfo(int size, ItemInstance... item)
	{
		_itemList = item;
		_size = size;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(222);
		writeD(_size);
		for(ItemInstance item : _itemList)
		{
			if(item.getTemplate().getAgathionEnergy() == 0)
				continue;
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(2097152);
			writeD(item.getAgathionEnergy());
			writeD(item.getTemplate().getAgathionEnergy());
		}
	}
}