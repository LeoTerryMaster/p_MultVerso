package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.cache.Msg;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.Experience;
import l2.gameserver.model.instances.PetInstance;
import l2.gameserver.tables.PetDataTable;

public class AdminLevel implements IAdminCommandHandler
{
	private void setLevel(Player activeChar, GameObject target, int level)
	{
		if(target == null || !target.isPlayer() && !target.isPet())
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}
		if(level < 1 || level > Experience.getMaxLevel())
		{
			activeChar.sendMessage("You must specify level 1 - " + Experience.getMaxLevel());
			return;
		}
		if(target.isPlayer())
		{
			Long exp_add = Experience.LEVEL[level] - ((Player) target).getExp();
			((Player) target).addExpAndSp(exp_add, 0);
			return;
		}
		if(target.isPet())
		{
			Long exp_add = PetDataTable.getInstance().getInfo(((PetInstance) target).getNpcId(), level).getExp() - ((PetInstance) target).getExp();
			((PetInstance) target).addExpAndSp(exp_add, 0);
		}
	}
	
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().CanEditChar)
		{
			return false;
		}
		GameObject target = activeChar.getTarget();
		if(target == null || !target.isPlayer() && !target.isPet())
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return false;
		}
		switch(command)
		{
			case admin_add_level:
			case admin_addLevel:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //addLevel level");
					return false;
				}
				int level;
				try
				{
					level = Integer.parseInt(wordList[1]);
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("You must specify level");
					return false;
				}
				setLevel(activeChar, target, level + ((Creature) target).getLevel());
				break;
			}
			case admin_set_level:
			case admin_setLevel:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //setLevel level");
					return false;
				}
				int level;
				try
				{
					level = Integer.parseInt(wordList[1]);
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("You must specify level");
					return false;
				}
				setLevel(activeChar, target, level);
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
		admin_add_level,
		admin_addLevel,
		admin_set_level,
		admin_setLevel;
	}
}