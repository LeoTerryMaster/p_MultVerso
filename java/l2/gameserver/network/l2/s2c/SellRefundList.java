package l2.gameserver.network.l2.s2c;

import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.TradeItem;

import java.util.LinkedList;
import java.util.List;

public class SellRefundList extends L2GameServerPacket
{
	private final List<TradeItem> _sellList;
	private final int _adena;
	private final boolean _isDone;
	
	public SellRefundList(Player player, boolean isDone)
	{
		_adena = (int) player.getAdena();
		_isDone = isDone;
		ItemInstance[] items = player.getInventory().getItems();
		_sellList = new LinkedList<>();
		for(ItemInstance item : items)
		{
			if(!item.canBeSold(player))
				continue;
			_sellList.add(new TradeItem(item));
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(16);
		writeD(_adena);
		writeD(_isDone);
		writeH(_sellList.size());
		for(TradeItem item : _sellList)
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
			writeD((int) Math.max(1, item.getReferencePrice() / Config.ALT_SHOP_REFUND_SELL_DIVISOR));
		}
	}
}