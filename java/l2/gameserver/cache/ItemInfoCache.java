package l2.gameserver.cache;

import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.items.ItemInfo;
import l2.gameserver.model.items.ItemInstance;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class ItemInfoCache
{
	private static final ItemInfoCache _instance = new ItemInfoCache();
	private final Cache cache;
	
	private ItemInfoCache()
	{
		cache = CacheManager.getInstance().getCache(getClass().getName());
	}
	
	public static final ItemInfoCache getInstance()
	{
		return _instance;
	}
	
	public void put(ItemInstance item)
	{
		cache.put(new Element(item.getObjectId(), new ItemInfo(item)));
	}
	
	public ItemInfo get(int objectId)
	{
		Element element = cache.get(Integer.valueOf(objectId));
		ItemInfo info = null;
		if(element != null)
		{
			info = (ItemInfo) element.getObjectValue();
		}
		if(info != null)
		{
			Player player = World.getPlayer(info.getOwnerId());
			ItemInstance item = null;
			if(player != null)
			{
				item = player.getInventory().getItemByObjectId(objectId);
			}
			if(item != null && item.getItemId() == info.getItemId())
			{
				info = new ItemInfo(item);
				cache.put(new Element(item.getObjectId(), info));
			}
		}
		return info;
	}
}