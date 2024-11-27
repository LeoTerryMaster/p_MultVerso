package l2.gameserver.network.l2.s2c;

public class ExPVPMatchCCMyRecord extends L2GameServerPacket
{
	private final int _points;
	
	public ExPVPMatchCCMyRecord(int points)
	{
		_points = points;
	}
	
	@Override
	public void writeImpl()
	{
		writeEx(138);
		writeD(_points);
	}
}