package l2.gameserver.data.xml.holder;

import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.templates.item.support.FishGroup;
import l2.gameserver.templates.item.support.FishTemplate;
import l2.gameserver.templates.item.support.LureTemplate;
import l2.gameserver.templates.item.support.LureType;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FishDataHolder extends AbstractHolder
{
	private static final FishDataHolder _instance = new FishDataHolder();
	private final List<FishTemplate> _fishes = new ArrayList<>();
	private final IntObjectMap<LureTemplate> _lures = new HashIntObjectMap();
	private final IntObjectMap<Map<LureType, Map<FishGroup, Integer>>> _distributionsForZones = new HashIntObjectMap();
	
	public static FishDataHolder getInstance()
	{
		return _instance;
	}
	
	public void addFish(FishTemplate fishTemplate)
	{
		_fishes.add(fishTemplate);
	}
	
	public void addLure(LureTemplate template)
	{
		_lures.put(template.getItemId(), template);
	}
	
	public void addDistribution(int id, LureType lureType, Map<FishGroup, Integer> map)
	{
		HashMap<LureType, Map<FishGroup, Integer>> byLureType = (HashMap<LureType, Map<FishGroup, Integer>>) _distributionsForZones.get(id);
		if(byLureType == null)
		{
			byLureType = new HashMap<>();
			_distributionsForZones.put(id, byLureType);
		}
		byLureType.put(lureType, map);
	}
	
	@Override
	public void log()
	{
		info("load " + _fishes.size() + " fish(es).");
		info("load " + _lures.size() + " lure(s).");
		info("load " + _distributionsForZones.size() + " distribution(s).");
	}
	
	@Deprecated
	@Override
	public int size()
	{
		return 0;
	}
	
	@Override
	public void clear()
	{
		_fishes.clear();
		_lures.clear();
		_distributionsForZones.clear();
	}
}