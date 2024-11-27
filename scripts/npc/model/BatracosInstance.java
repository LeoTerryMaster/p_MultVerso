package npc.model;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ReflectionUtils;

public final class BatracosInstance extends NpcInstance
{
	private static final int urogosIzId = 505;
	
	public BatracosInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		if(val == 0)
		{
			String htmlpath = getReflection().isDefault() ? "default/32740.htm" : "default/32740-4.htm";
			player.sendPacket(new NpcHtmlMessage(player, this, htmlpath, 0));
		}
		else
		{
			super.showChatWindow(player, val);
		}
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		if(command.equalsIgnoreCase("request_seer"))
		{
			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(505))
				{
					player.teleToLocation(r.getTeleportLoc(), r);
				}
			}
			else if(player.canEnterInstance(505))
			{
				ReflectionUtils.enterReflection(player, 505);
			}
		}
		else if(command.equalsIgnoreCase("leave"))
		{
			if(!getReflection().isDefault())
			{
				getReflection().collapse();
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}