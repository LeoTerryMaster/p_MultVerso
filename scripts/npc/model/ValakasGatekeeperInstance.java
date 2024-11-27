package npc.model;

import bosses.ValakasManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;

public final class ValakasGatekeeperInstance extends NpcInstance
{
	private static final int FLOATING_STONE = 7267;
	private static final Location TELEPORT_POSITION1 = new Location(183831, -115457, -3296);
	
	public ValakasGatekeeperInstance(int objectId, NpcTemplate template)
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
		if(command.equalsIgnoreCase("request_passage"))
		{
			if(!ValakasManager.isEnableEnterToLair())
			{
				player.sendMessage("Valakas is now reborning and there's no way to enter the hall now.");
				return;
			}
			if(player.getInventory().getCountOf(7267) < 1)
			{
				player.sendMessage("In order to enter the Hall of Flames you should carry at least one Flotaing Stone");
				return;
			}
			player.getInventory().destroyItemByItemId(7267, 1);
			player.teleToLocation(TELEPORT_POSITION1);
			return;
		}
		if(command.equalsIgnoreCase("request_valakas"))
		{
			ValakasManager.enterTheLair(player);
			return;
		}
		super.onBypassFeedback(player, command);
	}
}