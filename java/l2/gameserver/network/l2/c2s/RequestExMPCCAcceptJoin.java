package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.CommandChannel;
import l2.gameserver.model.Player;
import l2.gameserver.model.Request;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;

public class RequestExMPCCAcceptJoin extends L2GameClientPacket
{
	private int _response;
	private int _unk;
	
	@Override
	protected void readImpl()
	{
		_response = _buf.hasRemaining() ? readD() : 0;
		_unk = _buf.hasRemaining() ? readD() : 0;
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
		if(request == null || !request.isTypeOf(Request.L2RequestType.CHANNEL))
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
			activeChar.sendPacket(Msg.THAT_PLAYER_IS_NOT_ONLINE);
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
			requestor.sendPacket(new SystemMessage(1680).addString(activeChar.getName()));
			return;
		}
		if(!requestor.isInParty() || !activeChar.isInParty() || activeChar.getParty().isInCommandChannel())
		{
			request.cancel();
			requestor.sendPacket(Msg.NO_USER_HAS_BEEN_INVITED_TO_THE_COMMAND_CHANNEL);
			return;
		}
		if(activeChar.isTeleporting())
		{
			request.cancel();
			activeChar.sendPacket(Msg.YOU_CANNOT_JOIN_A_COMMAND_CHANNEL_WHILE_TELEPORTING);
			requestor.sendPacket(Msg.NO_USER_HAS_BEEN_INVITED_TO_THE_COMMAND_CHANNEL);
			return;
		}
		try
		{
			if(requestor.getParty().isInCommandChannel())
			{
				requestor.getParty().getCommandChannel().addParty(activeChar.getParty());
			}
			else if(CommandChannel.checkAuthority(requestor))
			{
				boolean haveSkill = requestor.getSkillLevel(391) > 0;
				boolean haveItem = false;
				if(!haveSkill && (haveItem = requestor.getInventory().destroyItemByItemId(8871, 1)))
				{
					requestor.sendPacket(SystemMessage2.removeItems(8871, 1));
				}
				if(!haveSkill && !haveItem)
				{
					return;
				}
				CommandChannel channel = new CommandChannel(requestor);
				requestor.sendPacket(Msg.THE_COMMAND_CHANNEL_HAS_BEEN_FORMED);
				channel.addParty(activeChar.getParty());
			}
		}
		finally
		{
			request.done();
		}
	}
}