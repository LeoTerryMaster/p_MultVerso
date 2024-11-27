package l2.gameserver.network.l2.s2c;

public class AskJoinPledge extends L2GameServerPacket
{
	private final int _requestorId;
	private final String _pledgeName;
	
	public AskJoinPledge(int requestorId, String pledgeName)
	{
		_requestorId = requestorId;
		_pledgeName = pledgeName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(50);
		writeD(_requestorId);
		writeS(_pledgeName);
	}
}