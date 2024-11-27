package l2.gameserver.model.items;

import l2.gameserver.dao.ItemsDAO;
import l2.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public abstract class Warehouse extends ItemContainer
{
	private static final ItemsDAO _itemsDAO = ItemsDAO.getInstance();
	protected final int _ownerId;
	
	protected Warehouse(int ownerId)
	{
		_ownerId = ownerId;
	}
	
	public int getOwnerId()
	{
		return _ownerId;
	}
	
	public abstract ItemInstance.ItemLocation getItemLocation();
	
	public ItemInstance[] getItems(ItemTemplate.ItemClass itemClass)
	{
		ArrayList<ItemInstance> result = new ArrayList<>();
		readLock();
		try
		{
			for(int i = 0;i < _items.size();++i)
			{
				ItemInstance item = _items.get(i);
				if(itemClass != null && itemClass != ItemTemplate.ItemClass.ALL && item.getItemClass() != itemClass)
					continue;
				result.add(item);
			}
		}
		finally
		{
			readUnlock();
		}
		return result.toArray(new ItemInstance[result.size()]);
	}
	
	public long getCountOfAdena()
	{
		return getCountOf(57);
	}
	
	@Override
	protected void onAddItem(ItemInstance item)
	{
		item.setOwnerId(getOwnerId());
		item.setLocation(getItemLocation());
		item.setLocData(0);
		item.save();
	}
	
	@Override
	protected void onModifyItem(ItemInstance item)
	{
		item.save();
	}
	
	@Override
	protected void onRemoveItem(ItemInstance item)
	{
		item.setLocData(-1);
		item.save();
	}
	
	@Override
	protected void onDestroyItem(ItemInstance item)
	{
		item.setCount(0);
		item.delete();
	}
	
	public void restore()
	{
		int ownerId = getOwnerId();
		writeLock();
		try
		{
			Collection<ItemInstance> items = _itemsDAO.loadItemsByOwnerIdAndLoc(ownerId, getItemLocation());
			for(ItemInstance item : items)
			{
				_items.add(item);
			}
		}
		finally
		{
			writeUnlock();
		}
	}
	
	public enum WarehouseType
	{
		NONE,
		PRIVATE,
		CLAN,
		CASTLE,
		FREIGHT;
	}
	
	public static class ItemClassComparator implements Comparator<ItemInstance>
	{
		private static final Comparator<ItemInstance> instance = new ItemClassComparator();
		
		public static final Comparator<ItemInstance> getInstance()
		{
			return instance;
		}
		
		@Override
		public int compare(ItemInstance o1, ItemInstance o2)
		{
			if(o1 == null || o2 == null)
			{
				return 0;
			}
			int diff = o1.getItemClass().ordinal() - o2.getItemClass().ordinal();
			if(diff == 0)
			{
				diff = o1.getCrystalType().ordinal() - o2.getCrystalType().ordinal();
			}
			if(diff == 0)
			{
				diff = o1.getItemId() - o2.getItemId();
			}
			if(diff == 0)
			{
				diff = o1.getEnchantLevel() - o2.getEnchantLevel();
			}
			return diff;
		}
	}
}