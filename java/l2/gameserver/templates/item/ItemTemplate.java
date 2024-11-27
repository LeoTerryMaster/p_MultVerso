package l2.gameserver.templates.item;

import l2.commons.lang.ArrayUtils;
import l2.gameserver.Config;
import l2.gameserver.handler.items.IItemHandler;
import l2.gameserver.instancemanager.CursedWeaponsManager;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Skill;
import l2.gameserver.model.base.Element;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.StatTemplate;
import l2.gameserver.stats.conditions.Condition;
import l2.gameserver.stats.funcs.Func;
import l2.gameserver.stats.funcs.FuncTemplate;
import l2.gameserver.templates.StatsSet;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import java.util.Calendar;

public abstract class ItemTemplate extends StatTemplate
{
	public static final int ITEM_ID_PC_BANG_POINTS = -100;
	public static final int ITEM_ID_CLAN_REPUTATION_SCORE = -200;
	public static final int ITEM_ID_FAME = -300;
	public static final int ITEM_ID_ADENA = 57;
	public static final int[] ITEM_ID_CASTLE_CIRCLET = {0, 6838, 6835, 6839, 6837, 6840, 6834, 6836, 8182, 8183};
	public static final int ITEM_ID_FORMAL_WEAR = 6408;
	public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
	public static final int TYPE1_SHIELD_ARMOR = 1;
	public static final int TYPE1_OTHER = 2;
	public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;
	public static final int TYPE2_WEAPON = 0;
	public static final int TYPE2_SHIELD_ARMOR = 1;
	public static final int TYPE2_ACCESSORY = 2;
	public static final int TYPE2_QUEST = 3;
	public static final int TYPE2_MONEY = 4;
	public static final int TYPE2_OTHER = 5;
	public static final int TYPE2_PET_WOLF = 6;
	public static final int TYPE2_PET_HATCHLING = 7;
	public static final int TYPE2_PET_STRIDER = 8;
	public static final int TYPE2_NODROP = 9;
	public static final int TYPE2_PET_GWOLF = 10;
	public static final int TYPE2_PENDANT = 11;
	public static final int TYPE2_PET_BABY = 12;
	public static final int SLOT_NONE = 0;
	public static final int SLOT_UNDERWEAR = 1;
	public static final int SLOT_R_EAR = 2;
	public static final int SLOT_L_EAR = 4;
	public static final int SLOT_NECK = 8;
	public static final int SLOT_R_FINGER = 16;
	public static final int SLOT_L_FINGER = 32;
	public static final int SLOT_HEAD = 64;
	public static final int SLOT_R_HAND = 128;
	public static final int SLOT_L_HAND = 256;
	public static final int SLOT_GLOVES = 512;
	public static final int SLOT_CHEST = 1024;
	public static final int SLOT_LEGS = 2048;
	public static final int SLOT_FEET = 4096;
	public static final int SLOT_BACK = 8192;
	public static final int SLOT_LR_HAND = 16384;
	public static final int SLOT_FULL_ARMOR = 32768;
	public static final int SLOT_HAIR = 65536;
	public static final int SLOT_FORMAL_WEAR = 131072;
	public static final int SLOT_DHAIR = 262144;
	public static final int SLOT_HAIRALL = 524288;
	public static final int SLOT_R_BRACELET = 1048576;
	public static final int SLOT_L_BRACELET = 2097152;
	public static final int SLOT_DECO = 4194304;
	public static final int SLOT_BELT = 268435456;
	public static final int SLOT_WOLF = -100;
	public static final int SLOT_HATCHLING = -101;
	public static final int SLOT_STRIDER = -102;
	public static final int SLOT_BABYPET = -103;
	public static final int SLOT_GWOLF = -104;
	public static final int SLOT_PENDANT = -105;
	public static final int SLOTS_ARMOR = 180032;
	public static final int SLOTS_JEWELRY = 62;
	public static final int CRYSTAL_NONE = 0;
	public static final int CRYSTAL_D = 1458;
	public static final int CRYSTAL_C = 1459;
	public static final int CRYSTAL_B = 1460;
	public static final int CRYSTAL_A = 1461;
	public static final int CRYSTAL_S = 1462;
	public static final int ATTRIBUTE_NONE = -2;
	public static final int ATTRIBUTE_FIRE = 0;
	public static final int ATTRIBUTE_WATER = 1;
	public static final int ATTRIBUTE_WIND = 2;
	public static final int ATTRIBUTE_EARTH = 3;
	public static final int ATTRIBUTE_HOLY = 4;
	public static final int ATTRIBUTE_DARK = 5;
	protected final int _itemId;
	protected final String _name;
	protected final String _addname;
	protected final String _icon;
	protected final String _icon32;
	protected final Grade _crystalType;
	private final ItemClass _class;
	private final int _weight;
	private final int _durability;
	private final int _referencePrice;
	private final int _crystalCount;
	private final boolean _temporal;
	private final boolean _stackable;
	private final boolean _crystallizable;
	private final ReuseType _reuseType;
	private final int _reuseDelay;
	private final int _reuseGroup;
	private final int _agathionEnergy;
	public ItemType type;
	protected int _type1;
	protected int _type2;
	protected int _bodyPart;
	protected Skill[] _skills;
	private int _flags;
	private Skill _enchant4Skill;
	private int[] _baseAttributes = new int[6];
	private IntObjectMap<int[]> _enchantOptions = Containers.emptyIntObjectMap();
	private Condition[] _conditions = Condition.EMPTY_ARRAY;
	private IItemHandler _handler = IItemHandler.NULL;
	private boolean _isShotItem;
	private boolean _isStatDisabled;
	
