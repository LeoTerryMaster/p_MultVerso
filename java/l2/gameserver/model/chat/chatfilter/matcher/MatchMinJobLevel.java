package l2.gameserver.model.chat.chatfilter.matcher;

import l2.gameserver.model.Player;
import l2.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import l2.gameserver.network.l2.components.ChatType;

public class MatchMinJobLevel implements ChatFilterMatcher
{
	private final int _classLevel;
	
	public MatchMinJobLevel(int classLevel)
	{
		_classLevel = classLevel;
	}
	
	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		return player.getClassId().level() < _classLevel;
	}
}