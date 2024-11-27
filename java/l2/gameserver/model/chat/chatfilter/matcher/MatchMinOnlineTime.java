package l2.gameserver.model.chat.chatfilter.matcher;

import l2.gameserver.model.Player;
import l2.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import l2.gameserver.network.l2.components.ChatType;

public class MatchMinOnlineTime implements ChatFilterMatcher
{
	private final long _onlineTime;
	
	public MatchMinOnlineTime(int onlineTime)
	{
		_onlineTime = (long) onlineTime * 1000;
	}
	
	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		return player.getOnlineTime() < _onlineTime;
	}
}