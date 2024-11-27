package l2.gameserver.network.l2.s2c;

public class ExChangeNicknameNColor extends L2GameServerPacket
{
	private final int _itemObjId;
	
	public ExChangeNicknameNColor(int itemObjId)
	{
		_itemObjId = itemObjId;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(131);
		writeD(_itemObjId);
	}
}