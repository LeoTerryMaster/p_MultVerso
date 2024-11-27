package l2.gameserver.network.l2.s2c;

public class ExRestartClient extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeEx(70);
	}
}