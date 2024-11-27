package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.pledge.Clan;

public class PledgeStatusChanged extends L2GameServerPacket
{
	private final int leader_id;
	private final int clan_id;
	private final int level;
	
	public PledgeStatusChanged(Clan clan)
	{
		leader_id = clan.getLeaderId();
		clan_id = clan.getClanId();
		level = clan.getLevel();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(205);
		writeD(leader_id);
		writeD(clan_id);
		writeD(0);
		writeD(level);
		writeD(0);
		writeD(0);
		writeD(0);
	}
}