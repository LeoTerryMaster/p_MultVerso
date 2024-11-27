package l2.gameserver.network.l2.c2s;

import l2.commons.math.SafeMath;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.BuyListHolder;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.TradeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RequestBuyItem extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestBuyItem.class);
	private int _listId;
	private int _count;
	private int[] _items;
	private long[] _itemQ;
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		_count = readD();
		if(_count * 8 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_itemQ = new long[_count];
		for(int i = 0;i < _count;++i)
		{
			_items[i] = readD();
			_itemQ[i] = readD();
			if(_itemQ[i] >= 1)
				continue;
			_count = 0;
			break;
		}
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _count == 0)
		{
			return;
		}
		if(activeChar.getBuyListId() != _listId)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getKarma() > 0 && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}
		NpcInstance merchant = activeChar.getLastNpc();
		boolean isValidMerchant;
		boolean bl = isValidMerchant = merchant != null && merchant.isMerchantNpc();
		if(!(activeChar.isGM() || merchant != null && isValidMerchant && merchant.isInActingRange(activeChar)))
		{
			activeChar.sendActionFailed();
			return;
		}
		BuyListHolder.NpcTradeList list = BuyListHolder.getInstance().getBuyList(_listId);
		if(list == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		double taxRate = 0.0;
		Castle castle = null;
		if(merchant != null && (castle = merchant.getCastle(activeChar)) != null)
		{
			taxRate = castle.getTaxRate();
		}
		ArrayList<TradeItem> buyList = new ArrayList<>(_count);
		List<TradeItem> tradeList = list.getItems();
		try
		{
			long totalPrice = 0;
			long weight = 0;
			int slots = 0;
			block2:
			for(int i = 0;i < _count;++i)
			{
				int itemId = _items[i];
				long count = _itemQ[i];
				long price = 0;
				for(TradeItem ti : tradeList)
				{
					if(ti.getItemId() != itemId)
						continue;
					if(ti.isCountLimited() && ti.getCurrentValue() < count)
						continue block2;
					price = ti.getOwnersPrice();
				}
				if(!(price != 0 || activeChar.isGM() && activeChar.getPlayerAccess().UseGMShop))
				{
					activeChar.sendActionFailed();
					return;
				}
				totalPrice = SafeMath.addAndCheck(totalPrice, SafeMath.mulAndCheck(count, price));
				TradeItem ti = new TradeItem();
				ti.setItemId(itemId);
				ti.setCount(count);
				ti.setOwnersPrice(price);
				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, (long) ti.getItem().getWeight()));
				if(!ti.getItem().isStackable() || activeChar.getInventory().getItemByItemId(itemId) == null)
				{
					++slots;
				}
				buyList.add(ti);
			}
			long tax = (long) ((double) totalPrice * taxRate);
			totalPrice = SafeMath.addAndCheck(totalPrice, tax);
			if(!activeChar.getInventory().validateWeight(weight))
			{
				sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}
			if(!activeChar.getInventory().validateCapacity(slots))
			{
				sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
				return;
			}
			if(!activeChar.reduceAdena(totalPrice))
			{
				activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			for(TradeItem ti : buyList)
			{
				activeChar.getInventory().addItem(ti.getItemId(), ti.getCount());
			}
			list.updateItems(buyList);
			if(castle != null && tax > 0 && castle.getOwnerId() > 0 && activeChar.getReflection() == ReflectionManager.DEFAULT)
			{
				castle.addToTreasury(tax, true, false);
			}
		}
		catch(ArithmeticException e)
		{
			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		activeChar.sendChanges();
	}
}