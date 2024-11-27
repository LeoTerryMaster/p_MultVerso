package l2.gameserver.network.l2.c2s;

import l2.gameserver.network.l2.s2c.ExShowAgitInfo;

public class RequestAllAgitInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		getClient().getActiveChar().sendPacket(new ExShowAgitInfo());
	}
}