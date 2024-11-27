package l2.gameserver.network.l2.s2c;

public class ClientAction extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(143);
	}
}