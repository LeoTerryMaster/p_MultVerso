package l2.gameserver.listener.actor.player;

import l2.gameserver.listener.PlayerListener;
import l2.gameserver.model.Player;
import l2.gameserver.model.quest.QuestState;

public interface OnQuestStateChangeListener extends PlayerListener
{
	void onQuestStateChange(Player player, QuestState questState);
}