package l2.gameserver.network.l2.c2s;

import l2.commons.math.SafeMath;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.instances.ManorManagerInstance;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.templates.manor.SeedProduction;

public class RequestBuySeed extends L2GameClientPacket
{
	private int _count;
	private int _manorId;
	private int[] _items;
	private long[] _itemQ;
	
	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_count = readD();
		if(_count * 8 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_itemQ = new long[_count];
		for(int i = 0;i < _count;++i)
		{
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
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
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
		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
		if(castle == null)
		{
			return;
		}
		long totalPrice = 0;
		int slots = 0;
		long weight = 0;
		int seedId;
		int i;
		long count;
		try
		{
			for(i = 0;i < _count;++i)
			{
				seedId = _items[i];
				count = _itemQ[i];
				SeedProduction seed = castle.getSeed(seedId, 0);
				long price = seed.getPrice();
				long residual = seed.getCanProduce();
				if(price < 1)
				{
					return;
				}
				if(residual < count)
				{
					return;
				}
				totalPrice = SafeMath.addAndCheck(totalPrice, SafeMath.mulAndCheck(count, price));
				ItemTemplate item = ItemHolder.getInstance().getTemplate(seedId);
				if(item == null)
				{
					return;
				}
				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, (long) item.getWeight()));
				if(item.isStackable() && activeChar.getInventory().getItemByItemId(seedId) != null)
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
			if(!activeChar.reduceAdena(totalPrice, true))
			{
				sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			castle.addToTreasuryNoTax(totalPrice, false, true);
			for(i = 0;i < _count;++i)
			{
				seedId = _items[i];
				count = _itemQ[i];
				SeedProduction seed = castle.getSeed(seedId, 0);
				seed.setCanProduce(seed.getCanProduce() - count);
				castle.updateSeed(seed.getId(), seed.getCanProduce(), 0);
				activeChar.getInventory().addItem(seedId, count);
				activeChar.sendPacket(SystemMessage2.obtainItems(seedId, count, 0));
			}
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}
		activeChar.sendChanges();
	}
}