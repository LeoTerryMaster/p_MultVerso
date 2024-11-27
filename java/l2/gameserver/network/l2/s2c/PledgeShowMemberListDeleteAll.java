package l2.gameserver.network.l2.s2c;

public class PledgeShowMemberListDeleteAll extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new PledgeShowMemberListDeleteAll();
	
	@Override
	protected final void writeImpl()
	{
		writeC(130);
	}
}