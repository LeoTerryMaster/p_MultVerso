package npc.model;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;

public class TriolsMirrorInstance extends NpcInstance
{
	public TriolsMirrorInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		if(getNpcId() == 32040)
		{
			player.teleToLocation(-12766, -35840, -10856);
		}
		else if(getNpcId() == 32039)
		{
			player.teleToLocation(35079, -49758, -760);
		}
	}
}