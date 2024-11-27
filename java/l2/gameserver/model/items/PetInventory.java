package l2.gameserver.model.items;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.PetInstance;
import l2.gameserver.network.l2.s2c.PetInventoryUpdate;
import l2.gameserver.utils.ItemFunctions;

import java.util.Collection;

public class PetInventory extends Inventory
{
	private final PetInstance _actor;
	
	public PetInventory(PetInstance actor)
	{
		super(actor.getPlayer().getObjectId());
		_actor = actor;
	}
	
	@Override
	public PetInstance getActor()
	{
		return _actor;
	}
	
	public Player getOwner()
	{
		return _actor.getPlayer();
	}
	
	@Override
	protected ItemInstance.ItemLocation getBaseLocation()
	{
		return ItemInstance.ItemLocation.PET_INVENTORY;
	}
	
	@Override
	protected ItemInstance.ItemLocation getEquipLocation()
	{
		return ItemInstance.ItemLocation.PET_PAPERDOLL;
	}
	
	@Override
	protected void onRefreshWeight()
	{
		getActor().sendPetInfo();
	}
	
	@Override
	protected void sendAddItem(ItemInstance item)
	{
		getOwner().sendPacket(new PetInventoryUpdate().addNewItem(item));
	}
	
	@Override
	protected void sendModifyItem(ItemInstance item)
	{
		getOwner().sendPacket(new PetInventoryUpdate().addModifiedItem(item));
	}
	
	@Override
	protected void sendRemoveItem(ItemInstance item)
	{
		getOwner().sendPacket(new PetInventoryUpdate().addRemovedItem(item));
	}
	
	@Override
	public void restore()
	{
		int ownerId = getOwnerId();
		writeLock();
		try
		{
			Collection<ItemInstance> items = _itemsDAO.loadItemsByOwnerIdAndLoc(ownerId, getBaseLocation());
			for(ItemInstance item : items)
			{
				_items.add(item);
				onRestoreItem(item);
			}
			items = _itemsDAO.loadItemsByOwnerIdAndLoc(ownerId, getEquipLocation());
			for(ItemInstance item : items)
			{
				_items.add(item);
				onRestoreItem(item);
				if(ItemFunctions.checkIfCanEquip(getActor(), item) != null)
					continue;
				setPaperdollItem(item.getEquipSlot(), item);
			}
		}
		finally
		{
			writeUnlock();
		}
		refreshWeight();
	}
	
	@Override
	public void store()
	{
		writeLock();
		try
		{
			_itemsDAO.store(_items);
		}
		finally
		{
			writeUnlock();
		}
	}
	
	public void validateItems()
	{
		for(ItemInstance item : _paperdoll)
		{
			if(item == null || ItemFunctions.checkIfCanEquip(getActor(), item) == null && item.getTemplate().testCondition(getActor(), item, false))
				continue;
			unEquipItem(item);
		}
	}
}