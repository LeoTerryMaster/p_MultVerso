package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.PremiumItem;
import l2.gameserver.network.l2.s2c.ExGetPremiumItemList;
import l2.gameserver.network.l2.s2c.SystemMessage2;

public final class RequestWithDrawPremiumItem extends L2GameClientPacket
{
	private int _itemNum;
	private int _charId;
	private long _itemcount;
	
	@Override
	protected void readImpl()
	{
		_itemNum = readD();
		_charId = readD();
		_itemcount = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(_itemcount <= 0)
		{
			return;
		}
		if(activeChar.getObjectId() != _charId)
		{
			return;
		}
		if(activeChar.getPremiumItemList().isEmpty())
		{
			return;
		}
		if(activeChar.getWeightPenalty() >= 3 || (double) activeChar.getInventoryLimit() * 0.8 <= (double) activeChar.getInventory().getSize())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_THE_VITAMIN_ITEM_BECAUSE_YOU_HAVE_EXCEED_YOUR_INVENTORY_WEIGHT_QUANTITY_LIMIT);
			return;
		}
		if(activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_A_VITAMIN_ITEM_DURING_AN_EXCHANGE);
			return;
		}
		PremiumItem _item = activeChar.getPremiumItemList().get(_itemNum);
		if(_item == null)
		{
			return;
		}
		boolean stackable = ItemHolder.getInstance().getTemplate(_item.getItemId()).isStackable();
		if(_item.getCount() < _itemcount)
		{
			return;
		}
		if(!stackable)
		{
			int i = 0;
			while((long) i < _itemcount)
			{
				addItem(activeChar, _item.getItemId(), 1);
				++i;
			}
		}
		else
		{
			addItem(activeChar, _item.getItemId(), _itemcount);
		}
		if(_itemcount < _item.getCount())
		{
			activeChar.getPremiumItemList().get(_itemNum).updateCount(_item.getCount() - _itemcount);
			activeChar.updatePremiumItem(_itemNum, _item.getCount() - _itemcount);
		}
		else
		{
			activeChar.getPremiumItemList().remove(_itemNum);
			activeChar.deletePremiumItem(_itemNum);
		}
		if(activeChar.getPremiumItemList().isEmpty())
		{
			activeChar.sendPacket(Msg.THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND);
		}
		else
		{
			activeChar.sendPacket(new ExGetPremiumItemList(activeChar));
		}
	}
	
	private void addItem(Player player, int itemId, long count)
	{
		player.getInventory().addItem(itemId, count);
		player.sendPacket(SystemMessage2.obtainItems(itemId, count, 0));
	}
}