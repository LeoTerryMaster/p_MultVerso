package l2.gameserver.skills.effects;

import l2.commons.lang.reference.HardReference;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.Stats;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class EffectDispelEffects extends Effect
{
	private final String _dispelType;
	private final int _cancelRate;
	private final String[] _stackTypes;
	private final int _negateCount;
	private final int _reApplyDelay;
	
	public EffectDispelEffects(Env env, EffectTemplate template)
	{
		super(env, template);
		_dispelType = template.getParam().getString("dispelType", "");
		_cancelRate = template.getParam().getInteger("cancelRate", 0);
		_negateCount = template.getParam().getInteger("negateCount", 5);
		_stackTypes = template.getParam().getString("negateStackTypes", "").split(";");
		_reApplyDelay = template.getParam().getInteger("reApplyDelay", 0);
	}
	
	@Override
	public void onStart()
	{
		ArrayList<Effect> musicList = new ArrayList<>();
		ArrayList<Effect> buffList = new ArrayList<>();
		for(Effect e : _effected.getEffectList().getAllEffects())
		{
			if(_dispelType.equals("cancellation"))
			{
				if(e.isOffensive() || e.getSkill().isToggle() || !e.isCancelable())
					continue;
				if(e.getSkill().isMusic())
				{
					musicList.add(e);
					continue;
				}
				buffList.add(e);
				continue;
			}
			if(_dispelType.equals("bane"))
			{
				if(!e.isCancelable() || !ArrayUtils.contains(_stackTypes, e.getStackType()) && !ArrayUtils.contains(_stackTypes, e.getStackType2()))
					continue;
				buffList.add(e);
				continue;
			}
			if(!_dispelType.equals("cleanse") || !e.isOffensive() || !e.isCancelable())
				continue;
			buffList.add(e);
		}
		List _effectList = new ArrayList<Effect>();
		_effectList.addAll(musicList);
		_effectList.addAll(buffList);
		if(_effectList.isEmpty())
		{
			return;
		}
		double cancel_res_multiplier = _effected.calcStat(Stats.CANCEL_RESIST, 0.0, null, null);
		Collections.shuffle(_effectList);
		_effectList = _effectList.subList(0, Math.min(_negateCount, _effectList.size()));
		HashSet<Skill> _stopSkills = new HashSet<>();
		Iterator iterator = _effectList.iterator();
		while(iterator.hasNext())
		{
			Effect e = (Effect) iterator.next();
			double eml = (double) e.getSkill().getMagicLevel();
			double dml = (double) getSkill().getMagicLevel() - (eml == 0.0 ? (double) _effected.getLevel() : eml);
			int buffTime = e.getTimeLeft();
			cancel_res_multiplier = 1.0 - cancel_res_multiplier * 0.01;
			double prelimChance = (2.0 * dml + (double) _cancelRate + (double) (buffTime / 120)) * cancel_res_multiplier;
			if(!Rnd.chance(calcSkillChanceLimits(prelimChance, _effector.isPlayable())))
				continue;
			_stopSkills.add(e.getSkill());
		}
		for(Skill stopSkill : _stopSkills)
		{
			_effected.getEffectList().stopEffect(stopSkill);
			_effected.sendPacket(new SystemMessage2(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(stopSkill));
		}
		if(_effected.isPlayer() && _reApplyDelay > 0)
		{
			HardReference<Player> reApplyRef = _effected.getPlayer().getRef();
			LinkedList<Skill> reApplySkills = new LinkedList<>();
			for(Skill stopSkill : _stopSkills)
			{
				if(stopSkill.isOffensive() || stopSkill.isToggle() || stopSkill.isTrigger())
					continue;
				reApplySkills.add(stopSkill);
			}
			ThreadPoolManager.getInstance().schedule(new RunnableImpl()
			{
				
				@Override
				public void runImpl() throws Exception
				{
					Player player = reApplyRef.get();
					if(player == null || player.isLogoutStarted() || player.isOutOfControl() || player.isDead() || player.isInDuel() || player.isAlikeDead() || player.isOlyParticipant() || player.isFlying() || player.isSitting() || player.getTeam() != TeamType.NONE || player.isInStoreMode())
					{
						return;
					}
					for(Skill reApplySkill : reApplySkills)
					{
						reApplySkill.getEffects(player, player, false, false);
					}
				}
			}, (long) _reApplyDelay * 1000);
		}
	}
	
	private double calcSkillChanceLimits(double prelimChance, boolean isPlayable)
	{
		if(_dispelType.equals("bane"))
		{
			if(prelimChance < 40.0)
			{
				return 40.0;
			}
			if(prelimChance > 90.0)
			{
				return 90.0;
			}
		}
		else
		{
			if(_dispelType.equals("cancellation"))
			{
				return Math.max((double) Config.SKILLS_DISPEL_MOD_MIN, Math.min((double) Config.SKILLS_DISPEL_MOD_MAX, prelimChance));
			}
			if(_dispelType.equals("cleanse"))
			{
				return _cancelRate;
			}
		}
		return prelimChance;
	}
	
	@Override
	protected boolean onActionTime()
	{
		return false;
	}
}