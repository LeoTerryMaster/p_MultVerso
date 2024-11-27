package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.actor.instances.player.Friend;
import l2.gameserver.network.l2.s2c.SystemMessage;

import java.util.Map;

public class RequestFriendList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		activeChar.sendPacket(Msg._FRIENDS_LIST_);
		Map<Integer, Friend> _list = activeChar.getFriendList().getList();
		for(Map.Entry<Integer, Friend> entry : _list.entrySet())
		{
			Player friend = World.getPlayer(entry.getKey());
			if(friend != null)
			{
				activeChar.sendPacket(new SystemMessage(488).addName(friend));
				continue;
			}
			activeChar.sendPacket(new SystemMessage(489).addString(entry.getValue().getName()));
		}
		activeChar.sendPacket(Msg.__EQUALS__);
	}
}