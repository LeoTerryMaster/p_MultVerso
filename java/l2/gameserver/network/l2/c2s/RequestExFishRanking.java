package l2.gameserver.network.l2.c2s;

import l2.gameserver.Config;
import l2.gameserver.instancemanager.games.FishingChampionShipManager;
import l2.gameserver.model.Player;

public class RequestExFishRanking extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
		{
			FishingChampionShipManager.getInstance().showMidResult(getClient().getActiveChar());
		}
	}
}