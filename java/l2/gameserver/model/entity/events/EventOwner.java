package l2.gameserver.model.entity.events;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public abstract class EventOwner implements Serializable
{
	private final Set<GlobalEvent> _events = new HashSet<>(2);
	
	public <E extends GlobalEvent> E getEvent(Class<E> eventClass)
	{
		for(GlobalEvent e : _events)
		{
			if(e.getClass() == eventClass)
			{
				return (E) e;
			}
			if(!eventClass.isAssignableFrom(e.getClass()))
				continue;
			return (E) e;
		}
		return null;
	}
	
	public void addEvent(GlobalEvent event)
	{
		_events.add(event);
	}
	
	public void removeEvent(GlobalEvent event)
	{
		_events.remove(event);
	}
	
	public void removeEventsByClass(Class<? extends GlobalEvent> eventClass)
	{
		for(GlobalEvent e : _events)
		{
			if(e.getClass() == eventClass)
			{
				_events.remove(e);
				continue;
			}
			if(!eventClass.isAssignableFrom(e.getClass()))
				continue;
			_events.remove(e);
		}
	}
	
	public Set<GlobalEvent> getEvents()
	{
		return _events;
	}
}