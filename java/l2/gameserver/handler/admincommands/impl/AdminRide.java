package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Player;

public class AdminRide implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().Rider)
		{
			return false;
		}
		switch(command)
		{
			case admin_ride:
			{
				if(activeChar.isMounted() || activeChar.getPet() != null)
				{
					activeChar.sendMessage("Already Have a Pet or Mounted.");
					return false;
				}
				if(wordList.length != 2)
				{
					activeChar.sendMessage("Incorrect id.");
					return false;
				}
				activeChar.setMount(Integer.parseInt(wordList[1]), 0, 85);
				break;
			}
			case admin_ride_wyvern:
			case admin_wr:
			{
				if(activeChar.isMounted() || activeChar.getPet() != null)
				{
					activeChar.sendMessage("Already Have a Pet or Mounted.");
					return false;
				}
				activeChar.setMount(12621, 0, 85);
				break;
			}
			case admin_ride_strider:
			case admin_sr:
			{
				if(activeChar.isMounted() || activeChar.getPet() != null)
				{
					activeChar.sendMessage("Already Have a Pet or Mounted.");
					return false;
				}
				activeChar.setMount(12526, 0, 85);
				break;
			}
			case admin_unride:
			case admin_ur:
			{
				activeChar.setMount(0, 0, 0);
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
		admin_ride,
		admin_ride_wyvern,
		admin_ride_strider,
		admin_unride,
		admin_wr,
		admin_sr,
		admin_ur;
	}
}