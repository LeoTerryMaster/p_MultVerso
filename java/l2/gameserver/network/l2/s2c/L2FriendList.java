package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.actor.instances.player.Friend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class L2FriendList extends L2GameServerPacket
{
	private List<FriendInfo> _list = Collections.emptyList();
	
	public L2FriendList(Player player)
	{
		Map<Integer, Friend> list = player.getFriendList().getList();
		_list = new ArrayList<>(list.size());
		for(Map.Entry<Integer, Friend> entry : list.entrySet())
		{
			FriendInfo f = new FriendInfo();
			f._objectId = entry.getKey();
			f._name = entry.getValue().getName();
			f._online = entry.getValue().isOnline();
			_list.add(f);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(250);
		writeD(_list.size());
		for(FriendInfo friendInfo : _list)
		{
			writeD(0);
			writeS(friendInfo._name);
			writeD(friendInfo._online ? 1 : 0);
			writeD(friendInfo._objectId);
		}
	}
	
	private static class FriendInfo
	{
		private int _objectId;
		private String _name;
		private boolean _online;
	}
}