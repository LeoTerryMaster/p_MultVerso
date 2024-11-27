package l2.authserver.network.l2;

import l2.authserver.network.l2.c2s.AuthGameGuard;
import l2.authserver.network.l2.c2s.L2LoginClientPacket;
import l2.authserver.network.l2.c2s.RequestAuthLogin;
import l2.authserver.network.l2.c2s.RequestServerList;
import l2.authserver.network.l2.c2s.RequestServerLogin;
import l2.commons.net.nio.impl.IPacketHandler;
import l2.commons.net.nio.impl.ReceivablePacket;

import java.nio.ByteBuffer;

public final class L2LoginPacketHandler implements IPacketHandler<L2LoginClient>
{
	@Override
	public ReceivablePacket<L2LoginClient> handlePacket(ByteBuffer buf, L2LoginClient client)
	{
		int opcode = buf.get() & 255;
		L2LoginClientPacket packet = null;
		L2LoginClient.LoginClientState state = client.getState();
		switch(state)
		{
			case CONNECTED:
			{
				if(opcode != 7)
					break;
				packet = new AuthGameGuard();
				break;
			}
			case AUTHED_GG:
			{
				if(opcode != 0)
					break;
				packet = new RequestAuthLogin();
				break;
			}
			case AUTHED:
			{
				if(opcode == 5)
				{
					packet = new RequestServerList();
					break;
				}
				if(opcode != 2)
					break;
				packet = new RequestServerLogin();
			}
		}
		return packet;
	}
}