	protected ItemTemplate(StatsSet set)
	{
		_itemId = set.getInteger("item_id");
		_class = set.getEnum("class", ItemClass.class, ItemClass.OTHER);
		_name = set.getString("name");
		_addname = set.getString("add_name", "");
		_icon = set.getString("icon", "");
		_icon32 = "<img src=" + _icon + " width=32 height=32>";
		_weight = set.getInteger("weight", 0);
		_crystallizable = set.getBool("crystallizable", false);
		_stackable = set.getBool("stackable", false);
		_crystalType = set.getEnum("crystal_type", Grade.class, Grade.NONE);
		_durability = set.getInteger("durability", -1);
		_temporal = set.getBool("temporal", false);
		_bodyPart = set.getInteger("bodypart", 0);
		_referencePrice = set.getInteger("price", 0);
		_crystalCount = set.getInteger("crystal_count", 0);
		_reuseType = set.getEnum("reuse_type", ReuseType.class, ReuseType.NORMAL);
		_reuseDelay = set.getInteger("reuse_delay", 0);
		_reuseGroup = set.getInteger("delay_share_group", -_itemId);
		_agathionEnergy = set.getInteger("agathion_energy", 0);
		for(ItemFlags f : ItemFlags.VALUES)
		{
			boolean flag = set.getBool(f.name().toLowerCase(), f.getDefaultValue());
			if(_name.contains("{PvP}"))
			{
				if(f == ItemFlags.TRADEABLE && Config.ALT_PVP_ITEMS_TREDABLE)
				{
					flag = true;
				}
				if(f == ItemFlags.ATTRIBUTABLE && Config.ALT_PVP_ITEMS_ATTRIBUTABLE)
				{
					flag = true;
				}
				if(f == ItemFlags.AUGMENTABLE && Config.ALT_PVP_ITEMS_AUGMENTABLE)
				{
					flag = true;
				}
			}
			if(!flag)
				continue;
			activeFlag(f);
		}
		_funcTemplates = FuncTemplate.EMPTY_ARRAY;
		_skills = Skill.EMPTY_ARRAY;
	}
	
	public ItemType getItemType()
	{
		return type;
	}
	
	public String getIcon()
	{
		return _icon;
	}
	
	public String getIcon32()
	{
		return _icon32;
	}
	
	public final int getDurability()
	{
		return _durability;
	}
	
	public final boolean isTemporal()
	{
		return _temporal;
	}
	
	public final int getItemId()
	{
		return _itemId;
	}
	
	public abstract long getItemMask();
	
	public final int getType2()
	{
		return _type2;
	}
	
