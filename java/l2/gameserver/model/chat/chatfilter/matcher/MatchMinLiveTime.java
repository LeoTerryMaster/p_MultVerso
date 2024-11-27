package l2.gameserver.model.chat.chatfilter.matcher;

import l2.gameserver.model.Player;
import l2.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import l2.gameserver.network.l2.components.ChatType;

public class MatchMinLiveTime implements ChatFilterMatcher
{
	private final long _createTime;
	
	public MatchMinLiveTime(int createTime)
	{
		_createTime = (long) createTime * 1000;
	}
	
	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		return System.currentTimeMillis() - player.getCreateTime() < _createTime;
	}
}