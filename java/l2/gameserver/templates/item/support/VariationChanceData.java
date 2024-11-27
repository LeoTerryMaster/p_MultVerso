package l2.gameserver.templates.item.support;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class VariationChanceData
{
	private final int _mineralItemId;
	private final List<Pair<List<Pair<Integer, Double>>, Double>> _variation1;
	private final List<Pair<List<Pair<Integer, Double>>, Double>> _variation2;
	
	public VariationChanceData(int mineralItemId, List<Pair<List<Pair<Integer, Double>>, Double>> variation1, List<Pair<List<Pair<Integer, Double>>, Double>> variation2)
	{
		_mineralItemId = mineralItemId;
		_variation1 = variation1;
		_variation2 = variation2;
	}
	
	public int getMineralItemId()
	{
		return _mineralItemId;
	}
	
	public List<Pair<List<Pair<Integer, Double>>, Double>> getVariation1()
	{
		return _variation1;
	}
	
	public List<Pair<List<Pair<Integer, Double>>, Double>> getVariation2()
	{
		return _variation2;
	}
}