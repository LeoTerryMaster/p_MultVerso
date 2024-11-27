package l2.gameserver.templates.item.support;

import l2.gameserver.model.items.ItemInstance;

public enum EnchantTargetType
{
	ALL(true, true, true),
	WEAPON(true, false, false),
	ARMOR(false, true, true);
	
	private final boolean _useOnWeapon;
	private final boolean _useOnArmor;
	private final boolean _useOnAccessory;
	
	EnchantTargetType(boolean weapon, boolean armor, boolean accesory)
	{
		_useOnWeapon = weapon;
		_useOnArmor = armor;
		_useOnAccessory = accesory;
	}
	
	public boolean isUsableOn(ItemInstance item)
	{
		if(_useOnWeapon && item.isWeapon())
		{
			return true;
		}
		if(_useOnArmor && item.isArmor())
		{
			return true;
		}
		return _useOnAccessory && item.isAccessory();
	}
}