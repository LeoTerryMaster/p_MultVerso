package l2.gameserver.data.xml.holder;

import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.templates.DoorTemplate;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

public final class DoorHolder extends AbstractHolder
{
	private static final DoorHolder _instance = new DoorHolder();
	private final IntObjectMap<DoorTemplate> _doors = new HashIntObjectMap();
	
	public static DoorHolder getInstance()
	{
		return _instance;
	}
	
	public void addTemplate(DoorTemplate door)
	{
		_doors.put(door.getNpcId(), door);
	}
	
	public DoorTemplate getTemplate(int doorId)
	{
		return _doors.get(doorId);
	}
	
	public IntObjectMap<DoorTemplate> getDoors()
	{
		return _doors;
	}
	
	@Override
	public int size()
	{
		return _doors.size();
	}
	
	@Override
	public void clear()
	{
		_doors.clear();
	}
}