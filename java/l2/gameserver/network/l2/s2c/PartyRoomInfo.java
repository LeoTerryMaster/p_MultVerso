package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.matching.MatchingRoom;

public class PartyRoomInfo extends L2GameServerPacket
{
	private final int _id;
	private final int _minLevel;
	private final int _maxLevel;
	private final int _lootDist;
	private final int _maxMembers;
	private final int _location;
	private final String _title;
	
	public PartyRoomInfo(MatchingRoom room)
	{
		_id = room.getId();
		_minLevel = room.getMinLevel();
		_maxLevel = room.getMaxLevel();
		_lootDist = room.getLootType();
		_maxMembers = room.getMaxMembersSize();
		_location = room.getLocationId();
		_title = room.getTopic();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(151);
		writeD(_id);
		writeD(_maxMembers);
		writeD(_minLevel);
		writeD(_maxLevel);
		writeD(_lootDist);
		writeD(_location);
		writeS(_title);
	}
}