package l2.gameserver.handler.usercommands.impl;

import l2.gameserver.cache.Msg;
import l2.gameserver.handler.usercommands.IUserCommandHandler;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.SystemMessage;

public class PartyInfo implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = {81};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(id != COMMAND_IDS[0])
		{
			return false;
		}
		Party playerParty = activeChar.getParty();
		if(!activeChar.isInParty())
		{
			return false;
		}
		Player partyLeader = playerParty.getPartyLeader();
		if(partyLeader == null)
		{
			return false;
		}
		int memberCount = playerParty.getMemberCount();
		int lootDistribution = playerParty.getLootDistribution();
		activeChar.sendPacket(Msg._PARTY_INFORMATION_);
		switch(lootDistribution)
		{
			case 0:
			{
				activeChar.sendPacket(Msg.LOOTING_METHOD_FINDERS_KEEPERS);
				break;
			}
			case 3:
			{
				activeChar.sendPacket(Msg.LOOTING_METHOD_BY_TURN);
				break;
			}
			case 4:
			{
				activeChar.sendPacket(Msg.LOOTING_METHOD_BY_TURN_INCLUDING_SPOIL);
				break;
			}
			case 1:
			{
				activeChar.sendPacket(Msg.LOOTING_METHOD_RANDOM);
				break;
			}
			case 2:
			{
				activeChar.sendPacket(Msg.LOOTING_METHOD_RANDOM_INCLUDING_SPOIL);
			}
		}
		activeChar.sendPacket(new SystemMessage(1611).addString(partyLeader.getName()));
		activeChar.sendMessage(new CustomMessage("scripts.commands.user.PartyInfo.Members", activeChar).addNumber(memberCount));
		activeChar.sendPacket(Msg.__DASHES__);
		return true;
	}
	
	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}