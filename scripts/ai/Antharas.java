package ai;

import bosses.AntharasManager;
import l2.commons.util.Rnd;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Antharas extends DefaultAI
{
	private static long _minionsSpawnDelay;
	final Skill s_fear;
	final Skill s_fear2;
	final Skill s_curse;
	final Skill s_paralyze;
	final Skill s_shock;
	final Skill s_shock2;
	final Skill s_antharas_ordinary_attack;
	final Skill s_antharas_ordinary_attack2;
	final Skill s_meteor;
	final Skill s_breath;
	final Skill s_regen1;
	final Skill s_regen2;
	final Skill s_regen3;
	private final List<NpcInstance> minions;
	private int _hpStage;
	
	public Antharas(NpcInstance actor)
	{
		super(actor);
		s_fear = getSkill(4108, 1);
		s_fear2 = getSkill(5092, 1);
		s_curse = getSkill(4109, 1);
		s_paralyze = getSkill(4111, 1);
		s_shock = getSkill(4106, 1);
		s_shock2 = getSkill(4107, 1);
		s_antharas_ordinary_attack = getSkill(4112, 1);
		s_antharas_ordinary_attack2 = getSkill(4113, 1);
		s_meteor = getSkill(5093, 1);
		s_breath = getSkill(4110, 1);
		s_regen1 = getSkill(4239, 1);
		s_regen2 = getSkill(4240, 1);
		s_regen3 = getSkill(4241, 1);
		_hpStage = 0;
		minions = new ArrayList<>();
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		AntharasManager.setLastAttackTime();
		for(Playable p : AntharasManager.getZone().getInsidePlayables())
		{
			notifyEvent(CtrlEvent.EVT_AGGRESSION, p, 1);
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		_minionsSpawnDelay = System.currentTimeMillis() + 120000;
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
	
	@Override
	protected boolean createNewTask()
	{
		clearTasks();
		Creature target = prepareTarget();
		if(target == null)
		{
			return false;
		}
		NpcInstance actor = getActor();
		if(actor.isDead())
		{
			return false;
		}
		double distance = actor.getDistance(target);
		double chp = actor.getCurrentHpPercents();
		if(_hpStage == 0)
		{
			actor.altOnMagicUseTimer(actor, s_regen1);
			_hpStage = 1;
		}
		else if(chp < 75.0 && _hpStage == 1)
		{
			actor.altOnMagicUseTimer(actor, s_regen2);
			_hpStage = 2;
		}
		else if(chp < 50.0 && _hpStage == 2)
		{
			actor.altOnMagicUseTimer(actor, s_regen3);
			_hpStage = 3;
		}
		else if(chp < 30.0 && _hpStage == 3)
		{
			actor.altOnMagicUseTimer(actor, s_regen3);
			_hpStage = 4;
		}
		if(_minionsSpawnDelay < System.currentTimeMillis() && getAliveMinionsCount() < 30 && Rnd.chance(5))
		{
			NpcInstance minion = Functions.spawn(Location.findPointToStay(actor.getLoc(), 400, 700, actor.getGeoIndex()), Rnd.chance(50) ? 29070 : 29069);
			minions.add(minion);
			AntharasManager.addSpawnedMinion(minion);
		}
		if(Rnd.chance(50))
		{
			return chooseTaskAndTargets(Rnd.chance(50) ? s_antharas_ordinary_attack : s_antharas_ordinary_attack2, target, distance);
		}
		HashMap d_skill = new HashMap();
		switch(_hpStage)
		{
			case 1:
			{
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				break;
			}
			case 2:
			{
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear2);
				break;
			}
			case 3:
			{
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear2);
				addDesiredSkill(d_skill, target, distance, s_shock2);
				addDesiredSkill(d_skill, target, distance, s_breath);
				break;
			}
			case 4:
			{
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear2);
				addDesiredSkill(d_skill, target, distance, s_shock2);
				addDesiredSkill(d_skill, target, distance, s_fear);
				addDesiredSkill(d_skill, target, distance, s_shock);
				addDesiredSkill(d_skill, target, distance, s_breath);
				break;
			}
		}
		Skill r_skill = selectTopSkill(d_skill);
		if(r_skill != null && !r_skill.isOffensive())
		{
			target = actor;
		}
		return chooseTaskAndTargets(r_skill, target, distance);
	}
	
	private int getAliveMinionsCount()
	{
		int i = 0;
		for(NpcInstance n : minions)
		{
			if(n == null || n.isDead())
				continue;
			++i;
		}
		return i;
	}
	
	private Skill getSkill(int id, int level)
	{
		return SkillTable.getInstance().getInfo(id, level);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		if(minions != null && !minions.isEmpty())
		{
			for(NpcInstance n : minions)
			{
				n.deleteMe();
			}
		}
		super.onEvtDead(killer);
	}
}