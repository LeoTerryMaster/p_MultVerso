package l2.gameserver.network.l2.c2s;

import l2.commons.math.SafeMath;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.SellRefundList;
import l2.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;

public class RequestSellItem extends L2GameClientPacket
{
	private int _listId;
	private int _count;
	private int[] _items;
	private long[] _itemQ;
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		_count = readD();
		if(_count * 12 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_itemQ = new long[_count];
		for(int i = 0;i < _count;++i)
		{
			_items[i] = readD();
			readD();
			_itemQ[i] = readD();
			if(_itemQ[i] >= 1 && ArrayUtils.indexOf(_items, _items[i]) >= i)
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
		if(!(activeChar.isGM() || merchant != null && isValidMerchant && activeChar.isInActingRange(merchant)))
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.getInventory().writeLock();
		try
		{
			for(int i = 0;i < _count;++i)
			{
				int objectId = _items[i];
				long count = _itemQ[i];
				ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
				if(item == null || item.getCount() < count || !item.canBeSold(activeChar))
					continue;
				long price = SafeMath.mulAndCheck(Math.max(1, (long) item.getReferencePrice() / Config.ALT_SHOP_REFUND_SELL_DIVISOR), count);
				ItemInstance refund = activeChar.getInventory().removeItemByObjectId(objectId, count);
				Log.LogItem(activeChar, Log.ItemLog.RefundSell, refund);
				activeChar.addAdena(price);
				activeChar.getRefund().addItem(refund);
			}
		}
		catch(ArithmeticException e)
		{
			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}
		activeChar.sendPacket(new SellRefundList(activeChar, true));
		activeChar.sendChanges();
	}
}