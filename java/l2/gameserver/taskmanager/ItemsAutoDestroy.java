package l2.gameserver.taskmanager;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.model.items.ItemInstance;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ItemsAutoDestroy
{
	private static ItemsAutoDestroy _instance;
	private final ConcurrentLinkedQueue<ItemInstance> _herbs = new ConcurrentLinkedQueue();
	private ConcurrentLinkedQueue<ItemInstance> _items;
	
	private ItemsAutoDestroy()
	{
		if(Config.AUTODESTROY_ITEM_AFTER > 0)
		{
			_items = new ConcurrentLinkedQueue();
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckItemsForDestroy(), 60000, 60000);
		}
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckHerbsForDestroy(), 1000, 1000);
	}
	
	public static ItemsAutoDestroy getInstance()
	{
		if(_instance == null)
		{
			_instance = new ItemsAutoDestroy();
		}
		return _instance;
	}
	
	public void addItem(ItemInstance item)
	{
		item.setDropTime(System.currentTimeMillis());
		_items.add(item);
	}
	
	public void addHerb(ItemInstance herb)
	{
		herb.setDropTime(System.currentTimeMillis());
		_herbs.add(herb);
	}
	
	public class CheckHerbsForDestroy extends RunnableImpl
	{
		static final long _sleep = 60000;
		
		@Override
		public void runImpl() throws Exception
		{
			long curtime = System.currentTimeMillis();
			for(ItemInstance item : _herbs)
			{
				if(item == null || item.getLastDropTime() == 0 || item.getLocation() != ItemInstance.ItemLocation.VOID)
				{
					_herbs.remove(item);
					continue;
				}
				if(item.getLastDropTime() + 60000 >= curtime)
					continue;
				item.deleteMe();
				_herbs.remove(item);
			}
		}
	}
	
	public class CheckItemsForDestroy extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			long _sleep = (long) Config.AUTODESTROY_ITEM_AFTER * 1000;
			long curtime = System.currentTimeMillis();
			for(ItemInstance item : _items)
			{
				if(item == null || item.getLastDropTime() == 0 || item.getLocation() != ItemInstance.ItemLocation.VOID)
				{
					_items.remove(item);
					continue;
				}
				if(item.getLastDropTime() + _sleep >= curtime)
					continue;
				item.deleteMe();
				item.delete();
				_items.remove(item);
			}
		}
	}
}