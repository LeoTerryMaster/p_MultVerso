package l2.gameserver.network.l2.s2c;

public class AllianceCrest extends L2GameServerPacket
{
	private final int _crestId;
	private final byte[] _data;
	
	public AllianceCrest(int crestId, byte[] data)
	{
		_crestId = crestId;
		_data = data;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(174);
		writeD(_crestId);
		writeD(_data.length);
		writeB(_data);
	}
}