package l2.gameserver.network.l2.s2c;

public class ExGoodsInventoryChangedNotify extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ExGoodsInventoryChangedNotify();
	
	@Override
	protected void writeImpl()
	{
		writeEx(226);
	}
}