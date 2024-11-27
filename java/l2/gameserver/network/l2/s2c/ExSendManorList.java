package l2.gameserver.network.l2.s2c;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.entity.residence.Residence;

import java.util.List;

public class ExSendManorList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(27);
		List<Castle> residences = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		writeD(residences.size());
		for(Residence castle : residences)
		{
			writeD(castle.getId());
			writeS(castle.getName().toLowerCase());
		}
	}
}