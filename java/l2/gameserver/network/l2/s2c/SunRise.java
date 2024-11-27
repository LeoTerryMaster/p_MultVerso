package l2.gameserver.network.l2.s2c;

public class SunRise extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(28);
	}
}