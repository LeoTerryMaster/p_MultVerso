package l2.gameserver.network.l2.c2s;

import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.model.Player;
import l2.gameserver.model.Summon;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.utils.ItemFunctions;

public class RequestPetGetItem extends L2GameClientPacket
{
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
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
		Summon summon = activeChar.getPet();
		if(summon == null || !summon.isPet() || summon.isDead() || summon.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		ItemInstance item = (ItemInstance) activeChar.getVisibleObject(_objectId);
		if(item == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!ItemFunctions.checkIfCanPickup(summon, item))
		{
			SystemMessage sm;
			if(item.getItemId() == 57)
			{
				sm = new SystemMessage(55);
				sm.addNumber(item.getCount());
			}
			else
			{
				sm = new SystemMessage(56);
				sm.addItemName(item.getItemId());
			}
			sendPacket(sm);
			activeChar.sendActionFailed();
			return;
		}
		summon.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item, null);
	}
}