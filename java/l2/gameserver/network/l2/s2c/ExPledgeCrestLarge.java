package l2.gameserver.network.l2.s2c;

public class ExPledgeCrestLarge extends L2GameServerPacket
{
	private final int _crestId;
	private final byte[] _data;
	
	public ExPledgeCrestLarge(int crestId, byte[] data)
	{
		_crestId = crestId;
		_data = data;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(40);
		writeD(0);
		writeD(_crestId);
		writeD(_data.length);
		writeB(_data);
	}
}