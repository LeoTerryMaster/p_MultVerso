package l2.gameserver.network.l2.s2c;

public class ExRotation extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _degree;
	
	public ExRotation(int charId, int degree)
	{
		_charObjId = charId;
		_degree = degree;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(193);
		writeD(_charObjId);
		writeD(_degree);
	}
}