package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.TradeItem;
import l2.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.List;

public class PrivateStoreManageListSell extends L2GameServerPacket
{
	private int _sellerId;
	private long _adena;
	private boolean _package;
	private List<TradeItem> _sellList;
	private List<TradeItem> _sellList0;
	
	public PrivateStoreManageListSell(Player seller, boolean pkg)
	{
		_sellerId = seller.getObjectId();
		_adena = seller.getAdena();
		_package = pkg;
		_sellList0 = seller.getSellList(_package);
		_sellList = new ArrayList<>();
		for(TradeItem si : _sellList0)
		{
			if(si.getCount() <= 0)
			{
				_sellList0.remove(si);
				continue;
			}
			ItemInstance item = seller.getInventory().getItemByObjectId(si.getObjectId());
			if(item == null)
			{
				item = seller.getInventory().getItemByItemId(si.getItemId());
			}
			if(item == null || !item.canBeTraded(seller) || item.getItemId() == 57)
			{
				_sellList0.remove(si);
				continue;
			}
			si.setCount(Math.min(item.getCount(), si.getCount()));
		}
		ItemInstance[] items = seller.getInventory().getItems();
		block1:
		for(ItemInstance item : items)
		{
			if(!item.canBeTraded(seller) || item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
				continue;
			for(TradeItem si : _sellList0)
			{
				if(si.getObjectId() != item.getObjectId())
					continue;
				if(si.getCount() == item.getCount())
					continue block1;
				TradeItem ti = new TradeItem(item);
				ti.setCount(item.getCount() - si.getCount());
				_sellList.add(ti);
				continue block1;
			}
			_sellList.add(new TradeItem(item));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(154);
		writeD(_sellerId);
		writeD(_package ? 1 : 0);
		writeD((int) _adena);
		writeD(_sellList.size());
		for(TradeItem si : _sellList)
		{
			writeD(si.getItem().getType2ForPackets());
			writeD(si.getObjectId());
			writeD(si.getItemId());
			writeD((int) si.getCount());
			writeH(0);
			writeH(si.getEnchantLevel());
			writeH(0);
			writeD(si.getItem().getBodyPart());
			writeD((int) si.getStorePrice());
		}
		writeD(_sellList0.size());
		for(TradeItem si : _sellList0)
		{
			writeD(si.getItem().getType2ForPackets());
			writeD(si.getObjectId());
			writeD(si.getItemId());
			writeD((int) si.getCount());
			writeH(0);
			writeH(si.getEnchantLevel());
			writeH(0);
			writeD(si.getItem().getBodyPart());
			writeD((int) si.getOwnersPrice());
			writeD((int) si.getStorePrice());
		}
	}
}