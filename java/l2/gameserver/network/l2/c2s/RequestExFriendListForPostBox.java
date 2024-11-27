package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.FriendList;

public class RequestExFriendListForPostBox extends L2GameClientPacket
{
	@Override
	protected void readImpl() throws Exception
	{
	}
	
	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		player.sendPacket(new FriendList(player));
	}
}