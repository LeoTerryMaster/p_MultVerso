package l2.authserver.network.gamecomm.gs2as;

import l2.authserver.network.gamecomm.GameServer;
import l2.authserver.network.gamecomm.ReceivablePacket;

public class PlayerInGame extends ReceivablePacket
{
	private String account;
	
	@Override
	protected void readImpl()
	{
		account = readS();
	}
	
	@Override
	protected void runImpl()
	{
		GameServer gs = getGameServer();
		if(gs.isAuthed())
		{
			gs.addAccount(account);
		}
	}
}