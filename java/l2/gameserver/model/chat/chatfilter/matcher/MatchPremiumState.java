package l2.gameserver.model.chat.chatfilter.matcher;

import l2.gameserver.model.Player;
import l2.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import l2.gameserver.network.l2.components.ChatType;

public class MatchPremiumState implements ChatFilterMatcher
{
	private final boolean _excludePremium;
	
	public MatchPremiumState(boolean premiumState)
	{
		_excludePremium = premiumState;
	}
	
	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		return _excludePremium || !player.hasBonus();
	}
}