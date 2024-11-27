package l2.gameserver.skills.skillclasses;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.Stats;
import l2.gameserver.templates.StatsSet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class StealBuff extends Skill
{
	private final int _stealCount;
	private final int _chanceMod;
	
	public StealBuff(StatsSet set)
	{
		super(set);
		_stealCount = set.getInteger("StealCount", 1);
		_chanceMod = set.getInteger("ChanceMod", 0);
	}
	
	public static boolean calcSkillCancel(Skill cancel, Effect effect, int chance_mod, double res_mul, boolean chance_restrict)
	{
		int dml = Math.max(0, cancel.getMagicLevel() - effect.getSkill().getMagicLevel());
		int chance = (int) ((double) ((long) (2 * dml + chance_mod) + effect.getPeriod() * (long) effect.getCount() / 120000) * res_mul);
		return Rnd.chance(Math.max(Config.SKILLS_DISPEL_MOD_MIN, Math.min(Config.SKILLS_DISPEL_MOD_MAX, chance)));
	}
	
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			if(target == null)
				continue;
			double res_mul = 1.0 - target.calcStat(Stats.CANCEL_RESIST, 0.0, null, null) * 0.01;
			Effect[] effects = target.getEffectList().getAllFirstEffects();
			LinkedList<Effect> eset = new LinkedList<>();
			for(Effect.EEffectSlot ees : Effect.EEffectSlot.VALUES)
			{
				for(Effect eff : effects)
				{
					Skill skill;
					if(eff == null || eff.getTemplate()._applyOnCaster || eff.getEffectSlot() != ees || !(skill = eff.getSkill()).isCancelable() || !skill.isActive() || skill.isOffensive() || skill.isToggle() || skill.isTrigger())
						continue;
					eset.add(eff);
				}
			}
			boolean update = false;
			Iterator it = eset.descendingIterator();
			int cnt = 0;
			while(it.hasNext() && cnt++ < _stealCount)
			{
				Effect effect = (Effect) it.next();
				if(!calcSkillCancel(this, effect, _chanceMod, res_mul, true))
					continue;
				Skill skill = effect.getSkill();
				for(Effect ceff : target.getEffectList().getEffectsBySkill(skill))
				{
					if(ceff == null)
						continue;
					Effect leff = ceff.getTemplate().getEffect(new Env(activeChar, activeChar, skill));
					leff.setCount(ceff.getCount());
					if(ceff.getCount() == 1)
					{
						leff.setPeriod(ceff.getPeriod() - ceff.getTime());
					}
					else
					{
						leff.setPeriod(ceff.getPeriod());
					}
					update = true;
					ceff.exit();
					activeChar.getEffectList().addEffect(leff);
				}
				target.sendPacket(new SystemMessage(92).addSkillName(skill.getId(), skill.getLevel()));
			}
			if(update)
			{
				target.sendChanges();
				target.updateEffectIcons();
				activeChar.sendChanges();
				activeChar.updateEffectIcons();
			}
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}