package l2.gameserver.network.l2.c2s;

import l2.gameserver.data.BoatHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.boat.Boat;
import l2.gameserver.utils.Location;

public class RequestGetOffVehicle extends L2GameClientPacket
{
	private final Location _location = new Location();
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_location.x = readD();
		_location.y = readD();
		_location.z = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		Boat boat = BoatHolder.getInstance().getBoat(_objectId);
		if(boat == null || boat.isMoving())
		{
			player.sendActionFailed();
			return;
		}
		boat.oustPlayer(player, _location, false);
	}
}