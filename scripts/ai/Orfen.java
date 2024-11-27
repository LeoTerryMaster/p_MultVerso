package ai;

import l2.commons.text.PrintfFormat;
import l2.commons.util.Rnd;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.Location;
import npc.model.OrfenInstance;

public class Orfen extends Fighter
{
	public static final PrintfFormat[] MsgOnRecall = {new PrintfFormat("%s. Stop kidding yourself about your own powerlessness!"), new PrintfFormat("%s. I'll make you feel what true fear is!"), new PrintfFormat("You're really stupid to have challenged me. %s! Get ready!"), new PrintfFormat("%s. Do you think that's going to work?!")};
	public final Skill[] _paralyze;
	
	public Orfen(NpcInstance actor)
	{
		super(actor);
		_paralyze = getActor().getTemplate().getDebuffSkills();
	}
	
	@Override
	protected boolean thinkActive()
	{
		if(super.thinkActive())
		{
			return true;
		}
		OrfenInstance actor = getActor();
		if(actor.isTeleported() && actor.getCurrentHpPercents() > 95.0)
		{
			actor.setTeleported(false);
			return true;
		}
		return false;
	}
	
	@Override
	protected boolean createNewTask()
	{
		return defaultNewTask();
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		super.onEvtAttacked(attacker, damage);
		OrfenInstance actor = getActor();
		if(actor.isCastingNow())
		{
			return;
		}
		double distance = actor.getDistance(attacker);
		Skill r_skill;
		if(distance > 300.0 && distance < 1000.0 && _damSkills.length > 0 && Rnd.chance(10))
		{
			Functions.npcSay(actor, MsgOnRecall[Rnd.get(MsgOnRecall.length - 1)].sprintf(attacker.getName()));
			teleToLocation(attacker, Location.findFrontPosition(actor, attacker, 0, 50));
			Skill r_skill2 = _damSkills[Rnd.get(_damSkills.length)];
			if(canUseSkill(r_skill2, attacker, -1.0))
			{
				addTaskAttack(attacker, r_skill2, 1000000);
			}
		}
		else if(_paralyze.length > 0 && Rnd.chance(20) && canUseSkill(r_skill = _paralyze[Rnd.get(_paralyze.length)], attacker, -1.0))
		{
			addTaskAttack(attacker, r_skill, 1000000);
		}
	}
	
	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		super.onEvtSeeSpell(skill, caster);
		OrfenInstance actor = getActor();
		if(actor.isCastingNow())
		{
			return;
		}
		double distance = actor.getDistance(caster);
		if(_damSkills.length > 0 && skill.getEffectPoint() > 0 && distance < 1000.0 && Rnd.chance(20))
		{
			Functions.npcSay(actor, MsgOnRecall[Rnd.get(MsgOnRecall.length)].sprintf(caster.getName()));
			teleToLocation(caster, Location.findFrontPosition(actor, caster, 0, 50));
			Skill r_skill = _damSkills[Rnd.get(_damSkills.length)];
			if(canUseSkill(r_skill, caster, -1.0))
			{
				addTaskAttack(caster, r_skill, 1000000);
			}
		}
	}
	
	@Override
	public OrfenInstance getActor()
	{
		return (OrfenInstance) super.getActor();
	}
	
	private void teleToLocation(Creature attacker, Location loc)
	{
		attacker.teleToLocation(loc);
	}
}