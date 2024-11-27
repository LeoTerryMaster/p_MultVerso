package l2.gameserver.network.l2.s2c;

public class ExEventMatchMessage extends L2GameServerPacket
{
	public static final ExEventMatchMessage FINISH = new ExEventMatchMessage(1);
	public static final ExEventMatchMessage START = new ExEventMatchMessage(2);
	public static final ExEventMatchMessage GAMEOVER = new ExEventMatchMessage(3);
	public static final ExEventMatchMessage COUNT1 = new ExEventMatchMessage(4);
	public static final ExEventMatchMessage COUNT2 = new ExEventMatchMessage(5);
	public static final ExEventMatchMessage COUNT3 = new ExEventMatchMessage(6);
	public static final ExEventMatchMessage COUNT4 = new ExEventMatchMessage(7);
	public static final ExEventMatchMessage COUNT5 = new ExEventMatchMessage(8);
	private final int _type;
	private final String _message;
	
	public ExEventMatchMessage(int type)
	{
		_type = type;
		_message = "";
	}
	
	public ExEventMatchMessage(String message)
	{
		_type = 0;
		_message = message;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(4);
		writeC(_type);
		writeS(_message);
	}
}