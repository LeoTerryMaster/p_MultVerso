package l2.gameserver.network.l2.c2s;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.network.l2.s2c.CastleSiegeAttackerList;

public class RequestCastleSiegeAttackerList extends L2GameClientPacket
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
		Object residence = ResidenceHolder.getInstance().getResidence(_unitId);
		if(residence != null)
		{
			sendPacket(new CastleSiegeAttackerList((Residence) residence));
		}
	}
}