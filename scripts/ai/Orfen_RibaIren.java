package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;

public class Orfen_RibaIren extends Fighter
{
	private static final int Orfen_id = 29014;
	
	public Orfen_RibaIren(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean createNewTask()
	{
		return defaultNewTask();
	}
	
	@Override
	protected void onEvtClanAttacked(Creature attacked_member, Creature attacker, int damage)
	{
		super.onEvtClanAttacked(attacked_member, attacker, damage);
		NpcInstance actor = getActor();
		if(_healSkills.length == 0)
		{
			return;
		}
		if(attacked_member.isDead() || actor.isDead() || attacked_member.getCurrentHpPercents() > 50.0)
		{
			return;
		}
		int heal_chance;
		if(attacked_member.getNpcId() == actor.getNpcId())
		{
			heal_chance = attacked_member.getObjectId() == actor.getObjectId() ? 100 : 0;
		}
		else
		{
			heal_chance = attacked_member.getNpcId() == 29014 ? 90 : 10;
		}
		if(Rnd.chance(heal_chance) && canUseSkill(_healSkills[0], attacked_member, -1.0))
		{
			addTaskAttack(attacked_member, _healSkills[0], 1000000);
		}
	}
}