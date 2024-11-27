package l2.gameserver.network.l2.s2c;

public class AskJoinAlliance extends L2GameServerPacket
{
	private final String _requestorName;
	private final String _requestorAllyName;
	private final int _requestorId;
	
	public AskJoinAlliance(int requestorId, String requestorName, String requestorAllyName)
	{
		_requestorName = requestorName;
		_requestorAllyName = requestorAllyName;
		_requestorId = requestorId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(168);
		writeD(_requestorId);
		writeS(_requestorName);
		writeS("");
		writeS(_requestorAllyName);
	}
}