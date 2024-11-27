package npc.model.residences;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.Experience;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.reward.RewardItem;
import l2.gameserver.model.reward.RewardList;
import l2.gameserver.model.reward.RewardType;
import l2.gameserver.stats.Stats;
import l2.gameserver.templates.npc.NpcTemplate;

import java.util.List;
import java.util.Map;

public class SiegeGuardInstance extends NpcInstance
{
	public SiegeGuardInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}
	
	@Override
	public boolean isSiegeGuard()
	{
		return true;
	}
	
	@Override
	public int getAggroRange()
	{
		return 1200;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		Player player = attacker.getPlayer();
		if(player == null)
		{
			return false;
		}
		SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
		SiegeEvent siegeEvent2 = attacker.getEvent(SiegeEvent.class);
		Clan clan = player.getClan();
		if(siegeEvent == null)
		{
			return false;
		}
		return clan == null || siegeEvent != siegeEvent2 || siegeEvent.getSiegeClan("defenders", clan) == null;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	public boolean isInvul()
	{
		return false;
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
		if(killer != null)
		{
			Player player = killer.getPlayer();
			if(siegeEvent != null && player != null)
			{
				Clan clan = player.getClan();
				SiegeEvent siegeEvent2 = killer.getEvent(SiegeEvent.class);
				if(clan != null && siegeEvent == siegeEvent2 && siegeEvent.getSiegeClan("defenders", clan) == null)
				{
					Creature topdam = getAggroList().getTopDamager();
					if(topdam == null)
					{
						topdam = killer;
					}
					for(Map.Entry<RewardType, RewardList> entry : getTemplate().getRewards().entrySet())
					{
						rollRewards(entry, killer, topdam);
					}
				}
			}
		}
		super.onDeath(killer);
	}
	
	public void rollRewards(Map.Entry<RewardType, RewardList> entry, Creature lastAttacker, Creature topDamager)
	{
		RewardList list = entry.getValue();
		Player activePlayer = topDamager.getPlayer();
		if(activePlayer == null)
		{
			return;
		}
		int diff = calculateLevelDiffForDrop(topDamager.getLevel());
		double mod = calcStat(Stats.ITEM_REWARD_MULTIPLIER, 1.0, topDamager, null);
		mod *= Experience.penaltyModifier((long) diff, 9.0);
		List<RewardItem> rewardItems = list.roll(activePlayer, mod, false, true);
		for(RewardItem drop : rewardItems)
		{
			dropItem(activePlayer, drop.itemId, drop.count);
		}
	}
	
	@Override
	public boolean isFearImmune()
	{
		return true;
	}
	
	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}
	
	@Override
	public Clan getClan()
	{
		return null;
	}
}