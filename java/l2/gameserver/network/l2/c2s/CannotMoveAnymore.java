package l2.gameserver.network.l2.c2s;

import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.StopMove;
import l2.gameserver.utils.Location;

public class CannotMoveAnymore extends L2GameClientPacket
{
	private final Location _loc = new Location();
	
	@Override
	protected void readImpl()
	{
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_loc.h = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.isOlyObserver())
		{
			activeChar.sendPacket(new StopMove(activeChar.getObjectId(), _loc));
			return;
		}
		if(!activeChar.isOutOfControl())
		{
			activeChar.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_BLOCKED, _loc, null);
			activeChar.stopMove();
		}
	}
}