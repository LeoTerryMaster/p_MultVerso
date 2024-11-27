package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.boat.Boat;
import l2.gameserver.utils.Location;

public class MoveToLocationInVehicle extends L2GameServerPacket
{
	private final int _playerObjectId;
	private final int _boatObjectId;
	private final Location _origin;
	private final Location _destination;
	
	public MoveToLocationInVehicle(Player cha, Boat boat, Location origin, Location destination)
	{
		_playerObjectId = cha.getObjectId();
		_boatObjectId = boat.getObjectId();
		_origin = origin;
		_destination = destination;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(113);
		writeD(_playerObjectId);
		writeD(_boatObjectId);
		writeD(_destination.x);
		writeD(_destination.y);
		writeD(_destination.z);
		writeD(_origin.x);
		writeD(_origin.y);
		writeD(_origin.z);
	}
}