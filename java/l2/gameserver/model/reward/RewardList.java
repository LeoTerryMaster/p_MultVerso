package l2.gameserver.model.reward;

import l2.gameserver.model.Player;

import java.util.ArrayList;
import java.util.List;

public class RewardList extends ArrayList<RewardGroup>
{
	public static final int MAX_CHANCE = 1000000;
	private final RewardType _type;
	private final boolean _autoLoot;
	
	public RewardList(RewardType rewardType, boolean a)
	{
		super(5);
		_type = rewardType;
		_autoLoot = a;
	}
	
	public List<RewardItem> roll(Player player)
	{
		return roll(player, 1.0, false, false);
	}
	
	public List<RewardItem> roll(Player player, double mod)
	{
		return roll(player, mod, false, false);
	}
	
	public List<RewardItem> roll(Player player, double mod, boolean isRaid)
	{
		return roll(player, mod, isRaid, false);
	}
	
	public List<RewardItem> roll(Player player, double mod, boolean isRaid, boolean isSiegeGuard)
	{
		ArrayList<RewardItem> temp = new ArrayList<>(size());
		for(RewardGroup g : this)
		{
			List<RewardItem> tdl = g.roll(_type, player, mod, isRaid, isSiegeGuard);
			if(tdl.isEmpty())
				continue;
			for(RewardItem itd : tdl)
			{
				temp.add(itd);
			}
		}
		return temp;
	}
	
	public boolean validate()
	{
		for(RewardGroup g : this)
		{
			int chanceSum = 0;
			for(RewardData d : g.getItems())
			{
				chanceSum = (int) ((double) chanceSum + d.getChance());
			}
			if(chanceSum <= 1000000)
			{
				return true;
			}
			double mod = 1000000 / chanceSum;
			for(RewardData d : g.getItems())
			{
				double chance = d.getChance() * mod;
				d.setChance(chance);
				g.setChance(1000000.0);
			}
		}
		return false;
	}
	
	public boolean isAutoLoot()
	{
		return _autoLoot;
	}
	
	public RewardType getType()
	{
		return _type;
	}
}