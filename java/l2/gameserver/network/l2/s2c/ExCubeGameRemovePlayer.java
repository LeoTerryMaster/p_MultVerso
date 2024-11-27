package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

public class ExCubeGameRemovePlayer extends L2GameServerPacket
{
	private final int _objectId;
	private final boolean _isRedTeam;
	
	public ExCubeGameRemovePlayer(Player player, boolean isRedTeam)
	{
		_objectId = player.getObjectId();
		_isRedTeam = isRedTeam;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(151);
		writeD(2);
		writeD(-1);
		writeD(_isRedTeam ? 1 : 0);
		writeD(_objectId);
	}
}