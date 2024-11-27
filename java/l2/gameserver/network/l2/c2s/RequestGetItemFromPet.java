package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.PetInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.PcInventory;
import l2.gameserver.model.items.PetInventory;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.utils.Log;

public class RequestGetItemFromPet extends L2GameClientPacket
{
	private int _objectId;
	private long _amount;
	private int _unknown;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readD();
		_unknown = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _amount < 1)
		{
			return;
		}
		PetInstance pet = (PetInstance) activeChar.getPet();
		if(pet == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if(activeChar.isInTrade() || activeChar.isProcessingRequest())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_);
			return;
		}
		PetInventory petInventory = pet.getInventory();
		PcInventory playerInventory = activeChar.getInventory();
		petInventory.writeLock();
		playerInventory.writeLock();
		try
		{
			ItemInstance item = petInventory.getItemByObjectId(_objectId);
			if(item == null || item.getCount() < _amount || item.isEquipped())
			{
				activeChar.sendActionFailed();
				return;
			}
			boolean slots = false;
			long weight = (long) item.getTemplate().getWeight() * _amount;
			if(!item.getTemplate().isStackable() || activeChar.getInventory().getItemByItemId(item.getItemId()) == null)
			{
				slots = true;
			}
			if(!activeChar.getInventory().validateWeight(weight))
			{
				activeChar.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}
			if(!activeChar.getInventory().validateCapacity(slots ? 1 : 0))
			{
				activeChar.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
				return;
			}
			item = petInventory.removeItemByObjectId(_objectId, _amount);
			Log.LogItem(activeChar, Log.ItemLog.FromPet, item);
			playerInventory.addItem(item);
			pet.sendChanges();
			activeChar.sendChanges();
		}
		finally
		{
			playerInventory.writeUnlock();
			petInventory.writeUnlock();
		}
	}
}