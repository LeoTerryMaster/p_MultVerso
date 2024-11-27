package l2.gameserver.network.l2.c2s;

import l2.commons.math.SafeMath;
import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.PcFreight;
import l2.gameserver.model.items.PcInventory;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;

public class RequestPackageSend extends L2GameClientPacket
{
	private static final long _FREIGHT_FEE = 1000;
	private int _objectId;
	private int _count;
	private int[] _items;
	private int[] _itemQ;
	
	public static boolean CanSendItem(ItemInstance item)
	{
		if(!item.getTemplate().isTradeable())
		{
			return false;
		}
		return !item.isEquipped() && !item.getTemplate().isQuest() && !item.isAugmented();
	}
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readD();
		if(_count * 8 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_itemQ = new int[_count];
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
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null || _count == 0)
		{
			return;
		}
		if(!player.getPlayerAccess().UseWarehouse)
		{
			player.sendActionFailed();
			return;
		}
		if(player.isActionsDisabled())
		{
			player.sendActionFailed();
			return;
		}
		if(player.isInStoreMode())
		{
			player.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if(player.isInTrade())
		{
			player.sendActionFailed();
			return;
		}
		NpcInstance whkeeper = player.getLastNpc();
		if(whkeeper == null || !whkeeper.isInActingRange(whkeeper))
		{
			return;
		}
		if(!player.getAccountChars().containsKey(_objectId))
		{
			return;
		}
		PcInventory inventory = player.getInventory();
		PcFreight freight = new PcFreight(_objectId);
		freight.restore();
		inventory.writeLock();
		freight.writeLock();
		try
		{
			int slotsleft = Config.FREIGHT_SLOTS - freight.getSize();
			int items = 0;
			long adenaDeposit = 0;
			for(int i = 0;i < _count;++i)
			{
				ItemInstance item = inventory.getItemByObjectId(_items[i]);
				if(item == null || item.getCount() < (long) _itemQ[i] || !item.getTemplate().isTradeable() || !CanSendItem(item))
				{
					_items[i] = 0;
					_itemQ[i] = 0;
					continue;
				}
				if(!item.isStackable() || freight.getItemByItemId(item.getItemId()) == null)
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
				player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			}
			if(items == 0)
			{
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return;
			}
			long fee = SafeMath.mulAndCheck((long) items, 1000);
			if(fee + adenaDeposit > player.getAdena())
			{
				player.sendPacket(SystemMsg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
				return;
			}
			if(!player.reduceAdena(fee, true))
			{
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			for(int i = 0;i < _count;++i)
			{
				if(_items[i] == 0)
					continue;
				ItemInstance item = inventory.removeItemByObjectId(_items[i], _itemQ[i]);
				Log.LogItem(player, Log.ItemLog.FreightDeposit, item);
				freight.addItem(item);
			}
		}
		catch(ArithmeticException e)
		{
			player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		finally
		{
			freight.writeUnlock();
			inventory.writeUnlock();
		}
		player.sendChanges();
		player.sendPacket(SystemMsg.THE_TRANSACTION_IS_COMPLETE);
	}
}