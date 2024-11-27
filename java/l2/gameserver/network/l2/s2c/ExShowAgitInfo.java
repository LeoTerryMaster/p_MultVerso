package l2.gameserver.network.l2.s2c;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import l2.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.tables.ClanTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExShowAgitInfo extends L2GameServerPacket
{
	private List<AgitInfo> _clanHalls = Collections.emptyList();
	
	public ExShowAgitInfo()
	{
		List<ClanHall> chs = ResidenceHolder.getInstance().getResidenceList(ClanHall.class);
		_clanHalls = new ArrayList<>(chs.size());
		for(ClanHall clanHall : chs)
		{
			int ch_id = clanHall.getId();
			int getType = clanHall.getSiegeEvent().getClass() == ClanHallAuctionEvent.class ? 0 : clanHall.getSiegeEvent().getClass() == ClanHallMiniGameEvent.class ? 2 : 1;
			Clan clan = ClanTable.getInstance().getClan(clanHall.getOwnerId());
			String clan_name = clanHall.getOwnerId() == 0 || clan == null ? "" : clan.getName();
			String leader_name = clanHall.getOwnerId() == 0 || clan == null ? "" : clan.getLeaderName();
			_clanHalls.add(new AgitInfo(clan_name, leader_name, ch_id, getType));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(22);
		writeD(_clanHalls.size());
		for(AgitInfo info : _clanHalls)
		{
			writeD(info.ch_id);
			writeS(info.clan_name);
			writeS(info.leader_name);
			writeD(info.getType);
		}
	}
	
	static class AgitInfo
	{
		public String clan_name;
		public String leader_name;
		public int ch_id;
		public int getType;
		
		public AgitInfo(String clan_name, String leader_name, int ch_id, int lease)
		{
			this.clan_name = clan_name;
			this.leader_name = leader_name;
			this.ch_id = ch_id;
			getType = lease;
		}
	}
}