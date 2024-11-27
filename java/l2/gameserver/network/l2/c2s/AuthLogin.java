package l2.gameserver.network.l2.c2s;

import l2.gameserver.Shutdown;
import l2.gameserver.network.authcomm.AuthServerCommunication;
import l2.gameserver.network.authcomm.SessionKey;
import l2.gameserver.network.authcomm.gs2as.PlayerAuthRequest;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.s2c.LoginFail;
import l2.gameserver.network.l2.s2c.ServerClose;

public class AuthLogin extends L2GameClientPacket
{
	private String _loginName;
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;
	private int _languageType;
	
	@Override
	protected void readImpl()
	{
		_loginName = readS(32).toLowerCase();
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
		_languageType = readD();
	}
	
	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);
		client.setSessionId(key);
		client.setLoginName(_loginName);
		if(client.getRevision() == 0 || Shutdown.getInstance().getMode() != -1 && Shutdown.getInstance().getSeconds() <= 15)
		{
			client.closeNow(false);
		}
		else
		{
			if(AuthServerCommunication.getInstance().isShutdown())
			{
				client.close(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
				return;
			}
			GameClient oldClient = AuthServerCommunication.getInstance().addWaitingClient(client);
			if(oldClient != null)
			{
				oldClient.close(ServerClose.STATIC);
			}
			AuthServerCommunication.getInstance().sendPacket(new PlayerAuthRequest(client));
		}
	}
}