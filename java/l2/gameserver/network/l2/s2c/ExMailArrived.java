package l2.gameserver.network.l2.s2c;

public class ExMailArrived extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ExMailArrived();
	
	@Override
	protected final void writeImpl()
	{
		writeEx(45);
	}
}