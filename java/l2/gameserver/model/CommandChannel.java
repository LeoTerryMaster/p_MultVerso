package l2.gameserver.model;

import l2.commons.collections.JoinedIterator;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.instances.NpcFriendInstance;
import l2.gameserver.model.matching.MatchingRoom;
import l2.gameserver.network.l2.components.IStaticPacket;
import l2.gameserver.network.l2.s2c.ExMPCCClose;
import l2.gameserver.network.l2.s2c.ExMPCCOpen;
import l2.gameserver.network.l2.s2c.ExMPCCPartyInfoUpdate;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.SystemMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommandChannel implements PlayerGroup
{
	public static final int STRATEGY_GUIDE_ID = 8871;
	public static final int CLAN_IMPERIUM_ID = 391;
	private final List<Party> _commandChannelParties = new CopyOnWriteArrayList<>();
	private Player _commandChannelLeader;
	private int _commandChannelLvl;
	private Reflection _reflection;
	private MatchingRoom _matchingRoom;
	
	public CommandChannel(Player leader)
	{
		_commandChannelLeader = leader;
		_commandChannelParties.add(leader.getParty());
		_commandChannelLvl = leader.getParty().getLevel();
		leader.getParty().setCommandChannel(this);
		broadCast(ExMPCCOpen.STATIC);
	}
	
	public static boolean checkAuthority(Player creator)
	{
		if(creator.getClan() == null || !creator.isInParty() || !creator.getParty().isLeader(creator) || creator.getPledgeClass() < 5)
		{
			creator.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
			return false;
		}
		boolean haveSkill = creator.getSkillLevel(391) > 0;
		boolean haveItem;
		boolean bl = haveItem = creator.getInventory().getItemByItemId(8871) != null;
		if(!haveSkill && !haveItem)
		{
			creator.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
			return false;
		}
		return true;
	}
	
	public void addParty(Party party)
	{
		broadCast(new ExMPCCPartyInfoUpdate(party, 1));
		_commandChannelParties.add(party);
		refreshLevel();
		party.setCommandChannel(this);
		for(Player member : party)
		{
			member.sendPacket(ExMPCCOpen.STATIC);
			if(_matchingRoom == null)
				continue;
			_matchingRoom.broadcastPlayerUpdate(member);
		}
	}
	
	public void removeParty(Party party)
	{
		_commandChannelParties.remove(party);
		refreshLevel();
		party.setCommandChannel(null);
		party.broadCast(ExMPCCClose.STATIC);
		Reflection reflection = getReflection();
		if(reflection != null)
		{
			for(Player player : party.getPartyMembers())
			{
				player.teleToLocation(reflection.getReturnLoc(), 0);
			}
		}
		if(_commandChannelParties.size() < 2)
		{
			disbandChannel();
		}
		else
		{
			for(Player member : party)
			{
				member.sendPacket(new ExMPCCPartyInfoUpdate(party, 0));
				if(_matchingRoom == null)
					continue;
				_matchingRoom.broadcastPlayerUpdate(member);
			}
		}
	}
	
	public void disbandChannel()
	{
		broadCast(Msg.THE_COMMAND_CHANNEL_HAS_BEEN_DISBANDED);
		for(Party party : _commandChannelParties)
		{
			party.setCommandChannel(null);
			party.broadCast(ExMPCCClose.STATIC);
			if(!isInReflection())
				continue;
			party.broadCast(new SystemMessage(2106).addNumber(1));
		}
		Reflection reflection = getReflection();
		if(reflection != null)
		{
			reflection.startCollapseTimer(60000);
			setReflection(null);
		}
		if(_matchingRoom != null)
		{
			_matchingRoom.disband();
		}
		_commandChannelParties.clear();
		_commandChannelLeader = null;
	}
	
	public int getMemberCount()
	{
		int count = 0;
		for(Party party : _commandChannelParties)
		{
			count += party.getMemberCount();
		}
		return count;
	}
	
	@Override
	public void broadCast(IStaticPacket... gsp)
	{
		for(Party party : _commandChannelParties)
		{
			party.broadCast(gsp);
		}
	}
	
	public void broadcastToChannelPartyLeaders(L2GameServerPacket gsp)
	{
		for(Party party : _commandChannelParties)
		{
			Player leader = party.getPartyLeader();
			if(leader == null)
				continue;
			leader.sendPacket(gsp);
		}
	}
	
	public List<Party> getParties()
	{
		return _commandChannelParties;
	}
	
	@Deprecated
	public List<Player> getMembers()
	{
		ArrayList<Player> members = new ArrayList<>(_commandChannelParties.size());
		for(Party party : getParties())
		{
			members.addAll(party.getPartyMembers());
		}
		return members;
	}
	
	@Override
	public Iterator<Player> iterator()
	{
		ArrayList<Iterator<Player>> iterators = new ArrayList<>(_commandChannelParties.size());
		for(Party p : getParties())
		{
			iterators.add(p.getPartyMembers().iterator());
		}
		return new JoinedIterator<>(iterators);
	}
	
	public int getLevel()
	{
		return _commandChannelLvl;
	}
	
	public Player getChannelLeader()
	{
		return _commandChannelLeader;
	}
	
	public void setChannelLeader(Player newLeader)
	{
		_commandChannelLeader = newLeader;
		broadCast(new SystemMessage(1589).addString(newLeader.getName()));
	}
	
	public boolean meetRaidWarCondition(NpcFriendInstance npc)
	{
		if(!npc.isRaid())
		{
			return false;
		}
		int npcId = npc.getNpcId();
		switch(npcId)
		{
			case 29001:
			case 29006:
			case 29014:
			case 29022:
			{
				return getMemberCount() > 36;
			}
			case 29020:
			{
				return getMemberCount() > 56;
			}
			case 29019:
			{
				return getMemberCount() > 225;
			}
			case 29028:
			{
				return getMemberCount() > 99;
			}
		}
		return getMemberCount() > 18;
	}
	
	private void refreshLevel()
	{
		_commandChannelLvl = 0;
		for(Party pty : _commandChannelParties)
		{
			if(pty.getLevel() <= _commandChannelLvl)
				continue;
			_commandChannelLvl = pty.getLevel();
		}
	}
	
	public boolean isInReflection()
	{
		return _reflection != null;
	}
	
	public Reflection getReflection()
	{
		return _reflection;
	}
	
	public void setReflection(Reflection reflection)
	{
		_reflection = reflection;
	}
	
	public MatchingRoom getMatchingRoom()
	{
		return _matchingRoom;
	}
	
	public void setMatchingRoom(MatchingRoom matchingRoom)
	{
		_matchingRoom = matchingRoom;
	}
}