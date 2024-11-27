package l2.gameserver.data;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.model.entity.boat.Boat;
import l2.gameserver.templates.CharTemplate;

import java.lang.reflect.Constructor;

public final class BoatHolder extends AbstractHolder
{
	public static final CharTemplate TEMPLATE = new CharTemplate(CharTemplate.getEmptyStatsSet());
	private static final BoatHolder _instance = new BoatHolder();
	private final TIntObjectHashMap<Boat> _boats = new TIntObjectHashMap();
	
	public static BoatHolder getInstance()
	{
		return _instance;
	}
	
	public void spawnAll()
	{
		log();
		TIntObjectIterator iterator = _boats.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			((Boat) iterator.value()).spawnMe();
			info("Spawning: " + ((Boat) iterator.value()).getName());
		}
	}
	
	public Boat initBoat(String name, String clazz)
	{
		try
		{
			Class cl = Class.forName("l2.gameserver.model.entity.boat." + clazz);
			Constructor constructor = cl.getConstructor(Integer.TYPE, CharTemplate.class);
			Boat boat = (Boat) constructor.newInstance(IdFactory.getInstance().getNextId(), TEMPLATE);
			boat.setName(name);
			addBoat(boat);
			return boat;
		}
		catch(Exception e)
		{
			error("Fail to init boat: " + clazz, e);
			return null;
		}
	}
	
	public Boat getBoat(String name)
	{
		TIntObjectIterator iterator = _boats.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			if(!((Boat) iterator.value()).getName().equals(name))
				continue;
			return (Boat) iterator.value();
		}
		return null;
	}
	
	public Boat getBoat(int objectId)
	{
		return _boats.get(objectId);
	}
	
	public void addBoat(Boat boat)
	{
		_boats.put(boat.getObjectId(), boat);
	}
	
	public void removeBoat(Boat boat)
	{
		_boats.remove(boat.getObjectId());
	}
	
	@Override
	public int size()
	{
		return _boats.size();
	}
	
	@Override
	public void clear()
	{
		_boats.clear();
	}
}