package l2.gameserver.listener.actor.player;

import l2.gameserver.listener.PlayerListener;
import l2.gameserver.model.Player;

public interface OnPlayerPartyInviteListener extends PlayerListener
{
	void onPartyInvite(Player player);
}