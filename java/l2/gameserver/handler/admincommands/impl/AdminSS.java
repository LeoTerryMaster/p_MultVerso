package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.SevenSigns;

public class AdminSS implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().Menu)
		{
			return false;
		}
		switch(command)
		{
			case admin_ssq_change:
			{
				if(wordList.length > 2)
				{
					int period = Integer.parseInt(wordList[1]);
					int minutes = Integer.parseInt(wordList[2]);
					SevenSigns.getInstance().changePeriod(period, minutes * 60);
					break;
				}
				if(wordList.length > 1)
				{
					int period = Integer.parseInt(wordList[1]);
					SevenSigns.getInstance().changePeriod(period);
					break;
				}
				SevenSigns.getInstance().changePeriod();
				break;
			}
			case admin_ssq_time:
			{
				if(wordList.length <= 1)
					break;
				int time = Integer.parseInt(wordList[1]);
				SevenSigns.getInstance().setTimeToNextPeriodChange(time);
				break;
			}
			case admin_ssq_cabal:
			{
				if(wordList.length <= 3)
					break;
				int player = Integer.parseInt(wordList[1]);
				int cabal = Integer.parseInt(wordList[2]);
				int seal = Integer.parseInt(wordList[3]);
				SevenSigns.getInstance().setPlayerInfo(player, cabal, seal);
			}
		}
		return true;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private enum Commands
	{
		admin_ssq_change,
		admin_ssq_time,
		admin_ssq_cabal;
	}
}