package handler.petition;

import l2.gameserver.handler.petition.IPetitionHandler;
import l2.gameserver.model.Player;

public class SimplePetitionHandler implements IPetitionHandler
{
	@Override
	public void handle(Player player, int id, String txt)
	{
		player.sendMessage(txt);
	}
}