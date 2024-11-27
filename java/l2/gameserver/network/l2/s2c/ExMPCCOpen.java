package l2.gameserver.network.l2.s2c;

public class ExMPCCOpen extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ExMPCCOpen();
	
	@Override
	protected void writeImpl()
	{
		writeEx(37);
	}
}