package l2.gameserver.model.items;

import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.base.Element;
import l2.gameserver.templates.item.ItemTemplate;

public class ItemInfo
{
	private int ownerId;
	private int lastChange;
	private int type1;
	private int objectId;
	private int itemId;
	private long count;
	private int type2;
	private int customType1;
	private boolean isEquipped;
	private int bodyPart;
	private int enchantLevel;
	private int customType2;
	private int variation_stat1;
	private int variation_stat2;
	private int shadowLifeTime;
	private int attackElement = Element.NONE.getId();
	private int attackElementValue;
	private int defenceFire;
	private int defenceWater;
	private int defenceWind;
	private int defenceEarth;
	private int defenceHoly;
	private int defenceUnholy;
	private int equipSlot;
	private int temporalLifeTime;
	private int[] enchantOptions = ItemInstance.EMPTY_ENCHANT_OPTIONS;
	private ItemTemplate item;
	
	public ItemInfo()
	{
	}
	
	public ItemInfo(ItemInstance item)
	{
		setOwnerId(item.getOwnerId());
		setObjectId(item.getObjectId());
		setItemId(item.getItemId());
		setCount(item.getCount());
		setCustomType1(item.getBlessed());
		setEquipped(item.isEquipped());
		setEnchantLevel(item.getEnchantLevel());
		setCustomType2(item.getDamaged());
		setVariationStat1(item.getVariationStat1());
		setVariationStat2(item.getVariationStat2());
		setShadowLifeTime(item.getDuration());
		setAttackElement(item.getAttackElement().getId());
		setAttackElementValue(item.getAttackElementValue());
		setDefenceFire(item.getDefenceFire());
		setDefenceWater(item.getDefenceWater());
		setDefenceWind(item.getDefenceWind());
		setDefenceEarth(item.getDefenceEarth());
		setDefenceHoly(item.getDefenceHoly());
		setDefenceUnholy(item.getDefenceUnholy());
		setEquipSlot(item.getEquipSlot());
		setTemporalLifeTime(item.getPeriod());
		setEnchantOptions(item.getEnchantOptions());
	}
	
	public ItemTemplate getItem()
	{
		return item;
	}
	
	public int getOwnerId()
	{
		return ownerId;
	}
	
	public void setOwnerId(int ownerId)
	{
		this.ownerId = ownerId;
	}
	
	public int getLastChange()
	{
		return lastChange;
	}
	
	public void setLastChange(int lastChange)
	{
		this.lastChange = lastChange;
	}
	
	public int getType1()
	{
		return type1;
	}
	
	public void setType1(int type1)
	{
		this.type1 = type1;
	}
	
	public int getObjectId()
	{
		return objectId;
	}
	
	public void setObjectId(int objectId)
	{
		this.objectId = objectId;
	}
	
	public int getItemId()
	{
		return itemId;
	}
	
	public void setItemId(int itemId)
	{
		this.itemId = itemId;
		item = itemId > 0 ? ItemHolder.getInstance().getTemplate(getItemId()) : null;
		if(item != null)
		{
			setType1(item.getType1());
			setType2(item.getType2ForPackets());
			setBodyPart(item.getBodyPart());
		}
	}
	
	public long getCount()
	{
		return count;
	}
	
	public void setCount(long count)
	{
		this.count = count;
	}
	
	public int getType2()
	{
		return type2;
	}
	
	public void setType2(int type2)
	{
		this.type2 = type2;
	}
	
	public int getCustomType1()
	{
		return customType1;
	}
	
	public void setCustomType1(int customType1)
	{
		this.customType1 = customType1;
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
		return bodyPart;
	}
	
	public void setBodyPart(int bodyPart)
	{
		this.bodyPart = bodyPart;
	}
	
	public int getEnchantLevel()
	{
		return enchantLevel;
	}
	
	public void setEnchantLevel(int enchantLevel)
	{
		this.enchantLevel = enchantLevel;
	}
	
	public int getVariationStat1()
	{
		return variation_stat1;
	}
	
	public void setVariationStat1(int var1)
	{
		variation_stat1 = var1;
	}
	
	public int getVariationStat2()
	{
		return variation_stat2;
	}
	
	public void setVariationStat2(int var2)
	{
		variation_stat2 = var2;
	}
	
	public int getShadowLifeTime()
	{
		return shadowLifeTime;
	}
	
	public void setShadowLifeTime(int shadowLifeTime)
	{
		this.shadowLifeTime = shadowLifeTime;
	}
	
	public int getCustomType2()
	{
		return customType2;
	}
	
	public void setCustomType2(int customType2)
	{
		this.customType2 = customType2;
	}
	
	public int getAttackElement()
	{
		return attackElement;
	}
	
	public void setAttackElement(int attackElement)
	{
		this.attackElement = attackElement;
	}
	
	public int getAttackElementValue()
	{
		return attackElementValue;
	}
	
	public void setAttackElementValue(int attackElementValue)
	{
		this.attackElementValue = attackElementValue;
	}
	
	public int getDefenceFire()
	{
		return defenceFire;
	}
	
	public void setDefenceFire(int defenceFire)
	{
		this.defenceFire = defenceFire;
	}
	
	public int getDefenceWater()
	{
		return defenceWater;
	}
	
	public void setDefenceWater(int defenceWater)
	{
		this.defenceWater = defenceWater;
	}
	
	public int getDefenceWind()
	{
		return defenceWind;
	}
	
	public void setDefenceWind(int defenceWind)
	{
		this.defenceWind = defenceWind;
	}
	
	public int getDefenceEarth()
	{
		return defenceEarth;
	}
	
	public void setDefenceEarth(int defenceEarth)
	{
		this.defenceEarth = defenceEarth;
	}
	
	public int getDefenceHoly()
	{
		return defenceHoly;
	}
	
	public void setDefenceHoly(int defenceHoly)
	{
		this.defenceHoly = defenceHoly;
	}
	
	public int getDefenceUnholy()
	{
		return defenceUnholy;
	}
	
	public void setDefenceUnholy(int defenceUnholy)
	{
		this.defenceUnholy = defenceUnholy;
	}
	
	public int getEquipSlot()
	{
		return equipSlot;
	}
	
	public void setEquipSlot(int equipSlot)
	{
		this.equipSlot = equipSlot;
	}
	
	public int getTemporalLifeTime()
	{
		return temporalLifeTime;
	}
	
	public void setTemporalLifeTime(int temporalLifeTime)
	{
		this.temporalLifeTime = temporalLifeTime;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(obj == null)
		{
			return false;
		}
		if(getClass() != obj.getClass())
		{
			return false;
		}
		if(getObjectId() == 0)
		{
			return getItemId() == ((ItemInfo) obj).getItemId();
		}
		return getObjectId() == ((ItemInfo) obj).getObjectId();
	}
	
	public int[] getEnchantOptions()
	{
		return enchantOptions;
	}
	
	public void setEnchantOptions(int[] enchantOptions)
	{
		this.enchantOptions = enchantOptions;
	}
}