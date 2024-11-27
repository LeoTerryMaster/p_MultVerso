package l2.gameserver.listener.actor.player;

import l2.gameserver.listener.PlayerListener;
import l2.gameserver.model.Player;

public interface OnPlayerEnterListener extends PlayerListener
{
	void onPlayerEnter(Player player);
}