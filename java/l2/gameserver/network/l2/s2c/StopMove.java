package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Creature;
import l2.gameserver.utils.Location;

public class StopMove extends L2GameServerPacket
{
	private final int _objectId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _h;
	
	public StopMove(Creature cha)
	{
		_objectId = cha.getObjectId();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_h = cha.getHeading();
	}
	
	public StopMove(int obj_id, Location loc)
	{
		_objectId = obj_id;
		_x = loc.x;
		_y = loc.y;
		_z = loc.z;
		_h = loc.h;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(71);
		writeD(_objectId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_h);
	}
}