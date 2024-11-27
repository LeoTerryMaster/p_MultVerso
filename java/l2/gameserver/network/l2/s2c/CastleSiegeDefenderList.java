package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.entity.events.objects.SiegeClanObject;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.pledge.Alliance;
import l2.gameserver.model.pledge.Clan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CastleSiegeDefenderList extends L2GameServerPacket
{
	public static int OWNER = 1;
	public static int WAITING = 2;
	public static int ACCEPTED = 3;
	public static int REFUSE = 4;
	private final int _id;
	private final int _registrationValid;
	private List<DefenderClan> _defenderClans = Collections.emptyList();
	
	public CastleSiegeDefenderList(Castle castle)
	{
		_id = castle.getId();
		_registrationValid = !castle.getSiegeEvent().isRegistrationOver() && castle.getOwner() != null ? 1 : 0;
		List<SiegeClanObject> defenders = castle.getSiegeEvent().getObjects("defenders");
		List<SiegeClanObject> defendersWaiting = castle.getSiegeEvent().getObjects("defenders_waiting");
		List<SiegeClanObject> defendersRefused = castle.getSiegeEvent().getObjects("defenders_refused");
		_defenderClans = new ArrayList<>(defenders.size() + defendersWaiting.size() + defendersRefused.size());
		if(castle.getOwner() != null)
		{
			_defenderClans.add(new DefenderClan(castle.getOwner(), OWNER, 0));
		}
		for(SiegeClanObject siegeClan : defenders)
		{
			_defenderClans.add(new DefenderClan(siegeClan.getClan(), ACCEPTED, (int) (siegeClan.getDate() / 1000)));
		}
		for(SiegeClanObject siegeClan : defendersWaiting)
		{
			_defenderClans.add(new DefenderClan(siegeClan.getClan(), WAITING, (int) (siegeClan.getDate() / 1000)));
		}
		for(SiegeClanObject siegeClan : defendersRefused)
		{
			_defenderClans.add(new DefenderClan(siegeClan.getClan(), REFUSE, (int) (siegeClan.getDate() / 1000)));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(203);
		writeD(_id);
		writeD(0);
		writeD(_registrationValid);
		writeD(0);
		writeD(_defenderClans.size());
		writeD(_defenderClans.size());
		for(DefenderClan defenderClan : _defenderClans)
		{
			Clan clan = defenderClan._clan;
			writeD(clan.getClanId());
			writeS(clan.getName());
			writeS(clan.getLeaderName());
			writeD(clan.getCrestId());
			writeD(defenderClan._time);
			writeD(defenderClan._type);
			writeD(clan.getAllyId());
			Alliance alliance = clan.getAlliance();
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
	
	private static class DefenderClan
	{
		private final Clan _clan;
		private final int _type;
		private final int _time;
		
		public DefenderClan(Clan clan, int type, int time)
		{
			_clan = clan;
			_type = type;
			_time = time;
		}
	}
}