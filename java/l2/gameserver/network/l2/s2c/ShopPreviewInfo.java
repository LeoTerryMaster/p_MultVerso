package l2.gameserver.network.l2.s2c;

import java.util.Map;

public class ShopPreviewInfo extends L2GameServerPacket
{
	private final Map<Integer, Integer> _itemlist;
	
	public ShopPreviewInfo(Map<Integer, Integer> itemlist)
	{
		_itemlist = itemlist;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(240);
		writeD(17);
		writeD(getFromList(0));
		writeD(getFromList(1));
		writeD(getFromList(2));
		writeD(getFromList(3));
		writeD(getFromList(4));
		writeD(getFromList(5));
		writeD(getFromList(7));
		writeD(getFromList(8));
		writeD(getFromList(9));
		writeD(getFromList(10));
		writeD(getFromList(11));
		writeD(getFromList(12));
		writeD(getFromList(13));
		writeD(getFromList(15));
		writeD(getFromList(15));
		writeD(getFromList(16));
	}
	
	private int getFromList(int key)
	{
		return _itemlist.get(key) != null ? _itemlist.get(key) : 0;
	}
}