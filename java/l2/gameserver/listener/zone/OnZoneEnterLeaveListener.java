package l2.gameserver.listener.zone;

import l2.commons.listener.Listener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Zone;

public interface OnZoneEnterLeaveListener extends Listener<Zone>
{
	void onZoneEnter(Zone zone, Creature actor);
	
	void onZoneLeave(Zone zone, Creature actor);
}