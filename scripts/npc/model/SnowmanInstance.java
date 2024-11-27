package npc.model;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;

public class SnowmanInstance extends NpcInstance
{
	public SnowmanInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		player.sendActionFailed();
	}
}