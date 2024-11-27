package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.StaticObjectInstance;

public class ChairSit extends L2GameServerPacket
{
	private final int _objectId;
	private final int _staticObjectId;
	
	public ChairSit(Player player, StaticObjectInstance throne)
	{
		_objectId = player.getObjectId();
		_staticObjectId = throne.getUId();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(225);
		writeD(_objectId);
		writeD(_staticObjectId);
	}
}