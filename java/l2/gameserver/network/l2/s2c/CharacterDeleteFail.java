package l2.gameserver.network.l2.s2c;

public class CharacterDeleteFail extends L2GameServerPacket
{
	public static int REASON_DELETION_FAILED = 1;
	public static int REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER = 2;
	public static int REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED = 3;
	int _error;
	
	public CharacterDeleteFail(int error)
	{
		_error = error;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(36);
		writeD(_error);
	}
}