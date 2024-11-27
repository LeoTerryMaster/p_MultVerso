package l2.gameserver.templates.item;

public enum ItemFlags
{
	DESTROYABLE(true),
	DROPABLE(true),
	FREIGHTABLE(false),
	AUGMENTABLE(true),
	ENCHANTABLE(true),
	ATTRIBUTABLE(true),
	SELLABLE(true),
	TRADEABLE(true),
	STOREABLE(true);
	
	public static final ItemFlags[] VALUES;
	
	static
	{
		VALUES = ItemFlags.values();
	}
	
	private final int _mask;
	private final boolean _defaultValue;
	
	ItemFlags(boolean defaultValue)
	{
		_defaultValue = defaultValue;
		_mask = 1 << ordinal();
	}
	
	public int mask()
	{
		return _mask;
	}
	
	public boolean getDefaultValue()
	{
		return _defaultValue;
	}
}