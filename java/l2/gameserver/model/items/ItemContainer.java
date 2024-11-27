package l2.gameserver.model.items;

import l2.commons.math.SafeMath;
import l2.gameserver.dao.ItemsDAO;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class ItemContainer
{
	protected static final ItemsDAO _itemsDAO = ItemsDAO.getInstance();
	private static final Logger _log = LoggerFactory.getLogger(ItemContainer.class);
	protected final List<ItemInstance> _items = new ArrayList<>();
	protected final ReadWriteLock lock = new ReentrantReadWriteLock();
	protected final Lock readLock = lock.readLock();
	protected final Lock writeLock = lock.writeLock();
	
	protected ItemContainer()
	{
	}
	
	public int getSize()
	{
		return _items.size();
	}
	
	public ItemInstance[] getItems()
	{
		readLock();
		try
		{
			ItemInstance[] arritemInstance = _items.toArray(new ItemInstance[_items.size()]);
			return arritemInstance;
		}
		finally
		{
			readUnlock();
		}
	}
	
	public void clear()
	{
		writeLock();
		try
		{
			_items.clear();
		}
		finally
		{
			writeUnlock();
		}
	}
	
	public final void writeLock()
	{
		writeLock.lock();
	}
	
	public final void writeUnlock()
	{
		writeLock.unlock();
	}
	
	public final void readLock()
	{
		readLock.lock();
	}
	
	public final void readUnlock()
	{
		readLock.unlock();
	}
	
	public ItemInstance getItemByObjectId(int objectId)
	{
		readLock();
		try
		{
			for(int i = 0;i < _items.size();++i)
			{
				ItemInstance item = _items.get(i);
				if(item.getObjectId() != objectId)
					continue;
				ItemInstance itemInstance = item;
				return itemInstance;
			}
		}
		finally
		{
			readUnlock();
		}
		return null;
	}
	
	public ItemInstance getItemByItemId(int itemId)
	{
		readLock();
		try
		{
			for(int i = 0;i < _items.size();++i)
			{
				ItemInstance item = _items.get(i);
				if(item.getItemId() != itemId)
					continue;
				ItemInstance itemInstance = item;
				return itemInstance;
			}
		}
		finally
		{
			readUnlock();
		}
		return null;
	}
	
	public List<ItemInstance> getItemsByItemId(int itemId)
	{
		ArrayList<ItemInstance> result = new ArrayList<>();
		readLock();
		try
		{
			for(int i = 0;i < _items.size();++i)
			{
				ItemInstance item = _items.get(i);
				if(item.getItemId() != itemId)
					continue;
				result.add(item);
			}
		}
		finally
		{
			readUnlock();
		}
		return result;
	}
	
	public long getCountOf(int itemId)
	{
		long count = 0;
		readLock();
		try
		{
			for(int i = 0;i < _items.size();++i)
			{
				ItemInstance item = _items.get(i);
				if(item.getItemId() != itemId)
					continue;
				count = SafeMath.addAndLimit(count, item.getCount());
			}
		}
		finally
		{
			readUnlock();
		}
		return count;
	}
	
	public ItemInstance addItem(int itemId, long count)
	{
		ItemInstance item;
		block8:
		{
			if(count < 1)
			{
				return null;
			}
			writeLock();
			try
			{
				item = getItemByItemId(itemId);
				if(item != null && item.isStackable())
				{
					ItemInstance itemInstance = item;
					synchronized(itemInstance)
					{
						item.setCount(SafeMath.addAndLimit(item.getCount(), count));
						onModifyItem(item);
						break block8;
					}
				}
				item = ItemFunctions.createItem(itemId);
				item.setCount(count);
				_items.add(item);
				onAddItem(item);
			}
			finally
			{
				writeUnlock();
			}
		}
		return item;
	}
	
	public ItemInstance addItem(ItemInstance item)
	{
		if(item == null)
		{
			return null;
		}
		if(item.getCount() < 1)
		{
			return null;
		}
		ItemInstance result = null;
		writeLock();
		try
		{
			if(getItemByObjectId(item.getObjectId()) != null)
			{
				ItemInstance itemInstance = null;
				return itemInstance;
			}
			if(item.isStackable())
			{
				int itemId = item.getItemId();
				result = getItemByItemId(itemId);
				if(result != null)
				{
					ItemInstance itemInstance = result;
					synchronized(itemInstance)
					{
						result.setCount(SafeMath.addAndLimit(item.getCount(), result.getCount()));
						onModifyItem(result);
						onDestroyItem(item);
					}
				}
			}
			if(result == null)
			{
				_items.add(item);
				result = item;
				onAddItem(result);
			}
		}
		finally
		{
			writeUnlock();
		}
		return result;
	}
	
	public ItemInstance removeItemByObjectId(int objectId, long count)
	{
		if(count < 1)
		{
			return null;
		}
		writeLock();
		ItemInstance result;
		try
		{
			ItemInstance item = getItemByObjectId(objectId);
			if(item == null)
			{
				ItemInstance itemInstance = null;
				return itemInstance;
			}
			ItemInstance itemInstance = item;
			synchronized(itemInstance)
			{
				result = removeItem(item, count);
			}
		}
		finally
		{
			writeUnlock();
		}
		return result;
	}
	
	public ItemInstance removeItemByItemId(int itemId, long count)
	{
		if(count < 1)
		{
			return null;
		}
		writeLock();
		ItemInstance result;
		try
		{
			ItemInstance item = getItemByItemId(itemId);
			if(item == null)
			{
				ItemInstance itemInstance = null;
				return itemInstance;
			}
			ItemInstance itemInstance = item;
			synchronized(itemInstance)
			{
				result = removeItem(item, count);
			}
		}
		finally
		{
			writeUnlock();
		}
		return result;
	}
	
	public ItemInstance removeItem(ItemInstance item, long count)
	{
		if(item == null)
		{
			return null;
		}
		if(count < 1)
		{
			return null;
		}
		if(item.getCount() < count)
		{
			return null;
		}
		writeLock();
		try
		{
			if(!_items.contains(item))
			{
				ItemInstance itemInstance = null;
				return itemInstance;
			}
			if(item.getCount() > count)
			{
				item.setCount(item.getCount() - count);
				onModifyItem(item);
				ItemInstance newItem = new ItemInstance(IdFactory.getInstance().getNextId(), item.getItemId());
				newItem.setCount(count);
				ItemInstance itemInstance = newItem;
				return itemInstance;
			}
			ItemInstance newItem = removeItem(item);
			return newItem;
		}
		finally
		{
			writeUnlock();
		}
	}
	
	public ItemInstance removeItem(ItemInstance item)
	{
		if(item == null)
		{
			return null;
		}
		writeLock();
		try
		{
			if(!_items.remove(item))
			{
				ItemInstance itemInstance = null;
				return itemInstance;
			}
			onRemoveItem(item);
			ItemInstance itemInstance = item;
			return itemInstance;
		}
		finally
		{
			writeUnlock();
		}
	}
	
	public boolean destroyItemByObjectId(int objectId, long count)
	{
		writeLock();
		try
		{
			ItemInstance item = getItemByObjectId(objectId);
			if(item == null)
			{
				boolean bl = false;
				return bl;
			}
			ItemInstance itemInstance = item;
			synchronized(itemInstance)
			{
				boolean bl = destroyItem(item, count);
				return bl;
			}
		}
		finally
		{
			writeUnlock();
		}
	}
	
	public boolean destroyItemByItemId(int itemId, long count)
	{
		writeLock();
		try
		{
			ItemInstance item = getItemByItemId(itemId);
			if(item == null)
			{
				boolean bl = false;
				return bl;
			}
			ItemInstance itemInstance = item;
			synchronized(itemInstance)
			{
				boolean bl = destroyItem(item, count);
				return bl;
			}
		}
		finally
		{
			writeUnlock();
		}
	}
	
	public boolean destroyItem(ItemInstance item, long count)
	{
		if(item == null)
		{
			return false;
		}
		if(count < 1)
		{
			return false;
		}
		if(item.getCount() < count)
		{
			return false;
		}
		writeLock();
		try
		{
			if(!_items.contains(item))
			{
				boolean bl = false;
				return bl;
			}
			if(item.getCount() > count)
			{
				item.setCount(item.getCount() - count);
				onModifyItem(item);
				boolean bl = true;
				return bl;
			}
			boolean bl = destroyItem(item);
			return bl;
		}
		finally
		{
			writeUnlock();
		}
	}
	
	public boolean destroyItem(ItemInstance item)
	{
		if(item == null)
		{
			return false;
		}
		writeLock();
		try
		{
			if(!_items.remove(item))
			{
				boolean bl = false;
				return bl;
			}
			onRemoveItem(item);
			onDestroyItem(item);
			boolean bl = true;
			return bl;
		}
		finally
		{
			writeUnlock();
		}
	}
	
	protected abstract void onAddItem(ItemInstance item);
	
	protected abstract void onModifyItem(ItemInstance item);
	
	protected abstract void onRemoveItem(ItemInstance item);
	
	protected abstract void onDestroyItem(ItemInstance item);
}