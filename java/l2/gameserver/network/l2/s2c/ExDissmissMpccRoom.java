package l2.gameserver.network.l2.s2c;

public class ExDissmissMpccRoom extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ExDissmissMpccRoom();
	
	@Override
	protected void writeImpl()
	{
		writeEx(38);
	}
}