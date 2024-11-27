package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.Request;
import l2.gameserver.network.l2.components.IStaticPacket;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ActionFail;
import l2.gameserver.network.l2.s2c.JoinParty;

public class RequestAnswerJoinParty extends L2GameClientPacket
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
		if(request == null || !request.isTypeOf(Request.L2RequestType.PARTY))
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
		if(_response <= 0)
		{
			request.cancel();
			requestor.sendPacket(JoinParty.FAIL);
			return;
		}
		if(activeChar.isOlyParticipant())
		{
			request.cancel();
			activeChar.sendPacket(SystemMsg.A_PARTY_CANNOT_BE_FORMED_IN_THIS_AREA);
			requestor.sendPacket(JoinParty.FAIL);
			return;
		}
		if(requestor.isOlyParticipant())
		{
			request.cancel();
			requestor.sendPacket(JoinParty.FAIL);
			return;
		}
		Party party = requestor.getParty();
		if(party != null && party.getMemberCount() >= 9)
		{
			request.cancel();
			activeChar.sendPacket(SystemMsg.THE_PARTY_IS_FULL);
			requestor.sendPacket(SystemMsg.THE_PARTY_IS_FULL);
			requestor.sendPacket(JoinParty.FAIL);
			return;
		}
		IStaticPacket problem = activeChar.canJoinParty(requestor);
		if(problem != null)
		{
			request.cancel();
			activeChar.sendPacket(problem, ActionFail.STATIC);
			requestor.sendPacket(JoinParty.FAIL);
			return;
		}
		if(party == null)
		{
			int itemDistribution = request.getInteger("itemDistribution");
			party = new Party(requestor, itemDistribution);
			requestor.setParty(party);
		}
		try
		{
			activeChar.joinParty(party);
			requestor.sendPacket(JoinParty.SUCCESS);
		}
		finally
		{
			request.done();
		}
	}
}