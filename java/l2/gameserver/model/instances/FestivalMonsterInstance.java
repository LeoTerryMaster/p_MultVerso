package l2.gameserver.model.instances;

import l2.commons.util.Rnd;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.reward.RewardList;
import l2.gameserver.model.reward.RewardType;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ItemFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FestivalMonsterInstance extends MonsterInstance
{
	protected int _bonusMultiplier = 1;
	
	public FestivalMonsterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		_hasRandomWalk = false;
	}
	
	public void setOfferingBonus(int bonusMultiplier)
	{
		_bonusMultiplier = bonusMultiplier;
	}
	
	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		List<Player> pl = World.getAroundPlayers(this);
		if(pl.isEmpty())
		{
			return;
		}
		ArrayList<Player> alive = new ArrayList<>(9);
		for(Player p : pl)
		{
			if(p.isDead())
				continue;
			alive.add(p);
		}
		if(alive.isEmpty())
		{
			return;
		}
		Player target = alive.get(Rnd.get(alive.size()));
		getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 1);
	}
	
	@Override
	public void rollRewards(Map.Entry<RewardType, RewardList> entry, Creature lastAttacker, Creature topDamager)
	{
		super.rollRewards(entry, lastAttacker, topDamager);
		if(entry.getKey() != RewardType.RATED_GROUPED)
		{
			return;
		}
		if(!topDamager.isPlayable())
		{
			return;
		}
		Player topDamagerPlayer = topDamager.getPlayer();
		Party associatedParty = topDamagerPlayer.getParty();
		if(associatedParty == null)
		{
			return;
		}
		Player partyLeader = associatedParty.getPartyLeader();
		if(partyLeader == null)
		{
			return;
		}
		ItemInstance bloodOfferings = ItemFunctions.createItem(5901);
		bloodOfferings.setCount(_bonusMultiplier);
		partyLeader.getInventory().addItem(bloodOfferings);
		partyLeader.sendPacket(SystemMessage2.obtainItems(5901, _bonusMultiplier, 0));
	}
	
	@Override
	public boolean isAggressive()
	{
		return true;
	}
	
	@Override
	public int getAggroRange()
	{
		return 1000;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	public boolean canChampion()
	{
		return false;
	}
}