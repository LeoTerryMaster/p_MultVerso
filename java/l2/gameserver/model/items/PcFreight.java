package l2.gameserver.model.items;

import l2.gameserver.model.Player;

public class PcFreight extends Warehouse
{
	public PcFreight(Player player)
	{
		super(player.getObjectId());
	}
	
	public PcFreight(int objectId)
	{
		super(objectId);
	}
	
	@Override
	public ItemInstance.ItemLocation getItemLocation()
	{
		return ItemInstance.ItemLocation.FREIGHT;
	}
}