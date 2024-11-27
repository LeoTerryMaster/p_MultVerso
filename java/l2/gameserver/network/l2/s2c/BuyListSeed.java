package l2.gameserver.network.l2.s2c;

import l2.gameserver.data.xml.holder.BuyListHolder;
import l2.gameserver.model.items.TradeItem;

import java.util.ArrayList;
import java.util.List;

public final class BuyListSeed extends L2GameServerPacket
{
	private final int _manorId;
	private final long _money;
	private List<TradeItem> _list = new ArrayList<>();
	
	public BuyListSeed(BuyListHolder.NpcTradeList list, int manorId, long currentMoney)
	{
		_money = currentMoney;
		_manorId = manorId;
		_list = list.getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(232);
		writeD((int) _money);
		writeD(_manorId);
		writeH(_list.size());
		for(TradeItem item : _list)
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD((int) item.getCount());
			writeH(item.getItem().getType2ForPackets());
			writeH(item.getCustomType1());
			writeD((int) item.getOwnersPrice());
		}
	}
}