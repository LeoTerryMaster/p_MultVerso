package l2.gameserver.handler.admincommands.impl;

import l2.commons.util.Rnd;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.instancemanager.MapRegionManager;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.Zone;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.network.l2.s2c.DropItem;
import l2.gameserver.templates.mapregion.DomainArea;

import java.util.ArrayList;

public class AdminZone implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(activeChar == null || !activeChar.getPlayerAccess().CanTeleport)
		{
			return false;
		}
		switch(command)
		{
			case admin_zone_check:
			{
				activeChar.sendMessage("Current region: " + activeChar.getCurrentRegion());
				activeChar.sendMessage("Zone list:");
				ArrayList<Zone> zones = new ArrayList<>();
				World.getZones(zones, activeChar.getLoc(), activeChar.getReflection());
				for(Zone zone : zones)
				{
					activeChar.sendMessage(zone.getType() + ", name: " + zone.getName() + ", state: " + (zone.isActive() ? "active" : "not active") + ", inside: " + zone.checkIfInZone(activeChar) + "/" + zone.checkIfInZone(activeChar.getX(), activeChar.getY(), activeChar.getZ()));
				}
				break;
			}
			case admin_region:
			{
				activeChar.sendMessage("Current region: " + activeChar.getCurrentRegion());
				activeChar.sendMessage("Objects list:");
				for(GameObject o : activeChar.getCurrentRegion())
				{
					if(o == null)
						continue;
					activeChar.sendMessage(o.toString());
				}
				break;
			}
			case admin_vis_count:
			{
				activeChar.sendMessage("Current region: " + activeChar.getCurrentRegion());
				activeChar.sendMessage("Players count: " + World.getAroundPlayers(activeChar).size());
				break;
			}
			case admin_pos:
			{
				String pos = "" + activeChar.getX() + ", " + activeChar.getY() + ", " + activeChar.getZ() + ", " + activeChar.getHeading() + " Geo [" + (activeChar.getX() - World.MAP_MIN_X >> 4) + ", " + (activeChar.getY() - World.MAP_MIN_Y >> 4) + "] Ref " + activeChar.getReflectionId();
				activeChar.sendMessage("Pos: " + pos);
				activeChar.sendPacket(new DropItem(0, 16777215 & Rnd.nextInt(), 57, activeChar.getLoc().clone().setZ(activeChar.getZ() + 64), false, 1));
				break;
			}
			case admin_domain:
			{
				Castle castle;
				DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, activeChar);
				Castle castle2 = castle = domain != null ? ResidenceHolder.getInstance().getResidence(Castle.class, domain.getId()) : null;
				if(castle != null)
				{
					activeChar.sendMessage("Domain: " + castle.getName());
					break;
				}
				activeChar.sendMessage("Domain: Unknown");
			}
		}
		return true;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private enum Commands
	{
		admin_zone_check,
		admin_region,
		admin_pos,
		admin_vis_count,
		admin_domain;
	}
}