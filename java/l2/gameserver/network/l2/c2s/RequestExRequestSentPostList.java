package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.ExShowSentPostList;

public class RequestExRequestSentPostList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		Player cha = getClient().getActiveChar();
		if(cha != null)
		{
			cha.sendPacket(new ExShowSentPostList(cha));
		}
	}
}