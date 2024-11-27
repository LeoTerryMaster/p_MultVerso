package l2.gameserver.model.actor.listener;

import l2.commons.listener.Listener;
import l2.gameserver.listener.actor.npc.OnDecayListener;
import l2.gameserver.listener.actor.npc.OnSpawnListener;
import l2.gameserver.model.instances.NpcInstance;

public class NpcListenerList extends CharListenerList
{
	public NpcListenerList(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	public NpcInstance getActor()
	{
		return (NpcInstance) actor;
	}
	
	public void onSpawn()
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener listener : global.getListeners())
			{
				if(!OnSpawnListener.class.isInstance(listener))
					continue;
				((OnSpawnListener) listener).onSpawn(getActor());
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnSpawnListener.class.isInstance(listener))
					continue;
				((OnSpawnListener) listener).onSpawn(getActor());
			}
		}
	}
	
	public void onDecay()
	{
		if(!global.getListeners().isEmpty())
		{
			for(Listener listener : global.getListeners())
			{
				if(!OnDecayListener.class.isInstance(listener))
					continue;
				((OnDecayListener) listener).onDecay(getActor());
			}
		}
		if(!getListeners().isEmpty())
		{
			for(Listener listener : getListeners())
			{
				if(!OnDecayListener.class.isInstance(listener))
					continue;
				((OnDecayListener) listener).onDecay(getActor());
			}
		}
	}
}