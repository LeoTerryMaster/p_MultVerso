package services.pawnshop;

import l2.commons.dbutils.DbUtils;
import l2.commons.math.SafeMath;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.dao.CharacterDAO;
import l2.gameserver.data.StringHolder;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.data.xml.holder.OptionDataHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.World;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.taskmanager.DelayedItemsManager;
import l2.gameserver.templates.OptionDataTemplate;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PawnShop extends Functions implements ScriptFile
{
	private static final Logger LOG = LoggerFactory.getLogger(PawnShop.class);
	private static final String HTML_BASE = "mods/pawnshop";
	private static final String BYPASS_PREFIX = "-h scripts_services.pawnshop.PawnShop:";
	private static final AtomicInteger LAST_ID = new AtomicInteger(0);
	private static final Comparator<PawnShopItem> PAWN_SHOP_ITEM_COMPARATOR = new Comparator<PawnShopItem>()
	{
		@Override
		public int compare(PawnShopItem o1, PawnShopItem o2)
		{
			if(o1.getCurrencyItemId() == o2.getCurrencyItemId())
			{
				return o1.getPrice() - o2.getPrice();
			}
			return o2.getCurrencyItemId() - o1.getCurrencyItemId();
		}
	};
	private static CopyOnWriteArrayList<PawnShopItem> PAWN_SHOP_ITEMS = new CopyOnWriteArrayList();
	
	private static void loadItems()
	{
		LinkedList<PawnShopItem> items = new LinkedList<>();
		int maxId = 0;
		ResultSet rset = null;
		Statement stmt = null;
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			stmt = con.createStatement();
			rset = stmt.executeQuery("{CALL `lip_ex_PawnShopLoadItems`()}");
			while(rset.next())
			{
				PawnShopItem item = new PawnShopItem(rset.getInt("id"), rset.getInt("ownerId"), rset.getInt("itemType"), rset.getInt("amount"), rset.getInt("enchantLevel"), rset.getInt("currency"), rset.getInt("price"), rset.getInt("varOpt1"), rset.getInt("varOpt2"));
				items.add(0, item);
				if(item.getId() <= maxId)
					continue;
				maxId = item.getId();
			}
		}
		catch(SQLException e)
		{
			LOG.error("PawnShop: Can't load items", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, stmt, rset);
		}
		int lastId = LAST_ID.get();
		if(maxId > lastId)
		{
			LAST_ID.compareAndSet(lastId, maxId);
		}
		PAWN_SHOP_ITEMS = new CopyOnWriteArrayList(items);
	}
	
	public static void showStartPage(Player player, NpcInstance npc)
	{
		if(!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc))
		{
			return;
		}
		doBuyList(player, npc, 0, "");
	}
	
	private static boolean isAllowedNpc(Player player, NpcInstance npc)
	{
		if(player == null || npc == null)
		{
			return false;
		}
		return !player.isActionsDisabled() && Config.ALLOW_TALK_WHILE_SITTING || !player.isSitting() && npc.isInActingRange(player);
	}
	
	private static void parseArgs(String[] args, int[] ints)
	{
		parseArgs(args, ints, null);
	}
	
	private static void parseArgs(String[] args, int[] ints, StringBuilder remind)
	{
		int argIdx = 0;
		if(ints != null)
		{
			for(int intIdx = 0;argIdx < args.length && intIdx < ints.length;++argIdx, ++intIdx)
			{
				String arg = args[argIdx];
				try
				{
					long longVal = Long.parseLong(arg);
					ints[intIdx] = (int) (longVal & 0xFFFFFFFFL);
					continue;
				}
				catch(Exception e)
				{
					LOG.warn("Can't parse int arg \"" + arg + "\"");
				}
			}
		}
		if(remind != null && argIdx < args.length)
		{
			remind.append(args[argIdx]);
			++argIdx;
			while(argIdx < args.length)
			{
				remind.append(' ').append(args[argIdx]);
				++argIdx;
			}
		}
	}
	
	private static void doBuyList(Player player, NpcInstance npc, int page, String queryStr)
	{
		String query = stripString(queryStr);
		PawnShop.PawnShopItem[] pawnShopItems = searchQuery(player, query);
		StringBuilder itemsHtmlBuilder = new StringBuilder();
		NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
		html.setFile("mods/pawnshop/buy_list.htm");
		int psiIdx = Config.PAWNSHOP_ITEMS_PER_PAGE * page;
		
		for(int psiLastIdx = psiIdx + Config.PAWNSHOP_ITEMS_PER_PAGE;psiIdx < psiLastIdx && psiIdx < pawnShopItems.length;++psiIdx)
		{
			PawnShop.PawnShopItem pawnShopItem = pawnShopItems[psiIdx];
			itemsHtmlBuilder.append(itemHtml(player, "pawnshop.buy_item_element", String.format("-h scripts_services.pawnshop.PawnShop:buyItem %d %d %s", pawnShopItem.getId(), page, query), pawnShopItem.getItemTemplate(), pawnShopItem.getEnchantLevel(), pawnShopItem.getAmount(), ItemHolder.getInstance().getTemplate(pawnShopItem.getCurrencyItemId()), (long) pawnShopItem.getPrice(), pawnShopItem.getOwnerName(), pawnShopItem.getVariationSkill()));
		}
		
		html.replace("%list%", itemsHtmlBuilder.toString());
		html.replace("%paging%", pagingHtml(player, page, pawnShopItems, "buyList", query));
		player.sendPacket(html);
	}
	
	private static PawnShopItem[] searchQuery(Player player, String query)
	{
		LinkedList<PawnShopItem> result = new LinkedList<>();
		boolean fallback = true;
		if(!query.isEmpty())
		{
			String enchLvlStr;
			if(query.charAt(0) == '+' && StringUtils.isNumeric(enchLvlStr = query.substring(1)))
			{
				try
				{
					int enchLvl = Integer.parseInt(enchLvlStr);
					for(PawnShopItem pawnShopItem : PAWN_SHOP_ITEMS)
					{
						if(!isAllowedItem(pawnShopItem) || pawnShopItem.getEnchantLevel() != enchLvl)
							continue;
						result.add(pawnShopItem);
					}
					fallback = false;
				}
				catch(Exception e)
				{
					LOG.warn("PawnShop: Can't process item enchant level query \"" + query + "\"", e);
				}
			}
			if(fallback && StringUtils.isNumeric(query))
			{
				try
				{
					int itemTypeId = Integer.parseInt(query);
					for(PawnShopItem pawnShopItem : PAWN_SHOP_ITEMS)
					{
						if(!isAllowedItem(pawnShopItem) || pawnShopItem.getItemTypeId() != itemTypeId)
							continue;
						result.add(pawnShopItem);
					}
					fallback = false;
				}
				catch(Exception e)
				{
					LOG.warn("PawnShop: Can't process item id query \"" + query + "\"", e);
				}
			}
			if(fallback)
			{
				if(query.length() >= Config.PAWNSHOP_MIN_QUERY_LENGTH)
				{
					for(PawnShopItem pawnShopItem : PAWN_SHOP_ITEMS)
					{
						if(!isAllowedItem(pawnShopItem) || !StringUtils.containsIgnoreCase(pawnShopItem.getNameForQuery(), query))
							continue;
						result.add(pawnShopItem);
					}
					fallback = false;
				}
				else
				{
					player.sendMessage(new CustomMessage("pawnshop.query_to_short", player));
				}
			}
			if(!fallback && Config.PAWNSHOP_PRICE_SORT)
			{
				result.sort(PAWN_SHOP_ITEM_COMPARATOR);
			}
		}
		if(fallback)
		{
			for(PawnShopItem pawnShopItem : PAWN_SHOP_ITEMS)
			{
				if(!isAllowedItem(pawnShopItem))
					continue;
				result.add(pawnShopItem);
			}
		}
		return result.toArray(new PawnShopItem[result.size()]);
	}
	
	private static void doBuyItem(Player player, int pawnShopItemId)
	{
		if(!player.getPlayerAccess().UseTrade)
		{
			player.sendPacket(Msg.THIS_ACCOUNT_CANOT_TRADE_ITEMS);
			return;
		}
		if(pawnShopItemId <= 0)
		{
			player.sendPacket(SystemMsg.SYSTEM_ERROR);
			return;
		}
		if(player.getWeightPenalty() >= 3 || player.getInventory().getSize() > player.getInventoryLimit() - 10)
		{
			player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return;
		}
		PawnShopItem pawnShopItem = null;
		for(PawnShopItem psi : PAWN_SHOP_ITEMS)
		{
			if(psi.getId() != pawnShopItemId)
				continue;
			pawnShopItem = psi;
		}
		if(pawnShopItem == null || !isAllowedItem(pawnShopItem))
		{
			return;
		}
		int currency = pawnShopItem.getCurrencyItemId();
		long price = pawnShopItem.getPrice();
		int ownerId = pawnShopItem.getOwnerId();
		if(ownerId == player.getObjectId())
		{
			player.sendMessage(new CustomMessage("pawnshop.owner_cant_buy_own_item", player));
			return;
		}
		if(currency <= 0 || price <= 0 || player.getInventory().getCountOf(currency) < price)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS, new SystemMessage(1959).addNumber(price).addItemName(currency));
			return;
		}
		if(pawnShopItem.getDeleted().compareAndSet(false, true))
		{
			try
			{
				if(currency <= 0 || price <= 0 || ItemFunctions.removeItem(player, currency, price, true) < price)
				{
					pawnShopItem.getDeleted().set(false);
					player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS, new SystemMessage(1959).addNumber(price).addItemName(currency));
					return;
				}
			}
			catch(Exception e)
			{
				LOG.error("PawnShop: Can't buy pawnshop item " + pawnShopItemId, e);
				pawnShopItem.getDeleted().set(false);
				return;
			}
			PAWN_SHOP_ITEMS.remove(pawnShopItem);
			pawnShopItem.delete();
			ItemInstance item = ItemFunctions.createItem(pawnShopItem.getItemTypeId());
			item.setCount((long) pawnShopItem.getAmount());
			item.setEnchantLevel(pawnShopItem.getEnchantLevel());
			item.setVariationStat1(pawnShopItem.getVarOpt1());
			item.setVariationStat2(pawnShopItem.getVarOpt2());
			Log.LogItem(player, Log.ItemLog.TradeBuy, item);
			player.getInventory().addItem(item);
			player.sendPacket(SystemMessage2.obtainItems(item.getItemId(), item.getCount(), item.getEnchantLevel()));
			Player owner = World.getPlayer(ownerId);
			if(owner != null && owner.isOnline())
			{
				ItemFunctions.addItem(owner, currency, price, true);
				Log.LogItem(player, Log.ItemLog.TradeSell, item);
			}
			else
			{
				DelayedItemsManager.getInstance().addDelayed(ownerId, currency, (int) price, 0, "Reward for pawnshop item " + pawnShopItemId + " bought by " + player);
			}
		}
	}
	
	private static void doRefundList(Player player, NpcInstance npc, int page)
	{
		PawnShop.PawnShopItem[] pawnShopItems = getOwnerItems(player);
		StringBuilder itemsHtmlBuilder = new StringBuilder();
		NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
		html.setFile("mods/pawnshop/refund_list.htm");
		int psiIdx = Config.PAWNSHOP_ITEMS_PER_PAGE * page;
		
		for(int psiLastIdx = psiIdx + Config.PAWNSHOP_ITEMS_PER_PAGE;psiIdx < psiLastIdx && psiIdx < pawnShopItems.length;++psiIdx)
		{
			PawnShop.PawnShopItem pawnShopItem = pawnShopItems[psiIdx];
			itemsHtmlBuilder.append(itemHtml(player, "pawnshop.refund_item_element", String.format("-h scripts_services.pawnshop.PawnShop:refundItem %d %d", pawnShopItem.getId(), page), pawnShopItem.getItemTemplate(), pawnShopItem.getEnchantLevel(), pawnShopItem.getAmount(), ItemHolder.getInstance().getTemplate(pawnShopItem.getCurrencyItemId()), (long) pawnShopItem.getPrice(), pawnShopItem.getOwnerName(), pawnShopItem.getVariationSkill()));
		}
		
		html.replace("%paging%", pagingHtml(player, page, pawnShopItems, "refundList"));
		html.replace("%list%", itemsHtmlBuilder.toString());
		player.sendPacket(html);
	}
	
	private static void doRefundItem(Player player, int pawnShopItemId)
	{
		if(!player.getPlayerAccess().UseTrade)
		{
			player.sendPacket(Msg.THIS_ACCOUNT_CANOT_TRADE_ITEMS);
			return;
		}
		if(pawnShopItemId <= 0)
		{
			player.sendPacket(SystemMsg.SYSTEM_ERROR);
			return;
		}
		if(player.getWeightPenalty() >= 3 || player.getInventory().getSize() > player.getInventoryLimit() - 10)
		{
			player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return;
		}
		PawnShopItem pawnShopItem = null;
		for(PawnShopItem psi : PAWN_SHOP_ITEMS)
		{
			if(psi.getId() != pawnShopItemId)
				continue;
			pawnShopItem = psi;
		}
		if(pawnShopItem == null || !isAllowedItem(pawnShopItem))
		{
			return;
		}
		if(Config.PAWNSHOP_REFUND_ITEM_ID > 0 && Config.PAWNSHOP_REFUND_ITEM_COUNT > 0 && player.getInventory().getCountOf(Config.PAWNSHOP_REFUND_ITEM_ID) < Config.PAWNSHOP_REFUND_ITEM_COUNT)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS, new SystemMessage(1959).addNumber(Config.PAWNSHOP_REFUND_ITEM_COUNT).addItemName(Config.PAWNSHOP_REFUND_ITEM_ID));
			return;
		}
		if(pawnShopItem.getDeleted().compareAndSet(false, true))
		{
			try
			{
				if(Config.PAWNSHOP_REFUND_ITEM_ID > 0 && Config.PAWNSHOP_REFUND_ITEM_COUNT > 0 && ItemFunctions.removeItem(player, Config.PAWNSHOP_REFUND_ITEM_ID, Config.PAWNSHOP_REFUND_ITEM_COUNT, true) < Config.PAWNSHOP_REFUND_ITEM_COUNT)
				{
					pawnShopItem.getDeleted().set(false);
					player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS, new SystemMessage(1959).addNumber(Config.PAWNSHOP_REFUND_ITEM_COUNT).addItemName(Config.PAWNSHOP_REFUND_ITEM_ID));
					return;
				}
			}
			catch(Exception e)
			{
				LOG.error("PawnShop: Can't refund pawnshop item " + pawnShopItemId, e);
				pawnShopItem.getDeleted().set(false);
				return;
			}
			PAWN_SHOP_ITEMS.remove(pawnShopItem);
			pawnShopItem.delete();
			ItemInstance item = ItemFunctions.createItem(pawnShopItem.getItemTypeId());
			item.setCount((long) pawnShopItem.getAmount());
			item.setEnchantLevel(pawnShopItem.getEnchantLevel());
			item.setVariationStat1(pawnShopItem.getVarOpt1());
			item.setVariationStat2(pawnShopItem.getVarOpt2());
			Log.LogItem(player, Log.ItemLog.RefundReturn, item);
			player.getInventory().addItem(item);
			player.sendPacket(SystemMessage2.obtainItems(item.getItemId(), item.getCount(), item.getEnchantLevel()));
		}
	}
	
	private static PawnShopItem[] getOwnerItems(Player owner)
	{
		LinkedList<PawnShopItem> result = new LinkedList<>();
		for(PawnShopItem pawnShopItem : PAWN_SHOP_ITEMS)
		{
			if(pawnShopItem.getOwnerId() != owner.getObjectId() || !isAllowedItem(pawnShopItem))
				continue;
			result.add(pawnShopItem);
		}
		return result.toArray(new PawnShopItem[result.size()]);
	}
	
	private static void doSellList(Player player, NpcInstance npc, int page)
	{
		ItemInstance[] items = getSellableItems(player);
		StringBuilder itemsHtmlBuilder = new StringBuilder();
		NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
		html.setFile("mods/pawnshop/sell_list.htm");
		int itemIdx = Config.PAWNSHOP_ITEMS_PER_PAGE * page;
		
		for(int lastItemIdx = itemIdx + Config.PAWNSHOP_ITEMS_PER_PAGE;itemIdx < lastItemIdx && itemIdx < items.length;++itemIdx)
		{
			ItemInstance item = items[itemIdx];
			itemsHtmlBuilder.append(itemHtml(player, "pawnshop.sell_item_element", String.format("-h scripts_services.pawnshop.PawnShop:sellItem %d %d", item.getObjectId(), page), item.getTemplate(), item.getEnchantLevel(), (int) item.getCount(), getVarOptSkill(item.getVariationStat1(), item.getVariationStat2())));
		}
		
		html.replace("%paging%", pagingHtml(player, page, items, "sellList"));
		html.replace("%list%", itemsHtmlBuilder.toString());
		player.sendPacket(html);
	}
	
	private static void doSellItem(Player player, NpcInstance npc, int itemObjId, int page)
	{
		if(!player.getPlayerAccess().UseTrade)
		{
			player.sendPacket(Msg.THIS_ACCOUNT_CANOT_TRADE_ITEMS);
			return;
		}
		if(itemObjId <= 0)
		{
			player.sendPacket(SystemMsg.SYSTEM_ERROR);
			return;
		}
		ItemInstance[] sellableItems = getSellableItems(player);
		ItemInstance item = null;
		for(ItemInstance sellableItem : sellableItems)
		{
			if(sellableItem.getObjectId() != itemObjId)
				continue;
			item = sellableItem;
		}
		if(item == null || !isAllowedItem(item))
		{
			player.sendPacket(SystemMsg.SYSTEM_ERROR);
			return;
		}
		if(Config.PAWNSHOP_TAX_ITEM_ID > 0 && Config.PAWNSHOP_TAX_ITEM_COUNT > 0 && player.getInventory().getCountOf(Config.PAWNSHOP_TAX_ITEM_ID) < Config.PAWNSHOP_TAX_ITEM_COUNT)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS, new SystemMessage(1959).addNumber(Config.PAWNSHOP_TAX_ITEM_COUNT).addItemName(Config.PAWNSHOP_TAX_ITEM_ID));
			return;
		}
		NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
		html.setFile("mods/pawnshop/sell_item.htm");
		html.replace("%back_page%", String.valueOf(page));
		html.replace("%sell_item_name%", item.getName());
		html.replace("%currency_list%", currencysHtmlList());
		html.replace("%item%", itemHtml(player, "pawnshop.sell_item", null, item.getTemplate(), item.getEnchantLevel(), (int) item.getCount(), getVarOptSkill(item.getVariationStat1(), item.getVariationStat2())));
		html.replace("%item_obj_id%", String.valueOf(item.getObjectId()));
		player.sendPacket(html);
	}
	
	private static void doSellApply(Player player, NpcInstance npc, int itemObjId, ItemTemplate currency, int price)
	{
		if(!player.getPlayerAccess().UseTrade)
		{
			player.sendPacket(Msg.THIS_ACCOUNT_CANOT_TRADE_ITEMS);
			return;
		}
		if(itemObjId <= 0 || currency == null)
		{
			player.sendPacket(SystemMsg.SYSTEM_ERROR);
			return;
		}
		ItemInstance[] sellableItems = getSellableItems(player);
		ItemInstance item = null;
		for(ItemInstance sellableItem : sellableItems)
		{
			if(sellableItem.getObjectId() != itemObjId)
				continue;
			item = sellableItem;
		}
		if(item == null || !isAllowedItem(item) || SafeMath.mulAndCheck((long) price, item.getCount()) <= 0)
		{
			player.sendPacket(SystemMsg.SYSTEM_ERROR);
			return;
		}
		if(Config.PAWNSHOP_TAX_ITEM_ID > 0 && Config.PAWNSHOP_TAX_ITEM_COUNT > 0 && ItemFunctions.removeItem(player, Config.PAWNSHOP_TAX_ITEM_ID, Config.PAWNSHOP_TAX_ITEM_COUNT, true) < Config.PAWNSHOP_TAX_ITEM_COUNT)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS, new SystemMessage(1959).addNumber(Config.PAWNSHOP_TAX_ITEM_COUNT).addItemName(Config.PAWNSHOP_TAX_ITEM_ID));
			return;
		}
		int amount = (int) item.getCount();
		PawnShopItem psi = new PawnShopItem(LAST_ID.incrementAndGet(), player.getObjectId(), item.getItemId(), amount, item.getEnchantLevel(), currency.getItemId(), price, item.getVariationStat1(), item.getVariationStat2());
		Log.LogItem(player, Log.ItemLog.RefundSell, item);
		if(!player.getInventory().destroyItem(item))
		{
			return;
		}
		player.sendPacket(SystemMessage2.removeItems(psi.getItemTypeId(), (long) amount));
		PAWN_SHOP_ITEMS.add(0, psi);
		psi.store();
	}
	
	private static ItemInstance[] getSellableItems(Player player)
	{
		ItemInstance[] inventory = player.getInventory().getItems();
		ArrayList<ItemInstance> result = new ArrayList<>();
		for(ItemInstance item : inventory)
		{
			if(!isAllowedItem(item) || item.getOwnerId() != player.getObjectId())
				continue;
			result.add(item);
		}
		return result.toArray(new ItemInstance[result.size()]);
	}
	
	private static String stripString(String str)
	{
		return str.replaceAll("[!\"#\\$%&'\\(\\)\\*,\\-\\./:;<=>\\?@\\[\\\\\\]\\^_`{\\|}~]+", "").replaceAll("[^\\w\\d+]+", " ").trim();
	}
	
	private static boolean isAllowedItem(ItemInstance item)
	{
		if(item == null)
		{
			return false;
		}
		if(item.getLocation() != ItemInstance.ItemLocation.INVENTORY)
		{
			return false;
		}
		if(item.isWeapon() && item.getEnchantLevel() < Config.PAWNSHOP_MIN_ENCHANT_LEVEL)
		{
			return false;
		}
		return isAllowedItem(item.getTemplate());
	}
	
	private static boolean isAllowedItem(PawnShopItem pawnShopItem)
	{
		if(pawnShopItem == null || pawnShopItem.getDeleted().get())
		{
			return false;
		}
		if(!ArrayUtils.contains(Config.PAWNSHOP_CURRENCY_ITEM_IDS, pawnShopItem.getCurrencyItemId()))
		{
			return false;
		}
		return isAllowedItem(pawnShopItem.getItemTemplate());
	}
	
	private static boolean isAllowedItem(ItemTemplate item)
	{
		if(item == null)
		{
			return false;
		}
		if(item.isTemporal() || item.isShadowItem())
		{
			return false;
		}
		if(!ArrayUtils.contains(Config.PAWNSHOP_ITEMS_CLASSES, item.getItemClass()))
		{
			return false;
		}
		if(item.getItemGrade().gradeOrd() < Config.PAWNSHOP_MIN_GRADE.gradeOrd())
		{
			return false;
		}
		return !ArrayUtils.contains(Config.PAWNSHOP_PROHIBITED_ITEM_IDS, item.getItemId());
	}
	
	private static String formatOptSkillText(Skill optSkill)
	{
		if(optSkill == null)
		{
			return "";
		}
		if(optSkill.isActive())
		{
			return "Active " + optSkill.getName();
		}
		if(!optSkill.getTriggerList().isEmpty())
		{
			return "Chance " + optSkill.getName();
		}
		if(optSkill.isPassive())
		{
			return "Passive " + optSkill.getName();
		}
		return optSkill.getName();
	}
	
	private static String itemHtml(Player player, String templateAddr, String bypass, ItemTemplate item, int enchant, int amount, Skill optSkill)
	{
		String html = StringHolder.getInstance().getNotNull(player, templateAddr);
		html = html.replace("%bypass%", bypass != null ? bypass : "");
		html = html.replace("%item_id%", String.format("%d", item.getItemId()));
		html = html.replace("%item_name%", item.getName());
		html = html.replace("%item_add_name%", item.getAdditionalName());
		html = html.replace("%item_icon%", item.getIcon());
		html = html.replace("%item_enchant%", enchant > 0 ? String.format("(+%d)", enchant) : "");
		html = html.replace("%item_amount%", String.valueOf(amount));
		html = html.replace("%option%", formatOptSkillText(optSkill));
		return html;
	}
	
	private static String itemHtml(Player player, String templateAddr, String bypass, ItemTemplate item, int enchant, int amount, ItemTemplate currency, long price, String ownerName, Skill optSkill)
	{
		String html = StringHolder.getInstance().getNotNull(player, templateAddr);
		html = html.replace("%bypass%", bypass != null ? bypass : "");
		html = html.replace("%item_id%", String.valueOf(item.getItemId()));
		html = html.replace("%item_name%", item.getName());
		html = html.replace("%item_add_name%", item.getAdditionalName());
		html = html.replace("%item_icon%", item.getIcon());
		html = html.replace("%item_enchant%", enchant > 0 ? String.format("(+%d)", enchant) : "");
		html = html.replace("%item_amount%", String.valueOf(amount));
		html = html.replace("%option%", formatOptSkillText(optSkill));
		if(currency != null)
		{
			html = html.replace("%currency_id%", String.valueOf(currency.getItemId()));
			html = html.replace("%currency_name%", currency.getName());
			html = html.replace("%price%", String.valueOf(price));
		}
		else
		{
			html = html.replace("%currency_id%", "");
			html = html.replace("%currency_name%", "");
			html = html.replace("%price%", "");
		}
		html = html.replace("%owner%", ownerName != null ? ownerName : "");
		return html;
	}
	
	private static String currencysHtmlList()
	{
		StringBuilder currencysHtml = new StringBuilder();
		for(int cIdx = 0;cIdx < Config.PAWNSHOP_CURRENCY_ITEM_IDS.length;++cIdx)
		{
			ItemTemplate currencyItem = ItemHolder.getInstance().getTemplate(Config.PAWNSHOP_CURRENCY_ITEM_IDS[cIdx]);
			if(currencyItem == null)
				continue;
			if(currencysHtml.length() > 0)
			{
				currencysHtml.append(';');
			}
			currencysHtml.append(currencyItem.getName());
		}
		return currencysHtml.toString();
	}
	
	private static ItemTemplate getCurrencyItem(String s)
	{
		s = stripString(s);
		for(int cIdx = 0;cIdx < Config.PAWNSHOP_CURRENCY_ITEM_IDS.length;++cIdx)
		{
			ItemTemplate currencyItem = ItemHolder.getInstance().getTemplate(Config.PAWNSHOP_CURRENCY_ITEM_IDS[cIdx]);
			if(currencyItem == null || !s.equalsIgnoreCase(stripString(currencyItem.getName())))
				continue;
			return currencyItem;
		}
		return null;
	}
	
	private static boolean hasNextPage(int length, int page)
	{
		return (page + 1) * Config.PAWNSHOP_ITEMS_PER_PAGE < length;
	}
	
	private static String pagingHtml(Player player, int page, Object[] items, String method)
	{
		return pagingHtml(player, page, items, method, null);
	}
	
	private static String pagingHtml(Player player, int page, Object[] items, String method, String args)
	{
		String bypassFmt = "-h scripts_services.pawnshop.PawnShop:" + method + (args != null ? new StringBuilder().append(" %d ").append(args).toString() : " %d");
		return pagingHtml(player, page > 0 ? String.format(bypassFmt, page - 1).trim() : null, page, hasNextPage(items.length, page) ? String.format(bypassFmt, page + 1).trim() : null);
	}
	
	private static String pagingHtml(Player player, String prevBypass, int currPage, String nextBypass)
	{
		String html = StringHolder.getInstance().getNotNull(player, "pawnshop.paging");
		html = html.replace("%prev_button%", prevBypass != null ? "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">".replace("%prev_bypass%", prevBypass) : "");
		html = html.replace("%curr_page%", Integer.toString(currPage + 1));
		html = html.replace("%next_button%", nextBypass != null ? "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">".replace("%next_bypass%", nextBypass) : "");
		return html;
	}
	
	private static Skill getVarOptSkill(int varOpt1, int varOpt2)
	{
		Skill varOptSkill = null;
		if(varOpt1 > 0 || varOpt2 > 0)
		{
			OptionDataTemplate odt1 = OptionDataHolder.getInstance().getTemplate(varOpt1);
			OptionDataTemplate odt2 = OptionDataHolder.getInstance().getTemplate(varOpt2);
			if(odt2 != null && !odt2.getSkills().isEmpty())
			{
				varOptSkill = odt2.getSkills().get(0);
			}
			if(odt1 != null && !odt1.getSkills().isEmpty())
			{
				varOptSkill = odt1.getSkills().get(0);
			}
		}
		return varOptSkill;
	}
	
	@Override
	public void onLoad()
	{
		if(Config.PAWNSHOP_ENABLED)
		{
			loadItems();
		}
	}
	
	@Override
	public void onReload()
	{
		onShutdown();
		onLoad();
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	public void buyList()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc))
		{
			return;
		}
		doBuyList(player, npc, 0, "");
	}
	
	public void buyList(String[] args)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc))
		{
			return;
		}
		StringBuilder queryStrBuilder = new StringBuilder();
		int[] ints = {0};
		parseArgs(args, ints, queryStrBuilder);
		doBuyList(player, npc, ints[0], queryStrBuilder.toString());
	}
	
	public void buyItem(String[] args)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc))
		{
			return;
		}
		StringBuilder queryStrBuilder = new StringBuilder();
		int[] ints = {-1, 0};
		parseArgs(args, ints, queryStrBuilder);
		doBuyItem(player, ints[0]);
		doBuyList(player, npc, ints[1], queryStrBuilder.toString());
	}
	
	public void refundList()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc))
		{
			return;
		}
		doRefundList(player, npc, 0);
	}
	
	public void refundList(String[] args)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc))
		{
			return;
		}
		int[] ints = {0};
		parseArgs(args, ints);
		doRefundList(player, npc, ints[0]);
	}
	
	public void refundItem(String[] args)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc))
		{
			return;
		}
		int[] ints = {-1, 0};
		parseArgs(args, ints);
		doRefundItem(player, ints[0]);
		doRefundList(player, npc, ints[1]);
	}
	
	public void sellList()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc))
		{
			return;
		}
		doSellList(player, npc, 0);
	}
	
	public void sellList(String[] args)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc))
		{
			return;
		}
		int[] ints = {0};
		parseArgs(args, ints);
		doSellList(player, npc, ints[0]);
	}
	
	public void sellItem(String[] args)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc))
		{
			return;
		}
		int[] ints = {-1, 0};
		parseArgs(args, ints);
		doSellItem(player, npc, ints[0], ints[1]);
	}
	
	public void sellApply(String[] args)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(!Config.PAWNSHOP_ENABLED || !isAllowedNpc(player, npc))
		{
			return;
		}
		StringBuilder currSb = new StringBuilder();
		int[] ints = {0, 0, -1};
		parseArgs(args, ints, currSb);
		doSellApply(player, npc, ints[1], getCurrencyItem(currSb.toString()), ints[2]);
		doSellList(player, npc, ints[0]);
	}
	
	private static class PawnShopItem
	{
		private final int _id;
		private final int _ownerId;
		private final int _itemTypeId;
		private final int _amount;
		private final int _enchantLevel;
		private final int _currencyItemId;
		private final int _price;
		private final int _varOpt1;
		private final int _varOpt2;
		private final AtomicBoolean _deleted;
		private final Skill _optSkill;
		private String _ownerName;
		private String _nameForQuery;
		
		private PawnShopItem(int id, int ownerId, int itemTypeId, int amount, int enchantLevel, int currencyItemId, int price, int varOpt1, int varOpt2)
		{
			_id = id;
			_ownerId = ownerId;
			_itemTypeId = itemTypeId;
			_amount = amount;
			_enchantLevel = enchantLevel;
			_currencyItemId = currencyItemId;
			_price = price;
			_varOpt1 = varOpt1;
			_varOpt2 = varOpt2;
			_optSkill = getVarOptSkill(varOpt1, varOpt2);
			_deleted = new AtomicBoolean(false);
		}
		
		public AtomicBoolean getDeleted()
		{
			return _deleted;
		}
		
		public String getOwnerName()
		{
			if(_ownerName == null)
			{
				Player player = World.getPlayer(getOwnerId());
				if(player != null)
				{
					_ownerName = player.getName();
				}
				else
				{
					_ownerName = CharacterDAO.getInstance().getNameByObjectId(getOwnerId());
					if(_ownerName == null)
					{
						_ownerName = "";
					}
				}
			}
			return _ownerName;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public int getOwnerId()
		{
			return _ownerId;
		}
		
		public int getItemTypeId()
		{
			return _itemTypeId;
		}
		
		public int getAmount()
		{
			return _amount;
		}
		
		public int getEnchantLevel()
		{
			return _enchantLevel;
		}
		
		public int getCurrencyItemId()
		{
			return _currencyItemId;
		}
		
		public int getPrice()
		{
			return _price;
		}
		
		public int getVarOpt1()
		{
			return _varOpt1;
		}
		
		public int getVarOpt2()
		{
			return _varOpt2;
		}
		
		public Skill getVariationSkill()
		{
			return _optSkill;
		}
		
		public ItemTemplate getItemTemplate()
		{
			return ItemHolder.getInstance().getTemplate(getItemTypeId());
		}
		
		public String getNameForQuery()
		{
			if(_nameForQuery == null)
			{
				ItemTemplate item = getItemTemplate();
				_nameForQuery = item != null ? stripString(item.getName()) : "";
			}
			return _nameForQuery;
		}
		
		public void store()
		{
			Connection con = null;
			CallableStatement cstmt = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				cstmt = con.prepareCall("{CALL `lip_ex_PawnShopStoreItem`(?, ?, ?, ?, ?, ?, ?, ?, ?)}");
				cstmt.setInt(1, _id);
				cstmt.setInt(2, _ownerId);
				cstmt.setInt(3, _itemTypeId);
				cstmt.setInt(4, _amount);
				cstmt.setInt(5, _enchantLevel);
				cstmt.setInt(6, _currencyItemId);
				cstmt.setInt(7, _price);
				cstmt.setInt(8, _varOpt1);
				cstmt.setInt(9, _varOpt2);
				cstmt.execute();
			}
			catch(SQLException e)
			{
				LOG.error("", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, cstmt);
			}
		}
		
		public void delete()
		{
			Connection con = null;
			CallableStatement cstmt = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				cstmt = con.prepareCall("{CALL `lip_ex_PawnShopDeleteItem`(?)}");
				cstmt.setInt(1, _id);
				cstmt.execute();
			}
			catch(SQLException e)
			{
				LOG.error("", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, cstmt);
			}
			_deleted.set(true);
		}
	}
}