package l2.gameserver.network.l2.s2c;

public class ExRedSky extends L2GameServerPacket
{
	private final int _duration;
	
	public ExRedSky(int duration)
	{
		_duration = duration;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(64);
		writeD(_duration);
	}
}