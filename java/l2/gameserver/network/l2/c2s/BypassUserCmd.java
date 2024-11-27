package l2.gameserver.network.l2.c2s;

import l2.gameserver.handler.usercommands.IUserCommandHandler;
import l2.gameserver.handler.usercommands.UserCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.CustomMessage;

public class BypassUserCmd extends L2GameClientPacket
{
	private int _command;
	
	@Override
	protected void readImpl()
	{
		_command = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		IUserCommandHandler handler = UserCommandHandler.getInstance().getUserCommandHandler(_command);
		if(handler == null)
		{
			activeChar.sendMessage(new CustomMessage("common.S1NotImplemented", activeChar).addString(String.valueOf(_command)));
		}
		else
		{
			handler.useUserCommand(_command, activeChar);
		}
	}
}