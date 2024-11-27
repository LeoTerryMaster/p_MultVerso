package l2.gameserver.network.l2.s2c;

import l2.gameserver.instancemanager.MatchingRoomManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.matching.MatchingRoom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExMpccRoomMember extends L2GameServerPacket
{
	private final int _type;
	private List<MpccRoomMemberInfo> _members = Collections.emptyList();
	
	public ExMpccRoomMember(MatchingRoom room, Player player)
	{
		_type = room.getMemberType(player);
		_members = new ArrayList<>(room.getPlayers().size());
		for(Player member : room.getPlayers())
		{
			_members.add(new MpccRoomMemberInfo(member, room.getMemberType(member)));
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeEx(14);
		writeD(_type);
		writeD(_members.size());
		for(MpccRoomMemberInfo member : _members)
		{
			writeD(member.objectId);
			writeS(member.name);
			writeD(member.level);
			writeD(member.classId);
			writeD(member.location);
			writeD(member.memberType);
		}
	}
	
	static class MpccRoomMemberInfo
	{
		public final int objectId;
		public final int classId;
		public final int level;
		public final int location;
		public final int memberType;
		public final String name;
		
		public MpccRoomMemberInfo(Player member, int type)
		{
			objectId = member.getObjectId();
			name = member.getName();
			classId = member.getClassId().ordinal();
			level = member.getLevel();
			location = MatchingRoomManager.getInstance().getLocation(member);
			memberType = type;
		}
	}
}