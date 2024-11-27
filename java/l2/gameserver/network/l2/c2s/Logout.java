package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;

public class Logout extends L2GameClientPacket
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
		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_EXIT_THE_GAME_WHILE_IN_COMBAT);
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isBlocked() && !activeChar.isFlying())
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.Logout.OutOfControl", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFestivalParticipant() && SevenSignsFestival.getInstance().isFestivalInitialized())
		{
			activeChar.sendMessage("You cannot log out while you are a participant in a festival.");
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isOlyParticipant())
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.Logout.Olympiad", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInObserverMode())
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.Logout.Observer", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		activeChar.kick();
	}
}