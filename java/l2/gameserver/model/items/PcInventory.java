package l2.gameserver.model.items;

import l2.commons.collections.CollectionUtils;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.instancemanager.CursedWeaponsManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.listeners.AccessoryListener;
import l2.gameserver.model.items.listeners.ArmorSetListener;
import l2.gameserver.model.items.listeners.BowListener;
import l2.gameserver.model.items.listeners.ItemAugmentationListener;
import l2.gameserver.model.items.listeners.ItemEnchantOptionsListener;
import l2.gameserver.model.items.listeners.ItemSkillsListener;
import l2.gameserver.network.l2.s2c.ExBR_AgathionEnergyInfo;
import l2.gameserver.network.l2.s2c.InventoryUpdate;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.taskmanager.DelayedItemsManager;
import l2.gameserver.templates.item.EtcItemTemplate;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collection;

public class PcInventory extends Inventory
{
	private static final int[][] arrows = {{17}, {1341, 22067}, {1342, 22068}, {1343, 22069}, {1344, 22070}, {1345, 22071}};
	private final Player _owner;
	public boolean isRefresh;
	private LockType _lockType = LockType.NONE;
	private int[] _lockItems = ArrayUtils.EMPTY_INT_ARRAY;
	
	public PcInventory(Player owner)
	{
		super(owner.getObjectId());
		_owner = owner;
		addListener(ItemSkillsListener.getInstance());
		addListener(ItemAugmentationListener.getInstance());
		addListener(ItemEnchantOptionsListener.getInstance());
		addListener(ArmorSetListener.getInstance());
		addListener(BowListener.getInstance());
		addListener(AccessoryListener.getInstance());
	}
	
	@Override
	public Player getActor()
	{
		return _owner;
	}
	
	@Override
	protected ItemInstance.ItemLocation getBaseLocation()
	{
		return ItemInstance.ItemLocation.INVENTORY;
	}
	
	@Override
	protected ItemInstance.ItemLocation getEquipLocation()
	{
		return ItemInstance.ItemLocation.PAPERDOLL;
	}
	
	public long getAdena()
	{
		ItemInstance _adena = getItemByItemId(57);
		if(_adena == null)
		{
			return 0;
		}
		return _adena.getCount();
	}
	
	public ItemInstance addAdena(long amount)
	{
		return addItem(57, amount);
	}
	
	public boolean reduceAdena(long adena)
	{
		return destroyItemByItemId(57, adena);
	}
	
	public int getPaperdollAugmentationId(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if(item != null && item.isAugmented())
		{
			return item.getVariationStat1() & 65535 | item.getVariationStat2() << 16;
		}
		return 0;
	}
	
	@Override
	protected void onRefreshWeight()
	{
		getActor().refreshOverloaded();
	}
	
	public void validateItems()
	{
		for(ItemInstance item : _paperdoll)
		{
			if(item == null || ItemFunctions.checkIfCanEquip(getActor(), item) == null && item.getTemplate().testCondition(getActor(), item, true))
				continue;
			unEquipItem(item);
			getActor().sendDisarmMessage(item);
		}
	}
	
	public void validateItemsSkills()
	{
		for(ItemInstance item : _paperdoll)
		{
			if(item == null || item.getTemplate().getType2() != 0)
				continue;
			boolean needUnequipSkills;
			boolean bl = needUnequipSkills = getActor().getGradePenalty() > 0;
			boolean has;
			if(item.getTemplate().getAttachedSkills().length > 0)
			{
				boolean bl2 = has = getActor().getSkillLevel(item.getTemplate().getAttachedSkills()[0].getId()) > 0;
				if(needUnequipSkills && has)
				{
					ItemSkillsListener.getInstance().onUnequip(item.getEquipSlot(), item, getActor());
					continue;
				}
				if(needUnequipSkills || has)
					continue;
				ItemSkillsListener.getInstance().onEquip(item.getEquipSlot(), item, getActor());
				continue;
			}
			if(item.getTemplate().getEnchant4Skill() != null)
			{
				boolean bl3 = has = getActor().getSkillLevel(item.getTemplate().getEnchant4Skill().getId()) > 0;
				if(needUnequipSkills && has)
				{
					ItemSkillsListener.getInstance().onUnequip(item.getEquipSlot(), item, getActor());
					continue;
				}
				if(needUnequipSkills || has)
					continue;
				ItemSkillsListener.getInstance().onEquip(item.getEquipSlot(), item, getActor());
				continue;
			}
			if(item.getTemplate().getTriggerList().isEmpty())
				continue;
			if(needUnequipSkills)
			{
				ItemSkillsListener.getInstance().onUnequip(item.getEquipSlot(), item, getActor());
				continue;
			}
			ItemSkillsListener.getInstance().onEquip(item.getEquipSlot(), item, getActor());
		}
	}
	
