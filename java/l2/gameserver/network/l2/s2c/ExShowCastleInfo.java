package l2.gameserver.network.l2.s2c;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.tables.ClanTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExShowCastleInfo extends L2GameServerPacket
{
	private List<CastleInfo> _infos = Collections.emptyList();
	
	public ExShowCastleInfo()
	{
		List<Castle> castles = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		_infos = new ArrayList<>(castles.size());
		for(Castle castle : castles)
		{
			String ownerName = ClanTable.getInstance().getClanName(castle.getOwnerId());
			int id = castle.getId();
			int tax = castle.getTaxPercent();
			int nextSiege = (int) (castle.getSiegeDate().getTimeInMillis() / 1000);
			_infos.add(new CastleInfo(ownerName, id, tax, nextSiege));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(20);
		writeD(_infos.size());
		for(CastleInfo info : _infos)
		{
			writeD(info._id);
			writeS(info._ownerName);
			writeD(info._tax);
			writeD(info._nextSiege);
		}
		_infos.clear();
	}
	
	private static class CastleInfo
	{
		public String _ownerName;
		public int _id;
		public int _tax;
		public int _nextSiege;
		
		public CastleInfo(String ownerName, int id, int tax, int nextSiege)
		{
			_ownerName = ownerName;
			_id = id;
			_tax = tax;
			_nextSiege = nextSiege;
		}
	}
}