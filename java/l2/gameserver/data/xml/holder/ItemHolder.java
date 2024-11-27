package l2.gameserver.data.xml.holder;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import l2.commons.data.xml.AbstractHolder;
import l2.commons.lang.ArrayUtils;
import l2.gameserver.templates.item.ItemTemplate;

import java.util.HashMap;

public final class ItemHolder extends AbstractHolder
{
	private static final ItemHolder _instance = new ItemHolder();
	private final TIntObjectHashMap<ItemTemplate> _items = new TIntObjectHashMap();
	private ItemTemplate[] _allTemplates;
	
	private ItemHolder()
	{
	}
	
	public static ItemHolder getInstance()
	{
		return _instance;
	}
	
	public void addItem(ItemTemplate template)
	{
		_items.put(template.getItemId(), template);
	}
	
	private void buildFastLookupTable()
	{
		int highestId = 0;
		for(int id : _items.keys())
		{
			if(id <= highestId)
				continue;
			highestId = id;
		}
		_allTemplates = new ItemTemplate[highestId + 1];
		TIntObjectIterator iterator = _items.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			_allTemplates[iterator.key()] = (ItemTemplate) iterator.value();
		}
	}
	
	public ItemTemplate getTemplate(int id)
	{
		ItemTemplate item = ArrayUtils.valid(_allTemplates, id);
		if(item == null)
		{
			warn("Not defined item id : " + id + ", or out of range!", new Exception());
			return null;
		}
		return _allTemplates[id];
	}
	
	public ItemTemplate[] getAllTemplates()
	{
		return _allTemplates;
	}
	
	private void itemBreakCrystalPrice()
	{
		HashMap<ItemTemplate.Grade, Long> refGradeCrystalPrices = new HashMap<>();
		for(ItemTemplate.Grade grade : ItemTemplate.Grade.values())
		{
			if(grade.cry <= 0)
				continue;
			ItemTemplate crystalItem = getTemplate(grade.cry);
			refGradeCrystalPrices.put(grade, Long.valueOf(crystalItem.getReferencePrice()));
		}
		TIntObjectIterator iterator = _items.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			ItemTemplate itemTemplate = (ItemTemplate) iterator.value();
			if(itemTemplate == null)
				continue;
			int crystalCount = itemTemplate.getCrystalCount();
			long refPrice = itemTemplate.getReferencePrice();
			ItemTemplate.Grade grade = itemTemplate.getCrystalType();
			Long crystalPrice = refGradeCrystalPrices.get(grade);
			if(crystalPrice == null || grade.cry == itemTemplate.getItemId() || crystalCount == 0 || refPrice == 0)
				continue;
			long crystalizedPrice = (long) crystalCount * crystalPrice;
			if(crystalPrice <= refPrice)
				continue;
			warn("Reference price (" + refPrice + ") of item \"" + itemTemplate.getItemId() + "\" lower than crystal price (" + crystalizedPrice + ")");
		}
	}
	
	private void processAdditionalChecks()
	{
		itemBreakCrystalPrice();
	}
	
	@Override
	protected void process()
	{
		buildFastLookupTable();
		processAdditionalChecks();
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