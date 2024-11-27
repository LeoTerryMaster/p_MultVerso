package l2.gameserver.network.authcomm.as2gs;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.network.authcomm.AuthServerCommunication;
import l2.gameserver.network.authcomm.ReceivablePacket;
import l2.gameserver.network.authcomm.SessionKey;
import l2.gameserver.network.authcomm.gs2as.PlayerInGame;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.s2c.CharacterSelectionInfo;
import l2.gameserver.network.l2.s2c.LoginFail;
import l2.gameserver.network.l2.s2c.ServerClose;

public class PlayerAuthResponse extends ReceivablePacket
{
	private String account;
	private boolean authed;
	private int playOkId1;
	private int playOkId2;
	private int loginOkId1;
	private int loginOkId2;
	
	@Override
	public void readImpl()
	{
		account = readS();
		boolean bl = authed = readC() == 1;
		if(authed)
		{
			playOkId1 = readD();
			playOkId2 = readD();
			loginOkId1 = readD();
			loginOkId2 = readD();
		}
	}
	
	@Override
	protected void runImpl()
	{
		SessionKey skey = new SessionKey(loginOkId1, loginOkId2, playOkId1, playOkId2);
		GameClient client = AuthServerCommunication.getInstance().removeWaitingClient(account);
		if(client == null)
		{
			return;
		}
		if(authed && client.getSessionKey().equals(skey))
		{
			client.setAuthed(true);
			client.setState(GameClient.GameClientState.AUTHED);
			GameClient oldClient = AuthServerCommunication.getInstance().addAuthedClient(client);
			if(!Config.ALLOW_MULILOGIN && oldClient != null)
			{
				oldClient.setAuthed(false);
				Player activeChar = oldClient.getActiveChar();
				if(activeChar != null)
				{
					activeChar.sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
					activeChar.logout();
				}
				else
				{
					oldClient.close(ServerClose.STATIC);
				}
			}
			sendPacket(new PlayerInGame(client.getLogin()));
			CharacterSelectionInfo csi = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
			client.sendPacket(csi);
			client.setCharSelection(csi.getCharInfo());
		}
		else
		{
			client.close(new LoginFail(LoginFail.ACCESS_FAILED_TRY_LATER));
		}
	}
}