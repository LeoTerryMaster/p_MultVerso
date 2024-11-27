package l2.gameserver.handler.items;

import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.templates.item.ItemTemplate;

public class ItemHandler extends AbstractHolder
{
	private static final ItemHandler _instance = new ItemHandler();
	
	private ItemHandler()
	{
	}
	
	public static ItemHandler getInstance()
	{
		return _instance;
	}
	
	public void registerItemHandler(IItemHandler handler)
	{
		int[] ids = handler.getItemIds();
		for(int itemId : ids)
		{
			ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
			if(template == null)
			{
				warn("Item not found: " + itemId + " handler: " + handler.getClass().getSimpleName());
				continue;
			}
			if(template.getHandler() != IItemHandler.NULL)
			{
				warn("Duplicate handler for item: " + itemId + "(" + template.getHandler().getClass().getSimpleName() + "," + handler.getClass().getSimpleName() + ")");
				continue;
			}
			template.setHandler(handler);
		}
	}
	
	public void unregisterItemHandler(IItemHandler handler)
	{
		int[] ids = handler.getItemIds();
		for(int itemId : ids)
		{
			ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
			if(template == null)
			{
				warn("Item not found: " + itemId + " handler: " + handler.getClass().getSimpleName());
				continue;
			}
			if(template.getHandler() != handler)
			{
				warn("Attempt to unregister item handler");
				continue;
			}
			template.setHandler(handler);
		}
	}
	
	@Override
	public int size()
	{
		return 0;
	}
	
	@Override
	public void clear()
	{
	}
}