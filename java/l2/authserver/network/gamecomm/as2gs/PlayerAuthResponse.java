package l2.authserver.network.gamecomm.as2gs;

import l2.authserver.accounts.Account;
import l2.authserver.accounts.SessionManager;
import l2.authserver.network.gamecomm.SendablePacket;
import l2.authserver.network.l2.SessionKey;

public class PlayerAuthResponse extends SendablePacket
{
	private final String login;
	private final boolean authed;
	private int playOkID1;
	private int playOkID2;
	private int loginOkID1;
	private int loginOkID2;
	private double bonus;
	private int bonusExpire;
	
	public PlayerAuthResponse(SessionManager.Session session, boolean authed)
	{
		Account account = session.getAccount();
		login = account.getLogin();
		this.authed = authed;
		if(authed)
		{
			SessionKey skey = session.getSessionKey();
			playOkID1 = skey.playOkID1;
			playOkID2 = skey.playOkID2;
			loginOkID1 = skey.loginOkID1;
			loginOkID2 = skey.loginOkID2;
		}
	}
	
	public PlayerAuthResponse(String account)
	{
		login = account;
		authed = false;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(2);
		writeS(login);
		writeC(authed ? 1 : 0);
		if(authed)
		{
			writeD(playOkID1);
			writeD(playOkID2);
			writeD(loginOkID1);
			writeD(loginOkID2);
		}
	}
}