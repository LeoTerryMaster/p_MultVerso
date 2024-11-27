package l2.gameserver.templates.item.support;

import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.templates.item.ItemTemplate;
import org.napile.primitive.Containers;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

public class EnchantScroll extends EnchantItem
{
	private final EnchantScrollOnFailAction _failResultType;
	private final boolean _isInfallible;
	private final boolean _hasAVE;
	private final int _failResultLevel;
	private final int _increment;
	private IntSet _items = Containers.EMPTY_INT_SET;
	
	public EnchantScroll(int itemId, int increment, double chanceMod, ItemTemplate.Grade scrollGrade, int minLvl, int maxLvl, EnchantTargetType ett, EnchantScrollOnFailAction frt, int lrl, boolean isInfallible, boolean hasAVE)
	{
		super(itemId, chanceMod, scrollGrade, minLvl, maxLvl, ett);
		_increment = increment;
		_failResultLevel = lrl;
		_failResultType = frt;
		_isInfallible = isInfallible;
		_hasAVE = hasAVE;
	}
	
	public int getIncrement()
	{
		return _increment;
	}
	
	public int getFailResultLevel()
	{
		return _failResultLevel;
	}
	
	public EnchantScrollOnFailAction getOnFailAction()
	{
		return _failResultType;
	}
	
	public void addItemRestrict(int item_type)
	{
		if(_items.isEmpty())
		{
			_items = new HashIntSet();
		}
		_items.add(item_type);
	}
	
	public boolean isHasAbnormalVisualEffect()
	{
		return _hasAVE;
	}
	
	public boolean isInfallible()
	{
		return _isInfallible;
	}
	
	public boolean isUsableWith(ItemInstance target)
	{
		if(!_items.isEmpty() && !_items.contains(target.getItemId()))
		{
			return false;
		}
		int toLvl = target.getEnchantLevel() + getIncrement();
		ItemTemplate.Grade itemGrade = target.getCrystalType();
		if(itemGrade.gradeOrd() != getGrade().gradeOrd())
		{
			return false;
		}
		if(toLvl < getMinLvl() || toLvl > getMaxLvl())
		{
			return false;
		}
		return getTargetType().isUsableOn(target);
	}
}