package npc.model;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.DoorInstance;
import l2.gameserver.model.instances.GuardInstance;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ReflectionUtils;

public class BorderOutpostDoormanInstance extends GuardInstance
{
	public BorderOutpostDoormanInstance(int objectId, NpcTemplate template)
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
		if(command.equals("openDoor"))
		{
			DoorInstance door = ReflectionUtils.getDoor(24170001);
			door.openMe();
		}
		else if(command.equals("closeDoor"))
		{
			DoorInstance door = ReflectionUtils.getDoor(24170001);
			door.closeMe();
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}