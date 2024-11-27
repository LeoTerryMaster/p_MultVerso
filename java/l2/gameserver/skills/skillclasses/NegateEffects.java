package l2.gameserver.skills.skillclasses;

import l2.commons.util.Rnd;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.skills.EffectType;
import l2.gameserver.stats.Formulas;
import l2.gameserver.templates.StatsSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NegateEffects extends Skill
{
	private final boolean _onlyPhysical;
	private final boolean _negateDebuffs;
	private final Map<EffectType, Integer> _negateEffects = new HashMap<>();
	private final Map<String, Integer> _negateStackType = new HashMap<>();
	
	public NegateEffects(StatsSet set)
	{
		super(set);
		String[] negateEffectsString = set.getString("negateEffects", "").split(";");
		
		for(int i = 0;i < negateEffectsString.length;++i)
		{
			if(!negateEffectsString[i].isEmpty())
			{
				String[] entry = negateEffectsString[i].split(":");
				_negateEffects.put(Enum.valueOf(EffectType.class, entry[0]), entry.length > 1 ? Integer.decode(entry[1]) : Integer.MAX_VALUE);
			}
		}
		
		String[] negateStackTypeString = set.getString("negateStackType", "").split(";");
		
		for(int i = 0;i < negateStackTypeString.length;++i)
		{
			if(!negateStackTypeString[i].isEmpty())
			{
				String[] entry = negateStackTypeString[i].split(":");
				_negateStackType.put(entry[0], entry.length > 1 ? Integer.decode(entry[1]) : Integer.MAX_VALUE);
			}
		}
		
		_onlyPhysical = set.getBool("onlyPhysical", false);
		_negateDebuffs = set.getBool("negateDebuffs", true);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			if(target == null)
				continue;
			if(!_negateDebuffs && !Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()))
			{
				activeChar.sendPacket(new SystemMessage(139).addString(target.getName()).addSkillName(getDisplayId(), getDisplayLevel()));
				continue;
			}
			if(!_negateEffects.isEmpty())
			{
				for(Map.Entry<EffectType, Integer> e : _negateEffects.entrySet())
				{
					negateEffectAtPower(target, e.getKey(), e.getValue());
				}
			}
			if(!_negateStackType.isEmpty())
			{
				for(Map.Entry e : _negateStackType.entrySet())
				{
					negateEffectAtPower(target, (String) e.getKey(), (Integer) e.getValue());
				}
			}
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
	
	private void negateEffectAtPower(Creature target, EffectType type, int power)
	{
		for(Effect e : target.getEffectList().getAllEffects())
		{
			Skill skill = e.getSkill();
			if(_onlyPhysical && skill.isMagic() || !skill.isCancelable() || skill.isOffensive() && !_negateDebuffs || !skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel()) || e.getEffectType() != type || e.getStackOrder() > power)
				continue;
			e.exit();
		}
	}
	
	private void negateEffectAtPower(Creature target, String stackType, int power)
	{
		for(Effect e : target.getEffectList().getAllEffects())
		{
			Skill skill = e.getSkill();
			if(_onlyPhysical && skill.isMagic() || !skill.isCancelable() || skill.isOffensive() && !_negateDebuffs || !skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel()) || !e.isStackTypeMatch(stackType) || e.getStackOrder() > power)
				continue;
			e.exit();
		}
	}
}