package l2.gameserver.network.l2.s2c;

public class ExAskJoinMPCC extends L2GameServerPacket
{
	private final String _requestorName;
	
	public ExAskJoinMPCC(String requestorName)
	{
		_requestorName = requestorName;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(39);
		writeS(_requestorName);
	}
}