package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.boat.Boat;
import l2.gameserver.utils.Location;

public class GetOffVehicle extends L2GameServerPacket
{
	private final int _playerObjectId;
	private final int _boatObjectId;
	private final Location _loc;
	
	public GetOffVehicle(Player cha, Boat boat, Location loc)
	{
		_playerObjectId = cha.getObjectId();
		_boatObjectId = boat.getObjectId();
		_loc = loc;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(93);
		writeD(_playerObjectId);
		writeD(_boatObjectId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}