package l2.gameserver.network.l2.s2c;

import l2.commons.lang.ArrayUtils;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.TradeItem;
import l2.gameserver.model.items.Warehouse;

import java.util.ArrayList;
import java.util.List;

public class PrivateStoreManageListBuy extends L2GameServerPacket
{
	private final int _buyerId;
	private final long _adena;
	private final List<TradeItem> _buyList0;
	private final List<TradeItem> _buyList;
	
	public PrivateStoreManageListBuy(Player buyer)
	{
		_buyerId = buyer.getObjectId();
		_adena = buyer.getAdena();
		_buyList0 = buyer.getBuyList();
		_buyList = new ArrayList<>();
		ItemInstance[] items = buyer.getInventory().getItems();
		ArrayUtils.eqSort(items, Warehouse.ItemClassComparator.getInstance());
		for(ItemInstance item : items)
		{
			if(!item.canBeTraded(buyer) || item.getItemId() == 57)
				continue;
			TradeItem bi = new TradeItem(item);
			_buyList.add(bi);
			bi.setObjectId(0);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(183);
		writeD(_buyerId);
		writeD((int) _adena);
		writeD(_buyList.size());
		for(TradeItem bi : _buyList)
		{
			writeD(bi.getItemId());
			writeH(bi.getEnchantLevel());
			writeD((int) bi.getCount());
			writeD((int) bi.getStorePrice());
			writeH(0);
			writeD(bi.getItem().getBodyPart());
			writeH(bi.getItem().getType2ForPackets());
		}
		writeD(_buyList0.size());
		for(TradeItem bi : _buyList0)
		{
			writeD(bi.getItemId());
			writeH(bi.getEnchantLevel());
			writeD((int) bi.getCount());
			writeD((int) bi.getStorePrice());
			writeH(0);
			writeD(bi.getItem().getBodyPart());
			writeH(bi.getItem().getType2ForPackets());
			writeD((int) bi.getOwnersPrice());
			writeD((int) bi.getStorePrice());
		}
	}
}