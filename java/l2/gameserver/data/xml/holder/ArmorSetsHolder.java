package l2.gameserver.data.xml.holder;

import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.model.ArmorSet;

import java.util.ArrayList;
import java.util.List;

public final class ArmorSetsHolder extends AbstractHolder
{
	private static final ArmorSetsHolder _instance = new ArmorSetsHolder();
	private final List<ArmorSet> _armorSets = new ArrayList<>();
	
	public static ArmorSetsHolder getInstance()
	{
		return _instance;
	}
	
	public void addArmorSet(ArmorSet armorset)
	{
		_armorSets.add(armorset);
	}
	
	public ArmorSet getArmorSet(int chestItemId)
	{
		for(ArmorSet as : _armorSets)
		{
			if(!as.getChestItemIds().contains(chestItemId))
				continue;
			return as;
		}
		return null;
	}
	
	@Override
	public int size()
	{
		return _armorSets.size();
	}
	
	@Override
	public void clear()
	{
		_armorSets.clear();
	}
}