package l2.gameserver.network.l2.s2c;

public class ExOlympiadMode extends L2GameServerPacket
{
	private final int _mode;
	
	public ExOlympiadMode(int mode)
	{
		_mode = mode;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(43);
		writeC(_mode);
	}
}