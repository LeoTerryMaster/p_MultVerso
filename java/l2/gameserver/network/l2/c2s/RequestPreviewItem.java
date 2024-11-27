package l2.gameserver.network.l2.c2s;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.BuyListHolder;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.Inventory;
import l2.gameserver.network.l2.s2c.ShopPreviewInfo;
import l2.gameserver.network.l2.s2c.ShopPreviewList;
import l2.gameserver.templates.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class RequestPreviewItem extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestPreviewItem.class);
	private int _unknow;
	private int _listId;
	private int _count;
	private int[] _items;
	
	@Override
	protected void readImpl()
	{
		_unknow = readD();
		_listId = readD();
		_count = readD();
		if(_count * 4 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		for(int i = 0;i < _count;++i)
		{
			_items[i] = readD();
		}
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _count == 0)
		{
			return;
		}
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getKarma() > 0 && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}
		NpcInstance merchant = activeChar.getLastNpc();
		boolean isValidMerchant;
		boolean bl = isValidMerchant = merchant != null && merchant.isMerchantNpc();
		if(!(activeChar.isGM() || merchant != null && isValidMerchant && merchant.isInActingRange(activeChar)))
		{
			activeChar.sendActionFailed();
			return;
		}
		BuyListHolder.NpcTradeList list = BuyListHolder.getInstance().getBuyList(_listId);
		if(list == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		boolean slots = false;
		HashMap<Integer, Integer> itemList = new HashMap<>();
		try
		{
			long totalPrice = 0;
			for(int i = 0;i < _count;++i)
			{
				int itemId = _items[i];
				if(list.getItemByItemId(itemId) == null)
				{
					activeChar.sendActionFailed();
					return;
				}
				ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
				int paperdoll;
				if(template == null || !template.isEquipable() || (paperdoll = Inventory.getPaperdollIndex(template.getBodyPart())) < 0)
					continue;
				if(itemList.containsKey(paperdoll))
				{
					activeChar.sendPacket(Msg.THOSE_ITEMS_MAY_NOT_BE_TRIED_ON_SIMULTANEOUSLY);
					return;
				}
				itemList.put(paperdoll, itemId);
				totalPrice += (long) ShopPreviewList.getWearPrice(template);
			}
			if(!activeChar.reduceAdena(totalPrice))
			{
				activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
		}
		catch(ArithmeticException e)
		{
			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		if(!itemList.isEmpty())
		{
			activeChar.sendPacket(new ShopPreviewInfo(itemList));
			ThreadPoolManager.getInstance().schedule(new RemoveWearItemsTask(activeChar), Config.WEAR_DELAY * 1000);
		}
	}
	
	private static class RemoveWearItemsTask extends RunnableImpl
	{
		private final Player _activeChar;
		
		public RemoveWearItemsTask(Player activeChar)
		{
			_activeChar = activeChar;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			_activeChar.sendPacket(Msg.TRYING_ON_MODE_HAS_ENDED);
			_activeChar.sendUserInfo(true);
		}
	}
}