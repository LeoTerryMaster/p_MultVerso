package services;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.DoorInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.ReflectionUtils;

public class PaganDoormans extends Functions
{
	private static final int MainDoorId = 19160001;
	private static final int SecondDoor1Id = 19160011;
	private static final int SecondDoor2Id = 19160010;
	private static final int q_mark_of_sacrifice = 8064;
	private static final int q_faded_mark_of_sac = 8065;
	private static final int q_mark_of_heresy = 8067;
	
	private static void openDoor(int doorId)
	{
		DoorInstance door = ReflectionUtils.getDoor(doorId);
		door.openMe();
	}
	
	public void openMainDoor()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		if(!NpcInstance.canBypassCheck(player, npc))
		{
			return;
		}
		long items = getItemCount(player, 8064);
		if(items == 0 && getItemCount(player, 8067) == 0)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
			return;
		}
		if(items > 0)
		{
			removeItem(player, 8064, items);
			addItem(player, 8065, (long) 1);
		}
		openDoor(19160001);
		show("default/32034-1.htm", player, npc, (Object[]) new Object[0]);
	}
	
	public void openSecondDoor()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		if(!NpcInstance.canBypassCheck(player, npc))
		{
			return;
		}
		if(getItemCount(player, 8067) == 0)
		{
			show("default/32036-2.htm", player, npc, (Object[]) new Object[0]);
			return;
		}
		openDoor(19160011);
		openDoor(19160010);
		show("default/32036-1.htm", player, npc, (Object[]) new Object[0]);
	}
	
	public void pressSkull()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		if(!NpcInstance.canBypassCheck(player, npc))
		{
			return;
		}
		openDoor(19160001);
		show("default/32035-1.htm", player, npc, (Object[]) new Object[0]);
	}
	
	public void press2ndSkull()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		if(!NpcInstance.canBypassCheck(player, npc))
		{
			return;
		}
		openDoor(19160011);
		openDoor(19160010);
		show("default/32037-1.htm", player, npc, (Object[]) new Object[0]);
	}
}