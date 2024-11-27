package handler.items;

import l2.gameserver.dao.CastleHiredGuardDAO;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.pledge.Privilege;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ActionFail;
import l2.gameserver.templates.item.support.MerchantGuard;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.PositionUtils;
import org.napile.primitive.sets.impl.HashIntSet;

import java.util.List;

public class MercTicket extends ScriptItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		return false;
	}
	
	@Override
	public void dropItem(Player player, ItemInstance item, long count, Location loc)
	{
		if(!player.hasPrivilege(Privilege.CS_FS_MERCENARIES) || player.getClan().getCastle() == 0)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_POSITION_MERCENARIES, ActionFail.STATIC);
			return;
		}
		Castle castle = player.getCastle();
		MerchantGuard guard = castle.getMerchantGuard(item.getItemId());
		if(guard == null || !castle.checkIfInZone(loc, ReflectionManager.DEFAULT) || player.isActionBlocked("drop_merchant_guard"))
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_POSITION_MERCENARIES_HERE, ActionFail.STATIC);
			return;
		}
		if(castle.getSiegeEvent().isInProgress() || !guard.isValidSSQPeriod())
		{
			player.sendPacket(SystemMsg.A_MERCENARY_CAN_BE_ASSIGNED_TO_A_POSITION_FROM_THE_BEGINNING_OF_THE_SEAL_VALIDATION_PERIOD_UNTIL_THE_TIME_WHEN_A_SIEGE_STARTS, ActionFail.STATIC);
			return;
		}
		int countOfGuard = 0;
		for(ItemInstance ticket : castle.getSpawnMerchantTickets())
		{
			if(PositionUtils.getDistance(ticket.getLoc(), loc) < 200.0)
			{
				player.sendPacket(SystemMsg.POSITIONING_CANNOT_BE_DONE_HERE_BECAUSE_THE_DISTANCE_BETWEEN_MERCENARIES_IS_TOO_SHORT, ActionFail.STATIC);
				return;
			}
			if(ticket.getItemId() != guard.getItemId())
				continue;
			++countOfGuard;
		}
		if(countOfGuard >= guard.getMax())
		{
			player.sendPacket(SystemMsg.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE, ActionFail.STATIC);
			return;
		}
		item = player.getInventory().removeItemByObjectId(item.getObjectId(), 1);
		if(item == null)
		{
			player.sendActionFailed();
			return;
		}
		Log.LogItem(player, Log.ItemLog.Drop, item);
		item.dropToTheGround(player, loc);
		player.disableDrop(1000);
		player.sendChanges();
		item.delete();
		castle.getSpawnMerchantTickets().add(item);
		CastleHiredGuardDAO.getInstance().insert(castle, item.getItemId(), item.getLoc());
	}
	
	@Override
	public boolean pickupItem(Playable playable, ItemInstance item)
	{
		if(!playable.isPlayer())
		{
			return false;
		}
		Player player = (Player) playable;
		if(!player.hasPrivilege(Privilege.CS_FS_MERCENARIES) || player.getClan().getCastle() == 0)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_CANCEL_MERCENARY_POSITIONING);
			return false;
		}
		Castle castle = player.getCastle();
		if(!castle.getSpawnMerchantTickets().contains(item))
		{
			player.sendPacket(SystemMsg.THIS_IS_NOT_A_MERCENARY_OF_A_CASTLE_THAT_YOU_OWN_AND_SO_YOU_CANNOT_CANCEL_ITS_POSITIONING);
			return false;
		}
		if(castle.getSiegeEvent().isInProgress())
		{
			player.sendPacket(SystemMsg.A_MERCENARY_CAN_BE_ASSIGNED_TO_A_POSITION_FROM_THE_BEGINNING_OF_THE_SEAL_VALIDATION_PERIOD_UNTIL_THE_TIME_WHEN_A_SIEGE_STARTS, ActionFail.STATIC);
			return false;
		}
		castle.getSpawnMerchantTickets().remove(item);
		CastleHiredGuardDAO.getInstance().delete(castle, item);
		return true;
	}
	
	@Override
	public final int[] getItemIds()
	{
		HashIntSet set = new HashIntSet(100);
		List<Castle> castles = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		for(Castle c : castles)
		{
			set.addAll(c.getMerchantGuards().keySet());
		}
		return set.toArray();
	}
}