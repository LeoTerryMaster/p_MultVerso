package l2.gameserver.data.xml.holder;

import l2.gameserver.Config;
import l2.gameserver.model.items.TradeItem;
import l2.gameserver.templates.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuyListHolder
{
	private static final Logger _log = LoggerFactory.getLogger(BuyListHolder.class);
	private static BuyListHolder _instance;
	private final Map<Integer, NpcTradeList> _lists = new HashMap<>();
	
	private BuyListHolder()
	{
		try
		{
			File filelists = new File(Config.DATAPACK_ROOT, "data/merchant_filelists.xml");
			DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
			factory1.setValidating(false);
			factory1.setIgnoringComments(true);
			Document doc1 = factory1.newDocumentBuilder().parse(filelists);
			int counterFiles = 0;
			int counterItems = 0;
			for(Node n1 = doc1.getFirstChild();n1 != null;n1 = n1.getNextSibling())
			{
				if(!"list".equalsIgnoreCase(n1.getNodeName()))
					continue;
				for(Node d1 = n1.getFirstChild();d1 != null;d1 = d1.getNextSibling())
				{
					if(!"file".equalsIgnoreCase(d1.getNodeName()))
						continue;
					String filename = d1.getAttributes().getNamedItem("name").getNodeValue();
					File file = new File(Config.DATAPACK_ROOT, "data/" + filename);
					DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
					factory2.setValidating(false);
					factory2.setIgnoringComments(true);
					Document doc2 = factory2.newDocumentBuilder().parse(file);
					++counterFiles;
					for(Node n2 = doc2.getFirstChild();n2 != null;n2 = n2.getNextSibling())
					{
						if(!"list".equalsIgnoreCase(n2.getNodeName()))
							continue;
						for(Node d2 = n2.getFirstChild();d2 != null;d2 = d2.getNextSibling())
						{
							if(!"tradelist".equalsIgnoreCase(d2.getNodeName()))
								continue;
							String[] npcs = d2.getAttributes().getNamedItem("npc").getNodeValue().split(";");
							String[] shopIds = d2.getAttributes().getNamedItem("shop").getNodeValue().split(";");
							String[] markups = {};
							boolean haveMarkups = false;
							if(d2.getAttributes().getNamedItem("markup") != null)
							{
								markups = d2.getAttributes().getNamedItem("markup").getNodeValue().split(";");
								haveMarkups = true;
							}
							int size = npcs.length;
							if(!haveMarkups)
							{
								markups = new String[size];
								for(int i = 0;i < size;++i)
								{
									markups[i] = "0";
								}
							}
							if(shopIds.length != size || markups.length != size)
							{
								_log.warn("Do not correspond to the size of arrays");
								continue;
							}
							for(int n = 0;n < size;++n)
							{
								int npc_id = Integer.parseInt(npcs[n]);
								int shop_id = Integer.parseInt(shopIds[n]);
								double markup = npc_id > 0 ? 1.0 + Double.parseDouble(markups[n]) / 100.0 : 0.0;
								NpcTradeList tl = new NpcTradeList(shop_id);
								tl.setNpcId(npc_id);
								for(Node i = d2.getFirstChild();i != null;i = i.getNextSibling())
								{
									if(!"item".equalsIgnoreCase(i.getNodeName()))
										continue;
									int itemId = Integer.parseInt(i.getAttributes().getNamedItem("id").getNodeValue());
									ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
									if(template == null)
									{
										_log.warn("Template not found for itemId: " + itemId + " for shop " + shop_id);
										continue;
									}
									if(!checkItem(template))
										continue;
									++counterItems;
									long price = i.getAttributes().getNamedItem("price") != null ? Long.parseLong(i.getAttributes().getNamedItem("price").getNodeValue()) : Math.round((double) template.getReferencePrice() * markup);
									TradeItem item = new TradeItem();
									item.setItemId(itemId);
									int itemCount = i.getAttributes().getNamedItem("count") != null ? Integer.parseInt(i.getAttributes().getNamedItem("count").getNodeValue()) : 0;
									int itemRechargeTime = i.getAttributes().getNamedItem("time") != null ? Integer.parseInt(i.getAttributes().getNamedItem("time").getNodeValue()) : 0;
									item.setOwnersPrice(price);
									item.setCount(itemCount);
									item.setCurrentValue(itemCount);
									item.setLastRechargeTime((int) (System.currentTimeMillis() / 60000));
									item.setRechargeTime(itemRechargeTime);
									tl.addItem(item);
								}
								_lists.put(shop_id, tl);
							}
						}
					}
				}
			}
			_log.info("TradeController: Loaded " + counterFiles + " file(s).");
			_log.info("TradeController: Loaded " + counterItems + " Items.");
			_log.info("TradeController: Loaded " + _lists.size() + " Buylists.");
		}
		catch(Exception e)
		{
			_log.warn("TradeController: Buylists could not be initialized.");
			_log.error("", e);
		}
	}
	
	public static BuyListHolder getInstance()
	{
		if(_instance == null)
		{
			_instance = new BuyListHolder();
		}
		return _instance;
	}
	
	public static void reload()
	{
		_instance = new BuyListHolder();
	}
	
	public boolean checkItem(ItemTemplate template)
	{
		if(template.isEquipment() && !template.isForPet() && Config.ALT_SHOP_PRICE_LIMITS.length > 0)
		{
			for(int i = 0;i < Config.ALT_SHOP_PRICE_LIMITS.length;i += 2)
			{
				if(template.getBodyPart() != Config.ALT_SHOP_PRICE_LIMITS[i])
					continue;
				if(template.getReferencePrice() <= Config.ALT_SHOP_PRICE_LIMITS[i + 1])
					break;
				return false;
			}
		}
		if(Config.ALT_SHOP_UNALLOWED_ITEMS.length > 0)
		{
			for(int i : Config.ALT_SHOP_UNALLOWED_ITEMS)
			{
				if(template.getItemId() != i)
					continue;
				return false;
			}
		}
		return true;
	}
	
	public NpcTradeList getBuyList(int listId)
	{
		return _lists.get(listId);
	}
	
	public void addToBuyList(int listId, NpcTradeList list)
	{
		_lists.put(listId, list);
	}
	
	public static class NpcTradeList
	{
		private final List<TradeItem> tradeList = new ArrayList<>();
		private final int _id;
		private int _npcId;
		
		public NpcTradeList(int id)
		{
			_id = id;
		}
		
		public int getListId()
		{
			return _id;
		}
		
		public int getNpcId()
		{
			return _npcId;
		}
		
		public void setNpcId(int id)
		{
			_npcId = id;
		}
		
		public void addItem(TradeItem ti)
		{
			tradeList.add(ti);
		}
		
		public synchronized List<TradeItem> getItems()
		{
			ArrayList<TradeItem> result = new ArrayList<>();
			long currentTime = System.currentTimeMillis() / 60000;
			for(TradeItem ti : tradeList)
			{
				if(ti.isCountLimited())
				{
					if(ti.getCurrentValue() < ti.getCount() && (long) (ti.getLastRechargeTime() + ti.getRechargeTime()) <= currentTime)
					{
						ti.setLastRechargeTime(ti.getLastRechargeTime() + ti.getRechargeTime());
						ti.setCurrentValue(ti.getCount());
					}
					if(ti.getCurrentValue() == 0)
						continue;
				}
				result.add(ti);
			}
			return result;
		}
		
		public TradeItem getItemByItemId(int itemId)
		{
			for(TradeItem ti : tradeList)
			{
				if(ti.getItemId() != itemId)
					continue;
				return ti;
			}
			return null;
		}
		
		public synchronized void updateItems(List<TradeItem> buyList)
		{
			for(TradeItem ti : buyList)
			{
				TradeItem ic = getItemByItemId(ti.getItemId());
				if(!ic.isCountLimited())
					continue;
				ic.setCurrentValue(Math.max(ic.getCurrentValue() - ti.getCount(), 0));
			}
		}
	}
}