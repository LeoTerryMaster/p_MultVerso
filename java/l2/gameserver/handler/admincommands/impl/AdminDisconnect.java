package l2.gameserver.handler.admincommands.impl;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.network.l2.components.CustomMessage;

public class AdminDisconnect implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().CanKick)
		{
			return false;
		}
		switch(command)
		{
			case admin_disconnect:
			case admin_kick:
			{
				Player player;
				if(wordList.length == 1)
				{
					GameObject target = activeChar.getTarget();
					if(target == null)
					{
						activeChar.sendMessage("Select character or specify player name.");
						break;
					}
					if(!target.isPlayer())
					{
						activeChar.sendPacket(Msg.INVALID_TARGET);
						break;
					}
					player = (Player) target;
				}
				else
				{
					player = World.getPlayer(wordList[1]);
					if(player == null)
					{
						activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
						break;
					}
				}
				if(player.getObjectId() == activeChar.getObjectId())
				{
					activeChar.sendMessage("You can't logout your character.");
					break;
				}
				activeChar.sendMessage("Character " + player.getName() + " disconnected from server.");
				if(player.isInOfflineMode())
				{
					player.setOfflineMode(false);
					player.kick();
					return true;
				}
				player.sendMessage(new CustomMessage("admincommandhandlers.AdminDisconnect.YoureKickedByGM", player));
				player.sendPacket(Msg.YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER_PLEASE_LOGIN_AGAIN);
				ThreadPoolManager.getInstance().schedule(new RunnableImpl()
				{
					
					@Override
					public void runImpl() throws Exception
					{
						player.kick();
					}
				}, 500);
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
		admin_disconnect,
		admin_kick;
	}
}