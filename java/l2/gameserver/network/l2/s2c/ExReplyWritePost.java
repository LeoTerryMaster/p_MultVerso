package l2.gameserver.network.l2.s2c;

public class ExReplyWritePost extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC_TRUE = new ExReplyWritePost(1);
	public static final L2GameServerPacket STATIC_FALSE = new ExReplyWritePost(0);
	private final int _reply;
	
	public ExReplyWritePost(int i)
	{
		_reply = i;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(180);
		writeD(_reply);
	}
}