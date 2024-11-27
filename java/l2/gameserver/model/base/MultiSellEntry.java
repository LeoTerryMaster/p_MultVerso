package l2.gameserver.model.base;

import java.util.ArrayList;
import java.util.List;

public class MultiSellEntry
{
	private final List<MultiSellIngredient> _ingredients = new ArrayList<>();
	private final List<MultiSellIngredient> _production = new ArrayList<>();
	private int _entryId;
	private long _tax;
	
	public MultiSellEntry()
	{
	}
	
	public MultiSellEntry(int id)
	{
		_entryId = id;
	}
	
	public MultiSellEntry(int id, int product, int prod_count, int enchant)
	{
		_entryId = id;
		addProduct(new MultiSellIngredient(product, prod_count, enchant));
	}
	
	public int getEntryId()
	{
		return _entryId;
	}
	
	public void setEntryId(int entryId)
	{
		_entryId = entryId;
	}
	
	public void addIngredient(MultiSellIngredient ingredient)
	{
		if(ingredient.getItemCount() > 0)
		{
			_ingredients.add(ingredient);
		}
	}
	
	public List<MultiSellIngredient> getIngredients()
	{
		return _ingredients;
	}
	
	public void addProduct(MultiSellIngredient ingredient)
	{
		_production.add(ingredient);
	}
	
	public List<MultiSellIngredient> getProduction()
	{
		return _production;
	}
	
	public long getTax()
	{
		return _tax;
	}
	
	public void setTax(long tax)
	{
		_tax = tax;
	}
	
	@Override
	public int hashCode()
	{
		return _entryId;
	}
	
	@Override
	public MultiSellEntry clone()
	{
		MultiSellEntry ret = new MultiSellEntry(_entryId);
		for(MultiSellIngredient i : _ingredients)
		{
			ret.addIngredient(i.clone());
		}
		for(MultiSellIngredient i : _production)
		{
			ret.addProduct(i.clone());
		}
		return ret;
	}
}