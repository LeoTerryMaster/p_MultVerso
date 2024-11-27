package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.pledge.Clan;

import java.util.ArrayList;
import java.util.List;

public class PledgeReceiveWarList extends L2GameServerPacket
{
	private final List<WarInfo> infos = new ArrayList<>();
	private final int _updateType;
	private final int _page;
	
	public PledgeReceiveWarList(Clan clan, int type, int page)
	{
		_updateType = type;
		_page = page;
		List<Clan> clans = _updateType == 1 ? clan.getAttackerClans() : clan.getEnemyClans();
		for(Clan _clan : clans)
		{
			if(_clan == null)
				continue;
			infos.add(new WarInfo(_clan.getName(), _updateType, 0));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(62);
		writeD(_updateType);
		writeD(0);
		writeD(infos.size());
		for(WarInfo _info : infos)
		{
			writeS(_info.clan_name);
			writeD(_info.unk1);
			writeD(_info.unk2);
		}
	}
	
	static class WarInfo
	{
		public String clan_name;
		public int unk1;
		public int unk2;
		
		public WarInfo(String _clan_name, int _unk1, int _unk2)
		{
			clan_name = _clan_name;
			unk1 = _unk1;
			unk2 = _unk2;
		}
	}
}