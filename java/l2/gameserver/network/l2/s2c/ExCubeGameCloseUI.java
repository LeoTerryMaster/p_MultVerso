package l2.gameserver.network.l2.s2c;

public class ExCubeGameCloseUI extends L2GameServerPacket
{
	int _seconds;
	
	@Override
	protected void writeImpl()
	{
		writeEx(151);
		writeD(-1);
	}
}