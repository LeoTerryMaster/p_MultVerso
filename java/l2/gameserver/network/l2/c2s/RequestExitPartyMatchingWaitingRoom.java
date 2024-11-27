package l2.gameserver.network.l2.c2s;

import l2.gameserver.instancemanager.MatchingRoomManager;
import l2.gameserver.model.Player;

public class RequestExitPartyMatchingWaitingRoom extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		MatchingRoomManager.getInstance().removeFromWaitingList(player);
	}
}