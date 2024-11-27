package l2.gameserver.network.l2.s2c;

public class RestartResponse extends L2GameServerPacket
{
	public static final RestartResponse OK = new RestartResponse(1);
	public static final RestartResponse FAIL = new RestartResponse(0);
	private final String _message = "bye";
	private final int _param;
	
	public RestartResponse(int param)
	{
		_param = param;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(95);
		writeD(_param);
		writeS(_message);
	}
}