	public void refreshEquip()
	{
		isRefresh = true;
		for(ItemInstance item : getItems())
		{
			if(item.isEquipped())
			{
				int slot = item.getEquipSlot();
				_listeners.onUnequip(slot, item);
				_listeners.onEquip(slot, item);
				continue;
			}
			if(item.getItemType() != EtcItemTemplate.EtcItemType.RUNE)
				continue;
			_listeners.onUnequip(-1, item);
			_listeners.onEquip(-1, item);
		}
		isRefresh = false;
	}
	
	public void sort(int[][] order)
	{
		boolean needSort = false;
		for(int[] element : order)
		{
			ItemInstance item = getItemByObjectId(element[0]);
			if(item == null || item.getLocation() != ItemInstance.ItemLocation.INVENTORY || item.getLocData() == element[1])
				continue;
			item.setLocData(element[1]);
			needSort = true;
		}
		if(needSort)
		{
			CollectionUtils.eqSort(_items, Inventory.ItemOrderComparator.getInstance());
		}
	}
	
	public ItemInstance findArrowForBow(ItemTemplate bow)
	{
		int[] arrowsId = arrows[bow.getCrystalType().gradeOrd()];
		for(int id : arrowsId)
		{
			ItemInstance ret = getItemByItemId(id);
			if(ret == null)
				continue;
			return ret;
		}
		return null;
	}
	
	public ItemInstance findEquippedLure()
	{
		int last_lure = 0;
		Player owner = getActor();
		String LastLure = owner.getVar("LastLure");
		if(LastLure != null && !LastLure.isEmpty())
		{
			last_lure = Integer.valueOf(LastLure);
		}
		ItemInstance res = null;
		for(ItemInstance temp : getItems())
		{
			if(temp.getItemType() != EtcItemTemplate.EtcItemType.BAIT)
				continue;
			if(temp.getLocation() == ItemInstance.ItemLocation.PAPERDOLL && temp.getEquipSlot() == 8)
			{
				return temp;
			}
			if(last_lure <= 0 || res != null || temp.getObjectId() != last_lure)
				continue;
			res = temp;
		}
		return res;
	}
	
	public void lockItems(LockType lock, int[] items)
	{
		if(_lockType != LockType.NONE)
		{
			return;
		}
		_lockType = lock;
		_lockItems = items;
		getActor().sendItemList(false);
	}
	
	public void unlock()
	{
		if(_lockType == LockType.NONE)
		{
			return;
		}
		_lockType = LockType.NONE;
		_lockItems = ArrayUtils.EMPTY_INT_ARRAY;
		getActor().sendItemList(false);
	}
	
	public boolean isLockedItem(ItemInstance item)
	{
		switch(_lockType)
		{
			case INCLUDE:
			{
				return ArrayUtils.contains(_lockItems, item.getItemId());
			}
			case EXCLUDE:
			{
				return !ArrayUtils.contains(_lockItems, item.getItemId());
			}
		}
		return false;
	}
	
	public LockType getLockType()
	{
		return _lockType;
	}
	
	public int[] getLockItems()
	{
		return _lockItems;
	}
	
	@Override
	protected void onRestoreItem(ItemInstance item)
	{
		super.onRestoreItem(item);
		if(item.getItemType() == EtcItemTemplate.EtcItemType.RUNE)
		{
			_listeners.onEquip(-1, item);
		}
		if(item.isTemporalItem())
		{
			item.startTimer(new LifeTimeTask(item));
		}
		if(item.isCursed())
		{
			CursedWeaponsManager.getInstance().checkPlayer(getActor(), item);
		}
	}
	
	@Override
	protected void onAddItem(ItemInstance item)
	{
		super.onAddItem(item);
		if(item.getItemType() == EtcItemTemplate.EtcItemType.RUNE)
		{
			_listeners.onEquip(-1, item);
		}
		if(item.isTemporalItem())
		{
			item.startTimer(new LifeTimeTask(item));
		}
		if(item.isCursed())
		{
			CursedWeaponsManager.getInstance().checkPlayer(getActor(), item);
		}
	}
	
	@Override
	protected void onRemoveItem(ItemInstance item)
	{
		super.onRemoveItem(item);
		getActor().removeItemFromShortCut(item.getObjectId());
		if(item.getItemType() == EtcItemTemplate.EtcItemType.RUNE)
		{
			_listeners.onUnequip(-1, item);
		}
		if(item.isTemporalItem())
		{
			item.stopTimer();
		}
	}
	
