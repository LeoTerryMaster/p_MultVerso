package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SkillList extends L2GameServerPacket
{
	private final List<SkillListRecord> _skillRecords;
	
	public SkillList(Player player)
	{
		Collection<Skill> playerSkills = player.getAllSkills();
		_skillRecords = new ArrayList<>(playerSkills.size());
		for(Skill skill : playerSkills)
		{
			_skillRecords.add(new SkillListRecord(player, skill));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(88);
		writeD(_skillRecords.size());
		for(SkillListRecord skr : _skillRecords)
		{
			skr.writeRecord();
		}
	}
	
	private class SkillListRecord implements Comparable<SkillListRecord>
	{
		private final int _id;
		private final int _lvl;
		private final boolean _disabled;
		private final int _order;
		
		public SkillListRecord(Player player, Skill skill)
		{
			_id = skill.getDisplayId();
			_lvl = skill.getDisplayLevel();
			_disabled = player.isUnActiveSkill(skill.getId());
			_order = skill.isActive() || skill.isToggle() ? 0 : 1;
		}
		
		public void writeRecord()
		{
			writeD(_order);
			writeD(_lvl);
			writeD(_id);
			writeC(_disabled ? 1 : 0);
		}
		
		@Override
		public int compareTo(SkillListRecord o)
		{
			return _id - o._id;
		}
	}
}