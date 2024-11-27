package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Skill;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.pledge.SubUnit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PledgeSkillList extends L2GameServerPacket
{
	private final List<UnitSkillInfo> _unitSkills = new ArrayList<>();
	private List<SkillInfo> _allSkills = Collections.emptyList();
	
	public PledgeSkillList(Clan clan)
	{
		Collection<Skill> skills = clan.getSkills();
		_allSkills = new ArrayList<>(skills.size());
		for(Skill sk : skills)
		{
			_allSkills.add(new SkillInfo(sk.getId(), sk.getLevel()));
		}
		for(SubUnit subUnit : clan.getAllSubUnits())
		{
			for(Skill sk : subUnit.getSkills())
			{
				_unitSkills.add(new UnitSkillInfo(subUnit.getType(), sk.getId(), sk.getLevel()));
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(57);
		writeD(_allSkills.size() + _unitSkills.size());
		for(SkillInfo info : _allSkills)
		{
			writeD(info._id);
			writeD(info._level);
		}
		for(UnitSkillInfo info : _unitSkills)
		{
			writeD(info._id);
			writeD(info._level);
		}
	}
	
	static class UnitSkillInfo extends SkillInfo
	{
		private final int _type;
		
		public UnitSkillInfo(int type, int id, int level)
		{
			super(id, level);
			_type = type;
		}
	}
	
	static class SkillInfo
	{
		public int _id;
		public int _level;
		
		public SkillInfo(int id, int level)
		{
			_id = id;
			_level = level;
		}
	}
}