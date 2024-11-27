package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Party;
import l2.gameserver.model.Player;

import java.util.ArrayList;
import java.util.List;

public class PartySmallWindowAll extends L2GameServerPacket
{
	private final int leaderId;
	private final int loot;
	private final List<PartySmallWindowMemberInfo> members = new ArrayList<>();
	
	public PartySmallWindowAll(Party party, Player exclude)
	{
		leaderId = party.getPartyLeader().getObjectId();
		loot = party.getLootDistribution();
		for(Player member : party.getPartyMembers())
		{
			if(member == exclude)
				continue;
			members.add(new PartySmallWindowMemberInfo(member));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(78);
		writeD(leaderId);
		writeD(loot);
		writeD(members.size());
		for(PartySmallWindowMemberInfo member : members)
		{
			writeD(member._id);
			writeS(member._name);
			writeD(member.curCp);
			writeD(member.maxCp);
			writeD(member.curHp);
			writeD(member.maxHp);
			writeD(member.curMp);
			writeD(member.maxMp);
			writeD(member.level);
			writeD(member.class_id);
			writeD(0);
			writeD(member.race_id);
		}
	}
	
	public static class PartySmallWindowMemberInfo
	{
		public String _name;
		public int _id;
		public int curCp;
		public int maxCp;
		public int curHp;
		public int maxHp;
		public int curMp;
		public int maxMp;
		public int level;
		public int class_id;
		public int race_id;
		
		public PartySmallWindowMemberInfo(Player member)
		{
			_name = member.getName();
			_id = member.getObjectId();
			curCp = (int) member.getCurrentCp();
			maxCp = member.getMaxCp();
			curHp = (int) member.getCurrentHp();
			maxHp = member.getMaxHp();
			curMp = (int) member.getCurrentMp();
			maxMp = member.getMaxMp();
			level = member.getLevel();
			class_id = member.getClassId().getId();
			race_id = member.getRace().ordinal();
		}
	}
}