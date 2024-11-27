package npc.model;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.MerchantInstance;
import l2.gameserver.network.l2.s2c.PackageToList;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.WarehouseFunctions;

public class FreightSenderInstance extends MerchantInstance
{
	public FreightSenderInstance(int objectId, NpcTemplate template)
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
		if(command.equalsIgnoreCase("deposit_items"))
		{
			player.sendPacket(new PackageToList(player));
		}
		else if(command.equalsIgnoreCase("withdraw_items"))
		{
			WarehouseFunctions.showFreightWindow(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}