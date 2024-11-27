package l2.gameserver.model.items;

import l2.commons.collections.LazyArrayList;
import l2.commons.util.concurrent.atomic.AtomicEnumBitFlag;
import l2.gameserver.Config;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.dao.ItemsDAO;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.instancemanager.CursedWeaponsManager;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.Element;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.attachment.ItemAttachment;
import l2.gameserver.model.items.listeners.ItemEnchantOptionsListener;
import l2.gameserver.network.l2.s2c.DropItem;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.SpawnItem;
import l2.gameserver.scripts.Events;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.funcs.Func;
import l2.gameserver.stats.funcs.FuncTemplate;
import l2.gameserver.tables.PetDataTable;
import l2.gameserver.taskmanager.ItemsAutoDestroy;
import l2.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.templates.item.ItemType;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Location;
import org.napile.primitive.Containers;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public final class ItemInstance extends GameObject
{
	public static final int[] EMPTY_ENCHANT_OPTIONS = new int[3];
	public static final long MAX_AMMOUNT = Integer.MAX_VALUE;
	public static final int CHARGED_NONE = 0;
	public static final int CHARGED_SOULSHOT = 1;
	public static final int CHARGED_SPIRITSHOT = 1;
	public static final int CHARGED_BLESSED_SPIRITSHOT = 2;
	public static final int FLAG_NO_DROP = 1;
	public static final int FLAG_NO_TRADE = 2;
	public static final int FLAG_NO_TRANSFER = 4;
	public static final int FLAG_NO_CRYSTALLIZE = 8;
	public static final int FLAG_NO_ENCHANT = 16;
	public static final int FLAG_NO_DESTROY = 32;
	private static final long serialVersionUID = 3162753878915133228L;
	private static final ItemsDAO _itemsDAO = ItemsDAO.getInstance();
	private final AtomicEnumBitFlag<ItemStateFlags> _stateFlags = new AtomicEnumBitFlag();
	private ItemAttributes attrs = new ItemAttributes();
	private int[] _enchantOptions = EMPTY_ENCHANT_OPTIONS;
	private int _owner_id;
	private int _item_id;
	private long _ammount;
	private ItemLocation _location;
	private int _slot;
	private int _enchant;
	private int _duaration = -1;
	private int _period = -9999;
	private int _variation_stat1;
	private int _variation_stat2;
	private int _blessed;
	private int _damaged;
	private int _energy;
	private int _cflags;
	private int _visItemId;
	private ItemTemplate template;
	private boolean isEquipped;
	private long _dropTime;
	private IntSet _dropPlayers = Containers.EMPTY_INT_SET;
	private long _dropTimeOwner;
	private int _chargedSoulshot;
	private int _chargedSpiritshot;
	private boolean _chargedFishtshot;
	private ItemAttachment _attachment;
	private ScheduledFuture<?> _timerTask;
	
	public ItemInstance(int objectId)
	{
		super(objectId);
	}
	
	public ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		setItemId(itemId);
		setDuration(getTemplate().getDurability());
		setPeriodBegin(getTemplate().isTemporal() ? (int) (System.currentTimeMillis() / 1000) + getTemplate().getDurability() * 60 : -9999);
		setAgathionEnergy(getTemplate().getAgathionEnergy());
		setLocData(-1);
		setEnchantLevel(0);
	}
	
	public int getOwnerId()
	{
		return _owner_id;
	}
	
	public void setOwnerId(int ownerId)
	{
		if(_owner_id == ownerId)
		{
			return;
		}
		_owner_id = ownerId;
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public int getItemId()
	{
		return _item_id;
	}
	
	public void setItemId(int id)
	{
		_item_id = id;
		template = ItemHolder.getInstance().getTemplate(id);
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public int getVisibleItemId()
	{
		if(_visItemId > 0)
		{
			return _visItemId;
		}
		return getItemId();
	}
	
	public void setVisibleItemId(int visItemId)
	{
		_visItemId = visItemId;
	}
	
	public long getCount()
	{
		return _ammount;
	}
	
	public void setCount(long count)
	{
		if(count < 0)
		{
			count = 0;
		}
		if(isStackable() && count > Integer.MAX_VALUE)
		{
			_ammount = Integer.MAX_VALUE;
			getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
			return;
		}
		if(!isStackable() && count > 1)
		{
			_ammount = 1;
			getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
			return;
		}
		if(_ammount == count)
		{
			return;
		}
		_ammount = count;
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public AtomicEnumBitFlag<ItemStateFlags> getItemStateFlag()
	{
		return _stateFlags;
	}
	
	public int getEnchantLevel()
	{
		return _enchant;
	}
	
	public void setEnchantLevel(int enchantLevel)
	{
		int old = _enchant;
		_enchant = enchantLevel;
		if(old != _enchant && getTemplate().getEnchantOptions().size() > 0)
		{
			Player player = GameObjectsStorage.getPlayer(getOwnerId());
			if(isEquipped() && player != null)
			{
				ItemEnchantOptionsListener.getInstance().onUnequip(getEquipSlot(), this, player);
			}
			int[] enchantOptions;
			int[] arrn = _enchantOptions = (enchantOptions = getTemplate().getEnchantOptions().get(_enchant)) == null ? EMPTY_ENCHANT_OPTIONS : enchantOptions;
			if(isEquipped() && player != null)
			{
				ItemEnchantOptionsListener.getInstance().onEquip(getEquipSlot(), this, player);
			}
		}
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public String getLocName()
	{
		return _location.name();
	}
	
	public void setLocName(String loc)
	{
		setLocation(ItemLocation.valueOf(loc));
	}
	
	public ItemLocation getLocation()
	{
		return _location;
	}
	
	public void setLocation(ItemLocation loc)
	{
		if(_location == loc)
		{
			return;
		}
		_location = loc;
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public int getLocData()
	{
		return _slot;
	}
	
	public void setLocData(int slot)
	{
		if(_slot == slot)
		{
			return;
		}
		_slot = slot;
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public int getBlessed()
	{
		return _blessed;
	}
	
	public void setBlessed(int val)
	{
		_blessed = val;
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public int getDamaged()
	{
		return _damaged;
	}
	
	public void setDamaged(int val)
	{
		_damaged = val;
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public int getCustomFlags()
	{
		return _cflags;
	}
	
	public void setCustomFlags(int flags)
	{
		if(_cflags == flags)
		{
			return;
		}
		_cflags = flags;
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public ItemAttributes getAttributes()
	{
		return attrs;
	}
	
	public void setAttributes(ItemAttributes attrs)
	{
		this.attrs = attrs;
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public int getDuration()
	{
		if(!isShadowItem())
		{
			return -1;
		}
		return _duaration;
	}
	
	public void setDuration(int duration)
	{
		_duaration = duration;
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public int getPeriod()
	{
		if(!isTemporalItem())
		{
			return -9999;
		}
		return _period - (int) (System.currentTimeMillis() / 1000);
	}
	
	public int getPeriodBegin()
	{
		if(!isTemporalItem())
		{
			return -9999;
		}
		return _period;
	}
	
	public void setPeriodBegin(int period)
	{
		_period = period;
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public void startTimer(Runnable r)
	{
		_timerTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(r, 0, 60000);
	}
	
	public void stopTimer()
	{
		if(_timerTask != null)
		{
			_timerTask.cancel(false);
			_timerTask = null;
		}
	}
	
	public boolean isEquipable()
	{
		return template.isEquipable();
	}
	
	public boolean isEquipped()
	{
		return isEquipped;
	}
	
	public void setEquipped(boolean isEquipped)
	{
		this.isEquipped = isEquipped;
	}
	
	public int getBodyPart()
	{
		return template.getBodyPart();
	}
	
	public int getEquipSlot()
	{
		return getLocData();
	}
	
	public ItemTemplate getTemplate()
	{
		return template;
	}
	
	public void setDropTime(long time)
	{
		_dropTime = time;
	}
	
	public long getLastDropTime()
	{
		return _dropTime;
	}
	
	public long getDropTimeOwner()
	{
		return _dropTimeOwner;
	}
	
	public ItemType getItemType()
	{
		return template.getItemType();
	}
	
	public boolean isArmor()
	{
		return template.isArmor();
	}
	
	public boolean isAccessory()
	{
		return template.isAccessory();
	}
	
	public boolean isWeapon()
	{
		return template.isWeapon();
	}
	
	public int getReferencePrice()
	{
		return template.getReferencePrice();
	}
	
	public boolean isStackable()
	{
		return template.isStackable();
	}
	
	@Override
	public void onAction(Player player, boolean shift)
	{
		if(Events.onAction(player, this, shift))
		{
			return;
		}
		if(player.isCursedWeaponEquipped() && CursedWeaponsManager.getInstance().isCursed(getItemId()))
		{
			return;
		}
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this, null);
	}
	
	@Override
	public int getActingRange()
	{
		return 16;
	}
	
	public boolean isAugmented()
	{
		return getVariationStat1() != 0 || getVariationStat2() != 0;
	}
	
	public int getVariationStat1()
	{
		return _variation_stat1;
	}
	
	public void setVariationStat1(int stat)
	{
		_variation_stat1 = stat;
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public int getVariationStat2()
	{
		return _variation_stat2;
	}
	
	public void setVariationStat2(int stat)
	{
		_variation_stat2 = stat;
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public int getChargedSoulshot()
	{
		return _chargedSoulshot;
	}
	
	public void setChargedSoulshot(int type)
	{
		_chargedSoulshot = type;
	}
	
	public int getChargedSpiritshot()
	{
		return _chargedSpiritshot;
	}
	
	public void setChargedSpiritshot(int type)
	{
		_chargedSpiritshot = type;
	}
	
	public boolean getChargedFishshot()
	{
		return _chargedFishtshot;
	}
	
	public void setChargedFishshot(boolean type)
	{
		_chargedFishtshot = type;
	}
	
	public Func[] getStatFuncs()
	{
		LazyArrayList<Func> funcs = LazyArrayList.newInstance();
		if(template.getAttachedFuncs().length > 0)
		{
			for(FuncTemplate t : template.getAttachedFuncs())
			{
				Func f = t.getFunc(this);
				if(f == null)
					continue;
				funcs.add(f);
			}
		}
		for(Element e : Element.VALUES)
		{
			if(isWeapon())
			{
				funcs.add(new FuncAttack(e, 64, this));
			}
			if(!isArmor())
				continue;
			funcs.add(new FuncDefence(e, 64, this));
		}
		Func[] result = Func.EMPTY_FUNC_ARRAY;
		if(!funcs.isEmpty())
		{
			result = funcs.toArray(new Func[funcs.size()]);
		}
		LazyArrayList.recycle(funcs);
		return result;
	}
	
	public boolean isHeroWeapon()
	{
		return template.isHeroWeapon();
	}
	
	public boolean canBeDestroyed(Player player)
	{
		if((getCustomFlags() & 32) == 32)
		{
			return false;
		}
		if(isHeroWeapon())
		{
			return false;
		}
		if(PetDataTable.isPetControlItem(this) && player.isMounted())
		{
			return false;
		}
		if(player.getPetControlItem() == this)
		{
			return false;
		}
		if(player.getEnchantScroll() == this)
		{
			return false;
		}
		if(isCursed())
		{
			return false;
		}
		return template.isDestroyable();
	}
	
	public boolean canBeDropped(Player player, boolean pk)
	{
		if(player.isGM())
		{
			return true;
		}
		if((getCustomFlags() & 1) == 1)
		{
			return false;
		}
		if(isShadowItem())
		{
			return false;
		}
		if(isTemporalItem())
		{
			return false;
		}
		if(!(!isAugmented() || pk && Config.DROP_ITEMS_AUGMENTED || Config.ALT_ALLOW_DROP_AUGMENTED))
		{
			return false;
		}
		if(!ItemFunctions.checkIfCanDiscard(player, this))
		{
			return false;
		}
		return template.isDropable();
	}
	
	public boolean canBeTraded(Player player)
	{
		if(isEquipped())
		{
			return false;
		}
		if(player.isGM())
		{
			return true;
		}
		if((getCustomFlags() & 2) == 2)
		{
			return false;
		}
		if(isShadowItem())
		{
			return false;
		}
		if(isTemporalItem())
		{
			return false;
		}
		if(isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
		{
			return false;
		}
		if(!ItemFunctions.checkIfCanDiscard(player, this))
		{
			return false;
		}
		return template.isTradeable();
	}
	
	public boolean canBeSold(Player player)
	{
		if((getCustomFlags() & 32) == 32)
		{
			return false;
		}
		if(getItemId() == 57)
		{
			return false;
		}
		if(template.getReferencePrice() == 0)
		{
			return false;
		}
		if(isShadowItem())
		{
			return false;
		}
		if(isTemporalItem())
		{
			return false;
		}
		if(isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
		{
			return false;
		}
		if(isEquipped())
		{
			return false;
		}
		if(!ItemFunctions.checkIfCanDiscard(player, this))
		{
			return false;
		}
		return template.isSellable();
	}
	
	public boolean canBeStored(Player player, boolean privatewh)
	{
		if((getCustomFlags() & 4) == 4)
		{
			return false;
		}
		if(!getTemplate().isStoreable())
		{
			return false;
		}
		if(!privatewh && (isShadowItem() || isTemporalItem()))
		{
			return false;
		}
		if(!privatewh && isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
		{
			return false;
		}
		if(isEquipped())
		{
			return false;
		}
		if(!ItemFunctions.checkIfCanDiscard(player, this))
		{
			return false;
		}
		return privatewh || template.isTradeable();
	}
	
	public boolean canBeCrystallized(Player player)
	{
		if((getCustomFlags() & 8) == 8)
		{
			return false;
		}
		if(isShadowItem())
		{
			return false;
		}
		if(isTemporalItem())
		{
			return false;
		}
		if(!ItemFunctions.checkIfCanDiscard(player, this))
		{
			return false;
		}
		return template.isCrystallizable();
	}
	
	public boolean canBeEnchanted(boolean gradeCheck)
	{
		if((getCustomFlags() & 16) == 16)
		{
			return false;
		}
		return template.canBeEnchanted(gradeCheck);
	}
	
	public boolean canBeExchanged(Player player)
	{
		if((getCustomFlags() & 32) == 32)
		{
			return false;
		}
		if(isShadowItem())
		{
			return false;
		}
		if(isTemporalItem())
		{
			return false;
		}
		if(!ItemFunctions.checkIfCanDiscard(player, this))
		{
			return false;
		}
		return template.isDestroyable();
	}
	
	public boolean isShadowItem()
	{
		return template.isShadowItem();
	}
	
	public boolean isTemporalItem()
	{
		return template.isTemporal();
	}
	
	public boolean isAltSeed()
	{
		return template.isAltSeed();
	}
	
	public boolean isCursed()
	{
		return template.isCursed();
	}
	
	public void dropToTheGround(Player lastAttacker, NpcInstance fromNpc)
	{
		Creature dropper = fromNpc;
		if(dropper == null)
		{
			dropper = lastAttacker;
		}
		Location pos = Location.findAroundPosition(dropper, 128);
		if(lastAttacker != null)
		{
			_dropPlayers = new HashIntSet(1, 2.0f);
			for(Player member : lastAttacker.getPlayerGroup())
			{
				_dropPlayers.add(member.getObjectId());
			}
			_dropTimeOwner = System.currentTimeMillis();
			_dropTimeOwner = fromNpc != null && fromNpc.isRaid() ? (_dropTimeOwner += Config.NONOWNER_ITEM_PICKUP_DELAY_RAID) : (_dropTimeOwner += Config.NONOWNER_ITEM_PICKUP_DELAY);
		}
		dropMe(dropper, pos);
		if(isHerb())
		{
			ItemsAutoDestroy.getInstance().addHerb(this);
		}
		else if(Config.AUTODESTROY_ITEM_AFTER > 0 && !isCursed())
		{
			ItemsAutoDestroy.getInstance().addItem(this);
		}
	}
	
	public void dropToTheGround(Creature dropper, Location dropPos)
	{
		if(GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, dropper.getGeoIndex()))
		{
			dropMe(dropper, dropPos);
		}
		else
		{
			dropMe(dropper, dropper.getLoc());
		}
		if(isHerb())
		{
			ItemsAutoDestroy.getInstance().addHerb(this);
		}
		else if(Config.AUTODESTROY_ITEM_AFTER > 0 && !isCursed())
		{
			ItemsAutoDestroy.getInstance().addItem(this);
		}
	}
	
	public void dropToTheGround(Playable dropper, Location dropPos)
	{
		setLocation(ItemLocation.VOID);
		save();
		if(GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, dropper.getGeoIndex()))
		{
			dropMe(dropper, dropPos);
		}
		else
		{
			dropMe(dropper, dropper.getLoc());
		}
		if(isHerb())
		{
			ItemsAutoDestroy.getInstance().addHerb(this);
		}
		else if(Config.AUTODESTROY_ITEM_AFTER > 0 && !isCursed())
		{
			ItemsAutoDestroy.getInstance().addItem(this);
		}
	}
	
	public void dropMe(Creature dropper, Location loc)
	{
		if(dropper != null)
		{
			setReflection(dropper.getReflection());
		}
		spawnMe0(loc, dropper);
	}
	
	public final void pickupMe()
	{
		decayMe();
		setReflection(ReflectionManager.DEFAULT);
	}
	
	public ItemTemplate.ItemClass getItemClass()
	{
		return template.getItemClass();
	}
	
	private int getDefence(Element element)
	{
		return isArmor() ? getAttributeElementValue(element, true) : 0;
	}
	
	public int getDefenceFire()
	{
		return getDefence(Element.FIRE);
	}
	
	public int getDefenceWater()
	{
		return getDefence(Element.WATER);
	}
	
	public int getDefenceWind()
	{
		return getDefence(Element.WIND);
	}
	
	public int getDefenceEarth()
	{
		return getDefence(Element.EARTH);
	}
	
	public int getDefenceHoly()
	{
		return getDefence(Element.HOLY);
	}
	
	public int getDefenceUnholy()
	{
		return getDefence(Element.UNHOLY);
	}
	
	public int getAttributeElementValue(Element element, boolean withBase)
	{
		return attrs.getValue(element) + (withBase ? template.getBaseAttributeValue(element) : 0);
	}
	
	public Element getAttributeElement()
	{
		return attrs.getElement();
	}
	
	public int getAttributeElementValue()
	{
		return attrs.getValue();
	}
	
	public Element getAttackElement()
	{
		Element element;
		Element element2 = element = isWeapon() ? getAttributeElement() : Element.NONE;
		if(element == Element.NONE)
		{
			for(Element e : Element.VALUES)
			{
				if(template.getBaseAttributeValue(e) <= 0)
					continue;
				return e;
			}
		}
		return element;
	}
	
	public int getAttackElementValue()
	{
		return isWeapon() ? getAttributeElementValue(getAttackElement(), true) : 0;
	}
	
	public void setAttributeElement(Element element, int value)
	{
		attrs.setValue(element, value);
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public boolean isHerb()
	{
		return getTemplate().isHerb();
	}
	
	public ItemTemplate.Grade getCrystalType()
	{
		return template.getCrystalType();
	}
	
	@Override
	public String getName()
	{
		return getTemplate().getName();
	}
	
	public void save()
	{
		_itemsDAO.store(this);
	}
	
	public void delete()
	{
		_itemsDAO.delete(this);
	}
	
	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		L2GameServerPacket packet = dropper != null ? new DropItem(this, dropper.getObjectId()) : new SpawnItem(this);
		return Collections.singletonList(packet);
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getTemplate().getItemId());
		sb.append(" ");
		if(getEnchantLevel() > 0)
		{
			sb.append("+");
			sb.append(getEnchantLevel());
			sb.append(" ");
		}
		sb.append(getTemplate().getName());
		if(!getTemplate().getAdditionalName().isEmpty())
		{
			sb.append(" ");
			sb.append("\\").append(getTemplate().getAdditionalName()).append("\\");
		}
		sb.append(" ");
		sb.append("(");
		sb.append(getCount());
		sb.append(")");
		sb.append("[");
		sb.append(getObjectId());
		sb.append("]");
		return sb.toString();
	}
	
	@Override
	public boolean isItem()
	{
		return true;
	}
	
	public ItemAttachment getAttachment()
	{
		return _attachment;
	}
	
	public void setAttachment(ItemAttachment attachment)
	{
		ItemAttachment old = _attachment;
		_attachment = attachment;
		if(_attachment != null)
		{
			_attachment.setItem(this);
		}
		if(old != null)
		{
			old.setItem(null);
		}
	}
	
	public int getAgathionEnergy()
	{
		return _energy;
	}
	
	public void setAgathionEnergy(int energy)
	{
		if(_energy == energy)
		{
			return;
		}
		_energy = energy;
		getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, true);
	}
	
	public int[] getEnchantOptions()
	{
		return _enchantOptions;
	}
	
	public IntSet getDropPlayers()
	{
		return _dropPlayers;
	}
	
	public enum ItemLocation
	{
		VOID,
		INVENTORY,
		PAPERDOLL,
		PET_INVENTORY,
		PET_PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		FREIGHT,
		LEASE,
		MAIL;
	}
	
	public class FuncDefence extends Func
	{
		private final Element element;
		
		public FuncDefence(Element element, int order, Object owner)
		{
			super(element.getDefence(), order, owner);
			this.element = element;
		}
		
		@Override
		public void calc(Env env)
		{
			env.value += (double) getAttributeElementValue(element, true);
		}
	}
	
	public class FuncAttack extends Func
	{
		private final Element element;
		
		public FuncAttack(Element element, int order, Object owner)
		{
			super(element.getAttack(), order, owner);
			this.element = element;
		}
		
		@Override
		public void calc(Env env)
		{
			env.value += (double) getAttributeElementValue(element, true);
		}
	}
}