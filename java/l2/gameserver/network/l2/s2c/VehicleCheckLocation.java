package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.entity.boat.Boat;
import l2.gameserver.utils.Location;

public class VehicleCheckLocation extends L2GameServerPacket
{
	private final int _boatObjectId;
	private final Location _loc;
	
	public VehicleCheckLocation(Boat instance)
	{
		_boatObjectId = instance.getObjectId();
		_loc = instance.getLoc();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(91);
		writeD(_boatObjectId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
	}
}