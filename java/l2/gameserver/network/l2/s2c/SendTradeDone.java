package l2.gameserver.network.l2.s2c;

public class SendTradeDone extends L2GameServerPacket
{
	public static final L2GameServerPacket SUCCESS = new SendTradeDone(1);
	public static final L2GameServerPacket FAIL = new SendTradeDone(0);
	private final int _response;
	
	private SendTradeDone(int num)
	{
		_response = num;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(34);
		writeD(_response);
	}
}