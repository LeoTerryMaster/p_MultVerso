package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.GameObject;
import l2.gameserver.utils.Location;

public class TargetUnselected extends L2GameServerPacket
{
	private final int _targetId;
	private final Location _loc;
	
	public TargetUnselected(GameObject obj)
	{
		_targetId = obj.getObjectId();
		_loc = obj.getLoc();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(42);
		writeD(_targetId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}