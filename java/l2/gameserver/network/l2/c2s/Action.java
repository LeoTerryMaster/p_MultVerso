package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ActionFail;

public class Action extends L2GameClientPacket
{
	private int _objectId;
	private int _actionId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		readD();
		readD();
		readD();
		_actionId = readC();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendActionFailed();
			return;
		}
		GameObject obj = activeChar.getVisibleObject(_objectId);
		if(obj == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.setActive();
		if(activeChar.getAggressionTarget() != null && activeChar.getAggressionTarget() != obj)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFrozen())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFail.STATIC);
			return;
		}
		obj.onAction(activeChar, _actionId == 1);
	}
}