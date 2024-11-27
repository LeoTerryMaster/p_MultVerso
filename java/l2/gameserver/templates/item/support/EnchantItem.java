package l2.gameserver.templates.item.support;

import l2.gameserver.templates.item.ItemTemplate;

public abstract class EnchantItem
{
	private final int _itemId;
	private final double _chanceMod;
	private final ItemTemplate.Grade _grade;
	private final int _minLvl;
	private final int _maxLvl;
	private final EnchantTargetType _targetType;
	
	public EnchantItem(int itemId, double chanceMod, ItemTemplate.Grade grade, int minLvl, int maxLvl, EnchantTargetType ett)
	{
		_itemId = itemId;
		_chanceMod = chanceMod;
		_grade = grade;
		_minLvl = minLvl;
		_maxLvl = maxLvl;
		_targetType = ett;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public double getChanceMod()
	{
		return _chanceMod;
	}
	
	public ItemTemplate.Grade getGrade()
	{
		return _grade;
	}
	
	public int getMinLvl()
	{
		return _minLvl;
	}
	
	public int getMaxLvl()
	{
		return _maxLvl;
	}
	
	public EnchantTargetType getTargetType()
	{
		return _targetType;
	}
}