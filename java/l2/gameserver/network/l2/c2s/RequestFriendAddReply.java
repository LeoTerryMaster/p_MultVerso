package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.Request;
import l2.gameserver.network.l2.s2c.L2Friend;
import l2.gameserver.network.l2.s2c.SystemMessage;

public class RequestFriendAddReply extends L2GameClientPacket
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
		if(request == null || !request.isTypeOf(Request.L2RequestType.FRIEND))
		{
			return;
		}
		if(activeChar.isOutOfControl())
		{
			request.cancel();
			activeChar.sendActionFailed();
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
			activeChar.sendPacket(Msg.THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME);
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
			requestor.sendPacket(Msg.YOU_HAVE_FAILED_TO_INVITE_A_FRIEND);
			activeChar.sendActionFailed();
			return;
		}
		requestor.getFriendList().addFriend(activeChar);
		activeChar.getFriendList().addFriend(requestor);
		requestor.sendPacket(Msg.YOU_HAVE_SUCCEEDED_IN_INVITING_A_FRIEND, new SystemMessage(132).addString(activeChar.getName()), new L2Friend(activeChar, true));
		activeChar.sendPacket(new SystemMessage(479).addString(requestor.getName()), new L2Friend(requestor, true));
	}
}