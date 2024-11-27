package l2.gameserver.skills.effects;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Env;

public class EffectRelax extends Effect
{
	private boolean _isWereSitting;
	
	public EffectRelax(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean checkCondition()
	{
		Player player = _effected.getPlayer();
		if(player == null)
		{
			return false;
		}
		if(player.isMounted())
		{
			player.sendPacket(new SystemMessage(113).addSkillName(_skill.getId(), _skill.getLevel()));
			return false;
		}
		return super.checkCondition();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		Player player = _effected.getPlayer();
		if(player.isMoving())
		{
			player.stopMove();
		}
		_isWereSitting = player.isSitting();
		player.sitDown(null);
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		Skill skill = getSkill();
		if(skill != null)
		{
			for(Effect other : _effected.getEffectList().getEffectsBySkill(skill))
			{
				if(other == this)
					continue;
				other.exit();
			}
		}
		if(!_isWereSitting)
		{
			_effected.getPlayer().standUp();
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		Player player = _effected.getPlayer();
		if(player.isAlikeDead() || player == null)
		{
			return false;
		}
		if(!player.isSitting())
		{
			return false;
		}
		if(player.isCurrentHpFull() && getSkill().isToggle())
		{
			getEffected().sendPacket(Msg.HP_WAS_FULLY_RECOVERED_AND_SKILL_WAS_REMOVED);
			return false;
		}
		double manaDam = calc();
		if(manaDam > _effected.getCurrentMp() && getSkill().isToggle())
		{
			player.sendPacket(Msg.NOT_ENOUGH_MP, new SystemMessage(749).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
			return false;
		}
		_effected.reduceCurrentMp(manaDam, null);
		return true;
	}
}