	public final int getBaseAttributeValue(Element element)
	{
		if(element == Element.NONE)
		{
			return 0;
		}
		return _baseAttributes[element.getId()];
	}
	
	public void setBaseAtributeElements(int[] val)
	{
		_baseAttributes = val;
	}
	
	public final int getType2ForPackets()
	{
		int type2 = _type2;
		switch(_type2)
		{
			case 6:
			case 7:
			case 8:
			case 10:
			case 12:
			{
				if(_bodyPart == 1024)
				{
					type2 = 1;
					break;
				}
				type2 = 0;
				break;
			}
			case 11:
			{
				type2 = 2;
			}
		}
		return type2;
	}
	
	public final int getWeight()
	{
		return _weight;
	}
	
	public final boolean isCrystallizable()
	{
		return _crystallizable && !isStackable() && getCrystalType() != Grade.NONE && getCrystalCount() > 0;
	}
	
	public final Grade getCrystalType()
	{
		return _crystalType;
	}
	
	public final Grade getItemGrade()
	{
		return getCrystalType();
	}
	
	public final int getCrystalCount()
	{
		return _crystalCount;
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public final String getAdditionalName()
	{
		return _addname;
	}
	
	public final int getBodyPart()
	{
		return _bodyPart;
	}
	
	public final int getType1()
	{
		return _type1;
	}
	
	public final boolean isStackable()
	{
		return _stackable;
	}
	
	public final int getReferencePrice()
	{
		return _referencePrice;
	}
	
	public boolean isForHatchling()
	{
		return _type2 == 7;
	}
	
	public boolean isForStrider()
	{
		return _type2 == 8;
	}
	
	public boolean isForWolf()
	{
		return _type2 == 6;
	}
	
	public boolean isForPetBaby()
	{
		return _type2 == 12;
	}
	
	public boolean isForGWolf()
	{
		return _type2 == 10;
	}
	
	public boolean isPendant()
	{
		return _type2 == 11;
	}
	
	public boolean isForPet()
	{
		return _type2 == 11 || _type2 == 7 || _type2 == 6 || _type2 == 8 || _type2 == 10 || _type2 == 12;
	}
	
	public void attachSkill(Skill skill)
	{
		_skills = ArrayUtils.add(_skills, skill);
	}
	
	public Skill[] getAttachedSkills()
	{
		return _skills;
	}
	
	public Skill getFirstSkill()
	{
		if(_skills.length > 0)
		{
			return _skills[0];
		}
		return null;
	}
	
	public Skill getEnchant4Skill()
	{
		return _enchant4Skill;
	}
	
	public void setEnchant4Skill(Skill enchant4Skill)
	{
		_enchant4Skill = enchant4Skill;
	}
	
	@Override
	public String toString()
	{
		return "" + _itemId + " " + _name;
	}
	
	public boolean isShadowItem()
	{
		return _durability > 0 && !isTemporal();
	}
	
	public boolean isSealedItem()
	{
		return _name.startsWith("Sealed");
	}
	
	public boolean isAltSeed()
	{
		return _name.contains("Alternative");
	}
	
	public ItemClass getItemClass()
	{
		return _class;
	}
	
	public boolean isSealStone()
	{
		switch(_itemId)
		{
			case 6360:
			case 6361:
			case 6362:
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isAdena()
	{
		return _itemId == 57;
	}
	
	public boolean isEquipment()
	{
		return _type1 != 4;
	}
	
	public boolean isKeyMatherial()
	{
		return _class == ItemClass.PIECES;
	}
	
	public boolean isRecipe()
	{
		return _class == ItemClass.RECIPIES;
	}
	
	public boolean isArrow()
	{
		return type == EtcItemTemplate.EtcItemType.ARROW;
	}
	
	public boolean isBelt()
	{
		return _bodyPart == 268435456;
	}
	
	public boolean isBracelet()
	{
		return _bodyPart == 1048576 || _bodyPart == 2097152;
	}
	
	public boolean isUnderwear()
	{
		return _bodyPart == 1;
	}
	
	public boolean isCloak()
	{
		return _bodyPart == 8192;
	}
	
	public boolean isTalisman()
	{
		return _bodyPart == 4194304;
	}
	
	public boolean isHerb()
	{
		return type == EtcItemTemplate.EtcItemType.HERB;
	}
	
	public boolean isAttributeCrystal()
	{
		return _itemId == 9552 || _itemId == 9553 || _itemId == 9554 || _itemId == 9555 || _itemId == 9556 || _itemId == 9557;
	}
	
	public boolean isAttributeEnergy()
	{
		return _itemId >= 9564 && _itemId <= 9569;
	}
	
	public boolean isHeroWeapon()
	{
		return _itemId >= 6611 && _itemId <= 6621 || _itemId >= 9388 && _itemId <= 9390;
	}
	
	public boolean isCursed()
	{
		return CursedWeaponsManager.getInstance().isCursed(_itemId);
	}
	
	public boolean isMercenaryTicket()
	{
		return type == EtcItemTemplate.EtcItemType.MERCENARY_TICKET;
	}
	
	public boolean isRod()
	{
		return getItemType() == WeaponTemplate.WeaponType.ROD;
	}
	
	public boolean isWeapon()
	{
		return getType2() == 0;
	}
	
	public boolean isArmor()
	{
		return getType2() == 1;
	}
	
	public boolean isAccessory()
	{
		return getType2() == 2;
	}
	
	public boolean isQuest()
	{
		return getType2() == 3;
	}
	
	public boolean isMageItem()
	{
		return false;
	}
	
	public boolean canBeEnchanted(@Deprecated boolean gradeCheck)
	{
		if(gradeCheck && getCrystalType() == Grade.NONE)
		{
			return false;
		}
		if(isCursed())
		{
			return false;
		}
		if(isQuest())
		{
			return false;
		}
		return isEnchantable();
	}
	
	public boolean isEquipable()
	{
		return getItemType() == EtcItemTemplate.EtcItemType.BAIT || getItemType() == EtcItemTemplate.EtcItemType.ARROW || getBodyPart() != 0 && !(this instanceof EtcItemTemplate);
	}
	
	public boolean testCondition(Playable player, ItemInstance instance)
	{
		return testCondition(player, instance, true);
	}
	
	public boolean testCondition(Playable player, ItemInstance instance, boolean showMessage)
	{
		if(getConditions().length == 0)
		{
			return true;
		}
		Env env = new Env();
		env.character = player;
		env.item = instance;
		for(Condition con : getConditions())
		{
			if(con.test(env))
				continue;
			if(showMessage && con.getSystemMsg() != null)
			{
				if(con.getSystemMsg().size() > 0)
				{
					player.sendPacket(new SystemMessage2(con.getSystemMsg()).addItemName(getItemId()));
				}
				else
				{
					player.sendPacket(con.getSystemMsg());
				}
			}
			return false;
		}
		return true;
	}
	
	public void addCondition(Condition condition)
	{
		_conditions = ArrayUtils.add(_conditions, condition);
	}
	
	public Condition[] getConditions()
	{
		return _conditions;
	}
	
	public boolean isEnchantable()
	{
		return hasFlag(ItemFlags.ENCHANTABLE);
	}
	
	public boolean isTradeable()
	{
		return hasFlag(ItemFlags.TRADEABLE);
	}
	
	public boolean isDestroyable()
	{
		return hasFlag(ItemFlags.DESTROYABLE);
	}
	
	public boolean isDropable()
	{
		return hasFlag(ItemFlags.DROPABLE);
	}
	
	public final boolean isSellable()
	{
		return hasFlag(ItemFlags.SELLABLE);
	}
	
	public final boolean isAugmentable()
	{
		return hasFlag(ItemFlags.AUGMENTABLE);
	}
	
	public final boolean isAttributable()
	{
		return hasFlag(ItemFlags.ATTRIBUTABLE);
	}
	
	public final boolean isStoreable()
	{
		return hasFlag(ItemFlags.STOREABLE);
	}
	
	public final boolean isFreightable()
	{
		return hasFlag(ItemFlags.FREIGHTABLE);
	}
	
	public boolean hasFlag(ItemFlags f)
	{
		return (_flags & f.mask()) == f.mask();
	}
	
	private void activeFlag(ItemFlags f)
	{
		_flags |= f.mask();
	}
	
	public IItemHandler getHandler()
	{
		return _handler;
	}
	
	public void setHandler(IItemHandler handler)
	{
		_handler = handler;
	}
	
	public boolean isShotItem()
	{
		return _isShotItem;
	}
	
	public void setIsShotItem(boolean isShotItem)
	{
		_isShotItem = isShotItem;
	}
	
	public int getReuseDelay()
	{
		return _reuseDelay;
	}
	
	public int getReuseGroup()
	{
		return _reuseGroup;
	}
	
	public int getDisplayReuseGroup()
	{
		return _reuseGroup < 0 ? -1 : _reuseGroup;
	}
	
	public int getAgathionEnergy()
	{
		return _agathionEnergy;
	}
	
	public void addEnchantOptions(int level, int[] options)
	{
		if(_enchantOptions.isEmpty())
		{
			_enchantOptions = new HashIntObjectMap();
		}
		_enchantOptions.put(level, options);
	}
	
	public IntObjectMap<int[]> getEnchantOptions()
	{
		return _enchantOptions;
	}
	
	public ReuseType getReuseType()
	{
		return _reuseType;
	}
	
	public void setStatDisabled(boolean val)
	{
		_isStatDisabled = val;
	}
	
	@Override
	public FuncTemplate[] getAttachedFuncs()
	{
		if(_isStatDisabled)
		{
			return FuncTemplate.EMPTY_ARRAY;
		}
		return super.getAttachedFuncs();
	}
	
	@Override
	public Func[] getStatFuncs(Object owner)
	{
		if(_isStatDisabled)
		{
			return Func.EMPTY_FUNC_ARRAY;
		}
		return super.getStatFuncs(owner);
	}
	
	public enum Grade
	{
		NONE(0, 0),
		D(1458, 1),
		C(1459, 2),
		B(1460, 3),
		A(1461, 4),
		S(1462, 5);
		
		public final int cry;
		public final int externalOrdinal;
		
		Grade(int crystal, int ext)
		{
			cry = crystal;
			externalOrdinal = ext;
		}
		
		public int gradeOrd()
		{
			return externalOrdinal;
		}
	}
	
	public enum ItemClass
	{
		ALL,
		WEAPON,
		ARMOR,
		JEWELRY,
		ACCESSORY,
		CONSUMABLE,
		MATHERIALS,
		PIECES,
		RECIPIES,
		SPELLBOOKS,
		MISC,
		OTHER;
	}
	
	public enum ReuseType
	{
		NORMAL(SystemMsg.THERE_ARE_S2_SECONDS_REMAINING_IN_S1S_REUSE_TIME, SystemMsg.THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_IN_S1S_REUSE_TIME, SystemMsg.THERE_ARE_S2_HOURS_S3_MINUTES_AND_S4_SECONDS_REMAINING_IN_S1S_REUSE_TIME)
				{
					@Override
					public long next(ItemInstance item)
					{
						return System.currentTimeMillis() + (long) item.getTemplate().getReuseDelay();
					}
				},
		EVERY_DAY_AT_6_30(SystemMsg.THERE_ARE_S2_SECONDS_REMAINING_FOR_S1S_REUSE_TIME, SystemMsg.THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_FOR_S1S_REUSE_TIME, SystemMsg.THERE_ARE_S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_S1S_REUSE_TIME)
				{
					@Override
					public long next(ItemInstance item)
					{
						Calendar nextTime = Calendar.getInstance();
						if(nextTime.get(11) > 6 || nextTime.get(11) == 6 && nextTime.get(12) >= 30)
						{
							nextTime.add(5, 1);
						}
						nextTime.set(11, 6);
						nextTime.set(12, 30);
						return nextTime.getTimeInMillis();
					}
				};
		
		private final SystemMsg[] _messages;
		
		ReuseType(SystemMsg... msg)
		{
			_messages = msg;
		}
		
		public abstract long next(ItemInstance item);
		
		public SystemMsg[] getMessages()
		{
			return _messages;
		}
	}
}