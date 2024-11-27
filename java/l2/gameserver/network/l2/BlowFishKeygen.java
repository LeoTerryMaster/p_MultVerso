package l2.gameserver.network.l2;

import l2.commons.util.Rnd;

public class BlowFishKeygen
{
	private static final int CRYPT_KEYS_SIZE = 20;
	private static final byte[][] CRYPT_KEYS = new byte[20][16];
	
	static
	{
		for(int i = 0;i < 20;++i)
		{
			for(int j = 0;j < CRYPT_KEYS[i].length;++j)
			{
				CRYPT_KEYS[i][j] = (byte) Rnd.get(255);
			}
			CRYPT_KEYS[i][8] = -56;
			CRYPT_KEYS[i][9] = 39;
			CRYPT_KEYS[i][10] = -109;
			CRYPT_KEYS[i][11] = 1;
			CRYPT_KEYS[i][12] = -95;
			CRYPT_KEYS[i][13] = 108;
			CRYPT_KEYS[i][14] = 49;
			CRYPT_KEYS[i][15] = -105;
		}
	}
	
	private BlowFishKeygen()
	{
	}
	
	public static byte[] getRandomKey()
	{
		return CRYPT_KEYS[Rnd.get(20)];
	}
}