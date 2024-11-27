package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.CommandChannel;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.Request;
import l2.gameserver.model.World;
import l2.gameserver.network.l2.s2c.ExAskJoinMPCC;
import l2.gameserver.network.l2.s2c.SystemMessage;

public class RequestExMPCCAskJoin extends L2GameClientPacket
{
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS(16);
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(Msg.WAITING_FOR_ANOTHER_REPLY);
			return;
		}
		if(!activeChar.isInParty())
		{
			activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL);
			return;
		}
		Player target = World.getPlayer(_name);
		if(target == null)
		{
			activeChar.sendPacket(Msg.THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE);
			return;
		}
		if(activeChar == target || !target.isInParty() || activeChar.getParty() == target.getParty())
		{
			activeChar.sendPacket(Msg.YOU_HAVE_INVITED_WRONG_TARGET);
			return;
		}
		if(target.isInParty() && !target.getParty().isLeader(target))
		{
			target = target.getParty().getPartyLeader();
		}
		if(target == null)
		{
			activeChar.sendPacket(Msg.THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE);
			return;
		}
		if(target.getParty().isInCommandChannel())
		{
			activeChar.sendPacket(new SystemMessage(1594).addString(target.getName()));
			return;
		}
		if(target.isBusy())
		{
			activeChar.sendPacket(new SystemMessage(153).addString(target.getName()));
			return;
		}
		Party activeParty = activeChar.getParty();
		if(activeParty.isInCommandChannel())
		{
			if(activeParty.getCommandChannel().getChannelLeader() != activeChar)
			{
				activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL);
				return;
			}
			sendInvite(activeChar, target);
		}
		else if(CommandChannel.checkAuthority(activeChar))
		{
			sendInvite(activeChar, target);
		}
	}
	
	private void sendInvite(Player requestor, Player target)
	{
		new Request(Request.L2RequestType.CHANNEL, requestor, target).setTimeout(10000);
		target.sendPacket(new ExAskJoinMPCC(requestor.getName()));
		requestor.sendMessage("You invited " + target.getName() + " to your Command Channel.");
	}
}