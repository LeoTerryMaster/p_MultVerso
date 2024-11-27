package l2.gameserver.network.l2.s2c;

import l2.commons.lang.ArrayUtils;
import l2.gameserver.instancemanager.MatchingRoomManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.matching.MatchingRoom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExPartyRoomMember extends L2GameServerPacket
{
	private final int _type;
	private List<PartyRoomMemberInfo> _members = Collections.emptyList();
	
	public ExPartyRoomMember(MatchingRoom room, Player activeChar)
	{
		_type = room.getMemberType(activeChar);
		_members = new ArrayList<>(room.getPlayers().size());
		for(Player member : room.getPlayers())
		{
			_members.add(new PartyRoomMemberInfo(member, room.getMemberType(member)));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(14);
		writeD(_type);
		writeD(_members.size());
		for(PartyRoomMemberInfo member_info : _members)
		{
			writeD(member_info.objectId);
			writeS(member_info.name);
			writeD(member_info.classId);
			writeD(member_info.level);
			writeD(member_info.location);
			writeD(member_info.memberType);
		}
	}
	
	static class PartyRoomMemberInfo
	{
		public final int objectId;
		public final int classId;
		public final int level;
		public final int location;
		public final int memberType;
		public final String name;
		public final int[] instanceReuses;
		
		public PartyRoomMemberInfo(Player member, int type)
		{
			objectId = member.getObjectId();
			name = member.getName();
			classId = member.getClassId().ordinal();
			level = member.getLevel();
			location = MatchingRoomManager.getInstance().getLocation(member);
			memberType = type;
			instanceReuses = ArrayUtils.toArray(member.getInstanceReuses().keySet());
		}
	}
}