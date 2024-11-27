package npc.model;

import l2.commons.util.Rnd;
import l2.gameserver.cache.Msg;
import l2.gameserver.instancemanager.DimensionalRiftManager;
import l2.gameserver.model.Party;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.DelusionChamber;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;

import java.util.Map;

public final class DelustionGatekeeperInstance extends NpcInstance
{
	public DelustionGatekeeperInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		if(command.startsWith("enterDC"))
		{
			int izId = Integer.parseInt(command.substring(8));
			int type = izId - 120;
			Map rooms = DimensionalRiftManager.getInstance().getRooms(type);
			if(rooms == null)
			{
				player.sendPacket(Msg.SYSTEM_ERROR);
				return;
			}
			Reflection r = player.getActiveReflection();
			Party party;
			if(r != null)
			{
				if(player.canReenterInstance(izId))
				{
					player.teleToLocation(r.getTeleportLoc(), r);
				}
			}
			else if(player.canEnterInstance(izId) && (party = player.getParty()) != null)
			{
				new DelusionChamber(party, type, Rnd.get(1, rooms.size() - 1));
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}