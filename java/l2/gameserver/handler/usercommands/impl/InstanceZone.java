package l2.gameserver.handler.usercommands.impl;

import l2.gameserver.data.xml.holder.InstantZoneHolder;
import l2.gameserver.handler.usercommands.IUserCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage2;

import java.util.Iterator;

public class InstanceZone implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = {114};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(COMMAND_IDS[0] != id)
		{
			return false;
		}
		if(activeChar.getActiveReflection() != null)
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.INSTANT_ZONE_CURRENTLY_IN_USE_S1).addInstanceName(activeChar.getActiveReflection().getInstancedZoneId()));
		}
		boolean noLimit = true;
		boolean showMsg = false;
		Iterator<Integer> iterator = activeChar.getInstanceReuses().keySet().iterator();
		while(iterator.hasNext())
		{
			int i = iterator.next();
			int limit = InstantZoneHolder.getInstance().getMinutesToNextEntrance(i, activeChar);
			if(limit <= 0)
				continue;
			noLimit = false;
			if(!showMsg)
			{
				activeChar.sendPacket(SystemMsg.INSTANCE_ZONE_TIME_LIMIT);
				showMsg = true;
			}
			activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_WILL_BE_AVAILABLE_FOR_REUSE_AFTER_S2_HOURS_S3_MINUTES).addInstanceName(i).addInteger(limit / 60).addInteger(limit % 60));
		}
		if(noLimit)
		{
			activeChar.sendPacket(SystemMsg.THERE_IS_NO_INSTANCE_ZONE_UNDER_A_TIME_LIMIT);
		}
		return true;
	}
	
	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}