package npc.model;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.WarehouseInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.scripts.Functions;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.npc.NpcTemplate;

public class ArenaManagerInstance extends WarehouseInstance
{
	private static final int RECOVER_CP_SKILLID = 4380;
	
	public ArenaManagerInstance(int objectId, NpcTemplate template)
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
		if(!player.isInPeaceZone() || player.isCursedWeaponEquipped())
		{
			return;
		}
		if(command.startsWith("CPRecovery"))
		{
			if(Functions.getItemCount(player, 57) >= 100)
			{
				Functions.removeItem(player, 57, (long) 100);
				doCast(SkillTable.getInstance().getInfo(4380, 1), player, true);
			}
			else
			{
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}