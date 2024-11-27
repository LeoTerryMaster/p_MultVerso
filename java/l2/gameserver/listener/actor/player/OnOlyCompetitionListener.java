package l2.gameserver.listener.actor.player;

import l2.gameserver.listener.PlayerListener;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.oly.Competition;

public interface OnOlyCompetitionListener extends PlayerListener
{
	void onOlyCompetitionCompleted(Player player, Competition competition, boolean isWin);
}