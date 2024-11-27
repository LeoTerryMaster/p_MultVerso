package l2.gameserver.data.xml.holder;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import l2.commons.data.xml.AbstractHolder;
import l2.commons.lang.ArrayUtils;
import l2.gameserver.templates.npc.NpcTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NpcHolder extends AbstractHolder
{
	private static final NpcHolder _instance = new NpcHolder();
	private final TIntObjectHashMap<NpcTemplate> _npcs = new TIntObjectHashMap(20000);
	private TIntObjectHashMap<List<NpcTemplate>> _npcsByLevel;
	private NpcTemplate[] _allTemplates;
	private Map<String, NpcTemplate> _npcsNames;
	
	NpcHolder()
	{
	}
	
	public static NpcHolder getInstance()
	{
		return _instance;
	}
	
	public void addTemplate(NpcTemplate template)
	{
		_npcs.put(template.npcId, template);
	}
	
	public NpcTemplate getTemplate(int id)
	{
		NpcTemplate npc = ArrayUtils.valid(_allTemplates, id);
		if(npc == null)
		{
			warn("Not defined npc id : " + id + ", or out of range!", new Exception());
			return null;
		}
		return _allTemplates[id];
	}
	
	public NpcTemplate getTemplateByName(String name)
	{
		return _npcsNames.get(name.toLowerCase());
	}
	
	public List<NpcTemplate> getAllOfLevel(int lvl)
	{
		return _npcsByLevel.get(lvl);
	}
	
	public NpcTemplate[] getAll()
	{
		return (NpcTemplate[]) _npcs.getValues((Object[]) new NpcTemplate[_npcs.size()]);
	}
	
	private void buildFastLookupTable()
	{
		_npcsByLevel = new TIntObjectHashMap();
		_npcsNames = new HashMap<>();
		int highestId = 0;
		for(int id : _npcs.keys())
		{
			if(id <= highestId)
				continue;
			highestId = id;
		}
		_allTemplates = new NpcTemplate[highestId + 1];
		TIntObjectIterator iterator = _npcs.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			int npcId = iterator.key();
			NpcTemplate npc;
			_allTemplates[npcId] = npc = (NpcTemplate) iterator.value();
			ArrayList<NpcTemplate> byLevel = (ArrayList<NpcTemplate>) _npcsByLevel.get(npc.level);
			if(byLevel == null)
			{
				byLevel = new ArrayList<>();
				_npcsByLevel.put(npcId, byLevel);
			}
			byLevel.add(npc);
			_npcsNames.put(npc.name.toLowerCase(), npc);
		}
	}
	
	@Override
	protected void process()
	{
		buildFastLookupTable();
	}
	
	@Override
	public int size()
	{
		return _npcs.size();
	}
	
	@Override
	public void clear()
	{
		_npcsNames.clear();
		_npcs.clear();
	}
}