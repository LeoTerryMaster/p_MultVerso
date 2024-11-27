package l2.gameserver.network.l2.s2c;

public class ExPVPMatchUserDie extends L2GameServerPacket
{
	private final int _blueKills;
	private final int _redKills;
	
	public ExPVPMatchUserDie(int blue, int red)
	{
		_blueKills = blue;
		_redKills = red;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(127);
		writeD(_blueKills);
		writeD(_redKills);
	}
}