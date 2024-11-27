package l2.gameserver.templates.item;

import l2.gameserver.templates.StatsSet;

public final class EtcItemTemplate extends ItemTemplate
{
	public EtcItemTemplate(StatsSet set)
	{
		super(set);
		type = set.getEnum("type", EtcItemType.class);
		_type1 = 4;
		switch(getItemType())
		{
			case QUEST:
			{
				_type2 = 3;
				break;
			}
			case MONEY:
			{
				_type2 = 4;
				break;
			}
			default:
			{
				_type2 = 5;
			}
		}
	}
	
	@Override
	public EtcItemType getItemType()
	{
		return (EtcItemType) type;
	}
	
	@Override
	public long getItemMask()
	{
		return getItemType().mask();
	}
	
	@Override
	public final boolean isShadowItem()
	{
		return false;
	}
	
	@Override
	public final boolean canBeEnchanted(boolean gradeCheck)
	{
		return false;
	}
	
	public enum EtcItemType implements ItemType
	{
		ARROW(1, "Arrow"),
		MATERIAL(2, "Material"),
		PET_COLLAR(3, "PetCollar"),
		POTION(4, "Potion"),
		RECIPE(5, "Recipe"),
		SCROLL(6, "Scroll"),
		QUEST(7, "Quest"),
		MONEY(8, "Money"),
		OTHER(9, "Other"),
		SPELLBOOK(10, "Spellbook"),
		SEED(11, "Seed"),
		BAIT(12, "Bait"),
		SHOT(13, "Shot"),
		BOLT(14, "Bolt"),
		RUNE(15, "Rune"),
		HERB(16, "Herb"),
		MERCENARY_TICKET(17, "Mercenary Ticket");
		
		private final long _mask;
		private final String _name;
		
		EtcItemType(int id, String name)
		{
			_mask = 1 << id + WeaponTemplate.WeaponType.VALUES.length + ArmorTemplate.ArmorType.VALUES.length;
			_name = name;
		}
		
		@Override
		public long mask()
		{
			return _mask;
		}
		
		@Override
		public String toString()
		{
			return _name;
		}
	}
}