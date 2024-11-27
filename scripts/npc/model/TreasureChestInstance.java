package npc.model;

import l2.commons.util.Rnd;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.ChestInstance;
import l2.gameserver.network.l2.s2c.PlaySound;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.npc.NpcTemplate;

public class TreasureChestInstance extends ChestInstance
{
	private static final int TREASURE_BOMB_ID = 4143;
	
	public TreasureChestInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void tryOpen(Player opener, Skill skill)
	{
		if(isCommonTreasureChest())
		{
			getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, opener, 100);
			return;
		}
		double chance = calcChance(opener, skill);
		if(Rnd.chance(chance))
		{
			getAggroList().addDamageHate(opener, 10000, 0);
			doDie(opener);
		}
		else
		{
			fakeOpen(opener);
		}
		opener.sendPacket(new PlaySound("ItemSound2.broken_key"));
	}
	
	public double calcChance(Player opener, Skill skill)
	{
		int skill_name_id = skill.getId();
		int skill_name_level = skill.getLevel();
		int chance = 1;
		if(skill_name_id == 27)
		{
			int success_rate = 1;
			if(skill_name_level == 1)
			{
				success_rate = 98;
			}
			else if(skill_name_level == 2)
			{
				success_rate = 84;
			}
			else if(skill_name_level == 3)
			{
				success_rate = 99;
			}
			else if(skill_name_level == 4)
			{
				success_rate = 84;
			}
			else if(skill_name_level == 5)
			{
				success_rate = 88;
			}
			else if(skill_name_level == 6)
			{
				success_rate = 90;
			}
			else if(skill_name_level == 7)
			{
				success_rate = 89;
			}
			else if(skill_name_level == 8)
			{
				success_rate = 88;
			}
			else if(skill_name_level == 9)
			{
				success_rate = 86;
			}
			else if(skill_name_level == 10)
			{
				success_rate = 90;
			}
			else if(skill_name_level == 11)
			{
				success_rate = 87;
			}
			else if(skill_name_level == 12)
			{
				success_rate = 89;
			}
			else if(skill_name_level == 13)
			{
				success_rate = 89;
			}
			else if(skill_name_level == 14)
			{
				success_rate = 89;
			}
			else if(skill_name_level == 15)
			{
				success_rate = 89;
			}
			chance = success_rate - (getLevel() - skill_name_level * 4 - 16) * 6;
			if(chance > success_rate)
			{
				chance = success_rate;
			}
		}
		else if(skill_name_id == 2065)
		{
			chance = (int) (60.0 - (double) (getLevel() - (skill_name_level - 1) * 10) * 1.5);
			if(chance > 60)
			{
				chance = 60;
			}
		}
		else if(skill_name_id == 2229)
		{
			if(skill_name_level == 1)
			{
				int level_mod = getLevel() - 19;
				chance = level_mod <= 0 ? 100 : (int) ((2.0E-4 * (double) (level_mod * level_mod) - 0.0264 * (double) level_mod + 0.7695) * 100.0);
			}
			else if(skill_name_level == 2)
			{
				int level_mod = getLevel() - 29;
				chance = level_mod <= 0 ? 100 : (int) ((3.0E-4 * (double) level_mod * (double) level_mod - 0.0279 * (double) level_mod + 0.7568) * 100.0);
			}
			else if(skill_name_level == 3)
			{
				int level_mod = getLevel() - 39;
				chance = level_mod <= 0 ? 100 : (int) ((3.0E-4 * (double) level_mod * (double) level_mod - 0.0269 * (double) level_mod + 0.7334) * 100.0);
			}
			else if(skill_name_level == 4)
			{
				int level_mod = getLevel() - 49;
				chance = level_mod <= 0 ? 100 : (int) ((3.0E-4 * (double) level_mod * (double) level_mod - 0.0284 * (double) level_mod + 0.8034) * 100.0);
			}
			else if(skill_name_level == 5)
			{
				int level_mod = getLevel() - 59;
				chance = level_mod <= 0 ? 100 : (int) ((5.0E-4 * (double) level_mod * (double) level_mod - 0.0356 * (double) level_mod + 0.9065) * 100.0);
			}
			else if(skill_name_level == 6)
			{
				int level_mod = getLevel() - 69;
				chance = level_mod <= 0 ? 100 : (int) ((9.0E-4 * (double) level_mod * (double) level_mod - 0.0373 * (double) level_mod + 0.8572) * 100.0);
			}
			else if(skill_name_level == 7)
			{
				int level_mod = getLevel() - 79;
				chance = level_mod <= 0 ? 100 : (int) ((0.0043 * (double) level_mod * (double) level_mod - 0.0671 * (double) level_mod + 0.9593) * 100.0);
			}
			else if(skill_name_level == 8)
			{
				chance = 100;
			}
		}
		return chance;
	}
	
	private void fakeOpen(Creature opener)
	{
		Skill bomb = SkillTable.getInstance().getInfo(4143, getBombLvl());
		if(bomb != null)
		{
			doCast(bomb, opener, false);
		}
		onDecay();
	}
	
	private int getBombLvl()
	{
		int npcLvl = getLevel();
		int lvl = 1;
		if(npcLvl >= 78)
		{
			lvl = 10;
		}
		else if(npcLvl >= 72)
		{
			lvl = 9;
		}
		else if(npcLvl >= 66)
		{
			lvl = 8;
		}
		else if(npcLvl >= 60)
		{
			lvl = 7;
		}
		else if(npcLvl >= 54)
		{
			lvl = 6;
		}
		else if(npcLvl >= 48)
		{
			lvl = 5;
		}
		else if(npcLvl >= 42)
		{
			lvl = 4;
		}
		else if(npcLvl >= 36)
		{
			lvl = 3;
		}
		else if(npcLvl >= 30)
		{
			lvl = 2;
		}
		return lvl;
	}
	
	private boolean isCommonTreasureChest()
	{
		int npcId = getNpcId();
		return npcId >= 21801 && npcId <= 21822;
	}
	
	@Override
	public void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(!isCommonTreasureChest())
		{
			fakeOpen(attacker);
		}
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}
}