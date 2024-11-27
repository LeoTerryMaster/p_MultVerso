package npc.model;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.templates.npc.NpcTemplate;

public class QueenAntLarvaInstance extends MonsterInstance
{
	public QueenAntLarvaInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		damage = getCurrentHp() - damage > 1.0 ? damage : getCurrentHp() - 1.0;
		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}
	
	@Override
	public boolean canChampion()
	{
		return false;
	}
	
	@Override
	public boolean isImmobilized()
	{
		return true;
	}
}