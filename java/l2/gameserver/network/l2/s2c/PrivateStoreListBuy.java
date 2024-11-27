package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.TradeItem;

import java.util.ArrayList;
import java.util.List;

public class PrivateStoreListBuy extends L2GameServerPacket
{
	private final int _buyerId;
	private final long _adena;
	private final List<TradeItem> _sellList;
	
	public PrivateStoreListBuy(Player seller, Player buyer)
	{
		_adena = seller.getAdena();
		_buyerId = buyer.getObjectId();
		_sellList = new ArrayList<>();
		List<TradeItem> buyList = buyer.getBuyList();
		ItemInstance[] items = seller.getInventory().getItems();
		for(TradeItem bi : buyList)
		{
			TradeItem si = null;
			for(ItemInstance item : items)
			{
				if(item.getItemId() != bi.getItemId() || !item.canBeTraded(seller))
					continue;
				si = new TradeItem(item);
				_sellList.add(si);
				si.setOwnersPrice(bi.getOwnersPrice());
				si.setCount(bi.getCount());
				si.setCurrentValue(Math.min(bi.getCount(), item.getCount()));
			}
			if(si != null)
				continue;
			si = new TradeItem();
			si.setItemId(bi.getItemId());
			si.setOwnersPrice(bi.getOwnersPrice());
			si.setCount(bi.getCount());
			si.setCurrentValue(0);
			_sellList.add(si);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(184);
		writeD(_buyerId);
		writeD((int) _adena);
		writeD(_sellList.size());
		for(TradeItem si : _sellList)
		{
			writeD(si.getObjectId());
			writeD(si.getItemId());
			writeH(si.getEnchantLevel());
			writeD((int) si.getCurrentValue());
			writeD((int) si.getStorePrice());
			writeH(0);
			writeD(si.getBodyPart());
			writeH(si.getItem().getType2ForPackets());
			writeD((int) si.getOwnersPrice());
			writeD((int) si.getCount());
		}
	}
}