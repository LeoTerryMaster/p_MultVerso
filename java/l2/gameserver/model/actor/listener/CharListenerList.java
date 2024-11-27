package l2.gameserver.model.actor.listener;

import l2.commons.listener.Listener;
import l2.commons.listener.ListenerList;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.listener.actor.OnAttackHitListener;
import l2.gameserver.listener.actor.OnAttackListener;
import l2.gameserver.listener.actor.OnCurrentHpDamageListener;
import l2.gameserver.listener.actor.OnCurrentMpReduceListener;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.listener.actor.OnKillListener;
import l2.gameserver.listener.actor.OnMagicHitListener;
import l2.gameserver.listener.actor.OnMagicUseListener;
import l2.gameserver.listener.actor.ai.OnAiEventListener;
import l2.gameserver.listener.actor.ai.OnAiIntentionListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;

public class CharListenerList extends ListenerList<Creature>
{
	static final ListenerList<Creature> global = new ListenerList();
	protected final Creature actor;
	
	public CharListenerList(Creature actor)
	{
		this.actor = actor;
	}
	
	public static final boolean addGlobal(Listener<Creature> listener)
	{
		return global.add(listener);
	}
	
	public static final boolean removeGlobal(Listener<Creature> listener)
	{
		return global.remove(listener);
	}
	
	public Creature getActor()
	{
		return actor;
	}
	
	public void onAiIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnAiIntentionListener.class.isInstance(listener))
					continue;
				((OnAiIntentionListener) listener).onAiIntention(getActor(), intention, arg0, arg1);
			}
		}
	}
	
	public void onAiEvent(CtrlEvent evt, Object[] args)
	{
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnAiEventListener.class.isInstance(listener))
					continue;
				((OnAiEventListener) listener).onAiEvent(getActor(), evt, args);
			}
		}
	}
	
	public void onAttack(Creature target)
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener<Creature> listener : global.getListeners())
			{
				if(!OnAttackListener.class.isInstance(listener))
					continue;
				((OnAttackListener) listener).onAttack(getActor(), target);
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnAttackListener.class.isInstance(listener))
					continue;
				((OnAttackListener) listener).onAttack(getActor(), target);
			}
		}
	}
	
	public void onAttackHit(Creature attacker)
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener<Creature> listener : global.getListeners())
			{
				if(!OnAttackHitListener.class.isInstance(listener))
					continue;
				((OnAttackHitListener) listener).onAttackHit(getActor(), attacker);
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnAttackHitListener.class.isInstance(listener))
					continue;
				((OnAttackHitListener) listener).onAttackHit(getActor(), attacker);
			}
		}
	}
	
	public void onMagicUse(Skill skill, Creature target, boolean alt)
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener<Creature> listener : global.getListeners())
			{
				if(!OnMagicUseListener.class.isInstance(listener))
					continue;
				((OnMagicUseListener) listener).onMagicUse(getActor(), skill, target, alt);
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnMagicUseListener.class.isInstance(listener))
					continue;
				((OnMagicUseListener) listener).onMagicUse(getActor(), skill, target, alt);
			}
		}
	}
	
	public void onMagicHit(Skill skill, Creature caster)
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener<Creature> listener : global.getListeners())
			{
				if(!OnMagicHitListener.class.isInstance(listener))
					continue;
				((OnMagicHitListener) listener).onMagicHit(getActor(), skill, caster);
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnMagicHitListener.class.isInstance(listener))
					continue;
				((OnMagicHitListener) listener).onMagicHit(getActor(), skill, caster);
			}
		}
	}
	
	public void onDeath(Creature killer)
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener<Creature> listener : global.getListeners())
			{
				if(!OnDeathListener.class.isInstance(listener))
					continue;
				((OnDeathListener) listener).onDeath(getActor(), killer);
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnDeathListener.class.isInstance(listener))
					continue;
				((OnDeathListener) listener).onDeath(getActor(), killer);
			}
		}
	}
	
	public void onKill(Creature victim)
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener<Creature> listener : global.getListeners())
			{
				if(!OnKillListener.class.isInstance(listener) || ((OnKillListener) listener).ignorePetOrSummon())
					continue;
				((OnKillListener) listener).onKill(getActor(), victim);
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnKillListener.class.isInstance(listener) || ((OnKillListener) listener).ignorePetOrSummon())
					continue;
				((OnKillListener) listener).onKill(getActor(), victim);
			}
		}
	}
	
	public void onKillIgnorePetOrSummon(Creature victim)
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener<Creature> listener : global.getListeners())
			{
				if(!OnKillListener.class.isInstance(listener) || !((OnKillListener) listener).ignorePetOrSummon())
					continue;
				((OnKillListener) listener).onKill(getActor(), victim);
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnKillListener.class.isInstance(listener) || !((OnKillListener) listener).ignorePetOrSummon())
					continue;
				((OnKillListener) listener).onKill(getActor(), victim);
			}
		}
	}
	
	public void onCurrentHpDamage(double damage, Creature attacker, Skill skill)
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener<Creature> listener : global.getListeners())
			{
				if(!OnCurrentHpDamageListener.class.isInstance(listener))
					continue;
				((OnCurrentHpDamageListener) listener).onCurrentHpDamage(getActor(), damage, attacker, skill);
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnCurrentHpDamageListener.class.isInstance(listener))
					continue;
				((OnCurrentHpDamageListener) listener).onCurrentHpDamage(getActor(), damage, attacker, skill);
			}
		}
	}
	
	public void onCurrentMpReduce(double consumed, Creature attacker)
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener<Creature> listener : global.getListeners())
			{
				if(!OnCurrentMpReduceListener.class.isInstance(listener))
					continue;
				((OnCurrentMpReduceListener) listener).onCurrentMpReduce(getActor(), consumed, attacker);
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnCurrentMpReduceListener.class.isInstance(listener))
					continue;
				((OnCurrentMpReduceListener) listener).onCurrentMpReduce(getActor(), consumed, attacker);
			}
		}
	}
}