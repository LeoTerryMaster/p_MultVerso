package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.items.ItemInfo;

public class ExRpItemLink extends L2GameServerPacket
{
	private final ItemInfo _item;
	
	public ExRpItemLink(ItemInfo item)
	{
		_item = item;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(108);
		writeItemInfo(_item);
	}
}