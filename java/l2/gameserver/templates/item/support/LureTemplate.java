package l2.gameserver.templates.item.support;

import l2.commons.collections.MultiValueSet;

import java.util.Map;

public class LureTemplate
{
	private final int _itemId;
	private final int _lengthBonus;
	private final double _revisionNumber;
	private final double _rateBonus;
	private final LureType _lureType;
	private final Map<FishGroup, Integer> _chances;
	
	public LureTemplate(MultiValueSet<String> set)
	{
		_itemId = set.getInteger("item_id");
		_lengthBonus = set.getInteger("length_bonus");
		_revisionNumber = set.getDouble("revision_number");
		_rateBonus = set.getDouble("rate_bonus");
		_lureType = set.getEnum("type", LureType.class);
		_chances = (Map) set.get("chances");
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getLengthBonus()
	{
		return _lengthBonus;
	}
	
	public double getRevisionNumber()
	{
		return _revisionNumber;
	}
	
	public double getRateBonus()
	{
		return _rateBonus;
	}
	
	public LureType getLureType()
	{
		return _lureType;
	}
	
	public Map<FishGroup, Integer> getChances()
	{
		return _chances;
	}
}