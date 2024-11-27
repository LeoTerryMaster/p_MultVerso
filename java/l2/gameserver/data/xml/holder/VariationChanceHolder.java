package l2.gameserver.data.xml.holder;

import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.templates.item.support.VariationChanceData;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;

public class VariationChanceHolder extends AbstractHolder
{
	private static final VariationChanceHolder _instance = new VariationChanceHolder();
	private final HashMap<Integer, Pair<VariationChanceData, VariationChanceData>> _minerallChances = new HashMap();
	
	private VariationChanceHolder()
	{
	}
	
	public static VariationChanceHolder getInstance()
	{
		return _instance;
	}
	
	@Override
	public int size()
	{
		return _minerallChances.size();
	}
	
	@Override
	public void clear()
	{
		_minerallChances.clear();
	}
	
	public void add(Pair<VariationChanceData, VariationChanceData> vcdp)
	{
		if(vcdp.getLeft() != null && vcdp.getRight() != null && vcdp.getLeft().getMineralItemId() == vcdp.getRight().getMineralItemId())
		{
			_minerallChances.put(vcdp.getLeft().getMineralItemId(), vcdp);
		}
		else if(vcdp.getLeft() != null)
		{
			_minerallChances.put(vcdp.getLeft().getMineralItemId(), vcdp);
		}
		else if(vcdp.getRight() != null)
		{
			_minerallChances.put(vcdp.getRight().getMineralItemId(), vcdp);
		}
		else
		{
			throw new RuntimeException("Empty mineral");
		}
	}
	
	public Pair<VariationChanceData, VariationChanceData> getVariationChanceDataForMineral(int mineralItemId)
	{
		return _minerallChances.get(mineralItemId);
	}
}