package l2.gameserver.skills.effects;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntIterator;
import l2.commons.lang.reference.HardReference;
import l2.gameserver.listener.actor.OnCurrentHpDamageListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Env;

public final class EffectCurseOfLifeFlow extends Effect
{
	private final TObjectIntHashMap<HardReference<? extends Creature>> _damageList = new TObjectIntHashMap();
	private CurseOfLifeFlowListener _listener;
	
	public EffectCurseOfLifeFlow(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		_listener = new CurseOfLifeFlowListener();
		_effected.addListener(_listener);
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		_effected.removeListener(_listener);
		_listener = null;
	}
	
	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
		{
			return false;
		}
		TObjectIntIterator iterator = _damageList.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			Creature damager = (Creature) ((HardReference) iterator.key()).get();
			int damage;
			if(damager == null || damager.isDead() || damager.isCurrentHpFull() || (damage = iterator.value()) <= 0)
				continue;
			double max_heal = calc();
			double heal = Math.min((double) damage, max_heal);
			double newHp = Math.min(damager.getCurrentHp() + heal, (double) damager.getMaxHp());
			damager.sendPacket(new SystemMessage(1066).addNumber((long) (newHp - damager.getCurrentHp())));
			damager.setCurrentHp(newHp, false);
		}
		_damageList.clear();
		return true;
	}
	
	private class CurseOfLifeFlowListener implements OnCurrentHpDamageListener
	{
		@Override
		public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill)
		{
			if(attacker == actor || attacker == _effected)
			{
				return;
			}
			int old_damage;
			_damageList.put(attacker.getRef(), (old_damage = _damageList.get(attacker.getRef())) == 0 ? (int) damage : old_damage + (int) damage);
		}
	}
}