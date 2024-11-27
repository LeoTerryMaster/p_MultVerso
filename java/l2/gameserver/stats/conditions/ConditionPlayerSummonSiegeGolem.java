package l2.gameserver.stats.conditions;

import l2.gameserver.model.Player;
import l2.gameserver.model.Zone;
import l2.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.stats.Env;

public class ConditionPlayerSummonSiegeGolem extends Condition
{
	@Override
	protected boolean testImpl(Env env)
	{
		Player player = env.character.getPlayer();
		if(player == null)
		{
			return false;
		}
		Zone zone = player.getZone(Zone.ZoneType.RESIDENCE);
		if(zone != null)
		{
			return false;
		}
		zone = player.getZone(Zone.ZoneType.SIEGE);
		if(zone == null)
		{
			return false;
		}
		SiegeEvent event = player.getEvent(SiegeEvent.class);
		if(event == null)
		{
			return false;
		}
		if(event instanceof CastleSiegeEvent)
		{
			if(zone.getParams().getInteger("residence") != event.getId())
			{
				return false;
			}
			if(event.getSiegeClan("attackers", player.getClan()) == null)
			{
				return false;
			}
		}
		else if(event.getSiegeClan("defenders", player.getClan()) == null)
		{
			return false;
		}
		return true;
	}
}