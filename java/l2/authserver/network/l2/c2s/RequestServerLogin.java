package l2.authserver.network.l2.c2s;

import l2.authserver.GameServerManager;
import l2.authserver.accounts.Account;
import l2.authserver.network.gamecomm.GameServer;
import l2.authserver.network.gamecomm.ProxyServer;
import l2.authserver.network.l2.L2LoginClient;
import l2.authserver.network.l2.SessionKey;
import l2.authserver.network.l2.s2c.LoginFail;
import l2.authserver.network.l2.s2c.PlayOk;

public class RequestServerLogin extends L2LoginClientPacket
{
	private int _loginOkID1;
	private int _loginOkID2;
	private int _serverId;
	
	@Override
	protected void readImpl()
	{
		_loginOkID1 = readD();
		_loginOkID2 = readD();
		_serverId = readC();
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
		Account account = client.getAccount();
		GameServer gs = GameServerManager.getInstance().getGameServerById(_serverId);
		ProxyServer ps;
		if(gs == null && (ps = GameServerManager.getInstance().getProxyServerById(_serverId)) != null)
		{
			gs = GameServerManager.getInstance().getGameServerById(ps.getOrigServerId());
		}
		if(gs == null || !gs.isAuthed() || gs.getOnline() >= gs.getMaxPlayers() && account.getAccessLevel() < 50)
		{
			client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
			return;
		}
		if(gs.isGmOnly() && account.getAccessLevel() < 100)
		{
			client.close(LoginFail.LoginFailReason.REASON_SERVER_MAINTENANCE);
			return;
		}
		account.setLastServer(_serverId);
		account.update();
		client.close(new PlayOk(skey));
	}
}