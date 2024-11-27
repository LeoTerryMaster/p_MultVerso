package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.pledge.UnitMember;

public class PledgeReceiveMemberInfo extends L2GameServerPacket
{
	private final UnitMember _member;
	
	public PledgeReceiveMemberInfo(UnitMember member)
	{
		_member = member;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(61);
		writeD(_member.getPledgeType());
		writeS(_member.getName());
		writeS(_member.getTitle());
		writeD(_member.getPowerGrade());
		writeS(_member.getSubUnit().getName());
		writeS(_member.getRelatedName());
	}
}