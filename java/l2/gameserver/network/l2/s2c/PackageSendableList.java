package l2.gameserver.network.l2.s2c;

import l2.commons.lang.ArrayUtils;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInfo;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.Warehouse;
import l2.gameserver.network.l2.c2s.RequestPackageSend;

import java.util.LinkedList;
import java.util.List;

public class PackageSendableList extends L2GameServerPacket
{
	private final int _targetObjectId;
	private final long _adena;
	private final List<ItemInfo> _itemList;
	
	public PackageSendableList(int objectId, Player cha)
	{
		_adena = cha.getAdena();
		_targetObjectId = objectId;
		ItemInstance[] items = cha.getInventory().getItems();
		ArrayUtils.eqSort(items, Warehouse.ItemClassComparator.getInstance());
		_itemList = new LinkedList<>();
		for(ItemInstance item : items)
		{
			if(!RequestPackageSend.CanSendItem(item))
				continue;
			_itemList.add(new ItemInfo(item));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(195);
		writeD(_targetObjectId);
		writeD((int) _adena);
		writeD(_itemList.size());
		for(ItemInfo item : _itemList)
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD((int) item.getCount());
			writeH(item.getItem().getType2ForPackets());
			writeH(item.getCustomType1());
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getCustomType2());
			writeH(0);
			writeD(item.getObjectId());
		}
	}
}