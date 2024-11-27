package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.model.World;

public class RequestReload extends L2GameClientPacket
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
		player.sendUserInfo(true);
		World.showObjectsToPlayer(player);
	}
}