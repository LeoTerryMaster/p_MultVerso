package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.items.ItemInfo;

public class TradeUpdate extends L2GameServerPacket
{
	private final ItemInfo _item;
	private final long _amount;
	
	public TradeUpdate(ItemInfo item, long amount)
	{
		_item = item;
		_amount = amount;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(116);
		writeH(1);
		writeH(_amount > 0 && _item.getItem().isStackable() ? 3 : 2);
		writeH(0);
		writeD(_item.getObjectId());
		writeD(_item.getItemId());
		writeD((int) _amount);
		writeH(_item.getItem().getType2ForPackets());
		writeH(_item.getCustomType1());
		writeD(_item.getItem().getBodyPart());
		writeH(_item.getEnchantLevel());
		writeH(0);
		writeH(0);
	}
}