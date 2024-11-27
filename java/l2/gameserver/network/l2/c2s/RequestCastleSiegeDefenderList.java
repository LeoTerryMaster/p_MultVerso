package l2.gameserver.network.l2.c2s;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.network.l2.s2c.CastleSiegeDefenderList;

public class RequestCastleSiegeDefenderList extends L2GameClientPacket
{
	private int _unitId;
	
	@Override
	protected void readImpl()
	{
		_unitId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _unitId);
		if(castle == null || castle.getOwner() == null)
		{
			return;
		}
		player.sendPacket(new CastleSiegeDefenderList(castle));
	}
}