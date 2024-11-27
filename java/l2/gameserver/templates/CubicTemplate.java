package l2.gameserver.templates;

import gnu.trove.TIntIntHashMap;
import l2.gameserver.model.Skill;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CubicTemplate
{
	private final int _id;
	private final int _level;
	private final int _delay;
	private final List<Map.Entry<Integer, List<SkillInfo>>> _skills = new ArrayList<>(3);
	
	public CubicTemplate(int id, int level, int delay)
	{
		_id = id;
		_level = level;
		_delay = delay;
	}
	
	public void putSkills(int chance, List<SkillInfo> skill)
	{
		_skills.add(new AbstractMap.SimpleImmutableEntry<>(chance, skill));
	}
	
	public Iterable<Map.Entry<Integer, List<SkillInfo>>> getSkills()
	{
		return _skills;
	}
	
	public int getDelay()
	{
		return _delay;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public enum ActionType
	{
		ATTACK,
		DEBUFF,
		CANCEL,
		HEAL;
	}
	
	public static class SkillInfo
	{
		private final Skill _skill;
		private final int _chance;
		private final ActionType _actionType;
		private final boolean _canAttackDoor;
		private final int _minHp;
		private final int _minHpPercent;
		private final TIntIntHashMap _chanceList;
		
		public SkillInfo(Skill skill, int chance, ActionType actionType, boolean canAttackDoor, int minHp, int minHpPercent, TIntIntHashMap set)
		{
			_skill = skill;
			_chance = chance;
			_actionType = actionType;
			_canAttackDoor = canAttackDoor;
			_minHp = minHp;
			_minHpPercent = minHpPercent;
			_chanceList = set;
		}
		
		public int getChance()
		{
			return _chance;
		}
		
		public ActionType getActionType()
		{
			return _actionType;
		}
		
		public Skill getSkill()
		{
			return _skill;
		}
		
		public boolean isCanAttackDoor()
		{
			return _canAttackDoor;
		}
		
		public int getMinHp()
		{
			return _minHp;
		}
		
		public int getMinHpPercent()
		{
			return _minHpPercent;
		}
		
		public int getChance(int a)
		{
			return _chanceList.get(a);
		}
	}
}