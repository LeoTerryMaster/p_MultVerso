package l2.gameserver.templates.item;

import l2.gameserver.Config;
import l2.gameserver.templates.StatsSet;

public final class ArmorTemplate extends ItemTemplate
{
	public static final double EMPTY_RING = 5.0;
	public static final double EMPTY_EARRING = 9.0;
	public static final double EMPTY_NECKLACE = 13.0;
	public static final double EMPTY_HELMET = 12.0;
	public static final double EMPTY_BODY_FIGHTER = 31.0;
	public static final double EMPTY_LEGS_FIGHTER = 18.0;
	public static final double EMPTY_BODY_MYSTIC = 15.0;
	public static final double EMPTY_LEGS_MYSTIC = 8.0;
	public static final double EMPTY_GLOVES = 8.0;
	public static final double EMPTY_BOOTS = 7.0;
	
	public ArmorTemplate(StatsSet set)
	{
		super(set);
		type = set.getEnum("type", ArmorType.class);
		if(_bodyPart == 8 || (_bodyPart & 4) != 0 || (_bodyPart & 32) != 0)
		{
			_type1 = 0;
			_type2 = 2;
		}
		else if(_bodyPart == 65536 || _bodyPart == 262144 || _bodyPart == 524288)
		{
			_type1 = 2;
			_type2 = Config.ALT_HAIR_TO_ACC_SLOT ? 2 : 5;
		}
		else
		{
			_type1 = 1;
			_type2 = 1;
		}
		if(getItemType() == ArmorType.PET)
		{
			_type1 = 1;
			switch(_bodyPart)
			{
				case -100:
				{
					_type2 = 6;
					_bodyPart = 1024;
					break;
				}
				case -104:
				{
					_type2 = 10;
					_bodyPart = 1024;
					break;
				}
				case -101:
				{
					_type2 = 7;
					_bodyPart = 1024;
					break;
				}
				case -105:
				{
					_type2 = 11;
					_bodyPart = 8;
					break;
				}
				case -103:
				{
					_type2 = 12;
					_bodyPart = 1024;
					break;
				}
				default:
				{
					_type2 = 8;
					_bodyPart = 1024;
				}
			}
		}
	}
	
	@Override
	public ArmorType getItemType()
	{
		return (ArmorType) type;
	}
	
	@Override
	public final long getItemMask()
	{
		return getItemType().mask();
	}
	
	public enum ArmorType implements ItemType
	{
		NONE(1, "None"),
		LIGHT(2, "Light"),
		HEAVY(3, "Heavy"),
		MAGIC(4, "Magic"),
		PET(5, "Pet"),
		SIGIL(6, "Sigil");
		
		public static final ArmorType[] VALUES;
		
		static
		{
			VALUES = ArmorType.values();
		}
		
		private final long _mask;
		private final String _name;
		
		ArmorType(int id, String name)
		{
			_mask = 1 << id + WeaponTemplate.WeaponType.VALUES.length;
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