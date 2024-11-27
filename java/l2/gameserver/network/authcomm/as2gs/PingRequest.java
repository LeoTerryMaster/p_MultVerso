package l2.gameserver.network.authcomm.as2gs;

import l2.gameserver.network.authcomm.AuthServerCommunication;
import l2.gameserver.network.authcomm.ReceivablePacket;
import l2.gameserver.network.authcomm.gs2as.PingResponse;

public class PingRequest extends ReceivablePacket
{
	@Override
	public void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		AuthServerCommunication.getInstance().sendPacket(new PingResponse());
	}
}