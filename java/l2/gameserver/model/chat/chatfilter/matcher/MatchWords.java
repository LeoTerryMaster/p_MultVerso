package l2.gameserver.model.chat.chatfilter.matcher;

import l2.gameserver.model.Player;
import l2.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import l2.gameserver.network.l2.components.ChatType;

import java.util.regex.Pattern;

public class MatchWords implements ChatFilterMatcher
{
	public final Pattern[] _patterns;
	
	public MatchWords(String[] words)
	{
		_patterns = new Pattern[words.length];
		for(int i = 0;i < words.length;++i)
		{
			_patterns[i] = Pattern.compile(words[i], 66);
		}
	}
	
	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		for(Pattern p : _patterns)
		{
			if(!p.matcher(msg).find())
				continue;
			return true;
		}
		return false;
	}
}