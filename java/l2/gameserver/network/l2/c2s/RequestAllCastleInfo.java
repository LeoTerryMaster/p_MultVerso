package l2.gameserver.network.l2.c2s;

import l2.gameserver.network.l2.s2c.ExShowCastleInfo;

public class RequestAllCastleInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		getClient().getActiveChar().sendPacket(new ExShowCastleInfo());
	}
}