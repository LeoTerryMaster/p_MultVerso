package l2.gameserver.network.authcomm.gs2as;

import l2.gameserver.network.authcomm.SendablePacket;

public class PingResponse extends SendablePacket
{
	@Override
	protected void writeImpl()
	{
		writeC(255);
		writeQ(System.currentTimeMillis());
	}
}