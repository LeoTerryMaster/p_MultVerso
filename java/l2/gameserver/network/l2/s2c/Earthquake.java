package l2.gameserver.network.l2.s2c;

import l2.gameserver.utils.Location;

public class Earthquake extends L2GameServerPacket
{
	private final Location _loc;
	private final int _intensity;
	private final int _duration;
	
	public Earthquake(Location loc, int intensity, int duration)
	{
		_loc = loc;
		_intensity = intensity;
		_duration = duration;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(196);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_intensity);
		writeD(_duration);
		writeD(0);
	}
}