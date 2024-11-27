package l2.gameserver.network.l2.s2c;

import l2.commons.lang.ArrayUtils;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInfo;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.Warehouse;

import java.util.ArrayList;
import java.util.List;

public class WareHouseDepositList extends L2GameServerPacket
{
	private final int _whtype;
	private final long _adena;
	private final List<ItemInfo> _itemList;
	
	public WareHouseDepositList(Player cha, Warehouse.WarehouseType whtype)
	{
		_whtype = whtype.ordinal();
		_adena = cha.getAdena();
		ItemInstance[] items = cha.getInventory().getItems();
		ArrayUtils.eqSort(items, Warehouse.ItemClassComparator.getInstance());
		_itemList = new ArrayList<>(items.length);
		for(ItemInstance item : items)
		{
			if(!item.canBeStored(cha, _whtype == 1))
				continue;
			_itemList.add(new ItemInfo(item));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(65);
		writeH(_whtype);
		writeD((int) _adena);
		writeH(_itemList.size());
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
			writeD(item.getVariationStat1());
			writeD(item.getVariationStat2());
		}
	}
}