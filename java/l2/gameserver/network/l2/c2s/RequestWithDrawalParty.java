package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.DimensionalRift;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.network.l2.components.CustomMessage;

public class RequestWithDrawalParty extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		Party party = activeChar.getParty();
		if(party == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isOlyParticipant())
		{
			activeChar.sendMessage("Вы не можете сейчас выйти из группы.");
			return;
		}
		Reflection r = activeChar.getParty().getReflection();
		if(r != null && r instanceof DimensionalRift && activeChar.getReflection().equals(r))
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestWithDrawalParty.Rift", activeChar));
		}
		else if(r != null && activeChar.isInCombat())
		{
			activeChar.sendMessage("Вы не можете сейчас выйти из группы.");
		}
		else
		{
			activeChar.leaveParty();
		}
	}
}