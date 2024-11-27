package l2.gameserver.data.xml.holder;

import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.templates.spawn.SpawnTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SpawnHolder extends AbstractHolder
{
	private static final SpawnHolder _instance = new SpawnHolder();
	private final Map<String, List<SpawnTemplate>> _spawns = new HashMap<>();
	
	public static SpawnHolder getInstance()
	{
		return _instance;
	}
	
	public void addSpawn(String group, SpawnTemplate spawn)
	{
		List<SpawnTemplate> spawns = _spawns.get(group);
		if(spawns == null)
		{
			spawns = new ArrayList<>();
			_spawns.put(group, spawns);
		}
		spawns.add(spawn);
	}
	
	public List<SpawnTemplate> getSpawn(String name)
	{
		List template = _spawns.get(name);
		return template == null ? Collections.emptyList() : template;
	}
	
	@Override
	public int size()
	{
		int i = 0;
		for(List<SpawnTemplate> l : _spawns.values())
		{
			i += l.size();
		}
		return i;
	}
	
	@Override
	public void clear()
	{
		_spawns.clear();
	}
	
	public Map<String, List<SpawnTemplate>> getSpawns()
	{
		return _spawns;
	}
}