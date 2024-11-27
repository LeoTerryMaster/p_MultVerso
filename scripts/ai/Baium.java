package ai;

import bosses.BaiumManager;
import gnu.trove.TIntObjectHashMap;
import l2.commons.util.Rnd;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;

import java.util.HashMap;

public class Baium extends DefaultAI
{
	private final Skill baium_normal_attack;
	private final Skill energy_wave;
	private final Skill earth_quake;
	private final Skill thunderbolt;
	private final Skill group_hold;
	private boolean _firstTimeAttacked = true;
	
	public Baium(NpcInstance actor)
	{
		super(actor);
		TIntObjectHashMap skills = getActor().getTemplate().getSkills();
		baium_normal_attack = (Skill) skills.get(4127);
		energy_wave = (Skill) skills.get(4128);
		earth_quake = (Skill) skills.get(4129);
		thunderbolt = (Skill) skills.get(4130);
		group_hold = (Skill) skills.get(4131);
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		BaiumManager.setLastAttackTime();
		if(_firstTimeAttacked)
		{
			_firstTimeAttacked = false;
			NpcInstance actor = getActor();
			if(attacker == null)
			{
				return;
			}
			if(attacker.isPlayer() && attacker.getPet() != null)
			{
				attacker.getPet().doDie(actor);
			}
			else if((attacker.isSummon() || attacker.isPet()) && attacker.getPlayer() != null)
			{
				attacker.getPlayer().doDie(actor);
			}
			attacker.doDie(actor);
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected boolean createNewTask()
	{
		NpcInstance actor = getActor();
		if(actor == null)
		{
			return true;
		}
		if(!BaiumManager.getZone().checkIfInZone(actor))
		{
			teleportHome();
			return false;
		}
		clearTasks();
		Creature target = prepareTarget();
		if(target == null)
		{
			return false;
		}
		if(!BaiumManager.getZone().checkIfInZone(target))
		{
			actor.getAggroList().remove(target, false);
			return false;
		}
		int s_energy_wave = 20;
		int s_earth_quake = 20;
		int s_group_hold = actor.getCurrentHpPercents() > 50.0 ? 0 : 20;
		int s_thunderbolt = actor.getCurrentHpPercents() > 25.0 ? 0 : 20;
		Skill r_skill = null;
		if(actor.isMovementDisabled())
		{
			r_skill = thunderbolt;
		}
		else if(!Rnd.chance(100 - s_thunderbolt - s_group_hold - s_energy_wave - s_earth_quake))
		{
			HashMap d_skill = new HashMap();
			double distance = actor.getDistance(target);
			addDesiredSkill(d_skill, target, distance, energy_wave);
			addDesiredSkill(d_skill, target, distance, earth_quake);
			if(s_group_hold > 0)
			{
				addDesiredSkill(d_skill, target, distance, group_hold);
			}
			if(s_thunderbolt > 0)
			{
				addDesiredSkill(d_skill, target, distance, thunderbolt);
			}
			r_skill = selectTopSkill(d_skill);
		}
		if(r_skill == null)
		{
			r_skill = baium_normal_attack;
		}
		else if(r_skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF)
		{
			target = actor;
		}
		addTaskCast(target, r_skill);
		return true;
	}
	
	@Override
	protected boolean maybeMoveToHome()
	{
		NpcInstance actor = getActor();
		if(actor != null && !BaiumManager.getZone().checkIfInZone(actor))
		{
			teleportHome();
		}
		return false;
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		_firstTimeAttacked = true;
		super.onEvtDead(killer);
	}
}