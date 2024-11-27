package l2.gameserver.model.entity.oly;

import l2.gameserver.data.xml.holder.InstantZoneHolder;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.Zone;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.instances.DoorInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.InstantZone;
import l2.gameserver.utils.Location;

import java.util.ArrayList;

public class Stadium extends Reflection
{
	public static final int OLYMPIAD_HOST = 36402;
	private static final int[] EMPTY_VISITORS = new int[0];
	private final Location _observe_loc;
	private final int _stadium_id;
	private boolean _isFree = true;
	
	public Stadium(int id, int ozid, Location observe_loc)
	{
		InstantZone instantZone = InstantZoneHolder.getInstance().getInstantZone(ozid);
		init(instantZone);
		setName("OlyStadium-" + id);
		_stadium_id = id;
		_observe_loc = observe_loc;
		setCollapseIfEmptyTime(0);
		_isFree = true;
	}
	
	public boolean isFree()
	{
		return _isFree;
	}
	
	public void setFree(boolean val)
	{
		_isFree = val;
	}
	
	public void clear()
	{
		ArrayList<Player> teleport_list = new ArrayList<>();
		ArrayList<GameObject> delete_list = new ArrayList<>();
		lock.lock();
		try
		{
			for(GameObject o : _objects)
			{
				if(o == null || o instanceof DoorInstance || o.isNpc() && ((NpcInstance) o).getNpcId() == 36402)
					continue;
				if(o.isPlayer() && !o.getPlayer().isOlyObserver())
				{
					teleport_list.add((Player) o);
					continue;
				}
				if(o.isPlayer())
					continue;
				delete_list.add(o);
			}
		}
		finally
		{
			lock.unlock();
		}
		for(Player player : teleport_list)
		{
			if(player.getParty() != null && equals(player.getParty().getReflection()))
			{
				player.getParty().setReflection(null);
			}
			if(!equals(player.getReflection()))
				continue;
			if(getReturnLoc() != null)
			{
				player.teleToLocation(getReturnLoc(), 0);
				continue;
			}
			player.teleToClosestTown();
		}
		for(GameObject o : delete_list)
		{
			o.deleteMe();
		}
	}
	
	public final int getStadiumId()
	{
		return _stadium_id;
	}
	
	public Location getLocForParticipant(Participant part)
	{
		return Location.findPointToStay(getInstancedZone().getTeleportCoords().get(part.getSide() - 1), 50, 50, getGeoIndex());
	}
	
	public Location getObservingLoc()
	{
		return _observe_loc;
	}
	
	public int getObserverCount()
	{
		int cnt = 0;
		for(Player player : getPlayers())
		{
			if(!player.isOlyObserver())
				continue;
			++cnt;
		}
		return cnt;
	}
	
	public void setZonesActive(boolean active)
	{
		for(Zone zone : getZones())
		{
			zone.setActive(active);
		}
	}
	
	@Override
	public boolean isStatic()
	{
		return true;
	}
	
	@Override
	public int[] getVisitors()
	{
		return EMPTY_VISITORS;
	}
}