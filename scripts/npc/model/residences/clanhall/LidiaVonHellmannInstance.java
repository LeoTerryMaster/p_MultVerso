package npc.model.residences.clanhall;

import l2.gameserver.model.AggroList;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.templates.npc.NpcTemplate;
import npc.model.residences.SiegeGuardInstance;

import java.util.HashMap;
import java.util.Map;

public class LidiaVonHellmannInstance extends SiegeGuardInstance
{
	public LidiaVonHellmannInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onDeath(Creature killer)
	{
		SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
		if(siegeEvent == null)
		{
			return;
		}
		siegeEvent.processStep(getMostDamagedClan());
		super.onDeath(killer);
	}
	
	public Clan getMostDamagedClan()
	{
		ClanHallSiegeEvent siegeEvent = getEvent(ClanHallSiegeEvent.class);
		Player temp = null;
		HashMap<Player, Integer> damageMap = new HashMap<>();
		for(AggroList.HateInfo info : getAggroList().getPlayableMap().values())
		{
			Playable killer = (Playable) info.attacker;
			int damage = info.damage;
			if(killer.isPet() || killer.isSummon())
			{
				temp = killer.getPlayer();
			}
			else if(killer.isPlayer())
			{
				temp = (Player) killer;
			}
			if(temp == null || siegeEvent.getSiegeClan("attackers", temp.getClan()) == null)
				continue;
			if(!damageMap.containsKey(temp))
			{
				damageMap.put(temp, damage);
				continue;
			}
			int dmg = damageMap.get(temp) + damage;
			damageMap.put(temp, dmg);
		}
		int mostDamage = 0;
		Player player = null;
		for(Map.Entry entry : damageMap.entrySet())
		{
			int damage = (Integer) entry.getValue();
			Player t = (Player) entry.getKey();
			if(damage <= mostDamage)
				continue;
			mostDamage = damage;
			player = t;
		}
		return player == null ? null : player.getClan();
	}
	
	@Override
	public boolean isEffectImmune()
	{
		return true;
	}
}