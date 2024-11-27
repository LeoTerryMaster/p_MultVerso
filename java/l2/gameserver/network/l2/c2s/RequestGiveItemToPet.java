package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.PetInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.PcInventory;
import l2.gameserver.model.items.PetInventory;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.utils.Log;

public class RequestGiveItemToPet extends L2GameClientPacket
{
	private int _objectId;
	private long _amount;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readD();
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
		if(pet.isDead())
		{
			activeChar.sendPacket(SystemMsg.YOUR_PET_IS_DEAD_AND_ANY_ATTEMPT_YOU_MAKE_TO_GIVE_IT_SOMETHING_GOES_UNRECOGNIZED);
			return;
		}
		if(_objectId == pet.getControlItemObjId())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendActionFailed();
			return;
		}
		PetInventory petInventory = pet.getInventory();
		PcInventory playerInventory = activeChar.getInventory();
		petInventory.writeLock();
		playerInventory.writeLock();
		try
		{
			ItemInstance item = playerInventory.getItemByObjectId(_objectId);
			if(item == null || item.getCount() < _amount || !item.canBeDropped(activeChar, false))
			{
				activeChar.sendActionFailed();
				return;
			}
			boolean slots = false;
			long weight = (long) item.getTemplate().getWeight() * _amount;
			if(!item.getTemplate().isStackable() || pet.getInventory().getItemByItemId(item.getItemId()) == null)
			{
				slots = true;
			}
			if(!pet.getInventory().validateWeight(weight))
			{
				activeChar.sendPacket(Msg.EXCEEDED_PET_INVENTORYS_WEIGHT_LIMIT);
				return;
			}
			if(!pet.getInventory().validateCapacity((long) (slots ? 1 : 0)))
			{
				activeChar.sendPacket(Msg.EXCEEDED_PET_INVENTORYS_WEIGHT_LIMIT);
				return;
			}
			item = playerInventory.removeItemByObjectId(_objectId, _amount);
			Log.LogItem(activeChar, Log.ItemLog.ToPet, item);
			petInventory.addItem(item);
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