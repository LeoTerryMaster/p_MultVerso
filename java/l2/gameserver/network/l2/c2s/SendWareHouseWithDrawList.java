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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendWareHouseWithDrawList extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(SendWareHouseWithDrawList.class);
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
		Warehouse warehouse;
		Log.ItemLog logType;
		if(activeChar.getUsingWarehouseType() == Warehouse.WarehouseType.PRIVATE)
		{
			warehouse = activeChar.getWarehouse();
			logType = Log.ItemLog.WarehouseWithdraw;
		}
		else if(activeChar.getUsingWarehouseType() == Warehouse.WarehouseType.CLAN)
		{
			logType = Log.ItemLog.ClanWarehouseWithdraw;
			boolean canWithdrawCWH = false;
			if(activeChar.getClan() != null && (activeChar.getClanPrivileges() & 8) == 8 && (Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE || activeChar.isClanLeader() || activeChar.getVarB("canWhWithdraw")))
			{
				canWithdrawCWH = true;
			}
			if(!canWithdrawCWH)
			{
				return;
			}
			warehouse = activeChar.getClan().getWarehouse();
		}
		else if(activeChar.getUsingWarehouseType() == Warehouse.WarehouseType.FREIGHT)
		{
			warehouse = activeChar.getFreight();
			logType = Log.ItemLog.FreightWithdraw;
		}
		else
		{
			_log.warn("Error retrieving a warehouse object for char " + activeChar.getName() + " - using warehouse type: " + activeChar.getUsingWarehouseType());
			return;
		}
		PcInventory inventory = activeChar.getInventory();
		inventory.writeLock();
		warehouse.writeLock();
		try
		{
			ItemInstance item;
			int i;
			long weight = 0;
			int slots = 0;
			for(i = 0;i < _count;++i)
			{
				item = warehouse.getItemByObjectId(_items[i]);
				if(item == null || item.getCount() < _itemQ[i])
				{
					activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
					return;
				}
				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck((long) item.getTemplate().getWeight(), _itemQ[i]));
				if(item.isStackable() && inventory.getItemByItemId(item.getItemId()) != null)
					continue;
				++slots;
			}
			if(!activeChar.getInventory().validateCapacity(slots))
			{
				activeChar.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
				return;
			}
			if(!activeChar.getInventory().validateWeight(weight))
			{
				activeChar.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}
			for(i = 0;i < _count;++i)
			{
				item = warehouse.removeItemByObjectId(_items[i], _itemQ[i]);
				Log.LogItem(activeChar, logType, item);
				activeChar.getInventory().addItem(item);
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