package l2.gameserver.model.items;

import l2.gameserver.model.Player;

public class PcWarehouse extends Warehouse
{
	public PcWarehouse(Player owner)
	{
		super(owner.getObjectId());
	}
	
	public PcWarehouse(int ownerId)
	{
		super(ownerId);
	}
	
	@Override
	public ItemInstance.ItemLocation getItemLocation()
	{
		return ItemInstance.ItemLocation.WAREHOUSE;
	}
}