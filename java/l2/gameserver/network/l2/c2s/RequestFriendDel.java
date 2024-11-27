package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;

public class RequestFriendDel extends L2GameClientPacket
{
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS(16);
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		player.getFriendList().removeFriend(_name);
	}
}