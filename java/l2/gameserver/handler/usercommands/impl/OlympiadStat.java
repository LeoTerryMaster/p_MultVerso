package l2.gameserver.handler.usercommands.impl;

import l2.gameserver.handler.usercommands.IUserCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.oly.NoblesController;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;

public class OlympiadStat implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = {109};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(id != COMMAND_IDS[0])
		{
			return false;
		}
		if(!activeChar.isNoble())
		{
			activeChar.sendPacket(SystemMsg.THIS_COMMAND_CAN_ONLY_BE_USED_BY_A_NOBLESSE);
			return true;
		}
		NoblesController.NobleRecord nr = NoblesController.getInstance().getNobleRecord(activeChar.getObjectId());
		CustomMessage sm = new CustomMessage("Olympiad.stat", activeChar);
		sm = sm.addNumber(Math.max(0, nr.comp_done));
		sm = sm.addNumber(Math.max(0, nr.comp_win));
		sm = sm.addNumber(Math.max(0, nr.comp_loose));
		sm = sm.addNumber(Math.max(0, nr.points_current));
		activeChar.sendMessage(sm);
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}