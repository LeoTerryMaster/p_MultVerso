package l2.gameserver.model.chat.chatfilter.matcher;

import l2.gameserver.model.Player;
import l2.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import l2.gameserver.network.l2.components.ChatType;

public class MatchChatChannels implements ChatFilterMatcher
{
	private final ChatType[] _channels;
	
	public MatchChatChannels(ChatType[] channels)
	{
		_channels = channels;
	}
	
	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		for(ChatType ct : _channels)
		{
			if(ct != type)
				continue;
			return true;
		}
		return false;
	}
}