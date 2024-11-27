package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.model.matching.MatchingRoom;
import l2.gameserver.network.l2.s2c.ExMpccPartymasterList;

import java.util.HashSet;

public class RequestExMpccPartymasterList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		MatchingRoom room = player.getMatchingRoom();
		if(room == null || room.getType() != MatchingRoom.CC_MATCHING)
		{
			return;
		}
		HashSet<String> set = new HashSet<>();
		for(Player member : room.getPlayers())
		{
			if(member.getParty() == null)
				continue;
			set.add(member.getParty().getPartyLeader().getName());
		}
		player.sendPacket(new ExMpccPartymasterList(set));
	}
}