package l2.gameserver.network.l2.c2s;

import l2.commons.math.SafeMath;
import l2.commons.util.Rnd;
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
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.templates.manor.CropProcure;
import org.apache.commons.lang3.ArrayUtils;

public class RequestProcureCropList extends L2GameClientPacket
{
	private int _count;
	private int[] _items;
	private int[] _crop;
	private int[] _manor;
	private long[] _itemQ;
	
	@Override
	protected void readImpl()
	{
		_count = readD();
		if(_count * 16 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_crop = new int[_count];
		_manor = new int[_count];
		_itemQ = new long[_count];
		for(int i = 0;i < _count;++i)
		{
			_items[i] = readD();
			_crop[i] = readD();
			_manor[i] = readD();
			_itemQ[i] = readD();
			if(_crop[i] >= 1 && _manor[i] >= 1 && _itemQ[i] >= 1 && ArrayUtils.indexOf(_items, _items[i]) >= i)
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
		ManorManagerInstance manor;
		ManorManagerInstance manorManagerInstance = manor = target != null && target instanceof ManorManagerInstance ? (ManorManagerInstance) target : null;
		if(!(activeChar.isGM() || manor != null && manor.isInActingRange(activeChar)))
		{
			activeChar.sendActionFailed();
			return;
		}
		int currentManorId = manor == null ? 0 : manor.getCastle().getId();
		long totalFee = 0;
		int slots = 0;
		long weight = 0;
		CropProcure crop;
		long count;
		ItemInstance item;
		int cropId;
		int objId;
		Castle castle;
		int i;
		int manorId;
		try
		{
			for(i = 0;i < _count;++i)
			{
				objId = _items[i];
				cropId = _crop[i];
				manorId = _manor[i];
				count = _itemQ[i];
				item = activeChar.getInventory().getItemByObjectId(objId);
				if(item == null || item.getCount() < count || item.getItemId() != cropId)
				{
					return;
				}
				castle = ResidenceHolder.getInstance().getResidence(Castle.class, manorId);
				if(castle == null)
				{
					return;
				}
				crop = castle.getCrop(cropId, 0);
				if(crop == null || crop.getId() == 0 || crop.getPrice() == 0)
				{
					return;
				}
				if(count > crop.getAmount())
				{
					return;
				}
				long price = SafeMath.mulAndCheck(count, crop.getPrice());
				long fee = 0;
				if(currentManorId != 0 && manorId != currentManorId)
				{
					fee = price * 5 / 100;
				}
				totalFee = SafeMath.addAndCheck(totalFee, fee);
				int rewardItemId = Manor.getInstance().getRewardItem(cropId, crop.getReward());
				ItemTemplate template = ItemHolder.getInstance().getTemplate(rewardItemId);
				if(template == null)
				{
					return;
				}
				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, (long) template.getWeight()));
				if(template.isStackable() && activeChar.getInventory().getItemByItemId(cropId) != null)
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
			if(activeChar.getInventory().getAdena() < totalFee)
			{
				activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			for(i = 0;i < _count;++i)
			{
				objId = _items[i];
				cropId = _crop[i];
				manorId = _manor[i];
				count = _itemQ[i];
				item = activeChar.getInventory().getItemByObjectId(objId);
				if(item == null || item.getCount() < count || item.getItemId() != cropId || (castle = ResidenceHolder.getInstance().getResidence(Castle.class, manorId)) == null || (crop = castle.getCrop(cropId, 0)) == null || crop.getId() == 0 || crop.getPrice() == 0 || count > crop.getAmount())
					continue;
				int rewardItemId = Manor.getInstance().getRewardItem(cropId, crop.getReward());
				long sellPrice = count * crop.getPrice();
				long rewardPrice = ItemHolder.getInstance().getTemplate(rewardItemId).getReferencePrice();
				if(rewardPrice == 0)
					continue;
				double reward = (double) sellPrice / (double) rewardPrice;
				long rewardItemCount = (long) reward + (long) (Rnd.nextDouble() <= reward % 1.0D ? 1 : 0);
				if(rewardItemCount < 1)
				{
					SystemMessage sm = new SystemMessage(1491);
					sm.addItemName(cropId);
					sm.addNumber(count);
					activeChar.sendPacket(sm);
					continue;
				}
				long fee = 0;
				if(currentManorId != 0 && manorId != currentManorId)
				{
					fee = sellPrice * 5 / 100;
				}
				if(!activeChar.getInventory().destroyItemByObjectId(objId, count))
					continue;
				if(!activeChar.reduceAdena(fee, false))
				{
					SystemMessage sm = new SystemMessage(1491);
					sm.addItemName(cropId);
					sm.addNumber(count);
					activeChar.sendPacket(sm, Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					continue;
				}
				crop.setAmount(crop.getAmount() - count);
				castle.updateCrop(crop.getId(), crop.getAmount(), 0);
				castle.addToTreasuryNoTax(fee, false, false);
				if(activeChar.getInventory().addItem(rewardItemId, rewardItemCount) == null)
					continue;
				activeChar.sendPacket(new SystemMessage(1490).addItemName(cropId).addNumber(count), SystemMessage2.removeItems(cropId, count), SystemMessage2.obtainItems(rewardItemId, rewardItemCount, 0));
				if(fee <= 0)
					continue;
				activeChar.sendPacket(new SystemMessage(1607).addNumber(fee));
			}
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}
		activeChar.sendChanges();
	}
}