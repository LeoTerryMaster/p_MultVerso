package l2.gameserver.data.xml.holder;

import gnu.trove.TIntObjectHashMap;
import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.MultiSellEntry;
import l2.gameserver.model.base.MultiSellIngredient;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.MultiSellList;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.XMLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MultiSellHolder
{
	private static final Logger _log = LoggerFactory.getLogger(MultiSellHolder.class);
	private static final MultiSellHolder _instance = new MultiSellHolder();
	private static final String NODE_PRODUCTION = "production";
	private static final String NODE_INGRIDIENT = "ingredient";
	private final TIntObjectHashMap<MultiSellListContainer> entries = new TIntObjectHashMap();
	
	public MultiSellHolder()
	{
		parseData();
	}
	
	public static MultiSellHolder getInstance()
	{
		return _instance;
	}
	
	private static long[] parseItemIdAndCount(String s)
	{
		if(s == null || s.isEmpty())
		{
			return null;
		}
		String[] a = s.split(":");
		try
		{
			long id = Integer.parseInt(a[0]);
			long count = a.length > 1 ? Long.parseLong(a[1]) : 1;
			return new long[] {id, count};
		}
		catch(Exception e)
		{
			_log.error("", e);
			return null;
		}
	}
	
	public static MultiSellEntry parseEntryFromStr(String s)
	{
		if(s == null || s.isEmpty())
		{
			return null;
		}
		String[] a = s.split("->");
		if(a.length != 2)
		{
			return null;
		}
		long[] ingredient = parseItemIdAndCount(a[0]);
		long[] production;
		if(ingredient == null || (production = parseItemIdAndCount(a[1])) == null)
		{
			return null;
		}
		MultiSellEntry entry = new MultiSellEntry();
		entry.addIngredient(new MultiSellIngredient((int) ingredient[0], ingredient[1]));
		entry.addProduct(new MultiSellIngredient((int) production[0], production[1]));
		return entry;
	}
	
	public MultiSellListContainer getList(int id)
	{
		return entries.get(id);
	}
	
	public void reload()
	{
		parseData();
	}
	
	private void parseData()
	{
		entries.clear();
		parse();
	}
	
	private void hashFiles(String dirname, List<File> hash)
	{
		File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
		if(!dir.exists())
		{
			_log.info("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		File[] files = dir.listFiles();
		for(File f : files)
		{
			if(f.getName().endsWith(".xml"))
			{
				hash.add(f);
				continue;
			}
			if(!f.isDirectory() || f.getName().equals(".svn"))
				continue;
			hashFiles(dirname + "/" + f.getName(), hash);
		}
	}
	
	public void addMultiSellListContainer(int id, MultiSellListContainer list)
	{
		if(entries.containsKey(id))
		{
			_log.warn("MultiSell redefined: " + id);
		}
		list.setListId(id);
		entries.put(id, list);
	}
	
	public MultiSellListContainer remove(String s)
	{
		return remove(new File(s));
	}
	
	public MultiSellListContainer remove(File f)
	{
		return remove(Integer.parseInt(f.getName().replaceAll(".xml", "")));
	}
	
	public MultiSellListContainer remove(int id)
	{
		return entries.remove(id);
	}
	
	public void parseFile(File f)
	{
		int id;
		try
		{
			id = Integer.parseInt(f.getName().replaceAll(".xml", ""));
		}
		catch(Exception e)
		{
			_log.error("Error loading file " + f, e);
			return;
		}
		Document doc;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(f);
		}
		catch(Exception e)
		{
			_log.error("Error loading file " + f, e);
			return;
		}
		try
		{
			addMultiSellListContainer(id, parseDocument(doc, id));
		}
		catch(Exception e)
		{
			_log.error("Error in file " + f, e);
		}
	}
	
	private void parse()
	{
		ArrayList<File> files = new ArrayList<>();
		hashFiles("multisell", files);
		for(File f : files)
		{
			parseFile(f);
		}
	}
	
	protected MultiSellListContainer parseDocument(Document doc, int id)
	{
		MultiSellListContainer list = new MultiSellListContainer();
		int entId = 1;
		for(Node n = doc.getFirstChild();n != null;n = n.getNextSibling())
		{
			if(!"list".equalsIgnoreCase(n.getNodeName()))
				continue;
			for(Node d = n.getFirstChild();d != null;d = d.getNextSibling())
			{
				if("item".equalsIgnoreCase(d.getNodeName()))
				{
					MultiSellEntry e = parseEntry(d, id);
					if(e == null)
						continue;
					e.setEntryId(entId++);
					list.addEntry(e);
					continue;
				}
				if(!"config".equalsIgnoreCase(d.getNodeName()))
					continue;
				list.setShowAll(XMLUtil.getAttributeBooleanValue(d, "showall", true));
				list.setNoTax(XMLUtil.getAttributeBooleanValue(d, "notax", false));
				list.setKeepEnchant(XMLUtil.getAttributeBooleanValue(d, "keepenchanted", false));
				list.setNoKey(XMLUtil.getAttributeBooleanValue(d, "nokey", false));
			}
		}
		return list;
	}
	
	protected MultiSellEntry parseEntry(Node n, int multiSellId)
	{
		MultiSellEntry entry = new MultiSellEntry();
		for(Node d = n.getFirstChild();d != null;d = d.getNextSibling())
		{
			MultiSellIngredient mi;
			int id;
			long count;
			if("ingredient".equalsIgnoreCase(d.getNodeName()))
			{
				id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
				count = Long.parseLong(d.getAttributes().getNamedItem("count").getNodeValue());
				mi = new MultiSellIngredient(id, count);
				if(d.getAttributes().getNamedItem("enchant") != null)
				{
					mi.setItemEnchant(Integer.parseInt(d.getAttributes().getNamedItem("enchant").getNodeValue()));
				}
				if(d.getAttributes().getNamedItem("mantainIngredient") != null)
				{
					mi.setMantainIngredient(Boolean.parseBoolean(d.getAttributes().getNamedItem("mantainIngredient").getNodeValue()));
				}
				if(d.getAttributes().getNamedItem("fireAttr") != null)
				{
					mi.getItemAttributes().setFire(Integer.parseInt(d.getAttributes().getNamedItem("fireAttr").getNodeValue()));
				}
				if(d.getAttributes().getNamedItem("waterAttr") != null)
				{
					mi.getItemAttributes().setWater(Integer.parseInt(d.getAttributes().getNamedItem("waterAttr").getNodeValue()));
				}
				if(d.getAttributes().getNamedItem("earthAttr") != null)
				{
					mi.getItemAttributes().setEarth(Integer.parseInt(d.getAttributes().getNamedItem("earthAttr").getNodeValue()));
				}
				if(d.getAttributes().getNamedItem("windAttr") != null)
				{
					mi.getItemAttributes().setWind(Integer.parseInt(d.getAttributes().getNamedItem("windAttr").getNodeValue()));
				}
				if(d.getAttributes().getNamedItem("holyAttr") != null)
				{
					mi.getItemAttributes().setHoly(Integer.parseInt(d.getAttributes().getNamedItem("holyAttr").getNodeValue()));
				}
				if(d.getAttributes().getNamedItem("unholyAttr") != null)
				{
					mi.getItemAttributes().setUnholy(Integer.parseInt(d.getAttributes().getNamedItem("unholyAttr").getNodeValue()));
				}
				entry.addIngredient(mi);
				continue;
			}
			if(!"production".equalsIgnoreCase(d.getNodeName()))
				continue;
			id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
			count = Long.parseLong(d.getAttributes().getNamedItem("count").getNodeValue());
			mi = new MultiSellIngredient(id, count);
			if(d.getAttributes().getNamedItem("enchant") != null)
			{
				mi.setItemEnchant(Integer.parseInt(d.getAttributes().getNamedItem("enchant").getNodeValue()));
			}
			if(d.getAttributes().getNamedItem("fireAttr") != null)
			{
				mi.getItemAttributes().setFire(Integer.parseInt(d.getAttributes().getNamedItem("fireAttr").getNodeValue()));
			}
			if(d.getAttributes().getNamedItem("waterAttr") != null)
			{
				mi.getItemAttributes().setWater(Integer.parseInt(d.getAttributes().getNamedItem("waterAttr").getNodeValue()));
			}
			if(d.getAttributes().getNamedItem("earthAttr") != null)
			{
				mi.getItemAttributes().setEarth(Integer.parseInt(d.getAttributes().getNamedItem("earthAttr").getNodeValue()));
			}
			if(d.getAttributes().getNamedItem("windAttr") != null)
			{
				mi.getItemAttributes().setWind(Integer.parseInt(d.getAttributes().getNamedItem("windAttr").getNodeValue()));
			}
			if(d.getAttributes().getNamedItem("holyAttr") != null)
			{
				mi.getItemAttributes().setHoly(Integer.parseInt(d.getAttributes().getNamedItem("holyAttr").getNodeValue()));
			}
			if(d.getAttributes().getNamedItem("unholyAttr") != null)
			{
				mi.getItemAttributes().setUnholy(Integer.parseInt(d.getAttributes().getNamedItem("unholyAttr").getNodeValue()));
			}
			ItemTemplate item;
			if(!Config.ALT_ALLOW_SHADOW_WEAPONS && id > 0 && (item = ItemHolder.getInstance().getTemplate(id)) != null && item.isShadowItem() && item.isWeapon() && !Config.ALT_ALLOW_SHADOW_WEAPONS)
			{
				return null;
			}
			entry.addProduct(mi);
		}
		if(entry.getIngredients().isEmpty() || entry.getProduction().isEmpty())
		{
			_log.warn("MultiSell [" + multiSellId + "] is empty!");
			return null;
		}
		if(entry.getIngredients().size() == 1 && entry.getProduction().size() == 1 && entry.getIngredients().get(0).getItemId() == 57)
		{
			ItemTemplate item = ItemHolder.getInstance().getTemplate(entry.getProduction().get(0).getItemId());
			if(item == null)
			{
				_log.warn("MultiSell [" + multiSellId + "] Production [" + entry.getProduction().get(0).getItemId() + "] not found!");
				return null;
			}
			long refPrice = entry.getProduction().get(0).getItemCount() * (long) item.getReferencePrice();
			if(refPrice > entry.getIngredients().get(0).getItemCount())
			{
				_log.warn("MultiSell [" + multiSellId + "] Production '" + item.getName() + "' [" + entry.getProduction().get(0).getItemId() + "] price is lower than referenced | " + refPrice + " > " + entry.getIngredients().get(0).getItemCount());
			}
		}
		return entry;
	}
	
	public void SeparateAndSend(int listId, Player player, double taxRate)
	{
		for(int i : Config.ALT_DISABLED_MULTISELL)
		{
			if(i != listId)
				continue;
			player.sendMessage(new CustomMessage("common.Disabled", player));
			return;
		}
		MultiSellListContainer list = getList(listId);
		if(list == null)
		{
			player.sendMessage(new CustomMessage("common.Disabled", player));
			return;
		}
		SeparateAndSend(list, player, taxRate);
	}
	
	public void SeparateAndSend(MultiSellListContainer list, Player player, double taxRate)
	{
		list = generateMultiSell(list, player, taxRate);
		MultiSellListContainer temp = new MultiSellListContainer();
		temp.setListId(list.getListId());
		player.setMultisell(list);
		int page = 1;
		for(MultiSellEntry e : list.getEntries())
		{
			if(temp.getEntries().size() == Config.MULTISELL_SIZE)
			{
				player.sendPacket(new MultiSellList(temp, page, 0));
				++page;
				temp = new MultiSellListContainer();
				temp.setListId(list.getListId());
			}
			temp.addEntry(e);
		}
		player.sendPacket(new MultiSellList(temp, page, 1));
	}
	
	private MultiSellListContainer generateMultiSell(MultiSellListContainer container, Player player, double taxRate)
	{
		MultiSellListContainer list = new MultiSellListContainer();
		list.setListId(container.getListId());
		boolean enchant = container.isKeepEnchant();
		boolean notax = container.isNoTax();
		boolean showall = container.isShowAll();
		boolean nokey = container.isNoKey();
		list.setShowAll(showall);
		list.setKeepEnchant(enchant);
		list.setNoTax(notax);
		list.setNoKey(nokey);
		ItemInstance[] items = player.getInventory().getItems();
		for(MultiSellEntry origEntry : container.getEntries())
		{
			List<MultiSellIngredient> ingridients;
			MultiSellEntry ent = origEntry.clone();
			if(!notax && taxRate > 0.0)
			{
				ingridients = new ArrayList<>(ent.getIngredients().size() + 1);
				long adena = 0;
				double tax = 0.0;
				for(MultiSellIngredient i : ent.getIngredients())
				{
					if(i.getItemId() == 57)
					{
						adena += i.getItemCount();
						tax += (double) i.getItemCount() * taxRate;
						continue;
					}
					ingridients.add(i);
					if(i.getItemId() == -200)
					{
						tax += (double) (i.getItemCount() / 120 * 1000) * taxRate * 100.0;
					}
					ItemTemplate item;
					if(i.getItemId() < 1 || !(item = ItemHolder.getInstance().getTemplate(i.getItemId())).isStackable())
						continue;
					tax += (double) ((long) item.getReferencePrice() * i.getItemCount()) * taxRate;
				}
				if((adena = Math.round((double) adena + tax)) > 0)
				{
					ingridients.add(new MultiSellIngredient(57, adena));
				}
				ent.setTax(Math.round(tax));
				ent.getIngredients().clear();
				ent.getIngredients().addAll(ingridients);
			}
			else
			{
				ingridients = ent.getIngredients();
			}
			if(showall)
			{
				list.entries.add(ent);
				continue;
			}
			ArrayList<Integer> itms = new ArrayList<>();
			block2:
			for(MultiSellIngredient ingredient : ingridients)
			{
				ItemTemplate template;
				ItemTemplate itemTemplate = template = ingredient.getItemId() <= 0 ? null : ItemHolder.getInstance().getTemplate(ingredient.getItemId());
				if(ingredient.getItemId() > 0 && !nokey && !template.isEquipment() || ingredient.getItemId() == 12374)
					continue;
				if(ingredient.getItemId() == -200)
				{
					if(itms.contains(ingredient.getItemId()) || player.getClan() == null || (long) player.getClan().getReputationScore() < ingredient.getItemCount())
						continue;
					itms.add(ingredient.getItemId());
					continue;
				}
				if(ingredient.getItemId() == -100)
				{
					if(itms.contains(ingredient.getItemId()) || (long) player.getPcBangPoints() < ingredient.getItemCount())
						continue;
					itms.add(ingredient.getItemId());
					continue;
				}
				for(ItemInstance item : items)
				{
					if(item.getItemId() != ingredient.getItemId() || !item.canBeExchanged(player) || itms.contains(enchant ? (long) ingredient.getItemId() + (long) ingredient.getItemEnchant() * 100000 : (long) ingredient.getItemId()) || item.getEnchantLevel() < ingredient.getItemEnchant())
						continue;
					if(item.isStackable() && item.getCount() < ingredient.getItemCount())
						continue block2;
					itms.add(enchant ? ingredient.getItemId() + ingredient.getItemEnchant() * 100000 : ingredient.getItemId());
					MultiSellEntry possibleEntry = new MultiSellEntry(enchant ? ent.getEntryId() + item.getEnchantLevel() * 100000 : ent.getEntryId());
					for(MultiSellIngredient p : ent.getProduction())
					{
						if(enchant && template.canBeEnchanted(true))
						{
							p.setItemEnchant(item.getEnchantLevel());
							p.setItemAttributes(item.getAttributes().clone());
						}
						possibleEntry.addProduct(p);
					}
					for(MultiSellIngredient ig : ingridients)
					{
						if(enchant && ig.getItemId() > 0 && ItemHolder.getInstance().getTemplate(ig.getItemId()).canBeEnchanted(true))
						{
							ig.setItemEnchant(item.getEnchantLevel());
							ig.setItemAttributes(item.getAttributes().clone());
						}
						possibleEntry.addIngredient(ig);
					}
					list.entries.add(possibleEntry);
					continue block2;
				}
			}
		}
		return list;
	}
	
	public static class MultiSellListContainer
	{
		private final List<MultiSellEntry> entries = new ArrayList<>();
		private int _listId;
		private boolean _showall = true;
		private boolean keep_enchanted;
		private boolean is_dutyfree;
		private boolean nokey;
		
		public int getListId()
		{
			return _listId;
		}
		
		public void setListId(int listId)
		{
			_listId = listId;
		}
		
		public boolean isShowAll()
		{
			return _showall;
		}
		
		public void setShowAll(boolean bool)
		{
			_showall = bool;
		}
		
		public boolean isNoTax()
		{
			return is_dutyfree;
		}
		
		public void setNoTax(boolean bool)
		{
			is_dutyfree = bool;
		}
		
		public boolean isNoKey()
		{
			return nokey;
		}
		
		public void setNoKey(boolean bool)
		{
			nokey = bool;
		}
		
		public boolean isKeepEnchant()
		{
			return keep_enchanted;
		}
		
		public void setKeepEnchant(boolean bool)
		{
			keep_enchanted = bool;
		}
		
		public void addEntry(MultiSellEntry e)
		{
			entries.add(e);
		}
		
		public List<MultiSellEntry> getEntries()
		{
			return entries;
		}
		
		public boolean isEmpty()
		{
			return entries.isEmpty();
		}
	}
}