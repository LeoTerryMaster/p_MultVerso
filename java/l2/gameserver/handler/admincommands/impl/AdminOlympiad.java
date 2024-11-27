package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.entity.oly.HeroController;
import l2.gameserver.model.entity.oly.NoblesController;
import l2.gameserver.model.entity.oly.OlyController;

public class AdminOlympiad implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		switch(command)
		{
			case admin_oly_save:
			{
				if(!Config.OLY_ENABLED)
				{
					return false;
				}
				try
				{
					OlyController.getInstance().save();
				}
				catch(Exception e)
				{
					
				}
				activeChar.sendMessage("olympaid data saved.");
				break;
			}
			case admin_add_oly_points:
			{
				if(wordList.length < 3)
				{
					activeChar.sendMessage("Command syntax: //add_oly_points <char_name> <point_to_add>");
					activeChar.sendMessage("This command can be applied only for online players.");
					return false;
				}
				Player player = World.getPlayer(wordList[1]);
				if(player == null)
				{
					activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
					return false;
				}
				int pointToAdd;
				try
				{
					pointToAdd = Integer.parseInt(wordList[2]);
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("Please specify integer value for olympiad points.");
					return false;
				}
				int curPoints = NoblesController.getInstance().getPointsOf(player.getObjectId());
				int newPoints = curPoints + pointToAdd;
				NoblesController.getInstance().setPointsOf(player.getObjectId(), newPoints);
				activeChar.sendMessage("Added " + pointToAdd + " points to character " + player.getName());
				activeChar.sendMessage("Old points: " + curPoints + ", new points: " + newPoints);
				break;
			}
			case admin_oly_start:
			{
				Announcements.getInstance().announceToAll(Msg.THE_OLYMPIAD_GAME_HAS_STARTED);
				break;
			}
			case admin_oly_stop:
			{
				Announcements.getInstance().announceToAll(Msg.THE_OLYMPIAD_GAME_HAS_ENDED);
				try
				{
					OlyController.getInstance().save();
				}
				catch(Exception e)
				{
				}
				break;
			}
			case admin_add_hero:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage("Command syntax: //add_hero <char_name>");
					activeChar.sendMessage("This command can be applied only for online players.");
					return false;
				}
				Player player = World.getPlayer(wordList[1]);
				if(player == null)
				{
					activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
					return false;
				}
				HeroController.getInstance().activateHero(player);
				activeChar.sendMessage("Hero status added to player " + player.getName());
				break;
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
		admin_oly_save,
		admin_add_oly_points,
		admin_oly_start,
		admin_add_hero,
		admin_oly_stop;
	}
}