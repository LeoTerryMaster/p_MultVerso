package l2.gameserver.network.l2.s2c;

public class LeaveWorld extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new LeaveWorld();
	
	@Override
	protected final void writeImpl()
	{
		writeC(126);
	}
}