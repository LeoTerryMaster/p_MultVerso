package l2.authserver.crypt;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

public class ScrambledKeyPair
{
	private final KeyPair _pair;
	private final byte[] _scrambledModulus;
	
	public ScrambledKeyPair(KeyPair pPair)
	{
		_pair = pPair;
		_scrambledModulus = scrambleModulus(((RSAPublicKey) _pair.getPublic()).getModulus());
	}
	
	private static final byte[] scrambleModulus(BigInteger modulus)
	{
		byte[] scrambledMod = modulus.toByteArray();
		if(scrambledMod.length == 129 && scrambledMod[0] == 0)
		{
			byte[] temp = new byte[128];
			System.arraycopy(scrambledMod, 1, temp, 0, 128);
			scrambledMod = temp;
		}
		int i;
		for(i = 0;i < 4;++i)
		{
			byte temp = scrambledMod[i];
			scrambledMod[i] = scrambledMod[77 + i];
			scrambledMod[77 + i] = temp;
		}
		for(i = 0;i < 64;++i)
		{
			scrambledMod[i] = (byte) (scrambledMod[i] ^ scrambledMod[64 + i]);
		}
		for(i = 0;i < 4;++i)
		{
			scrambledMod[13 + i] = (byte) (scrambledMod[13 + i] ^ scrambledMod[52 + i]);
		}
		for(i = 0;i < 64;++i)
		{
			scrambledMod[64 + i] = (byte) (scrambledMod[64 + i] ^ scrambledMod[i]);
		}
		return scrambledMod;
	}
	
	public KeyPair getKeyPair()
	{
		return _pair;
	}
	
	public byte[] getScrambledModulus()
	{
		return _scrambledModulus;
	}
}