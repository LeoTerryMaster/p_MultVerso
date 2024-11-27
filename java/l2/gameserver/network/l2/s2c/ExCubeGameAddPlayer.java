package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

public class ExCubeGameAddPlayer extends L2GameServerPacket
{
	private final int _objectId;
	private final String _name;
	boolean _isRedTeam;
	
	public ExCubeGameAddPlayer(Player player, boolean isRedTeam)
	{
		_objectId = player.getObjectId();
		_name = player.getName();
		_isRedTeam = isRedTeam;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(151);
		writeD(1);
		writeD(-1);
		writeD(_isRedTeam ? 1 : 0);
		writeD(_objectId);
		writeS(_name);
	}
}