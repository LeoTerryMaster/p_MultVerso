package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Party;
import l2.gameserver.model.Player;

public class ExMPCCPartyInfoUpdate extends L2GameServerPacket
{
	private final Party _party;
	private final int _mode;
	private final int _count;
	Player _leader;
	
	public ExMPCCPartyInfoUpdate(Party party, int mode)
	{
		_party = party;
		_mode = mode;
		_count = _party.getMemberCount();
		_leader = _party.getPartyLeader();
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(90);
		writeS(_leader.getName());
		writeD(_leader.getObjectId());
		writeD(_count);
		writeD(_mode);
	}
}