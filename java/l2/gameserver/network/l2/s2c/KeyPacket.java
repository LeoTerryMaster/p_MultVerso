package l2.gameserver.network.l2.s2c;

public class KeyPacket extends L2GameServerPacket
{
	private final byte[] _key;
	
	public KeyPacket(byte[] key)
	{
		_key = key;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0);
		if(_key == null || _key.length == 0)
		{
			writeC(0);
			return;
		}
		writeC(1);
		writeB(_key);
		writeD(1);
		writeD(0);
		writeC(0);
		writeD(0);
	}
}