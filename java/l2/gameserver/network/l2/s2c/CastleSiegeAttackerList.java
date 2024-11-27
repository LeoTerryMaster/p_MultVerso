package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.entity.events.objects.SiegeClanObject;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.model.pledge.Alliance;
import l2.gameserver.model.pledge.Clan;

import java.util.Collections;
import java.util.List;

public class CastleSiegeAttackerList extends L2GameServerPacket
{
	private final int _id;
	private final int _registrationValid;
	private List<SiegeClanObject> _clans = Collections.emptyList();
	
	public CastleSiegeAttackerList(Residence residence)
	{
		_id = residence.getId();
		_registrationValid = !residence.getSiegeEvent().isRegistrationOver() ? 1 : 0;
		_clans = residence.getSiegeEvent().getObjects("attackers");
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(202);
		writeD(_id);
		writeD(0);
		writeD(_registrationValid);
		writeD(0);
		writeD(_clans.size());
		writeD(_clans.size());
		for(SiegeClanObject siegeClan : _clans)
		{
			Clan clan = siegeClan.getClan();
			writeD(clan.getClanId());
			writeS(clan.getName());
			writeS(clan.getLeaderName());
			writeD(clan.getCrestId());
			writeD((int) (siegeClan.getDate() / 1000));
			Alliance alliance = clan.getAlliance();
			writeD(clan.getAllyId());
			if(alliance != null)
			{
				writeS(alliance.getAllyName());
				writeS(alliance.getAllyLeaderName());
				writeD(alliance.getAllyCrestId());
				continue;
			}
			writeS("");
			writeS("");
			writeD(0);
		}
	}
}