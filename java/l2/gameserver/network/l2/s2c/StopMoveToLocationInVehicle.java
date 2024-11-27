package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.utils.Location;

public class StopMoveToLocationInVehicle extends L2GameServerPacket
{
	private final int _boatObjectId;
	private final int _playerObjectId;
	private final int _heading;
	private final Location _loc;
	
	public StopMoveToLocationInVehicle(Player player)
	{
		_boatObjectId = player.getBoat().getObjectId();
		_playerObjectId = player.getObjectId();
		_loc = player.getInBoatPosition();
		_heading = player.getHeading();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(114);
		writeD(_playerObjectId);
		writeD(_boatObjectId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_heading);
	}
}