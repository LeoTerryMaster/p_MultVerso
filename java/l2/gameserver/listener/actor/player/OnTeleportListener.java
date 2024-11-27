package l2.gameserver.listener.actor.player;

import l2.gameserver.listener.PlayerListener;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.Reflection;

public interface OnTeleportListener extends PlayerListener
{
	void onTeleport(Player player, int x, int y, int z, Reflection reflection);
}