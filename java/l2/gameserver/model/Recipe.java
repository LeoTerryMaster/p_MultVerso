package l2.gameserver.model;

import l2.gameserver.model.base.Race;
import l2.gameserver.templates.item.ItemTemplate;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class Recipe
{
	private final int _id;
	private final ItemTemplate _item;
	private final ERecipeType _type;
	private final int _requiredSkillLvl;
	private final int _mpConsume;
	private final int _successRate;
	private final List<Pair<ItemTemplate, Long>> _materials;
	private final List<Pair<ItemTemplate, Long>> _products;
	private final List<Pair<ItemTemplate, Long>> _npcFees;
	
	public Recipe(int id, ItemTemplate item, ERecipeType type, int requiredSkillLvl, int mpConsume, int successRate, List<Pair<ItemTemplate, Long>> materials, List<Pair<ItemTemplate, Long>> products, List<Pair<ItemTemplate, Long>> npcFees)
	{
		_id = id;
		_item = item;
		_type = type;
		_requiredSkillLvl = requiredSkillLvl;
		_mpConsume = mpConsume;
		_successRate = successRate;
		_materials = materials;
		_products = products;
		_npcFees = npcFees;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public ItemTemplate getItem()
	{
		return _item;
	}
	
	public ERecipeType getType()
	{
		return _type;
	}
	
	public int getRequiredSkillLvl()
	{
		return _requiredSkillLvl;
	}
	
	public int getMpConsume()
	{
		return _mpConsume;
	}
	
	public int getSuccessRate()
	{
		return _successRate;
	}
	
	public List<Pair<ItemTemplate, Long>> getMaterials()
	{
		return _materials;
	}
	
	public List<Pair<ItemTemplate, Long>> getProducts()
	{
		return _products;
	}
	
	public List<Pair<ItemTemplate, Long>> getNpcFees()
	{
		return _npcFees;
	}
	
	@Override
	public int hashCode()
	{
		return _id;
	}
	
	@Override
	public String toString()
	{
		return "Recipe{_id=" + _id + ", _item=" + _item + ", _type=" + _type + ", _requiredSkillLvl=" + _requiredSkillLvl + ", _mpConsume=" + _mpConsume + ", _successRate=" + _successRate + ", _materials=" + _materials + ", _products=" + _products + ", _npcFees=" + _npcFees + '}';
	}
	
	public enum ERecipeType
	{
		ERT_DWARF,
		ERT_COMMON;
		
		
		public boolean isApplicableBy(Player player)
		{
			return this != ERT_DWARF || player.getRace() == Race.dwarf;
		}
	}
}