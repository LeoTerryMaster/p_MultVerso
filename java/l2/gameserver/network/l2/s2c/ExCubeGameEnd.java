package l2.gameserver.network.l2.s2c;

public class ExCubeGameEnd extends L2GameServerPacket
{
	boolean _isRedTeamWin;
	
	public ExCubeGameEnd(boolean isRedTeamWin)
	{
		_isRedTeamWin = isRedTeamWin;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(152);
		writeD(1);
		writeD(_isRedTeamWin ? 1 : 0);
	}
}