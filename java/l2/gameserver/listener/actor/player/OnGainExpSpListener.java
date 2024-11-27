package l2.gameserver.listener.actor.player;

import l2.gameserver.listener.PlayerListener;
import l2.gameserver.model.Player;

public interface OnGainExpSpListener extends PlayerListener
{
	void onGainExpSp(Player player, long exp, long sp);
}