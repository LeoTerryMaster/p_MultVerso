package l2.gameserver.handler.voicecommands.impl;

import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.base.Experience;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.RadarControl;
import l2.gameserver.scripts.Functions;

public class Help extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = {"exp", "whereis"};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if((command = command.intern()).equalsIgnoreCase("whereis"))
		{
			return whereis(command, activeChar, args);
		}
		if(command.equalsIgnoreCase("exp"))
		{
			return exp(command, activeChar, args);
		}
		return false;
	}
	
	private boolean exp(String command, Player activeChar, String args)
	{
		if(activeChar.getLevel() >= (activeChar.isSubClassActive() ? Experience.getMaxSubLevel() : Experience.getMaxLevel()))
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Help.MaxLevel", activeChar));
		}
		else
		{
			long exp = Experience.LEVEL[activeChar.getLevel() + 1] - activeChar.getExp();
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Help.ExpLeft", activeChar).addNumber(exp));
		}
		return true;
	}
	
	private boolean whereis(String command, Player activeChar, String args)
	{
		Player friend = World.getPlayer(args);
		if(friend == null)
		{
			return false;
		}
		if(friend.getParty() == activeChar.getParty() || friend.getClan() == activeChar.getClan())
		{
			RadarControl rc = new RadarControl(0, 1, friend.getLoc());
			activeChar.sendPacket(rc);
			return true;
		}
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}