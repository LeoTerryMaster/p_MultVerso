package l2.gameserver.network.l2.c2s;

import l2.commons.util.Rnd;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.RecipeHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.Recipe;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.ManufactureItem;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.RecipeShopItemInfo;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.TradeHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class RequestRecipeShopMakeDo extends L2GameClientPacket
{
	private int _manufacturerId;
	private int _recipeId;
	private long _price;
	
	@Override
	protected void readImpl()
	{
		_manufacturerId = readD();
		_recipeId = readD();
		_price = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player buyer = getClient().getActiveChar();
		if(buyer == null)
		{
			return;
		}
		if(buyer.isActionsDisabled())
		{
			buyer.sendActionFailed();
			return;
		}
		if(buyer.isInStoreMode())
		{
			buyer.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if(buyer.isInTrade())
		{
			buyer.sendActionFailed();
			return;
		}
		if(buyer.isFishing())
		{
			buyer.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
			return;
		}
		if(!buyer.getPlayerAccess().UseTrade)
		{
			buyer.sendPacket(Msg.THIS_ACCOUNT_CANOT_USE_PRIVATE_STORES);
			return;
		}
		Player manufacturer = (Player) buyer.getVisibleObject(_manufacturerId);
		if(manufacturer == null || manufacturer.getPrivateStoreType() != 5 || !manufacturer.isInActingRange(buyer))
		{
			buyer.sendActionFailed();
			return;
		}
		Recipe recipe = null;
		for(ManufactureItem mi : manufacturer.getCreateList())
		{
			if(mi.getRecipeId() != _recipeId || _price != mi.getCost())
				continue;
			recipe = RecipeHolder.getInstance().getRecipeById(_recipeId);
			break;
		}
		if(recipe == null)
		{
			buyer.sendActionFailed();
			return;
		}
		int success = 0;
		if(recipe.getProducts().isEmpty() || recipe.getMaterials().isEmpty())
		{
			manufacturer.sendMessage(new CustomMessage("l2p.gameserver.RecipeController.NoRecipe", manufacturer).addItemName(recipe.getItem()));
			buyer.sendMessage(new CustomMessage("l2p.gameserver.RecipeController.NoRecipe", manufacturer).addItemName(recipe.getItem()));
			return;
		}
		if(!manufacturer.findRecipe(_recipeId))
		{
			buyer.sendActionFailed();
			return;
		}
		if(manufacturer.getCurrentMp() < (double) recipe.getMpConsume())
		{
			manufacturer.sendPacket(Msg.NOT_ENOUGH_MP);
			buyer.sendPacket(Msg.NOT_ENOUGH_MP, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
			return;
		}
		List<Pair<ItemTemplate, Long>> materials = recipe.getMaterials();
		List<Pair<ItemTemplate, Long>> products = recipe.getProducts();
		buyer.getInventory().writeLock();
		try
		{
			if(buyer.getAdena() < _price)
			{
				buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
				return;
			}
			for(Pair<ItemTemplate, Long> material : materials)
			{
				ItemTemplate materialItem = material.getKey();
				long materialAmount = material.getValue();
				if(materialAmount <= 0)
					continue;
				ItemInstance item = buyer.getInventory().getItemByItemId(materialItem.getItemId());
				if(item != null && item.getCount() >= materialAmount)
					continue;
				buyer.sendPacket(Msg.NOT_ENOUGH_MATERIALS, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
				return;
			}
			int totalWeight = 0;
			long totalSlotCount = 0;
			for(Pair<ItemTemplate, Long> product : products)
			{
				totalWeight = (int) ((long) totalWeight + (long) product.getKey().getWeight() * product.getValue());
				totalSlotCount += product.getKey().isStackable() ? 1 : (Long) product.getValue();
			}
			if(!buyer.getInventory().validateWeight(totalWeight) || !buyer.getInventory().validateCapacity(totalSlotCount))
			{
				buyer.sendPacket(Msg.THE_WEIGHT_AND_VOLUME_LIMIT_OF_INVENTORY_MUST_NOT_BE_EXCEEDED, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
				return;
			}
			if(!buyer.reduceAdena(_price, false))
			{
				buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
				return;
			}
			for(Pair<ItemTemplate, Long> material : materials)
			{
				ItemTemplate materialItem = material.getKey();
				long materialAmount = material.getValue();
				if(materialAmount <= 0)
					continue;
				buyer.getInventory().destroyItemByItemId(materialItem.getItemId(), materialAmount);
				buyer.sendPacket(SystemMessage2.removeItems(materialItem.getItemId(), materialAmount));
			}
			long tax = TradeHelper.getTax(manufacturer, _price);
			if(tax > 0)
			{
				_price -= tax;
				manufacturer.sendMessage(new CustomMessage("trade.HavePaidTax", manufacturer).addNumber(tax));
			}
			manufacturer.addAdena(_price);
		}
		finally
		{
			buyer.getInventory().writeUnlock();
		}
		for(Pair<ItemTemplate, Long> product : products)
		{
			manufacturer.sendMessage(new CustomMessage("l2p.gameserver.RecipeController.GotOrder", manufacturer).addItemName(product.getKey()));
		}
		manufacturer.reduceCurrentMp(recipe.getMpConsume(), null);
		manufacturer.sendStatusUpdate(false, false, 11);
		if(Rnd.chance(recipe.getSuccessRate()))
		{
			for(Pair<ItemTemplate, Long> product : products)
			{
				int itemId = product.getKey().getItemId();
				long count = product.getValue();
				ItemFunctions.addItem(buyer, itemId, count, true);
			}
			success = 1;
		}
		SystemMessage sm;
		if(success == 0)
		{
			for(Pair<ItemTemplate, Long> product : products)
			{
				int itemId = product.getKey().getItemId();
				sm = new SystemMessage(1150);
				sm.addString(manufacturer.getName());
				sm.addItemName(itemId);
				sm.addNumber(_price);
				buyer.sendPacket(sm);
				sm = new SystemMessage(1149);
				sm.addString(buyer.getName());
				sm.addItemName(itemId);
				sm.addNumber(_price);
				manufacturer.sendPacket(sm);
			}
		}
		else
		{
			for(Pair<ItemTemplate, Long> product : products)
			{
				int itemId = product.getKey().getItemId();
				long count = product.getValue();
				if(count > 1)
				{
					sm = new SystemMessage(1148);
					sm.addString(manufacturer.getName());
					sm.addItemName(itemId);
					sm.addNumber(count);
					sm.addNumber(_price);
					buyer.sendPacket(sm);
					sm = new SystemMessage(1152);
					sm.addString(buyer.getName());
					sm.addItemName(itemId);
					sm.addNumber(count);
					sm.addNumber(_price);
					manufacturer.sendPacket(sm);
					continue;
				}
				sm = new SystemMessage(1146);
				sm.addString(manufacturer.getName());
				sm.addItemName(itemId);
				sm.addNumber(_price);
				buyer.sendPacket(sm);
				sm = new SystemMessage(1151);
				sm.addString(buyer.getName());
				sm.addItemName(itemId);
				sm.addNumber(_price);
				manufacturer.sendPacket(sm);
			}
		}
		buyer.sendChanges();
		buyer.sendPacket(new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
	}
}