package l2.gameserver.skills.effects;

import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.data.xml.holder.CubicHolder;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.MagicSkillLaunched;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.stats.Env;
import l2.gameserver.templates.CubicTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class EffectCubic extends Effect
{
	private final CubicTemplate _template;
	private Future<?> _task;
	private long _reuse;
	
	public EffectCubic(Env env, EffectTemplate template)
	{
		super(env, template);
		_template = CubicHolder.getInstance().getTemplate(getTemplate().getParam().getInteger("cubicId"), getTemplate().getParam().getInteger("cubicLevel"));
	}
	
	private static boolean doHeal(Player player, CubicTemplate.SkillInfo info)
	{
		Skill skill = info.getSkill();
		Player target = null;
		if(player.getParty() == null)
		{
			if(!player.isCurrentHpFull() && !player.isDead())
			{
				target = player;
			}
		}
		else
		{
			double currentHp = 2.147483647E9;
			for(Player member : player.getParty().getPartyMembers())
			{
				if(member == null || !player.isInRange(member, (long) info.getSkill().getCastRange()) || member.isCurrentHpFull() || member.isDead() || member.getCurrentHp() >= currentHp)
					continue;
				currentHp = member.getCurrentHp();
				target = member;
			}
		}
		if(target == null)
		{
			return false;
		}
		int chance = info.getChance((int) target.getCurrentHpPercents());
		if(!Rnd.chance(chance))
		{
			return false;
		}
		Player aimTarget = target;
		player.broadcastPacket(new MagicSkillUse(player, aimTarget, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0));
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				ArrayList<Creature> targets = new ArrayList<>(1);
				targets.add(aimTarget);
				player.broadcastPacket(new MagicSkillLaunched(player, skill.getDisplayId(), skill.getDisplayLevel(), targets));
				player.callSkill(skill, targets, false);
			}
		}, skill.getHitTime());
		return true;
	}
	
	private static boolean doAttack(Player player, CubicTemplate.SkillInfo info)
	{
		if(!Rnd.chance(info.getChance()))
		{
			return false;
		}
		Creature target = getTarget(player, info);
		if(target == null)
		{
			return false;
		}
		Creature aimTarget = target;
		Skill skill = info.getSkill();
		player.broadcastPacket(new MagicSkillUse(player, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0));
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				ArrayList<Creature> targets = new ArrayList<>(1);
				targets.add(aimTarget);
				player.broadcastPacket(new MagicSkillLaunched(player, skill.getDisplayId(), skill.getDisplayLevel(), targets));
				player.callSkill(skill, targets, false);
				if(aimTarget.isNpc())
				{
					if(aimTarget.paralizeOnAttack(player))
					{
						if(Config.PARALIZE_ON_RAID_DIFF)
						{
							player.paralizeMe(aimTarget);
						}
					}
					else
					{
						int damage = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : (int) skill.getPower();
						aimTarget.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player, damage);
					}
				}
			}
		}, skill.getHitTime());
		return true;
	}
	
	private static boolean doDebuff(Player player, CubicTemplate.SkillInfo info)
	{
		if(!Rnd.chance(info.getChance()))
		{
			return false;
		}
		Creature target = getTarget(player, info);
		if(target == null)
		{
			return false;
		}
		Creature aimTarget = target;
		Skill skill = info.getSkill();
		player.broadcastPacket(new MagicSkillUse(player, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0));
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				ArrayList<Creature> targets = new ArrayList<>(1);
				targets.add(aimTarget);
				player.broadcastPacket(new MagicSkillLaunched(player, skill.getDisplayId(), skill.getDisplayLevel(), targets));
				player.callSkill(skill, targets, false);
			}
		}, skill.getHitTime());
		return true;
	}
	
	private static boolean doCancel(Player player, CubicTemplate.SkillInfo info)
	{
		if(!Rnd.chance(info.getChance()))
		{
			return false;
		}
		boolean hasDebuff = false;
		for(Effect e : player.getEffectList().getAllEffects())
		{
			if(!e.isOffensive() || !e.isCancelable() || e.getTemplate()._applyOnCaster)
				continue;
			hasDebuff = true;
			break;
		}
		if(!hasDebuff)
		{
			return false;
		}
		Skill skill = info.getSkill();
		player.broadcastPacket(new MagicSkillUse(player, player, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0));
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				ArrayList<Creature> targets = new ArrayList<>(1);
				targets.add(player);
				player.broadcastPacket(new MagicSkillLaunched(player, skill.getDisplayId(), skill.getDisplayLevel(), targets));
				player.callSkill(skill, targets, false);
			}
		}, skill.getHitTime());
		return true;
	}
	
	private static final Creature getTarget(Player owner, CubicTemplate.SkillInfo info)
	{
		if(!owner.isInCombat())
		{
			return null;
		}
		GameObject object = owner.getTarget();
		if(object == null || !object.isCreature())
		{
			return null;
		}
		Creature target = (Creature) object;
		if(target.isDead())
		{
			return null;
		}
		if(target.getCurrentHp() < (double) info.getMinHp() && target.getCurrentHpPercents() < (double) info.getMinHpPercent())
		{
			return null;
		}
		if(target.isDoor() && !info.isCanAttackDoor())
		{
			return null;
		}
		if(!owner.isInRangeZ(target, (long) info.getSkill().getCastRange()))
		{
			return null;
		}
		Player targetPlayer = target.getPlayer();
		if(targetPlayer != null && !targetPlayer.isInCombat())
		{
			return null;
		}
		if(!target.isAutoAttackable(owner))
		{
			return null;
		}
		return target;
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		Player player = _effected.getPlayer();
		if(player == null)
		{
			return;
		}
		player.addCubic(this);
		_task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ActionTask(), 1000, 1000);
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		Player player = _effected.getPlayer();
		if(player == null)
		{
			return;
		}
		player.removeCubic(getId());
		_task.cancel(true);
		_task = null;
	}
	
	public void doAction(Player player)
	{
		if(_reuse > System.currentTimeMillis())
		{
			return;
		}
		boolean result = false;
		int chance = Rnd.get(100);
		for(Map.Entry<Integer, List<CubicTemplate.SkillInfo>> entry : _template.getSkills())
		{
			if((chance -= entry.getKey().intValue()) >= 0)
				continue;
			for(CubicTemplate.SkillInfo skillInfo : entry.getValue())
			{
				switch(skillInfo.getActionType())
				{
					case ATTACK:
					{
						result = doAttack(player, skillInfo);
						break;
					}
					case DEBUFF:
					{
						result = doDebuff(player, skillInfo);
						break;
					}
					case HEAL:
					{
						result = doHeal(player, skillInfo);
						break;
					}
					case CANCEL:
					{
						result = doCancel(player, skillInfo);
					}
				}
			}
		}
		if(result)
		{
			_reuse = System.currentTimeMillis() + (long) _template.getDelay() * 1000;
		}
	}
	
	@Override
	protected boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public boolean isHidden()
	{
		return true;
	}
	
	@Override
	public boolean isCancelable()
	{
		return false;
	}
	
	public int getId()
	{
		return _template.getId();
	}
	
	private class ActionTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(!isActive())
			{
				return;
			}
			Player player;
			Player player2 = player = _effected != null && _effected.isPlayer() ? (Player) _effected : null;
			if(player == null)
			{
				return;
			}
			doAction(player);
		}
	}
}