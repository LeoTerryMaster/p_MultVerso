package l2.gameserver.data.xml.holder;

import l2.commons.data.xml.AbstractHolder;
import l2.commons.time.cron.SchedulingPattern;
import l2.gameserver.model.Player;
import l2.gameserver.templates.InstantZone;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InstantZoneHolder extends AbstractHolder
{
	private static final InstantZoneHolder _instance = new InstantZoneHolder();
	private final IntObjectMap<InstantZone> _zones = new HashIntObjectMap();
	
	public static InstantZoneHolder getInstance()
	{
		return _instance;
	}
	
	public void addInstantZone(InstantZone zone)
	{
		_zones.put(zone.getId(), zone);
	}
	
	public InstantZone getInstantZone(int id)
	{
		return _zones.get(id);
	}
	
	private SchedulingPattern getResetReuseById(int id)
	{
		InstantZone zone = getInstantZone(id);
		return zone == null ? null : zone.getResetReuse();
	}
	
	public int getMinutesToNextEntrance(int id, Player player)
	{
		SchedulingPattern resetReuse = getResetReuseById(id);
		if(resetReuse == null)
		{
			return 0;
		}
		Long time = null;
		if(getSharedReuseInstanceIds(id) != null && !getSharedReuseInstanceIds(id).isEmpty())
		{
			ArrayList<Long> reuses = new ArrayList<>();
			for(int i : getSharedReuseInstanceIds(id))
			{
				if(player.getInstanceReuse(i) == null)
					continue;
				reuses.add(player.getInstanceReuse(i));
			}
			if(!reuses.isEmpty())
			{
				Collections.sort(reuses);
				time = reuses.get(reuses.size() - 1);
			}
		}
		else
		{
			time = player.getInstanceReuse(id);
		}
		if(time == null)
		{
			return 0;
		}
		return (int) Math.max((resetReuse.next(time) - System.currentTimeMillis()) / 60000, 0);
	}
	
	public List<Integer> getSharedReuseInstanceIds(int id)
	{
		if(getInstantZone(id).getSharedReuseGroup() < 1)
		{
			return null;
		}
		ArrayList<Integer> sharedInstanceIds = new ArrayList<>();
		for(InstantZone iz : _zones.values())
		{
			if(iz.getSharedReuseGroup() <= 0 || getInstantZone(id).getSharedReuseGroup() <= 0 || iz.getSharedReuseGroup() != getInstantZone(id).getSharedReuseGroup())
				continue;
			sharedInstanceIds.add(iz.getId());
		}
		return sharedInstanceIds;
	}
	
	public List<Integer> getSharedReuseInstanceIdsByGroup(int groupId)
	{
		if(groupId < 1)
		{
			return null;
		}
		ArrayList<Integer> sharedInstanceIds = new ArrayList<>();
		for(InstantZone iz : _zones.values())
		{
			if(iz.getSharedReuseGroup() <= 0 || iz.getSharedReuseGroup() != groupId)
				continue;
			sharedInstanceIds.add(iz.getId());
		}
		return sharedInstanceIds;
	}
	
	@Override
	public int size()
	{
		return _zones.size();
	}
	
	@Override
	public void clear()
	{
		_zones.clear();
	}
}