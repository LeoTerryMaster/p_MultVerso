package l2.gameserver.model.entity.events.actions;

import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.EventAction;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.network.l2.s2c.PlaySound;

import java.util.List;

public class PlaySoundAction implements EventAction
{
	private final int _range;
	private final String _sound;
	private final PlaySound.Type _type;
	
	public PlaySoundAction(int range, String s, PlaySound.Type type)
	{
		_range = range;
		_sound = s;
		_type = type;
	}
	
	@Override
	public void call(GlobalEvent event)
	{
		GameObject object = event.getCenterObject();
		PlaySound packet = object != null ? new PlaySound(_type, _sound, 1, object.getObjectId(), object.getLoc()) : new PlaySound(_type, _sound, 0, 0, 0, 0, 0);
		List<Player> players = event.broadcastPlayers(_range);
		for(Player player : players)
		{
			if(player == null)
				continue;
			player.sendPacket(packet);
		}
	}
}