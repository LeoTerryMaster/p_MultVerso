package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.instancemanager.CursedWeaponsManager;
import l2.gameserver.model.CursedWeapon;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.utils.ItemFunctions;

public class AdminCursedWeapons implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().Menu)
		{
			return false;
		}
		CursedWeaponsManager cwm = CursedWeaponsManager.getInstance();
		CursedWeapon cw = null;
		switch(command)
		{
			case admin_cw_remove:
			case admin_cw_goto:
			case admin_cw_add:
			case admin_cw_drop:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage("Вы не указали id");
					return false;
				}
				for(CursedWeapon cwp : CursedWeaponsManager.getInstance().getCursedWeapons())
				{
					if(!cwp.getName().toLowerCase().contains(wordList[1].toLowerCase()))
						continue;
					cw = cwp;
				}
				if(cw != null)
					break;
				activeChar.sendMessage("Неизвестный id");
				return false;
			}
		}
		GameObject target;
		switch(command)
		{
			case admin_cw_info:
			{
				activeChar.sendMessage("======= Cursed Weapons: =======");
				for(CursedWeapon c : cwm.getCursedWeapons())
				{
					activeChar.sendMessage("> " + c.getName() + " (" + c.getItemId() + ")");
					if(c.isActivated())
					{
						Player pl = c.getPlayer();
						activeChar.sendMessage("  Player holding: " + pl.getName());
						activeChar.sendMessage("  Player karma: " + c.getPlayerKarma());
						activeChar.sendMessage("  Time Remaining: " + c.getTimeLeft() / 60000 + " min.");
						activeChar.sendMessage("  Kills : " + c.getNbKills());
						continue;
					}
					if(c.isDropped())
					{
						activeChar.sendMessage("  Lying on the ground.");
						activeChar.sendMessage("  Time Remaining: " + c.getTimeLeft() / 60000 + " min.");
						activeChar.sendMessage("  Kills : " + c.getNbKills());
						continue;
					}
					activeChar.sendMessage("  Don't exist in the world.");
				}
				break;
			}
			case admin_cw_reload:
			{
				activeChar.sendMessage("Cursed weapons can't be reloaded.");
				break;
			}
			case admin_cw_check:
			{
				CursedWeaponsManager.getInstance().checkConditions();
				break;
			}
			case admin_cw_remove:
			{
				if(cw == null)
				{
					return false;
				}
				CursedWeaponsManager.getInstance().endOfLife(cw);
				break;
			}
			case admin_cw_goto:
			{
				if(cw == null)
				{
					return false;
				}
				activeChar.teleToLocation(cw.getLoc());
				break;
			}
			case admin_cw_add:
			{
				if(cw == null)
				{
					return false;
				}
				if(cw.isActive())
				{
					activeChar.sendMessage("This cursed weapon is already active.");
					break;
				}
				target = activeChar.getTarget();
				if(target == null || !target.isPlayer() || target.isOlyParticipant())
					break;
				Player player = (Player) target;
				ItemInstance item = ItemFunctions.createItem(cw.getItemId());
				cwm.activate(player, player.getInventory().addItem(item));
				cwm.showUsageTime(player, cw);
				break;
			}
			case admin_cw_drop:
			{
				if(cw == null)
				{
					return false;
				}
				if(cw.isActive())
				{
					activeChar.sendMessage("This cursed weapon is already active.");
					break;
				}
				target = activeChar.getTarget();
				if(target == null || !target.isPlayer() || target.isOlyParticipant())
					break;
				Player player = (Player) target;
				cw.create(null, player);
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
		admin_cw_info,
		admin_cw_remove,
		admin_cw_goto,
		admin_cw_reload,
		admin_cw_add,
		admin_cw_drop,
		admin_cw_check;
	}
}