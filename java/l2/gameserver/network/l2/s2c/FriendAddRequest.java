package l2.gameserver.network.l2.s2c;

public class FriendAddRequest extends L2GameServerPacket
{
	private final String _requestorName;
	
	public FriendAddRequest(String requestorName)
	{
		_requestorName = requestorName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(125);
		writeS(_requestorName);
		writeD(0);
	}
}