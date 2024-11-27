package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.pledge.RankPrivs;

public class PledgePowerGradeList extends L2GameServerPacket
{
	private final RankPrivs[] _privs;
	
	public PledgePowerGradeList(RankPrivs[] privs)
	{
		_privs = privs;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(59);
		writeD(_privs.length);
		for(RankPrivs element : _privs)
		{
			writeD(element.getRank());
			writeD(element.getParty());
		}
	}
}