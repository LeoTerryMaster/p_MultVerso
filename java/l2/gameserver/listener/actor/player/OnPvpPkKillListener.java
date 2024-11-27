package l2.gameserver.listener.actor.player;

import l2.gameserver.listener.PlayerListener;
import l2.gameserver.model.Player;

public interface OnPvpPkKillListener extends PlayerListener
{
	void onPvpPkKill(Player killer, Player victim, boolean isPk);
}