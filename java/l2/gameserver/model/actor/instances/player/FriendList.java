package l2.gameserver.model.actor.instances.player;

import l2.gameserver.dao.CharacterFriendDAO;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.network.l2.components.IStaticPacket;
import l2.gameserver.network.l2.s2c.L2Friend;
import l2.gameserver.network.l2.s2c.L2FriendStatus;
import l2.gameserver.network.l2.s2c.SystemMessage;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;

public class FriendList
{
	private final Player _owner;
	private Map<Integer, Friend> _friendList = Collections.emptyMap();
	
	public FriendList(Player owner)
	{
		_owner = owner;
	}
	
	public void restore()
	{
		_friendList = CharacterFriendDAO.getInstance().select(_owner);
	}
	
	public void removeFriend(String name)
	{
		if(StringUtils.isEmpty(name))
		{
			return;
		}
		int objectId = removeFriend0(name);
		if(objectId > 0)
		{
			Player friendChar = World.getPlayer(objectId);
			IStaticPacket[] arriStaticPacket = new IStaticPacket[2];
			arriStaticPacket[0] = new SystemMessage(133).addString(name);
			arriStaticPacket[1] = new L2Friend(name, false, friendChar != null, objectId);
			_owner.sendPacket(arriStaticPacket);
			if(friendChar != null)
			{
				friendChar.sendPacket(new SystemMessage(481).addString(_owner.getName()), new L2Friend(_owner, false));
			}
		}
		else
		{
			_owner.sendPacket(new SystemMessage(171).addString(name));
		}
	}
	
	public void notifyFriends(boolean login)
	{
		try
		{
			for(Friend friend : _friendList.values())
			{
				Friend thisFriend;
				Player friendPlayer = GameObjectsStorage.getPlayer(friend.getObjectId());
				if(friendPlayer == null || (thisFriend = friendPlayer.getFriendList().getList().get(_owner.getObjectId())) == null)
					continue;
				thisFriend.update(_owner, login);
				if(login)
				{
					friendPlayer.sendPacket(new SystemMessage(503).addString(_owner.getName()));
				}
				friendPlayer.sendPacket(new L2FriendStatus(_owner, login));
				friend.update(friendPlayer, login);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void addFriend(Player friendPlayer)
	{
		_friendList.put(friendPlayer.getObjectId(), new Friend(friendPlayer));
		CharacterFriendDAO.getInstance().insert(_owner, friendPlayer);
	}
	
	private int removeFriend0(String name)
	{
		if(name == null)
		{
			return 0;
		}
		Integer objectId = 0;
		for(Map.Entry<Integer, Friend> entry : _friendList.entrySet())
		{
			if(!name.equalsIgnoreCase(entry.getValue().getName()))
				continue;
			objectId = entry.getKey();
			break;
		}
		if(objectId > 0)
		{
			_friendList.remove(objectId);
			CharacterFriendDAO.getInstance().delete(_owner, objectId);
			return objectId;
		}
		return 0;
	}
	
	public Map<Integer, Friend> getList()
	{
		return _friendList;
	}
	
	@Override
	public String toString()
	{
		return "FriendList[owner=" + _owner.getName() + "]";
	}
}