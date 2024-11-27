package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.pledge.Clan;

public class PledgeInfo extends L2GameServerPacket
{
	private final int clan_id;
	private final String clan_name;
	private final String ally_name;
	
	public PledgeInfo(Clan clan)
	{
		clan_id = clan.getClanId();
		clan_name = clan.getName();
		ally_name = clan.getAlliance() == null ? "" : clan.getAlliance().getAllyName();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(131);
		writeD(clan_id);
		writeS(clan_name);
		writeS(ally_name);
	}
}