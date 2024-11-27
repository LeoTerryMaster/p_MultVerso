package l2.gameserver.model.chat.chatfilter.matcher;

import l2.gameserver.model.Player;
import l2.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.utils.MapUtils;

public class MatchMaps implements ChatFilterMatcher
{
	private final int[] _maps;
	
	public MatchMaps(int[] maps)
	{
		_maps = maps;
	}
	
	@Override
	public boolean isMatch(Player player, ChatType type, String msg, Player recipient)
	{
		int rx = MapUtils.regionX(player);
		int ry = MapUtils.regionY(player);
		for(int i = 0;i < _maps.length;i += 2)
		{
			int mx = _maps[i];
			int my = _maps[i + 1];
			if(mx != rx || my != ry)
				continue;
			return true;
		}
		return false;
	}
}