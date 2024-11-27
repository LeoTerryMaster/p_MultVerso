package npc.model;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.DoorInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.ExChangeClientEffectInfo;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;
import org.apache.commons.lang3.ArrayUtils;

public class SirraInstance extends NpcInstance
{
	private static final int[] questInstances = {140, 138, 141};
	private static final int[] warInstances = {139, 144};
	
	public SirraInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		DoorInstance door = getReflection().getDoor(23140101);
		String htmlpath = ArrayUtils.contains(questInstances, getReflection().getInstancedZoneId()) ? "default/32762.htm" : ArrayUtils.contains(warInstances, getReflection().getInstancedZoneId()) ? door.isOpen() ? "default/32762_opened.htm" : "default/32762_closed.htm" : "default/32762.htm";
		return htmlpath;
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		if(command.equalsIgnoreCase("teleport_in"))
		{
			for(NpcInstance n : getReflection().getNpcs())
			{
				if(n.getNpcId() != 29179 && n.getNpcId() != 29180)
					continue;
				player.sendPacket(new ExChangeClientEffectInfo(2));
			}
			player.teleToLocation(new Location(114712, -113544, -11225));
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}