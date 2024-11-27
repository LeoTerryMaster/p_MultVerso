package l2.gameserver.tables;

import l2.gameserver.data.xml.holder.SkillAcquireHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.SkillLearn;
import l2.gameserver.model.base.AcquireType;
import l2.gameserver.model.base.EnchantSkillLearn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SkillTreeTable
{
	public static final int NORMAL_ENCHANT_COST_MULTIPLIER = 1;
	public static final int NORMAL_ENCHANT_BOOK = 6622;
	private static final Logger _log = LoggerFactory.getLogger(SkillTreeTable.class);
	public static Map<Integer, List<EnchantSkillLearn>> _enchant;
	private static SkillTreeTable _instance;
	
	static
	{
		_enchant = new ConcurrentHashMap<>();
	}
	
	private SkillTreeTable()
	{
		_log.info("SkillTreeTable: Loaded " + _enchant.size() + " enchanted skills.");
	}
	
	public static SkillTreeTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new SkillTreeTable();
		}
		return _instance;
	}
	
	public static void checkSkill(Player player, Skill skill)
	{
		SkillLearn learnBase = SkillAcquireHolder.getInstance().getSkillLearn(player, player.getClassId(), skill.getId(), 1, AcquireType.NORMAL);
		if(learnBase == null)
		{
			return;
		}
		if(learnBase.getMinLevel() >= player.getLevel() + 10)
		{
			player.removeSkill(skill, true);
		}
	}
	
	private static int levelWithoutEnchant(Skill skill)
	{
		return skill.getDisplayLevel() > 100 ? skill.getBaseLevel() : skill.getLevel();
	}
	
	public static int isEnchantable(Skill skill)
	{
		List<EnchantSkillLearn> enchants = _enchant.get(skill.getId());
		if(enchants == null)
		{
			return 0;
		}
		for(EnchantSkillLearn e : enchants)
		{
			if(e.getBaseLevel() > skill.getLevel())
				continue;
			return 1;
		}
		return 0;
	}
	
	public static void unload()
	{
		if(_instance != null)
		{
			_instance = null;
		}
		_enchant.clear();
	}
}