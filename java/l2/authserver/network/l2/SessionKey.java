package l2.authserver.network.l2;

import l2.commons.util.Rnd;

public class SessionKey
{
	public final int playOkID1;
	public final int playOkID2;
	public final int loginOkID1;
	public final int loginOkID2;
	private final int hashCode;
	
	public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
	{
		playOkID1 = playOK1;
		playOkID2 = playOK2;
		loginOkID1 = loginOK1;
		loginOkID2 = loginOK2;
		int hashCode = playOK1;
		hashCode *= 17;
		hashCode += playOK2;
		hashCode *= 37;
		hashCode += loginOK1;
		hashCode *= 51;
		hashCode += loginOK2;
		this.hashCode = hashCode;
	}
	
	public static final SessionKey create()
	{
		return new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt());
	}
	
	public boolean checkLoginPair(int loginOk1, int loginOk2)
	{
		return loginOkID1 == loginOk1 && loginOkID2 == loginOk2;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null)
		{
			return false;
		}
		if(o.getClass() == getClass())
		{
			SessionKey skey = (SessionKey) o;
			return playOkID1 == skey.playOkID1 && playOkID2 == skey.playOkID2 && skey.checkLoginPair(loginOkID1, loginOkID2);
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return hashCode;
	}
	
	@Override
	public String toString()
	{
		return "[playOkID1: " + playOkID1 + " playOkID2: " + playOkID2 + " loginOkID1: " + loginOkID1 + " loginOkID2: " + loginOkID2 + "]";
	}
}