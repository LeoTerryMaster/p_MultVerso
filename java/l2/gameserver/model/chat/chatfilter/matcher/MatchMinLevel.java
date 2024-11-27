package l2.gameserver.model.chat.chatfilter.matcher;

import l2.gameserver.model.Player;
import l2.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import l2.gameserver.network.l2.components.ChatType;

public class MatchMinLevel implements ChatFilterMatcher
{
	private final int _level;
	
	public MatchMinLevel(int level)
	{
		_level = level;
	}
	
	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		return player.getLevel() < _level;
	}
}