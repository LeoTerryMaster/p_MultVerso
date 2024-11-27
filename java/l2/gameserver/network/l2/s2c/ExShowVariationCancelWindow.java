package l2.gameserver.network.l2.s2c;

public class ExShowVariationCancelWindow extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ExShowVariationCancelWindow();
	
	@Override
	protected final void writeImpl()
	{
		writeEx(81);
	}
}