package l2.gameserver.network.l2.s2c;

import l2.gameserver.utils.Location;

public class TargetSelected extends L2GameServerPacket
{
	private final int _objectId;
	private final int _targetId;
	private final Location _loc;
	
	public TargetSelected(int objectId, int targetId, Location loc)
	{
		_objectId = objectId;
		_targetId = targetId;
		_loc = loc;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(41);
		writeD(_objectId);
		writeD(_targetId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}