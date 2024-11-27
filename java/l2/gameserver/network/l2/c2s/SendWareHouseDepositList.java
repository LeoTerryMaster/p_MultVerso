package l2.gameserver.network.l2.c2s;

import l2.commons.math.SafeMath;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.PcInventory;
import l2.gameserver.model.items.Warehouse;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;

public class SendWareHouseDepositList extends L2GameClientPacket
{
	private static final long _WAREHOUSE_FEE = 30;
	private int _count;
	private int[] _items;
	private long[] _itemQ;
	
	@Override
	protected void readImpl()
	{
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
			if(_itemQ[i] >= 1 && ArrayUtils.indexOf(_items, _items[i]) >= i)
				continue;
			_count = 0;
			return;
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
		if(!activeChar.getPlayerAccess().UseWarehouse)
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
		NpcInstance whkeeper = activeChar.getLastNpc();
		if(whkeeper == null || !whkeeper.isInActingRange(activeChar))
		{
			activeChar.sendPacket(Msg.WAREHOUSE_IS_TOO_FAR);
			return;
		}
		PcInventory inventory = activeChar.getInventory();
		boolean privatewh = activeChar.getUsingWarehouseType() != Warehouse.WarehouseType.CLAN;
		Warehouse warehouse = privatewh ? activeChar.getWarehouse() : activeChar.getClan().getWarehouse();
		inventory.writeLock();
		warehouse.writeLock();
		try
		{
			long adenaDeposit = 0;
			int slotsleft = privatewh ? activeChar.getWarehouseLimit() - warehouse.getSize() : activeChar.getClan().getWhBonus() + Config.WAREHOUSE_SLOTS_CLAN - warehouse.getSize();
			int items = 0;
			for(int i = 0;i < _count;++i)
			{
				ItemInstance item = inventory.getItemByObjectId(_items[i]);
				if(item == null || item.getCount() < _itemQ[i] || !item.canBeStored(activeChar, privatewh))
				{
					_items[i] = 0;
					_itemQ[i] = 0;
					continue;
				}
				if(!item.isStackable() || warehouse.getItemByItemId(item.getItemId()) == null)
				{
					if(slotsleft <= 0)
					{
						_items[i] = 0;
						_itemQ[i] = 0;
						continue;
					}
					--slotsleft;
				}
				if(item.getItemId() == 57)
				{
					adenaDeposit = _itemQ[i];
				}
				++items;
			}
			if(slotsleft <= 0)
			{
				activeChar.sendPacket(Msg.YOUR_WAREHOUSE_IS_FULL);
			}
			if(items == 0)
			{
				activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return;
			}
			long fee = SafeMath.mulAndCheck((long) items, 30);
			if(fee + adenaDeposit > activeChar.getAdena())
			{
				activeChar.sendPacket(Msg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
				return;
			}
			if(!activeChar.reduceAdena(fee, true))
			{
				sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			for(int i = 0;i < _count;++i)
			{
				if(_items[i] == 0)
					continue;
				ItemInstance item = inventory.removeItemByObjectId(_items[i], _itemQ[i]);
				Log.LogItem(activeChar, privatewh ? Log.ItemLog.WarehouseDeposit : Log.ItemLog.ClanWarehouseDeposit, item);
				warehouse.addItem(item);
			}
		}
		catch(ArithmeticException e)
		{
			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		finally
		{
			warehouse.writeUnlock();
			inventory.writeUnlock();
		}
		activeChar.sendChanges();
		activeChar.sendPacket(Msg.THE_TRANSACTION_IS_COMPLETE);
	}
}