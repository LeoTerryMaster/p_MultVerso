package l2.gameserver.network.l2.s2c;

import l2.gameserver.Config;
import l2.gameserver.model.GameObject;
import l2.gameserver.utils.Location;

public class TeleportToLocation extends L2GameServerPacket
{
	private final int _targetId;
	private final Location _loc;
	
	public TeleportToLocation(GameObject cha, Location loc)
	{
		_targetId = cha.getObjectId();
		_loc = loc;
	}
	
	public TeleportToLocation(GameObject cha, int x, int y, int z)
	{
		_targetId = cha.getObjectId();
		_loc = new Location(x, y, z, cha.getHeading());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(40);
		writeD(_targetId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
	}
}