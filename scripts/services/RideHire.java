package services;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SetupGauge;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.ItemFunctions;

public class RideHire extends Functions
{
	private static boolean canBeStarted()
	{
		for(Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
		{
			if(c.getSiegeEvent() == null || !c.getSiegeEvent().isInProgress())
				continue;
			return false;
		}
		return true;
	}
	
	public String DialogAppend_30827(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String getHtmlAppends(Integer val)
	{
		Player player = getSelf();
		if(Config.SERVICES_ALLOW_WYVERN_RIDE)
		{
			return player.isLangRus() ? "<br>[scripts_services.RideHire:ride_prices|Взять на прокат ездовое животное.]" : "<br>[scripts_services.RideHire:ride_prices|Ride hire mountable pet.]";
		}
		return "";
	}
	
	public void ride_prices()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		show("scripts/services/ride-prices.htm", player, npc, (Object[]) new Object[0]);
	}
	
	public void ride(String[] args)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		boolean ru = player.isLangRus();
		if(args.length != 3)
		{
			show(ru ? "Некорректные данные" : "Incorrect input", player, npc, (Object[]) new Object[0]);
			return;
		}
		if(!NpcInstance.canBypassCheck(player, npc))
		{
			return;
		}
		if(player.getActiveWeaponFlagAttachment() != null)
		{
			player.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
			return;
		}
		if(player.getTransformation() != 0)
		{
			show(ru ? "Вы не можете взять пета в прокат, пока находитесь в режиме трансформации." : "Can't ride while in transformation mode.", player, npc, (Object[]) new Object[0]);
			return;
		}
		if(player.getPet() != null || player.isMounted())
		{
			player.sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
			return;
		}
		int npc_id;
		switch(Integer.parseInt(args[0]))
		{
			case 1:
			{
				npc_id = 12621;
				break;
			}
			case 2:
			{
				npc_id = 12526;
				break;
			}
			default:
			{
				show(ru ? "У меня нет таких питомцев!" : "Unknown pet.", player, npc, (Object[]) new Object[0]);
				return;
			}
		}
		if(!(npc_id != 12621 && npc_id != 12526 || canBeStarted()))
		{
			show(ru ? "Прокат виверн/страйдеров не работает во время осады." : "Can't ride wyvern/strider while Siege in progress.", player, npc, (Object[]) new Object[0]);
			return;
		}
		Integer time = Integer.parseInt(args[1]);
		Long price = Long.parseLong(args[2]);
		if(time > 3600)
		{
			show(ru ? "Слишком большое время." : "Too long time to ride.", player, npc, (Object[]) new Object[0]);
			return;
		}
		if(ItemFunctions.getItemCount(player, Config.SERVICES_WYVERN_ITEM_ID) < price)
		{
			if(Config.SERVICES_WYVERN_ITEM_ID == 57)
			{
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			}
			else
			{
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			}
			return;
		}
		ItemFunctions.removeItem(player, Config.SERVICES_WYVERN_ITEM_ID, price, true);
		doLimitedRide(player, npc_id, time);
	}
	
	public void doLimitedRide(Player player, Integer npc_id, Integer time)
	{
		if(!ride(player, npc_id))
		{
			return;
		}
		player.sendPacket(new SetupGauge(player, 3, time * 1000));
		executeTask(player, "services.RideHire", "rideOver", new Object[0], (long) (time * 1000));
	}
	
	public void rideOver()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		unRide(player);
		show(player.isLangRus() ? "Время проката закончилось. Приходите еще!" : "Ride time is over.<br><br>Welcome back again!", player);
	}
}