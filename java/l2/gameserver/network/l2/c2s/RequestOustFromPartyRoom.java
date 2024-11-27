package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.matching.MatchingRoom;

public class RequestOustFromPartyRoom extends L2GameClientPacket
{
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		MatchingRoom room = player.getMatchingRoom();
		if(room == null || room.getType() != MatchingRoom.PARTY_MATCHING)
		{
			return;
		}
		if(room.getLeader() != player)
		{
			return;
		}
		Player member = GameObjectsStorage.getPlayer(_objectId);
		if(member == null)
		{
			return;
		}
		if(member == room.getLeader())
		{
			return;
		}
		room.removeMember(member, true);
	}
}