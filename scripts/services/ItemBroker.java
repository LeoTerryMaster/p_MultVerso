package services;

import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.data.xml.holder.RecipeHolder;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.Recipe;
import l2.gameserver.model.World;
import l2.gameserver.model.base.Element;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ManufactureItem;
import l2.gameserver.model.items.TradeItem;
import l2.gameserver.network.l2.s2c.CharInfo;
import l2.gameserver.network.l2.s2c.PrivateStoreMsgBuy;
import l2.gameserver.network.l2.s2c.PrivateStoreMsgSell;
import l2.gameserver.network.l2.s2c.RadarControl;
import l2.gameserver.network.l2.s2c.RecipeShopMsg;
import l2.gameserver.scripts.Functions;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.Util;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class ItemBroker extends Functions
{
	private static final int MAX_ITEMS_PER_PAGE = 10;
	private static final int MAX_PAGES_PER_LIST = 9;
	private static final Map<Integer, NpcInfo> _npcInfos = new ConcurrentHashMap<>();
	public int[] RARE_ITEMS = {16205, 16206, 16207, 16208, 16209, 16210, 16211, 16212, 16213, 16214, 16215, 16216, 16217, 16218, 16219, 16220, 16304, 16321, 16338, 16355};
	
	private TreeMap<String, TreeMap<Long, Item>> getItems(int type)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return null;
		}
		updateInfo(player, npc);
		NpcInfo info = _npcInfos.get(getNpc().getObjectId());
		if(info == null)
		{
			return null;
		}
		switch(type)
		{
			case 1:
			{
				return info.bestSellItems;
			}
			case 3:
			{
				return info.bestBuyItems;
			}
			case 5:
			{
				return info.bestCraftItems;
			}
		}
		return null;
	}
	
	public String DialogAppend_31732(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_31833(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_31838(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_31829(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String DialogAppend_31805(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String getHtmlAppends(Integer val)
	{
		if(!Config.ITEM_BROKER_ITEM_SEARCH)
		{
			return "";
		}
		StringBuilder append = new StringBuilder();
		int type = 0;
		String typeNameRu = "";
		String typeNameEn = "";
		switch(val)
		{
			case 0:
			{
				if(getSelf().isLangRus())
				{
					append.append("<br><font color=\"LEVEL\">Поиск торговцев:</font><br1>");
					append.append("[npc_%objectId%_Chat 11|<font color=\"FF9900\">Список продаваемых товаров</font>]<br1>");
					append.append("[npc_%objectId%_Chat 13|<font color=\"FF9900\">Список покупаемых товаров</font>]<br1>");
					append.append("[npc_%objectId%_Chat 15|<font color=\"FF9900\">Список создаваемых товаров</font>]<br1>");
					break;
				}
				append.append("<br><font color=\"LEVEL\">Search for dealers:</font><br1>");
				append.append("[npc_%objectId%_Chat 11|<font color=\"FF9900\">The list of goods for sale</font>]<br1>");
				append.append("[npc_%objectId%_Chat 13|<font color=\"FF9900\">The list of goods to buy</font>]<br1>");
				append.append("[npc_%objectId%_Chat 15|<font color=\"FF9900\">The list of goods to craft</font>]<br1>");
				break;
			}
			case 11:
			{
				type = 1;
				typeNameRu = "продаваемых";
				typeNameEn = "sell";
				break;
			}
			case 13:
			{
				type = 3;
				typeNameRu = "покупаемых";
				typeNameEn = "buy";
				break;
			}
			case 15:
			{
				type = 5;
				typeNameRu = "создаваемых";
				typeNameEn = "craft";
				break;
			}
			case 21:
			case 23:
			case 25:
			{
				type = val - 20;
				if(getSelf().isLangRus())
				{
					append.append("!Список снаряжения:<br>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 1 1 0 0|<font color=\"FF9900\">Оружие</font>]<br1>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 2 1 0 0|<font color=\"FF9900\">Броня</font>]<br1>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 3 1 0 0|<font color=\"FF9900\">Бижутерия</font>]<br1>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 4 1 0 0|<font color=\"FF9900\">Украшения</font>]<br1>");
					append.append("<br>[npc_%objectId%_Chat ").append(10 + type).append("|<font color=\"FF9900\">Назад</font>]");
				}
				else
				{
					append.append("!The list of equipment:<br>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 1 1 0 0|<font color=\"FF9900\">Weapons</font>]<br1>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 2 1 0 0|<font color=\"FF9900\">Armors</font>]<br1>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 3 1 0 0|<font color=\"FF9900\">Jewels</font>]<br1>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 4 1 0 0|<font color=\"FF9900\">Accessories</font>]<br1>");
					append.append("<br>[npc_%objectId%_Chat ").append(10 + type).append("|<font color=\"FF9900\">Back</font>]");
				}
				return append.toString();
			}
			case 31:
			case 33:
			case 35:
			{
				type = val - 30;
				if(getSelf().isLangRus())
				{
					append.append("!Список снаряжения, заточенного на +4 и выше:<br>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 1 1 4 0|<font color=\"FF9900\">Оружие</font>]<br1>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 2 1 4 0|<font color=\"FF9900\">Броня</font>]<br1>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 3 1 4 0|<font color=\"FF9900\">Бижутерия</font>]<br1>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 4 1 4 0|<font color=\"FF9900\">Украшения</font>]<br1>");
					append.append("<br>[npc_%objectId%_Chat ").append(10 + type).append("|<font color=\"FF9900\">Назад</font>]");
				}
				else
				{
					append.append("!The list of equipment, enchanted to +4 and more:<br>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 1 1 4 0|<font color=\"FF9900\">Weapons+</font>]<br1>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 2 1 4 0|<font color=\"FF9900\">Armors+</font>]<br1>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 3 1 4 0|<font color=\"FF9900\">Jewels+</font>]<br1>");
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 4 1 4 0|<font color=\"FF9900\">Accessories+</font>]<br1>");
					append.append("<br>[npc_%objectId%_Chat ").append(10 + type).append("|<font color=\"FF9900\">Back</font>]");
				}
				return append.toString();
			}
		}
		if(type > 0)
		{
			if(getSelf().isLangRus())
			{
				append.append("!Список ").append(typeNameRu).append(" товаров:<br>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 0 1 0 0|<font color=\"FF9900\">Весь список</font>]<br1>");
				append.append("[npc_%objectId%_Chat ").append(type + 20).append("|<font color=\"FF9900\">Снаряжение</font>]<br1>");
				if(type == 1)
				{
					append.append("[npc_%objectId%_Chat ").append(1 + 30).append("|<font color=\"FF9900\">Снаряжение +4 и выше</font>]<br1>");
				}
				if(type != 5)
				{
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 0 1 0 1|<font color=\"FF9900\">Редкое снаряжение</font>]<br1>");
				}
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 5 1 0 0|<font color=\"FF9900\">Расходные материалы</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 6 1 0 0|<font color=\"FF9900\">Ингредиенты</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 7 1 0 0|<font color=\"FF9900\">Ключевые ингредиенты</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 8 1 0 0|<font color=\"FF9900\">Рецепты</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 9 1 0 0|<font color=\"FF9900\">Книги и амулеты</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 10 1 0 0|<font color=\"FF9900\">Предметы для улучшения</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 11 1 0 0|<font color=\"FF9900\">Разное</font>]<br1>");
				if(type != 5)
				{
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 90 1 0 0|<font color=\"FF9900\">Стандартные предметы</font>]<br1>");
				}
				append.append("<edit var=\"tofind\" width=100><br1>");
				append.append("[scripts_services.ItemBroker:find ").append(type).append(" 1 $tofind|<font color=\"FF9900\">Найти</font>]<br1>");
				append.append("<br>[npc_%objectId%_Chat 0|<font color=\"FF9900\">Назад</font>]");
			}
			else
			{
				append.append("!The list of goods to ").append(typeNameEn).append(":<br>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 0 1 0 0|<font color=\"FF9900\">List all</font>]<br1>");
				append.append("[npc_%objectId%_Chat ").append(type + 20).append("|<font color=\"FF9900\">Equipment</font>]<br1>");
				if(type == 1)
				{
					append.append("[npc_%objectId%_Chat ").append(1 + 30).append("|<font color=\"FF9900\">Equipment +4 and more</font>]<br1>");
				}
				if(type != 5)
				{
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 0 1 0 1|<font color=\"FF9900\">Rare equipment</font>]<br1>");
				}
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 5 1 0 0|<font color=\"FF9900\">Consumable</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 6 1 0 0|<font color=\"FF9900\">Matherials</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 7 1 0 0|<font color=\"FF9900\">Key matherials</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 8 1 0 0|<font color=\"FF9900\">Recipies</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 9 1 0 0|<font color=\"FF9900\">Books and amulets</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 10 1 0 0|<font color=\"FF9900\">Enchant items</font>]<br1>");
				append.append("[scripts_services.ItemBroker:list ").append(type).append(" 11 1 0 0|<font color=\"FF9900\">Other</font>]<br1>");
				if(type != 5)
				{
					append.append("[scripts_services.ItemBroker:list ").append(type).append(" 90 1 0 0|<font color=\"FF9900\">Commons</font>]<br1>");
				}
				append.append("<edit var=\"tofind\" width=100><br1>");
				append.append("[scripts_services.ItemBroker:find ").append(type).append(" 1 $tofind|<font color=\"FF9900\">Find</font>]<br1>");
				append.append("<br>[npc_%objectId%_Chat 0|<font color=\"FF9900\">Back</font>]");
			}
		}
		return append.toString();
	}
	
	public void list(String[] var)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		if(var.length != 5)
		{
			show("Некорректная длина данных", player, npc, (Object[]) new Object[0]);
			return;
		}
		int type;
		int rare;
		int minEnchant;
		int itemType;
		int currentPage;
		try
		{
			type = Integer.valueOf(var[0]);
			itemType = Integer.valueOf(var[1]);
			currentPage = Integer.valueOf(var[2]);
			minEnchant = Integer.valueOf(var[3]);
			rare = Integer.valueOf(var[4]);
		}
		catch(Exception e)
		{
			show("Некорректные данные", player, npc, (Object[]) new Object[0]);
			return;
		}
		ItemTemplate.ItemClass itemClass = itemType >= ItemTemplate.ItemClass.values().length ? null : ItemTemplate.ItemClass.values()[itemType];
		TreeMap<String, TreeMap<Long, Item>> allItems = getItems(type);
		if(allItems == null)
		{
			show("Ошибка - предметов такого типа не найдено", player, npc, (Object[]) new Object[0]);
			return;
		}
		ArrayList<Item> items = new ArrayList<>(allItems.size() * 10);
		for(TreeMap<Long, Item> tempItems : allItems.values())
		{
			TreeMap<Long, Item> tempItems2 = new TreeMap<>();
			for(Map.Entry<Long, Item> entry : tempItems.entrySet())
			{
				ItemTemplate temp;
				Item tempItem = entry.getValue();
				if(tempItem == null || tempItem.enchant < minEnchant || (temp = tempItem.item != null ? tempItem.item.getItem() : ItemHolder.getInstance().getTemplate(tempItem.itemId)) == null || rare > 0 && !tempItem.rare || itemClass != null && itemClass != ItemTemplate.ItemClass.ALL && temp.getItemClass() != itemClass)
					continue;
				tempItems2.put(entry.getKey(), tempItem);
			}
			Item item;
			if(tempItems2.isEmpty() || (item = type == 3 ? tempItems2.lastEntry().getValue() : tempItems2.firstEntry().getValue()) == null)
				continue;
			items.add(item);
		}
		StringBuilder out = new StringBuilder(200);
		out.append("[npc_%objectId%_Chat 1");
		out.append(type);
		out.append("|««]&nbsp;&nbsp;");
		int totalPages = items.size();
		totalPages = totalPages / 10 + (totalPages % 10 > 0 ? 1 : 0);
		totalPages = Math.max(1, totalPages);
		currentPage = Math.min(totalPages, Math.max(1, currentPage));
		if(totalPages > 1)
		{
			int page = Math.max(1, Math.min(totalPages - 9 + 1, currentPage - 4));
			if(page > 1)
			{
				listPageNum(out, type, itemType, 1, minEnchant, rare, "1");
			}
			if(currentPage > 11)
			{
				listPageNum(out, type, itemType, currentPage - 10, minEnchant, rare, String.valueOf(currentPage - 10));
			}
			if(currentPage > 1)
			{
				listPageNum(out, type, itemType, currentPage - 1, minEnchant, rare, "<");
			}
			for(int count = 0;count < 9 && page <= totalPages;++count, ++page)
			{
				if(page == currentPage)
				{
					out.append(page).append("&nbsp;");
					continue;
				}
				listPageNum(out, type, itemType, page, minEnchant, rare, String.valueOf(page));
			}
			if(currentPage < totalPages)
			{
				listPageNum(out, type, itemType, currentPage + 1, minEnchant, rare, ">");
			}
			if(currentPage < totalPages - 10)
			{
				listPageNum(out, type, itemType, currentPage + 10, minEnchant, rare, String.valueOf(currentPage + 10));
			}
			if(page <= totalPages)
			{
				listPageNum(out, type, itemType, totalPages, minEnchant, rare, String.valueOf(totalPages));
			}
		}
		out.append("<table width=100%>");
		if(items.size() > 0)
		{
			int count = 0;
			ListIterator iter = items.listIterator((currentPage - 1) * 10);
			while(iter.hasNext() && count < 10)
			{
				ItemTemplate temp;
				Item item = (Item) iter.next();
				ItemTemplate itemTemplate = temp = item.item != null ? item.item.getItem() : ItemHolder.getInstance().getTemplate(item.itemId);
				if(temp == null)
					continue;
				out.append("<tr><td>");
				out.append(temp.getIcon32());
				out.append("</td><td><table width=100%><tr><td>[scripts_services.ItemBroker:listForItem ");
				out.append(type);
				out.append(" ");
				out.append(item.itemId);
				out.append(" ");
				out.append(minEnchant);
				out.append(" ");
				out.append(rare);
				out.append(" ");
				out.append(itemType);
				out.append(" 1 ");
				out.append(currentPage);
				out.append("|");
				out.append(item.name);
				out.append("</td></tr><tr><td>price: ");
				out.append(Util.formatAdena(item.price));
				if(temp.isStackable())
				{
					out.append(", count: ").append(Util.formatAdena(item.count));
				}
				out.append("</td></tr></table></td></tr>");
				++count;
			}
		}
		else if(getSelf().isLangRus())
		{
			out.append("<tr><td colspan=2>Ничего не найдено.</td></tr>");
		}
		else
		{
			out.append("<tr><td colspan=2>Nothing found.</td></tr>");
		}
		out.append("</table><br>&nbsp;");
		show(out.toString(), player, npc, (Object[]) new Object[0]);
	}
	
	private void listPageNum(StringBuilder out, int type, int itemType, int page, int minEnchant, int rare, String letter)
	{
		out.append("[scripts_services.ItemBroker:list ");
		out.append(type);
		out.append(" ");
		out.append(itemType);
		out.append(" ");
		out.append(page);
		out.append(" ");
		out.append(minEnchant);
		out.append(" ");
		out.append(rare);
		out.append("|");
		out.append(letter);
		out.append("]&nbsp;");
	}
	
	public void listForItem(String[] var)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		if(var.length < 7 || var.length > 12)
		{
			show("Некорректная длина данных", player, npc, (Object[]) new Object[0]);
			return;
		}
		String[] search = null;
		int itemType;
		int minEnchant;
		int currentPage;
		int itemId;
		int rare;
		int type;
		int returnPage;
		try
		{
			type = Integer.valueOf(var[0]);
			itemId = Integer.valueOf(var[1]);
			minEnchant = Integer.valueOf(var[2]);
			rare = Integer.valueOf(var[3]);
			itemType = Integer.valueOf(var[4]);
			currentPage = Integer.valueOf(var[5]);
			returnPage = Integer.valueOf(var[6]);
			if(var.length > 7)
			{
				search = new String[var.length - 7];
				System.arraycopy(var, 7, search, 0, search.length);
			}
		}
		catch(Exception e)
		{
			show("Некорректные данные", player, npc, (Object[]) new Object[0]);
			return;
		}
		ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
		if(template == null)
		{
			show("Ошибка - itemId не определен.", player, npc, (Object[]) new Object[0]);
			return;
		}
		TreeMap<String, TreeMap<Long, Item>> tmpItems = getItems(type);
		if(tmpItems == null)
		{
			show("Ошибка - такой тип предмета отсутствует.", player, npc, (Object[]) new Object[0]);
			return;
		}
		NavigableMap allItems = tmpItems.get(template.getName());
		if(allItems == null)
		{
			show("Ошибка - предметов с таким названием не найдено.", player, npc, (Object[]) new Object[0]);
			return;
		}
		StringBuilder out = new StringBuilder(200);
		if(search == null)
		{
			listPageNum(out, type, itemType, returnPage, minEnchant, rare, "««");
		}
		else
		{
			findPageNum(out, type, returnPage, search, "««");
		}
		out.append("&nbsp;&nbsp;");
		Map<Long, Item> sortedItems;
		Map map = sortedItems = type == 3 ? allItems.descendingMap() : allItems;
		if(sortedItems == null)
		{
			show("Ошибка - ничего не найдено.", player, npc, (Object[]) new Object[0]);
			return;
		}
		ArrayList<Item> items = new ArrayList<>(sortedItems.size());
		for(Item item : sortedItems.values())
		{
			if(item == null || item.enchant < minEnchant || rare > 0 && !item.rare)
				continue;
			items.add(item);
		}
		int totalPages = items.size();
		totalPages = totalPages / 10 + (totalPages % 10 > 0 ? 1 : 0);
		totalPages = Math.max(1, totalPages);
		currentPage = Math.min(totalPages, Math.max(1, currentPage));
		if(totalPages > 1)
		{
			int page = Math.max(1, Math.min(totalPages - 9 + 1, currentPage - 4));
			if(page > 1)
			{
				listForItemPageNum(out, type, itemId, minEnchant, rare, itemType, 1, returnPage, search, "1");
			}
			if(currentPage > 11)
			{
				listForItemPageNum(out, type, itemId, minEnchant, rare, itemType, currentPage - 10, returnPage, search, String.valueOf(currentPage - 10));
			}
			if(currentPage > 1)
			{
				listForItemPageNum(out, type, itemId, minEnchant, rare, itemType, currentPage - 1, returnPage, search, "<");
			}
			for(int count = 0;count < 9 && page <= totalPages;++count, ++page)
			{
				if(page == currentPage)
				{
					out.append(page).append("&nbsp;");
					continue;
				}
				listForItemPageNum(out, type, itemId, minEnchant, rare, itemType, page, returnPage, search, String.valueOf(page));
			}
			if(currentPage < totalPages)
			{
				listForItemPageNum(out, type, itemId, minEnchant, rare, itemType, currentPage + 1, returnPage, search, ">");
			}
			if(currentPage < totalPages - 10)
			{
				listForItemPageNum(out, type, itemId, minEnchant, rare, itemType, currentPage + 10, returnPage, search, String.valueOf(currentPage + 10));
			}
			if(page <= totalPages)
			{
				listForItemPageNum(out, type, itemId, minEnchant, rare, itemType, totalPages, returnPage, search, String.valueOf(totalPages));
			}
		}
		out.append("<table width=100%>");
		if(items.size() > 0)
		{
			int count = 0;
			ListIterator iter = items.listIterator((currentPage - 1) * 10);
			while(iter.hasNext() && count < 10)
			{
				ItemTemplate temp;
				Item item = (Item) iter.next();
				ItemTemplate itemTemplate = temp = item.item != null ? item.item.getItem() : ItemHolder.getInstance().getTemplate(item.itemId);
				if(temp == null)
					continue;
				out.append("<tr><td>");
				out.append(temp.getIcon32());
				out.append("</td><td><table width=100%><tr><td>[scripts_services.ItemBroker:path ");
				out.append(type);
				out.append(" ");
				out.append(item.itemId);
				out.append(" ");
				out.append(item.itemObjId);
				out.append("|");
				out.append(item.name);
				out.append("</td></tr><tr><td>price: ");
				out.append(Util.formatAdena(item.price));
				if(temp.isStackable())
				{
					out.append(", count: ").append(Util.formatAdena(item.count));
				}
				out.append(", owner: ").append(item.merchantName);
				out.append("</td></tr></table></td></tr>");
				++count;
			}
		}
		else if(getSelf().isLangRus())
		{
			out.append("<tr><td colspan=2>Ничего не найдено.</td></tr>");
		}
		else
		{
			out.append("<tr><td colspan=2>Nothing found.</td></tr>");
		}
		out.append("</table><br>&nbsp;");
		show(out.toString(), player, npc, (Object[]) new Object[0]);
	}
	
	private void listForItemPageNum(StringBuilder out, int type, int itemId, int minEnchant, int rare, int itemType, int page, int returnPage, String[] search, String letter)
	{
		out.append("[scripts_services.ItemBroker:listForItem ");
		out.append(type);
		out.append(" ");
		out.append(itemId);
		out.append(" ");
		out.append(minEnchant);
		out.append(" ");
		out.append(rare);
		out.append(" ");
		out.append(itemType);
		out.append(" ");
		out.append(page);
		out.append(" ");
		out.append(returnPage);
		if(search != null)
		{
			for(int i = 0;i < search.length;++i)
			{
				out.append(" ");
				out.append(search[i]);
			}
		}
		out.append("|");
		out.append(letter);
		out.append("]&nbsp;");
	}
	
	public void path(String[] var)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		if(var.length != 3)
		{
			show("Некорректная длина данных", player, npc, (Object[]) new Object[0]);
			return;
		}
		int itemObjId;
		int type;
		int itemId;
		try
		{
			type = Integer.valueOf(var[0]);
			itemId = Integer.valueOf(var[1]);
			itemObjId = Integer.valueOf(var[2]);
		}
		catch(Exception e)
		{
			show("Некорректные данные", player, npc, (Object[]) new Object[0]);
			return;
		}
		ItemTemplate temp = ItemHolder.getInstance().getTemplate(itemId);
		if(temp == null)
		{
			show("Ошибка - itemId не определен.", player, npc, (Object[]) new Object[0]);
			return;
		}
		TreeMap<String, TreeMap<Long, Item>> allItems = getItems(type);
		if(allItems == null)
		{
			show("Ошибка - предметов такого типа не найдено.", player, npc, (Object[]) new Object[0]);
			return;
		}
		TreeMap<Long, Item> items = allItems.get(temp.getName());
		if(items == null)
		{
			show("Ошибка - предметов с таким именем не найдено.", player, npc, (Object[]) new Object[0]);
			return;
		}
		Item item = null;
		for(Item i : items.values())
		{
			if(i.itemObjId != itemObjId)
				continue;
			item = i;
			break;
		}
		if(item == null)
		{
			show("Ошибка - предмет не найден.", player, npc, (Object[]) new Object[0]);
			return;
		}
		Player trader = GameObjectsStorage.getAsPlayer(item.merchantStoredId);
		if(trader == null)
		{
			show("Торговец не найден, возможно он вышел из игры.", player, npc, (Object[]) new Object[0]);
			return;
		}
		boolean found = false;
		block:
		switch(type)
		{
			case 1:
			{
				if(trader.getSellList() == null)
					break;
				for(TradeItem tradeItem : trader.getSellList())
				{
					if(tradeItem.getItemId() != item.itemId || tradeItem.getOwnersPrice() != item.price)
						continue;
					found = true;
					break block;
				}
				break;
			}
			case 3:
			{
				if(trader.getBuyList() == null)
					break;
				for(TradeItem tradeItem : trader.getBuyList())
				{
					if(tradeItem.getItemId() != item.itemId || tradeItem.getOwnersPrice() != item.price)
						continue;
					found = true;
					break block;
				}
				break;
			}
			case 5:
			{
				found = true;
			}
		}
		if(!found)
		{
			if(player.isLangRus())
			{
				show("Внимание, цена или предмет изменились, будьте осторожны !", player, npc, (Object[]) new Object[0]);
			}
			else
			{
				show("Caution, price or item was changed, please be careful !", player, npc, (Object[]) new Object[0]);
			}
		}
		RadarControl rc = new RadarControl(0, 1, item.player);
		player.sendPacket(rc);
		if(player.getVarB("notraders"))
		{
			player.sendPacket(new CharInfo(trader));
			if(trader.getPrivateStoreType() == 3)
			{
				player.sendPacket(new PrivateStoreMsgBuy(trader));
			}
			else if(trader.getPrivateStoreType() == 1 || trader.getPrivateStoreType() == 8)
			{
				player.sendPacket(new PrivateStoreMsgSell(trader));
			}
			else if(trader.getPrivateStoreType() == 5)
			{
				player.sendPacket(new RecipeShopMsg(trader));
			}
		}
		player.setTarget(trader);
	}
	
	public void updateInfo(Player player, NpcInstance npc)
	{
		NpcInfo info = _npcInfos.get(npc.getObjectId());
		if(info == null || info.lastUpdate < System.currentTimeMillis() - Config.ITEM_BROKER_UPDATE_TIME)
		{
			info = new NpcInfo();
			info.lastUpdate = System.currentTimeMillis();
			info.bestBuyItems = new TreeMap();
			info.bestSellItems = new TreeMap();
			info.bestCraftItems = new TreeMap();
			int itemObjId = 0;
			block5:
			for(Player pl : World.getAroundPlayers(npc, 4000, 400))
			{
				int type = pl.getPrivateStoreType();
				if(type != 1 && type != 3 && type != 5)
					continue;
				TreeMap<String, TreeMap<Long, Item>> items;
				Item newItem;
				TreeMap oldItems;
				ItemTemplate temp;
				long key;
				switch(type)
				{
					case 1:
					{
						items = info.bestSellItems;
						List<TradeItem> tradeList = pl.getSellList();
						for(TradeItem item : tradeList)
						{
							temp = item.getItem();
							if(temp == null)
								continue;
							oldItems = items.get(temp.getName());
							if(oldItems == null)
							{
								oldItems = new TreeMap();
								items.put(temp.getName(), oldItems);
							}
							newItem = new Item(item.getItemId(), type, item.getOwnersPrice(), item.getCount(), item.getEnchantLevel(), temp.getName(), pl.getStoredId(), pl.getName(), pl.getLoc(), item.getObjectId(), item);
							for(key = newItem.price * 100;key < newItem.price * 100 + 100 && oldItems.containsKey(key);++key)
							{
							}
							oldItems.put(key, newItem);
						}
						continue block5;
					}
					case 3:
					{
						items = info.bestBuyItems;
						List<TradeItem> tradeList = pl.getBuyList();
						for(TradeItem item : tradeList)
						{
							temp = item.getItem();
							if(temp == null)
								continue;
							oldItems = items.get(temp.getName());
							if(oldItems == null)
							{
								oldItems = new TreeMap();
								items.put(temp.getName(), oldItems);
							}
							newItem = new Item(item.getItemId(), type, item.getOwnersPrice(), item.getCount(), item.getEnchantLevel(), temp.getName(), pl.getStoredId(), pl.getName(), pl.getLoc(), itemObjId++, item);
							for(key = newItem.price * 100;key < newItem.price * 100 + 100 && oldItems.containsKey(key);++key)
							{
							}
							oldItems.put(key, newItem);
						}
						continue block5;
					}
					case 5:
					{
						items = info.bestCraftItems;
						List<ManufactureItem> createList = pl.getCreateList();
						if(createList == null)
							continue block5;
						for(ManufactureItem mitem : createList)
						{
							int recipeId = mitem.getRecipeId();
							Recipe recipe = RecipeHolder.getInstance().getRecipeById(recipeId);
							if(recipe == null)
								continue;
							for(Pair product : recipe.getProducts())
							{
								ItemTemplate temp2 = (ItemTemplate) product.getKey();
								if(temp2 == null)
									continue;
								TreeMap oldItems2 = items.get(temp2.getName());
								if(oldItems2 == null)
								{
									oldItems2 = new TreeMap();
									items.put(temp2.getName(), oldItems2);
								}
								Item newItem2 = new Item(((ItemTemplate) product.getKey()).getItemId(), type, mitem.getCost(), (Long) product.getValue(), 0, temp2.getName(), pl.getStoredId(), pl.getName(), pl.getLoc(), itemObjId++, null);
								long key2;
								for(key2 = newItem2.price * 100;key2 < newItem2.price * 100 + 100 && oldItems2.containsKey(key2);++key2)
								{
								}
								oldItems2.put(key2, newItem2);
							}
						}
						continue block5;
					}
					default:
					{
						continue block5;
					}
				}
			}
			_npcInfos.put(npc.getObjectId(), info);
		}
	}
	
	public void find(String[] var)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		if(var.length < 3 || var.length > 7)
		{
			if(player.isLangRus())
			{
				show("Пожалуйста введите от 1 до 16 символов.<br>[npc_%objectId%_Chat 0|<font color=\"FF9900\">Назад</font>]", player, npc, (Object[]) new Object[0]);
			}
			else
			{
				show("Please enter from 1 up to 16 symbols.<br>[npc_%objectId%_Chat 0|<font color=\"FF9900\">Back</font>]", player, npc, (Object[]) new Object[0]);
			}
			return;
		}
		int minEnchant = 0;
		String[] search;
		int type;
		int currentPage;
		try
		{
			type = Integer.valueOf(var[0]);
			currentPage = Integer.valueOf(var[1]);
			search = new String[var.length - 2];
			for(int i = 0;i < search.length;++i)
			{
				String line;
				search[i] = line = var[i + 2].trim().toLowerCase();
				if(line.length() <= 1 || !line.startsWith("+"))
					continue;
				minEnchant = Integer.valueOf(line.substring(1));
			}
		}
		catch(Exception e)
		{
			show("Некорректные данные", player, npc, (Object[]) new Object[0]);
			return;
		}
		TreeMap<String, TreeMap<Long, Item>> allItems = getItems(type);
		if(allItems == null)
		{
			show("Ошибка - предметов с таким типом не найдено.", player, npc, (Object[]) new Object[0]);
			return;
		}
		ArrayList<Object> items = new ArrayList<>();
		Item item;
		block3:
		for(Map.Entry<String, TreeMap<Long, Item>> entry : allItems.entrySet())
		{
			for(int i = 0;i < search.length;++i)
			{
				String line = search[i];
				if(!line.startsWith("+") && entry.getKey().toLowerCase().indexOf(line) == -1)
					continue block3;
			}
			TreeMap<Long, Item> itemMap = entry.getValue();
			item = null;
			for(Item itm : itemMap.values())
			{
				if(itm == null || itm.enchant < minEnchant)
					continue;
				item = itm;
				break;
			}
			if(item == null)
				continue;
			items.add(item);
		}
		StringBuilder out = new StringBuilder(200);
		out.append("[npc_%objectId%_Chat 1");
		out.append(type);
		out.append("|««]&nbsp;&nbsp;");
		int totalPages = items.size();
		totalPages = totalPages / 10 + (totalPages % 10 > 0 ? 1 : 0);
		totalPages = Math.max(1, totalPages);
		currentPage = Math.min(totalPages, Math.max(1, currentPage));
		if(totalPages > 1)
		{
			int page = Math.max(1, Math.min(totalPages - 9 + 1, currentPage - 4));
			if(page > 1)
			{
				findPageNum(out, type, 1, search, "1");
			}
			if(currentPage > 11)
			{
				findPageNum(out, type, currentPage - 10, search, String.valueOf(currentPage - 10));
			}
			if(currentPage > 1)
			{
				findPageNum(out, type, currentPage - 1, search, "<");
			}
			for(int count = 0;count < 9 && page <= totalPages;++count, ++page)
			{
				if(page == currentPage)
				{
					out.append(page).append("&nbsp;");
					continue;
				}
				findPageNum(out, type, page, search, String.valueOf(page));
			}
			if(currentPage < totalPages)
			{
				findPageNum(out, type, currentPage + 1, search, ">");
			}
			if(currentPage < totalPages - 10)
			{
				findPageNum(out, type, currentPage + 10, search, String.valueOf(currentPage + 10));
			}
			if(page <= totalPages)
			{
				findPageNum(out, type, totalPages, search, String.valueOf(totalPages));
			}
		}
		out.append("<table width=100%>");
		if(items.size() > 0)
		{
			int count = 0;
			ListIterator iter = items.listIterator((currentPage - 1) * 10);
			while(iter.hasNext() && count < 10)
			{
				item = (Item) iter.next();
				ItemTemplate temp;
				ItemTemplate itemTemplate = temp = item.item != null ? item.item.getItem() : ItemHolder.getInstance().getTemplate(item.itemId);
				if(temp == null)
					continue;
				out.append("<tr><td>");
				out.append(temp.getIcon32());
				out.append("</td><td><table width=100%><tr><td>[scripts_services.ItemBroker:listForItem ");
				out.append(type);
				out.append(" ");
				out.append(item.itemId);
				out.append(" ");
				out.append(minEnchant);
				out.append(" 0 0 1 ");
				out.append(currentPage);
				if(search != null)
				{
					for(int i = 0;i < search.length;++i)
					{
						out.append(" ");
						out.append(search[i]);
					}
				}
				out.append("|");
				out.append("<font color=\"LEVEL\">");
				out.append(temp.getName());
				out.append("</font>]");
				out.append("</td></tr>");
				out.append("</table></td></tr>");
				++count;
			}
		}
		else if(getSelf().isLangRus())
		{
			out.append("<tr><td colspan=2>Ничего не найдено.</td></tr>");
		}
		else
		{
			out.append("<tr><td colspan=2>Nothing found.</td></tr>");
		}
		out.append("</table><br>&nbsp;");
		show(out.toString(), player, npc, (Object[]) new Object[0]);
	}
	
	private void findPageNum(StringBuilder out, int type, int page, String[] search, String letter)
	{
		out.append("[scripts_services.ItemBroker:find ");
		out.append(type);
		out.append(" ");
		out.append(page);
		if(search != null)
		{
			for(int i = 0;i < search.length;++i)
			{
				out.append(" ");
				out.append(search[i]);
			}
		}
		out.append("|");
		out.append(letter);
		out.append("]&nbsp;");
	}
	
	public class Item
	{
		public final int itemId;
		public final int itemObjId;
		public final int type;
		public final long price;
		public final long count;
		public final int enchant;
		public final boolean rare;
		public final long merchantStoredId;
		public final String name;
		public final String merchantName;
		public final Location player;
		public final TradeItem item;
		
		public Item(int itemId, int type, long price, long count, int enchant, String itemName, long storedId, String merchantName, Location player, int itemObjId, TradeItem item)
		{
			this.itemId = itemId;
			this.type = type;
			this.price = price;
			this.count = count;
			this.enchant = enchant;
			rare = ArrayUtils.contains(RARE_ITEMS, itemId);
			StringBuilder out = new StringBuilder(70);
			if(enchant > 0)
			{
				if(rare)
				{
					out.append("<font color=\"FF0000\">+");
				}
				else
				{
					out.append("<font color=\"7CFC00\">+");
				}
				out.append(enchant);
				out.append(" ");
			}
			else if(rare)
			{
				out.append("<font color=\"0000FF\">Rare ");
			}
			else
			{
				out.append("<font color=\"LEVEL\">");
			}
			out.append(itemName);
			out.append("</font>]");
			if(item != null)
			{
				if(item.getAttackElement() != Element.NONE.getId())
				{
					out.append(" &nbsp;<font color=\"7CFC00\">+");
					out.append(item.getAttackElementValue());
					switch(item.getAttackElement())
					{
						case 0:
						{
							out.append(" Fire");
							break;
						}
						case 1:
						{
							out.append(" Water");
							break;
						}
						case 2:
						{
							out.append(" Wind");
							break;
						}
						case 3:
						{
							out.append(" Earth");
							break;
						}
						case 4:
						{
							out.append(" Holy");
							break;
						}
						case 5:
						{
							out.append(" Unholy");
						}
					}
					out.append("</font>");
				}
				else
				{
					int wind;
					int holy;
					int unholy;
					int water;
					int earth;
					int fire = item.getDefenceFire();
					if(fire + (water = item.getDefenceWater()) + (wind = item.getDefenceWind()) + (earth = item.getDefenceEarth()) + (holy = item.getDefenceHoly()) + (unholy = item.getDefenceUnholy()) > 0)
					{
						out.append("&nbsp;<font color=\"7CFC00\">");
						if(fire > 0)
						{
							out.append("+");
							out.append(fire);
							out.append(" Fire ");
						}
						if(water > 0)
						{
							out.append("+");
							out.append(water);
							out.append(" Water ");
						}
						if(wind > 0)
						{
							out.append("+");
							out.append(wind);
							out.append(" Wind ");
						}
						if(earth > 0)
						{
							out.append("+");
							out.append(earth);
							out.append(" Earth ");
						}
						if(holy > 0)
						{
							out.append("+");
							out.append(holy);
							out.append(" Holy ");
						}
						if(unholy > 0)
						{
							out.append("+");
							out.append(unholy);
							out.append(" Unholy ");
						}
						out.append("</font>");
					}
				}
			}
			name = out.toString();
			merchantStoredId = storedId;
			this.merchantName = merchantName;
			this.player = player;
			this.itemObjId = itemObjId;
			this.item = item;
		}
	}
	
	public class NpcInfo
	{
		public long lastUpdate;
		public TreeMap<String, TreeMap<Long, Item>> bestSellItems;
		public TreeMap<String, TreeMap<Long, Item>> bestBuyItems;
		public TreeMap<String, TreeMap<Long, Item>> bestCraftItems;
	}
}