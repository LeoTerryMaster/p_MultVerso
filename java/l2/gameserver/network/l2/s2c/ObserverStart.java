package l2.gameserver.network.l2.s2c;

import l2.gameserver.utils.Location;

public class ObserverStart extends L2GameServerPacket
{
	private final Location _loc;
	
	public ObserverStart(Location loc)
	{
		_loc = loc;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(223);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeC(0);
		writeC(192);
		writeC(0);
	}
}