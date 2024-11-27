package l2.gameserver.network.l2.s2c;

public class MyTargetSelected extends L2GameServerPacket
{
	private final int _objectId;
	private final int _color;
	
	public MyTargetSelected(int objectId, int color)
	{
		_objectId = objectId;
		_color = color;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(166);
		writeD(_objectId);
		writeH(_color);
		writeD(0);
	}
}