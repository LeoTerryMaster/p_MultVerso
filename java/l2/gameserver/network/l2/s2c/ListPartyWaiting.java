package l2.gameserver.network.l2.s2c;

import l2.gameserver.instancemanager.MatchingRoomManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.matching.MatchingRoom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListPartyWaiting extends L2GameServerPacket
{
	private final Collection<MatchingRoom> _rooms;
	private final int _fullSize;
	
	public ListPartyWaiting(int region, boolean allLevels, int page, Player activeChar)
	{
		_rooms = new ArrayList<>();
		List<MatchingRoom> temp = MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.PARTY_MATCHING, region, allLevels, activeChar);
		_fullSize = temp.size();
		int i = 0;
		int firstNot = page * 64;
		int first = (page - 1) * 64;
		for(MatchingRoom room : temp)
		{
			if(i < first || i >= firstNot)
				continue;
			_rooms.add(room);
			++i;
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(150);
		writeD(_fullSize);
		writeD(_rooms.size());
		for(MatchingRoom room : _rooms)
		{
			writeD(room.getId());
			writeS(room.getLeader() == null ? "None" : room.getLeader().getName());
			writeD(room.getLocationId());
			writeD(room.getMinLevel());
			writeD(room.getMaxLevel());
			writeD(room.getMaxMembersSize());
			writeS(room.getTopic());
			Collection<Player> players = room.getPlayers();
			writeD(players.size());
			for(Player player : players)
			{
				writeD(player.getClassId().getId());
				writeS(player.getName());
			}
		}
	}
}