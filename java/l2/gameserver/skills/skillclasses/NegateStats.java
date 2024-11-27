package l2.gameserver.skills.skillclasses;

import l2.commons.util.Rnd;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Formulas;
import l2.gameserver.stats.Stats;
import l2.gameserver.stats.funcs.FuncTemplate;
import l2.gameserver.templates.StatsSet;

import java.util.ArrayList;
import java.util.List;

public class NegateStats extends Skill
{
	private final List<Stats> _negateStats;
	private final boolean _negateOffensive;
	private final int _negateCount;
	
	public NegateStats(StatsSet set)
	{
		super(set);
		String[] negateStats = set.getString("negateStats", "").split(" ");
		_negateStats = new ArrayList<>(negateStats.length);
		for(String stat : negateStats)
		{
			if(stat.isEmpty())
				continue;
			_negateStats.add(Stats.valueOfXml(stat));
		}
		_negateOffensive = set.getBool("negateDebuffs", false);
		_negateCount = set.getInteger("negateCount", 0);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			if(target == null)
				continue;
			if(!_negateOffensive && !Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()))
			{
				activeChar.sendPacket(new SystemMessage(139).addString(target.getName()).addSkillName(getId(), getLevel()));
				continue;
			}
			int count = 0;
			List<Effect> effects = target.getEffectList().getAllEffects();
			block1:
			for(Stats stat : _negateStats)
			{
				for(Effect e : effects)
				{
					Skill skill = e.getSkill();
					if(!skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel()))
					{
						++count;
						continue;
					}
					if(skill.isOffensive() == _negateOffensive && containsStat(e, stat) && skill.isCancelable())
					{
						target.sendPacket(new SystemMessage(749).addSkillName(e.getSkill().getId(), e.getSkill().getDisplayLevel()));
						e.exit();
						++count;
					}
					if(_negateCount <= 0 || count < _negateCount)
						continue;
					continue block1;
				}
			}
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
	
	private boolean containsStat(Effect e, Stats stat)
	{
		for(FuncTemplate ft : e.getTemplate().getAttachedFuncs())
		{
			if(ft._stat != stat)
				continue;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isOffensive()
	{
		return !_negateOffensive;
	}
	
	public List<Stats> getNegateStats()
	{
		return _negateStats;
	}
}