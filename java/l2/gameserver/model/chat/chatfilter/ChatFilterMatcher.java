package l2.gameserver.model.chat.chatfilter;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.ChatType;

public interface ChatFilterMatcher
{
	boolean isMatch(Player player, ChatType type, String msg, Player recipient);
}