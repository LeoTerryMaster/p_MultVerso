package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.utils.Location;

public class ValidateLocationInVehicle extends L2GameServerPacket
{
	private final int _playerObjectId;
	private final int _boatObjectId;
	private final Location _loc;
	
	public ValidateLocationInVehicle(Player player)
	{
		_playerObjectId = player.getObjectId();
		_boatObjectId = player.getBoat().getObjectId();
		_loc = player.getInBoatPosition();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(115);
		writeD(_playerObjectId);
		writeD(_boatObjectId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
	}
}