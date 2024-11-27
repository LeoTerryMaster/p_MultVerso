package l2.gameserver.network.l2.s2c;

public class ChooseInventoryItem extends L2GameServerPacket
{
	private final int ItemID;
	
	public ChooseInventoryItem(int id)
	{
		ItemID = id;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(111);
		writeD(ItemID);
	}
}