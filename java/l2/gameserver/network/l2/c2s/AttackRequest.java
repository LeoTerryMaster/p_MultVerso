package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.ActionFail;

public class AttackRequest extends L2GameClientPacket
{
	private int _objectId;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _attackId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_attackId = readC();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		activeChar.setActive();
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!activeChar.getPlayerAccess().CanAttack)
		{
			activeChar.sendActionFailed();
			return;
		}
		GameObject target = activeChar.getVisibleObject(_objectId);
		if(target == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.getAggressionTarget() != null && activeChar.getAggressionTarget() != target && !activeChar.getAggressionTarget().isDead())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(target.isPlayer() && (activeChar.isInBoat() || target.isInBoat()))
		{
			activeChar.sendPacket(Msg.THIS_IS_NOT_ALLOWED_WHILE_USING_A_FERRY, ActionFail.STATIC);
			return;
		}
		if(target.isPlayable())
		{
			if(activeChar.isInZonePeace())
			{
				activeChar.sendPacket(Msg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE, ActionFail.STATIC);
				return;
			}
			if(((Playable) target).isInZonePeace())
			{
				activeChar.sendPacket(Msg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE, ActionFail.STATIC);
				return;
			}
		}
		if(activeChar.getTarget() != target)
		{
			target.onAction(activeChar, _attackId == 1);
			return;
		}
		if(target.getObjectId() != activeChar.getObjectId() && !activeChar.isInStoreMode() && !activeChar.isProcessingRequest())
		{
			target.onForcedAttack(activeChar, _attackId == 1);
		}
	}
}