package l2.gameserver.model.reward;

import l2.commons.math.SafeMath;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.Player;
import l2.gameserver.templates.item.ItemTemplate;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class RewardData implements Cloneable
{
	private final ItemTemplate _item;
	private boolean _notRate;
	private boolean _onePassOnly;
	private long _mindrop;
	private long _maxdrop;
	private double _chance;
	private double _chanceInGroup;
	
	public RewardData(int itemId)
	{
		_item = ItemHolder.getInstance().getTemplate(itemId);
		setNotRate(_item.isArrow() || Config.NO_RATE_EQUIPMENT && _item.isEquipment() || Config.NO_RATE_KEY_MATERIAL && _item.isKeyMatherial() || Config.NO_RATE_RECIPES && _item.isRecipe(), ArrayUtils.contains(Config.NO_RATE_ITEMS, itemId));
	}
	
	public RewardData(int itemId, long min, long max, double chance)
	{
		this(itemId);
		_mindrop = min;
		_maxdrop = max;
		_chance = chance;
	}
	
	public boolean notRate()
	{
		return _notRate;
	}
	
	public boolean onePassOnly()
	{
		return _onePassOnly;
	}
	
	public void setNotRate(boolean notRate, boolean onePassOnly)
	{
		_notRate = notRate || onePassOnly;
		_onePassOnly = onePassOnly;
	}
	
	public int getItemId()
	{
		return _item.getItemId();
	}
	
	public ItemTemplate getItem()
	{
		return _item;
	}
	
	public long getMinDrop()
	{
		return _mindrop;
	}
	
	public void setMinDrop(long mindrop)
	{
		_mindrop = mindrop;
	}
	
	public long getMaxDrop()
	{
		return _maxdrop;
	}
	
	public void setMaxDrop(long maxdrop)
	{
		_maxdrop = maxdrop;
	}
	
	public double getChance()
	{
		return _chance;
	}
	
	public void setChance(double chance)
	{
		_chance = chance;
	}
	
	public double getChanceInGroup()
	{
		return _chanceInGroup;
	}
	
	public void setChanceInGroup(double chance)
	{
		_chanceInGroup = chance;
	}
	
	@Override
	public String toString()
	{
		return "ItemID: " + getItem() + " Min: " + getMinDrop() + " Max: " + getMaxDrop() + " Chance: " + getChance() / 10000.0 + "%";
	}
	
	@Override
	public RewardData clone()
	{
		return new RewardData(getItemId(), getMinDrop(), getMaxDrop(), getChance());
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof RewardData)
		{
			RewardData drop = (RewardData) o;
			return drop.getItemId() == getItemId();
		}
		return false;
	}
	
	public List<RewardItem> roll(Player player, double mod)
	{
		double rate = _item.isAdena() ? Config.RATE_DROP_ADENA * player.getRateAdena() : Config.RATE_DROP_ITEMS * (player != null ? player.getRateItems() : 1.0);
		return roll(rate * mod);
	}
	
	public List<RewardItem> roll(double rate)
	{
		double mult = Math.ceil(rate);
		ArrayList<RewardItem> ret = new ArrayList<>(1);
		RewardItem t = null;
		int n = 0;
		while((double) n < mult)
		{
			if((double) Rnd.get(1000000) <= _chance * Math.min(rate - (double) n, 1.0))
			{
				long count = getMinDrop() >= getMaxDrop() ? getMinDrop() : Rnd.get(getMinDrop(), getMaxDrop());
				if(t == null)
				{
					t = new RewardItem(_item.getItemId());
					ret.add(t);
					t.count = count;
				}
				else
				{
					t.count = SafeMath.addAndLimit(t.count, count);
				}
			}
			++n;
		}
		return ret;
	}
}