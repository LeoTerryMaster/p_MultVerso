package l2.gameserver.network.l2.s2c;

public class ExCubeGameRequestReady extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(151);
		writeD(4);
	}
}