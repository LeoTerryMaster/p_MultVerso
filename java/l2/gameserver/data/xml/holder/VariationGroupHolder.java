package l2.gameserver.data.xml.holder;

import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.templates.item.support.VariationGroupData;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VariationGroupHolder extends AbstractHolder
{
	private static final VariationGroupHolder _instance = new VariationGroupHolder();
	private final List<Pair<int[], VariationGroupData>> _variationGroupData = new ArrayList<>();
	
	private VariationGroupHolder()
	{
	}
	
	public static VariationGroupHolder getInstance()
	{
		return _instance;
	}
	
	@Override
	public int size()
	{
		return _variationGroupData.size();
	}
	
	@Override
	public void clear()
	{
		_variationGroupData.clear();
	}
	
	public void add(int[] itemIds, VariationGroupData vgd)
	{
		int[] sortedIds = itemIds.clone();
		Arrays.sort(sortedIds);
		_variationGroupData.add((Pair<int[], VariationGroupData>) new ImmutablePair(sortedIds, vgd));
	}
	
	public void addSorted(int[] sortedIds, VariationGroupData vgd)
	{
		_variationGroupData.add((Pair<int[], VariationGroupData>) new ImmutablePair(sortedIds, vgd));
	}
	
	public List<VariationGroupData> getDataForItemId(int itemId)
	{
		ArrayList<VariationGroupData> resultList = new ArrayList<>();
		for(Pair<int[], VariationGroupData> e : _variationGroupData)
		{
			int[] ids = e.getLeft();
			if(Arrays.binarySearch(ids, itemId) < 0)
				continue;
			resultList.add(e.getRight());
		}
		return resultList;
	}
}