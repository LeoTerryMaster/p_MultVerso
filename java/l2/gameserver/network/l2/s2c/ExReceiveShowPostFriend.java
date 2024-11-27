package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import org.napile.primitive.maps.IntObjectMap;

public class ExReceiveShowPostFriend extends L2GameServerPacket
{
	private final IntObjectMap<String> _list;
	
	public ExReceiveShowPostFriend(Player player)
	{
		_list = player.getPostFriends();
	}
	
	@Override
	public void writeImpl()
	{
		writeEx(211);
		writeD(_list.size());
		for(String t : _list.values())
		{
			writeS(t);
		}
	}
}