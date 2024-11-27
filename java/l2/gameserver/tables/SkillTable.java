package l2.gameserver.tables;

import gnu.trove.TIntIntHashMap;
import l2.gameserver.model.Skill;
import l2.gameserver.skills.SkillsEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SkillTable
{
	private static final Logger _log = LoggerFactory.getLogger(SkillTable.class);
	private static final SkillTable _instance = new SkillTable();
	private Map<Integer, Map<Integer, Skill>> _skills;
	private TIntIntHashMap _maxLevelsTable;
	private TIntIntHashMap _baseLevelsTable;
	
	public static final SkillTable getInstance()
	{
		return _instance;
	}
	
	public void load()
	{
		_skills = SkillsEngine.getInstance().loadAllSkills();
		makeLevelsTable();
	}
	
	public void reload()
	{
		load();
	}
	
	public Skill getInfo(int skillId, int skillLevel)
	{
		Map<Integer, Skill> skillLevels = _skills.get(skillId);
		if(skillLevels == null)
		{
			return null;
		}
		return skillLevels.get(skillLevel);
	}
	
	public int getMaxLevel(int skillId)
	{
		return _maxLevelsTable.get(skillId);
	}
	
	public int getBaseLevel(int skillId)
	{
		return _baseLevelsTable.get(skillId);
	}
	
	private void makeLevelsTable()
	{
		_maxLevelsTable = new TIntIntHashMap();
		_baseLevelsTable = new TIntIntHashMap();
		for(Map<Integer, Skill> ss : _skills.values())
		{
			for(Skill s : ss.values())
			{
				int skillId = s.getId();
				int level = s.getLevel();
				int maxLevel = _maxLevelsTable.get(skillId);
				if(level > maxLevel)
				{
					_maxLevelsTable.put(skillId, level);
				}
				if(_baseLevelsTable.get(skillId) != 0)
					continue;
				_baseLevelsTable.put(skillId, s.getBaseLevel());
			}
		}
	}
}