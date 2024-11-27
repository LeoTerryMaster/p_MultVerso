package l2.gameserver.stats;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.base.BaseStats;
import l2.gameserver.model.base.Element;
import l2.gameserver.model.base.SkillTrait;
import l2.gameserver.model.instances.ReflectionBossInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.skills.EffectType;
import l2.gameserver.skills.effects.EffectTemplate;
import l2.gameserver.templates.item.WeaponTemplate;
import l2.gameserver.utils.PositionUtils;

public class Formulas
{
	public static double calcHpRegen(Creature cha)
	{
		double init = cha.isPlayer() ? (cha.getLevel() <= 10 ? 1.5 + (double) cha.getLevel() / 20.0 : 1.4 + (double) cha.getLevel() / 10.0) * cha.getLevelMod() : cha.getTemplate().baseHpReg;
		if(cha.isPlayable())
		{
			init *= BaseStats.CON.calcBonus(cha);
			if(cha.isSummon())
			{
				init *= 2.0;
			}
		}
		return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null);
	}
	
	public static double calcMpRegen(Creature cha)
	{
		double init = cha.isPlayer() ? (0.87 + (double) cha.getLevel() * 0.03) * cha.getLevelMod() : cha.getTemplate().baseMpReg;
		if(cha.isPlayable())
		{
			init *= BaseStats.MEN.calcBonus(cha);
			if(cha.isSummon())
			{
				init *= 2.0;
			}
		}
		return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null);
	}
	
	public static double calcCpRegen(Creature cha)
	{
		double init = (1.5 + (double) (cha.getLevel() / 10)) * cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
		return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null);
	}
	
	public static AttackInfo calcPhysDam(Creature attacker, Creature target, Skill skill, boolean dual, boolean blow, boolean ss, boolean onCrit)
	{
		AttackInfo info = new AttackInfo();
		info.damage = attacker.getPAtk(target);
		info.defence = target.getPDef(attacker);
		info.crit_static = attacker.calcStat(Stats.CRITICAL_DAMAGE_STATIC, target, skill);
		info.death_rcpt = 0.01 * target.calcStat(Stats.DEATH_VULNERABILITY, attacker, skill);
		info.lethal1 = skill == null ? 0.0 : skill.getLethal1() * info.death_rcpt;
		info.lethal2 = skill == null ? 0.0 : skill.getLethal2() * info.death_rcpt;
		info.crit = Rnd.chance(calcCrit(attacker, target, skill, blow));
		info.shld = (skill == null || !skill.getShieldIgnore()) && calcShldUse(attacker, target);
		info.lethal = false;
		info.miss = false;
		boolean isPvP;
		boolean bl = isPvP = attacker.isPlayable() && target.isPlayable();
		if(info.shld)
		{
			info.defence += (double) target.getShldDef();
		}
		info.defence = Math.max(info.defence, 1.0);
		if(skill != null)
		{
			if(!blow && !target.isLethalImmune())
			{
				if(Rnd.chance(info.lethal1))
				{
					if(target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = target.getCurrentCp();
						target.sendPacket(Msg.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
					}
					else
					{
						info.lethal_dmg = target.getCurrentHp() / 2.0;
					}
					attacker.sendPacket(Msg.HALF_KILL);
				}
				else if(Rnd.chance(info.lethal2))
				{
					if(target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = target.getCurrentHp() + target.getCurrentCp() - 1.1;
						target.sendPacket(SystemMsg.LETHAL_STRIKE);
					}
					else
					{
						info.lethal_dmg = target.getCurrentHp() - 1.0;
					}
					attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
				}
			}
			if(skill.getPower(target) == 0.0)
			{
				info.damage = 0.0;
				return info;
			}
			if(blow && !skill.isBehind() && ss)
			{
				info.damage *= 2.04;
			}
			info.damage = skill.isChargeBoost() ? attacker.calcStat(Stats.SKILL_POWER, info.damage + skill.getPower(target), null, null) : (info.damage += attacker.calcStat(Stats.SKILL_POWER, skill.getPower(target), null, null));
			if(blow && skill.isBehind() && ss)
			{
				info.damage *= 1.5;
			}
			if(!skill.isChargeBoost())
			{
				info.damage *= 1.0 + (Rnd.get() * (double) attacker.getRandomDamage() * 2.0 - (double) attacker.getRandomDamage()) / 100.0;
			}
			if(blow)
			{
				info.damage *= 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
				info.damage = target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill);
				info.damage += 6.1 * info.crit_static;
			}
			if(skill.isChargeBoost())
			{
				info.damage *= 0.8 + 0.2 * (double) (attacker.getIncreasedForce() + Math.max(skill.getNumCharges(), 0));
			}
			else if(skill.isSoulBoost())
			{
				info.damage *= 1.0 + 0.06 * (double) Math.min(attacker.getConsumedSouls(), 5);
			}
			if(info.crit)
			{
				info.damage *= 2.0;
			}
		}
		else
		{
			info.damage *= 1.0 + (Rnd.get() * (double) attacker.getRandomDamage() * 2.0 - (double) attacker.getRandomDamage()) / 100.0;
			if(dual)
			{
				info.damage /= 2.0;
			}
			if(info.crit)
			{
				info.damage *= 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
				info.damage = 2.0 * target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill);
				info.damage += info.crit_static;
			}
		}
		int chance;
		if(info.crit && (chance = attacker.getSkillLevel(467)) > 0)
		{
			if(chance >= 21)
			{
				chance = 30;
			}
			else if(chance >= 15)
			{
				chance = 25;
			}
			else if(chance >= 9)
			{
				chance = 20;
			}
			else if(chance >= 4)
			{
				chance = 15;
			}
			if(Rnd.chance(chance))
			{
				attacker.setConsumedSouls(attacker.getConsumedSouls() + 1, null);
			}
		}
		if(skill == null || !skill.isChargeBoost())
		{
			switch(PositionUtils.getDirectionTo(target, attacker))
			{
				case BEHIND:
				{
					info.damage *= 1.1;
					break;
				}
				case SIDE:
				{
					info.damage *= 1.05;
				}
			}
		}
		if(ss)
		{
			info.damage *= blow ? 1.0 : 2.0;
		}
		info.damage *= 70.0 / info.defence;
		info.damage = attacker.calcStat(Stats.PHYSICAL_DAMAGE, info.damage, target, skill);
		if(info.shld && Rnd.chance(5))
		{
			info.damage = 1.0;
		}
		if(isPvP)
		{
			if(skill == null)
			{
				info.damage *= attacker.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1.0, null, null);
				info.damage /= target.calcStat(Stats.PVP_PHYS_DEFENCE_BONUS, 1.0, null, null);
			}
			else
			{
				info.damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1.0, null, null);
				info.damage /= target.calcStat(Stats.PVP_PHYS_SKILL_DEFENCE_BONUS, 1.0, null, null);
			}
		}
		if(skill != null)
		{
			if(info.shld)
			{
				if(info.damage == 1.0)
				{
					target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				}
				else
				{
					target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
				}
			}
			if(info.damage > 1.0 && !skill.hasEffects() && Rnd.chance(target.calcStat(Stats.PSKILL_EVASION, 0.0, attacker, skill)))
			{
				attacker.sendPacket(new SystemMessage(43));
				target.sendPacket(new SystemMessage(42).addName(attacker));
				info.damage = 0.0;
			}
			if(info.damage > 1.0 && skill.isDeathlink())
			{
				info.damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());
			}
			if(onCrit && !calcBlow(attacker, target, skill))
			{
				info.miss = true;
				info.damage = 0.0;
				attacker.sendPacket(new SystemMessage(43));
			}
			if(blow)
			{
				if(Rnd.chance(info.lethal1))
				{
					if(target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = target.getCurrentCp();
						target.sendPacket(Msg.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
					}
					else if(target.isLethalImmune())
					{
						info.damage *= 2.0;
					}
					else
					{
						info.lethal_dmg = target.getCurrentHp() / 2.0;
					}
					attacker.sendPacket(Msg.HALF_KILL);
				}
				else if(Rnd.chance(info.lethal2))
				{
					if(target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = target.getCurrentHp() + target.getCurrentCp() - 1.1;
						target.sendPacket(SystemMsg.LETHAL_STRIKE);
					}
					else if(target.isLethalImmune())
					{
						info.damage *= 3.0;
					}
					else
					{
						info.lethal_dmg = target.getCurrentHp() - 1.0;
					}
					attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
				}
			}
			if(info.damage > 0.0)
			{
				attacker.displayGiveDamageMessage(target, (int) info.damage, info.crit || blow, false, false, false);
			}
			if(target.isStunned() && calcStunBreak(info.crit))
			{
				target.getEffectList().stopEffects(EffectType.Stun);
			}
			if(calcCastBreak(target, info.crit))
			{
				target.abortCast(false, true);
			}
		}
		return info;
	}
	
	public static double calcMagicDam(Creature attacker, Creature target, Skill skill, int sps)
	{
		boolean isPvP = attacker.isPlayable() && target.isPlayable();
		boolean shield = skill.getShieldIgnore() && calcShldUse(attacker, target);
		double mAtk = attacker.getMAtk(target, skill);
		if(sps == 2)
		{
			mAtk *= 4.0;
		}
		else if(sps == 1)
		{
			mAtk *= 2.0;
		}
		double mdef = target.getMDef(null, skill);
		if(shield)
		{
			mdef += (double) target.getShldDef();
		}
		if(mdef == 0.0)
		{
			mdef = 1.0;
		}
		double power = skill.getPower(target);
		double lethalDamage = 0.0;
		if(Rnd.chance(skill.getLethal1()))
		{
			if(target.isPlayer())
			{
				lethalDamage = target.getCurrentCp();
				target.sendPacket(Msg.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
			}
			else if(!target.isLethalImmune())
			{
				lethalDamage = target.getCurrentHp() / 2.0;
			}
			else
			{
				power *= 2.0;
			}
			attacker.sendPacket(Msg.HALF_KILL);
		}
		else if(Rnd.chance(skill.getLethal2()))
		{
			if(target.isPlayer())
			{
				lethalDamage = target.getCurrentHp() + target.getCurrentCp() - 1.1;
				target.sendPacket(SystemMsg.LETHAL_STRIKE);
			}
			else if(!target.isLethalImmune())
			{
				lethalDamage = target.getCurrentHp() - 1.0;
			}
			else
			{
				power *= 3.0;
			}
			attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
		}
		if(power == 0.0)
		{
			if(lethalDamage > 0.0)
			{
				attacker.displayGiveDamageMessage(target, (int) lethalDamage, false, false, false, false);
			}
			return lethalDamage;
		}
		if(skill.isSoulBoost())
		{
			power *= 1.0 + 0.06 * (double) Math.min(attacker.getConsumedSouls(), 5);
		}
		double damage = 91.0 * power * Math.sqrt(mAtk) / mdef;
		boolean crit = calcMCrit(attacker, target, attacker.getMagicCriticalRate(target, skill));
		if(crit)
		{
			damage *= attacker.calcStat(Stats.MCRITICAL_DAMAGE, Config.MCRITICAL_CRIT_POWER, target, skill);
		}
		damage = attacker.calcStat(Stats.MAGIC_DAMAGE, damage, target, skill);
		if(shield)
		{
			if(Rnd.chance(5))
			{
				damage = 0.0;
				target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				attacker.sendPacket(new SystemMessage(159).addName(attacker));
			}
			else
			{
				target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
				attacker.sendPacket(new SystemMessage(2151));
			}
		}
		int mLevel = skill.getMagicLevel() == 0 ? attacker.getLevel() : skill.getMagicLevel();
		int levelDiff = target.getLevel() - mLevel;
		if(damage > 1.0 && skill.isDeathlink())
		{
			damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());
		}
		if(damage > 1.0 && skill.isBasedOnTargetDebuff())
		{
			damage *= 1.0 + 0.05 * (double) Math.min(36, target.getEffectList().getAllEffects().size());
		}
		damage += lethalDamage;
		if(skill.getSkillType() == Skill.SkillType.MANADAM)
		{
			damage = Math.max(1.0, damage / 4.0);
		}
		if(isPvP && damage > 1.0)
		{
			damage *= attacker.calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1.0, null, null);
			damage /= target.calcStat(Stats.PVP_MAGIC_SKILL_DEFENCE_BONUS, 1.0, null, null);
		}
		boolean gradePenalty = attacker.isPlayer() && ((Player) attacker).getGradePenalty() > 0;
		double lvlMod = 4.0 * Math.max(1.0, target.getLevel() >= 80 ? (double) (levelDiff - 4) * 1.6 : (double) ((levelDiff - 14) * 2));
		double magic_rcpt = target.calcStat(Stats.MAGIC_RESIST, attacker, skill) - attacker.calcStat(Stats.MAGIC_POWER, target, skill);
		double failChance = gradePenalty ? 95.0 : Math.min(lvlMod * (1.0 + magic_rcpt / 100.0), 95.0);
		double resistChance;
		double d = resistChance = gradePenalty ? 95.0 : (double) (5 * Math.max(levelDiff - 10, 1));
		if(attacker.isPlayer() && ((Player) attacker).isDebug())
		{
			attacker.sendMessage("Fail chance " + (int) failChance + "/" + (int) resistChance);
		}
		if(Rnd.chance(failChance))
		{
			if(Rnd.chance(resistChance))
			{
				damage = 0.0;
				SystemMessage msg = new SystemMessage(158);
				attacker.sendPacket(msg);
				SystemMessage msg1 = new SystemMessage(159).addName(attacker);
				target.sendPacket(msg1);
			}
			else
			{
				damage /= 2.0;
				SystemMessage msg = new SystemMessage(158);
				attacker.sendPacket(msg);
				SystemMessage msg1 = new SystemMessage(159).addName(attacker);
				target.sendPacket(msg1);
			}
		}
		if(damage > 0.0)
		{
			attacker.displayGiveDamageMessage(target, (int) damage, crit, false, false, true);
		}
		if(calcCastBreak(target, crit))
		{
			target.abortCast(false, true);
		}
		return damage;
	}
	
	public static boolean calcStunBreak(boolean crit)
	{
		return Rnd.chance(crit ? 75 : 10);
	}
	
	public static boolean calcBlow(Creature activeChar, Creature target, Skill skill)
	{
		WeaponTemplate weapon = activeChar.getActiveWeaponItem();
		double base_weapon_crit = weapon == null ? 4.0 : (double) weapon.getCritical();
		double crit_height_bonus = 0.008 * (double) Math.min(25, Math.max(-25, target.getZ() - activeChar.getZ())) + 1.1;
		double buffs_mult = activeChar.calcStat(Stats.FATALBLOW_RATE, target, skill);
		double skill_mod = skill.isBehind() ? 3.0 : 2.0;
		double chance = base_weapon_crit * buffs_mult * crit_height_bonus * skill_mod;
		if(!target.isInCombat())
		{
			chance *= 1.1;
		}
		switch(PositionUtils.getDirectionTo(target, activeChar))
		{
			case BEHIND:
			{
				chance *= 1.1;
				break;
			}
			case SIDE:
			{
				chance *= 1.05;
				break;
			}
			case FRONT:
			{
				if(!skill.isBehind())
					break;
				chance = 3.0;
			}
		}
		chance = Math.min(skill.isBehind() ? 100.0 : 80.0, chance);
		return Rnd.chance(chance);
	}
	
	public static double calcCrit(Creature attacker, Creature target, Skill skill, boolean blow)
	{
		if(attacker.isPlayer() && attacker.getActiveWeaponItem() == null)
		{
			return 0.0;
		}
		if(skill != null)
		{
			return (double) skill.getCriticalRate() * (blow ? BaseStats.DEX.calcBonus(attacker) : BaseStats.STR.calcBonus(attacker)) * 0.01 * attacker.calcStat(Stats.SKILL_CRIT_CHANCE_MOD, target, skill);
		}
		double rate = (double) attacker.getCriticalHit(target, null) * 0.01 * target.calcStat(Stats.CRIT_CHANCE_RECEPTIVE, attacker, skill);
		switch(PositionUtils.getDirectionTo(target, attacker))
		{
			case BEHIND:
			{
				rate *= 1.1;
				break;
			}
			case SIDE:
			{
				rate *= 1.05;
			}
		}
		return rate / 10.0;
	}
	
	public static boolean calcMCrit(Creature attacker, Creature target, double mRate)
	{
		if(attacker != null && attacker.isNpc())
		{
			return Rnd.get() * 100.0 <= Math.min((double) Config.ALT_NPC_LIM_MCRIT, mRate);
		}
		return Rnd.get() * 100.0 <= Math.min((double) Config.LIM_MCRIT, mRate);
	}
	
	public static boolean calcCastBreak(Creature target, boolean crit)
	{
		if(target == null || target.isInvul() || target.isRaid() || !target.isCastingNow())
		{
			return false;
		}
		Skill skill = target.getCastingSkill();
		if(!(skill == null || skill.isMagic() && skill.getSkillType() != Skill.SkillType.TAKECASTLE))
		{
			return false;
		}
		return Rnd.chance(target.calcStat(Stats.CAST_INTERRUPT, crit ? 75.0 : 10.0, null, skill));
	}
	
	public static int calcPAtkSpd(double rate)
	{
		double base = 500.0 / rate;
		int result = (int) (base * 1000.0 * 0.9777777791023254);
		if(base * 1000.0 > (double) result)
		{
			result += (int) ((double) -result - base * -1000.0);
		}
		return result;
	}
	
	public static int calcMAtkSpd(Creature attacker, Skill skill, double skillTime)
	{
		if(skill.isMagic())
		{
			return (int) (skillTime * 333.0 / (double) Math.max(attacker.getMAtkSpd(), 1));
		}
		return (int) (skillTime * 333.0 / (double) Math.max(attacker.getPAtkSpd(), 1));
	}
	
	public static long calcSkillReuseDelay(Creature actor, Skill skill)
	{
		long reuseDelay = skill.getReuseDelay();
		if(actor.isMonster())
		{
			reuseDelay = skill.getReuseForMonsters();
		}
		if(skill.isReuseDelayPermanent() || skill.isHandler() || skill.isItemSkill())
		{
			return reuseDelay;
		}
		if(actor.getSkillMastery(skill.getId()) == 1)
		{
			actor.removeSkillMastery(skill.getId());
			return 0;
		}
		if(skill.isMusic())
		{
			return (long) actor.calcStat(Stats.MUSIC_REUSE_RATE, reuseDelay, null, skill) * 333 / (long) Math.max(actor.getMAtkSpd(), 1);
		}
		if(skill.isMagic())
		{
			return (long) actor.calcStat(Stats.MAGIC_REUSE_RATE, reuseDelay, null, skill) * 333 / (long) Math.max(actor.getMAtkSpd(), 1);
		}
		return (long) actor.calcStat(Stats.PHYSIC_REUSE_RATE, reuseDelay, null, skill) * 333 / (long) Math.max(actor.getPAtkSpd(), 1);
	}
	
	public static boolean calcHitMiss(Creature attacker, Creature target)
	{
		int chanceToHit = 88 + 2 * (attacker.getAccuracy() - target.getEvasionRate(attacker));
		chanceToHit = Math.max(chanceToHit, 28);
		chanceToHit = Math.min(chanceToHit, 98);
		PositionUtils.TargetDirection direction = PositionUtils.getDirectionTo(attacker, target);
		switch(direction)
		{
			case BEHIND:
			{
				chanceToHit = (int) ((double) chanceToHit * 1.2);
				break;
			}
			case SIDE:
			{
				chanceToHit = (int) ((double) chanceToHit * 1.1);
			}
		}
		return !Rnd.chance(chanceToHit);
	}
	
	public static boolean calcShldUse(Creature attacker, Creature target)
	{
		WeaponTemplate template = target.getSecondaryWeaponItem();
		if(template == null || template.getItemType() != WeaponTemplate.WeaponType.NONE)
		{
			return false;
		}
		int angle = (int) target.calcStat(Stats.SHIELD_ANGLE, attacker, null);
		if(!PositionUtils.isFacing(target, attacker, angle))
		{
			return false;
		}
		return Rnd.chance((int) target.calcStat(Stats.SHIELD_RATE, attacker, null));
	}
	
	public static boolean calcSkillSuccess(Env env, EffectTemplate et, int spiritshot)
	{
		if(env.value == -1.0)
		{
			return true;
		}
		double base = env.value = Math.max(Math.min(env.value, 100.0), 1.0);
		Skill skill = env.skill;
		if(!skill.isOffensive())
		{
			return Rnd.chance(env.value);
		}
		Creature caster = env.character;
		Creature target = env.target;
		boolean debugCaster = false;
		boolean debugTarget = false;
		boolean debugGlobal = false;
		if(Config.ALT_DEBUG_ENABLED)
		{
			debugCaster = caster.getPlayer() != null && caster.getPlayer().isDebug();
			debugTarget = target.getPlayer() != null && target.getPlayer().isDebug();
			boolean debugPvP = Config.ALT_DEBUG_PVP_ENABLED && debugCaster && debugTarget && (!Config.ALT_DEBUG_PVP_DUEL_ONLY || caster.getPlayer().isInDuel() && target.getPlayer().isInDuel());
			debugGlobal = debugPvP || Config.ALT_DEBUG_PVE_ENABLED && (debugCaster && target.isMonster() || debugTarget && caster.isMonster());
		}
		double statMod = 1.0;
		if(skill.getSaveVs() != null)
		{
			statMod = skill.getSaveVs().calcChanceMod(target);
			env.value *= statMod;
		}
		env.value = Math.max(env.value, 1.0);
		double mAtkMod = 1.0;
		if(skill.isMagic())
		{
			int mdef = Math.max(1, target.getMDef(target, skill));
			double matk = caster.getMAtk(target, skill);
			if(skill.isSSPossible())
			{
				int ssMod;
				switch(spiritshot)
				{
					case 2:
					{
						ssMod = 4;
						break;
					}
					case 1:
					{
						ssMod = 2;
						break;
					}
					default:
					{
						ssMod = 1;
					}
				}
				matk *= (double) ssMod;
			}
			mAtkMod = Config.SKILLS_CHANCE_MOD * Math.pow(matk, Config.SKILLS_CHANCE_POW) / (double) mdef;
			env.value *= mAtkMod;
			env.value = Math.max(env.value, 1.0);
		}
		double lvlDependMod;
		if((lvlDependMod = (double) skill.getLevelModifier()) != 0.0)
		{
			int attackLevel = skill.getMagicLevel() > 0 ? skill.getMagicLevel() : caster.getLevel();
			lvlDependMod = 1.0 + (double) (attackLevel - target.getLevel()) * 0.03 * lvlDependMod;
			if(lvlDependMod < 0.0)
			{
				lvlDependMod = 0.0;
			}
			else if(lvlDependMod > 2.0)
			{
				lvlDependMod = 2.0;
			}
			env.value *= lvlDependMod;
		}
		double vulnMod = 0.0;
		double profMod = 0.0;
		double resMod = 1.0;
		double debuffMod = 1.0;
		if(!skill.isIgnoreResists())
		{
			debuffMod = 1.0 - target.calcStat(Stats.DEBUFF_RESIST, caster, skill) / 120.0;
			if(debuffMod != 1.0)
			{
				if(debuffMod == Double.NEGATIVE_INFINITY)
				{
					if(debugGlobal)
					{
						if(debugCaster)
						{
							caster.getPlayer().sendMessage("Full debuff immunity");
						}
						if(debugTarget)
						{
							target.getPlayer().sendMessage("Full debuff immunity");
						}
					}
					return false;
				}
				if(debuffMod == Double.POSITIVE_INFINITY)
				{
					if(debugGlobal)
					{
						if(debugCaster)
						{
							caster.getPlayer().sendMessage("Full debuff vulnerability");
						}
						if(debugTarget)
						{
							target.getPlayer().sendMessage("Full debuff vulnerability");
						}
					}
					return true;
				}
				debuffMod = Math.max(debuffMod, 0.0);
				env.value *= debuffMod;
			}
			SkillTrait trait;
			if((trait = skill.getTraitType()) != null)
			{
				vulnMod = trait.calcVuln(env);
				profMod = trait.calcProf(env);
				double maxResist = 90.0 + profMod * 0.85;
				resMod = (maxResist - vulnMod) / 60.0;
			}
			if(resMod != 1.0)
			{
				if(resMod == Double.NEGATIVE_INFINITY)
				{
					if(debugGlobal)
					{
						if(debugCaster)
						{
							caster.getPlayer().sendMessage("Full immunity");
						}
						if(debugTarget)
						{
							target.getPlayer().sendMessage("Full immunity");
						}
					}
					return false;
				}
				if(resMod == Double.POSITIVE_INFINITY)
				{
					if(debugGlobal)
					{
						if(debugCaster)
						{
							caster.getPlayer().sendMessage("Full vulnerability");
						}
						if(debugTarget)
						{
							target.getPlayer().sendMessage("Full vulnerability");
						}
					}
					return true;
				}
				resMod = Math.max(resMod, 0.0);
				env.value *= resMod;
			}
		}
		double elementMod = 0.0;
		Element element = skill.getElement();
		if(element != Element.NONE)
		{
			elementMod = skill.getElementPower();
			Element attackElement = getAttackElement(caster, target);
			if(attackElement == element)
			{
				elementMod += caster.calcStat(element.getAttack(), 0.0);
			}
			elementMod -= target.calcStat(element.getDefence(), 0.0);
			elementMod = Math.round(elementMod / 10.0);
			env.value += elementMod;
		}
		env.value = Math.max(env.value, Math.min(base, Config.SKILLS_CHANCE_MIN));
		env.value = Math.max(Math.min(env.value, Config.SKILLS_CHANCE_CAP), 1.0);
		boolean result = Rnd.chance((int) env.value);
		if(debugGlobal)
		{
			StringBuilder stat = new StringBuilder(100);
			if(et == null)
			{
				stat.append(skill.getName());
			}
			else
			{
				stat.append(et._effectType.name());
			}
			stat.append(" AR:");
			stat.append((int) base);
			stat.append(" ");
			if(skill.getSaveVs() != null)
			{
				stat.append(skill.getSaveVs().name());
				stat.append(":");
				stat.append(String.format("%1.1f", statMod));
			}
			if(skill.isMagic())
			{
				stat.append(" ");
				stat.append(" mAtk:");
				stat.append(String.format("%1.1f", mAtkMod));
			}
			if(skill.getTraitType() != null)
			{
				stat.append(" ");
				stat.append(skill.getTraitType().name());
			}
			stat.append(" ");
			stat.append(String.format("%1.1f", resMod));
			stat.append("(");
			stat.append(String.format("%1.1f", profMod));
			stat.append("/");
			stat.append(String.format("%1.1f", vulnMod));
			if(debuffMod != 0.0)
			{
				stat.append("+");
				stat.append(String.format("%1.1f", debuffMod));
			}
			stat.append(") lvl:");
			stat.append(String.format("%1.1f", lvlDependMod));
			stat.append(" elem:");
			stat.append((int) elementMod);
			stat.append(" Chance:");
			stat.append(String.format("%1.1f", env.value));
			if(!result)
			{
				stat.append(" failed");
			}
			if(debugCaster)
			{
				caster.getPlayer().sendMessage(stat.toString());
			}
			if(debugTarget)
			{
				target.getPlayer().sendMessage(stat.toString());
			}
		}
		return result;
	}
	
	public static boolean calcSkillSuccess(Creature player, Creature target, Skill skill, int activateRate)
	{
		Env env = new Env();
		env.character = player;
		env.target = target;
		env.skill = skill;
		env.value = activateRate;
		return calcSkillSuccess(env, null, player.getChargedSpiritShot());
	}
	
	public static void calcSkillMastery(Skill skill, Creature activeChar)
	{
		if(skill.isHandler())
		{
			return;
		}
		if(activeChar.getSkillLevel(331) > 0 && activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getINT(), null, skill) >= (double) Rnd.get(5000) || activeChar.getSkillLevel(330) > 0 && activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getSTR(), null, skill) >= (double) Rnd.get(5000))
		{
			Skill.SkillType type = skill.getSkillType();
			int masteryLevel = skill.isMusic() || type == Skill.SkillType.BUFF || type == Skill.SkillType.HOT || type == Skill.SkillType.HEAL_PERCENT ? 2 : type == Skill.SkillType.HEAL ? 3 : 1;
			if(masteryLevel > 0)
			{
				activeChar.setSkillMastery(skill.getId(), masteryLevel);
			}
		}
	}
	
	public static double calcDamageResists(Skill skill, Creature attacker, Creature defender, double value)
	{
		if(attacker == defender)
		{
			return value;
		}
		if(attacker.isBoss())
		{
			value *= Config.RATE_EPIC_ATTACK;
		}
		else if(attacker.isRaid() || attacker instanceof ReflectionBossInstance)
		{
			value *= Config.RATE_RAID_ATTACK;
		}
		if(defender.isBoss())
		{
			value /= Config.RATE_EPIC_DEFENSE;
		}
		else if(defender.isRaid() || defender instanceof ReflectionBossInstance)
		{
			value /= Config.RATE_RAID_DEFENSE;
		}
		Player pAttacker = attacker.getPlayer();
		int diff = defender.getLevel() - (pAttacker != null ? pAttacker.getLevel() : attacker.getLevel());
		if(attacker.isPlayable() && defender.isMonster() && defender.getLevel() >= 78 && diff > 2)
		{
			value *= 0.7 / Math.pow(diff - 2, 0.25);
		}
		Element element;
		double power = 0.0;
		if(skill != null)
		{
			element = skill.getElement();
			power = skill.getElementPower();
		}
		else
		{
			element = getAttackElement(attacker, defender);
		}
		if(element == Element.NONE)
		{
			return value;
		}
		if(pAttacker != null && pAttacker.isGM() && Config.DEBUG)
		{
			pAttacker.sendMessage("Element: " + element.name());
			pAttacker.sendMessage("Attack: " + attacker.calcStat(element.getAttack(), power));
			pAttacker.sendMessage("Defence: " + defender.calcStat(element.getDefence(), 0.0));
			pAttacker.sendMessage("Modifier: " + getElementMod(defender.calcStat(element.getDefence(), 0.0), attacker.calcStat(element.getAttack(), power)));
		}
		return value * getElementMod(defender.calcStat(element.getDefence(), 0.0), attacker.calcStat(element.getAttack(), power));
	}
	
	private static double getElementMod(double defense, double attack)
	{
		if(defense < 0.0)
		{
			attack += -defense;
			defense = 0.0;
		}
		double attrAtk = 1.0 + attack / 100.0;
		double attrDef = 1.0 + defense / 100.0;
		return attrAtk / attrDef;
	}
	
	public static Element getAttackElement(Creature attacker, Creature target)
	{
		double max = Double.MIN_VALUE;
		Element result = Element.NONE;
		for(Element e : Element.VALUES)
		{
			double val = attacker.calcStat(e.getAttack(), 0.0, null, null);
			if(val <= 0.0)
				continue;
			if(target != null)
			{
				val -= target.calcStat(e.getDefence(), 0.0, null, null);
			}
			if(val <= max)
				continue;
			result = e;
			max = val;
		}
		return result;
	}
	
	public static class AttackInfo
	{
		public double damage;
		public double defence;
		public double crit_static;
		public double death_rcpt;
		public double lethal1;
		public double lethal2;
		public double lethal_dmg;
		public boolean crit;
		public boolean shld;
		public boolean lethal;
		public boolean miss;
	}
}