package services;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.VariationGroupHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.Player;
import l2.gameserver.model.actor.instances.player.ShortCut;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.PcInventory;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.ActionFail;
import l2.gameserver.network.l2.s2c.InventoryUpdate;
import l2.gameserver.network.l2.s2c.ShortCutRegister;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariationSellService extends Functions implements ScriptFile
{
	private static final Logger LOG = LoggerFactory.getLogger(VariationSellService.class);
	private static Map<Integer, VariationSellServiceTemplate> VARIATION_SELL_SERVICE_TEMPLATES;
	
	private static Map<Integer, VariationSellServiceTemplate> loadVariationSell()
	{
		HashMap<Integer, VariationSellServiceTemplate> result = new HashMap<>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			stmt = conn.createStatement();
			rset = stmt.executeQuery("SELECT `menuId` AS `menuId`,  `variationOption1` AS `variationOption1`,  `variationOption2` AS `variationOption2`,  `consumeList` AS `consumeList` FROM `variation_sell_service_template`");
			while(rset.next())
			{
				int menuId = rset.getInt("menuId");
				int variationOption1 = rset.getInt("variationOption1");
				int variationOption2 = rset.getInt("variationOption2");
				String consumeListText = rset.getString("consumeList").trim();
				List consumeList = Functions.parseItemIdAmountList(consumeListText);
				result.put(menuId, new VariationSellServiceTemplate(menuId, variationOption1, variationOption2, consumeList));
			}
		}
		catch(Exception e)
		{
			LOG.error("VariationSellService: Cant load variation sell template data.", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, stmt, rset);
		}
		return Collections.unmodifiableMap(result);
	}
	
	private static boolean checkPlayerConditions(Player player)
	{
		if(player.isInStoreMode())
		{
			player.sendPacket(Msg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
			return false;
		}
		if(player.isInTrade())
		{
			player.sendActionFailed();
			return false;
		}
		if(player.isDead())
		{
			player.sendPacket(Msg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
			return false;
		}
		if(player.isParalyzed())
		{
			player.sendPacket(Msg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
			return false;
		}
		if(player.isFishing())
		{
			player.sendPacket(Msg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
			return false;
		}
		if(player.isSitting())
		{
			player.sendPacket(Msg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
			return false;
		}
		if(player.isActionsDisabled())
		{
			player.sendActionFailed();
			return false;
		}
		return true;
	}
	
	public void buyVariation(String[] args_)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null || !NpcInstance.canBypassCheck(player, npc) || args_ == null || args_.length == 0)
		{
			return;
		}
		VariationSellServiceTemplate variationSellServiceTemplate = null;
		try
		{
			variationSellServiceTemplate = VARIATION_SELL_SERVICE_TEMPLATES.get(Integer.parseInt(args_[0]));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if(variationSellServiceTemplate == null)
		{
			return;
		}
		ItemInstance targetItem = variationSellServiceTemplate.process(player);
		if(targetItem == null)
		{
			player.sendMessage(new CustomMessage("services.VariationSellService.failed", player));
			return;
		}
		player.sendMessage(new CustomMessage("services.VariationSellService.success", player, targetItem));
	}
	
	@Override
	public void onLoad()
	{
		VARIATION_SELL_SERVICE_TEMPLATES = loadVariationSell();
	}
	
	@Override
	public void onReload()
	{
		VARIATION_SELL_SERVICE_TEMPLATES = loadVariationSell();
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	private static class VariationSellServiceTemplate
	{
		private final int _menuId;
		private final int _variationOption1;
		private final int _variationOption2;
		private final List<Pair<ItemTemplate, Long>> _consumeList;
		
		private VariationSellServiceTemplate(int menuId, int variationOption1, int variationOption2, List<Pair<ItemTemplate, Long>> consumeList)
		{
			_menuId = menuId;
			_variationOption1 = variationOption1;
			_variationOption2 = variationOption2;
			_consumeList = consumeList;
		}
		
		private boolean checkConsumeList(Player player)
		{
			for(Pair<ItemTemplate, Long> consumeItem : _consumeList)
			{
				if(player.getInventory().getCountOf(consumeItem.getLeft().getItemId()) >= consumeItem.getRight())
					continue;
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
				return false;
			}
			return true;
		}
		
		private boolean consume(Player player)
		{
			PcInventory inventory = player.getInventory();
			for(Pair<ItemTemplate, Long> consumeItem : _consumeList)
			{
				if(ItemFunctions.removeItem(player, consumeItem.getLeft().getItemId(), consumeItem.getRight(), true) >= consumeItem.getRight())
					continue;
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
				return false;
			}
			return true;
		}
		
		public ItemInstance process(Player player)
		{
			if(player == null || player.isLogoutStarted() || player.isTeleporting() || !checkPlayerConditions(player))
			{
				return null;
			}
			if(!checkConsumeList(player))
			{
				return null;
			}
			ItemInstance targetItem = player.getInventory().getPaperdollItem(7);
			if(targetItem == null)
			{
				player.sendMessage(new CustomMessage("services.VariationSellService.process.EquippedItemRequired", player));
				return null;
			}
			if(targetItem.isAugmented())
			{
				player.sendPacket(Msg.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN, ActionFail.STATIC);
				return null;
			}
			List variationGroupDataList = VariationGroupHolder.getInstance().getDataForItemId(targetItem.getItemId());
			if(variationGroupDataList == null || variationGroupDataList.isEmpty())
			{
				player.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ActionFail.STATIC);
				return null;
			}
			if(!consume(player))
			{
				return null;
			}
			boolean equipped = targetItem.isEquipped();
			if(equipped)
			{
				player.getInventory().unEquipItem(targetItem);
			}
			targetItem.setVariationStat1(_variationOption1);
			targetItem.setVariationStat2(_variationOption2);
			if(equipped)
			{
				player.getInventory().equipItem(targetItem);
			}
			player.sendPacket(new InventoryUpdate().addModifiedItem(targetItem));
			for(ShortCut sc : player.getAllShortCuts())
			{
				if(sc.getId() != targetItem.getObjectId() || sc.getType() != 1)
					continue;
				player.sendPacket(new ShortCutRegister(player, sc));
			}
			player.sendChanges();
			return targetItem;
		}
	}
}