package l2.gameserver.network.l2.s2c;

public class ExEventMatchLockResult extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(11);
	}
}