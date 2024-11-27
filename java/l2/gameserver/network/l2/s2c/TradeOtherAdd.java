package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.items.ItemInfo;

public class TradeOtherAdd extends L2GameServerPacket
{
	private final ItemInfo _temp;
	private final long _amount;
	
	public TradeOtherAdd(ItemInfo item, long amount)
	{
		_temp = item;
		_amount = amount;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(33);
		writeH(1);
		writeH(0);
		writeD(_temp.getObjectId());
		writeD(_temp.getItemId());
		writeD((int) _amount);
		writeH(_temp.getItem().getType2ForPackets());
		writeH(_temp.getCustomType1());
		writeD(_temp.getItem().getBodyPart());
		writeH(_temp.getEnchantLevel());
		writeH(0);
		writeH(0);
	}
}