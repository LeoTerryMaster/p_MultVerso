package l2.gameserver.listener.actor.player.impl;

import l2.commons.lang.reference.HardReference;
import l2.gameserver.listener.actor.player.OnAnswerListener;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.utils.Location;

public class SummonAnswerListener implements OnAnswerListener
{
	private final HardReference<Player> _playerRef;
	private final Location _location;
	private final long _count;
	
	public SummonAnswerListener(Player player, Location loc, long count)
	{
		_playerRef = player.getRef();
		_location = loc;
		_count = count;
	}
	
	@Override
	public void sayYes()
	{
		Player player = _playerRef.get();
		if(player == null)
		{
			return;
		}
		player.abortAttack(true, true);
		player.abortCast(true, true);
		player.stopMove();
		if(_count > 0)
		{
			if(player.getInventory().destroyItemByItemId(8615, _count))
			{
				player.sendPacket(SystemMessage2.removeItems(8615, _count));
				player.teleToLocation(_location);
			}
			else
			{
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			}
		}
		else
		{
			player.teleToLocation(_location);
		}
	}
	
	@Override
	public void sayNo()
	{
	}
}