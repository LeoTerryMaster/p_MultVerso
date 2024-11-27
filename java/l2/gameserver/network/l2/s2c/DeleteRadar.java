package l2.gameserver.network.l2.s2c;

public class DeleteRadar extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(235);
		writeD(1);
	}
}