package l2.gameserver.network.l2.s2c;

import l2.gameserver.instancemanager.MatchingRoomManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.matching.MatchingRoom;

public class ExManageMpccRoomMember extends L2GameServerPacket
{
	public static int ADD_MEMBER;
	public static int UPDATE_MEMBER = 1;
	public static int REMOVE_MEMBER = 2;
	private final int _type;
	private final MpccRoomMemberInfo _memberInfo;
	
	public ExManageMpccRoomMember(int type, MatchingRoom room, Player target)
	{
		_type = type;
		_memberInfo = new MpccRoomMemberInfo(target, room.getMemberType(target));
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(16);
		writeD(_type);
		writeD(_memberInfo.objectId);
		writeS(_memberInfo.name);
		writeD(_memberInfo.classId);
		writeD(_memberInfo.level);
		writeD(_memberInfo.location);
		writeD(_memberInfo.memberType);
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