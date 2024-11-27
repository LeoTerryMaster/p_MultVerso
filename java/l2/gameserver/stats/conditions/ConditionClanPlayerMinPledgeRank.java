package l2.gameserver.stats.conditions;

import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.stats.Env;

public class ConditionClanPlayerMinPledgeRank extends Condition
{
	private final Player.EPledgeRank _minPledgeRank;
	
	public ConditionClanPlayerMinPledgeRank(String minPledgeRankName)
	{
		this(parsePledgeRank(minPledgeRankName));
	}
	
	public ConditionClanPlayerMinPledgeRank(Player.EPledgeRank minPledgeRank)
	{
		_minPledgeRank = minPledgeRank;
	}
	
	private static Player.EPledgeRank parsePledgeRank(String pledgeRankText)
	{
		Player.EPledgeRank pledgeRank = Player.EPledgeRank.valueOf(pledgeRankText.toUpperCase());
		if(pledgeRank == null)
		{
			throw new IllegalArgumentException("Unknown pledge rank \"" + pledgeRankText + "\"");
		}
		return pledgeRank;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		if(env.character == null)
		{
			return false;
		}
		Player player = env.character.getPlayer();
		if(player == null)
		{
			return false;
		}
		Clan clan = player.getClan();
		if(clan == null)
		{
			return false;
		}
		return player.getPledgeRank().getRankId() >= _minPledgeRank.getRankId();
	}
}