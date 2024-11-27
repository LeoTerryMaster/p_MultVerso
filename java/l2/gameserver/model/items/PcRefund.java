package l2.gameserver.model.items;

import l2.gameserver.model.Player;

public class PcRefund extends ItemContainer
{
	public PcRefund(Player player)
	{
	}
	
	@Override
	protected void onAddItem(ItemInstance item)
	{
		item.setLocation(ItemInstance.ItemLocation.VOID);
		item.save();
		if(_items.size() > 12)
		{
			destroyItem(_items.remove(0));
		}
	}
	
	@Override
	protected void onModifyItem(ItemInstance item)
	{
		item.save();
	}
	
	@Override
	protected void onRemoveItem(ItemInstance item)
	{
		item.save();
	}
	
	@Override
	protected void onDestroyItem(ItemInstance item)
	{
		item.setCount(0);
		item.delete();
	}
	
	@Override
	public void clear()
	{
		writeLock();
		try
		{
			_itemsDAO.delete(_items);
			_items.clear();
		}
		finally
		{
			writeUnlock();
		}
	}
}