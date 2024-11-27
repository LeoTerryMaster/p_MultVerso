package l2.authserver.network.l2.c2s;

import l2.authserver.network.l2.L2LoginClient;
import l2.authserver.network.l2.SessionKey;
import l2.authserver.network.l2.s2c.LoginFail;
import l2.authserver.network.l2.s2c.ServerList;

public class RequestServerList extends L2LoginClientPacket
{
	private int _loginOkID1;
	private int _loginOkID2;
	
	@Override
	protected void readImpl()
	{
		_loginOkID1 = readD();
		_loginOkID2 = readD();
		readC();
	}
	
	@Override
	protected void runImpl()
	{
		L2LoginClient client = getClient();
		SessionKey skey = client.getSessionKey();
		if(skey == null || !skey.checkLoginPair(_loginOkID1, _loginOkID2))
		{
			client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
			return;
		}
		client.sendPacket(new ServerList(client.getAccount()));
	}
}