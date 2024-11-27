package l2.gameserver.network.l2.c2s;

import l2.commons.math.SafeMath;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Manor;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.instances.ManorManagerInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.templates.manor.CropProcure;

import java.util.Collections;
import java.util.List;

public class RequestProcureCrop extends L2GameClientPacket
{
	private int _manorId;
	private int _count;
	private int[] _items;
	private long[] _itemQ;
	private List<CropProcure> _procureList = Collections.emptyList();
	
	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_count = readD();
		if(_count * 16 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_itemQ = new long[_count];
		for(int i = 0;i < _count;++i)
		{
			readD();
			_items[i] = readD();
			_itemQ[i] = readD();
			if(_itemQ[i] >= 1)
				continue;
			_count = 0;
			return;
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
		GameObject target = activeChar.getTarget();
		ManorManagerInstance manor = target != null && target instanceof ManorManagerInstance ? (ManorManagerInstance) target : null;
		if(!(activeChar.isGM() || manor != null && activeChar.isInActingRange(manor)))
		{
			activeChar.sendActionFailed();
			return;
		}
		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
		if(castle == null)
		{
			return;
		}
		int slots = 0;
		long weight = 0;
		long count;
		int i;
		int itemId;
		try
		{
			for(i = 0;i < _count;++i)
			{
				itemId = _items[i];
				count = _itemQ[i];
				CropProcure crop = castle.getCrop(itemId, 0);
				if(crop == null)
				{
					return;
				}
				int rewradItemId = Manor.getInstance().getRewardItem(itemId, castle.getCrop(itemId, 0).getReward());
				ItemTemplate template = ItemHolder.getInstance().getTemplate(rewradItemId);
				if(template == null)
				{
					return;
				}
				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, (long) template.getWeight()));
				if(template.isStackable() && activeChar.getInventory().getItemByItemId(itemId) != null)
					continue;
				++slots;
			}
		}
		catch(ArithmeticException e)
		{
			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		activeChar.getInventory().writeLock();
		try
		{
			if(!activeChar.getInventory().validateWeight(weight))
			{
				sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}
			if(!activeChar.getInventory().validateCapacity(slots))
			{
				sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
				return;
			}
			_procureList = castle.getCropProcure(0);
			for(i = 0;i < _count;++i)
			{
				itemId = _items[i];
				count = _itemQ[i];
				int rewradItemId = Manor.getInstance().getRewardItem(itemId, castle.getCrop(itemId, 0).getReward());
				long rewradItemCount = Manor.getInstance().getRewardAmountPerCrop(castle.getId(), itemId, castle.getCropRewardType(itemId));
				rewradItemCount = SafeMath.mulAndCheck(count, rewradItemCount);
				ItemInstance item = activeChar.getInventory().addItem(rewradItemId, rewradItemCount);
				if(!activeChar.getInventory().destroyItemByItemId(itemId, count) || item == null)
					continue;
				activeChar.sendPacket(SystemMessage2.obtainItems(rewradItemId, rewradItemCount, 0));
			}
		}
		catch(ArithmeticException e)
		{
			_count = 0;
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}
		activeChar.sendChanges();
	}
}