	@Override
	protected void onEquip(int slot, ItemInstance item)
	{
		super.onEquip(slot, item);
		if(item.isShadowItem())
		{
			item.startTimer(new ShadowLifeTimeTask(item));
		}
	}
	
	@Override
	protected void onUnequip(int slot, ItemInstance item)
	{
		super.onUnequip(slot, item);
		if(item.isShadowItem())
		{
			item.stopTimer();
		}
	}
	
	@Override
	public void restore()
	{
		int ownerId = getOwnerId();
		writeLock();
		try
		{
			Collection<ItemInstance> items = _itemsDAO.loadItemsByOwnerIdAndLoc(ownerId, getBaseLocation());
			for(ItemInstance item : items)
			{
				_items.add(item);
				onRestoreItem(item);
			}
			CollectionUtils.eqSort(_items, Inventory.ItemOrderComparator.getInstance());
			items = _itemsDAO.loadItemsByOwnerIdAndLoc(ownerId, getEquipLocation());
			for(ItemInstance item : items)
			{
				_items.add(item);
				onRestoreItem(item);
				if(item.getEquipSlot() >= 17)
				{
					item.setLocation(getBaseLocation());
					item.setLocData(0);
					item.setEquipped(false);
					continue;
				}
				setPaperdollItem(item.getEquipSlot(), item);
			}
		}
		finally
		{
			writeUnlock();
		}
		DelayedItemsManager.getInstance().loadDelayed(getActor(), false);
		refreshWeight();
	}
	
	@Override
	public void store()
	{
		writeLock();
		try
		{
			_itemsDAO.store(_items);
		}
		finally
		{
			writeUnlock();
		}
	}
	
	@Override
	protected void sendAddItem(ItemInstance item)
	{
		Player actor = getActor();
		actor.sendPacket(new InventoryUpdate().addNewItem(item));
		if(item.getTemplate().getAgathionEnergy() > 0)
		{
			actor.sendPacket(new ExBR_AgathionEnergyInfo(1, item));
		}
	}
	
	@Override
	protected void sendModifyItem(ItemInstance item)
	{
		Player actor = getActor();
		actor.sendPacket(new InventoryUpdate().addModifiedItem(item));
		if(item.getTemplate().getAgathionEnergy() > 0)
		{
			actor.sendPacket(new ExBR_AgathionEnergyInfo(1, item));
		}
	}
	
	@Override
	protected void sendRemoveItem(ItemInstance item)
	{
		getActor().sendPacket(new InventoryUpdate().addRemovedItem(item));
	}
	
	public void startTimers()
	{
	}
	
	public void stopAllTimers()
	{
		for(ItemInstance item : getItems())
		{
			if(!item.isShadowItem() && !item.isTemporalItem())
				continue;
			item.stopTimer();
		}
	}
	
	protected class LifeTimeTask extends RunnableImpl
	{
		private final ItemInstance item;
		
		LifeTimeTask(ItemInstance item)
		{
			this.item = item;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			int left;
			Player player = getActor();
			ItemInstance itemInstance = item;
			synchronized(itemInstance)
			{
				left = item.getPeriod();
				if(left <= 0)
				{
					destroyItem(item);
				}
			}
			if(left <= 0)
			{
				player.sendPacket(new SystemMessage(1726).addItemName(item.getItemId()));
			}
		}
	}
	
	protected class ShadowLifeTimeTask extends RunnableImpl
	{
		private final ItemInstance item;
		
		ShadowLifeTimeTask(ItemInstance item)
		{
			this.item = item;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			Player player = getActor();
			if(!item.isEquipped())
			{
				return;
			}
			ItemInstance itemInstance = item;
			int duration;
			synchronized(itemInstance)
			{
				duration = Math.max(0, item.getDuration() - 1);
				item.setDuration(duration);
				if(duration == 0)
				{
					destroyItem(item);
				}
			}
			SystemMessage sm = null;
			if(duration == 10)
			{
				sm = new SystemMessage(1979);
			}
			else if(duration == 5)
			{
				sm = new SystemMessage(1980);
			}
			else if(duration == 1)
			{
				sm = new SystemMessage(1981);
			}
			else if(duration <= 0)
			{
				sm = new SystemMessage(1982);
			}
			else
			{
				player.sendPacket(new InventoryUpdate().addModifiedItem(item));
			}
			if(sm != null)
			{
				sm.addItemName(item.getItemId());
				player.sendPacket(sm);
			}
		}
	}
}