package l2.gameserver.network.l2.s2c;

import l2.gameserver.utils.Location;

public class ObserverEnd extends L2GameServerPacket
{
	private final Location _loc;
	
	public ObserverEnd(Location loc)
	{
		_loc = loc;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(224);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}