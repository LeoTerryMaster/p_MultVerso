package npc.model;

import bosses.SailrenManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ItemFunctions;

public final class SairlenGatekeeperInstance extends NpcInstance
{
	private static final int GAZKH = 8784;
	
	public SairlenGatekeeperInstance(int objectId, NpcTemplate template)
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
		if(command.startsWith("request_entrance"))
		{
			if(player.getLevel() < 75)
			{
				showChatWindow(player, "default/32109-3.htm");
			}
			else if(ItemFunctions.getItemCount(player, 8784) > 0)
			{
				int check = SailrenManager.canIntoSailrenLair(player);
				if(check == 1 || check == 2)
				{
					showChatWindow(player, "default/32109-5.htm");
				}
				else if(check == 3)
				{
					showChatWindow(player, "default/32109-4.htm");
				}
				else if(check == 4)
				{
					showChatWindow(player, "default/32109-1.htm");
				}
				else if(check == 0)
				{
					ItemFunctions.removeItem(player, 8784, (long) 1, true);
					SailrenManager.setSailrenSpawnTask();
					SailrenManager.entryToSailrenLair(player);
				}
			}
			else
			{
				showChatWindow(player, "default/32109-2.htm");
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}