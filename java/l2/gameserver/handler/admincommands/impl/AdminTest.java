package l2.gameserver.handler.admincommands.impl;

import l2.commons.util.Rnd;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;

public class AdminTest implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		switch(command)
		{
			case admin_collapse_this:
			{
				if(activeChar.getReflection() != null)
				{
					activeChar.getReflection().startCollapseTimer(1000);
					break;
				}
				activeChar.sendMessage("No reflection");
				break;
			}
			case admin_collapse_this2:
			{
				if(activeChar.getReflection() != null)
				{
					activeChar.getReflection().collapse();
					break;
				}
				activeChar.sendMessage("No reflection");
				break;
			}
			case admin_alt_set_target_hwid:
			{
				Player targetPlayer;
				Player player = targetPlayer = activeChar.getTarget() != null ? activeChar.getTarget().getPlayer() : null;
				if(targetPlayer == null)
					break;
				targetPlayer.getNetConnection().setHwid(wordList[1]);
				break;
			}
			case admin_alt_move_000:
			{
				Player targetPlayer;
				Player player = targetPlayer = activeChar.getTarget() != null ? activeChar.getTarget().getPlayer() : null;
				if(targetPlayer == null)
				{
					targetPlayer = activeChar;
				}
				targetPlayer.moveToLocation(0, 0, 0, 0, true);
				break;
			}
			case admin_alt_move_rnd:
			{
				Player targetPlayer;
				Player player = targetPlayer = activeChar.getTarget() != null ? activeChar.getTarget().getPlayer() : null;
				if(targetPlayer == null)
				{
					targetPlayer = activeChar;
				}
				targetPlayer.moveToLocation(Rnd.get(World.MAP_MIN_X, World.MAP_MAX_X), Rnd.get(World.MAP_MIN_Y, World.MAP_MAX_Y), Rnd.get(World.MAP_MIN_Z, World.MAP_MAX_Z), 0, true);
				break;
			}
		}
		return false;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private enum Commands
	{
		admin_collapse_this,
		admin_collapse_this2,
		admin_alt_set_target_hwid,
		admin_alt_move_000,
		admin_alt_move_rnd;
	}
}