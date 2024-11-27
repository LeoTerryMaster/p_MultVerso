package l2.gameserver.model.matching;

import l2.gameserver.instancemanager.MatchingRoomManager;
import l2.gameserver.listener.actor.player.OnPlayerPartyInviteListener;
import l2.gameserver.listener.actor.player.OnPlayerPartyLeaveListener;
import l2.gameserver.model.Player;
import l2.gameserver.model.PlayerGroup;
import l2.gameserver.network.l2.components.IStaticPacket;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.SystemMessage2;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class MatchingRoom implements PlayerGroup
{
	public static int PARTY_MATCHING;
	public static int CC_MATCHING = 1;
	public static int WAIT_PLAYER;
	public static int ROOM_MASTER = 1;
	public static int PARTY_MEMBER = 2;
	public static int UNION_LEADER = 3;
	public static int UNION_PARTY = 4;
	public static int WAIT_PARTY = 5;
	public static int WAIT_NORMAL = 6;
	protected final Player _leader;
	private final int _id;
	private final PartyListenerImpl _listener;
	protected Set<Player> _members;
	private int _minLevel;
	private int _maxLevel;
	private int _maxMemberSize;
	private int _lootType;
	private String _topic;
	
	public MatchingRoom(Player leader, int minLevel, int maxLevel, int maxMemberSize, int lootType, String topic)
	{
		_listener = new PartyListenerImpl();
		_members = new CopyOnWriteArraySet<>();
		_leader = leader;
		_id = MatchingRoomManager.getInstance().addMatchingRoom(this);
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_maxMemberSize = maxMemberSize;
		_lootType = lootType;
		_topic = topic;
		addMember0(leader, null);
	}
	
	public boolean addMember(Player player)
	{
		if(_members.contains(player))
		{
			return true;
		}
		if(player.getLevel() < getMinLevel() || player.getLevel() > getMaxLevel() || getPlayers().size() >= getMaxMembersSize())
		{
			player.sendPacket(notValidMessage());
			return false;
		}
		return addMember0(player, new SystemMessage2(enterMessage()).addName(player));
	}
	
	private boolean addMember0(Player player, L2GameServerPacket p)
	{
		if(!_members.isEmpty())
		{
			player.addListener(_listener);
		}
		_members.add(player);
		player.setMatchingRoom(this);
		for(Player member : this)
		{
			if(member == player)
				continue;
			member.sendPacket(p, addMemberPacket(member, player));
		}
		MatchingRoomManager.getInstance().removeFromWaitingList(player);
		player.sendPacket(infoRoomPacket(), membersPacket(player));
		player.sendChanges();
		return true;
	}
	
	public void removeMember(Player member, boolean oust)
	{
		if(!_members.remove(member))
		{
			return;
		}
		member.removeListener(_listener);
		member.setMatchingRoom(null);
		if(_members.isEmpty())
		{
			disband();
		}
		else
		{
			L2GameServerPacket infoPacket = infoRoomPacket();
			SystemMsg exitMessage0 = exitMessage(true, oust);
			SystemMessage2 exitMessage = exitMessage0 != null ? new SystemMessage2(exitMessage0).addName(member) : null;
			for(Player player : this)
			{
				player.sendPacket(infoPacket, removeMemberPacket(player, member), exitMessage);
			}
		}
		member.sendPacket(closeRoomPacket(), exitMessage(false, oust));
		MatchingRoomManager.getInstance().addToWaitingList(member);
		member.sendChanges();
	}
	
	public void broadcastPlayerUpdate(Player player)
	{
		for(Player member : this)
		{
			member.sendPacket(updateMemberPacket(member, player));
		}
	}
	
	public void disband()
	{
		for(Player player : this)
		{
			player.removeListener(_listener);
			player.sendPacket(closeRoomMessage());
			player.sendPacket(closeRoomPacket());
			player.setMatchingRoom(null);
			player.sendChanges();
			MatchingRoomManager.getInstance().addToWaitingList(player);
		}
		_members.clear();
		MatchingRoomManager.getInstance().removeMatchingRoom(this);
	}
	
	public abstract SystemMsg notValidMessage();
	
	public abstract SystemMsg enterMessage();
	
	public abstract SystemMsg exitMessage(boolean toOthers, boolean kick);
	
	public abstract SystemMsg closeRoomMessage();
	
	public abstract L2GameServerPacket closeRoomPacket();
	
	public abstract L2GameServerPacket infoRoomPacket();
	
	public abstract L2GameServerPacket addMemberPacket(Player member, Player active);
	
	public abstract L2GameServerPacket removeMemberPacket(Player member, Player active);
	
	public abstract L2GameServerPacket updateMemberPacket(Player member, Player active);
	
	public abstract L2GameServerPacket membersPacket(Player active);
	
	public abstract int getType();
	
	public abstract int getMemberType(Player member);
	
	@Override
	public void broadCast(IStaticPacket... arg)
	{
		for(Player player : this)
		{
			player.sendPacket(arg);
		}
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getMinLevel()
	{
		return _minLevel;
	}
	
	public void setMinLevel(int minLevel)
	{
		_minLevel = minLevel;
	}
	
	public int getMaxLevel()
	{
		return _maxLevel;
	}
	
	public void setMaxLevel(int maxLevel)
	{
		_maxLevel = maxLevel;
	}
	
	public String getTopic()
	{
		return _topic;
	}
	
	public void setTopic(String topic)
	{
		_topic = topic;
	}
	
	public int getMaxMembersSize()
	{
		return _maxMemberSize;
	}
	
	public int getLocationId()
	{
		return MatchingRoomManager.getInstance().getLocation(_leader);
	}
	
	public Player getLeader()
	{
		return _leader;
	}
	
	public Collection<Player> getPlayers()
	{
		return _members;
	}
	
	public int getLootType()
	{
		return _lootType;
	}
	
	public void setLootType(int lootType)
	{
		_lootType = lootType;
	}
	
	@Override
	public Iterator<Player> iterator()
	{
		return _members.iterator();
	}
	
	public void setMaxMemberSize(int maxMemberSize)
	{
		_maxMemberSize = maxMemberSize;
	}
	
	private class PartyListenerImpl implements OnPlayerPartyInviteListener, OnPlayerPartyLeaveListener
	{
		@Override
		public void onPartyInvite(Player player)
		{
			broadcastPlayerUpdate(player);
		}
		
		@Override
		public void onPartyLeave(Player player)
		{
			broadcastPlayerUpdate(player);
		}
	}
}