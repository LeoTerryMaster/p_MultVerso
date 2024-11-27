package l2.gameserver.model;

import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.Location;

public class TeleportLocation extends Location
{
	private final long _price;
	private final int _minLevel;
	private final int _maxLevel;
	private final ItemTemplate _item;
	private final String _name;
	private final int _castleId;
	
	public TeleportLocation(int item, long price, int minLevel, int maxLevel, String name, int castleId)
	{
		_price = price;
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_name = name;
		_item = ItemHolder.getInstance().getTemplate(item);
		_castleId = castleId;
	}
	
	public TeleportLocation(int item, long price, String name, int castleId)
	{
		_price = price;
		_minLevel = 0;
		_maxLevel = 0;
		_name = name;
		_item = ItemHolder.getInstance().getTemplate(item);
		_castleId = castleId;
	}
	
	public int getMinLevel()
	{
		return _minLevel;
	}
	
	public int getMaxLevel()
	{
		return _maxLevel;
	}
	
	public long getPrice()
	{
		return _price;
	}
	
	public ItemTemplate getItem()
	{
		return _item;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getCastleId()
	{
		return _castleId;
	}
}