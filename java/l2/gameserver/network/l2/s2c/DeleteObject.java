package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.GameObject;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;

public class DeleteObject extends L2GameServerPacket
{
	private final int _objectId;
	
	public DeleteObject(GameObject obj)
	{
		_objectId = obj.getObjectId();
	}
	
	@Override
	protected final void writeImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.getObjectId() == _objectId)
		{
			return;
		}
		writeC(18);
		writeD(_objectId);
		writeD(1);
	}
	
	@Override
	public String getType()
	{
		return super.getType() + " " + GameObjectsStorage.findObject(_objectId) + " (" + _objectId + ")";
	}
}