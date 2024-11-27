package l2.gameserver.model.entity.events.actions;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.EventAction;
import l2.gameserver.model.entity.events.GlobalEvent;

public class GiveItemAction implements EventAction
{
	private final int _itemId;
	private final long _count;
	
	public GiveItemAction(int itemId, long count)
	{
		_itemId = itemId;
		_count = count;
	}
	
	@Override
	public void call(GlobalEvent event)
	{
		for(Player player : event.itemObtainPlayers())
		{
			event.giveItem(player, _itemId, _count);
		}
	}
}