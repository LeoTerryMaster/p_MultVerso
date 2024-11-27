package l2.gameserver.handler.voicecommands.impl;

import l2.gameserver.GameServer;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.scripts.Functions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerInfo extends Functions implements IVoicedCommandHandler
{
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	private final String[] _commandList = {"rev", "ver", "date", "time"};
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(command.equals("rev") || command.equals("ver"))
		{
			activeChar.sendMessage("Revision: " + GameServer.getInstance().getVersion().getRevisionNumber());
			activeChar.sendMessage("Build date: " + GameServer.getInstance().getVersion().getBuildDate());
		}
		else if(command.equals("date") || command.equals("time"))
		{
			activeChar.sendMessage(DATE_FORMAT.format(new Date(System.currentTimeMillis())));
			return true;
		}
		return false;
	}
}