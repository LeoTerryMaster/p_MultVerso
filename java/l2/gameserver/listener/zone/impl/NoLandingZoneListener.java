package l2.gameserver.listener.zone.impl;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Zone;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.network.l2.components.SystemMsg;

public class NoLandingZoneListener implements OnZoneEnterLeaveListener
{
	public static final OnZoneEnterLeaveListener STATIC = new NoLandingZoneListener();
	
	@Override
	public void onZoneEnter(Zone zone, Creature actor)
	{
		Residence residence = ResidenceHolder.getInstance().getResidence(zone.getParams().getInteger("residence", 0));
		Player player = actor.getPlayer();
		if(player != null && player.isFlying() && player.getMountNpcId() == 12621 && (residence == null || player.getClan() == null || residence.getOwner() != player.getClan()))
		{
			player.stopMove();
			player.sendPacket(SystemMsg.THIS_AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_ATOP_OF_A_WYVERN);
			player.setMount(0, 0, 0);
		}
	}
	
	@Override
	public void onZoneLeave(Zone zone, Creature cha)
	{
	}
}