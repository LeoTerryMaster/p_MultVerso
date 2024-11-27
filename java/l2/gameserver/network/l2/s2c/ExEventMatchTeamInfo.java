package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.Summon;

import java.util.ArrayList;
import java.util.List;

public class ExEventMatchTeamInfo extends L2GameServerPacket
{
	private final int leader_id;
	private final int loot;
	private final List<EventMatchTeamInfo> members = new ArrayList<>();
	
	public ExEventMatchTeamInfo(List<Player> party, Player exclude)
	{
		leader_id = party.get(0).getObjectId();
		loot = party.get(0).getParty().getLootDistribution();
		for(Player member : party)
		{
			if(member.equals(exclude))
				continue;
			members.add(new EventMatchTeamInfo(member));
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(28);
	}
	
	public static class EventMatchTeamInfo
	{
		public String _name;
		public String pet_Name;
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
		public int pet_id;
		public int pet_NpcId;
		public int pet_curHp;
		public int pet_maxHp;
		public int pet_curMp;
		public int pet_maxMp;
		public int pet_level;
		
		public EventMatchTeamInfo(Player member)
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
			Summon pet = member.getPet();
			if(pet != null)
			{
				pet_id = pet.getObjectId();
				pet_NpcId = pet.getNpcId() + 1000000;
				pet_Name = pet.getName();
				pet_curHp = (int) pet.getCurrentHp();
				pet_maxHp = pet.getMaxHp();
				pet_curMp = (int) pet.getCurrentMp();
				pet_maxMp = pet.getMaxMp();
				pet_level = pet.getLevel();
			}
			else
			{
				pet_id = 0;
			}
		}
	}
}