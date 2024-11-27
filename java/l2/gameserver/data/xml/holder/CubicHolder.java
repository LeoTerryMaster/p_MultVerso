package l2.gameserver.data.xml.holder;

import gnu.trove.TIntObjectHashMap;
import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.templates.CubicTemplate;

public final class CubicHolder extends AbstractHolder
{
	private static final CubicHolder _instance = new CubicHolder();
	private final TIntObjectHashMap<CubicTemplate> _cubics = new TIntObjectHashMap(10);
	
	private CubicHolder()
	{
	}
	
	public static CubicHolder getInstance()
	{
		return _instance;
	}
	
	public void addCubicTemplate(CubicTemplate template)
	{
		_cubics.put(hash(template.getId(), template.getLevel()), template);
	}
	
	public CubicTemplate getTemplate(int id, int level)
	{
		return _cubics.get(hash(id, level));
	}
	
	public int hash(int id, int level)
	{
		return id * 10000 + level;
	}
	
	@Override
	public int size()
	{
		return _cubics.size();
	}
	
	@Override
	public void clear()
	{
		_cubics.clear();
	}
}