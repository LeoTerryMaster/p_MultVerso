package l2.gameserver.network.l2.c2s;

import l2.gameserver.data.BoatHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.boat.Boat;
import l2.gameserver.utils.Location;

public class RequestGetOnVehicle extends L2GameClientPacket
{
	private final Location _loc = new Location();
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
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
		if(boat == null)
		{
			return;
		}
		player._stablePoint = boat.getCurrentWay().getReturnLoc();
		boat.addPlayer(player, _loc);
	}
}