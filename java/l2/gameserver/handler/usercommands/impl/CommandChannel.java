package l2.gameserver.handler.usercommands.impl;

import l2.gameserver.cache.Msg;
import l2.gameserver.handler.usercommands.IUserCommandHandler;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.ExMultiPartyCommandChannelInfo;
import l2.gameserver.network.l2.s2c.SystemMessage;

public class CommandChannel implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = {92, 93, 96, 97};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(id != COMMAND_IDS[0] && id != COMMAND_IDS[1] && id != COMMAND_IDS[2] && id != COMMAND_IDS[3])
		{
			return false;
		}
		switch(id)
		{
			case 92:
			{
				activeChar.sendMessage(new CustomMessage("usercommandhandlers.CommandChannel", activeChar));
				break;
			}
			case 93:
			{
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					return true;
				}
				if(activeChar.getParty().getCommandChannel().getChannelLeader() == activeChar)
				{
					l2.gameserver.model.CommandChannel channel = activeChar.getParty().getCommandChannel();
					channel.disbandChannel();
					break;
				}
				activeChar.sendPacket(Msg.ONLY_THE_CREATOR_OF_A_CHANNEL_CAN_USE_THE_CHANNEL_DISMISS_COMMAND);
				break;
			}
			case 96:
			{
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					return true;
				}
				if(!activeChar.getParty().isLeader(activeChar))
				{
					activeChar.sendPacket(Msg.ONLY_A_PARTY_LEADER_CAN_CHOOSE_THE_OPTION_TO_LEAVE_A_CHANNEL);
					return true;
				}
				l2.gameserver.model.CommandChannel channel = activeChar.getParty().getCommandChannel();
				if(channel.getChannelLeader() == activeChar)
				{
					if(channel.getParties().size() > 1)
					{
						return false;
					}
					channel.disbandChannel();
					return true;
				}
				Party party = activeChar.getParty();
				channel.removeParty(party);
				party.broadCast(Msg.YOU_HAVE_QUIT_THE_COMMAND_CHANNEL);
				channel.broadCast(new SystemMessage(1587).addString(activeChar.getName()));
				break;
			}
			case 97:
			{
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					return false;
				}
				activeChar.sendPacket(new ExMultiPartyCommandChannelInfo(activeChar.getParty().getCommandChannel()));
			}
		}
		return true;
	}
	
	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}