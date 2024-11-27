package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.model.Request;
import l2.gameserver.model.matching.MatchingRoom;
import l2.gameserver.network.l2.components.SystemMsg;

public class AnswerJoinPartyRoom extends L2GameClientPacket
{
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = _buf.hasRemaining() ? readD() : 0;
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		Request request = activeChar.getRequest();
		if(request == null || !request.isTypeOf(Request.L2RequestType.PARTY_ROOM))
		{
			return;
		}
		if(!request.isInProgress())
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isOutOfControl())
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}
		Player requestor = request.getRequestor();
		if(requestor == null)
		{
			request.cancel();
			activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
			activeChar.sendActionFailed();
			return;
		}
		if(requestor.getRequest() != request)
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}
		if(_response == 0)
		{
			request.cancel();
			requestor.sendPacket(SystemMsg.THE_PLAYER_DECLINED_TO_JOIN_YOUR_PARTY);
			return;
		}
		if(activeChar.getMatchingRoom() != null)
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}
		try
		{
			MatchingRoom room = requestor.getMatchingRoom();
			if(room == null || room.getType() != MatchingRoom.PARTY_MATCHING)
			{
				return;
			}
			room.addMember(activeChar);
		}
		finally
		{
			request.done();
		}
	}
}