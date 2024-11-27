package l2.gameserver.network.l2.s2c;

public class CameraMode extends L2GameServerPacket
{
	int _mode;
	
	public CameraMode(int mode)
	{
		_mode = mode;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(241);
		writeD(_mode);
	}
}