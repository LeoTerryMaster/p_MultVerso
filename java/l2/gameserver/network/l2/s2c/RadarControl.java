package l2.gameserver.network.l2.s2c;

import l2.gameserver.utils.Location;

public class RadarControl extends L2GameServerPacket
{
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _type;
	private final int _showRadar;
	
	public RadarControl(int showRadar, int type, Location loc)
	{
		this(showRadar, type, loc.x, loc.y, loc.z);
	}
	
	public RadarControl(int showRadar, int type, int x, int y, int z)
	{
		_showRadar = showRadar;
		_type = type;
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(235);
		writeD(_showRadar);
		writeD(_type);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}