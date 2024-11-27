package l2.gameserver.handler.voicecommands.impl;

import l2.gameserver.Config;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.scripts.Functions;

public class Banking extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = {"deposit", "withdraw"};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(!Config.SERVICES_BANKING_ENABLED)
		{
			return false;
		}
		if((command = command.intern()).equalsIgnoreCase("deposit"))
		{
			return deposit(command, activeChar, args);
		}
		if(command.equalsIgnoreCase("withdraw"))
		{
			return withdraw(command, activeChar, args);
		}
		return false;
	}
	
	public boolean deposit(String command, Player activeChar, String args)
	{
		if(getItemCount(activeChar, Config.SERVICES_DEPOSIT_ITEM_ID_NEEDED) < (long) Config.SERVICES_DEPOSIT_ITEM_COUNT_NEEDED)
		{
			activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return false;
		}
		removeItem(activeChar, Config.SERVICES_DEPOSIT_ITEM_ID_NEEDED, Config.SERVICES_DEPOSIT_ITEM_COUNT_NEEDED);
		activeChar.sendMessage("Deposit successfully converted");
		addItem(activeChar, Config.SERVICES_DEPOSIT_ITEM_ID_GIVED, Config.SERVICES_DEPOSIT_ITEM_COUNT_GIVED);
		return true;
	}
	
	public boolean withdraw(String command, Player activeChar, String args)
	{
		if(getItemCount(activeChar, Config.SERVICES_WITHDRAW_ITEM_ID_NEEDED) < (long) Config.SERVICES_WITHDRAW_ITEM_COUNT_NEEDED)
		{
			activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return false;
		}
		removeItem(activeChar, Config.SERVICES_WITHDRAW_ITEM_ID_NEEDED, Config.SERVICES_WITHDRAW_ITEM_COUNT_NEEDED);
		activeChar.sendMessage("Withdraw successfully converted");
		addItem(activeChar, Config.SERVICES_WITHDRAW_ITEM_ID_GIVED, Config.SERVICES_WITHDRAW_ITEM_COUNT_GIVED);
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}