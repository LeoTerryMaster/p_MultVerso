package l2.gameserver.network.l2.s2c;

public class SendTradeRequest extends L2GameServerPacket
{
	private final int _senderId;
	
	public SendTradeRequest(int senderId)
	{
		_senderId = senderId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(94);
		writeD(_senderId);
	}
}