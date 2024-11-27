package l2.gameserver.data.xml.holder;

import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.templates.item.support.EnchantItem;
import l2.gameserver.templates.item.support.EnchantScroll;
import org.napile.primitive.sets.impl.HashIntSet;

import java.util.HashMap;
import java.util.Map;

public class EnchantItemHolder extends AbstractHolder
{
	private static final EnchantItemHolder _instance = new EnchantItemHolder();
	private final Map<Integer, EnchantItem> _items = new HashMap<>();
	
	private EnchantItemHolder()
	{
	}
	
	public static EnchantItemHolder getInstance()
	{
		return _instance;
	}
	
	@Override
	public void log()
	{
		info("load " + _items.size() + " enchant item(s).");
	}
	
	public EnchantItem getEnchantItem(int item_id)
	{
		return _items.get(item_id);
	}
	
	public EnchantScroll getEnchantScroll(int item_id)
	{
		EnchantItem ei = getEnchantItem(item_id);
		if(ei != null && ei instanceof EnchantScroll)
		{
			return (EnchantScroll) ei;
		}
		return null;
	}
	
	public void addEnchantItem(EnchantItem ei)
	{
		_items.put(ei.getItemId(), ei);
	}
	
	public int[] getScrollIds()
	{
		HashIntSet is = new HashIntSet();
		for(EnchantItem ei : _items.values())
		{
			if(!(ei instanceof EnchantScroll))
				continue;
			is.add(ei.getItemId());
		}
		return is.toArray(new int[is.size()]);
	}
	
	@Override
	public int size()
	{
		return _items.size();
	}
	
	@Override
	public void clear()
	{
		_items.clear();
	}
}