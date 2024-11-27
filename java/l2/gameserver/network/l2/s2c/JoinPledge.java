package l2.gameserver.network.l2.s2c;

public class JoinPledge extends L2GameServerPacket
{
	private final int _pledgeId;
	
	public JoinPledge(int pledgeId)
	{
		_pledgeId = pledgeId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(51);
		writeD(_pledgeId);
	}
}