package l2.gameserver.network.l2.s2c;

import l2.gameserver.data.xml.holder.BuyListHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.TradeItem;

import java.util.Collections;
import java.util.List;

public class BuyList extends L2GameServerPacket
{
	private final int _listId;
	private final List<TradeItem> _buyList;
	private final long _adena;
	private final double _taxRate;
	
	public BuyList(BuyListHolder.NpcTradeList tradeList, Player activeChar, double taxRate)
	{
		_adena = activeChar.getAdena();
		_taxRate = taxRate;
		if(tradeList != null)
		{
			_listId = tradeList.getListId();
			_buyList = tradeList.getItems();
			activeChar.setBuyListId(_listId);
		}
		else
		{
			_listId = 0;
			_buyList = Collections.emptyList();
			activeChar.setBuyListId(0);
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(17);
		writeD((int) _adena);
		writeD(_listId);
		writeH(_buyList.size());
		for(TradeItem item : _buyList)
		{
			writeH(item.getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD((int) item.getCurrentValue());
			writeH(item.getType2());
			writeH(item.getCustomType1());
			writeD(item.getBodyPart());
			writeH(item.getEnchantLevel());
			writeD(0);
			writeD((int) ((double) item.getOwnersPrice() * (1.0 + _taxRate)));
		}
	}
}