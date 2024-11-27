package l2.gameserver.network.l2.s2c;

@Deprecated
public class TradePressOwnOk extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(34);
		writeD(0);
	}
}