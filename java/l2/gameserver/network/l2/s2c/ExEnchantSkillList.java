package l2.gameserver.network.l2.s2c;

import l2.gameserver.data.xml.holder.EnchantSkillHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.templates.SkillEnchant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ExEnchantSkillList extends L2GameServerPacket
{
	private final List<SkillEnchantEntry> _skills = new ArrayList<>();
	
	public static ExEnchantSkillList packetFor(Player player)
	{
		Collection<Skill> playerSkills = player.getAllSkills();
		ExEnchantSkillList esl = new ExEnchantSkillList();
		for(Skill currSkill : playerSkills)
		{
			Map<Integer, Map<Integer, SkillEnchant>> skillEnchRoutes;
			int baseSkillLevel;
			int skillId = currSkill.getId();
			int currSkillLevel = currSkill.getLevel();
			if(currSkillLevel < (baseSkillLevel = currSkill.getBaseLevel()) || (skillEnchRoutes = EnchantSkillHolder.getInstance().getRoutesOf(skillId)) == null || skillEnchRoutes.isEmpty())
				continue;
			SkillEnchant currSkillEnch = EnchantSkillHolder.getInstance().getSkillEnchant(skillId, currSkillLevel);
			if(currSkillLevel == baseSkillLevel)
			{
				for(Map<Integer, SkillEnchant> skillEnchLevels2 : skillEnchRoutes.values())
				{
					for(SkillEnchant newSkillEnch2 : skillEnchLevels2.values())
					{
						if(newSkillEnch2.getEnchantLevel() != 1)
							continue;
						esl.addSkill(newSkillEnch2.getSkillId(), newSkillEnch2.getSkillLevel(), newSkillEnch2.getSp(), newSkillEnch2.getExp());
					}
				}
				continue;
			}
			if(currSkillEnch == null)
				continue;
			Map<Integer, SkillEnchant> skillEnchLevels = skillEnchRoutes.get(currSkillEnch.getRouteId());
			int newSkillLevel = currSkillLevel + 1;
			SkillEnchant newSkillEnch = skillEnchLevels.get(newSkillLevel);
			if(newSkillEnch == null)
				continue;
			
			esl.addSkill(newSkillEnch.getSkillId(), newSkillEnch.getSkillLevel(), newSkillEnch.getSp(), newSkillEnch.getExp());
		}
		return esl;
	}
	
	public void addSkill(int id, int level, int sp, long exp)
	{
		_skills.add(new SkillEnchantEntry(id, level, sp, exp));
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(23);
		writeD(_skills.size());
		for(SkillEnchantEntry see : _skills)
		{
			see.write();
		}
	}
	
	class SkillEnchantEntry
	{
		private final int _skillId;
		private final int _skillLevel;
		private final int _neededSp;
		private final long _neededExp;
		
		public SkillEnchantEntry(int skillId, int skillLevel, int neededSp, long neededExp)
		{
			_skillId = skillId;
			_skillLevel = skillLevel;
			_neededSp = neededSp;
			_neededExp = neededExp;
		}
		
		private void write()
		{
			writeD(_skillId);
			writeD(_skillLevel);
			writeD(_neededSp);
			writeQ(_neededExp);
		}
	}
}