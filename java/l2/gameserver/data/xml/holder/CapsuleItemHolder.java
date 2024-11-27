package l2.gameserver.data.xml.holder;

import l2.commons.data.xml.AbstractHolder;
import l2.commons.util.RandomUtils;
import l2.commons.util.Rnd;
import l2.gameserver.handler.items.IItemHandler;
import l2.gameserver.handler.items.ItemHandler;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.InventoryUpdate;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.Location;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CapsuleItemHolder extends AbstractHolder
{
	private static final Logger LOG = LoggerFactory.getLogger(CapsuleItemHolder.class);
	private static final CapsuleItemHolder INSTANCE = new CapsuleItemHolder();
	private final Map<Integer, List<CapsuledItem>> _capsuleItems = new HashMap<>();
	private final Map<Integer, Pair<Integer, Long>> _capsuleRequiredItems = new HashMap<>();
	private CapsuleItemsHandler _itemsHandler;
	
	public static CapsuleItemHolder getInstance()
	{
		return INSTANCE;
	}
	
	public Pair<Integer, Long> getCapsuleRequiredItems(int capsuleItemId)
	{
		return _capsuleRequiredItems.get(capsuleItemId);
	}
	
	public List<CapsuledItem> getCapsuledItems(int capsuleItemId)
	{
		return _capsuleItems.get(capsuleItemId);
	}
	
	public void add(int itemId, List<CapsuledItem> capsuledItems_)
	{
		add(itemId, null, capsuledItems_);
	}
	
	public void add(int itemId, Pair<Integer, Long> requiredItem, List<CapsuledItem> capsuledItems_)
	{
		if(_capsuleItems.containsKey(itemId))
		{
			LOG.warn("Capsule item " + itemId + " already defined.");
		}
		_capsuleItems.put(itemId, Collections.unmodifiableList(capsuledItems_));
		if(requiredItem != null)
		{
			_capsuleRequiredItems.put(itemId, requiredItem);
		}
	}
	
	@Override
	public int size()
	{
		return _capsuleItems.size();
	}
	
	@Override
	public void clear()
	{
		ItemHandler.getInstance().unregisterItemHandler(_itemsHandler);
		_capsuleItems.clear();
	}
	
	@Override
	protected void process()
	{
		int[] itemIds = new int[_capsuleItems.size()];
		Iterator<Integer> capsuleItemIdIt = _capsuleItems.keySet().iterator();
		int i = 0;
		while(capsuleItemIdIt.hasNext())
		{
			itemIds[i] = capsuleItemIdIt.next();
			++i;
		}
		_itemsHandler = new CapsuleItemsHandler(itemIds);
		ItemHandler.getInstance().registerItemHandler(_itemsHandler);
	}
	
	public static class CapsuledItem
	{
		private final int _itemId;
		private final long _min;
		private final long _max;
		private final double _chance;
		private final int _minEnchant;
		private final int _maxEnchant;
		
		public CapsuledItem(int itemId, long min, long max, double chance, int minEnchant, int maxEnchant)
		{
			_itemId = itemId;
			_min = min;
			_max = max;
			_chance = chance;
			_minEnchant = minEnchant;
			_maxEnchant = maxEnchant;
		}
		
		public int getItemId()
		{
			return _itemId;
		}
		
		public double getChance()
		{
			return _chance;
		}
		
		public long getMax()
		{
			return _max;
		}
		
		public long getMin()
		{
			return _min;
		}
		
		public int getMinEnchant()
		{
			return _minEnchant;
		}
		
		public int getMaxEnchant()
		{
			return _maxEnchant;
		}
	}
	
	public static class CapsuleItemsHandler implements IItemHandler
	{
		private final int[] _capsuleItemIds;
		
		public CapsuleItemsHandler(int[] capsuleItemIds)
		{
			_capsuleItemIds = capsuleItemIds;
		}
		
		private static List<ItemInstance> addItem(Playable playable, int itemId, long count)
		{
			if(playable == null || count < 1)
			{
				return Collections.emptyList();
			}
			Playable player = playable.isSummon() ? playable.getPlayer() : playable;
			LinkedList<ItemInstance> result = new LinkedList<>();
			ItemTemplate t = ItemHolder.getInstance().getTemplate(itemId);
			if(t.isStackable())
			{
				result.add(player.getInventory().addItem(itemId, count));
			}
			else
			{
				for(long i = 0;i < count;++i)
				{
					result.add(player.getInventory().addItem(itemId, 1));
				}
			}
			player.sendPacket(SystemMessage2.obtainItems(itemId, count, 0));
			return result;
		}
		
		@Override
		public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
		{
			int itemId = item.getItemId();
			List<CapsuledItem> capsuledItems = getInstance().getCapsuledItems(itemId);
			if(capsuledItems == null)
			{
				return false;
			}
			Pair<Integer, Long> reqiredItem = getInstance().getCapsuleRequiredItems(itemId);
			if(reqiredItem != null && reqiredItem.getKey() > 0 && reqiredItem.getValue() > 0 && !playable.getInventory().destroyItemByItemId(reqiredItem.getKey(), reqiredItem.getValue()))
			{
				playable.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return false;
			}
			if(!playable.getInventory().destroyItem(item, 1))
			{
				playable.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return false;
			}
			playable.sendPacket(new SystemMessage(47).addItemName(item.getItemId()));
			ArrayList chancedItems = new ArrayList();
			for(CapsuledItem capsuledItem : capsuledItems)
			{
				if(capsuledItem.getChance() == 100.0)
				{
					long count = capsuledItem.getMax() > capsuledItem.getMin() ? Rnd.get(capsuledItem.getMin(), capsuledItem.getMax()) : capsuledItem.getMin();
					List<ItemInstance> addedItems = addItem(playable, capsuledItem.getItemId(), count);
					for(ItemInstance addedItem : addedItems)
					{
						if(!addedItem.canBeEnchanted(true))
							continue;
						if(capsuledItem.getMaxEnchant() > capsuledItem.getMinEnchant())
						{
							addedItem.setEnchantLevel(Rnd.get(capsuledItem.getMinEnchant(), capsuledItem.getMaxEnchant()));
						}
						else
						{
							addedItem.setEnchantLevel(capsuledItem.getMinEnchant());
						}
						playable.sendPacket(new InventoryUpdate().addModifiedItem(addedItem));
					}
					continue;
				}
				chancedItems.add(Pair.of((Object) capsuledItem, (Object) capsuledItem.getChance()));
			}
			if(!chancedItems.isEmpty())
			{
				Collections.sort(chancedItems, RandomUtils.DOUBLE_GROUP_COMPARATOR);
				CapsuledItem capsuledItem = (CapsuledItem) RandomUtils.pickRandomSortedGroup(chancedItems, 100.0);
				if(capsuledItem != null)
				{
					long count = capsuledItem.getMax() > capsuledItem.getMin() ? Rnd.get(capsuledItem.getMin(), capsuledItem.getMax()) : capsuledItem.getMin();
					List<ItemInstance> addedItems = addItem(playable, capsuledItem.getItemId(), count);
					for(ItemInstance addedItem : addedItems)
					{
						if(!addedItem.canBeEnchanted(true))
							continue;
						if(capsuledItem.getMaxEnchant() > capsuledItem.getMinEnchant())
						{
							addedItem.setEnchantLevel(Rnd.get(capsuledItem.getMinEnchant(), capsuledItem.getMaxEnchant()));
						}
						else
						{
							addedItem.setEnchantLevel(capsuledItem.getMinEnchant());
						}
						playable.sendPacket(new InventoryUpdate().addModifiedItem(addedItem));
					}
				}
			}
			return true;
		}
		
		@Override
		public void dropItem(Player player, ItemInstance item, long count, Location loc)
		{
			IItemHandler.NULL.dropItem(player, item, count, loc);
		}
		
		@Override
		public boolean pickupItem(Playable playable, ItemInstance item)
		{
			return IItemHandler.NULL.pickupItem(playable, item);
		}
		
		@Override
		public int[] getItemIds()
		{
			return _capsuleItemIds;
		}
	}
}