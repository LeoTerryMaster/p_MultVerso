package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.pledge.Alliance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.pledge.SubUnit;
import l2.gameserver.model.pledge.UnitMember;

import java.util.ArrayList;
import java.util.List;

public class PledgeShowMemberListAll extends L2GameServerPacket
{
	private final int _clanObjectId;
	private final int _clanCrestId;
	private final int _level;
	private final int _rank;
	private final int _reputation;
	private final int _hasCastle;
	private final int _hasClanHall;
	private final int _atClanWar;
	private final String _unitName;
	private final String _leaderName;
	private final int _pledgeType;
	private final List<PledgePacketMember> _members;
	private int _allianceObjectId;
	private int _allianceCrestId;
	private String _allianceName;
	
	public PledgeShowMemberListAll(Clan clan, SubUnit sub)
	{
		_pledgeType = sub.getType();
		_clanObjectId = clan.getClanId();
		_unitName = sub.getName();
		_leaderName = sub.getLeaderName();
		_clanCrestId = clan.getCrestId();
		_level = clan.getLevel();
		_hasCastle = clan.getCastle();
		_hasClanHall = clan.getHasHideout();
		_rank = clan.getRank();
		_reputation = clan.getReputationScore();
		_atClanWar = clan.isAtWarOrUnderAttack();
		Alliance ally = clan.getAlliance();
		if(ally != null)
		{
			_allianceObjectId = ally.getAllyId();
			_allianceName = ally.getAllyName();
			_allianceCrestId = ally.getAllyCrestId();
		}
		_members = new ArrayList<>(sub.size());
		for(UnitMember m : sub.getUnitMembers())
		{
			_members.add(new PledgePacketMember(m));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(83);
		writeD(_pledgeType == 0 ? 0 : 1);
		writeD(_clanObjectId);
		writeD(_pledgeType);
		writeS(_unitName);
		writeS(_leaderName);
		writeD(_clanCrestId);
		writeD(_level);
		writeD(_hasCastle);
		writeD(_hasClanHall);
		writeD(_rank);
		writeD(_reputation);
		writeD(0);
		writeD(0);
		writeD(_allianceObjectId);
		writeS(_allianceName);
		writeD(_allianceCrestId);
		writeD(_atClanWar);
		writeD(_members.size());
		for(PledgePacketMember m : _members)
		{
			writeS(m._name);
			writeD(m._level);
			writeD(m._classId);
			writeD(m._sex);
			writeD(m._race);
			writeD(m._online);
			writeD(m._hasSponsor ? 1 : 0);
		}
	}
	
	private class PledgePacketMember
	{
		private final String _name;
		private final int _level;
		private final int _classId;
		private final int _sex;
		private final int _race;
		private final int _online;
		private final boolean _hasSponsor;
		
		public PledgePacketMember(UnitMember m)
		{
			_name = m.getName();
			_level = m.getLevel();
			_classId = m.getClassId();
			_sex = m.getSex();
			_race = 0;
			_online = m.isOnline() ? m.getObjectId() : 0;
			_hasSponsor = m.getSponsor() != 0;
		}
	}
}