package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.pledge.Alliance;
import l2.gameserver.model.pledge.Clan;

public class PledgeShowInfoUpdate extends L2GameServerPacket
{
	private final int clan_id;
	private final int clan_level;
	private final int clan_rank;
	private final int clan_rep;
	private final int crest_id;
	private final int ally_id;
	private final int atwar;
	private final int HasCastle;
	private final int HasHideout;
	private int ally_crest;
	private String ally_name = "";
	
	public PledgeShowInfoUpdate(Clan clan)
	{
		clan_id = clan.getClanId();
		clan_level = clan.getLevel();
		HasCastle = clan.getCastle();
		HasHideout = clan.getHasHideout();
		clan_rank = clan.getRank();
		clan_rep = clan.getReputationScore();
		crest_id = clan.getCrestId();
		ally_id = clan.getAllyId();
		atwar = clan.isAtWar();
		Alliance ally = clan.getAlliance();
		if(ally != null)
		{
			ally_name = ally.getAllyName();
			ally_crest = ally.getAllyCrestId();
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(136);
		writeD(clan_id);
		writeD(crest_id);
		writeD(clan_level);
		writeD(HasCastle);
		writeD(HasHideout);
		writeD(clan_rank);
		writeD(clan_rep);
		writeD(0);
		writeD(0);
		writeD(ally_id);
		writeS(ally_name);
		writeD(ally_crest);
		writeD(atwar);
	}
}