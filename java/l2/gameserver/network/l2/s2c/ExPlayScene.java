package l2.gameserver.network.l2.s2c;

public class ExPlayScene extends L2GameServerPacket
{
	public static final ExPlayScene STATIC = new ExPlayScene();
	
	@Override
	protected void writeImpl()
	{
		writeEx(91);
		writeD(0);
	}
}