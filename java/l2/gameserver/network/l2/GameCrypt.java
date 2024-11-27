package l2.gameserver.network.l2;

public class GameCrypt
{
	private final byte[] _inKey = new byte[16];
	private final byte[] _outKey = new byte[16];
	private boolean _isEnabled;
	
	public void setKey(byte[] key)
	{
		System.arraycopy(key, 0, _inKey, 0, 16);
		System.arraycopy(key, 0, _outKey, 0, 16);
	}
	
	public void setKey(byte[] key, boolean value)
	{
		setKey(key);
	}
	
	public boolean decrypt(byte[] raw, int offset, int size)
	{
		if(!_isEnabled)
		{
			return true;
		}
		int temp = 0;
		for(int i = 0;i < size;++i)
		{
			int temp2 = raw[offset + i] & 255;
			raw[offset + i] = (byte) (temp2 ^ _inKey[i & 15] ^ temp);
			temp = temp2;
		}
		int old = _inKey[8] & 255;
		old |= _inKey[9] << 8 & 65280;
		old |= _inKey[10] << 16 & 16711680;
		old |= _inKey[11] << 24 & -16777216;
		_inKey[8] = (byte) ((old += size) & 255);
		_inKey[9] = (byte) (old >> 8 & 255);
		_inKey[10] = (byte) (old >> 16 & 255);
		_inKey[11] = (byte) (old >> 24 & 255);
		return true;
	}
	
	public void encrypt(byte[] raw, int offset, int size)
	{
		if(!_isEnabled)
		{
			_isEnabled = true;
			return;
		}
		int temp = 0;
		for(int i = 0;i < size;++i)
		{
			int temp2 = raw[offset + i] & 255;
			temp = temp2 ^ _outKey[i & 15] ^ temp;
			raw[offset + i] = (byte) temp;
		}
		int old = _outKey[8] & 255;
		old |= _outKey[9] << 8 & 65280;
		old |= _outKey[10] << 16 & 16711680;
		old |= _outKey[11] << 24 & -16777216;
		_outKey[8] = (byte) ((old += size) & 255);
		_outKey[9] = (byte) (old >> 8 & 255);
		_outKey[10] = (byte) (old >> 16 & 255);
		_outKey[11] = (byte) (old >> 24 & 255);
	}
}