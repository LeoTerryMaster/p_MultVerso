package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.utils.Location;

public class Ride extends L2GameServerPacket
{
	private final int _mountType;
	private final int _id;
	private final int _rideClassID;
	private final Location _loc;
	
	public Ride(Player cha)
	{
		_id = cha.getObjectId();
		_mountType = cha.getMountType();
		_rideClassID = cha.getMountNpcId() + 1000000;
		_loc = cha.getLoc();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(134);
		writeD(_id);
		writeD(_mountType == 0 ? 0 : 1);
		writeD(_mountType);
		writeD(_rideClassID);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}