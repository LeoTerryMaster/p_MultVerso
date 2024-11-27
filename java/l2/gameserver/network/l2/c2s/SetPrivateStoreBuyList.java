package l2.gameserver.network.l2.c2s;

import l2.commons.math.SafeMath;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.TradeItem;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.PrivateStoreManageListBuy;
import l2.gameserver.network.l2.s2c.PrivateStoreMsgBuy;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.TradeHelper;

import java.util.concurrent.CopyOnWriteArrayList;

public class SetPrivateStoreBuyList extends L2GameClientPacket
{
	private int _count;
	private int[] _items;
	private long[] _itemQ;
	private long[] _itemP;
	
	@Override
	protected void readImpl()
	{
		_count = readD();
		if(_count * 12 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_itemQ = new long[_count];
		_itemP = new long[_count];
		for(int i = 0;i < _count;++i)
		{
			_items[i] = readD();
			readH();
			readH();
			_itemQ[i] = readD();
			_itemP[i] = readD();
			if(_itemQ[i] >= 1 && _itemP[i] >= 1)
				continue;
			_count = 0;
			break;
		}
	}
	
	@Override
	protected void runImpl()
	{
		Player buyer = getClient().getActiveChar();
		if(buyer == null || _count == 0)
		{
			return;
		}
		if(!TradeHelper.checksIfCanOpenStore(buyer, 3))
		{
			buyer.sendActionFailed();
			return;
		}
		CopyOnWriteArrayList<TradeItem> buyList = new CopyOnWriteArrayList<>();
		long totalCost = 0;
		try
		{
			block2:
			for(int i = 0;i < _count;++i)
			{
				int itemId = _items[i];
				long count = _itemQ[i];
				long price = _itemP[i];
				ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
				if(item == null || itemId == 57)
					continue;
				if((long) (item.getReferencePrice() / 2) > price)
				{
					buyer.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.SetPrivateStoreBuyList.TooLowPrice", buyer).addItemName(item).addNumber(item.getReferencePrice() / 2));
					continue;
				}
				if(item.isStackable())
				{
					for(TradeItem bi : buyList)
					{
						if(bi.getItemId() != itemId)
							continue;
						bi.setOwnersPrice(price);
						bi.setCount(bi.getCount() + count);
						totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
						continue block2;
					}
				}
				TradeItem bi = new TradeItem();
				bi.setItemId(itemId);
				bi.setCount(count);
				bi.setOwnersPrice(price);
				totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
				buyList.add(bi);
			}
		}
		catch(ArithmeticException e)
		{
			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		if(buyList.size() > buyer.getTradeLimit())
		{
			buyer.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			buyer.sendPacket(new PrivateStoreManageListBuy(buyer));
			return;
		}
		if(totalCost > buyer.getAdena())
		{
			buyer.sendPacket(Msg.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE);
			buyer.sendPacket(new PrivateStoreManageListBuy(buyer));
			return;
		}
		if(!buyList.isEmpty())
		{
			buyer.setBuyList(buyList);
			buyer.saveTradeList();
			buyer.setPrivateStoreType(3);
			buyer.broadcastPacket(new PrivateStoreMsgBuy(buyer));
			buyer.sitDown(null);
			buyer.broadcastCharInfo();
		}
		buyer.sendActionFailed();
	}
}