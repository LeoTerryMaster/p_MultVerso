package ai;

import bosses.ValakasManager;
import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Zone;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.tables.SkillTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Valakas extends DefaultAI
{
	final Skill s_lava_skin;
	final Skill s_fear;
	final Skill s_regen1;
	final Skill s_regen2;
	final Skill s_regen3;
	final Skill s_regen4;
	final Skill s_tremple_left;
	final Skill s_tremple_right;
	final Skill s_tail_stomp_a;
	final Skill s_tail_lash;
	final Skill s_meteor;
	final Skill s_breath_low;
	final Skill s_breath_high;
	private final long defenceDownReuse = 120000;
	private final List<NpcInstance> minions;
	private long defenceDownTimer;
	private double _rangedAttacksIndex;
	private int _hpStage;
	
	public Valakas(NpcInstance actor)
	{
		super(actor);
		s_lava_skin = getSkill(4680, 1);
		s_fear = getSkill(4689, 1);
		s_regen1 = getSkill(4691, 1);
		s_regen2 = getSkill(4691, 2);
		s_regen3 = getSkill(4691, 3);
		s_regen4 = getSkill(4691, 4);
		s_tremple_left = getSkill(4681, 1);
		s_tremple_right = getSkill(4682, 1);
		s_tail_stomp_a = getSkill(4685, 1);
		s_tail_lash = getSkill(4688, 1);
		s_meteor = getSkill(4690, 1);
		s_breath_low = getSkill(4683, 1);
		s_breath_high = getSkill(4684, 1);
		defenceDownTimer = Long.MAX_VALUE;
		_hpStage = 0;
		minions = new ArrayList<>();
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		ValakasManager.setLastAttackTime();
		for(Playable p : ValakasManager.getZone().getInsidePlayables())
		{
			notifyEvent(CtrlEvent.EVT_AGGRESSION, p, 1);
		}
		if(damage > 100 && attacker.getDistance(actor) > 400.0)
		{
			_rangedAttacksIndex += (double) damage / 1000.0;
		}
		super.onEvtAttacked(attacker, damage);
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
		else if(chp < 80.0 && _hpStage == 1)
		{
			actor.altOnMagicUseTimer(actor, s_regen2);
			defenceDownTimer = System.currentTimeMillis();
			_hpStage = 2;
		}
		else if(chp < 50.0 && _hpStage == 2)
		{
			actor.altOnMagicUseTimer(actor, s_regen3);
			_hpStage = 3;
		}
		else if(chp < 30.0 && _hpStage == 3)
		{
			actor.altOnMagicUseTimer(actor, s_regen4);
			_hpStage = 4;
		}
		if(_rangedAttacksIndex > 2000.0)
		{
			if(Rnd.chance(60))
			{
				Creature randomHated = actor.getAggroList().getRandomHated();
				if(randomHated != null && Math.abs(actor.getZ() - randomHated.getZ()) < 400)
				{
					setAttackTarget(randomHated);
					if(_madnessTask == null && !actor.isConfused())
					{
						actor.startConfused();
						_madnessTask = ThreadPoolManager.getInstance().schedule(new MadnessTask(), 20000);
					}
				}
				ValakasManager.broadcastCustomScreenMessage("ValakasRangedAttack");
				_rangedAttacksIndex = 0.0;
			}
		}
		else if(defenceDownTimer < System.currentTimeMillis())
		{
			ValakasManager.broadcastCustomScreenMessage("ValakasAttaksKeepIt");
			defenceDownTimer = System.currentTimeMillis() + 120000 + (long) Rnd.get(60) * 1000;
			return chooseTaskAndTargets(s_fear, target, distance);
		}
		if(Rnd.chance(50))
		{
			return chooseTaskAndTargets(Rnd.chance(50) ? s_tremple_left : s_tremple_right, target, distance);
		}
		HashMap d_skill = new HashMap();
		switch(_hpStage)
		{
			case 1:
			{
				addDesiredSkill(d_skill, target, distance, s_breath_low);
				addDesiredSkill(d_skill, target, distance, s_tail_stomp_a);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear);
				break;
			}
			case 2:
			case 3:
			{
				addDesiredSkill(d_skill, target, distance, s_breath_low);
				addDesiredSkill(d_skill, target, distance, s_tail_stomp_a);
				addDesiredSkill(d_skill, target, distance, s_breath_high);
				addDesiredSkill(d_skill, target, distance, s_tail_lash);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear);
				break;
			}
			case 4:
			{
				addDesiredSkill(d_skill, target, distance, s_breath_low);
				addDesiredSkill(d_skill, target, distance, s_tail_stomp_a);
				addDesiredSkill(d_skill, target, distance, s_breath_high);
				addDesiredSkill(d_skill, target, distance, s_tail_lash);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear);
			}
		}
		Skill r_skill = selectTopSkill(d_skill);
		if(r_skill != null && !r_skill.isOffensive())
		{
			target = actor;
		}
		return chooseTaskAndTargets(r_skill, target, distance);
	}
	
	@Override
	protected void thinkAttack()
	{
		NpcInstance actor = getActor();
		if(actor.isInZone(Zone.ZoneType.poison) && actor.getEffectList() != null && actor.getEffectList().getEffectsBySkill(s_lava_skin) == null)
		{
			actor.altOnMagicUseTimer(actor, s_lava_skin);
		}
		super.thinkAttack();
	}
	
	private Skill getSkill(int id, int level)
	{
		return SkillTable.getInstance().getInfo(id, level);
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