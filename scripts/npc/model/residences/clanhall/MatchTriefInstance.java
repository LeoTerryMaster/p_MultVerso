package npc.model.residences.clanhall;

import ai.residences.clanhall.MatchTrief;
import l2.commons.util.Rnd;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.residences.clanhall.CTBBossInstance;
import l2.gameserver.templates.npc.NpcTemplate;

public class MatchTriefInstance extends CTBBossInstance
{
	private long _massiveDamage;
	
	public MatchTriefInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(_massiveDamage > System.currentTimeMillis())
		{
			damage = 10000.0;
			if(Rnd.chance(10))
			{
				((MatchTrief) getAI()).hold();
			}
		}
		else if(getCurrentHpPercents() > 50.0)
		{
			damage = attacker.isPlayer() ? damage / (double) getMaxHp() / 0.05 * 100.0 : damage / (double) getMaxHp() / 0.05 * 10.0;
		}
		else if(getCurrentHpPercents() > 30.0)
		{
			if(Rnd.chance(90))
			{
				damage = attacker.isPlayer() ? damage / (double) getMaxHp() / 0.05 * 100.0 : damage / (double) getMaxHp() / 0.05 * 10.0;
			}
			else
			{
				_massiveDamage = System.currentTimeMillis() + 5000;
			}
		}
		else
		{
			_massiveDamage = System.currentTimeMillis() + 5000;
		}
		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}
}