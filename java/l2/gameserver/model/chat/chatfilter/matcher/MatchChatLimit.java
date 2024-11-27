package l2.gameserver.model.chat.chatfilter.matcher;

import l2.gameserver.model.Player;
import l2.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import l2.gameserver.model.chat.chatfilter.ChatMsg;
import l2.gameserver.network.l2.components.ChatType;

import java.util.Deque;
import java.util.Iterator;

public class MatchChatLimit implements ChatFilterMatcher
{
	private final int _limitCount;
	private final int _limitTime;
	private final int _limitBurst;
	
	public MatchChatLimit(int limitCount, int limitTime, int limitBurst)
	{
		_limitCount = limitCount;
		_limitTime = limitTime;
		_limitBurst = limitBurst;
	}
	
	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		int currentTime;
		int firstMsgTime = currentTime = (int) (System.currentTimeMillis() / 1000);
		int count = 0;
		Deque<ChatMsg> msgBucket = player.getMessageBucket();
		Iterator<ChatMsg> itr = msgBucket.descendingIterator();
		while(itr.hasNext())
		{
			ChatMsg cm = itr.next();
			if(cm.chatType != type)
				continue;
			firstMsgTime = cm.time;
			if(_limitBurst != ++count)
				continue;
			break;
		}
		count -= (currentTime - firstMsgTime) / _limitTime * _limitCount;
		return _limitBurst <= count;
	}
}