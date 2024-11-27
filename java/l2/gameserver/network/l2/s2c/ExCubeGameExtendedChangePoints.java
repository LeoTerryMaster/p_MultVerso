package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

public class ExCubeGameExtendedChangePoints extends L2GameServerPacket
{
	private final int _timeLeft;
	private final int _bluePoints;
	private final int _redPoints;
	private final boolean _isRedTeam;
	private final int _objectId;
	private final int _playerPoints;
	
	public ExCubeGameExtendedChangePoints(int timeLeft, int bluePoints, int redPoints, boolean isRedTeam, Player player, int playerPoints)
	{
		_timeLeft = timeLeft;
		_bluePoints = bluePoints;
		_redPoints = redPoints;
		_isRedTeam = isRedTeam;
		_objectId = player.getObjectId();
		_playerPoints = playerPoints;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(152);
		writeD(0);
		writeD(_timeLeft);
		writeD(_bluePoints);
		writeD(_redPoints);
		writeD(_isRedTeam ? 1 : 0);
		writeD(_objectId);
		writeD(_playerPoints);
	}
}