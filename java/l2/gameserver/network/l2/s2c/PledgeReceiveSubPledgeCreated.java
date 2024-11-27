package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.pledge.SubUnit;

public class PledgeReceiveSubPledgeCreated extends L2GameServerPacket
{
	private final int type;
	private final String _name;
	private final String leader_name;
	
	public PledgeReceiveSubPledgeCreated(SubUnit subPledge)
	{
		type = subPledge.getType();
		_name = subPledge.getName();
		leader_name = subPledge.getLeaderName();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(63);
		writeD(1);
		writeD(type);
		writeS(_name);
		writeS(leader_name);
	}
}