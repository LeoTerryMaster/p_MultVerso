package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.model.matching.MatchingRoom;

public class RequestDismissPartyRoom extends L2GameClientPacket
{
	private int _roomId;
	
	@Override
	protected void readImpl()
	{
		_roomId = readD();
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
		if(room.getId() != _roomId || room.getType() != MatchingRoom.PARTY_MATCHING)
		{
			return;
		}
		if(room.getLeader() != player)
		{
			return;
		}
		room.disband();
	}
}