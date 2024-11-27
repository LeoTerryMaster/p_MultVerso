package l2.gameserver.network.l2.s2c;

public class ShowXMasSeal extends L2GameServerPacket
{
	private final int _item;
	
	public ShowXMasSeal(int item)
	{
		_item = item;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(242);
		writeD(_item);
	}
}