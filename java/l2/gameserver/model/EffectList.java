package l2.gameserver.model;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import l2.gameserver.Config;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.skills.EffectType;
import l2.gameserver.skills.effects.EffectTemplate;
import l2.gameserver.skills.skillclasses.Transformation;
import l2.gameserver.stats.Stats;
import l2.gameserver.stats.funcs.FuncTemplate;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EffectList
{
	public static final int NONE_SLOT_TYPE = -1;
	public static final int BUFF_SLOT_TYPE = 0;
	public static final int MUSIC_SLOT_TYPE = 1;
	public static final int TRIGGER_SLOT_TYPE = 2;
	public static final int DEBUFF_SLOT_TYPE = 3;
	public static final int DEBUFF_LIMIT = 8;
	public static final int MUSIC_LIMIT = 12;
	public static final int TRIGGER_LIMIT = 12;
	private final Creature _actor;
	private final Lock lock = new ReentrantLock();
	private List<Effect> _effects;
	
	public EffectList(Creature owner)
	{
		_actor = owner;
	}
	
	public static int getSlotType(Effect e)
	{
		if(e.getSkill().isPassive() || e.getSkill().isSlotNone() || e.getSkill().isToggle() || e.getSkill() instanceof Transformation || e.isStackTypeMatch("HpRecoverCast") || e.getEffectType() == EffectType.Cubic)
		{
			return -1;
		}
		if(e.getSkill().isOffensive())
		{
			return 3;
		}
		if(e.getSkill().isTrigger())
		{
			return 2;
		}
		return 0;
	}
	
	public static boolean checkStackType(EffectTemplate ef1, EffectTemplate ef2)
	{
		if(!ef1._stackType.equals("none") && ef1._stackType.equalsIgnoreCase(ef2._stackType))
		{
			return true;
		}
		if(!ef1._stackType.equals("none") && ef1._stackType.equalsIgnoreCase(ef2._stackType2))
		{
			return true;
		}
		if(!ef1._stackType2.equals("none") && ef1._stackType2.equalsIgnoreCase(ef2._stackType))
		{
			return true;
		}
		return !ef1._stackType2.equals("none") && ef1._stackType2.equalsIgnoreCase(ef2._stackType2);
	}
	
	public int getEffectsCountForSkill(int skill_id)
	{
		if(isEmpty())
		{
			return 0;
		}
		int count = 0;
		for(Effect e : _effects)
		{
			if(e.getSkill().getId() != skill_id)
				continue;
			++count;
		}
		return count;
	}
	
	public Effect getEffectByType(EffectType et)
	{
		if(isEmpty())
		{
			return null;
		}
		for(Effect e : _effects)
		{
			if(e.getEffectType() != et)
				continue;
			return e;
		}
		return null;
	}
	
	public List<Effect> getEffectsBySkill(Skill skill)
	{
		if(skill == null)
		{
			return null;
		}
		return getEffectsBySkillId(skill.getId());
	}
	
	public int getActiveMusicCount(int skillId)
	{
		if(isEmpty())
		{
			return 0;
		}
		int count = 0;
		for(Effect e : _effects)
		{
			if(!Config.ALT_ADDITIONAL_DANCE_SONG_MANA_CONSUME || !e.getSkill().isMusic() || e.getSkill().getId() == skillId || e.getTimeLeft() <= Config.ALT_MUSIC_COST_GUARD_INTERVAL)
				continue;
			++count;
		}
		return count;
	}
	
	public List<Effect> getEffectsBySkillId(int skillId)
	{
		if(isEmpty())
		{
			return null;
		}
		ArrayList<Effect> list = new ArrayList<>(2);
		for(Effect e : _effects)
		{
			if(e.getSkill().getId() != skillId)
				continue;
			list.add(e);
		}
		return list.isEmpty() ? null : list;
	}
	
	public Effect getEffectByIndexAndType(int skillId, EffectType type)
	{
		if(isEmpty())
		{
			return null;
		}
		for(Effect e : _effects)
		{
			if(e.getSkill().getId() != skillId || e.getEffectType() != type)
				continue;
			return e;
		}
		return null;
	}
	
	public Effect getEffectByStackType(String type)
	{
		if(isEmpty())
		{
			return null;
		}
		for(Effect e : _effects)
		{
			if(!e.getStackType().equals(type))
				continue;
			return e;
		}
		return null;
	}
	
	public boolean containEffectFromSkills(int... skillIds)
	{
		if(isEmpty())
		{
			return false;
		}
		for(Effect e : _effects)
		{
			int skillId = e.getSkill().getId();
			if(!ArrayUtils.contains(skillIds, skillId))
				continue;
			return true;
		}
		return false;
	}
	
	public List<Effect> getAllEffects()
	{
		if(isEmpty())
		{
			return Collections.emptyList();
		}
		return new ArrayList<>(_effects);
	}
	
	public boolean isEmpty()
	{
		return _effects == null || _effects.isEmpty();
	}
	
	public Effect[] getAllFirstEffects()
	{
		if(isEmpty())
		{
			return Effect.EMPTY_L2EFFECT_ARRAY;
		}
		LinkedHashMap<Skill, Effect> map = new LinkedHashMap<>();
		for(Effect e : _effects)
		{
			if(map.containsKey(e.getSkill()))
				continue;
			map.put(e.getSkill(), e);
		}
		Collection<Effect> coll = map.values();
		return coll.toArray(new Effect[coll.size()]);
	}
	
	private void checkSlotLimit(Effect newEffect)
	{
		if(_effects == null)
		{
			return;
		}
		int slotType = getSlotType(newEffect);
		if(slotType == -1)
		{
			return;
		}
		int size = 0;
		TIntArrayList skillIds = new TIntArrayList();
		for(Effect e : _effects)
		{
			if(!e.isInUse())
				continue;
			if(e.getSkill().equals(newEffect.getSkill()))
			{
				return;
			}
			if(skillIds.contains(e.getSkill().getId()) || getSlotType(e) != slotType)
				continue;
			++size;
			skillIds.add(e.getSkill().getId());
		}
		int limit = 0;
		switch(slotType)
		{
			case 0:
			{
				limit = _actor.getBuffLimit();
				break;
			}
			case 3:
			{
				limit = Config.ALT_DEBUFF_LIMIT;
				break;
			}
			case 2:
			{
				limit = 12 + Config.ALT_TRIGGER_SLOT_ADDER;
			}
		}
		if(size < limit)
		{
			return;
		}
		int skillId = 0;
		for(Effect e : _effects)
		{
			if(!e.isInUse() || getSlotType(e) != slotType)
				continue;
			skillId = e.getSkill().getId();
			break;
		}
		if(skillId != 0)
		{
			stopEffect(skillId);
		}
	}
	
	public void addEffect(Effect effect)
	{
		double hp = _actor.getCurrentHp();
		double mp = _actor.getCurrentMp();
		double cp = _actor.getCurrentCp();
		String stackType = effect.getStackType();
		HashSet<Skill> removed = new HashSet<>();
		lock.lock();
		boolean add;
		try
		{
			if(_effects == null)
			{
				_effects = new CopyOnWriteArrayList<>();
			}
			if(stackType.equals("none"))
			{
				for(Effect e : _effects)
				{
					if(!e.isInUse())
						continue;
					if(e.getSkill().getId() != effect.getSkill().getId() || e.getEffectType() != effect.getEffectType() || !e.getStackType().equals("none"))
						continue;
					if(effect.getTimeLeft() > e.getTimeLeft())
					{
						removed.add(e.getSkill());
						e.exit();
						continue;
					}
					return;
				}
			}
			else
			{
				for(Effect e : _effects)
				{
					if(!e.isInUse() || !checkStackType(e.getTemplate(), effect.getTemplate()))
						continue;
					if(e.getSkill().getId() != effect.getSkill().getId() || e.getEffectType() == effect.getEffectType())
					{
						if(e.getStackOrder() == -1)
						{
							return;
						}
						if(!e.maybeScheduleNext(effect))
						{
							return;
						}
						removed.add(e.getSkill());
						continue;
					}
					break;
				}
			}
			checkSlotLimit(effect);
			add = _effects.add(effect);
			if(add)
			{
				effect.setInUse(true);
			}
		}
		finally
		{
			lock.unlock();
		}
		if(!add)
		{
			return;
		}
		if(!removed.isEmpty())
		{
			for(Skill s : removed)
			{
				effect.getEffected().sendPacket(new SystemMessage(92).addSkillName(s.getDisplayId(), s.getDisplayLevel()));
			}
		}
		effect.start();
		for(FuncTemplate ft : effect.getTemplate().getAttachedFuncs())
		{
			if(ft._stat == Stats.MAX_HP)
			{
				_actor.setCurrentHp(hp, false);
				continue;
			}
			if(ft._stat == Stats.MAX_MP)
			{
				_actor.setCurrentMp(mp);
				continue;
			}
			if(ft._stat != Stats.MAX_CP)
				continue;
			_actor.setCurrentCp(cp);
		}
		_actor.updateStats();
		_actor.updateEffectIcons();
	}
	
	public void removeEffect(Effect effect)
	{
		if(effect == null)
		{
			return;
		}
		lock.lock();
		boolean remove;
		try
		{
			if(_effects == null)
			{
				return;
			}
			remove = _effects.remove(effect);
			if(!remove)
			{
				return;
			}
		}
		finally
		{
			lock.unlock();
		}
		if(!remove)
		{
			return;
		}
		_actor.updateStats();
		_actor.updateEffectIcons();
	}
	
	public void stopAllEffects()
	{
		if(isEmpty())
		{
			return;
		}
		lock.lock();
		try
		{
			for(Effect e : _effects)
			{
				e.exit();
			}
		}
		finally
		{
			lock.unlock();
		}
		_actor.updateStats();
		_actor.updateEffectIcons();
	}
	
	public void stopEffect(int skillId)
	{
		if(isEmpty())
		{
			return;
		}
		for(Effect e : _effects)
		{
			if(e.getSkill().getId() != skillId)
				continue;
			e.exit();
		}
	}
	
	public void stopEffect(Skill skill)
	{
		if(skill != null)
		{
			stopEffect(skill.getId());
		}
	}
	
	public void stopEffectByDisplayId(int skillId)
	{
		if(isEmpty())
		{
			return;
		}
		for(Effect e : _effects)
		{
			if(e.getSkill().getDisplayId() != skillId)
				continue;
			e.exit();
		}
	}
	
	public void stopEffects(EffectType type)
	{
		if(isEmpty())
		{
			return;
		}
		for(Effect e : _effects)
		{
			if(e.getEffectType() != type)
				continue;
			e.exit();
		}
	}
	
	public void stopAllSkillEffects(EffectType type)
	{
		if(isEmpty())
		{
			return;
		}
		TIntHashSet skillIds = new TIntHashSet();
		for(Effect e : _effects)
		{
			if(e.getEffectType() != type)
				continue;
			skillIds.add(e.getSkill().getId());
		}
		for(int skillId : skillIds.toArray())
		{
			stopEffect(skillId);
		}
	}
}