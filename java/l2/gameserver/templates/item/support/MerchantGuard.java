package l2.gameserver.templates.item.support;

import l2.gameserver.model.entity.SevenSigns;
import org.napile.primitive.sets.IntSet;

public class MerchantGuard
{
	private final int _itemId;
	private final int _npcId;
	private final int _max;
	private final IntSet _ssq;
	
	public MerchantGuard(int itemId, int npcId, int max, IntSet ssq)
	{
		_itemId = itemId;
		_npcId = npcId;
		_max = max;
		_ssq = ssq;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public int getMax()
	{
		return _max;
	}
	
	public boolean isValidSSQPeriod()
	{
		return SevenSigns.getInstance().getCurrentPeriod() == 3 && _ssq.contains(SevenSigns.getInstance().getSealOwner(3));
	}
}