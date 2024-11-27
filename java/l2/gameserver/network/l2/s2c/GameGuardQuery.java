package l2.gameserver.network.l2.s2c;

public class GameGuardQuery extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(249);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
	}
}