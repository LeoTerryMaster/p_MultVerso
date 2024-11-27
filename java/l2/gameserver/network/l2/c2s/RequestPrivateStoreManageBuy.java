package l2.gameserver.network.l2.c2s;

import l2.commons.math.SafeMath;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.TradeItem;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.TradeHelper;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class RequestPrivateStoreManageBuy extends L2GameClientPacket
{
	private int _sellerId;
	private int _count;
	private int[] _items;
	private long[] _itemQ;
	private long[] _itemP;
	
	@Override
	protected void readImpl()
	{
		_sellerId = readD();
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
			_itemQ[i] = readD();
			_itemP[i] = readD();
			if(_itemQ[i] >= 1 && _itemP[i] >= 1 && ArrayUtils.indexOf(_items, _items[i]) >= i)
				continue;
			_count = 0;
			break;
		}
	}
	
	@Override
	protected void runImpl()
	{
		Player seller;
		List<TradeItem> sellList;
		Player buyer;
		block45:
		{
			buyer = getClient().getActiveChar();
			if(buyer == null || _count == 0)
			{
				return;
			}
			if(buyer.isActionsDisabled())
			{
				buyer.sendActionFailed();
				return;
			}
			if(buyer.isInStoreMode())
			{
				buyer.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
				return;
			}
			if(buyer.isInTrade())
			{
				buyer.sendActionFailed();
				return;
			}
			if(buyer.isFishing())
			{
				buyer.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
				return;
			}
			if(!buyer.getPlayerAccess().UseTrade)
			{
				buyer.sendPacket(Msg.THIS_ACCOUNT_CANOT_USE_PRIVATE_STORES);
				return;
			}
			seller = (Player) buyer.getVisibleObject(_sellerId);
			if(seller == null || seller.getPrivateStoreType() != 1 && seller.getPrivateStoreType() != 8 || !seller.isInActingRange(buyer))
			{
				buyer.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED);
				buyer.sendActionFailed();
				return;
			}
			sellList = seller.getSellList();
			if(sellList.isEmpty())
			{
				buyer.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED);
				buyer.sendActionFailed();
				return;
			}
			buyer.getInventory().writeLock();
			seller.getInventory().writeLock();
			long weight = 0;
			int slots = 0;
			long totalCost = 0;
			ArrayList<TradeItem> buyList = new ArrayList<>();
			try
			{
				block24:
				for(int i = 0;i < _count;++i)
				{
					int objectId = _items[i];
					long count = _itemQ[i];
					long price = _itemP[i];
					for(TradeItem si : sellList)
					{
						if(si.getObjectId() != objectId || si.getOwnersPrice() != price)
							continue;
						if(count > si.getCount())
						{
							break block45;
						}
						ItemInstance item = seller.getInventory().getItemByObjectId(objectId);
						if(item == null || item.getCount() < count)
							break block45;
						if(!item.canBeTraded(seller))
						{
							break block45;
						}
						totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
						weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, (long) item.getTemplate().getWeight()));
						if(!item.isStackable() || buyer.getInventory().getItemByItemId(item.getItemId()) == null)
						{
							++slots;
						}
						TradeItem bi = new TradeItem();
						bi.setObjectId(objectId);
						bi.setItemId(item.getItemId());
						bi.setCount(count);
						bi.setOwnersPrice(price);
						buyList.add(bi);
						continue block24;
					}
				}
			}
			catch(ArithmeticException e)
			{
				buyList.clear();
				sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}
			finally
			{
				try
				{
					if(buyList.size() != _count || seller.getPrivateStoreType() == 8 && buyList.size() != sellList.size())
					{
						buyer.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED);
						buyer.sendActionFailed();
						return;
					}
					if(!buyer.getInventory().validateWeight(weight))
					{
						buyer.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
						buyer.sendActionFailed();
						return;
					}
					if(!buyer.getInventory().validateCapacity(slots))
					{
						buyer.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
						buyer.sendActionFailed();
						return;
					}
					if(!buyer.reduceAdena(totalCost))
					{
						buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
						buyer.sendActionFailed();
						return;
					}
					for(TradeItem bi : buyList)
					{
						ItemInstance item = seller.getInventory().removeItemByObjectId(bi.getObjectId(), bi.getCount());
						for(TradeItem si : sellList)
						{
							if(si.getObjectId() != bi.getObjectId())
								continue;
							si.setCount(si.getCount() - bi.getCount());
							if(si.getCount() >= 1)
								break;
							sellList.remove(si);
							break;
						}
						Log.LogItem(seller, Log.ItemLog.PrivateStoreSell, item);
						Log.LogItem(buyer, Log.ItemLog.PrivateStoreBuy, item);
						buyer.getInventory().addItem(item);
						TradeHelper.purchaseItem(buyer, seller, bi);
					}
					long tax = TradeHelper.getTax(seller, totalCost);
					if(tax > 0)
					{
						totalCost -= tax;
						seller.sendMessage(new CustomMessage("trade.HavePaidTax", seller).addNumber(tax));
					}
					seller.addAdena(totalCost);
					seller.saveTradeList();
				}
				finally
				{
					seller.getInventory().writeUnlock();
					buyer.getInventory().writeUnlock();
				}
			}
		}
		if(sellList.isEmpty())
		{
			TradeHelper.cancelStore(seller);
		}
		seller.sendChanges();
		buyer.sendChanges();
		buyer.sendActionFailed();
	}
}