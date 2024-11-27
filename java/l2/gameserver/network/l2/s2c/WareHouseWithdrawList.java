package l2.gameserver.network.l2.s2c;

import l2.commons.lang.ArrayUtils;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInfo;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.Warehouse;
import l2.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WareHouseWithdrawList extends L2GameServerPacket
{
	private final long _adena;
	private final int _type;
	private List<ItemInfo> _itemList = new ArrayList<>();
	
	public WareHouseWithdrawList(Player player, Warehouse.WarehouseType type, ItemTemplate.ItemClass clss)
	{
		_adena = player.getAdena();
		_type = type.ordinal();
		ItemInstance[] items;
		switch(type)
		{
			case PRIVATE:
			{
				items = player.getWarehouse().getItems(clss);
				break;
			}
			case FREIGHT:
			{
				items = player.getFreight().getItems(clss);
				break;
			}
			case CLAN:
			case CASTLE:
			{
				items = player.getClan().getWarehouse().getItems(clss);
				break;
			}
			default:
			{
				_itemList = Collections.emptyList();
				return;
			}
		}
		_itemList = new ArrayList<>(items.length);
		ArrayUtils.eqSort(items, Warehouse.ItemClassComparator.getInstance());
		for(ItemInstance item : items)
		{
			_itemList.add(new ItemInfo(item));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x42);
		writeH(_type);
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