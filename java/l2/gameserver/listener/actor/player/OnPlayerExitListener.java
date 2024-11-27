package l2.gameserver.listener.actor.player;

import l2.gameserver.listener.PlayerListener;
import l2.gameserver.model.Player;

public interface OnPlayerExitListener extends PlayerListener
{
	void onPlayerExit(Player player);
}