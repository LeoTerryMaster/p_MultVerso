package l2.gameserver.model.items;

import l2.gameserver.model.pledge.Clan;

public final class ClanWarehouse extends Warehouse
{
	public ClanWarehouse(Clan clan)
	{
		super(clan.getClanId());
	}
	
	@Override
	public ItemInstance.ItemLocation getItemLocation()
	{
		return ItemInstance.ItemLocation.CLANWH;
	}
}