package l2.gameserver.network.l2.c2s;

import l2.commons.math.SafeMath;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.OptionDataHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.Request;
import l2.gameserver.model.Skill;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.TradeItem;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SendTradeDone;
import l2.gameserver.network.l2.s2c.TradeOtherAdd;
import l2.gameserver.network.l2.s2c.TradeOwnAdd;
import l2.gameserver.network.l2.s2c.TradeUpdate;
import l2.gameserver.templates.OptionDataTemplate;

import java.util.List;

public class AddTradeItem extends L2GameClientPacket
{
	private int _tradeId;
	private int _objectId;
	private long _amount;
	
	@Override
	protected void readImpl()
	{
		_tradeId = readD();
		_objectId = readD();
		_amount = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player parthner1 = getClient().getActiveChar();
		if(parthner1 == null || _amount < 1)
		{
			return;
		}
		Request request = parthner1.getRequest();
		if(request == null || !request.isTypeOf(Request.L2RequestType.TRADE))
		{
			parthner1.sendActionFailed();
			return;
		}
		if(!request.isInProgress())
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendActionFailed();
			return;
		}
		if(parthner1.isOutOfControl())
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendActionFailed();
			return;
		}
		Player parthner2 = request.getOtherPlayer(parthner1);
		if(parthner2 == null)
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendPacket(Msg.THAT_PLAYER_IS_NOT_ONLINE);
			parthner1.sendActionFailed();
			return;
		}
		if(parthner2.getRequest() != request)
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendActionFailed();
			return;
		}
		if(request.isConfirmed(parthner1) || request.isConfirmed(parthner2))
		{
			parthner1.sendPacket(SystemMsg.YOU_MAY_NO_LONGER_ADJUST_ITEMS_IN_THE_TRADE_BECAUSE_THE_TRADE_HAS_BEEN_CONFIRMED);
			parthner1.sendActionFailed();
			return;
		}
		ItemInstance item = parthner1.getInventory().getItemByObjectId(_objectId);
		if(item == null || !item.canBeTraded(parthner1))
		{
			parthner1.sendPacket(SystemMsg.THIS_ITEM_CANNOT_BE_TRADED_OR_SOLD);
			return;
		}
		long count;
		long addedAmount = count = Math.min(_amount, item.getCount());
		List<TradeItem> tradeList = parthner1.getTradeList();
		TradeItem tradeItem = null;
		try
		{
			for(TradeItem ti : parthner1.getTradeList())
			{
				if(ti.getObjectId() != _objectId)
					continue;
				long currAmount = ti.getCount();
				count = SafeMath.addAndCheck(count, currAmount);
				count = Math.min(count, item.getCount());
				ti.setCount(count);
				addedAmount = Math.max(0, ti.getCount() - currAmount);
				tradeItem = ti;
				break;
			}
		}
		catch(ArithmeticException e)
		{
			parthner1.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		if(tradeItem == null)
		{
			tradeItem = new TradeItem(item);
			tradeItem.setCount(count);
			tradeList.add(tradeItem);
		}
		if(Config.ALT_ALLOW_DROP_AUGMENTED && item.isAugmented())
		{
			Skill varOptSkill = null;
			if(item.getVariationStat1() > 0 || item.getVariationStat2() > 0)
			{
				OptionDataTemplate odt1 = OptionDataHolder.getInstance().getTemplate(item.getVariationStat1());
				OptionDataTemplate odt2 = OptionDataHolder.getInstance().getTemplate(item.getVariationStat2());
				if(odt2 != null && !odt2.getSkills().isEmpty())
				{
					varOptSkill = odt2.getSkills().get(0);
				}
				if(odt1 != null && !odt1.getSkills().isEmpty())
				{
					varOptSkill = odt1.getSkills().get(0);
				}
			}
			if(varOptSkill != null)
			{
				if(varOptSkill.isActive())
				{
					parthner2.sendMessage(new CustomMessage("trade.AugmentItemActive", parthner1, parthner1, item, varOptSkill));
				}
				else if(!varOptSkill.getTriggerList().isEmpty())
				{
					parthner2.sendMessage(new CustomMessage("trade.AugmentItemChance", parthner1, parthner1, item, varOptSkill));
				}
				else if(varOptSkill.isPassive())
				{
					parthner2.sendMessage(new CustomMessage("trade.AugmentItemPassive", parthner1, parthner1, item, varOptSkill));
				}
				else
				{
					parthner2.sendMessage(new CustomMessage("trade.AugmentItem", parthner1, parthner1, item, varOptSkill));
				}
			}
			else
			{
				parthner2.sendMessage(new CustomMessage("trade.AugmentItemWithoutSkill", parthner1, parthner1, item));
			}
		}
		parthner1.sendPacket(new TradeOwnAdd(tradeItem, addedAmount), new TradeUpdate(tradeItem, item.getCount() - tradeItem.getCount()));
		parthner2.sendPacket(new TradeOtherAdd(tradeItem, addedAmount));
	}
}