package l2.gameserver.model.entity.boat;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.*;
import l2.gameserver.templates.CharTemplate;
import l2.gameserver.utils.Location;

public class Vehicle extends Boat
{
	public Vehicle(int objectId, CharTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public L2GameServerPacket startPacket()
	{
		return new VehicleStart(this);
	}
	
	@Override
	public L2GameServerPacket validateLocationPacket(Player player)
	{
		return new ValidateLocationInVehicle(player);
	}
	
	@Override
	public L2GameServerPacket checkLocationPacket()
	{
		return new VehicleCheckLocation(this);
	}
	
	@Override
	public L2GameServerPacket infoPacket()
	{
		return new VehicleInfo(this);
	}
	
	@Override
	public L2GameServerPacket movePacket()
	{
		return new VehicleDeparture(this, getDestination());
	}
	
	@Override
	public L2GameServerPacket inMovePacket(Player player, Location src, Location desc)
	{
		return new MoveToLocationInVehicle(player, this, src, desc);
	}
	
	@Override
	public L2GameServerPacket stopMovePacket()
	{
		return new StopMove(this);
	}
	
	@Override
	public L2GameServerPacket inStopMovePacket(Player player)
	{
		return new StopMoveToLocationInVehicle(player);
	}
	
	@Override
	public L2GameServerPacket getOnPacket(Player player, Location location)
	{
		return new GetOnVehicle(player, this, location);
	}
	
	@Override
	public L2GameServerPacket getOffPacket(Player player, Location location)
	{
		return new GetOffVehicle(player, this, location);
	}
	
	@Override
	public void oustPlayers()
	{
	}
	
	@Override
	public boolean isVehicle()
	{
		return true;
	}
}