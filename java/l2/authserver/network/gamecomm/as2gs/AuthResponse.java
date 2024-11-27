package l2.authserver.network.gamecomm.as2gs;

import l2.authserver.network.gamecomm.GameServer;
import l2.authserver.network.gamecomm.SendablePacket;

public class AuthResponse extends SendablePacket
{
	private final int serverId;
	private final String name;
	
	public AuthResponse(GameServer gs)
	{
		serverId = gs.getId();
		name = gs.getName();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0);
		writeC(serverId);
		writeS(name);
	}
}