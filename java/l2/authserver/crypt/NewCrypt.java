package l2.authserver.crypt;

import java.io.IOException;

public class NewCrypt
{
	private final BlowfishEngine _crypt = new BlowfishEngine();
	private final BlowfishEngine _decrypt;
	
	public NewCrypt(byte[] blowfishKey)
	{
		_crypt.init(true, blowfishKey);
		_decrypt = new BlowfishEngine();
		_decrypt.init(false, blowfishKey);
	}
	
	public NewCrypt(String key)
	{
		this(key.getBytes());
	}
	
	public static boolean verifyChecksum(byte[] raw)
	{
		return verifyChecksum(raw, 0, raw.length);
	}
	
	public static boolean verifyChecksum(byte[] raw, int offset, int size)
	{
		if((size & 3) != 0 || size <= 4)
		{
			return false;
		}
		long chksum = 0;
		int count = size - 4;
		long check;
		int i;
		for(i = offset;i < count;i += 4)
		{
			check = raw[i] & 255;
			check |= (long) (raw[i + 1] << 8 & 65280);
			check |= (long) (raw[i + 2] << 16 & 16711680);
			check |= (long) (raw[i + 3] << 24 & -16777216);
			chksum ^= check;
		}
		check = raw[i] & 255;
		check |= (long) (raw[i + 1] << 8 & 65280);
		check |= (long) (raw[i + 2] << 16 & 16711680);
		check |= (long) (raw[i + 3] << 24 & -16777216);
		return check == chksum;
	}
	
	public static void appendChecksum(byte[] raw)
	{
		appendChecksum(raw, 0, raw.length);
	}
	
	public static void appendChecksum(byte[] raw, int offset, int size)
	{
		long ecx;
		int i;
		long chksum = 0;
		int count = size - 4;
		for(i = offset;i < count;i += 4)
		{
			ecx = raw[i] & 255;
			ecx |= (long) (raw[i + 1] << 8 & '\uff00');
			ecx |= (long) (raw[i + 2] << 16 & 16711680);
			ecx |= (long) (raw[i + 3] << 24 & -16777216);
			chksum ^= ecx;
		}
		ecx = raw[i] & 255;
		ecx |= (long) (raw[i + 1] << 8 & 65280);
		ecx |= (long) (raw[i + 2] << 16 & 16711680);
		ecx |= (long) (raw[i + 3] << 24 & -16777216);
		raw[i] = (byte) (chksum & 255);
		raw[i + 1] = (byte) (chksum >> 8 & 255);
		raw[i + 2] = (byte) (chksum >> 16 & 255);
		raw[i + 3] = (byte) (chksum >> 24 & 255);
	}
	
	public static void encXORPass(byte[] raw, int key)
	{
		encXORPass(raw, 0, raw.length, key);
	}
	
	public static void encXORPass(byte[] raw, int offset, int size, int key)
	{
		int stop = size - 8;
		int pos = 4 + offset;
		int ecx = key;
		while(pos < stop)
		{
			int edx = raw[pos] & 255;
			edx |= (raw[pos + 1] & 255) << 8;
			edx |= (raw[pos + 2] & 255) << 16;
			edx |= (raw[pos + 3] & 255) << 24;
			ecx += edx;
			edx ^= ecx;
			raw[pos++] = (byte) (edx & 255);
			raw[pos++] = (byte) (edx >> 8 & 255);
			raw[pos++] = (byte) (edx >> 16 & 255);
			raw[pos++] = (byte) (edx >> 24 & 255);
		}
		raw[pos++] = (byte) (ecx & 255);
		raw[pos++] = (byte) (ecx >> 8 & 255);
		raw[pos++] = (byte) (ecx >> 16 & 255);
		raw[pos] = (byte) (ecx >> 24 & 255);
	}
	
	public byte[] decrypt(byte[] raw) throws IOException
	{
		byte[] result = new byte[raw.length];
		int count = raw.length / 8;
		for(int i = 0;i < count;++i)
		{
			_decrypt.processBlock(raw, i * 8, result, i * 8);
		}
		return result;
	}
	
	public void decrypt(byte[] raw, int offset, int size) throws IOException
	{
		byte[] result = new byte[size];
		int count = size / 8;
		for(int i = 0;i < count;++i)
		{
			_decrypt.processBlock(raw, offset + i * 8, result, i * 8);
		}
		System.arraycopy(result, 0, raw, offset, size);
	}
	
	public byte[] crypt(byte[] raw) throws IOException
	{
		int count = raw.length / 8;
		byte[] result = new byte[raw.length];
		for(int i = 0;i < count;++i)
		{
			_crypt.processBlock(raw, i * 8, result, i * 8);
		}
		return result;
	}
	
	public void crypt(byte[] raw, int offset, int size) throws IOException
	{
		int count = size / 8;
		byte[] result = new byte[size];
		for(int i = 0;i < count;++i)
		{
			_crypt.processBlock(raw, offset + i * 8, result, i * 8);
		}
		System.arraycopy(result, 0, raw, offset, size);
	}
}