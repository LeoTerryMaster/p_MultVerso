package l2.gameserver.stats;

import l2.gameserver.Config;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Summon;
import l2.gameserver.model.base.BaseStats;
import l2.gameserver.model.base.Element;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.stats.conditions.ConditionPlayerState;
import l2.gameserver.stats.funcs.Func;
import l2.gameserver.tables.LevelUpTable;
import l2.gameserver.templates.item.WeaponTemplate;

public class StatFunctions
{
	public static void addPredefinedFuncs(Creature cha)
	{
		if(cha.isPlayer())
		{
			cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncMaxCpAdd.func);
			cha.addStatFunc(FuncMaxHpAdd.func);
			cha.addStatFunc(FuncMaxMpAdd.func);
			cha.addStatFunc(FuncMaxCpMul.func);
			cha.addStatFunc(FuncMaxHpMul.func);
			cha.addStatFunc(FuncMaxMpMul.func);
			cha.addStatFunc(FuncAttackRange.func);
			cha.addStatFunc(FuncMoveSpeedMul.func);
			cha.addStatFunc(FuncHennaSTR.func);
			cha.addStatFunc(FuncHennaDEX.func);
			cha.addStatFunc(FuncHennaINT.func);
			cha.addStatFunc(FuncHennaMEN.func);
			cha.addStatFunc(FuncHennaCON.func);
			cha.addStatFunc(FuncHennaWIT.func);
			cha.addStatFunc(FuncInventory.func);
			cha.addStatFunc(FuncWarehouse.func);
			cha.addStatFunc(FuncTradeLimit.func);
			cha.addStatFunc(FuncSDefPlayers.func);
			cha.addStatFunc(FuncMaxHpLimit.func);
			cha.addStatFunc(FuncMaxMpLimit.func);
			cha.addStatFunc(FuncMaxCpLimit.func);
			cha.addStatFunc(FuncRunSpdLimit.func);
			cha.addStatFunc(FuncRunSpdLimit.func);
			cha.addStatFunc(FuncPDefLimit.func);
			cha.addStatFunc(FuncMDefLimit.func);
			cha.addStatFunc(FuncPAtkLimit.func);
			cha.addStatFunc(FuncMAtkLimit.func);
		}
		if(cha.isPlayer() || cha.isPet())
		{
			cha.addStatFunc(FuncPAtkMul.func);
			cha.addStatFunc(FuncMAtkMul.func);
			cha.addStatFunc(FuncPDefMul.func);
			cha.addStatFunc(FuncMDefMul.func);
		}
		if(!cha.isPet())
		{
			cha.addStatFunc(FuncAccuracyAdd.func);
			cha.addStatFunc(FuncEvasionAdd.func);
		}
		if(!cha.isPet() && !cha.isSummon())
		{
			cha.addStatFunc(FuncPAtkSpeedMul.func);
			cha.addStatFunc(FuncMAtkSpeedMul.func);
			cha.addStatFunc(FuncSDefInit.func);
			cha.addStatFunc(FuncSDefAll.func);
		}
		else
		{
			cha.addStatFunc(FuncMaxHpMul.func);
			cha.addStatFunc(FuncMaxMpMul.func);
		}
		cha.addStatFunc(FuncPAtkSpdLimit.func);
		cha.addStatFunc(FuncMAtkSpdLimit.func);
		cha.addStatFunc(FuncCAtkLimit.func);
		cha.addStatFunc(FuncEvasionLimit.func);
		cha.addStatFunc(FuncAccuracyLimit.func);
		cha.addStatFunc(FuncCritLimit.func);
		cha.addStatFunc(FuncMCritLimit.func);
		cha.addStatFunc(FuncMCriticalRateMul.func);
		cha.addStatFunc(FuncPCriticalRateMul.func);
		cha.addStatFunc(FuncPDamageResists.func);
		cha.addStatFunc(FuncMDamageResists.func);
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.FIRE));
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.WATER));
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.EARTH));
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.WIND));
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.HOLY));
		cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.UNHOLY));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.FIRE));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.WATER));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.EARTH));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.WIND));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.HOLY));
		cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.UNHOLY));
	}
	
	private static class FuncAttributeDefenceInit extends Func
	{
		static final Func[] func = new FuncAttributeDefenceInit[Element.VALUES.length];
		
		static
		{
			for(int i = 0;i < Element.VALUES.length;++i)
			{
				func[i] = new FuncAttributeDefenceInit(Element.VALUES[i]);
			}
		}
		
		private final Element element;
		
		private FuncAttributeDefenceInit(Element element)
		{
			super(element.getDefence(), 1, null);
			this.element = element;
		}
		
		static Func getFunc(Element element)
		{
			return func[element.getId()];
		}
		
		@Override
		public void calc(Env env)
		{
			env.value += (double) env.character.getTemplate().baseAttributeDefence[element.getId()];
		}
	}
	
	private static class FuncAttributeAttackInit extends Func
	{
		static final Func[] func = new FuncAttributeAttackInit[Element.VALUES.length];
		
		static
		{
			for(int i = 0;i < Element.VALUES.length;++i)
			{
				func[i] = new FuncAttributeAttackInit(Element.VALUES[i]);
			}
		}
		
		private final Element element;
		
		private FuncAttributeAttackInit(Element element)
		{
			super(element.getAttack(), 1, null);
			this.element = element;
		}
		
		static Func getFunc(Element element)
		{
			return func[element.getId()];
		}
		
		@Override
		public void calc(Env env)
		{
			env.value += (double) env.character.getTemplate().baseAttributeAttack[element.getId()];
		}
	}
	
	private static class FuncMCritLimit extends Func
	{
		static final Func func = new FuncMCritLimit();
		
		private FuncMCritLimit()
		{
			super(Stats.MCRITICAL_RATE, 256, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = Math.min((double) Config.LIM_MCRIT, env.value);
		}
	}
	
	private static class FuncCritLimit extends Func
	{
		static final Func func = new FuncCritLimit();
		
		private FuncCritLimit()
		{
			super(Stats.CRITICAL_BASE, 256, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = Math.min((double) Config.LIM_CRIT, env.value);
		}
	}
	
	private static class FuncAccuracyLimit extends Func
	{
		static final Func func = new FuncAccuracyLimit();
		
		private FuncAccuracyLimit()
		{
			super(Stats.ACCURACY_COMBAT, 256, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = Math.min((double) Config.LIM_ACCURACY, env.value);
		}
	}
	
	private static class FuncEvasionLimit extends Func
	{
		static final Func func = new FuncEvasionLimit();
		
		private FuncEvasionLimit()
		{
			super(Stats.EVASION_RATE, 256, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = Math.min((double) Config.LIM_EVASION, env.value);
		}
	}
	
	private static class FuncCAtkLimit extends Func
	{
		static final Func func = new FuncCAtkLimit();
		
		private FuncCAtkLimit()
		{
			super(Stats.CRITICAL_DAMAGE, 256, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = Math.min((double) Config.LIM_CRIT_DAM / 2.0, env.value);
		}
	}
	
	private static class FuncMAtkSpdLimit extends Func
	{
		static final Func func = new FuncMAtkSpdLimit();
		
		private FuncMAtkSpdLimit()
		{
			super(Stats.MAGIC_ATTACK_SPEED, 256, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = Math.min((double) Config.LIM_MATK_SPD, env.value);
		}
	}
	
	private static class FuncPAtkSpdLimit extends Func
	{
		static final Func func = new FuncPAtkSpdLimit();
		
		private FuncPAtkSpdLimit()
		{
			super(Stats.POWER_ATTACK_SPEED, 256, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = Math.min((double) Config.LIM_PATK_SPD, env.value);
		}
	}
	
	private static class FuncMAtkLimit extends Func
	{
		static final Func func = new FuncMAtkLimit();
		
		private FuncMAtkLimit()
		{
			super(Stats.MAGIC_ATTACK, 256, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = Math.min((double) Config.LIM_MATK, env.value);
		}
	}
	
	private static class FuncPAtkLimit extends Func
	{
		static final Func func = new FuncPAtkLimit();
		
		private FuncPAtkLimit()
		{
			super(Stats.POWER_ATTACK, 256, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = Math.min((double) Config.LIM_PATK, env.value);
		}
	}
	
	private static class FuncMDefLimit extends Func
	{
		static final Func func = new FuncMDefLimit();
		
		private FuncMDefLimit()
		{
			super(Stats.MAGIC_DEFENCE, 256, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = Math.min((double) Config.LIM_MDEF, env.value);
		}
	}
	
	private static class FuncPDefLimit extends Func
	{
		static final Func func = new FuncPDefLimit();
		
		private FuncPDefLimit()
		{
			super(Stats.POWER_DEFENCE, 256, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = Math.min((double) Config.LIM_PDEF, env.value);
		}
	}
	
	private static class FuncRunSpdLimit extends Func
	{
		static final Func func = new FuncRunSpdLimit();
		
		private FuncRunSpdLimit()
		{
			super(Stats.RUN_SPEED, 256, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = Math.min((double) Config.LIM_MOVE, env.value);
		}
	}
	
	private static class FuncMaxCpLimit extends Func
	{
		static final Func func = new FuncMaxCpLimit();
		
		private FuncMaxCpLimit()
		{
			super(Stats.MAX_CP, 256, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = Math.min((double) Config.LIM_MAX_CP, env.value);
		}
	}
	
	private static class FuncMaxMpLimit extends Func
	{
		static final Func func = new FuncMaxMpLimit();
		
		private FuncMaxMpLimit()
		{
			super(Stats.MAX_MP, 256, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = Math.min((double) Config.LIM_MAX_MP, env.value);
		}
	}
	
	private static class FuncMaxHpLimit extends Func
	{
		static final Func func = new FuncMaxHpLimit();
		
		private FuncMaxHpLimit()
		{
			super(Stats.MAX_HP, 256, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = Math.min((double) Config.LIM_MAX_HP, env.value);
		}
	}
	
	private static class FuncSDefPlayers extends Func
	{
		static final FuncSDefPlayers func = new FuncSDefPlayers();
		
		private FuncSDefPlayers()
		{
			super(Stats.SHIELD_RATE, 32, null);
		}
		
		@Override
		public void calc(Env env)
		{
			if(env.value == 0.0)
			{
				return;
			}
			Creature cha = env.character;
			ItemInstance shld = ((Player) cha).getInventory().getPaperdollItem(8);
			if(shld == null || shld.getItemType() != WeaponTemplate.WeaponType.NONE)
			{
				return;
			}
			env.value *= BaseStats.DEX.calcBonus(env.character);
		}
	}
	
	private static class FuncSDefAll extends Func
	{
		static final FuncSDefAll func = new FuncSDefAll();
		
		private FuncSDefAll()
		{
			super(Stats.SHIELD_RATE, 32, null);
		}
		
		@Override
		public void calc(Env env)
		{
			if(env.value == 0.0)
			{
				return;
			}
			Creature target = env.target;
			WeaponTemplate weapon;
			if(target != null && (weapon = target.getActiveWeaponItem()) != null)
			{
				switch(weapon.getItemType())
				{
					case BOW:
					{
						env.value += 30.0;
						break;
					}
					case DAGGER:
					{
						env.value += 12.0;
					}
				}
			}
		}
	}
	
	private static class FuncSDefInit extends Func
	{
		static final Func func = new FuncSDefInit();
		
		private FuncSDefInit()
		{
			super(Stats.SHIELD_RATE, 1, null);
		}
		
		@Override
		public void calc(Env env)
		{
			Creature cha = env.character;
			env.value = cha.getTemplate().baseShldRate;
		}
	}
	
	private static class FuncTradeLimit extends Func
	{
		static final FuncTradeLimit func = new FuncTradeLimit();
		
		private FuncTradeLimit()
		{
			super(Stats.TRADE_LIMIT, 1, null);
		}
		
		@Override
		public void calc(Env env)
		{
			Player activeChar = (Player) env.character;
			if(activeChar.getRace() == Race.dwarf)
			{
				env.value = activeChar.getLevel() < 40 ? 3.0 : (double) Config.MAX_PVTSTORE_SLOTS_DWARF;
			}
			else
			{
				if(activeChar.getLevel() < 40)
				{
					env.value = 2.0;
				}
				env.value = Config.MAX_PVTSTORE_SLOTS_OTHER;
			}
		}
	}
	
	private static class FuncWarehouse extends Func
	{
		static final FuncWarehouse func = new FuncWarehouse();
		
		private FuncWarehouse()
		{
			super(Stats.STORAGE_LIMIT, 1, null);
		}
		
		@Override
		public void calc(Env env)
		{
			Player player = (Player) env.character;
			env.value = player.getTemplate().race == Race.dwarf ? (double) Config.WAREHOUSE_SLOTS_DWARF : (double) Config.WAREHOUSE_SLOTS_NO_DWARF;
			env.value += (double) player.getExpandWarehouse();
		}
	}
	
	private static class FuncInventory extends Func
	{
		static final FuncInventory func = new FuncInventory();
		
		private FuncInventory()
		{
			super(Stats.INVENTORY_LIMIT, 1, null);
		}
		
		@Override
		public void calc(Env env)
		{
			Player player = (Player) env.character;
			env.value = player.isGM() ? (double) Config.INVENTORY_MAXIMUM_GM : player.getTemplate().race == Race.dwarf ? (double) Config.INVENTORY_MAXIMUM_DWARF : (double) Config.INVENTORY_MAXIMUM_NO_DWARF;
			env.value += (double) player.getExpandInventory();
			env.value = Math.min(env.value, (double) Config.SERVICES_EXPAND_INVENTORY_MAX);
		}
	}
	
	private static class FuncMDamageResists extends Func
	{
		static final FuncMDamageResists func = new FuncMDamageResists();
		
		private FuncMDamageResists()
		{
			super(Stats.MAGIC_DAMAGE, 48, null);
		}
		
		@Override
		public void calc(Env env)
		{
			if(env.target.isRaid() && Math.abs(env.character.getLevel() - env.target.getLevel()) > Config.RAID_MAX_LEVEL_DIFF)
			{
				env.value = 1.0;
				return;
			}
			env.value = Formulas.calcDamageResists(env.skill, env.character, env.target, env.value);
		}
	}
	
	private static class FuncPDamageResists extends Func
	{
		static final FuncPDamageResists func = new FuncPDamageResists();
		
		private FuncPDamageResists()
		{
			super(Stats.PHYSICAL_DAMAGE, 48, null);
		}
		
		@Override
		public void calc(Env env)
		{
			if(env.target.isRaid() && env.character.getLevel() - env.target.getLevel() > Config.RAID_MAX_LEVEL_DIFF)
			{
				env.value = 1.0;
				return;
			}
			WeaponTemplate weapon = env.character.getActiveWeaponItem();
			if(weapon == null)
			{
				env.value *= 0.01 * env.target.calcStat(Stats.FIST_WPN_VULNERABILITY, env.character, env.skill);
			}
			else if(weapon.getItemType().getDefence() != null)
			{
				env.value *= 0.01 * env.target.calcStat(weapon.getItemType().getDefence(), env.character, env.skill);
			}
			env.value = Formulas.calcDamageResists(env.skill, env.character, env.target, env.value);
		}
	}
	
	private static class FuncMaxMpMul extends Func
	{
		static final FuncMaxMpMul func = new FuncMaxMpMul();
		
		private FuncMaxMpMul()
		{
			super(Stats.MAX_MP, 32, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.MEN.calcBonus(env.character);
		}
	}
	
	private static class FuncMaxMpAdd extends Func
	{
		static final FuncMaxMpAdd func = new FuncMaxMpAdd();
		
		private FuncMaxMpAdd()
		{
			super(Stats.MAX_MP, 16, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value += LevelUpTable.getInstance().getMaxMP(env.character);
		}
	}
	
	private static class FuncMaxCpMul extends Func
	{
		static final FuncMaxCpMul func = new FuncMaxCpMul();
		
		private FuncMaxCpMul()
		{
			super(Stats.MAX_CP, 32, null);
		}
		
		@Override
		public void calc(Env env)
		{
			double cpSSmod = 1.0;
			int sealOwnedBy = SevenSigns.getInstance().getSealOwner(3);
			int playerCabal = SevenSigns.getInstance().getPlayerCabal((Player) env.character);
			if(sealOwnedBy != 0)
			{
				cpSSmod = playerCabal == sealOwnedBy ? 1.1 : 0.9;
			}
			env.value *= BaseStats.CON.calcBonus(env.character) * cpSSmod;
		}
	}
	
	private static class FuncMaxCpAdd extends Func
	{
		static final FuncMaxCpAdd func = new FuncMaxCpAdd();
		
		private FuncMaxCpAdd()
		{
			super(Stats.MAX_CP, 16, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value += env.value += LevelUpTable.getInstance().getMaxCP(env.character);
		}
	}
	
	private static class FuncMaxHpMul extends Func
	{
		static final FuncMaxHpMul func = new FuncMaxHpMul();
		
		private FuncMaxHpMul()
		{
			super(Stats.MAX_HP, 32, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.CON.calcBonus(env.character);
		}
	}
	
	private static class FuncMaxHpAdd extends Func
	{
		static final FuncMaxHpAdd func = new FuncMaxHpAdd();
		
		private FuncMaxHpAdd()
		{
			super(Stats.MAX_HP, 16, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value += LevelUpTable.getInstance().getMaxHP(env.character);
		}
	}
	
	private static class FuncHennaWIT extends Func
	{
		static final FuncHennaWIT func = new FuncHennaWIT();
		
		private FuncHennaWIT()
		{
			super(Stats.STAT_WIT, 16, null);
		}
		
		@Override
		public void calc(Env env)
		{
			Player pc = (Player) env.character;
			if(pc != null)
			{
				env.value = Math.max(1.0, env.value + (double) pc.getHennaStatWIT());
			}
		}
	}
	
	private static class FuncHennaCON extends Func
	{
		static final FuncHennaCON func = new FuncHennaCON();
		
		private FuncHennaCON()
		{
			super(Stats.STAT_CON, 16, null);
		}
		
		@Override
		public void calc(Env env)
		{
			Player pc = (Player) env.character;
			if(pc != null)
			{
				env.value = Math.max(1.0, env.value + (double) pc.getHennaStatCON());
			}
		}
	}
	
	private static class FuncHennaMEN extends Func
	{
		static final FuncHennaMEN func = new FuncHennaMEN();
		
		private FuncHennaMEN()
		{
			super(Stats.STAT_MEN, 16, null);
		}
		
		@Override
		public void calc(Env env)
		{
			Player pc = (Player) env.character;
			if(pc != null)
			{
				env.value = Math.max(1.0, env.value + (double) pc.getHennaStatMEN());
			}
		}
	}
	
	private static class FuncHennaINT extends Func
	{
		static final FuncHennaINT func = new FuncHennaINT();
		
		private FuncHennaINT()
		{
			super(Stats.STAT_INT, 16, null);
		}
		
		@Override
		public void calc(Env env)
		{
			Player pc = (Player) env.character;
			if(pc != null)
			{
				env.value = Math.max(1.0, env.value + (double) pc.getHennaStatINT());
			}
		}
	}
	
	private static class FuncHennaDEX extends Func
	{
		static final FuncHennaDEX func = new FuncHennaDEX();
		
		private FuncHennaDEX()
		{
			super(Stats.STAT_DEX, 16, null);
		}
		
		@Override
		public void calc(Env env)
		{
			Player pc = (Player) env.character;
			if(pc != null)
			{
				env.value = Math.max(1.0, env.value + (double) pc.getHennaStatDEX());
			}
		}
	}
	
	private static class FuncHennaSTR extends Func
	{
		static final FuncHennaSTR func = new FuncHennaSTR();
		
		private FuncHennaSTR()
		{
			super(Stats.STAT_STR, 16, null);
		}
		
		@Override
		public void calc(Env env)
		{
			Player pc = (Player) env.character;
			if(pc != null)
			{
				env.value = Math.max(1.0, env.value + (double) pc.getHennaStatSTR());
			}
		}
	}
	
	private static class FuncMAtkSpeedMul extends Func
	{
		static final FuncMAtkSpeedMul func = new FuncMAtkSpeedMul();
		
		private FuncMAtkSpeedMul()
		{
			super(Stats.MAGIC_ATTACK_SPEED, 32, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.WIT.calcBonus(env.character);
		}
	}
	
	private static class FuncPAtkSpeedMul extends Func
	{
		static final FuncPAtkSpeedMul func = new FuncPAtkSpeedMul();
		
		private FuncPAtkSpeedMul()
		{
			super(Stats.POWER_ATTACK_SPEED, 32, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.DEX.calcBonus(env.character);
		}
	}
	
	private static class FuncMoveSpeedMul extends Func
	{
		static final FuncMoveSpeedMul func = new FuncMoveSpeedMul();
		
		private FuncMoveSpeedMul()
		{
			super(Stats.RUN_SPEED, 32, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.DEX.calcBonus(env.character);
		}
	}
	
	private static class FuncPCriticalRateMul extends Func
	{
		static final FuncPCriticalRateMul func = new FuncPCriticalRateMul();
		
		private FuncPCriticalRateMul()
		{
			super(Stats.CRITICAL_BASE, 16, null);
		}
		
		@Override
		public void calc(Env env)
		{
			if(!(env.character instanceof Summon))
			{
				env.value *= BaseStats.DEX.calcBonus(env.character);
			}
			env.value *= 0.01 * env.character.calcStat(Stats.CRITICAL_RATE, env.target, env.skill);
		}
	}
	
	private static class FuncMCriticalRateMul extends Func
	{
		static final FuncMCriticalRateMul func = new FuncMCriticalRateMul();
		
		private FuncMCriticalRateMul()
		{
			super(Stats.MCRITICAL_RATE, 16, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= 0.1 * BaseStats.WIT.calcBonus(env.character);
		}
	}
	
	private static class FuncEvasionAdd extends Func
	{
		static final FuncEvasionAdd func = new FuncEvasionAdd();
		
		private FuncEvasionAdd()
		{
			super(Stats.EVASION_RATE, 16, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value += Math.sqrt(env.character.getDEX()) * 6.0 + (double) env.character.getLevel();
			if(env.character.getLevel() > 77)
			{
				env.value += (double) (env.character.getLevel() - 77);
			}
			if(env.character.getLevel() > 69)
			{
				env.value += (double) (env.character.getLevel() - 69);
			}
		}
	}
	
	private static class FuncAccuracyAdd extends Func
	{
		static final FuncAccuracyAdd func = new FuncAccuracyAdd();
		
		private FuncAccuracyAdd()
		{
			super(Stats.ACCURACY_COMBAT, 16, null);
		}
		
		@Override
		public void calc(Env env)
		{
			if(env.character.isPet())
			{
				return;
			}
			env.value += Math.sqrt(env.character.getDEX()) * 6.0 + (double) env.character.getLevel();
			if(env.character.isSummon())
			{
				env.value += env.character.getLevel() < 60 ? 4.0 : 5.0;
			}
			if(env.character.getLevel() > 77)
			{
				env.value += (double) (env.character.getLevel() - 77);
			}
			if(env.character.getLevel() > 69)
			{
				env.value += (double) (env.character.getLevel() - 69);
			}
		}
	}
	
	private static class FuncAttackRange extends Func
	{
		static final FuncAttackRange func = new FuncAttackRange();
		
		private FuncAttackRange()
		{
			super(Stats.POWER_ATTACK_RANGE, 32, null);
		}
		
		@Override
		public void calc(Env env)
		{
			WeaponTemplate weapon = env.character.getActiveWeaponItem();
			if(weapon != null)
			{
				env.value = weapon.getAttackRange();
			}
		}
	}
	
	private static class FuncMDefMul extends Func
	{
		static final FuncMDefMul func = new FuncMDefMul();
		
		private FuncMDefMul()
		{
			super(Stats.MAGIC_DEFENCE, 32, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.MEN.calcBonus(env.character) * env.character.getLevelMod();
		}
	}
	
	private static class FuncPDefMul extends Func
	{
		static final FuncPDefMul func = new FuncPDefMul();
		
		private FuncPDefMul()
		{
			super(Stats.POWER_DEFENCE, 32, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= env.character.getLevelMod();
		}
	}
	
	private static class FuncMAtkMul extends Func
	{
		static final FuncMAtkMul func = new FuncMAtkMul();
		
		private FuncMAtkMul()
		{
			super(Stats.MAGIC_ATTACK, 32, null);
		}
		
		@Override
		public void calc(Env env)
		{
			double ib = BaseStats.INT.calcBonus(env.character);
			double lvlb = env.character.getLevelMod();
			env.value *= lvlb * lvlb * ib * ib;
		}
	}
	
	private static class FuncPAtkMul extends Func
	{
		static final FuncPAtkMul func = new FuncPAtkMul();
		
		private FuncPAtkMul()
		{
			super(Stats.POWER_ATTACK, 32, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.STR.calcBonus(env.character) * env.character.getLevelMod();
		}
	}
	
	private static class FuncMultRegenRunning extends Func
	{
		static final FuncMultRegenRunning[] func = new FuncMultRegenRunning[Stats.NUM_STATS];
		
		private FuncMultRegenRunning(Stats stat)
		{
			super(stat, 48, null);
			setCondition(new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RUNNING, true));
		}
		
		static Func getFunc(Stats stat)
		{
			int pos = stat.ordinal();
			if(func[pos] == null)
			{
				func[pos] = new FuncMultRegenRunning(stat);
			}
			return func[pos];
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= 0.7;
		}
	}
	
	private static class FuncMultRegenStanding extends Func
	{
		static final FuncMultRegenStanding[] func = new FuncMultRegenStanding[Stats.NUM_STATS];
		
		private FuncMultRegenStanding(Stats stat)
		{
			super(stat, 48, null);
			setCondition(new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.STANDING, true));
		}
		
		static Func getFunc(Stats stat)
		{
			int pos = stat.ordinal();
			if(func[pos] == null)
			{
				func[pos] = new FuncMultRegenStanding(stat);
			}
			return func[pos];
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= 1.1;
		}
	}
	
	private static class FuncMultRegenResting extends Func
	{
		static final FuncMultRegenResting[] func = new FuncMultRegenResting[Stats.NUM_STATS];
		
		private FuncMultRegenResting(Stats stat)
		{
			super(stat, 48, null);
			setCondition(new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RESTING, true));
		}
		
		static Func getFunc(Stats stat)
		{
			int pos = stat.ordinal();
			if(func[pos] == null)
			{
				func[pos] = new FuncMultRegenResting(stat);
			}
			return func[pos];
		}
		
		@Override
		public void calc(Env env)
		{
			env.value = env.character.isPlayer() && env.character.getLevel() <= 40 && ((Player) env.character).getClassId().getLevel() < 3 && stat == Stats.REGENERATE_HP_RATE ? (env.value *= 6.0) : (env.value *= 1.5);
		}
	}
}