package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.items.ItemInstance;

public class ExPutItemResultForVariationCancel extends L2GameServerPacket
{
	private final int _itemObjectId;
	private final int _itemId;
	private final int _aug1;
	private final int _aug2;
	private final long _price;
	private final boolean _isSuccess;
	
	public ExPutItemResultForVariationCancel(ItemInstance item, long price, boolean isSuccess)
	{
		_itemObjectId = item.getObjectId();
		_itemId = item.getItemId();
		_aug1 = item.getVariationStat1();
		_aug2 = item.getVariationStat2();
		_price = price;
		_isSuccess = isSuccess;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(86);
		writeD(_itemObjectId);
		writeD(_itemId);
		writeD(_aug1);
		writeD(_aug2);
		writeD((int) _price);
		writeD(0);
		writeD(_isSuccess ? 1 : 0);
	}
}