package l2.gameserver.model.reward;

import l2.commons.math.SafeMath;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RewardGroup implements Cloneable
{
	private final List<RewardData> _items = new ArrayList<>();
	private double _chance;
	private boolean _isAdena;
	private boolean _isSealStone;
	private boolean _notRate;
	private double _chanceSum;
	
	public RewardGroup(double chance)
	{
		setChance(chance);
	}
	
	public boolean notRate()
	{
		return _notRate;
	}
	
	public void setNotRate(boolean notRate)
	{
		_notRate = notRate;
	}
	
	public double getChance()
	{
		return _chance;
	}
	
	public void setChance(double chance)
	{
		_chance = chance;
	}
	
	public boolean isAdena()
	{
		return _isAdena;
	}
	
	public boolean isSealStone()
	{
		return _isSealStone;
	}
	
	public void setIsAdena(boolean isAdena)
	{
		_isAdena = isAdena;
	}
	
	public void addData(RewardData item)
	{
		if(item.getItem().isAdena())
		{
			_isAdena = true;
		}
		else if(item.getItem().isSealStone())
		{
			_isSealStone = true;
		}
		_chanceSum += item.getChance();
		item.setChanceInGroup(_chanceSum);
		_items.add(item);
	}
	
	public List<RewardData> getItems()
	{
		return _items;
	}
	
	@Override
	public RewardGroup clone()
	{
		RewardGroup ret = new RewardGroup(_chance);
		for(RewardData i : _items)
		{
			ret.addData(i.clone());
		}
		return ret;
	}
	
	public List<RewardItem> roll(RewardType type, Player player, double mod, boolean isRaid, boolean isSiegeGuard)
	{
		switch(type)
		{
			case NOT_RATED_GROUPED:
			case NOT_RATED_NOT_GROUPED:
			{
				return rollItems(mod, 1.0, 1.0);
			}
			case SWEEP:
			{
				return rollSpoil(Config.RATE_DROP_SPOIL, player.getRateSpoil(), mod);
			}
			case RATED_GROUPED:
			{
				if(_isAdena)
				{
					return rollAdena(mod, player.getRateAdena());
				}
				if(_isSealStone)
				{
					return rollSealStones(mod, player.getRateItems());
				}
				if(isRaid)
				{
					return rollItems(mod, Config.RATE_DROP_RAIDBOSS * (double) player.getBonus().getDropRaidItems(), 1.0);
				}
				if(isSiegeGuard)
				{
					return rollItems(mod, Config.RATE_DROP_SIEGE_GUARD, 1.0);
				}
				return rollItems(mod, Config.RATE_DROP_ITEMS, player.getRateItems());
			}
		}
		return Collections.emptyList();
	}
	
	private List<RewardItem> rollSealStones(double mod, double playerRate)
	{
		List<RewardItem> ret = rollItems(mod, Config.RATE_DROP_SEAL_STONES, playerRate);
		for(RewardItem rewardItem : ret)
		{
			rewardItem.isSealStone = true;
		}
		return ret;
	}
	
	public List<RewardItem> rollItems(double mod, double baseRate, double playerRate)
	{
		if(mod <= 0.0)
		{
			return Collections.emptyList();
		}
		double rate = _notRate ? Math.min(mod, 1.0) : baseRate * playerRate * mod;
		double mult = Math.ceil(rate);
		boolean firstPass = true;
		ArrayList<RewardItem> ret = new ArrayList<>(_items.size() * 3 / 2);
		long n = 0;
		while((double) n < mult)
		{
			double gmult = rate - (double) n;
			if((double) Rnd.get(1, 1000000) <= _chance * Math.min(gmult, 1.0))
			{
				if(Config.ALT_MULTI_DROP)
				{
					rollFinal(_items, ret, 1.0, firstPass);
				}
				else
				{
					rollFinal(_items, ret, Math.max(gmult, 1.0), firstPass);
					break;
				}
			}
			firstPass = false;
			++n;
		}
		return ret;
	}
	
	private List<RewardItem> rollSpoil(double baseRate, double playerRate, double mod)
	{
		if(mod <= 0.0)
		{
			return Collections.emptyList();
		}
		double rate = _notRate ? Math.min(mod, 1.0) : baseRate * playerRate * mod;
		double mult = Math.ceil(rate);
		boolean firstPass = true;
		ArrayList<RewardItem> ret = new ArrayList<>(_items.size() * 3 / 2);
		long n = 0;
		while((double) n < mult)
		{
			if((double) Rnd.get(1, 1000000) <= _chance * Math.min(rate - (double) n, 1.0))
			{
				rollFinal(_items, ret, 1.0, firstPass);
			}
			firstPass = false;
			++n;
		}
		return ret;
	}
	
	private List<RewardItem> rollAdena(double mod, double playerRate)
	{
		return rollAdena(mod, Config.RATE_DROP_ADENA, playerRate);
	}
	
	private List<RewardItem> rollAdena(double mod, double baseRate, double playerRate)
	{
		double chance = _chance;
		if(mod > 10.0)
		{
			mod *= _chance / 1000000.0;
			chance = 1000000.0;
		}
		if(mod <= 0.0)
		{
			return Collections.emptyList();
		}
		if((double) Rnd.get(1, 1000000) > chance)
		{
			return Collections.emptyList();
		}
		double rate = baseRate * playerRate * mod;
		ArrayList<RewardItem> ret = new ArrayList<>(_items.size());
		rollFinal(_items, ret, rate, true);
		for(RewardItem i : ret)
		{
			i.isAdena = true;
		}
		return ret;
	}
	
	private void rollFinal(List<RewardData> items, List<RewardItem> ret, double mult, boolean firstPass)
	{
		int chance = Rnd.get(0, (int) Math.max(_chanceSum, 1000000.0));
		for(RewardData i : items)
		{
			if(!firstPass && i.onePassOnly() || (double) chance >= i.getChanceInGroup() || (double) chance <= i.getChanceInGroup() - i.getChance())
				continue;
			double imult = i.notRate() ? 1.0 : mult;
			long count = (long) Math.floor((double) i.getMinDrop() * imult);
			long max;
			if(count != (max = (long) Math.ceil((double) i.getMaxDrop() * imult)))
			{
				count = Rnd.get(count, max);
			}
			RewardItem t = null;
			for(RewardItem r : ret)
			{
				if(i.getItemId() != r.itemId)
					continue;
				t = r;
				break;
			}
			if(t == null)
			{
				t = new RewardItem(i.getItemId());
				ret.add(t);
				t.count = count;
				break;
			}
			if(i.notRate())
				break;
			t.count = SafeMath.addAndLimit(t.count, count);
			break;
		}
	}
}