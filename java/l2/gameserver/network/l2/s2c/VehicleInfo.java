package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.entity.boat.Boat;
import l2.gameserver.utils.Location;

public class VehicleInfo extends L2GameServerPacket
{
	private final int _boatObjectId;
	private final Location _loc;
	
	public VehicleInfo(Boat boat)
	{
		_boatObjectId = boat.getObjectId();
		_loc = boat.getLoc();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(89);
		writeD(_boatObjectId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
	}
}