package l2.gameserver.network.authcomm.as2gs;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.network.authcomm.AuthServerCommunication;
import l2.gameserver.network.authcomm.ReceivablePacket;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.s2c.ServerClose;

public class KickPlayer extends ReceivablePacket
{
	String account;
	
	@Override
	public void readImpl()
	{
		account = readS();
	}
	
	@Override
	protected void runImpl()
	{
		GameClient client = AuthServerCommunication.getInstance().removeWaitingClient(account);
		if(client == null)
		{
			client = AuthServerCommunication.getInstance().removeAuthedClient(account);
		}
		if(client == null)
		{
			return;
		}
		Player activeChar = client.getActiveChar();
		if(activeChar != null)
		{
			activeChar.sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
			activeChar.kick();
		}
		else
		{
			client.close(ServerClose.STATIC);
		}
	}
}