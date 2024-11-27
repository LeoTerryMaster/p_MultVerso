package l2.gameserver.model.base;

import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.items.ItemAttributes;

public class MultiSellIngredient implements Cloneable
{
	private int _itemId;
	private long _itemCount;
	private int _itemEnchant;
	private ItemAttributes _itemAttributes;
	private boolean _mantainIngredient;
	
	public MultiSellIngredient(int itemId, long itemCount)
	{
		this(itemId, itemCount, 0);
	}
	
	public MultiSellIngredient(int itemId, long itemCount, int enchant)
	{
		_itemId = itemId;
		_itemCount = itemCount;
		_itemEnchant = enchant;
		_mantainIngredient = false;
		_itemAttributes = new ItemAttributes();
	}
	
	@Override
	public MultiSellIngredient clone()
	{
		MultiSellIngredient mi = new MultiSellIngredient(_itemId, _itemCount, _itemEnchant);
		mi.setMantainIngredient(_mantainIngredient);
		mi.setItemAttributes(_itemAttributes.clone());
		return mi;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}
	
	public long getItemCount()
	{
		return _itemCount;
	}
	
	public void setItemCount(long itemCount)
	{
		_itemCount = itemCount;
	}
	
	public boolean isStackable()
	{
		return _itemId <= 0 || ItemHolder.getInstance().getTemplate(_itemId).isStackable();
	}
	
	public int getItemEnchant()
	{
		return _itemEnchant;
	}
	
	public void setItemEnchant(int itemEnchant)
	{
		_itemEnchant = itemEnchant;
	}
	
	public ItemAttributes getItemAttributes()
	{
		return _itemAttributes;
	}
	
	public void setItemAttributes(ItemAttributes attr)
	{
		_itemAttributes = attr;
	}
	
	@Override
	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = 31 * result + (int) (_itemCount ^ _itemCount >>> 32);
		for(Element e : Element.VALUES)
		{
			result = 31 * result + _itemAttributes.getValue(e);
		}
		result = 31 * result + _itemEnchant;
		result = 31 * result + _itemId;
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(obj == null)
		{
			return false;
		}
		if(getClass() != obj.getClass())
		{
			return false;
		}
		MultiSellIngredient other = (MultiSellIngredient) obj;
		if(_itemId != other._itemId)
		{
			return false;
		}
		if(_itemCount != other._itemCount)
		{
			return false;
		}
		if(_itemEnchant != other._itemEnchant)
		{
			return false;
		}
		for(Element e : Element.VALUES)
		{
			if(_itemAttributes.getValue(e) == other._itemAttributes.getValue(e))
				continue;
			return false;
		}
		return true;
	}
	
	public boolean getMantainIngredient()
	{
		return _mantainIngredient;
	}
	
	public void setMantainIngredient(boolean mantainIngredient)
	{
		_mantainIngredient = mantainIngredient;
	}
}