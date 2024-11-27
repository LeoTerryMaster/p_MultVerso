package l2.gameserver.handler.usercommands.impl;

import l2.gameserver.handler.usercommands.IUserCommandHandler;
import l2.gameserver.instancemanager.MapRegionManager;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.templates.mapregion.RestartArea;

public class LocCommand implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = {0};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(COMMAND_IDS[0] != id)
		{
			return false;
		}
		RestartArea ra = MapRegionManager.getInstance().getRegionData(RestartArea.class, activeChar);
		int msgId;
		int n = msgId = ra != null ? ra.getRestartPoint().get(activeChar.getRace()).getMsgId() : 0;
		if(msgId > 0)
		{
			activeChar.sendPacket(new SystemMessage(msgId).addNumber(activeChar.getX()).addNumber(activeChar.getY()).addNumber(activeChar.getZ()));
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(1983).addString("Current location : " + activeChar.getX() + ", " + activeChar.getY() + ", " + activeChar.getZ()));
		}
		return true;
	}
	
	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}