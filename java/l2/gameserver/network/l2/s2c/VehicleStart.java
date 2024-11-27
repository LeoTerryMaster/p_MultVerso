package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.entity.boat.Boat;

public class VehicleStart extends L2GameServerPacket
{
	private final int _objectId;
	private final int _state;
	
	public VehicleStart(Boat boat)
	{
		_objectId = boat.getObjectId();
		_state = boat.getRunState();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(186);
		writeD(_objectId);
		writeD(_state);
	}
}