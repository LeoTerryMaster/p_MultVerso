package l2.gameserver.model.matching;

import l2.gameserver.model.CommandChannel;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ExDissmissMpccRoom;
import l2.gameserver.network.l2.s2c.ExManageMpccRoomMember;
import l2.gameserver.network.l2.s2c.ExMpccRoomMember;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.PartyRoomInfo;

public class CCMatchingRoom extends MatchingRoom
{
	public CCMatchingRoom(Player leader, int minLevel, int maxLevel, int maxMemberSize, int lootType, String topic)
	{
		super(leader, minLevel, maxLevel, maxMemberSize, lootType, topic);
		leader.sendPacket(SystemMsg.THE_COMMAND_CHANNEL_MATCHING_ROOM_WAS_CREATED);
	}
	
	@Override
	public SystemMsg notValidMessage()
	{
		return SystemMsg.YOU_CANNOT_ENTER_THE_COMMAND_CHANNEL_MATCHING_ROOM_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS;
	}
	
	@Override
	public SystemMsg enterMessage()
	{
		return SystemMsg.C1_ENTERED_THE_COMMAND_CHANNEL_MATCHING_ROOM;
	}
	
	@Override
	public SystemMsg exitMessage(boolean toOthers, boolean kick)
	{
		if(!toOthers)
		{
			return kick ? SystemMsg.YOU_WERE_EXPELLED_FROM_THE_COMMAND_CHANNEL_MATCHING_ROOM : SystemMsg.YOU_EXITED_FROM_THE_COMMAND_CHANNEL_MATCHING_ROOM;
		}
		return null;
	}
	
	@Override
	public SystemMsg closeRoomMessage()
	{
		return SystemMsg.THE_COMMAND_CHANNEL_MATCHING_ROOM_WAS_CANCELLED;
	}
	
	@Override
	public L2GameServerPacket closeRoomPacket()
	{
		return ExDissmissMpccRoom.STATIC;
	}
	
	@Override
	public L2GameServerPacket infoRoomPacket()
	{
		return new PartyRoomInfo(this);
	}
	
	@Override
	public L2GameServerPacket addMemberPacket(Player member, Player active)
	{
		return new ExManageMpccRoomMember(ExManageMpccRoomMember.ADD_MEMBER, this, active);
	}
	
	@Override
	public L2GameServerPacket removeMemberPacket(Player member, Player active)
	{
		return new ExManageMpccRoomMember(ExManageMpccRoomMember.REMOVE_MEMBER, this, active);
	}
	
	@Override
	public L2GameServerPacket updateMemberPacket(Player member, Player active)
	{
		return new ExManageMpccRoomMember(ExManageMpccRoomMember.UPDATE_MEMBER, this, active);
	}
	
	@Override
	public L2GameServerPacket membersPacket(Player active)
	{
		return new ExMpccRoomMember(this, active);
	}
	
	@Override
	public int getType()
	{
		return CC_MATCHING;
	}
	
	@Override
	public void disband()
	{
		CommandChannel commandChannel;
		Party party = _leader.getParty();
		if(party != null && (commandChannel = party.getCommandChannel()) != null)
		{
			commandChannel.setMatchingRoom(null);
		}
		super.disband();
	}
	
	@Override
	public int getMemberType(Player member)
	{
		Party party = _leader.getParty();
		CommandChannel commandChannel = party.getCommandChannel();
		if(member == _leader)
		{
			return MatchingRoom.UNION_LEADER;
		}
		if(member.getParty() == null)
		{
			return MatchingRoom.WAIT_NORMAL;
		}
		if(member.getParty() == party || commandChannel.getParties().contains(member.getParty()))
		{
			return MatchingRoom.UNION_PARTY;
		}
		if(member.getParty() != null)
		{
			return MatchingRoom.WAIT_PARTY;
		}
		return MatchingRoom.WAIT_NORMAL;
	}
}