package l2.gameserver.instancemanager;

import l2.gameserver.model.Player;
import l2.gameserver.model.matching.MatchingRoom;
import l2.gameserver.templates.mapregion.RestartArea;
import l2.gameserver.templates.mapregion.RestartPoint;
import org.apache.commons.lang3.ArrayUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class MatchingRoomManager
{
	private static final MatchingRoomManager _instance = new MatchingRoomManager();
	private final RoomsHolder[] _holder = new RoomsHolder[2];
	private final Set<Player> _players = new CopyOnWriteArraySet<>();
	
	public MatchingRoomManager()
	{
		_holder[MatchingRoom.PARTY_MATCHING] = new RoomsHolder();
		_holder[MatchingRoom.CC_MATCHING] = new RoomsHolder();
	}
	
	public static MatchingRoomManager getInstance()
	{
		return _instance;
	}
	
	public void addToWaitingList(Player player)
	{
		_players.add(player);
	}
	
	public void removeFromWaitingList(Player player)
	{
		_players.remove(player);
	}
	
	public List<Player> getWaitingList(int minLevel, int maxLevel, int[] classes)
	{
		ArrayList<Player> res = new ArrayList<>();
		for(Player member : _players)
		{
			if(member.getLevel() < minLevel || member.getLevel() > maxLevel || classes.length != 0 && !ArrayUtils.contains(classes, member.getClassId().getId()))
				continue;
			res.add(member);
		}
		return res;
	}
	
	public List<MatchingRoom> getMatchingRooms(int type, int region, boolean allLevels, Player activeChar)
	{
		ArrayList<MatchingRoom> res = new ArrayList<>();
		for(MatchingRoom room : _holder[type]._rooms.values())
		{
			if(region > 0 && room.getLocationId() != region || region == -2 && room.getLocationId() != getInstance().getLocation(activeChar) || !allLevels && (room.getMinLevel() > activeChar.getLevel() || room.getMaxLevel() < activeChar.getLevel()))
				continue;
			res.add(room);
		}
		return res;
	}
	
	public int addMatchingRoom(MatchingRoom r)
	{
		return _holder[r.getType()].addRoom(r);
	}
	
	public void removeMatchingRoom(MatchingRoom r)
	{
		_holder[r.getType()]._rooms.remove(r.getId());
	}
	
	public MatchingRoom getMatchingRoom(int type, int id)
	{
		return _holder[type]._rooms.get(id);
	}
	
	public int getLocation(Player player)
	{
		if(player == null)
		{
			return 0;
		}
		RestartArea ra = MapRegionManager.getInstance().getRegionData(RestartArea.class, player);
		if(ra != null)
		{
			RestartPoint rp = ra.getRestartPoint().get(player.getRace());
			return rp.getBbs();
		}
		return 0;
	}
	
	private class RoomsHolder
	{
		private final IntObjectMap<MatchingRoom> _rooms;
		private int _id;
		
		private RoomsHolder()
		{
			_id = 1;
			_rooms = new CHashIntObjectMap();
		}
		
		public int addRoom(MatchingRoom r)
		{
			int val = _id++;
			_rooms.put(val, r);
			return val;
		}
	}
}