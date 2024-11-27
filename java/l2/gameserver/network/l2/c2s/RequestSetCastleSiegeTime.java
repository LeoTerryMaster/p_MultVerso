package l2.gameserver.network.l2.c2s;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.CastleSiegeInfo;

public class RequestSetCastleSiegeTime extends L2GameClientPacket
{
	private int _id;
	private int _time;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
		_time = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _id);
		if(castle == null)
		{
			return;
		}
		if(player.getClan().getCastle() != castle.getId())
		{
			return;
		}
		if((player.getClanPrivileges() & 131072) != 131072)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_SIEGE_TIME);
			return;
		}
		CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
		siegeEvent.setNextSiegeTime(_time);
		player.sendPacket(new CastleSiegeInfo(castle, player));
	}
}