package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

public class ExBR_GamePoint extends L2GameServerPacket
{
	private final int _objectId;
	private final long _points;
	
	public ExBR_GamePoint(Player player)
	{
		_objectId = player.getObjectId();
		_points = player.getPremiumPoints();
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(213);
		writeD(_objectId);
		writeQ(_points);
		writeD(0);
	}
}