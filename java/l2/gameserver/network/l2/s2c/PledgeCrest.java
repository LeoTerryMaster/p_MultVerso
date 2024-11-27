package l2.gameserver.network.l2.s2c;

public class PledgeCrest extends L2GameServerPacket
{
	private final int _crestId;
	private final int _crestSize;
	private final byte[] _data;
	
	public PledgeCrest(int crestId, byte[] data)
	{
		_crestId = crestId;
		_data = data;
		_crestSize = _data.length;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(108);
		writeD(_crestId);
		writeD(_crestSize);
		writeB(_data);
	}
}