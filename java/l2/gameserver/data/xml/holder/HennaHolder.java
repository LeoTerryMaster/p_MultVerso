package l2.gameserver.data.xml.holder;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.model.Player;
import l2.gameserver.templates.Henna;

import java.util.ArrayList;
import java.util.List;

public final class HennaHolder extends AbstractHolder
{
	private static final HennaHolder _instance = new HennaHolder();
	private final TIntObjectHashMap<Henna> _hennas = new TIntObjectHashMap();
	
	public static HennaHolder getInstance()
	{
		return _instance;
	}
	
	public void addHenna(Henna h)
	{
		_hennas.put(h.getSymbolId(), h);
	}
	
	public Henna getHenna(int symbolId)
	{
		return _hennas.get(symbolId);
	}
	
	public List<Henna> generateList(Player player)
	{
		ArrayList<Henna> list = new ArrayList<>();
		TIntObjectIterator iterator = _hennas.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			Henna h = (Henna) iterator.value();
			if(!h.isForThisClass(player))
				continue;
			list.add(h);
		}
		return list;
	}
	
	@Override
	public int size()
	{
		return _hennas.size();
	}
	
	@Override
	public void clear()
	{
		_hennas.clear();
	}
}