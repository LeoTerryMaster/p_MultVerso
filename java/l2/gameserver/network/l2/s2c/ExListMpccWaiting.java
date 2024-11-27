package l2.gameserver.network.l2.s2c;

import l2.gameserver.instancemanager.MatchingRoomManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.matching.MatchingRoom;

import java.util.ArrayList;
import java.util.List;

public class ExListMpccWaiting extends L2GameServerPacket
{
	private static final int PAGE_SIZE = 10;
	private final int _fullSize;
	private final List<MatchingRoom> _list;
	
	public ExListMpccWaiting(Player player, int page, int location, boolean allLevels)
	{
		List<MatchingRoom> all = MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.CC_MATCHING, location, allLevels, player);
		_fullSize = all.size();
		_list = new ArrayList<>(10);
		int i = 0;
		int firstNot = page * 10;
		int first = (page - 1) * 10;
		for(MatchingRoom c : all)
		{
			if(i < first || i >= firstNot)
				continue;
			_list.add(c);
			++i;
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeEx(156);
		writeD(_fullSize);
		writeD(_list.size());
		for(MatchingRoom room : _list)
		{
			writeD(room.getId());
			writeS(room.getTopic());
			writeD(room.getPlayers().size());
			writeD(room.getMinLevel());
			writeD(room.getMaxLevel());
			writeD(1);
			writeD(room.getMaxMembersSize());
			Player leader = room.getLeader();
			writeS(leader == null ? "" : leader.getName());
		}
	}
}