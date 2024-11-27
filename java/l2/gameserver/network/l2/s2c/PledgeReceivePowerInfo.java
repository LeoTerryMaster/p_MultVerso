package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.pledge.RankPrivs;
import l2.gameserver.model.pledge.UnitMember;

public class PledgeReceivePowerInfo extends L2GameServerPacket
{
	private final int PowerGrade;
	private final int privs;
	private final String member_name;
	
	public PledgeReceivePowerInfo(UnitMember member)
	{
		PowerGrade = member.getPowerGrade();
		member_name = member.getName();
		RankPrivs temp;
		privs = member.isClanLeader() ? 8388606 : (temp = member.getClan().getRankPrivs(member.getPowerGrade())) != null ? temp.getPrivs() : 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(60);
		writeD(PowerGrade);
		writeS(member_name);
		writeD(privs);
	}
}