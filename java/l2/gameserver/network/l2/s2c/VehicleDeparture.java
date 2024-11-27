package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.entity.boat.Boat;
import l2.gameserver.utils.Location;

public class VehicleDeparture extends L2GameServerPacket
{
	private final int _moveSpeed;
	private final int _rotationSpeed;
	private final int _boatObjId;
	private Location _loc;
	
	public VehicleDeparture(Boat boat)
	{
		_boatObjId = boat.getObjectId();
		_moveSpeed = boat.getMoveSpeed();
		_rotationSpeed = boat.getRotationSpeed();
		_loc = boat.getDestination();
		if(_loc == null)
		{
			_loc = boat.getReturnLoc();
		}
	}
	
	public VehicleDeparture(Boat boat, Location dest)
	{
		_boatObjId = boat.getObjectId();
		_moveSpeed = boat.getMoveSpeed();
		_rotationSpeed = boat.getRotationSpeed();
		_loc = dest;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(90);
		writeD(_boatObjId);
		writeD(_moveSpeed);
		writeD(_rotationSpeed);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}