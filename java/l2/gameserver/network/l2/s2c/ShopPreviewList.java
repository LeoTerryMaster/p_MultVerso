package l2.gameserver.network.l2.s2c;

import l2.gameserver.data.xml.holder.BuyListHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInfo;
import l2.gameserver.model.items.TradeItem;
import l2.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.List;

public class ShopPreviewList extends L2GameServerPacket
{
	private final int _listId;
	private final List<ItemInfo> _itemList;
	private final long _money;
	
	public ShopPreviewList(BuyListHolder.NpcTradeList list, Player player)
	{
		_listId = list.getListId();
		_money = player.getAdena();
		List<TradeItem> tradeList = list.getItems();
		_itemList = new ArrayList<>(tradeList.size());
		for(TradeItem item : list.getItems())
		{
			if(!item.getItem().isEquipable())
				continue;
			_itemList.add(item);
		}
	}
	
	public static int getWearPrice(ItemTemplate item)
	{
		switch(item.getItemGrade())
		{
			case D:
			{
				return 50;
			}
			case C:
			{
				return 100;
			}
			case B:
			{
				return 200;
			}
			case A:
			{
				return 500;
			}
			case S:
			{
				return 1000;
			}
		}
		return 10;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(239);
		writeC(192);
		writeC(19);
		writeC(0);
		writeC(0);
		writeD((int) _money);
		writeD(_listId);
		writeH(_itemList.size());
		for(ItemInfo item : _itemList)
		{
			if(!item.getItem().isEquipable())
				continue;
			writeD(item.getItemId());
			writeH(item.getItem().getType2ForPackets());
			writeH(item.getItem().isEquipable() ? item.getItem().getBodyPart() : 0);
			writeD(getWearPrice(item.getItem()));
		}
	}
}