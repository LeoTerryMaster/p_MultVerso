package l2.gameserver.templates;

public class SkillEnchant
{
	private final int _skillId;
	private final int _skillLevel;
	private final int _enchantLevel;
	private final int _routeId;
	private final long _exp;
	private final int _sp;
	private final int[] _chances;
	private final int _itemId;
	private final long _itemCount;
	
	public SkillEnchant(int skillId, int skillLevel, int enchantLevel, int routeId, long exp, int sp, int[] chances, int itemId, long itemCount)
	{
		_skillId = skillId;
		_skillLevel = skillLevel;
		_enchantLevel = enchantLevel;
		_routeId = routeId;
		_exp = exp;
		_sp = sp;
		_chances = chances;
		_itemId = itemId;
		_itemCount = itemCount;
	}
	
	public int[] getChances()
	{
		return _chances;
	}
	
	public long getExp()
	{
		return _exp;
	}
	
	public long getItemCount()
	{
		return _itemCount;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}
	
	public int getRouteId()
	{
		return _routeId;
	}
	
	public int getSkillId()
	{
		return _skillId;
	}
	
	public int getSkillLevel()
	{
		return _skillLevel;
	}
	
	public int getSp()
	{
		return _sp;
	}
}