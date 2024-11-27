package l2.gameserver.model.chat.chatfilter.matcher;

import l2.gameserver.model.Player;
import l2.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import l2.gameserver.network.l2.components.ChatType;

public class MatchLogicalXor implements ChatFilterMatcher
{
	private final ChatFilterMatcher[] _matches;
	
	public MatchLogicalXor(ChatFilterMatcher[] matches)
	{
		_matches = matches;
	}
	
	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		boolean match = false;
		for(ChatFilterMatcher m : _matches)
		{
			if(!m.isMatch(player, type, msg, recipient))
				continue;
			if(match)
			{
				return false;
			}
			match = true;
		}
		return match;
	}
}