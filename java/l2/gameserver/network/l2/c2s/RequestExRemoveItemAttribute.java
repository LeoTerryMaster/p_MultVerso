package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.model.base.Element;
import l2.gameserver.model.items.ItemAttributes;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.PcInventory;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ActionFail;
import l2.gameserver.network.l2.s2c.ExBaseAttributeCancelResult;
import l2.gameserver.network.l2.s2c.ExShowBaseAttributeCancelWindow;
import l2.gameserver.network.l2.s2c.InventoryUpdate;

public class RequestExRemoveItemAttribute extends L2GameClientPacket
{
	private int _objectId;
	private int _attributeId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_attributeId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.isActionsDisabled() || activeChar.isInStoreMode() || activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}
		PcInventory inventory = activeChar.getInventory();
		ItemInstance itemToUnnchant = inventory.getItemByObjectId(_objectId);
		if(itemToUnnchant == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		ItemAttributes set = itemToUnnchant.getAttributes();
		Element element = Element.getElementById(_attributeId);
		if(element == Element.NONE || set.getValue(element) <= 0)
		{
			activeChar.sendPacket(new ExBaseAttributeCancelResult(false, itemToUnnchant, element), ActionFail.STATIC);
			return;
		}
		if(!activeChar.reduceAdena(ExShowBaseAttributeCancelWindow.getAttributeRemovePrice(itemToUnnchant), true))
		{
			activeChar.sendPacket(new ExBaseAttributeCancelResult(false, itemToUnnchant, element), SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, ActionFail.STATIC);
			return;
		}
		boolean equipped = itemToUnnchant.isEquipped();
		if(equipped)
		{
			activeChar.getInventory().unEquipItem(itemToUnnchant);
		}
		itemToUnnchant.setAttributeElement(element, 0);
		if(equipped)
		{
			activeChar.getInventory().equipItem(itemToUnnchant);
		}
		activeChar.sendPacket(new InventoryUpdate().addModifiedItem(itemToUnnchant));
		activeChar.sendPacket(new ExBaseAttributeCancelResult(true, itemToUnnchant, element));
		activeChar.updateStats();
	}
}