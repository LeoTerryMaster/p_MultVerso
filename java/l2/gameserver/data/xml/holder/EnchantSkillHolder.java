package l2.gameserver.data.xml.holder;

import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.templates.SkillEnchant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class EnchantSkillHolder extends AbstractHolder
{
	private static final Logger LOG = LoggerFactory.getLogger(EnchantSkillHolder.class);
	private static final EnchantSkillHolder INSTANCE = new EnchantSkillHolder();
	private final Map<Integer, Map<Integer, SkillEnchant>> _skillsEnchantLevels = new TreeMap<>();
	private final Map<Integer, Map<Integer, Map<Integer, SkillEnchant>>> _skillsEnchantRoutes = new TreeMap<>();
	
	private EnchantSkillHolder()
	{
	}
	
	public static EnchantSkillHolder getInstance()
	{
		return INSTANCE;
	}
	
	public void addEnchantSkill(SkillEnchant skillEnchant)
	{
		int skillId = skillEnchant.getSkillId();
		Map<Integer, SkillEnchant> skillEnchantLevels = _skillsEnchantLevels.get(skillId);
		if(skillEnchantLevels == null)
		{
			skillEnchantLevels = new TreeMap<>();
			_skillsEnchantLevels.put(skillId, skillEnchantLevels);
		}
		skillEnchantLevels.put(skillEnchant.getSkillLevel(), skillEnchant);
		Map<Integer, Map<Integer, SkillEnchant>> skillEnchantRoutes = _skillsEnchantRoutes.get(skillId);
		if(skillEnchantRoutes == null)
		{
			skillEnchantRoutes = new TreeMap<>();
			_skillsEnchantRoutes.put(skillId, skillEnchantRoutes);
		}
		Map<Integer, SkillEnchant> skillRouteLevels;
		int skillRouteId;
		if((skillRouteLevels = skillEnchantRoutes.get(skillRouteId = skillEnchant.getRouteId())) == null)
		{
			skillRouteLevels = new TreeMap<>();
			skillEnchantRoutes.put(skillRouteId, skillRouteLevels);
		}
		skillRouteLevels.put(skillEnchant.getSkillLevel(), skillEnchant);
	}
	
	public SkillEnchant getSkillEnchant(int skillId, int skillLvl)
	{
		Map<Integer, SkillEnchant> skillEnchantLevels = _skillsEnchantLevels.get(skillId);
		if(skillEnchantLevels == null)
		{
			return null;
		}
		return skillEnchantLevels.get(skillLvl);
	}
	
	public SkillEnchant getSkillEnchant(int skillId, int routeId, int enchantLevel)
	{
		Map<Integer, SkillEnchant> skillEnchantLevels = _skillsEnchantLevels.get(skillId);
		if(skillEnchantLevels == null)
		{
			return null;
		}
		for(SkillEnchant skillEnchant : skillEnchantLevels.values())
		{
			if(skillEnchant.getRouteId() != routeId || skillEnchant.getEnchantLevel() != enchantLevel)
				continue;
			return skillEnchant;
		}
		return null;
	}
	
	public Map<Integer, Map<Integer, SkillEnchant>> getRoutesOf(int skillId)
	{
		Map<Integer, Map<Integer, SkillEnchant>> skillEnchantRoutes = _skillsEnchantRoutes.get(skillId);
		if(skillEnchantRoutes == null)
		{
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(skillEnchantRoutes);
	}
	
	public int getFirstSkillLevelOf(int skillId, int routeId)
	{
		Map<Integer, SkillEnchant> skillEnchantLevels = _skillsEnchantLevels.get(skillId);
		if(skillEnchantLevels == null)
		{
			return 0;
		}
		for(SkillEnchant se : skillEnchantLevels.values())
		{
			if(se.getRouteId() != routeId || se.getEnchantLevel() != 1)
				continue;
			return se.getSkillLevel();
		}
		return 0;
	}
	
	public int getMaxEnchantLevelOf(int skillId)
	{
		Map<Integer, SkillEnchant> skillEnchantLevels = _skillsEnchantLevels.get(skillId);
		if(skillEnchantLevels == null)
		{
			return 0;
		}
		int maxEnchLevel = 0;
		for(SkillEnchant se : skillEnchantLevels.values())
		{
			if(se.getEnchantLevel() <= maxEnchLevel)
				continue;
			maxEnchLevel = se.getEnchantLevel();
		}
		return maxEnchLevel;
	}
	
	public Map<Integer, SkillEnchant> getLevelsOf(int skillId)
	{
		Map<Integer, SkillEnchant> skillEnchantLevels = _skillsEnchantLevels.get(skillId);
		if(skillEnchantLevels == null)
		{
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(skillEnchantLevels);
	}
	
	public void addEnchantSkill(int skillId, int skillLevel, int enchantLevel, int routeId, long exp, int sp, int[] chances, int itemId, long itemCount)
	{
		addEnchantSkill(new SkillEnchant(skillId, skillLevel, enchantLevel, routeId, exp, sp, chances, itemId, itemCount));
	}
	
	@Override
	public int size()
	{
		return _skillsEnchantLevels.size();
	}
	
	@Override
	public void clear()
	{
		_skillsEnchantLevels.clear();
	}
}