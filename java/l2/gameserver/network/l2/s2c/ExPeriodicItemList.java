package l2.gameserver.network.l2.s2c;

public class ExPeriodicItemList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(135);
		writeD(0);
	}
}