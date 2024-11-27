package l2.gameserver.stats.conditions;

import l2.gameserver.model.Zone;
import l2.gameserver.stats.Env;

public class ConditionZoneType extends Condition
{
	private final Zone.ZoneType _zoneType;
	
	public ConditionZoneType(String zoneType)
	{
		_zoneType = Zone.ZoneType.valueOf(zoneType);
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
		{
			return false;
		}
		return env.character.isInZone(_zoneType);
	}
}