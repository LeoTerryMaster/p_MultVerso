package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.GameObject;

public class Revive extends L2GameServerPacket
{
	private final int _objectId;
	
	public Revive(GameObject obj)
	{
		_objectId = obj.getObjectId();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(7);
		writeD(_objectId);
	}
}