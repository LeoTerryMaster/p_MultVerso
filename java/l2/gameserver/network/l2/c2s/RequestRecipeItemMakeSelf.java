package l2.gameserver.network.l2.c2s;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.RecipeHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.Recipe;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.ActionFail;
import l2.gameserver.network.l2.s2c.RecipeItemMakeInfo;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.templates.item.EtcItemTemplate;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class RequestRecipeItemMakeSelf extends L2GameClientPacket
{
	private int _recipeId;
	
	@Override
	protected void readImpl()
	{
		_recipeId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
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
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isProcessingRequest())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}
		Recipe recipe = RecipeHolder.getInstance().getRecipeById(_recipeId);
		if(recipe == null || recipe.getMaterials().isEmpty() || recipe.getProducts().isEmpty())
		{
			activeChar.sendPacket(Msg.THE_RECIPE_IS_INCORRECT);
			return;
		}
		if(activeChar.getCurrentMp() < (double) recipe.getMpConsume())
		{
			activeChar.sendPacket(Msg.NOT_ENOUGH_MP, new RecipeItemMakeInfo(activeChar, recipe, 0));
			return;
		}
		if(!activeChar.findRecipe(_recipeId))
		{
			activeChar.sendPacket(Msg.PLEASE_REGISTER_A_RECIPE, ActionFail.STATIC);
			return;
		}
		boolean succeed = false;
		List<Pair<ItemTemplate, Long>> materials = recipe.getMaterials();
		List<Pair<ItemTemplate, Long>> products = recipe.getProducts();
		activeChar.getInventory().writeLock();
		try
		{
			for(Pair<ItemTemplate, Long> material : materials)
			{
				ItemTemplate materialItem = material.getKey();
				long materialAmount = material.getValue();
				if(materialAmount <= 0)
					continue;
				if(Config.ALT_GAME_UNREGISTER_RECIPE && materialItem.getItemType() == EtcItemTemplate.EtcItemType.RECIPE)
				{
					Recipe recipe1 = RecipeHolder.getInstance().getRecipeByItem(materialItem);
					if(activeChar.hasRecipe(recipe1))
						continue;
					activeChar.sendPacket(Msg.NOT_ENOUGH_MATERIALS, new RecipeItemMakeInfo(activeChar, recipe, 0));
					return;
				}
				ItemInstance item = activeChar.getInventory().getItemByItemId(materialItem.getItemId());
				if(item != null && item.getCount() >= materialAmount)
					continue;
				activeChar.sendPacket(Msg.NOT_ENOUGH_MATERIALS, new RecipeItemMakeInfo(activeChar, recipe, 0));
				return;
			}
			int totalWeight = 0;
			long totalSlotCount = 0;
			for(Pair<ItemTemplate, Long> product : products)
			{
				totalWeight = (int) ((long) totalWeight + (long) product.getKey().getWeight() * product.getValue());
				totalSlotCount += product.getKey().isStackable() ? 1 : (Long) product.getValue();
			}
			if(!activeChar.getInventory().validateWeight(totalWeight) || !activeChar.getInventory().validateCapacity(totalSlotCount))
			{
				activeChar.sendPacket(Msg.WEIGHT_AND_VOLUME_LIMIT_HAS_BEEN_EXCEEDED_THAT_SKILL_IS_CURRENTLY_UNAVAILABLE, new RecipeItemMakeInfo(activeChar, recipe, 0));
				return;
			}
			for(Pair<ItemTemplate, Long> material : materials)
			{
				ItemTemplate materialItem = material.getKey();
				long materialAmount = material.getValue();
				if(materialAmount <= 0)
					continue;
				if(Config.ALT_GAME_UNREGISTER_RECIPE && materialItem.getItemType() == EtcItemTemplate.EtcItemType.RECIPE)
				{
					activeChar.unregisterRecipe(RecipeHolder.getInstance().getRecipeByItem(materialItem).getId());
					continue;
				}
				if(!activeChar.getInventory().destroyItemByItemId(materialItem.getItemId(), materialAmount))
					continue;
				activeChar.sendPacket(SystemMessage2.removeItems(materialItem.getItemId(), materialAmount));
			}
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}
		activeChar.resetWaitSitTime();
		activeChar.reduceCurrentMp(recipe.getMpConsume(), null);
		if(Rnd.chance(recipe.getSuccessRate()))
		{
			for(Pair<ItemTemplate, Long> product : products)
			{
				int itemId = product.getKey().getItemId();
				long count = product.getValue();
				ItemFunctions.addItem(activeChar, itemId, count, true);
			}
			succeed = true;
		}
		if(!succeed)
		{
			for(Pair<ItemTemplate, Long> product : products)
			{
				activeChar.sendPacket(new SystemMessage(960).addItemName(product.getKey().getItemId()));
			}
		}
		activeChar.sendPacket(new RecipeItemMakeInfo(activeChar, recipe, succeed ? 0 : 1));
	}
}