package l2.gameserver.network.l2.s2c;

public class ExConfirmAddingPostFriend extends L2GameServerPacket
{
	public static int NAME_IS_NOT_EXISTS;
	public static int SUCCESS = 1;
	public static int PREVIOS_NAME_IS_BEEN_REGISTERED = -1;
	public static int NAME_IS_NOT_EXISTS2 = -2;
	public static int LIST_IS_FULL = -3;
	public static int ALREADY_ADDED = -4;
	public static int NAME_IS_NOT_REGISTERED = -4;
	private final String _name;
	private final int _result;
	
	public ExConfirmAddingPostFriend(String name, int s)
	{
		_name = name;
		_result = s;
	}
	
	@Override
	public void writeImpl()
	{
		writeEx(210);
		writeS(_name);
		writeD(_result);
	}
}