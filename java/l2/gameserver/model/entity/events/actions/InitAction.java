package l2.gameserver.model.entity.events.actions;

import l2.gameserver.model.entity.events.EventAction;
import l2.gameserver.model.entity.events.GlobalEvent;

public class InitAction implements EventAction
{
	private final String _name;
	
	public InitAction(String name)
	{
		_name = name;
	}
	
	@Override
	public void call(GlobalEvent event)
	{
		event.initAction(_name);
	}
}