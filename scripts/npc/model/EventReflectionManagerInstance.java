package npc.model;

import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ReflectionUtils;

public final class EventReflectionManagerInstance extends NpcInstance
{
	public EventReflectionManagerInstance(int objectId, NpcTemplate template)
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
		if(command.startsWith("event_instance"))
		{
			int val = Integer.parseInt(command.substring(15));
			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(val))
				{
					player.teleToLocation(r.getTeleportLoc(), r);
				}
			}
			else if(player.canEnterInstance(val))
			{
				ReflectionUtils.enterReflection(player, val);
			}
		}
		else if(command.startsWith("escape_event_instance"))
		{
			if(player.getParty() == null || !player.getParty().isLeader(player))
			{
				showChatWindow(player, "not_party_leader.htm");
				return;
			}
			player.getReflection().collapse();
		}
		else if(command.startsWith("return"))
		{
			Reflection r = player.getReflection();
			if(r.getReturnLoc() != null)
			{
				player.teleToLocation(r.getReturnLoc(), ReflectionManager.DEFAULT);
			}
			else
			{
				player.setReflection(ReflectionManager.DEFAULT);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		super.showChatWindow(player, val);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom = val == 0 ? "" + npcId : "" + npcId + "-" + val;
		return "instance/event_instance/" + pom + ".htm";
	}
}