package l2.gameserver.network.l2.s2c;

public class L2FriendSay extends L2GameServerPacket
{
	private final String _sender;
	private final String _receiver;
	private final String _message;
	
	public L2FriendSay(String sender, String reciever, String message)
	{
		_sender = sender;
		_receiver = reciever;
		_message = message;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(253);
		writeD(0);
		writeS(_receiver);
		writeS(_sender);
		writeS(_message);
	}
}