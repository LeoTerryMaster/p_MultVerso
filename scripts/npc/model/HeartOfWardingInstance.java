package npc.model;

import bosses.AntharasManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;

public final class HeartOfWardingInstance extends NpcInstance
{
	public HeartOfWardingInstance(int objectId, NpcTemplate template)
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
		if(command.equalsIgnoreCase("enter_lair"))
		{
			AntharasManager.enterTheLair(player);
			return;
		}
		super.onBypassFeedback(player, command);
	}
}